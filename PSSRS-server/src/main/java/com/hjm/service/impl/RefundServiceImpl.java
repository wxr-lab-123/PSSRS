package com.hjm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.mapper.RefundLogMapper;
import com.hjm.mapper.RefundRequestMapper;
import com.hjm.pojo.DTO.RefundApplyDTO;
import com.hjm.pojo.DTO.RefundApproveDTO;
import com.hjm.pojo.DTO.RefundCallbackDTO;
import com.hjm.pojo.Entity.OrderInfo;
import com.hjm.pojo.Entity.RefundLog;
import com.hjm.pojo.Entity.RefundRequest;
import com.hjm.pojo.VO.RefundStatusVO;
import com.hjm.result.Result;
import com.hjm.service.IOrderInfoService;
import com.hjm.service.IAppointmentOrderService;
import com.hjm.service.IDoctorScheduleService;
import com.hjm.service.IMessageService;
import com.hjm.service.IRefundService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class RefundServiceImpl implements IRefundService {

    @Resource
    private RefundRequestMapper refundRequestMapper;
    @Resource
    private RefundLogMapper refundLogMapper;
    @Resource
    private IOrderInfoService orderInfoService;
    @Resource
    private IAppointmentOrderService appointmentOrderService;
    @Resource
    private IDoctorScheduleService doctorScheduleService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IMessageService messageService;

    @Override
    @Transactional
    public Result<String> apply(RefundApplyDTO dto, Long operatorId) {
        String orderNo = dto.getOrderNo();
        String lockKey = "refund:lock:" + orderNo;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return Result.error("申请频繁");
        }
        try {
            OrderInfo order = orderInfoService.getByOrderNo(orderNo);
            if (order == null) return Result.error("订单不存在");
            if (order.getStatus() == null || order.getStatus() != 1) return Result.error("订单不可退款");
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.hjm.pojo.Entity.AppointmentOrder> aqw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            aqw.eq("patient_id", order.getPatientId()).eq("schedule_id", order.getScheduleId()).eq("is_deleted", 0).orderByDesc("create_time").last("LIMIT 1");
            com.hjm.pojo.Entity.AppointmentOrder ap = appointmentOrderService.getOne(aqw);
            if (ap != null) {
                boolean taken = (ap.getVisitNumber() != null && !ap.getVisitNumber().isBlank()) || (ap.getStatus() != null && ap.getStatus() == 1);
                if (taken) return Result.error("已取号不可退款");
            }
            String refundNo = "RF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
            RefundRequest rr = new RefundRequest();
            rr.setRefundNo(refundNo);
            rr.setOrderNo(orderNo);
            rr.setAmount(order.getAmount());
            rr.setReason(dto.getReason());
            rr.setOperatorId(operatorId);
            rr.setSource("patient");
            rr.setStatus("APPLIED");
            rr.setCreateTime(LocalDateTime.now());
            rr.setUpdateTime(LocalDateTime.now());
            refundRequestMapper.insert(rr);
            order.setStatus(3);
            orderInfoService.updateById(order);
            RefundLog log = new RefundLog();
            log.setRefundNo(refundNo);
            log.setStep("APPLY");
            log.setStatus("APPLIED");
            log.setMsg("apply");
            log.setCreatedAt(LocalDateTime.now());
            refundLogMapper.insert(log);
            return Result.success(refundNo);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public Result approve(RefundApproveDTO dto, Long adminId) {
        RefundRequest rr = refundRequestMapper.selectById(dto.getRefundNo());
        if (rr == null) return Result.error("退款申请不存在");
        OrderInfo order = orderInfoService.getByOrderNo(rr.getOrderNo());
        if (order != null) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.hjm.pojo.Entity.AppointmentOrder> aqw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            aqw.eq("patient_id", order.getPatientId()).eq("schedule_id", order.getScheduleId()).eq("is_deleted", 0).orderByDesc("create_time").last("LIMIT 1");
            com.hjm.pojo.Entity.AppointmentOrder ap = appointmentOrderService.getOne(aqw);
            if (ap != null) {
                boolean taken = (ap.getVisitNumber() != null && !ap.getVisitNumber().isBlank()) || (ap.getStatus() != null && ap.getStatus() == 1);
                if (taken) return Result.error("已取号不可退款");
            }
        }
        RefundLog log = new RefundLog();
        log.setRefundNo(rr.getRefundNo());
        log.setStep("APPROVE");
        log.setStatus(Boolean.TRUE.equals(dto.getApprove()) ? "APPROVED" : "REJECTED");
        log.setMsg("approve");
        log.setCreatedAt(LocalDateTime.now());
        refundLogMapper.insert(log);
        if (Boolean.TRUE.equals(dto.getApprove())) {
            rr.setStatus("PROCESSING");
            rr.setUpdateTime(LocalDateTime.now());
            refundRequestMapper.updateById(rr);
            submitToChannel(rr.getRefundNo());
            return Result.success();
        } else {
            return Result.error("审批拒绝");
        }
    }

    @Override
    public void submitToChannel(String refundNo) {
        RefundLog log = new RefundLog();
        log.setRefundNo(refundNo);
        log.setStep("SUBMIT");
        log.setStatus("ACCEPTED");
        log.setMsg("submit");
        log.setCreatedAt(LocalDateTime.now());
        refundLogMapper.insert(log);
        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> {
            RefundCallbackDTO dto = new RefundCallbackDTO();
            dto.setRefundNo(refundNo);
            dto.setStatus("SUCCEEDED");
            dto.setChannelMsg("mock");
            handleCallback(dto);
        });
    }

    @Override
    @Transactional
    public Result handleCallback(RefundCallbackDTO dto) {
        RefundRequest rr = refundRequestMapper.selectById(dto.getRefundNo());
        if (rr == null) return Result.error("退款申请不存在");
        if ("SUCCEEDED".equals(rr.getStatus()) || "FAILED".equals(rr.getStatus())) {
            return Result.success();
        }
        rr.setStatus(dto.getStatus());
        rr.setUpdateTime(LocalDateTime.now());
        refundRequestMapper.updateById(rr);
        OrderInfo order = orderInfoService.getByOrderNo(rr.getOrderNo());
        if (order != null) {
            if ("SUCCEEDED".equals(dto.getStatus())) order.setStatus(4); else order.setStatus(1);
            orderInfoService.updateById(order);
            if ("SUCCEEDED".equals(dto.getStatus())) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.hjm.pojo.Entity.AppointmentOrder> aqw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                aqw.eq("patient_id", order.getPatientId()).eq("schedule_id", order.getScheduleId()).eq("is_deleted", 0).orderByDesc("create_time").last("LIMIT 1");
                com.hjm.pojo.Entity.AppointmentOrder ap = appointmentOrderService.getOne(aqw);
                if (ap != null) {
                    boolean notTaken = ap.getVisitNumber() == null || ap.getVisitNumber().isBlank();
                    if (notTaken && (ap.getStatus() == null || ap.getStatus() != 2)) {
                        ap.setStatus(2);
                        ap.setCancelReason("退款成功自动取消");
                        appointmentOrderService.updateById(ap);
                        com.hjm.pojo.Entity.DoctorSchedule ds = doctorScheduleService.getById(ap.getScheduleId());
                        if (ds != null && ds.getCurrentAppointments() != null && ds.getCurrentAppointments() > 0) {
                            ds.setCurrentAppointments(ds.getCurrentAppointments() - 1);
                            doctorScheduleService.updateById(ds);
                        }
                    }
                }
            }
        }
        RefundLog log = new RefundLog();
        log.setRefundNo(rr.getRefundNo());
        log.setStep("CALLBACK");
        log.setStatus(dto.getStatus());
        log.setMsg(dto.getChannelMsg());
        log.setCreatedAt(LocalDateTime.now());
        refundLogMapper.insert(log);
        try {
            String payload;
            if (order != null) {
                BigDecimal amt = rr.getAmount();
                if ("SUCCEEDED".equals(dto.getStatus())) {
                    payload = "{" +
                            "\"type\":\"REFUND_SUCCEEDED\"," +
                            "\"orderNo\":\"" + rr.getOrderNo() + "\"," +
                            "\"amount\":\"" + (amt == null ? "0" : amt.toPlainString()) + "\"," +
                            "\"timestamp\":" + System.currentTimeMillis() +
                            "}";
                } else {
                    payload = "{" +
                            "\"type\":\"REFUND_FAILED\"," +
                            "\"orderNo\":\"" + rr.getOrderNo() + "\"," +
                            "\"amount\":\"" + (amt == null ? "0" : amt.toPlainString()) + "\"," +
                            "\"timestamp\":" + System.currentTimeMillis() +
                            "}";
                }
                com.hjm.WebSocket.WebSocketServer wss = com.hjm.utils.SpringUtils.getBean(com.hjm.WebSocket.WebSocketServer.class);
                try { wss.sendMessage(String.valueOf(order.getPatientId()), payload, "patient"); } catch (Exception ignored) {}
                com.hjm.pojo.Entity.Message m = new com.hjm.pojo.Entity.Message();
                m.setPatientId(order.getPatientId());
                m.setMessageType("REFUND_" + ("SUCCEEDED".equals(dto.getStatus()) ? "SUCCEEDED" : "FAILED"));
                m.setContent("订单[" + rr.getOrderNo() + "]退款" + ("SUCCEEDED".equals(dto.getStatus()) ? "成功" : "失败") + "，金额" + (amt == null ? "0" : amt.toPlainString()) + "元");
                m.setStatus("未读");
                m.setCreateTime(java.time.LocalDateTime.now());
                m.setIsDeleted(false);
                messageService.save(m);
            }
        } catch (Exception ignored) {}
        return Result.success();
    }

    @Override
    public RefundStatusVO queryStatus(String orderNo) {
        RefundRequest rr = refundRequestMapper.selectOne(new QueryWrapper<RefundRequest>().eq("order_no", orderNo));
        RefundStatusVO vo = new RefundStatusVO();
        vo.setOrderNo(orderNo);
        if (rr != null) {
            vo.setRefundNo(rr.getRefundNo());
            vo.setStatus(rr.getStatus());
            vo.setAmount(rr.getAmount());
        } else {
            vo.setStatus("NONE");
        }
        return vo;
    }

    @Override
    public com.hjm.result.PageResult list(String refundNo, String orderNo, String status, LocalDate startDate, LocalDate endDate, Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;
        QueryWrapper<RefundRequest> qw = new QueryWrapper<>();
        if (refundNo != null && !refundNo.isBlank()) qw.eq("refund_no", refundNo);
        if (orderNo != null && !orderNo.isBlank()) qw.eq("order_no", orderNo);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (startDate != null) qw.ge("create_time", java.time.LocalDateTime.of(startDate, java.time.LocalTime.MIN));
        if (endDate != null) qw.le("create_time", java.time.LocalDateTime.of(endDate, java.time.LocalTime.MAX));
        qw.orderByDesc("update_time");
        Page<RefundRequest> pageParam = new Page<>(p, s);
        Page<RefundRequest> result = refundRequestMapper.selectPage(pageParam, qw);
        return new com.hjm.result.PageResult(result.getTotal(), result.getRecords());
    }
}
