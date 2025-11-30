package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.Entity.PatientMessage;
import com.hjm.result.PageResult;
import com.hjm.result.Result;

import java.util.List;

public interface IPatientMessageService extends IService<PatientMessage> {
    void saveForPatients(List<Long> patientIds, String type, String content, Long doctorId);
    PageResult listByPatient(Long patientId, Long page, Long size, String status);
    Result markRead(Long patientId, Long id);
    Result<Long> unreadCount(Long patientId);
}
