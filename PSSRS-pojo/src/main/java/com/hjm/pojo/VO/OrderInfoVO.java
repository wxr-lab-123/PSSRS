package com.hjm.pojo.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderInfoVO {

    private Long id;

    private String orderNo;

    private Long patientId;

    private Long userPatientId;

    private Long scheduleId;

    private BigDecimal amount;

    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime refundTime;

    private String remark;

    private String patientName;

}
