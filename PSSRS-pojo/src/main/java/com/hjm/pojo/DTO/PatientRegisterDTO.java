package com.hjm.pojo.DTO;

import lombok.Data;

@Data
public class PatientRegisterDTO {
    private String name;
    private String password;
    private String phone;
    private String code;
    private String idCard;
    private String confirmPassword;
    private String relation;
}
