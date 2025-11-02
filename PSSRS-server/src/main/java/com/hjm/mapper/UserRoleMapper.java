package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjm.pojo.Entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户角色关联表 Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

}
