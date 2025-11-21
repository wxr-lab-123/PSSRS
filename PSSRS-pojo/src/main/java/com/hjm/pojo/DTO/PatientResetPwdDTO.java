package com.hjm.pojo.DTO;

import lombok.Data;

@Data
public class PatientResetPwdDTO {
    private String phone;
    private String code;
    private String newPassword;
    private String confirmPassword;
}
