package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.UserPatient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPatientMapper extends BaseMapper<UserPatient> {
}
