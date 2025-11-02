package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.AppointmentOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 挂号订单表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Mapper
public interface AppointmentOrderMapper extends BaseMapper<AppointmentOrder> {

}
