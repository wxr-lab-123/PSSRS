package com.hjm.pojo.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 医生注册/更新传输对象
 */
@Data
@ApiModel(value = "DoctorDTO", description = "医生数据传输对象")
public class DoctorDTO {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "性别(0男 1女)")
    private String gender;

    @ApiModelProperty(value = "状态(1启用 0禁用)")
    private Integer status;

    @ApiModelProperty(value = "职称（主任医师、副主任医师等）")
    private String title;

    @ApiModelProperty(value = "所属科室ID")
    private Long departmentId;

    @ApiModelProperty(value = "医生简介")
    private String description;

    @ApiModelProperty(value = "医生头像")
    private String image;
}
