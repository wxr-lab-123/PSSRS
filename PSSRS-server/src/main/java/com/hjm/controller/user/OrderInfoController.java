package com.hjm.controller.user;


import com.hjm.constant.OrderConstant;
import com.hjm.context.PatientContext;
import com.hjm.context.UserPatientContext;
import com.hjm.pojo.Entity.OrderInfo;
import com.hjm.pojo.VO.OrderInfoVO;
import com.hjm.result.Result;
import com.hjm.service.IOrderInfoService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 挂号系统订单表（先订单，再挂号） 前端控制器
 * </p>
 *
 * @author hjm
 * @since 2025-11-21
 */
@RestController("userOrderInfoController")
@RequestMapping("/api/order")
@Slf4j
@RequiredArgsConstructor
public class OrderInfoController {

    private final IOrderInfoService orderInfoService;



    @PostMapping("/create")
    public Result create(@RequestParam Integer scheduleId, @RequestParam Long patientId) {
        log.info("创建订单：{}", scheduleId);
        return  orderInfoService.createOrder(scheduleId, patientId);
    }

    @GetMapping("/list")
    public Result<List<OrderInfoVO>> list(@RequestParam(required = false)  Integer status
  , @RequestParam(required = false)  LocalDate startDate
    ) {
        log.info("查询订单");
        Long userPatientId = UserPatientContext.get().getId();
        List<OrderInfoVO> orders = orderInfoService.listByPId(userPatientId, status, startDate);
        return Result.success(orders);
    }

}
