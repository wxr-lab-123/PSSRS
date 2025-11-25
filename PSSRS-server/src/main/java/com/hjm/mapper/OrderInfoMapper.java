package com.hjm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.Entity.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.VO.OrderPageVO;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 挂号系统订单表（先订单，再挂号） Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-11-21
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //查询未支付订单
    boolean existsUnpaidOrder(Long patientId, Long scheduleId);

    @Select("select * from order_info where order_no = #{orderNo}")
    OrderInfo getByOrderNo(String orderNo);

//    @Select("select * from order_info where patient_id = #{patientId} and status = #{status} and create_time >= #{startDate}")
    List<OrderInfo> listByPId(Long patientId, Integer status, java.time.LocalDate startDate);

    Page<OrderPageVO> listByPage(Page<OrderInfo> pageParam, String orderNo, Integer status, LocalDate startDate, LocalDate endDate, String patientName);
    //boolean existsUnpaidOrder();
}
