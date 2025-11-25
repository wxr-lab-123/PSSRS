package com.hjm.pojo.VO;

import lombok.Data;

@Data
public class AppointmentOrderPageVO {
    private Long id;
    private String registrationNo;
    private String patientName;
    private String doctorName;
    private String departmentName;
    private String scheduleTime;
    private String status;
    private String createTime;
    private String updateTime;
    private String fee;
    private String appointmentDate;
    private String timeSlot;
}
