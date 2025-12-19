package com.hjm.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.constant.OrderConstant;
import com.hjm.constant.RedisConstants;
import com.hjm.context.PatientContext;
import com.hjm.context.UserPatientContext;
import com.hjm.exception.AppiontmentOrderException;
import com.hjm.exception.OrderBusinessException;
import com.hjm.mapper.AppointmentOrderMapper;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.pojo.Entity.OrderInfo;
import com.hjm.mapper.OrderInfoMapper;
import com.hjm.pojo.VO.OrderCreateVO;
import com.hjm.pojo.VO.OrderPageVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IDoctorScheduleService;
import com.hjm.service.IOrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.hjm.constant.OrderConstant.ORDER_DEFAULT_REMARK;

/**
 * <p>
 * 挂号系统订单表（先订单，再挂号） 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-11-21
 */
@Service
@RequiredArgsConstructor
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final OrderInfoMapper orderInfoMapper;
    private final AppointmentOrderMapper appointmentOrderMapper;
    private final IDoctorScheduleService doctorScheduleService;
    private final RedissonClient redissonClient;
    @Override
    @Transactional
    public Result createOrder(Integer scheduleId, Long patientId) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setScheduleId(scheduleId.longValue());
        Long id = patientId;
        orderInfo.setPatientId(id);
        Long uid = com.hjm.context.UserPatientContext.get() == null ? null : UserPatientContext.get().getId();
        orderInfo.setUserPatientId(uid);
        // 2. 防止重复下单（同一患者同一号源未支付订单存在）
        boolean exists = orderInfoMapper.existsUnpaidOrder(id, scheduleId.longValue());
        if (exists) {
            throw new OrderBusinessException("已有未支付订单，请先支付或等待过期");
        }
        RLock orderLock = redissonClient.getLock(OrderConstant.LOCK_ORDER_CREATE + scheduleId);
        orderLock.lock();
        try {
            if (!orderLock.isHeldByCurrentThread()) {
                throw new OrderBusinessException("网络问题，请稍后重试");
            }
            orderInfo.setStatus(OrderConstant.TO_BE_PAID);
            // 生成订单号`
            String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String redisKey = "order:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Long incr = stringRedisTemplate.opsForValue().increment(redisKey);
            String orderNo = prefix + String.format("%06d", incr);
            // 获取价格
            String scheduleType = doctorScheduleService.getById(scheduleId).getScheduleType();
            Long fee = appointmentOrderMapper.getFee(scheduleType);
            orderInfo.setAmount(BigDecimal.valueOf(fee));
            //设置过期时间
            orderInfo.setExpireTime(LocalDateTime.now().plusMinutes(OrderConstant.DEFAULT_EXPIRE_TIME));
            //设置备注
            orderInfo.setRemark(ORDER_DEFAULT_REMARK);
            // 保存
            orderInfo.setOrderNo(orderNo);
            save(orderInfo);
            DoctorSchedule s = doctorScheduleService.getById(scheduleId);
            LocalTime now = LocalTime.now();
            LocalDate scheduleDate = LocalDate.now();
//            if (scheduleDate.equals(s.getScheduleDate()) || now.isAfter(s.getEndTime())) {
//                throw new AppiontmentOrderException("号源已过期");
//            }
            // 扣减号源
            Boolean success = doctorScheduleService.update()
                    .setSql("current_appointments = current_appointments + 1")
                    .lt("current_appointments", s.getMaxAppointments())
                    .eq("id", scheduleId)
                    .update();
            if (!success) {
                doctorScheduleService.update(
                        ).setSql("status = FULL")
                        .eq("id", scheduleId)
                        .update();
                throw new AppiontmentOrderException("挂号已满");
            }
            OrderCreateVO orderCreateVO = new OrderCreateVO();
            orderCreateVO.setOrderNo(orderNo);
            orderCreateVO.setAmount(orderInfo.getAmount());
            orderCreateVO.setExpireTime(orderInfo.getExpireTime());
            orderCreateVO.setRemark(orderInfo.getRemark());
            orderCreateVO.setStatus(String.valueOf(orderInfo.getStatus()));
            orderCreateVO.setScheduleId(orderInfo.getScheduleId());
            return Result.success(orderCreateVO);
        } finally {
            orderLock.unlock();
        }
}

    @Override
    public OrderInfo getByOrderNo(String orderNo) {
        return orderInfoMapper.getByOrderNo(orderNo);
    }

    @Override
    public List<OrderInfo> listByPId(Long userPatientId, Integer status, LocalDate startDate) {
        return orderInfoMapper.listByPId(userPatientId, status, startDate);
    }

    @Override
    public Result<PageResult> listByPage(String orderNo, Integer status, LocalDate startDate, LocalDate endDate, Integer page, Integer size, String patientName) {

        Page<OrderInfo> pageParam = new Page<>(page, size);
        Page<OrderPageVO> pageResult = orderInfoMapper.listByPage(pageParam, orderNo, status, startDate, endDate, patientName);
        return Result.success(new PageResult(pageResult.getTotal(), pageResult.getRecords()));
    }

    @Override
    public void cleanOutTimeOrder(LocalDateTime now) {

        orderInfoMapper.cleanOutTimeOrder(now);

    }
}
