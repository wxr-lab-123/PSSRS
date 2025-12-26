package com.hjm.service;

import com.hjm.pojo.Entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.VO.OrderInfoVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    Result createOrder(Integer scheduleId, Long patientId);

    OrderInfo getByOrderNo(String orderNo);

    List<OrderInfoVO> listByPId(Long userPatientId, Integer status, LocalDate startDate);

    Result<PageResult> listByPage(String orderNo, Integer status, LocalDate startDate, LocalDate endDate, Integer page, Integer size, String patientName);


    void cleanOutTimeOrder(LocalDateTime now);
}
