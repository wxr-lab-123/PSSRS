package com.hjm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.mapper.RolePermissionMapper;
import com.hjm.pojo.Entity.RolePermission;
import com.hjm.service.IRolePermissionService;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements IRolePermissionService {
}