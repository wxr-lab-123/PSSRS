package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 医生扩展信息表
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("doctor_profile")
@ApiModel(value="DoctorProfile对象", description="医生扩展信息表")
public class DoctorProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "医生档案ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联 user 表ID")
    private Long userId;

    @ApiModelProperty(value = "职称（主任医师、副主任医师等）")
    private String title;

    @ApiModelProperty(value = "所属科室ID")
    private Long departmentId;

    @ApiModelProperty(value = "医生简介")
    private String description;

    @ApiModelProperty(value = "医生头像")
    private String image;


}
