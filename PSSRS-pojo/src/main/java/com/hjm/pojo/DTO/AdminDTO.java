package com.hjm.pojo.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理员注册/更新传输对象
 */
@Data
@ApiModel(value = "AdminDTO", description = "管理员数据传输对象")
public class AdminDTO {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "性别(0男 1女)")
    private String gender;

    @ApiModelProperty(value = "状态(1启用 0禁用)")
    private Integer status;
}
