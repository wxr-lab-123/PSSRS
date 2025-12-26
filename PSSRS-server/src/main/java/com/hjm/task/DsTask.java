package com.hjm.task;


import com.hjm.service.IAppointmentOrderService;
import com.hjm.service.IOrderInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class DsTask {

    private final IOrderInfoService orderInfoService;
    private final IAppointmentOrderService appointmentOrderService;

    // 每分钟定时
    @Scheduled(cron = "0 0/1 * * * ?")
    public void ordertask() {
        log.info("定时任务开始执行...");
        LocalDateTime now = LocalDateTime.now();
        orderInfoService.cleanOutTimeOrder(now);
        log.info("定时任务执行完毕...");
    }
    // 每天定时
    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 0/1 * * * ?")
    public void rgtask() {
        log.info("定时任务开始执行...");
        LocalDateTime now = LocalDateTime.now();
        appointmentOrderService.cleanOutTimeReg(now);
        log.info("定时任务执行完毕...");
    }
}
