package com.hjm.pojo.Entity;

import java.math.BigDecimal;
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
 * 挂号订单表
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("appointment_order")
@ApiModel(value="AppointmentOrder对象", description="挂号订单表")
public class AppointmentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "挂号号")
    private String registrationNo;

    @ApiModelProperty(value = "患者ID（patient表）")
    private Long patientId;

    @ApiModelProperty(value = "所属用户ID(user_patient表)")
    private Long userPatientId;

    @ApiModelProperty(value = "医生档案ID（doctor_profile表）")
    private Long doctorId;

    @ApiModelProperty(value = "科室ID")
    private Long departmentId;

    @ApiModelProperty(value = "预约日期")
    private LocalDate appointmentDate;

    @ApiModelProperty(value = "预约时段（上午/下午）")
    private String timeSlot;

    @ApiModelProperty(value = "挂号费用")
    private BigDecimal fee;

    @ApiModelProperty(value = "状态(0待就诊,1已完成,2已取消)")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除(0正常 1删除)")
    private Integer isDeleted;

    private String scheduleType;
    @ApiModelProperty(value = "手机号")
    private String patientPhone;

    @ApiModelProperty(value = "排班ID")
    private Long scheduleId;

    @ApiModelProperty(value = "就诊号")
    private String visitNumber;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "就诊状态")
    private Integer queueStatus;
    @ApiModelProperty(value = "叫号时间")
    private LocalDateTime callTime;
    @ApiModelProperty(value = "叫号医生")
    private Integer calledBy;
    @ApiModelProperty(value = "跳号时间")
    private LocalDateTime skipTime;
    @ApiModelProperty(value = "重叫时间")
    private LocalDateTime recallTime;
    @ApiModelProperty(value = "就诊时间")
    private LocalDateTime startTime;
    @ApiModelProperty(value = "就诊完成时间")
    private LocalDateTime endTime;



}
