package com.hjm.controller.admin;

import com.hjm.context.BaseContext;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IStaffMessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/messages")
public class StaffMessageController {

    @Resource
    private IStaffMessageService staffMessageService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required=false) String status,
                                   @RequestParam(required=false) String type,
                                   @RequestParam(required=false) String q,
                                   @RequestParam(defaultValue="1") Integer page,
                                   @RequestParam(defaultValue="10") Integer size) {
        Long doctorId = BaseContext.getCurrentId();
        PageResult pr = staffMessageService.list(doctorId, page, size, status, type, q);
        return Result.success(pr);
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long doctorId = BaseContext.getCurrentId();
        return staffMessageService.countUnread(doctorId);
    }

    @PutMapping("/{id}/read")
    public Result read(@PathVariable Long id) {
        Long doctorId = BaseContext.getCurrentId();
        return staffMessageService.markRead(id, doctorId);
    }

    @PostMapping("/announce")
    public Result announce(@RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(defaultValue="1") Integer priority,
                           @RequestParam(defaultValue="false") Boolean pinned,
                           @RequestParam(required=false) List<Long> doctorIds) {
        if (doctorIds == null || doctorIds.isEmpty()) doctorIds = java.util.List.of();
        IStaffMessageService svc = staffMessageService;
        for (Long id : doctorIds) { svc.saveForDoctor(id, "SYSTEM_ANNOUNCEMENT", title, content, priority, pinned != null && pinned); }
        return Result.success();
    }

}
