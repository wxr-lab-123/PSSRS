package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 医生请假申请表
 * </p>
 *
 * @author hjm
 * @since 2025-11-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("leave_request")
@ApiModel(value="LeaveRequest对象", description="医生请假申请表")
public class LeaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long doctorId;

    private Long scheduleId;

    private LocalDate date;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String timeSlot;

    private String reason;

    private String status;

    private Long approverId;

    private LocalDateTime approvalTime;

    private LocalDateTime createTime;

    @ApiModelProperty(value = "附件图片url")
    private String attachments;

    @ApiModelProperty(value = "请假类型")
    private String leaveType;

    @ApiModelProperty(value = "拒绝原因")
    private String rejectReason;


}
