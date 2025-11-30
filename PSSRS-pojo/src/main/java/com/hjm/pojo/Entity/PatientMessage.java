package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("message")
@ApiModel(value = "PatientMessage", description = "患者消息表")
public class PatientMessage implements Serializable {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    @ApiModelProperty("患者ID")
    private Long patientId;
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
}
