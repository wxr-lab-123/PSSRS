package com.hjm.service.impl;


import com.hjm.mapper.AppointmentOrderMapper;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.service.IAppointmentOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 挂号订单表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public class AppointmentOrderServiceImpl extends ServiceImpl<AppointmentOrderMapper, AppointmentOrder> implements IAppointmentOrderService {

}
