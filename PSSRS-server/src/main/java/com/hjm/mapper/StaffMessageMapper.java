package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.StaffMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StaffMessageMapper extends BaseMapper<StaffMessage> {
    Long countUnreadByDoctorId(Long doctorId);
}
