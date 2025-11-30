package com.hjm.mapper;

import com.hjm.pojo.Entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-11-24
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户id查询角色名称
     * @param id
     * @return
     */
    @Select("SELECT role_name FROM role WHERE id IN (SELECT role_id FROM user_role WHERE user_id = #{id})")
    List<String> getRoleNamesByUserId(Long id);

    @Select("SELECT parent_id FROM role WHERE id = #{roleId}")
    Long selectParentIdByRoleId(Long roleId);
}
