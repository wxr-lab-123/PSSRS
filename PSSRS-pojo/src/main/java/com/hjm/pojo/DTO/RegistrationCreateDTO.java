package com.hjm.pojo.DTO;

import lombok.Data;

@Data
public class RegistrationCreateDTO {
    private Long departmentId;
    private Long doctorId;
    private String registrationDate;
    private String timeSlot;
    private String scheduleType;
    private String patientPhone;
}
