package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 患者用户表
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("patient")
@ApiModel(value="Patient对象", description="患者用户表")
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "患者ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "性别(0男 1女)")
    private String gender;

    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "身份证号")
    private String idCard;

    @ApiModelProperty(value = "登录密码（加密存储）")
    private String password;

    @ApiModelProperty(value = "家庭住址")
    private String address;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除(0正常 1删除)")
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;


}
