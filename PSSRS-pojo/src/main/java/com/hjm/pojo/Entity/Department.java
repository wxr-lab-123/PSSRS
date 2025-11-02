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
 * 医院科室表
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("department")
@ApiModel(value="Department对象", description="医院科室表")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "科室ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "科室名称")
    private String name;

    @ApiModelProperty(value = "科室简介")
    private String description;

    @ApiModelProperty(value = "创建时间")

    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除(0正常 1删除)")
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;


}
