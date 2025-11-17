package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 医生排班表
 * </p>
 *
 * @author hjm
 * @since 2025-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("doctor_schedule")
@ApiModel(value="DoctorSchedule对象", description="医生排班表")
public class DoctorSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "排班ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "医生ID")
    @TableField("doctor_id")
    private Long doctorId;

    @ApiModelProperty(value = "科室ID")
    @TableField("department_id")
    private Long departmentId;

    @ApiModelProperty(value = "排班日期")
    @TableField("schedule_date")
    private LocalDate scheduleDate;

    @ApiModelProperty(value = "排班类型(专家/普通)")
    @TableField("schedule_type")
    private String scheduleType;

    @ApiModelProperty(value = "时间段(MORNING/AFTERNOON/EVENING)")
    @TableField("time_slot")
    private String timeSlot;

    @ApiModelProperty(value = "开始时间")
    @TableField("start_time")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间")
    @TableField("end_time")
    private LocalTime endTime;

    @ApiModelProperty(value = "最大挂号数")
    @TableField("max_appointments")
    private Integer maxAppointments;

    @ApiModelProperty(value = "当前已挂号数")
    @TableField("current_appointments")
    private Integer currentAppointments;

    @ApiModelProperty(value = "剩余号源")
    @TableField(value = "available_appointments",exist = false)
    private Integer availableAppointments;

    @ApiModelProperty(value = "状态(AVAILABLE-可用/FULL-已满/CANCELLED-已取消)")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "诊室号")
    @TableField("room_number")
    private String roomNumber;

    @ApiModelProperty(value = "备注")
    @TableField("notes")
    private String notes;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "创建人")
    @TableField("created_by")
    private Long createdBy;

    @ApiModelProperty(value = "更新人")
    @TableField("updated_by")
    private Long updatedBy;



}
