package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.Entity.StaffMessage;
import com.hjm.result.PageResult;
import com.hjm.result.Result;

import java.util.List;

public interface IStaffMessageService extends IService<StaffMessage> {
    Result<Long> countUnread(Long doctorId);
    PageResult list(Long doctorId, Integer page, Integer size, String status, String type, String q);
    Result markRead(Long id, Long doctorId);
    void saveForDoctor(Long doctorId, String type, String title, String content, int priority, boolean pinned);
    void saveForDoctors(List<Long> doctorIds, String type, String title, String content, int priority, boolean pinned);
    void saveForDoctor(Long doctorId, Long patientId, String type, String title, String content, int priority, boolean pinned);
}
