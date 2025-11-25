package com.hjm.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.result.Result;
import com.hjm.service.IDoctorScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDoctorScheduleController")
@RequestMapping("/api/user/doctor")
@RequiredArgsConstructor
@Slf4j
public class DoctoeScheduleController {
    private final IDoctorScheduleService doctorScheduleService;

    @RequestMapping("/list")
    public Result<List<DoctorScheduleVO>> list(
            @RequestParam Long departmentId,
            @RequestParam String date
    ) {
        log.info("查询医生排班列表：{}", departmentId);
        if (date == null || date.isEmpty()){
            return Result.error(404, "日期不能为空");
        }
        if (departmentId == null)
            return Result.error(404, "部门ID不能为空");
        List<DoctorScheduleVO> result = doctorScheduleService.listScheduleByDid(departmentId,date);
        return Result.success(result);
    }
    @GetMapping("/schedule/detail")
    public Result<DoctorScheduleVO> detail(
            @RequestParam Long id
    ) throws InterruptedException {
        log.info("查询医生排班详情：{}", id);
        if (id == null)
            return Result.error(404, "排班ID不能为空");
        Result<DoctorScheduleVO> result = doctorScheduleService.getXq(id);
        return result;
    }
}
