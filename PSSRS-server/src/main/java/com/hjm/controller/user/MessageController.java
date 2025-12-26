package com.hjm.controller.user;

import com.hjm.context.PatientContext;
import com.hjm.context.UserPatientContext;
import com.hjm.pojo.DTO.UserPatientDTO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IPatientMessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/messages")
public class MessageController {

    @Resource
    private IPatientMessageService patientMessageService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) Long page,
                                   @RequestParam(required = false) Long size,
                                   @RequestParam(required = false) String status) {
        Long upid = null;
        try {
            UserPatientDTO u = UserPatientContext.get();
            if (u != null) upid = u.getId();
        } catch (Exception ignored) {}
        PageResult pr = patientMessageService.listByPatient(upid, page, size, status);
        return Result.success(pr);
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long pid = PatientContext.getPatient().getId();
        Long upid = null;
        try { UserPatientDTO u = UserPatientContext.get(); if (u != null) upid = u.getId(); } catch (Exception ignored) {}
        return patientMessageService.unreadCount(pid, upid);
    }

    @PutMapping("/{id}/read")
    public Result read(@PathVariable Long id) {
        Long upid = null;
        try { UserPatientDTO u = UserPatientContext.get(); if (u != null) upid = u.getId(); } catch (Exception ignored) {}
        return patientMessageService.markRead(upid, id);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        patientMessageService.removeByUserId(id);
        return Result.success();
    }
}
