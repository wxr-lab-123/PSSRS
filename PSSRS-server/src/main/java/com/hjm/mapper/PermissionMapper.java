package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.Permission;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT p.code FROM permission p JOIN role_permission rp ON rp.permission_id = p.id WHERE rp.role_id = #{roleId} AND rp.effect = 'ALLOW'")
    List<String> listCodesByRoleId(Long roleId);
}