package com.hjm.pojo.VO;

import lombok.Data;

@Data
public class OrderPageVO {
    private String orderNo;
    private String patientName;
    private Double amount;
    private String status;
    private String payTime;
    private String payWay;
}
