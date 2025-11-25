package com.hjm.controller.admin;

import com.hjm.pojo.VO.OrderPageVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IOrderInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController()
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderInfoController {

    private final IOrderInfoService orderInfoService;

    @RequestMapping("/list")
    public Result<PageResult> list(@RequestParam(required = false) String orderNo,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) LocalDate startDate,
                                   @RequestParam(required = false) LocalDate endDate,
                                   @RequestParam(required = false, defaultValue = "1") Integer page,
                                   @RequestParam(required = false, defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String patientName
    ) {
        return orderInfoService.listByPage(orderNo, status, startDate, endDate, page, size, patientName);
    }
}
