package com.hjm.service.impl;

import com.hjm.pojo.Entity.Role;
import com.hjm.mapper.RoleMapper;
import com.hjm.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Override
    public List<String> getRoleNamesByUserId(Long id) {
        return roleMapper.getRoleNamesByUserId(id);
    }
}
