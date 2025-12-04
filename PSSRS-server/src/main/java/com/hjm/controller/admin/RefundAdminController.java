package com.hjm.controller.admin;

import com.hjm.context.BaseContext;
import com.hjm.pojo.DTO.RefundApproveDTO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IRefundService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("api/admin/order/refund")
public class RefundAdminController {

    @Resource
    private IRefundService refundService;

    @PostMapping("/approve")
    public Result approve(@RequestBody RefundApproveDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        return refundService.approve(dto, adminId);
    }

    @GetMapping("/list")
    public Result<PageResult> list(
            @RequestParam(required=false) String refundNo,
            @RequestParam(required=false) String orderNo,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) LocalDate startDate,
            @RequestParam(required=false) LocalDate endDate,
            @RequestParam(required=false) Integer page,
            @RequestParam(required=false) Integer size
    ) {
        PageResult pr = refundService.list(refundNo, orderNo, status, startDate, endDate, page, size);
        return Result.success(pr);
    }
}
