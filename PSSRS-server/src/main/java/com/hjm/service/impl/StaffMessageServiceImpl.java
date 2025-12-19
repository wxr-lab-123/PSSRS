package com.hjm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.mapper.StaffMessageMapper;
import com.hjm.pojo.Entity.StaffMessage;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IStaffMessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StaffMessageServiceImpl extends ServiceImpl<StaffMessageMapper, StaffMessage> implements IStaffMessageService {

    @Resource
    private StaffMessageMapper staffMessageMapper;

    @Override
    public Result<Long> countUnread(Long doctorId) {
        try {
            return Result.success(staffMessageMapper.countUnreadByDoctorId(doctorId)); }
        catch (Exception e)
        {
            return Result.success(0L);
        }
    }

    @Override
    public PageResult list(Long doctorId, Integer page, Integer size, String status, String type, String q) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        int offset = (p - 1) * s;
        Long total;
        List<StaffMessage> rows;
        try {
            java.util.Map<String,Object> params = new java.util.HashMap<>();
            params.put("doctorId", doctorId);
            params.put("status", status);
            params.put("type", type);
            params.put("q", q);
            params.put("offset", offset);
            params.put("size", s);
            total = staffMessageMapper.selectCount(
                    new QueryWrapper<StaffMessage>()
                            .eq("receiver_id", doctorId)
                    .eq("is_deleted", 0));
            rows = staffMessageMapper.selectList(
                    new QueryWrapper<StaffMessage>()
                            .eq("receiver_id", doctorId)
                    .eq("is_deleted", 0)
                    .orderByDesc("pinned","create_time")
                    .last("LIMIT "+offset+","+s));
        } catch (Exception e) {
            total = 0L;
            rows = new ArrayList<>();
        }
        return new PageResult(total, rows);
    }

    @Override
    public Result markRead(Long id, Long doctorId) {
        try {
            StaffMessage message =  getById( id);
            if (message == null) return Result.error("消息不存在");
            if (!message.getReceiverId().equals(doctorId)) return Result.error("无权操作");
            UpdateWrapper<StaffMessage> u = new UpdateWrapper<>();
            u.eq("message_id", id)
                    .eq("receiver_id", doctorId)
                    .eq("status", "未读")
                    .set("status", "已读")
                    .set("read_time", LocalDateTime.now());
            update(u);
            return Result.success();
        } catch (Exception e) { return Result.error("操作失败"); }
    }

    @Override
    public void saveForDoctor(Long doctorId, String type, String title, String content, int priority, boolean pinned) {
        StaffMessage m = new StaffMessage();
        m.setReceiverId(doctorId);
        m.setMessageType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setStatus("未读");
        m.setPriority(priority);
        m.setPinned(pinned ? 1 : 0);
        m.setIsDeleted(0);
        m.setCreateTime(LocalDateTime.now());
        staffMessageMapper.insert(m);
    }

    @Override
    public void saveForDoctor(Long doctorId, Long patientId, String type, String title, String content, int priority, boolean pinned) {
        StaffMessage m = new StaffMessage();
        m.setReceiverId(doctorId);
        m.setPatientId(patientId);
        m.setMessageType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setStatus("未读");
        m.setPriority(priority);
        m.setPinned(pinned ? 1 : 0);
        m.setIsDeleted(0);
        m.setCreateTime(LocalDateTime.now());
        staffMessageMapper.insert(m);
    }

    @Override
    public void saveForDoctors(List<Long> doctorIds, String type, String title, String content, int priority, boolean pinned) {
        if (doctorIds == null) return;
        for (Long id : doctorIds) { saveForDoctor(id, type, title, content, priority, pinned); }
    }
}
