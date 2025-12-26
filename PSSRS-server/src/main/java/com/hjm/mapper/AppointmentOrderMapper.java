package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.DTO.AppointmentOrderPageDTO;
import com.hjm.pojo.Entity.AppointmentOrder;

import com.hjm.pojo.VO.AppointmentOrderPageVO;
import com.hjm.pojo.VO.RegistrationVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * @return
     */
    List<RegistrationVO> listByPId(Long userId);

    Long countTodayRegistrations();

    Long countTodayPendingVisits();

    Page<AppointmentOrderPageVO> getByPage(Page<AppointmentOrder> page, AppointmentOrderPageDTO appointmentOrderPageDTO);

    List<Long> listPatientIdsByScheduleId(Long scheduleId);

    @Select("SELECT DATE_FORMAT(appointment_date, '%m-%d') as date, COUNT(1) as count " +
            "FROM appointment_order " +
            "WHERE is_deleted = 0 " +
            "AND appointment_date >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY) " +
            "GROUP BY date " +
            "ORDER BY date")
    List<Map<String, Object>> getTrendData();

    @Select("SELECT dp.name as name, COUNT(1) as value " +
            "FROM appointment_order a " +
            "JOIN department dp ON a.department_id = dp.id " +
            "WHERE a.is_deleted = 0 " +
            "GROUP BY dp.name " +
            "ORDER BY value DESC " +
            "LIMIT 8")
    List<Map<String, Object>> getDepartmentProportion();

    @Update("update  appointment_order set status = 2 WHERE appointment_date < #{now} and status = 0")
    void cleanOutTimeReg(LocalDateTime now);
}
