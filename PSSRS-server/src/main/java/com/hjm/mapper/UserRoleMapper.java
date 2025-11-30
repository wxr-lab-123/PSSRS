package com.hjm.mapper;

import com.hjm.pojo.Entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 用户角色关联表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-11-24
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @org.apache.ibatis.annotations.Select("SELECT role_id FROM user_role WHERE user_id = #{userId}")
    java.util.List<Long> selectRoleIdsByUserId(Long userId);
}
