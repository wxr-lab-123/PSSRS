package com.hjm.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.DTO.DoctorSchedulesDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IDoctorScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class DoctorScheduleController {

    @Autowired
    private IDoctorScheduleService doctorScheduleService;

    @PostMapping("/schedules")
    public Result addSchedule(@RequestBody DoctorSchedulesDTO doctorSchedulesDTO) {
        log.info("添加排班：{}", doctorSchedulesDTO);
        //添加排班
        doctorScheduleService.save(BeanUtil.copyProperties(doctorSchedulesDTO, DoctorSchedule.class));
        return Result.success("添加成功");
    }
    @GetMapping("/schedules")
    public Result<PageResult> listSchedules(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(value = "page",defaultValue = "1") Long page,
            @RequestParam(value = "limit",defaultValue = "10") Long limit,
            @RequestParam(required = false) String scheduleDate,
            @RequestParam(name="time_slot",required = false) String tiemSlot,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long doctorId)
    {
        log.info("查询排班列表，当前页：{}，页大小：{}，医生名称={}", page, limit);
        DoctorScheduleDTO doctorScheduleDTO = new DoctorScheduleDTO();
        doctorScheduleDTO.setDepartmentId(departmentId);
        doctorScheduleDTO.setScheduleDate(scheduleDate);
        doctorScheduleDTO.setDoctorId(doctorId);
        doctorScheduleDTO.setTimeSlot(tiemSlot);
        doctorScheduleDTO.setStatus( status);
        doctorScheduleDTO.setPage(page);
        doctorScheduleDTO.setLimit(limit);
        PageResult result = doctorScheduleService.listSchedules(doctorScheduleDTO);
        return Result.success(result);
    }
}
