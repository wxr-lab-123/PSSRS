package com.hjm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.Entity.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.VO.OrderPageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    List<OrderInfo> listByPId(Long userPatientId, Integer status, LocalDate startDate);

    Long countRefundRequests();
    Page<OrderPageVO> listByPage(Page<OrderInfo> pageParam, String orderNo, Integer status, LocalDate startDate, LocalDate endDate, String patientName);

    @Update("update order_info set status = 2 where expire_time <= #{now} and status = 0")
    void cleanOutTimeOrder(LocalDateTime now);
    //boolean existsUnpaidOrder();
}
