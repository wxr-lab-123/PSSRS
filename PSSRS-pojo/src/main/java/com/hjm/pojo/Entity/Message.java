package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * 
 * </p>
 *
 * @author hjm
 * @since 2025-11-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("message")
@ApiModel(value="Message对象", description="")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    @ApiModelProperty(value = "患者ID,逻辑外键")
    private Long patientId;

    @ApiModelProperty(value = "医生ID,逻辑外键")
    private Long doctorId;

    private String messageType;

    private String content;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime readTime;

    private Boolean isDeleted;


}
