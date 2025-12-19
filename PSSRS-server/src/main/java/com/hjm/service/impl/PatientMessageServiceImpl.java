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
    @Resource
    private com.hjm.mapper.PatientMapper patientMapper;

    @Override
    public void saveForPatients(List<Long> patientIds, String type, String content, Long doctorId) {
        if (patientIds == null || patientIds.isEmpty()) return;
        List<PatientMessage> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        java.util.Map<Long, Long> upidMap = new java.util.HashMap<>();
        try {
            java.util.List<com.hjm.pojo.Entity.Patient> pts = patientMapper.selectBatchIds(patientIds);
            if (pts != null) {
                for (com.hjm.pojo.Entity.Patient p : pts) {
                    if (p != null && p.getId() != null) {
                        upidMap.put(p.getId(), p.getUserPatientId());
                    }
                }
            }
        } catch (Exception ignored) {}
        for (Long pid : patientIds) {
            PatientMessage m = new PatientMessage();
            m.setPatientId(pid);
            try { m.setUserPatientId(upidMap.get(pid)); } catch (Exception ignored) {}
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
    public PageResult listByPatient(Long userPatientId, Long page, Long size, String status) {
        int p = page == null ? 1 : page.intValue();
        int s = size == null ? 10 : size.intValue();
        int offset = (p - 1) * s;
        List<PatientMessage> rows = patientMessageMapper.listByPatientId( userPatientId, offset, s, status);
        Long total = patientMessageMapper.countByPatientId(userPatientId, status);
        return new PageResult(total == null ? 0 : total, rows);
    }

    @Override
    public Result markRead(Long userPatientId, Long id) {
        int n = patientMessageMapper.markRead(id, userPatientId);
        return n > 0 ? Result.success(null) : Result.error("更新失败");
    }

    @Override
    public Result<Long> unreadCount(Long patientId, Long userPatientId) {
        Long c = patientMessageMapper.countUnreadByPatientId(userPatientId);
        return Result.success(c);
    }
}
