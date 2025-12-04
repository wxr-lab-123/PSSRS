package com.hjm.pojo.DTO;

import java.math.BigDecimal;

public class RefundApproveDTO {
    private String refundNo;
    private Boolean approve;
    private BigDecimal amount;

    public String getRefundNo() { return refundNo; }
    public void setRefundNo(String refundNo) { this.refundNo = refundNo; }
    public Boolean getApprove() { return approve; }
    public void setApprove(Boolean approve) { this.approve = approve; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
