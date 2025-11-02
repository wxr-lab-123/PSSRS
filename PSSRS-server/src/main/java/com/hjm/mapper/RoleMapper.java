package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户id获取角色名称
     *
     * @param userId 用户id
     * @return 角色名称列表
     */
    @Select("SELECT r.role_name FROM role r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> getRoleNamesByUserId(Long userId);
}
