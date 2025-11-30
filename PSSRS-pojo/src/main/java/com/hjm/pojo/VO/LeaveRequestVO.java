package com.hjm.pojo.VO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequestVO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long scheduleId;
    private LocalDate date;
    private String timeSlot;
    private String reason;
    private String status;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime approvalTime;
    private String attachments;
    private Long approverId;
    private String approverName;

}
