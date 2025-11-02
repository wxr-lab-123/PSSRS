package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 患者用户表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

}
