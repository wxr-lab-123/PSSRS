package com.hjm.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.hjm.pojo.DTO.BatchAddScheduleDTO;
import com.hjm.pojo.DTO.CopyDSDTO;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.DTO.DoctorSchedulesDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.result.PageResult;
import com.hjm.security.RequiresPermissions;
import com.hjm.result.Result;
import com.hjm.service.IDoctorScheduleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.hjm.constant.RedisConstants.SCHEDULE_;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class DoctorScheduleController {

    @Autowired
    private IDoctorScheduleService doctorScheduleService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/schedules")
    @RequiresPermissions({"schedules:create"})
    public Result addSchedule(@RequestBody DoctorSchedulesDTO doctorSchedulesDTO) {
        log.info("添加排班：{}", doctorSchedulesDTO);
        //添加排班
        doctorScheduleService.save(BeanUtil.copyProperties(doctorSchedulesDTO, DoctorSchedule.class));
        stringRedisTemplate.delete(SCHEDULE_+doctorSchedulesDTO.getScheduleDate()+":"+doctorSchedulesDTO.getDepartmentId());
        return Result.success("添加成功");
    }
    @GetMapping("/schedules")
    @RequiresPermissions({"schedules:view"})
    public Result<PageResult> listSchedules(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(value = "page",defaultValue = "1") Long page,
            @RequestParam(value = "limit",defaultValue = "10") Long limit,
            @RequestParam(required = false) String scheduleDate,
            @RequestParam(name="time_slot",required = false) String tiemSlot,
            @RequestParam(required = false) String status,
            @RequestParam(name = "doctor_id",required = false) Long doctorId,
            @RequestParam(required = false) String scheduleType)
    {
        log.info("查询排班列表，当前页：{}，页大小：{}，医生名称={}", page, limit, doctorId);
        DoctorScheduleDTO doctorScheduleDTO = new DoctorScheduleDTO();
        doctorScheduleDTO.setDepartmentId(departmentId);
        doctorScheduleDTO.setScheduleDate(scheduleDate);
        doctorScheduleDTO.setDoctorId(doctorId);
        doctorScheduleDTO.setTimeSlot(tiemSlot);
        doctorScheduleDTO.setStatus( status);
        doctorScheduleDTO.setPage(page);
        doctorScheduleDTO.setLimit(limit);
        doctorScheduleDTO.setScheduleType(scheduleType);
        PageResult result = doctorScheduleService.listSchedules(doctorScheduleDTO);
        return Result.success(result);
    }

    @GetMapping("/schedules/{id}")
    @RequiresPermissions({"schedules:view"})
    public Result<DoctorScheduleVO> getXq(@PathVariable Long id) throws InterruptedException {
        return doctorScheduleService.getXq(id);
    }

    @DeleteMapping("/schedules/{id}")
    @RequiresPermissions({"schedules:delete"})
    public Result delete(@PathVariable Long id){
        log.info("删除排班：{}", id);
        String key = SCHEDULE_+doctorScheduleService.getById(id).getScheduleDate()+":"+doctorScheduleService.getById(id).getDepartmentId();
        doctorScheduleService.removeById(id);
        stringRedisTemplate.delete(key);
        return Result.success();
    }

    @PutMapping("/schedules")
    @RequiresPermissions({"schedules:update"})
    public Result update(@RequestBody DoctorSchedulesDTO doctorSchedulesDTO){
        log.info("修改排班：{}", doctorSchedulesDTO);
        log.debug("{}", doctorSchedulesDTO);
        String key = SCHEDULE_+doctorSchedulesDTO.getScheduleDate()+":"+doctorSchedulesDTO.getDepartmentId();
        DoctorSchedule doctorSchedule = BeanUtil.copyProperties(doctorSchedulesDTO, DoctorSchedule.class);
        if (doctorSchedulesDTO.getMaxAppointments() == 0){
            doctorSchedule.setStatus("FULL");
        }else {
            doctorSchedule.setStatus("AVAILABLE");
        }
        doctorScheduleService.updateById(doctorSchedule);
        stringRedisTemplate.delete(key);
        return Result.success();
    }

    @PostMapping("/schedules/copy")
    @RequiresPermissions({"schedules:create"})
    public Result copy(@RequestBody CopyDSDTO copyDSDTO)
    {
        return doctorScheduleService.copy(copyDSDTO);
    }

    @PatchMapping("/schedules/{id}/status")
    @RequiresPermissions({"schedules:update"})
    public Result updateStatus(@PathVariable Long id, @RequestParam String status){
        log.info("修改排班状态：{}", status);
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        doctorSchedule.setId(id);
        Long departmentId = doctorScheduleService.getById(id).getDepartmentId();
        String key = SCHEDULE_+doctorScheduleService.getById(id).getScheduleDate()+":"+departmentId;
        doctorSchedule.setStatus(status);
        doctorScheduleService.updateById(doctorSchedule);
        stringRedisTemplate.delete(key);
        return Result.success();
    }

    @PostMapping("/schedules/batch")
    @RequiresPermissions({"schedules:create"})
    public Result batchAdd(@RequestBody BatchAddScheduleDTO batchAddScheduleDTO){
        return doctorScheduleService.batchAdd(batchAddScheduleDTO);
    }

    @GetMapping("/schedules/doctor/{id}/calendar")
    @RequiresPermissions({"schedules:view"})
    public Result<List<DoctorScheduleVO>> getDoctorScheduleCalender(@PathVariable Long id,
                                                                   @RequestParam (name = "start_date",required = false) LocalDate startDate,
                                                                   @RequestParam(name = "end_date",required = false) LocalDate endDate
                                                    ){
        return doctorScheduleService.getDoctorScheduleCalender(id, startDate, endDate);
    }


}
