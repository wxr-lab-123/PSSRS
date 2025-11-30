package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.DTO.AppointmentOrderPageDTO;
import com.hjm.pojo.Entity.AppointmentOrder;

import com.hjm.pojo.VO.AppointmentOrderPageVO;
import com.hjm.pojo.VO.RegistrationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 获取挂号费
     * @param scheduleType
     * @return
     */
    @Select("select fee from registration_fee where type = #{scheduleType}")
    Long getFee(String scheduleType);

    /**
     * 根据订单编号查询挂号信息
     * @param orderNo
     * @return
     */
    @Select("select * from appointment_order where registration_no = #{orderNo}")
    AppointmentOrder getByOrderNo(String orderNo);

    /**
     * 根据患者ID查询挂号信息
     * @param patientId
     * @return
     */
    List<RegistrationVO> listByPId(Long patientId);

    Long countTodayRegistrations();

    Long countTodayPendingVisits();

    Page<AppointmentOrderPageVO> getByPage(Page<AppointmentOrder> page, AppointmentOrderPageDTO appointmentOrderPageDTO);

    List<Long> listPatientIdsByScheduleId(Long scheduleId);
}
