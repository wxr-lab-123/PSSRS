package com.hjm.mapper;

import com.hjm.pojo.Entity.LeaveRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.VO.LeaveRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 医生请假申请表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-11-26
 */
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

    List<LeaveRequestVO> getLeaveList(Long doctorId);

    List<LeaveRequestVO> listAllLeaveRequests();

    @Select("select count(*) from leave_request where schedule_id = #{scheduleId} and status = 'PENDING'")
    Integer getLeaveRequestByScheduleId(Long scheduleId);
}
