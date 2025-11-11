package com.hjm.controller.user;

import com.hjm.service.IAppointmentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userAppointmentOrderController")
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AppointmentOrderController {
    private final IAppointmentOrderService appointmentOrderService;

    @RequestMapping("/list")
    public String list() {
        log.info("查询当日所有排班信息");
        return "查询所有预约订单";
    }
}
