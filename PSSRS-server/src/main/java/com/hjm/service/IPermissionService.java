package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.Entity.Permission;

import java.util.List;

public interface IPermissionService extends IService<Permission> {

    List<String> getUserPermissionCodes(Long userId);

    void assignPermissionsToRole(Long roleId, List<Long> allowIds, List<Long> denyIds);
}