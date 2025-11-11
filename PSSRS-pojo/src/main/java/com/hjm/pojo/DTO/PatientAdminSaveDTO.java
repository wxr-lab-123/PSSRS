package com.hjm.pojo.DTO;

import lombok.Data;

@Data
public class PatientAdminSaveDTO {
    private String name;
    private String phone;
    private String idCard;
    private String gender;
    private String age;
    private String address;
}
