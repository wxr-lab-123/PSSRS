package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.DTO.RegistrationCreateDTO;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.result.Result;
import org.springframework.stereotype.Service;

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

    Result createRegistration(RegistrationCreateDTO registrationCreateDTO);
}
