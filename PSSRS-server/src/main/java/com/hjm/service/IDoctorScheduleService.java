package com.hjm.service;

import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.result.PageResult;

/**
 * <p>
 * 医生排班表 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-11-05
 */
public interface IDoctorScheduleService extends IService<DoctorSchedule> {

    PageResult listSchedules(DoctorScheduleDTO doctorScheduleDTO);
}
