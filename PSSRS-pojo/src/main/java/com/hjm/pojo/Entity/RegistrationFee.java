package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("registration_fee")
public class RegistrationFee {
    private String type;
    private Integer fee;

}
