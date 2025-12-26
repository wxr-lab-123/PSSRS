package com.hjm.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hjm.WebSocket.WebSocketServer;
import com.hjm.context.BaseContext;
import com.hjm.context.PatientContext;
import com.hjm.mapper.AppointmentOrderMapper;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.pojo.Entity.LeaveRequest;
import com.hjm.mapper.LeaveRequestMapper;
import com.hjm.pojo.Entity.Message;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.pojo.VO.LeaveRequestVO;
import com.hjm.result.Result;
import com.hjm.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.utils.SpringUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 医生请假申请表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-11-26
 */
@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements ILeaveRequestService {

    private final IDoctorScheduleService doctorScheduleService;
    private final IMessageService messageService;
    private final IPatientMessageService patientMessageService;
    private final IAppointmentOrderService appointmentOrderService;
    private StringRedisTemplate stringRedisTemplate;
    private AppointmentOrderMapper appointmentOrderMapper;

    @Override
    public List<LeaveRequestVO> getLeaveList(Long doctorId) {
        return baseMapper.getLeaveList(doctorId);
    }

    @Override
    public List<LeaveRequestVO> listAllLeaveRequests() {
        return baseMapper.listAllLeaveRequests();
    }

    @Override
    public Integer getLeaveRequestByScheduleId(Long scheduleId) {
        return baseMapper.getLeaveRequestByScheduleId(scheduleId);
    }

    @Override
    @Transactional
    public Result approve(Map<String, Object> body) {
        Object doctorId = body.get("doctorId");
        Object scheduleId = body.get("scheduleId");
        if (!(doctorId instanceof Number) || !(scheduleId instanceof Number)) {
            return Result.error("参数错误");
        }
        long did = ((Number) doctorId).longValue();
        long sid = ((Number) scheduleId).longValue();
        Long approverId = null;
        try {
            approverId = BaseContext.getCurrentId();
        } catch (Exception ignored) {
        }
        UpdateWrapper<LeaveRequest> uw = new UpdateWrapper<>();
        uw.eq("doctor_id", did).eq("schedule_id", sid)
                .in("status", Arrays.asList("REJECTED","PENDING"))
                .set("status", "APPROVED")
                .set(approverId != null, "approver_id", approverId)
                .set("approval_time", java.time.LocalDateTime.now());
        UpdateWrapper<AppointmentOrder> uw2 = new UpdateWrapper<>();
        uw2.eq("schedule_id", sid)
                .set("status", "2");
        appointmentOrderService.update(uw2);
        boolean ok = update(uw);


        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setId(sid);
        schedule.setStatus("CANCELLED");
        doctorScheduleService.updateById(schedule);
        DoctorSchedule byId = doctorScheduleService.getById(sid);
        String key = "schedule:" + byId.getScheduleDate() + ":" + schedule.getDepartmentId();
        if (ok) {
            WebSocketServer wss = SpringUtils.getBean(WebSocketServer.class);
            // 推送给医生审批结果
            String payloadDoctor = "{" +
                    "\"type\":\"LEAVE_APPROVAL_RESULT\"," +
                    "\"scheduleId\":\"" + sid + "\"," +
                    "\"doctorId\":\"" + did + "\"," +
                    "\"status\":\"APPROVED\"," +
                    "\"reason\":\"\"," +
                    "\"timestamp\":" + System.currentTimeMillis() +
                    "}";
            wss.sendMessage(String.valueOf(did), payloadDoctor, "doctor");

            // 查询排班详情用于患者文案
            String doctorName = "";
            String scheduleDate = "";
            String timeSlot = "";
            String startTime = "";
            String endTime = "";
            try {
                com.hjm.result.Result<com.hjm.pojo.VO.DoctorScheduleVO> rs = doctorScheduleService.getXq(sid);
                if (rs != null && rs.getCode() == 0 && rs.getData() != null) {
                    DoctorScheduleVO vo = rs.getData();
                    doctorName = String.valueOf(vo.getDoctorName());
                    scheduleDate = String.valueOf(vo.getScheduleDate());
                    timeSlot = String.valueOf(vo.getTimeSlot());
                    startTime = String.valueOf(vo.getStartTime());
                    endTime = String.valueOf(vo.getEndTime());
                }
            } catch (Exception ignored) {
            }

            // 面向患者的取消通知
            String payloadPatient = "{" +
                    "\"type\":\"APPOINTMENT_CANCELLED_BY_DOCTOR\"," +
                    "\"message\":\"你挂的号由于医生原因已取消\"," +
                    "\"scheduleId\":\"" + sid + "\"," +
                    "\"doctorId\":\"" + did + "\"," +
                    "\"doctorName\":\"" + doctorName + "\"," +
                    "\"scheduleDate\":\"" + scheduleDate + "\"," +
                    "\"timeSlot\":\"" + timeSlot + "\"," +
                    "\"startTime\":\"" + startTime + "\"," +
                    "\"endTime\":\"" + endTime + "\"," +
                    "\"timestamp\":" + System.currentTimeMillis() +
                    "}";

            wss.sendToRelevantPatientsByScheduleId(String.valueOf(sid), payloadPatient);
            try {
                List<Long> pids = appointmentOrderMapper.listPatientIdsByScheduleId(sid);
                patientMessageService.saveForPatients(pids, "APPOINTMENT_CANCELLED_BY_DOCTOR", payloadPatient, did);
            } catch (Exception ignored) {}
            return Result.success("批准");
        } else {
            return Result.error("批准失败");
        }
    }
}
