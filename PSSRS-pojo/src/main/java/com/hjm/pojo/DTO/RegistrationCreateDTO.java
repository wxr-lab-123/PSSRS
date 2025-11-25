package com.hjm.pojo.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistrationCreateDTO {
    private Long departmentId;
    private Long doctorId;
    private LocalDate registrationDate;
    private String timeSlot;
    private String scheduleType;
    private String patientPhone;
    private Long scheduleId;
}
