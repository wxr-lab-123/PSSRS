package com.hjm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.mapper.PermissionMapper;
import com.hjm.mapper.RolePermissionMapper;
import com.hjm.mapper.UserRoleMapper;
import com.hjm.mapper.RoleMapper;
import com.hjm.mapper.OperationLogMapper;
import com.hjm.pojo.Entity.Permission;
import com.hjm.pojo.Entity.RolePermission;
import com.hjm.pojo.Entity.OperationLog;
import com.hjm.service.IPermissionService;
import com.hjm.service.IRoleService;
import jakarta.annotation.Resource;
import com.hjm.context.BaseContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Resource
    private RolePermissionMapper rolePermissionMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        String cacheKey = com.hjm.constant.RedisConstants.USER_PERMS_KEY + userId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            String[] arr = cached.split(",");
            return Arrays.asList(arr);
        }
        List<String> codes = computePermissionsByUser(userId);
        if (!codes.isEmpty()) {
            stringRedisTemplate.opsForValue().set(cacheKey, String.join(",", codes), com.hjm.constant.RedisConstants.USER_PERMS_TTL, java.util.concurrent.TimeUnit.MINUTES);
        }
        return codes;
    }

    private List<String> computePermissionsByUser(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new QueryWrapper<>()).isEmpty() ? Collections.emptyList() : userRoleMapper.selectRoleIdsByUserId(userId);
        Set<Long> allow = new HashSet<>();
        Set<Long> deny = new HashSet<>();
        for (Long roleId : roleIds) {
            List<Long> a = rolePermissionMapper.listAllowedPermissionIds(roleId);
            List<Long> d = rolePermissionMapper.listDeniedPermissionIds(roleId);
            if (CollUtil.isNotEmpty(a)) allow.addAll(a);
            if (CollUtil.isNotEmpty(d)) deny.addAll(d);
            Long parentId = roleMapper.selectParentIdByRoleId(roleId);
            while (parentId != null && parentId > 0) {
                List<Long> pa = rolePermissionMapper.listAllowedPermissionIds(parentId);
                List<Long> pd = rolePermissionMapper.listDeniedPermissionIds(parentId);
                if (CollUtil.isNotEmpty(pa)) allow.addAll(pa);
                if (CollUtil.isNotEmpty(pd)) deny.addAll(pd);
                parentId = roleMapper.selectParentIdByRoleId(parentId);
            }
        }
        allow.removeAll(deny);
        if (allow.isEmpty()) return Collections.emptyList();
        List<String> codes = new ArrayList<>();
        if (!allow.isEmpty()) {
            List<Permission> list = list(new QueryWrapper<Permission>().in("id", allow));
            for (Permission p : list) {
                codes.add(p.getCode());
            }
        }
        return codes;
    }

    @Override
    public void assignPermissionsToRole(Long roleId, List<Long> allowIds, List<Long> denyIds) {
        Long operatorId = BaseContext.getCurrentId();
        List<String> operatorCodes = operatorId == null ? Collections.emptyList() : getUserPermissionCodes(operatorId);
        if (allowIds != null && !allowIds.isEmpty()) {
            List<Permission> toAssign = list(new QueryWrapper<Permission>().in("id", allowIds));
            for (Permission p : toAssign) {
                if (!operatorCodes.contains(p.getCode())) {
                    throw new com.hjm.exception.BaseException("超出可分配权限范围");
                }
            }
        }
        rolePermissionMapper.delete(new QueryWrapper<RolePermission>().eq("role_id", roleId));
        if (CollUtil.isNotEmpty(allowIds)) {
            for (Long pid : allowIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rp.setEffect("ALLOW");
                rolePermissionMapper.insert(rp);
            }
        }
        if (CollUtil.isNotEmpty(denyIds)) {
            for (Long pid : denyIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rp.setEffect("DENY");
                rolePermissionMapper.insert(rp);
            }
        }
        Set<String> keys = stringRedisTemplate.keys(com.hjm.constant.RedisConstants.USER_PERMS_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        OperationLog log = new OperationLog();
        log.setUserId(operatorId);
        log.setOperation("RBAC_ASSIGN");
        log.setMethod("POST /api/admin/roles/" + roleId + "/permissions");
        log.setParams("allow=" + String.valueOf(allowIds) + ",deny=" + String.valueOf(denyIds));
        log.setResult("OK");
        log.setStatus("SUCCESS");
        log.setCreateTime(java.time.LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}