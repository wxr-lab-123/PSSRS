package com.hjm.service.impl;


import com.hjm.mapper.UserRoleMapper;
import com.hjm.pojo.Entity.UserRole;
import com.hjm.service.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

}
