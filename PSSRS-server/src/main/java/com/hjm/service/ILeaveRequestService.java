package com.hjm.service;

import com.hjm.pojo.Entity.LeaveRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.VO.LeaveRequestVO;
import com.hjm.result.Result;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 医生请假申请表 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-11-26
 */
public interface ILeaveRequestService extends IService<LeaveRequest> {

    List<LeaveRequestVO> getLeaveList(Long doctorId);

    List<LeaveRequestVO> listAllLeaveRequests();

    Integer getLeaveRequestByScheduleId(Long scheduleId);

    Result approve(Map<String, Object> body);
}
