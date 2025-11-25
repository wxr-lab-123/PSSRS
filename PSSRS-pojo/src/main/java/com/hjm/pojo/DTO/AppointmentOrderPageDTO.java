package com.hjm.pojo.DTO;

import java.time.LocalDate;

@lombok.Data
public class AppointmentOrderPageDTO {
    private Integer page;
    private Integer size;
    private String patientName;
    private LocalDate date;
    private String status;
    private String doctorName;
    private String departmentName;
    private LocalDate startDate;
    private LocalDate endDate;


}
