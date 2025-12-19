package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.PatientMessage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PatientMessageMapper extends BaseMapper<PatientMessage> {
    List<PatientMessage> listByPatientId(
                                         @Param("userPatientId") Long userPatientId,
                                         @Param("offset") Integer offset,
                                         @Param("size") Integer size,
                                         @Param("status") String status);
    Long countUnreadByPatientId(
                                @Param("userPatientId") Long userPatientId);
    Long countByPatientId(
                          @Param("userPatientId") Long userPatientId,
                          @Param("status") String status);
    int markRead(@Param("id") Long id,
                 @Param("userPatientId") Long userPatientId);
}
