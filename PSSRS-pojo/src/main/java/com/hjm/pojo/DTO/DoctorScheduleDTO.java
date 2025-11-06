package com.hjm.pojo.DTO;

import lombok.Data;

@Data
public class DoctorScheduleDTO {

    private Long doctorId;
    private Integer departmentId;
    private Long page;
    private Long limit;
    private String scheduleDate;
    private String timeSlot;
    private String status;

}
