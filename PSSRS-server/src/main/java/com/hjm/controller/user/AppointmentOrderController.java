package com.hjm.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjm.context.PatientContext;
import com.hjm.exception.AppiontmentOrderException;
import com.hjm.pojo.DTO.PaymentDTO;
import com.hjm.pojo.DTO.RegistrationCreateDTO;
import com.hjm.pojo.DTO.RegistrationDTO;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.pojo.Entity.OrderInfo;
import com.hjm.pojo.VO.RegistrationVO;
import com.hjm.result.Result;
import com.hjm.service.IAppointmentOrderService;
import com.hjm.service.IDoctorScheduleService;
import com.hjm.service.IOrderInfoService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController("userAppointmentOrderController")
@RequestMapping("api")
@RequiredArgsConstructor
@Slf4j
public class AppointmentOrderController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final IAppointmentOrderService appointmentOrderService;
    private final IOrderInfoService orderInfoService;
    private final IDoctorScheduleService doctorScheduleService;
//    @PostMapping("/registration/create")
//    public Result<Map<String,Object>> list(@RequestBody RegistrationCreateDTO registrationCreateDTO) {
//        log.info("创建挂号信息:  {}", registrationCreateDTO);
//        return appointmentOrderService.createRegistration(registrationCreateDTO);
//    }
    @PostMapping("/payment/create")
    public Result createPayment(@RequestBody PaymentDTO paymentDTO) {
        log.info("创建支付信息:  {}", paymentDTO);
        return appointmentOrderService.createPayment(paymentDTO);
    }

    @GetMapping("/query/registrations")
    public Result<List<RegistrationVO>> list() {
        log.info("查询挂号信息");
        Long patientId = PatientContext.getPatient().getId();
        List<RegistrationVO> registrations = appointmentOrderService.listByPId(patientId);
        return Result.success(registrations);
    }

    @PostMapping("/appointment/takeNumber")
    @Transactional
    public Result takeNumber(@RequestBody RegistrationDTO registrationDTO) {
        log.info("取号:  {}", registrationDTO);
        Long patientId = PatientContext.getPatient().getId();
        String patientName = PatientContext.getPatient().getName();
        //AppointmentOrder appointmentOrder = appointmentOrderService.getByOrderNo(paymentDTO.getOrderNo());
        AppointmentOrder appointmentOrder = appointmentOrderService.getOne(new QueryWrapper<AppointmentOrder>().eq("registration_no", registrationDTO.getRegistrationNo()));
        if (appointmentOrder == null) {
            throw new AppiontmentOrderException("挂号单不存在");
        }
        if (appointmentOrder.getPatientId() != patientId) {
            throw new AppiontmentOrderException("这个号单不属于当前用户");
        }
        if (appointmentOrder.getStatus() != 0) {
            throw new AppiontmentOrderException("这个号不能取");
        }
        LocalDate today = LocalDate.now();
        if (!appointmentOrder.getAppointmentDate().equals(today)) {
            throw new AppiontmentOrderException("不是今天");
        }
        appointmentOrder.setStatus(1);

        String prefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long seq = stringRedisTemplate.opsForValue().increment("visitNo:" + prefix);

        String visitNumber = prefix + String.format("%06d", seq);
        appointmentOrder.setVisitNumber(visitNumber);
        boolean update = appointmentOrderService.updateById(appointmentOrder);
        if (!update) {
            throw new AppiontmentOrderException("取号失败");
        }
        try {
            String title = "取号通知";
            String content = "患者[" + patientName + "]于[" + java.time.LocalDateTime.now().toString() + "]成功取号，就诊序号：" + visitNumber + "，挂号号：" + appointmentOrder.getRegistrationNo();
            com.hjm.service.IStaffMessageService sms = com.hjm.utils.SpringUtils.getBean(com.hjm.service.IStaffMessageService.class);
            sms.saveForDoctor(appointmentOrder.getDoctorId(), patientId, "TAKE_NUMBER_NOTICE", title, content, 2, true);
            String payload = "{"+
                    "\"type\":\"TAKE_NUMBER_NOTICE\","+
                    "\"patientName\":\"" + patientName + "\","+
                    "\"visitNumber\":\"" + visitNumber + "\","+
                    "\"registrationNo\":\"" + appointmentOrder.getRegistrationNo() + "\","+
                    "\"timestamp\":" + System.currentTimeMillis() +
                    "}";
            com.hjm.WebSocket.WebSocketServer wss = com.hjm.utils.SpringUtils.getBean(com.hjm.WebSocket.WebSocketServer.class);
            try {
                wss.sendMessage(String.valueOf(appointmentOrder.getDoctorId()), payload, "doctor");
            } catch (Exception e) {
                try { Thread.sleep(500); wss.sendMessage(String.valueOf(appointmentOrder.getDoctorId()), payload, "doctor"); } catch (Exception ignored) {}
                org.slf4j.LoggerFactory.getLogger(AppointmentOrderController.class).error("取号通知推送失败", e);
            }
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AppointmentOrderController.class).error("取号通知处理异常", ex);
        }
        return Result.success("取号成功");
    }

    @PostMapping("/registration/cancel")
    @Transactional
    public Result cancel(@RequestBody RegistrationDTO registrationDTO) {
    log.info("取消挂号:  {}", registrationDTO);
    Long patientId = PatientContext.getPatient().getId();
    AppointmentOrder appointmentOrder = appointmentOrderService.getByOrderNo(registrationDTO.getRegistrationNo());
    log.info("取消挂号:  {}", appointmentOrder);
    if (appointmentOrder == null) {
        throw new AppiontmentOrderException("号源不存在");
    }
    if (appointmentOrder.getPatientId() != patientId) {
        throw new AppiontmentOrderException("号源不属于当前用户");
    }
    if (appointmentOrder.getStatus() != 0) {
        throw new AppiontmentOrderException("号源状态异常");
    }
    boolean update = appointmentOrderService.update()
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
    return Result.success();
}
}
