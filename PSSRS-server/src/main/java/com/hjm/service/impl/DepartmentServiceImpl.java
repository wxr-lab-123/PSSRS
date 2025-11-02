package com.hjm.service.impl;

import com.hjm.mapper.DepartmentMapper;
import com.hjm.pojo.Entity.Department;
import com.hjm.service.IDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医院科室表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

}
