package com.hjm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.VO.DoctorScheduleVO;

/**
 * <p>
 * 医生排班表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-11-05
 */
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {

    /**
     * 医生排班列表
     */
    Page<DoctorScheduleVO> listSchedules(Page<DoctorSchedule> page, DoctorScheduleDTO doctorScheduleDTO);
}
