package com.hjm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.mapper.DoctorScheduleMapper;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.result.PageResult;
import com.hjm.service.IDoctorScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医生排班表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-11-05
 */
@Service
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule> implements IDoctorScheduleService {

    @Override
    public PageResult listSchedules(DoctorScheduleDTO doctorScheduleDTO) {
        Page<DoctorSchedule> page = new Page<>(doctorScheduleDTO.getPage(), doctorScheduleDTO.getLimit());
        Page<DoctorScheduleVO> result = baseMapper.listSchedules(page, doctorScheduleDTO);
        PageResult pageResult = new PageResult(result.getTotal(), result.getRecords());
        return pageResult;
    }

}
