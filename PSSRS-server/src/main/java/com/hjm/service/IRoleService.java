package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.Entity.Role;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public interface IRoleService extends IService<Role> {

    List<String> getRoleNamesByUserId(Long id);
}
