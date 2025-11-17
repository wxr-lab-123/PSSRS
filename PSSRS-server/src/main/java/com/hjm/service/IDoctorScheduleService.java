package com.hjm.service;

import com.hjm.pojo.DTO.BatchAddScheduleDTO;
import com.hjm.pojo.DTO.CopyDSDTO;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;

import java.util.HashMap;
import java.util.List;

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

    Result<DoctorScheduleVO> getXq(Long id) throws InterruptedException;

    Result copy(CopyDSDTO copyDSDTO);

    List<DoctorScheduleVO> listScheduleByDid(Long departmentId, String date);

    Result batchAdd(BatchAddScheduleDTO batchAddScheduleDTO);
}
