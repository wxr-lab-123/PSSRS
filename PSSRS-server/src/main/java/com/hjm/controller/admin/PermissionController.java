package com.hjm.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.Entity.Permission;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.security.RequiresPermissions;
import com.hjm.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    @GetMapping
    @RequiresPermissions({"permissions:view"})
    public Result<PageResult> list(@RequestParam(required = false) String code,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        Page<Permission> pg = permissionService.page(new Page<>(page, size), new QueryWrapper<Permission>().like(code != null, "code", code));
        return Result.success(new PageResult(pg.getTotal(), pg.getRecords()));
    }

    @PostMapping
    @RequiresPermissions({"permissions:create"})
    public Result create(@RequestBody Permission p) {
        permissionService.save(p);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequiresPermissions({"permissions:update"})
    public Result update(@PathVariable Long id, @RequestBody Permission p) {
        p.setId(id);
        permissionService.updateById(p);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions({"permissions:delete"})
    public Result delete(@PathVariable Long id) {
        permissionService.removeById(id);
        return Result.success();
    }
}