package com.hjm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.constant.OrderConstant;
import com.hjm.context.PatientContext;
import com.hjm.context.UserPatientContext;
import com.hjm.exception.AppiontmentOrderException;
import com.hjm.mapper.AppointmentOrderMapper;
import com.hjm.mapper.DoctorScheduleMapper;
import com.hjm.pojo.DTO.AppointmentOrderPageDTO;
import com.hjm.pojo.DTO.PaymentDTO;
import com.hjm.pojo.DTO.RegistrationCreateDTO;
import com.hjm.pojo.DTO.RegistrationDTO;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.pojo.Entity.OrderInfo;
import com.hjm.pojo.VO.AppointmentOrderPageVO;
import com.hjm.pojo.VO.RegistrationVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IAppointmentOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.service.IDoctorScheduleService;
import com.hjm.service.IOrderInfoService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 挂号订单表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
@RequiredArgsConstructor
public class AppointmentOrderServiceImpl extends ServiceImpl<AppointmentOrderMapper, AppointmentOrder> implements IAppointmentOrderService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final AppointmentOrderMapper appointmentOrderMapper;
    private final DoctorScheduleMapper doctorScheduleMapper;
    private final IDoctorScheduleService doctorScheduleService;
    private final IOrderInfoService orderInfoService;
    private final RedissonClient redissonClient;
//    @Override
//    @Transactional
//    public Result<Map<String,Object>> createRegistration(RegistrationCreateDTO registrationCreateDTO) {
//
//        if (registrationCreateDTO == null) {
//            throw new AppiontmentOrderException("参数不能为空");
//        }
//        if (registrationCreateDTO.getDepartmentId() == null) {
//            throw new AppiontmentOrderException("请选择科室");
//        }
//        if (registrationCreateDTO.getDoctorId() == null) {
//            throw new AppiontmentOrderException("请选择医生");
//        }
//        if (registrationCreateDTO.getRegistrationDate() == null) {
//            throw new AppiontmentOrderException("请选择预约日期");
//        }
//        if (registrationCreateDTO.getTimeSlot() == null) {
//            throw new AppiontmentOrderException("请选择预约时段");
//        }
//        if (registrationCreateDTO.getScheduleType() == null) {
//            throw new AppiontmentOrderException("请选择预约类型");
//        }
//        if (registrationCreateDTO.getPatientPhone() == null){
//            throw new AppiontmentOrderException("请填写手机号");
//        };
//        AppointmentOrder appointmentOrder = new AppointmentOrder();
//        BeanUtil.copyProperties(registrationCreateDTO, appointmentOrder);
//        Long patientId = PatientContext.getPatient().getId();
//        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//        String key = "order:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        Long increment = stringRedisTemplate.opsForValue().increment(key);
//        String orderNo = prefix + String.format("%06d", increment);
//        appointmentOrder.setOrderNo(orderNo);
//        appointmentOrder.setPatientId(patientId);
//        LocalDate appointmentDate = LocalDate.parse(registrationCreateDTO.getRegistrationDate());
//        appointmentOrder.setAppointmentDate(appointmentDate);
//        Long fee = appointmentOrderMapper.getFee(registrationCreateDTO.getScheduleType());
//        appointmentOrder.setFee(BigDecimal.valueOf(fee));
//        Integer max = doctorScheduleService.getById(registrationCreateDTO.getScheduleId()).getMaxAppointments();
//        Boolean success = doctorScheduleService.update()
//                .setSql("current_appointments= current_appointments + 1 ")
//                .lt("current_appointments", max)
//                .eq("id",registrationCreateDTO.getScheduleId() )
//                .update();
//        if (!success) {
//            throw new AppiontmentOrderException("挂号已满了");
//        }
//        else {
//            boolean save = this.save(appointmentOrder);
//            if (!save) {
//                throw new AppiontmentOrderException("挂号失败");
//            }
//            Map<String, Object> data = new HashMap<>();
//            data.put("orderNo", orderNo);
//            data.put("message", "挂号成功");
//            return Result.success(data);
//        }
//    }

    /**
     * 挂号入口（负责校验参数 + 并发控制）
     */
    @Override
    public Result<Map<String,Object>> createRegistration(RegistrationCreateDTO registrationCreateDTO) {

        // 1. 参数校验
        if (registrationCreateDTO == null) {
            throw new AppiontmentOrderException("参数不能为空");
        }
        if (registrationCreateDTO.getDepartmentId() == null) {
            throw new AppiontmentOrderException("请选择科室");
        }
        if (registrationCreateDTO.getDoctorId() == null) {
            throw new AppiontmentOrderException("请选择医生");
        }
        if (registrationCreateDTO.getRegistrationDate() == null) {
            throw new AppiontmentOrderException("请选择预约日期");
        }
        if (registrationCreateDTO.getTimeSlot() == null) {
            throw new AppiontmentOrderException("请选择预约时段");
        }
        if (registrationCreateDTO.getScheduleType() == null) {
            throw new AppiontmentOrderException("请选择预约类型");
        }
        if (registrationCreateDTO.getPatientPhone() == null){
            throw new AppiontmentOrderException("请填写手机号");
        };
        // 2. 查询排班信息判断是否存在
        DoctorSchedule schedule = doctorScheduleService.getById(registrationCreateDTO.getScheduleId());
        if (schedule == null) {
            throw new AppiontmentOrderException("排班不存在");
        }

        // 3. 判断是否可挂号（时间、状态等可自行扩展）
        // Example: 判断 maxAppointments > currentAppointments
        if (schedule.getCurrentAppointments() >= schedule.getMaxAppointments()) {
            throw new AppiontmentOrderException("当前排班已满");
        }

        // 4. 当前患者ID
        Long patientId = registrationCreateDTO.getPatientId();
        // 5. 给当前患者加分布式锁，防止并发重复挂号
        RLock lock = redissonClient.getLock("lock:appointment:" + patientId);
        Boolean locked = lock.tryLock();
        if (!locked) {
            throw new AppiontmentOrderException("请勿重复挂号");
        }
        synchronized (patientId.toString().intern()) {

            // 6. 获取当前对象的代理，确保事务生效（
            try {
                IAppointmentOrderService proxy =
                    (IAppointmentOrderService) AopContext.currentProxy();
                // 7. 调用事务方法
                return proxy.createRegistrationOrder(registrationCreateDTO);
            }finally {
                lock.unlock();
            }
        }
    }

    @Override
    public AppointmentOrder getByOrderNo(String orderNo) {
        return appointmentOrderMapper.getByOrderNo(orderNo);
    }

    @Override
    public List<RegistrationVO> listByPId(Long userId) {
        return appointmentOrderMapper.listByPId(userId);
    }

    @Override
    public Result<PageResult> listByPage(AppointmentOrderPageDTO appointmentOrderPageDTO) {

        Page<AppointmentOrder> page = new Page<>(appointmentOrderPageDTO.getPage(), appointmentOrderPageDTO.getSize());
        Page<AppointmentOrderPageVO> pageResult = appointmentOrderMapper.getByPage(page, appointmentOrderPageDTO);
        return Result.success(new PageResult(pageResult.getTotal(),pageResult.getRecords() ));
    }

    @Override
    @Transactional
    public Result createPayment(PaymentDTO paymentDTO) {
        OrderInfo orderInfo = orderInfoService.getByOrderNo(paymentDTO.getOrderNo());
        if (orderInfo == null) {
            throw new AppiontmentOrderException("订单不存在");
        }
        if (orderInfo.getStatus() != 0) {
            throw new AppiontmentOrderException("订单已支付");
        }
        orderInfo.setStatus(1);
        orderInfo.setPayTime(LocalDateTime.now());
        if (paymentDTO.getPayWay().equals("WECHAT")){
            orderInfo.setPayWay(0);
        }
        else if (paymentDTO.getPayWay().equals("ALIPAY")){
            orderInfo.setPayWay(1);
        }
        orderInfo.setAmount(orderInfo.getAmount());
        orderInfo.setRemark(OrderConstant.ORDER_PAID_REMARK);
        boolean update = orderInfoService.updateById(orderInfo);
        if (!update) {
            throw new AppiontmentOrderException("支付失败");
        }
        DoctorSchedule schedule = doctorScheduleService.getById(orderInfo.getScheduleId());
        RegistrationCreateDTO registrationCreateDTO = new RegistrationCreateDTO();
        registrationCreateDTO.setScheduleId(orderInfo.getScheduleId());
        com.hjm.service.IPatientService ps = com.hjm.utils.SpringUtils.getBean(com.hjm.service.IPatientService.class);
        com.hjm.pojo.Entity.Patient p = ps.getById(orderInfo.getPatientId());
        registrationCreateDTO.setPatientPhone(p == null ? null : p.getPhone());
        registrationCreateDTO.setScheduleType(schedule.getScheduleType());
        registrationCreateDTO.setRegistrationDate(schedule.getScheduleDate());
        registrationCreateDTO.setTimeSlot(schedule.getTimeSlot()+" "+schedule.getStartTime()+" "+schedule.getEndTime());
        registrationCreateDTO.setDepartmentId(schedule.getDepartmentId());
        registrationCreateDTO.setDoctorId(schedule.getDoctorId());
        registrationCreateDTO.setPatientId(orderInfo.getPatientId());
        return createRegistration(registrationCreateDTO);
    }

    @Override
    public List<Long> listPatientIdsByScheduleId(Long scheduleId) {
        return appointmentOrderMapper.listPatientIdsByScheduleId(scheduleId);
    }

    @Override
    public Result cancel(RegistrationDTO registrationDTO) {
        Long userPatientId = UserPatientContext.get().getId();
        AppointmentOrder appointmentOrder = getByOrderNo(registrationDTO.getRegistrationNo());
        if (appointmentOrder == null) {
            throw new AppiontmentOrderException("号源不存在");
        }
        if (appointmentOrder.getUserPatientId() != userPatientId) {
            throw new AppiontmentOrderException("号源不属于当前用户");
        }
        if (appointmentOrder.getStatus() != 0) {
            throw new AppiontmentOrderException("号源状态异常");
        }
        boolean update = update()
                .set("status", 2)
                .eq("id", appointmentOrder.getId())
                .update();
        DoctorSchedule doctorSchedule = doctorScheduleService.getById(appointmentOrder.getScheduleId());
        Integer currentAppointments = doctorSchedule.getCurrentAppointments();
        currentAppointments--;
        doctorSchedule.setCurrentAppointments(currentAppointments);
        doctorScheduleService.updateById(doctorSchedule);
        if (!update) {
            throw new AppiontmentOrderException("取消失败");
        }
        return Result.success("取消成功");
    }

    @Override
    public void cleanOutTimeReg(LocalDateTime now) {
        appointmentOrderMapper.cleanOutTimeReg(now);
    }


    /**
     * 创建挂号订单（事务方法）
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createRegistrationOrder(RegistrationCreateDTO dto) {
        Long patientId = dto.getPatientId();
        Long count = query().eq("patient_id", patientId).eq("schedule_id", dto.getScheduleId()).eq("status", 0).count();
        // 若已存在订单则返回失败
        if (count > 0) {
           throw new AppiontmentOrderException ("挂过该号!!!");
        }
        // 1. 构造订单对象
        AppointmentOrder order = new AppointmentOrder();
        BeanUtil.copyProperties(dto, order);
        order.setPatientId(patientId);
        order.setAppointmentDate(dto.getRegistrationDate());
        Long uid = com.hjm.context.UserPatientContext.get() == null ? null : com.hjm.context.UserPatientContext.get().getId();
        order.setUserPatientId(uid);
        // 2. 生成挂号号
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String redisKey = "registration:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long incr = stringRedisTemplate.opsForValue().increment(redisKey);
        String registrationNo = prefix + String.format("%06d", incr);
        order.setRegistrationNo(registrationNo);
        // 3. 查询费用
        Long fee = appointmentOrderMapper.getFee(dto.getScheduleType());
        order.setFee(BigDecimal.valueOf(fee));
        // 4. 扣减号源（乐观锁）
        Long scheduleId = dto.getScheduleId();
//        DoctorSchedule s = doctorScheduleService.getById(scheduleId);
//        Boolean success = doctorScheduleService.update()
//                .setSql("current_appointments = current_appointments + 1")
//                .lt("current_appointments", s.getMaxAppointments())
//                .eq("id", scheduleId)
//                .update();
//
//        if (!success) {
//            doctorScheduleService.update()
//                    .setSql("status = FULL")
//                    .eq("id", dto.getScheduleId())
//                    .update();
//            throw new AppiontmentOrderException("挂号已满");
//        }
        // 5. 保存订单
        boolean saved = this.save(order);
        if (!saved) {
            throw new AppiontmentOrderException("挂号失败，请稍后再试");
        }
        // 设置初始队列状态为等待(0)，避免后续更新条件因NULL而不命中
        this.update()
                .set("queue_status", 0)
                .eq("id", order.getId())
                .update();
        // 6. 返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("registrationNo", order.getRegistrationNo());
        map.put("message", "挂号成功");

        return Result.success(map);
    }

}
