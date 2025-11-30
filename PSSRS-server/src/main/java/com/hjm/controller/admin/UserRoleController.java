package com.hjm.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjm.pojo.Entity.UserRole;
import com.hjm.result.Result;
import com.hjm.security.RequiresPermissions;
import com.hjm.service.IUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final IUserRoleService userRoleService;

    @PostMapping("/users/{userId}")
    @RequiresPermissions({"user_roles:assign"})
    public Result assign(@PathVariable Long userId, @RequestBody AssignReq req) {
        userRoleService.remove(new QueryWrapper<UserRole>().eq("user_id", userId));
        List<UserRole> list = new ArrayList<>();
        for (Long rid : req.getRoleIds()) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            list.add(ur);
        }
        userRoleService.saveBatch(list);
        return Result.success();
    }

    @PostMapping("/batch")
    @RequiresPermissions({"user_roles:assign"})
    public Result batchAssign(@RequestBody BatchAssignReq req) {
        for (Long userId : req.getUserIds()) {
            userRoleService.remove(new QueryWrapper<UserRole>().eq("user_id", userId));
            List<UserRole> list = new ArrayList<>();
            for (Long rid : req.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(rid);
                list.add(ur);
            }
            userRoleService.saveBatch(list);
        }
        return Result.success();
    }

    public static class AssignReq {
        private List<Long> roleIds;
        public List<Long> getRoleIds() { return roleIds; }
        public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    }
    public static class BatchAssignReq {
        private List<Long> userIds;
        private List<Long> roleIds;
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public List<Long> getRoleIds() { return roleIds; }
        public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    }
}
