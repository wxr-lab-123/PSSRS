package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.RolePermission;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    @Select("SELECT permission_id FROM role_permission WHERE role_id = #{roleId} AND effect = 'ALLOW'")
    List<Long> listAllowedPermissionIds(Long roleId);

    @Select("SELECT permission_id FROM role_permission WHERE role_id = #{roleId} AND effect = 'DENY'")
    List<Long> listDeniedPermissionIds(Long roleId);
}