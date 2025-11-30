package com.hjm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.mapper.PatientMessageMapper;
import com.hjm.pojo.Entity.PatientMessage;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IPatientMessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PatientMessageServiceImpl extends ServiceImpl<PatientMessageMapper, PatientMessage> implements IPatientMessageService {

    @Resource
    private PatientMessageMapper patientMessageMapper;

    @Override
    public void saveForPatients(List<Long> patientIds, String type, String content, Long doctorId) {
        if (patientIds == null || patientIds.isEmpty()) return;
        List<PatientMessage> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Long pid : patientIds) {
            PatientMessage m = new PatientMessage();
            m.setPatientId(pid);
            m.setDoctorId(doctorId);
            m.setMessageType(type);
            m.setContent(content);
            m.setStatus("未读");
            m.setCreateTime(now);
            m.setIsDeleted(0);
            list.add(m);
        }
        saveBatch(list);
    }

    @Override
    public PageResult listByPatient(Long patientId, Long page, Long size, String status) {
        int p = page == null ? 1 : page.intValue();
        int s = size == null ? 10 : size.intValue();
        int offset = (p - 1) * s;
        List<PatientMessage> rows = patientMessageMapper.listByPatientId(patientId, offset, s, status);
        Long total = patientMessageMapper.countByPatientId(patientId, status);
        return new PageResult(total == null ? 0 : total, rows);
    }

    @Override
    public Result markRead(Long patientId, Long id) {
        int n = patientMessageMapper.markRead(id, patientId);
        return n > 0 ? Result.success(null) : Result.error("更新失败");
    }

    @Override
    public Result<Long> unreadCount(Long patientId) {
        Long c = patientMessageMapper.countUnreadByPatientId(patientId);
        return Result.success(c);
    }
}
