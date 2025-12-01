package com.hjm.controller.admin;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.mapper.DoctorProfileMapper;
import com.hjm.pojo.DTO.DepartmentDTO;
import com.hjm.pojo.Entity.Department;
import com.hjm.result.Result;
import com.hjm.security.RequiresPermissions;
import com.hjm.service.IDepartmentService;
import com.hjm.service.IDoctorProfileService;
import com.hjm.service.IDoctorScheduleService;
import com.hjm.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 医院科室表 前端控制器
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/admin/departments")
@Slf4j
@RequiredArgsConstructor
public class DepartmentController {
    private final IDepartmentService departmentService;
    private final DoctorProfileMapper doctorMapper; // Changed from private final IDoctorScheduleService doctorScheduleService;
    @GetMapping
    @RequiresPermissions({"departments:view"})
    public Result<Page<Department>> list(
            @RequestParam(required = false) String name,  // 查询条件，可选
            @RequestParam Long page,
            @RequestParam Long pageSize) {

        log.info("查询科室列表，当前页：{}，页大小：{}，科室名称={}", page, pageSize, name);

        // 创建分页对象
        Page<Department> pageParam = new Page<>(page, pageSize);

        // 创建条件构造器
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name); // 模糊查询科室名称
        }

        // 执行分页查询
        Page<Department> pageResult = departmentService.page(pageParam, queryWrapper);

        return Result.success(pageResult);
    }


    @PostMapping
    @RequiresPermissions({"departments:create"})
    public Result save(@RequestBody DepartmentDTO departmentDTO) {

        log.info("添加科室：{}", departmentDTO);
        //添加科室
        departmentService.save(BeanUtil.copyProperties(departmentDTO, Department.class));
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequiresPermissions({"departments:update"})
    public Result update(@PathVariable Long id, @RequestBody DepartmentDTO departmentDTO) {
        log.info("修改科室：{}", departmentDTO);
        Department department = BeanUtil.copyProperties(departmentDTO, Department.class);
        department.setId(id);
        departmentService.updateById(department);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    @RequiresPermissions({"departments:delete"})
    @Transactional
    public Result delete(@PathVariable Long id) {
        log.info("删除科室：{}", id);
        doctorMapper.getDoctorByDepartmentId( id);
        if (doctorMapper.getDoctorByDepartmentId( id) != null) {
            return Result.error("科室下有医生，无法删除");
        }
        departmentService.removeById(id);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<List<Department>> list() {
        log.info("查询所有科室");
        return Result.success(departmentService.list());
    }
}
