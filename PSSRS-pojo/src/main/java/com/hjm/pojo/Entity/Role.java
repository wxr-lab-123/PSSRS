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
 * 角色表
 * </p>
 *
 * @author hjm
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("role")
@ApiModel(value="Role对象", description="角色表")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "角色标识（ADMIN/DOCTOR）")
    private String roleName;

    @ApiModelProperty(value = "角色说明")
    private String description;

    @ApiModelProperty(value = "角色唯一编码")
    private String roleKey;

    @ApiModelProperty(value = "父角色ID")
    private Long parentId;

    @ApiModelProperty(value = "角色层级")
    private Integer level;

    @ApiModelProperty(value = "启用=1,禁用=0")
    private Integer status;


}
