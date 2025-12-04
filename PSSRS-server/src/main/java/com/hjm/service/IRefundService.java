package com.hjm.service;

import com.hjm.pojo.DTO.RefundApplyDTO;
import com.hjm.pojo.DTO.RefundApproveDTO;
import com.hjm.pojo.DTO.RefundCallbackDTO;
import com.hjm.pojo.VO.RefundStatusVO;
import com.hjm.result.Result;
import com.hjm.result.PageResult;
import java.time.LocalDate;

public interface IRefundService {
    Result<String> apply(RefundApplyDTO dto, Long operatorId);
    Result approve(RefundApproveDTO dto, Long adminId);
    void submitToChannel(String refundNo);
    Result handleCallback(RefundCallbackDTO dto);
    RefundStatusVO queryStatus(String orderNo);
    PageResult list(String refundNo, String orderNo, String status, LocalDate startDate, LocalDate endDate, Integer page, Integer size);
}
