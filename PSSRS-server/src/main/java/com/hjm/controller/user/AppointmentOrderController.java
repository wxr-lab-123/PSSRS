package com.hjm.controller.user;

import com.hjm.context.PatientContext;
import com.hjm.exception.AppiontmentOrderException;
import com.hjm.pojo.DTO.PaymentDTO;
import com.hjm.pojo.DTO.RegistrationCreateDTO;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.pojo.VO.RegistrationVO;
import com.hjm.result.Result;
import com.hjm.service.IAppointmentOrderService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @PostMapping("/registration/create")
    public Result<Map<String,Object>> list(@RequestBody RegistrationCreateDTO registrationCreateDTO) {
        log.info("创建挂号信息:  {}", registrationCreateDTO);
        return appointmentOrderService.createRegistration(registrationCreateDTO);
    }
    @PostMapping("/payment/create")
    public Result createPayment(@RequestBody PaymentDTO paymentDTO) {
        log.info("创建支付信息:  {}", paymentDTO);
        AppointmentOrder appointmentOrder = appointmentOrderService.getByOrderNo(paymentDTO.getOrderNo());
        if (appointmentOrder == null) {
            throw new AppiontmentOrderException("订单不存在");
        }
        if (appointmentOrder.getStatus() != 0) {
            throw new AppiontmentOrderException("订单已支付");
        }
        appointmentOrder.setStatus(1);
        boolean update = appointmentOrderService.updateById(appointmentOrder);
        if (!update) {
            throw new AppiontmentOrderException("支付失败");
        }
        return Result.success("支付完成");
    }

    @GetMapping("/query/registrations")
    public Result<List<RegistrationVO>> list() {
        log.info("查询挂号信息");
        Long patientId = PatientContext.getPatient().getId();
        List<RegistrationVO> registrations = appointmentOrderService.listByPId(patientId);
        return Result.success(registrations);
    }

    @PostMapping("/appointment/takeNumber")
    public Result takeNumber(@RequestBody PaymentDTO paymentDTO) {
        log.info("取号:  {}", paymentDTO);
        Long patientId = PatientContext.getPatient().getId();
        AppointmentOrder appointmentOrder = appointmentOrderService.getByOrderNo(paymentDTO.getOrderNo());
        if (appointmentOrder == null) {
            throw new AppiontmentOrderException("订单不存在");
        }
        if (appointmentOrder.getPatientId() != patientId) {
            throw new AppiontmentOrderException("订单不属于当前用户");
        }
        if (appointmentOrder.getStatus() != 1) {
            throw new AppiontmentOrderException("订单未支付");
        }
        LocalDate today = LocalDate.now();
        if (!appointmentOrder.getAppointmentDate().equals(today)) {
            throw new AppiontmentOrderException("不是今天");
        }
        appointmentOrder.setStatus(2);

        String prefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long seq = stringRedisTemplate.opsForValue().increment("visitNo:" + prefix);

        String visitNumber = prefix + String.format("%06d", seq);
        appointmentOrder.setVisitNumber(visitNumber);
        boolean update = appointmentOrderService.updateById(appointmentOrder);
        if (!update) {
            throw new AppiontmentOrderException("取号失败");
        }
        return Result.success("取号成功");
    }

    @PostMapping("/registration/cancel")
    public Result cancel(@RequestBody PaymentDTO paymentDTO) {
        log.info("取消挂号:  {}", paymentDTO);
        Long patientId = PatientContext.getPatient().getId();
        AppointmentOrder appointmentOrder = appointmentOrderService.getByOrderNo(paymentDTO.getOrderNo());
        if (appointmentOrder == null) {
            throw new AppiontmentOrderException("订单不存在");
        }
        if (appointmentOrder.getPatientId() != patientId) {
            throw new AppiontmentOrderException("订单不属于当前用户");
        }
        if (appointmentOrder.getStatus() != 1) {
            throw new AppiontmentOrderException("订单未支付");
        }
        boolean update = appointmentOrderService.update()
                .set("status", 3)
                .eq("id", appointmentOrder.getId())
                .update();
                if (!update) {
                    throw new AppiontmentOrderException("取消失败");
                }
        return Result.success();
    }


}
