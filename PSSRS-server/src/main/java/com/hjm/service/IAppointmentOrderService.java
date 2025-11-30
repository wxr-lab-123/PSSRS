package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.DTO.AppointmentOrderPageDTO;
import com.hjm.pojo.DTO.PaymentDTO;
import com.hjm.pojo.DTO.RegistrationCreateDTO;
import com.hjm.pojo.Entity.AppointmentOrder;

import com.hjm.pojo.VO.RegistrationVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 挂号订单表 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public interface IAppointmentOrderService extends IService<AppointmentOrder> {

    Result<Map<String,Object>> createRegistration(RegistrationCreateDTO registrationCreateDTO);

    Result<Map<String, Object>> createRegistrationOrder(RegistrationCreateDTO dto);

    AppointmentOrder getByOrderNo(String orderNo);

    List<RegistrationVO> listByPId(Long patientId);

    Result<PageResult> listByPage(AppointmentOrderPageDTO appointmentOrderPageDTO);

    Result createPayment(PaymentDTO paymentDTO);

    List<Long> listPatientIdsByScheduleId(Long scheduleId);
}
