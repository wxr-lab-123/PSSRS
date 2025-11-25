package com.hjm.service;

import com.hjm.pojo.Entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.result.PageResult;
import com.hjm.result.Result;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 挂号系统订单表（先订单，再挂号） 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-11-21
 */
public interface IOrderInfoService extends IService<OrderInfo> {

    Result createOrder(Integer scheduleId);

    OrderInfo getByOrderNo(String orderNo);

    List<OrderInfo> listByPId(Long patientId, Integer status, LocalDate startDate);

    Result<PageResult> listByPage(String orderNo, Integer status, LocalDate startDate, LocalDate endDate, Integer page, Integer size, String patientName);
}
