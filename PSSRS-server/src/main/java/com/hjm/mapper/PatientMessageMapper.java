package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.PatientMessage;
import java.util.List;

public interface PatientMessageMapper extends BaseMapper<PatientMessage> {
    List<PatientMessage> listByPatientId(Long patientId, Integer offset, Integer size, String status);
    Long countUnreadByPatientId(Long patientId);
    Long countByPatientId(Long patientId, String status);
    int markRead(Long id, Long patientId);
}
