package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.DoctorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 医生扩展信息表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Mapper
public interface DoctorProfileMapper extends BaseMapper<DoctorProfile> {


    void updateByUserId(DoctorProfile doctorProfile);

    @Select("SELECT * FROM doctor_profile WHERE department_id = #{departmentId} ")
    List<DoctorProfile> getDoctorByDepartmentId(Long departmentId);
}
