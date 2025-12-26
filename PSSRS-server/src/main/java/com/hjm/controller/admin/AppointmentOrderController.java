package com.hjm.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.context.BaseContext;
import com.hjm.pojo.DTO.AppointmentOrderPageDTO;
import com.hjm.pojo.DTO.RegistrationCancelDTO;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.pojo.Entity.Patient;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.security.RequiresPermissions;
import com.hjm.service.IAppointmentOrderService;
import com.hjm.service.IPatientMessageService;
import com.hjm.mapper.DoctorScheduleMapper;
import com.hjm.mapper.PatientMapper;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.WebSocket.WebSocketServer;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hjm.constant.OrderConstant.QUEUE_STATUS_BECALLED;

/**
 * <p>
 * 挂号订单表 前端控制器
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AppointmentOrderController {

    private  final IAppointmentOrderService appointmentOrderService;
    @Resource
    private WebSocketServer webSocketServer; // 保留注入，如不可用则通过 SpringUtils 获取
    @Resource
    private IPatientMessageService patientMessageService;
    @Resource
    private DoctorScheduleMapper doctorScheduleMapper;
    @Resource
    private PatientMapper patientMapper;

    @GetMapping("/registrations")
    public Result<PageResult> list(AppointmentOrderPageDTO appointmentOrderPageDTO) {
        return appointmentOrderService.listByPage(appointmentOrderPageDTO);
    }

    @PutMapping("/registrations/{id}/cancel")
    public Result cancel(@PathVariable Integer id,@RequestBody RegistrationCancelDTO DTO) {
        AppointmentOrder order = appointmentOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus(2);
        order.setCancelReason(DTO.getReason());
        order.setRemark(DTO.getRemark());
        boolean update = appointmentOrderService.updateById(order);
        if (!update) {
            return Result.error("取消订单失败");
        }
        return Result.success("取消订单成功");
    }

    // 医生端：诊疗队列查询（当天/状态筛选）
    @GetMapping("/orders")
    @RequiresPermissions({"orders:view"})
    public Result<PageResult> listDoctorOrders(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer queueStatus,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size
    ) {
        Long doctorId = BaseContext.getCurrentId();
        Page<AppointmentOrder> p = new Page<>(page, size);
        QueryWrapper<AppointmentOrder> qw = new QueryWrapper<>();
        qw.eq("doctor_id", doctorId)
                .in("status",1);
        if (date != null && !date.isEmpty()) qw.eq("appointment_date", java.time.LocalDate.parse(date));
        if (queueStatus != null) qw.eq("queue_status", queueStatus);
        Page<AppointmentOrder> pageData = appointmentOrderService.page(p, qw);
        List<Map<String,Object>> enriched = new ArrayList<>();
        for (AppointmentOrder o : pageData.getRecords()) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("registrationNo", o.getRegistrationNo());
            m.put("patientId", o.getPatientId());
            m.put("doctorId", o.getDoctorId());
            m.put("departmentId", o.getDepartmentId());
            m.put("appointmentDate", o.getAppointmentDate());
            m.put("timeSlot", o.getTimeSlot());
            m.put("fee", o.getFee());
            m.put("status", o.getStatus());
            m.put("remark", o.getRemark());
            m.put("createTime", o.getCreateTime());
            m.put("updateTime", o.getUpdateTime());
            m.put("isDeleted", o.getIsDeleted());
            m.put("scheduleType", o.getScheduleType());
            m.put("patientPhone", o.getPatientPhone());
            m.put("scheduleId", o.getScheduleId());
            m.put("visitNumber", o.getVisitNumber());
            m.put("cancelReason", o.getCancelReason());
            m.put("queueStatus", o.getQueueStatus());
            m.put("callTime", o.getCallTime());
            m.put("calledBy", o.getCalledBy());
            m.put("skipTime", o.getSkipTime());
            m.put("recallTime", o.getRecallTime());
            m.put("startTime", o.getStartTime());
            m.put("endTime", o.getEndTime());
            try {
                Patient ptn = patientMapper.selectById(o.getPatientId());
                if (ptn != null) m.put("patientName", ptn.getName());
            } catch (Exception ignored) {}
            try {
                DoctorScheduleVO vo = doctorScheduleMapper.getXq(o.getScheduleId());
                if (vo != null) {
                    m.put("departmentName", String.valueOf(vo.getDepartmentName()));
                    m.put("doctorName", String.valueOf(vo.getDoctorName()));
                    m.put("roomNumber", String.valueOf(vo.getRoomNumber()));
                }
            } catch (Exception ignored) {}
            enriched.add(m);
        }
        PageResult pr = new PageResult(pageData.getTotal(), enriched);
        return Result.success(pr);
    }

    // 叫号：等待→已叫号，记录叫号时间与医生
    // 只有已就诊的挂号才可以叫号
    @PostMapping("/orders/{id}/call")
    @RequiresPermissions({"orders:call"})
    @Transactional
    public Result call(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        Long doctorId = BaseContext.getCurrentId();
        AppointmentOrder order = appointmentOrderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != null && order.getStatus() == 2) return Result.error("订单已取消");
        // 仅允许等待(0)或已叫号(1)的修正
        LocalDateTime now = java.time.LocalDateTime.now();
        boolean ok = appointmentOrderService.update()
                .set("queue_status", 1)
                .set("call_time", now)
                .set("called_by", doctorId)
                .eq("id", id)
                .in("queue_status", 0, 1)
                .update();
        if (!ok) return Result.error("叫号失败");
        try {
            DoctorScheduleVO vo = doctorScheduleMapper.getXq(order.getScheduleId());
            String dep = vo != null ? String.valueOf(vo.getDepartmentName()) : "";
            String doc = vo != null ? String.valueOf(vo.getDoctorName()) : "";
            String room = vo != null ? String.valueOf(vo.getRoomNumber()) : "";
            String reg = String.valueOf(order.getRegistrationNo());
            String content = String.join(", ", dep, doc, room, reg);
            patientMessageService.saveForPatients(java.util.List.of(order.getPatientId()), "ORDER_CALLED", content, doctorId);
        } catch (Exception ignored) {}
        try {
            DoctorScheduleVO vo = doctorScheduleMapper.getXq(order.getScheduleId());
            String payload = "{"+
                    "\"type\":\"ORDER_CALLED\","+
                    "\"orderId\":\"" + id + "\","+
                    "\"doctorId\":\"" + doctorId + "\","+
                    "\"departmentName\":\"" + (vo != null ? String.valueOf(vo.getDepartmentName()) : "") + "\","+
                    "\"doctorName\":\"" + (vo != null ? String.valueOf(vo.getDoctorName()) : "") + "\","+
                    "\"roomNumber\":\"" + (vo != null ? String.valueOf(vo.getRoomNumber()) : "") + "\","+
                    "\"registrationNo\":\"" + String.valueOf(order.getRegistrationNo()) + "\","+
                    "\"timestamp\":" + System.currentTimeMillis() +
                    "}";
            WebSocketServer wss = webSocketServer != null ? webSocketServer : com.hjm.utils.SpringUtils.getBean(WebSocketServer.class);
            wss.sendMessage(String.valueOf(order.getPatientId()), payload, "patient");
        } catch (Exception ignored) {}
        return Result.success("叫号成功");
    }

    // 跳号：已叫号→等待，记录跳号时间，移动至队尾交由前端
    @PostMapping("/orders/{id}/skip")
    @RequiresPermissions({"orders:skip"})
    @Transactional
    public Result skip(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        AppointmentOrder order = appointmentOrderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        boolean ok = appointmentOrderService.update()
                .set("queue_status", 0)
                .set("skip_time", java.time.LocalDateTime.now())
                .eq("id", id)
                .eq("queue_status", 1)
                .update();
        if (!ok) return Result.error("跳号失败");
        return Result.success("跳号成功");
    }

    // 重叫：等待→已叫号，记录重叫时间并推送
    @PostMapping("/orders/{id}/recall")
    @RequiresPermissions({"orders:recall"})
    @Transactional
    public Result recall(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        Long doctorId = BaseContext.getCurrentId();
        AppointmentOrder order = appointmentOrderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        boolean ok = appointmentOrderService.update()
                .set("queue_status", 1)
                .set("recall_time", java.time.LocalDateTime.now())
                .set("called_by", doctorId)
                .eq("id", id)
                .ne("queue_status", 3)
                .update();
        if (!ok) return Result.error("重叫失败");
        try {
            DoctorScheduleVO vo = doctorScheduleMapper.getXq(order.getScheduleId());
            String dep = vo != null ? String.valueOf(vo.getDepartmentName()) : "";
            String doc = vo != null ? String.valueOf(vo.getDoctorName()) : "";
            String room = vo != null ? String.valueOf(vo.getRoomNumber()) : "";
            String reg = String.valueOf(order.getRegistrationNo());
            String content = String.join(", ", dep, doc, room, reg);
            patientMessageService.saveForPatients(java.util.List.of(order.getPatientId()), "ORDER_RECALLED", content, doctorId);
        } catch (Exception ignored) {}
        try {
            DoctorScheduleVO vo = doctorScheduleMapper.getXq(order.getScheduleId());
            String payload = "{"+
                    "\"type\":\"ORDER_RECALLED\","+
                    "\"orderId\":\"" + id + "\","+
                    "\"doctorId\":\"" + doctorId + "\","+
                    "\"departmentName\":\"" + (vo != null ? String.valueOf(vo.getDepartmentName()) : "") + "\","+
                    "\"doctorName\":\"" + (vo != null ? String.valueOf(vo.getDoctorName()) : "") + "\","+
                    "\"roomNumber\":\"" + (vo != null ? String.valueOf(vo.getRoomNumber()) : "") + "\","+
                    "\"registrationNo\":\"" + String.valueOf(order.getRegistrationNo()) + "\","+
                    "\"timestamp\":" + System.currentTimeMillis() +
                    "}";
            WebSocketServer wss = webSocketServer != null ? webSocketServer : com.hjm.utils.SpringUtils.getBean(WebSocketServer.class);
            wss.sendMessage(String.valueOf(order.getPatientId()), payload, "patient");
        } catch (Exception ignored) {}
        return Result.success("重叫成功");
    }

    // 开始就诊：已叫号→就诊中，记录开始时间
    @PostMapping("/orders/{id}/start")
    @RequiresPermissions({"orders:start"})
    @Transactional
    public Result start(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        AppointmentOrder order = appointmentOrderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        boolean ok = appointmentOrderService.update()
                .set("queue_status", 2)
                .set("start_time", java.time.LocalDateTime.now())
                .eq("id", id)
                .eq("queue_status", 1)
                .update();
        if (!ok) return Result.error("开始就诊失败");
        return Result.success("开始就诊成功");
    }

    // 结束就诊：就诊中→已结束，记录结束时间
    @PostMapping("/orders/{id}/end")
    @RequiresPermissions({"orders:end"})
    @Transactional
    public Result end(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        AppointmentOrder order = appointmentOrderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        boolean ok = appointmentOrderService.update()
                .set("queue_status", 3)
                .set("end_time", java.time.LocalDateTime.now())
                .eq("id", id)
                .eq("queue_status", 2)
                .update();
        if (!ok) return Result.error("结束就诊失败");
        return Result.success("结束就诊成功");
    }

    // 直接更新队列状态（保留接口，谨慎使用）
    @PatchMapping("/orders/{id}/queue-status")
    @RequiresPermissions({"orders:update"})
    @Transactional
    public Result updateQueueStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean ok = appointmentOrderService.update()
                .set("queue_status", status)
                .eq("id", id)
                .update();
        if (!ok) return Result.error("更新失败");
        return Result.success("更新成功");
    }

}
