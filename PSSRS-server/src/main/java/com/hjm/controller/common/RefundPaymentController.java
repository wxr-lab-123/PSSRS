package com.hjm.controller.common;

import com.hjm.pojo.DTO.RefundCallbackDTO;
import com.hjm.result.Result;
import com.hjm.service.IRefundService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/payment/refund")
public class RefundPaymentController {

    @Resource
    private IRefundService refundService;

    @PostMapping("/callback")
    public Result callback(@RequestBody RefundCallbackDTO dto) {
        return refundService.handleCallback(dto);
    }
}
