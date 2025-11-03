package com.hjm.pojo.VO;

import lombok.Data;

@Data
public class DoctorVO {
    private Long id;
    private String image;
    private String name;
    private String title;
    private String departmentName;
    private String description;
    private String phone;
    private String gender;
    private Integer status;
}
