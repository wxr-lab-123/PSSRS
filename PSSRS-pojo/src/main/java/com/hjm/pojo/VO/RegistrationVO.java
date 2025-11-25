package com.hjm.pojo.VO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistrationVO {
    private String registrationNo;
    private String scheduleId;
    private String patientId;
    private String doctorId;
    private String departmentId;
    private String status;
    private String createTime;
    private String updateTime;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String doctorName;
    private String departmentName;
    private String patientName;
    private String patientPhone;
    private String scheduleType;
    private Long fee;
    private String visitNumber;

}
