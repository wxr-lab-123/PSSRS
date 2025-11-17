package com.hjm.pojo.VO;

import lombok.Data;

@Data
public class DoctorScheduleVO {
    private Long id;
    private String doctorName;
    private String scheduleDate;
    private String timeSlot;
    private String image;
    private String startTime;
    private String endTime;
    private Long current_appointments;
    private Long roomNumber;
    private String notes;
    private String status;
    private String departmentName;
    private String scheduleType;
    private Integer price;
}
