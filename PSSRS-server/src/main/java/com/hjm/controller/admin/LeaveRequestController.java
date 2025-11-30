package com.hjm.controller.admin;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hjm.exception.LeaveRequestException;
import com.hjm.pojo.DTO.LeaveDTO;
import com.hjm.pojo.Entity.LeaveRequest;
import com.hjm.pojo.VO.LeaveRequestVO;
import com.hjm.result.Result;
import com.hjm.service.ILeaveRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 医生请假申请表 前端控制器
 * </p>
 *
 * @author hjm
 * @since 2025-11-26
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
public class LeaveRequestController {

    private final ILeaveRequestService leaveRequestService;
    /**
     * 医生请假申请
     * @param leaveDTO
     * @return
     */
    @PostMapping("/leave/apply")
    public Result leaveApply(@RequestBody LeaveDTO leaveDTO) {
        log.info("请假信息 {}",leaveDTO);
        if (leaveDTO.getStartTime().isAfter(leaveDTO.getEndTime())){
            throw new LeaveRequestException("开始时间不能早于结束时间");
        }
        if (leaveDTO.getStartTime().isBefore(LocalDateTime.now())){
            throw new LeaveRequestException("开始时间不能早于当前时间");
        }
        if (leaveDTO.getStartTime().isAfter(leaveDTO.getEndTime())){
            throw new LeaveRequestException("开始时间不能早于结束时间");
        }
        Integer c = leaveRequestService.getLeaveRequestByScheduleId(leaveDTO.getScheduleId());
        if (c > 0){
            throw new LeaveRequestException("该时间段已有请假申请");
        }
        leaveRequestService.save(BeanUtil.copyProperties(leaveDTO, LeaveRequest.class));
        return Result.success();
    }

    /**
     * 获取请假信息列表
     * @return
     */
    @GetMapping("/leave")
    public Result<List<LeaveRequestVO>> getLeaveList(@RequestParam(required = false) Long doctorId) {
        return Result.success(leaveRequestService.getLeaveList(doctorId));
    }

    /**
     * 获取所有请假信息列表
     * @return
     */
    @GetMapping("/leave/all")
    public Result<List<LeaveRequestVO>> listAll(@RequestParam(value = "status", required = false) String status) {
        List<LeaveRequestVO> list = leaveRequestService.listAllLeaveRequests();
        return Result.success(list);
    }

    @PostMapping("/leave/cancel")
    public Result cancel(@RequestBody java.util.Map<String, Object> body) {
        Object doctorId = body.get("doctorId");
        Object scheduleId = body.get("scheduleId");
        if (!(doctorId instanceof Number) || !(scheduleId instanceof Number)) {
            return Result.error("参数错误");
        }
        long did = ((Number) doctorId).longValue();
        long sid = ((Number) scheduleId).longValue();
        UpdateWrapper<LeaveRequest> uw = new UpdateWrapper<>();
        uw.eq("doctor_id", did).eq("schedule_id", sid)
                .in("status", "PENDING", "APPROVED")
                .set("status", "WITHDRAWN");
        boolean ok = leaveRequestService.update(uw);
        return ok ? Result.success(null) : Result.error("撤销失败");
    }

    @PostMapping("/leave/approve")
    public Result approve(@RequestBody java.util.Map<String, Object> body) {
        return leaveRequestService.approve(body);
    }

    @PostMapping("/leave/reject")
    public Result reject(@RequestBody java.util.Map<String, Object> body) {
        Object doctorId = body.get("doctorId");
        Object scheduleId = body.get("scheduleId");
        if (!(doctorId instanceof Number) || !(scheduleId instanceof Number)) {
            return Result.error("参数错误");
        }
        long did = ((Number) doctorId).longValue();
        long sid = ((Number) scheduleId).longValue();
        Long approverId = null;
        try { approverId = com.hjm.context.BaseContext.getCurrentId(); } catch (Exception ignored) {}
        UpdateWrapper<LeaveRequest> uw = new UpdateWrapper<>();
        uw.eq("doctor_id", did).eq("schedule_id", sid)
                .eq("status", "PENDING")
                .set("status", "REJECTED")
                .set(approverId != null, "approver_id", approverId)
                .set("approval_time", java.time.LocalDateTime.now());
        boolean ok = leaveRequestService.update(uw);
        return ok ? Result.success(null) : Result.error("拒绝失败");
    }


}
