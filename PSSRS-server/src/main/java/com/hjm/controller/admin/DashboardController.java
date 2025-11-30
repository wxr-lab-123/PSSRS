package com.hjm.controller.admin;

import com.hjm.result.Result;
import com.hjm.mapper.AppointmentOrderMapper;
import com.hjm.mapper.OrderInfoMapper;
import com.hjm.WebSocket.WebSocketServer;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Resource
    private AppointmentOrderMapper appointmentOrderMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Long todayRegs = 0L;
        Long todayPending = 0L;
        Long refundReqs = 0L;
        int onlineDoctors = 0;

        try { todayRegs = appointmentOrderMapper.countTodayRegistrations(); } catch (Exception ignored) {}
        try { todayPending = appointmentOrderMapper.countTodayPendingVisits(); } catch (Exception ignored) {}
        try { refundReqs = orderInfoMapper.countRefundRequests(); } catch (Exception ignored) {}
        try { onlineDoctors = com.hjm.utils.SpringUtils.getBean(WebSocketServer.class).getOnlineDoctorCount(); } catch (Exception ignored) {}

        Map<String, Object> data = new HashMap<>();
        data.put("today_registrations", todayRegs);
        data.put("today_pending_visits", todayPending);
        data.put("online_doctors", onlineDoctors);
        data.put("refund_requests", refundReqs);
        return Result.success(data);
    }
}
