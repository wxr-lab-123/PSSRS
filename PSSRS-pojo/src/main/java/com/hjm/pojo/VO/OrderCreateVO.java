package com.hjm.pojo.VO;

import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单展示 VO
 * 用于返回给前端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateVO {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 号源ID
     */
    private Long scheduleId;

    /**
     * 挂号费用
     */
    private BigDecimal amount;

    /**
     * 订单状态，例如 WAIT_PAY / PAID / CANCEL
     */
    private String status;

    /**
     * 订单超时时间
     */
    private LocalDateTime expireTime;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 科室名称
     */
    private String departmentName;

    /**
     * 备注
     */
    private String remark;

}