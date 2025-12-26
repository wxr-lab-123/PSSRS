package com.hjm.pojo.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatientMessageVO {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    @ApiModelProperty("患者ID")
    private Long patientId;
    @ApiModelProperty("所属用户ID")
    private Long userPatientId;
    @ApiModelProperty("医生ID")
    private Long doctorId;
    @ApiModelProperty("消息类型")
    private String messageType;
    @ApiModelProperty("消息内容JSON")
    private String content;
    @ApiModelProperty("状态：未读/已读")
    private String status;
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("阅读时间")
    private LocalDateTime readTime;
    private Integer isDeleted;

    @ApiModelProperty("患者姓名")
    private String patientName;
}
