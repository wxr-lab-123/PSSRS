package com.hjm.controller.user;

import com.hjm.context.PatientContext;
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
        Long pid = PatientContext.getPatient().getId();
        PageResult pr = patientMessageService.listByPatient(pid, page, size, status);
        return Result.success(pr);
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long pid = PatientContext.getPatient().getId();
        return patientMessageService.unreadCount(pid);
    }

    @PutMapping("/{id}/read")
    public Result read(@PathVariable Long id) {
        Long pid = PatientContext.getPatient().getId();
        return patientMessageService.markRead(pid, id);
    }
}
