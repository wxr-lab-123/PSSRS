package com.hjm.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.Entity.Role;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.security.RequiresPermissions;
import com.hjm.service.IPermissionService;
import com.hjm.service.IRoleService;
import com.hjm.service.IRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;
    private final IPermissionService permissionService;
    private final IRolePermissionService rolePermissionService;

    @GetMapping
    @RequiresPermissions({"roles:view"})
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        Page<Role> pageParam = new Page<>(page, size);
        Page<Role> pg = roleService.page(pageParam, new QueryWrapper<Role>().like(name != null, "role_name", name));
        return Result.success(new PageResult(pg.getTotal(), pg.getRecords()));
    }

    @PostMapping
    @RequiresPermissions({"roles:create"})
    public Result create(@RequestBody Role role) {
        roleService.save(role);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequiresPermissions({"roles:update"})
    public Result update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions({"roles:delete"})
    public Result delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success();
    }

    @PostMapping("/{id}/permissions")
    @RequiresPermissions({"roles:assign"})
    public Result assign(@PathVariable Long id, @RequestBody AssignDTO dto) {
        permissionService.assignPermissionsToRole(id, dto.getAllowIds(), dto.getDenyIds());
        return Result.success();
    }

    public static class AssignDTO {
        private List<Long> allowIds;
        private List<Long> denyIds;
        public List<Long> getAllowIds() { return allowIds; }
        public void setAllowIds(List<Long> allowIds) { this.allowIds = allowIds; }
        public List<Long> getDenyIds() { return denyIds; }
        public void setDenyIds(List<Long> denyIds) { this.denyIds = denyIds; }
    }
}