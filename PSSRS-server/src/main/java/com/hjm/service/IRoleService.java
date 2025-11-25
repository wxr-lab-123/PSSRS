package com.hjm.service;

import com.hjm.pojo.Entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-11-24
 */
public interface IRoleService extends IService<Role> {

    List<String> getRoleNamesByUserId(Long id);
}
