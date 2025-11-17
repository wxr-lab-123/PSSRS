package com.hjm.controller.user;

import com.hjm.pojo.DTO.RegistrationCreateDTO;
import com.hjm.result.Result;
import com.hjm.service.IAppointmentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController("userAppointmentOrderController")
@RequestMapping("api")
@RequiredArgsConstructor
@Slf4j
public class AppointmentOrderController {
    private final IAppointmentOrderService appointmentOrderService;

    @PostMapping("/registration/create")
    public Result list(@RequestBody RegistrationCreateDTO registrationCreateDTO) {
        log.info("创建挂号信息:  {}", registrationCreateDTO);
        return appointmentOrderService.createRegistration(registrationCreateDTO);
    }
}
