package com.hjm.controller.admin;

import com.hjm.result.Result;
import com.hjm.mapper.AppointmentOrderMapper;
import com.hjm.mapper.OrderInfoMapper;
import com.hjm.WebSocket.WebSocketServer;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        try { todayPending = appointmentOrderMapper.countTodayPendingVisits();
        } catch (Exception ignored) {}
        try { refundReqs = orderInfoMapper.countRefundRequests(); } catch (Exception ignored) {}
        try { onlineDoctors = com.hjm.utils.SpringUtils.getBean(WebSocketServer.class).getOnlineDoctorCount(); } catch (Exception ignored) {}

        Map<String, Object> data = new HashMap<>();
        data.put("today_registrations", todayRegs);
        data.put("today_pending_visits", todayPending);
        data.put("online_doctors", onlineDoctors);
        data.put("refund_requests", refundReqs);
        return Result.success(data);
    }

    @GetMapping("/charts")
    public Result<Map<String, Object>> charts() {
        // 1. Trend Data
        List<Map<String, Object>> trendList = appointmentOrderMapper.getTrendData();
        List<String> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        Map<String, Long> trendMap = new HashMap<>();
        if (trendList != null) {
            for (Map<String, Object> item : trendList) {
                trendMap.put((String) item.get("date"), ((Number) item.get("count")).longValue());
            }
        }

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String dateStr = today.minusDays(i).format(DateTimeFormatter.ofPattern("MM-dd"));
            dates.add(dateStr);
            values.add(trendMap.getOrDefault(dateStr, 0L));
        }

        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", dates);
        trend.put("values", values);

        // 2. Department Proportion
        List<Map<String, Object>> deptList = appointmentOrderMapper.getDepartmentProportion();

        Map<String, Object> data = new HashMap<>();
        data.put("trend", trend);
        data.put("departmentProportion", deptList);

        return Result.success(data);
    }
}
