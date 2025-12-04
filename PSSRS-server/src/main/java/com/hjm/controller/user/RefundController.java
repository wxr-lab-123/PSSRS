package com.hjm.controller.user;

import com.hjm.context.PatientContext;
import com.hjm.pojo.DTO.RefundApplyDTO;
import com.hjm.pojo.VO.RefundStatusVO;
import com.hjm.result.Result;
import com.hjm.service.IRefundService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/order/refund")
public class RefundController {

    @Resource
    private IRefundService refundService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody RefundApplyDTO dto) {
        Long operatorId = PatientContext.getPatient().getId();
        return refundService.apply(dto, operatorId);
    }

    @GetMapping("/status")
    public Result<RefundStatusVO> status(@RequestParam String orderNo) {
        RefundStatusVO vo = refundService.queryStatus(orderNo);
        return Result.success(vo);
    }
}
