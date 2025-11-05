package com.hjm.pojo.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatientInfoVO {
    private String name;
    private String phone;
    private String idCard;
    private String gender;
    private String age;
    private LocalDateTime createTime;
}
