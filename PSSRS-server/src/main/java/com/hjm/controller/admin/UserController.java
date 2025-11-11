package com.hjm.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjm.pojo.DTO.AdminDTO;
import com.hjm.pojo.DTO.DoctorDTO;
import com.hjm.pojo.DTO.UserLoginDTO;
import com.hjm.pojo.Entity.DoctorProfile;
import com.hjm.pojo.Entity.User;
import com.hjm.pojo.Entity.UserRole;
import com.hjm.properties.JwtProperties;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IDoctorProfileService;
import com.hjm.service.IRoleService;
import com.hjm.service.IUserRoleService;
import com.hjm.service.IUserService;
import com.hjm.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统用户表（医生/管理员） 前端控制器
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IRoleService roleService;
    private final IDoctorProfileService doctorProfileService;
    private final JwtProperties jwtProperties;
    private final IUserRoleService userRoleService;

    @PostMapping("/auth/login")
    public Result login(@RequestBody UserLoginDTO userLoginDTO) throws AccountNotFoundException {
        log.info("用户登录：{}", userLoginDTO);
        User user = userService.userLogin(userLoginDTO);

        List<String> roles = roleService.getRoleNamesByUserId(user.getId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("roles", roles);


        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), userInfo);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userInfo);

        return Result.success(data);
    }
    // ==================== 医生管理接口 ====================
    
    /**
     * 注册医生账号
     */
    @PostMapping("/doctors")
    public Result registerDoctor(@RequestBody DoctorDTO doctorDTO) {
        log.info("注册医生账号：{}", doctorDTO);
        userService.registerDoctor(doctorDTO);
        return Result.success();
    }
    /**
     * 查询医生列表（分页）
     */
    @GetMapping("/doctors")
    public Result<PageResult> listDoctors(
            @RequestParam(required = false,value = "username") String name,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam("page") Long page,
            @RequestParam("size") Long pageSize
    ) {
        log.info("查询医生列表，当前页：{}，页大小：{}，医生名称={}", page, pageSize, name);
        PageResult result = userService.listDoctors(name, departmentId, page, pageSize);
        return Result.success(result);
    }
    /**
     * 删除医生账号
     */
    @DeleteMapping("/doctors/{id}")
    public Result deleteDoctor(@PathVariable Long id) {
        log.info("删除医生账号：{}", id);
        //逻辑删除
        userService.removeById(id);
        userRoleService.remove(new QueryWrapper<UserRole>().eq("user_id", id));
        doctorProfileService.remove(new QueryWrapper<DoctorProfile>().eq("user_id", id));
        return Result.success();
    }
    /**
     * 修改医生信息
     */
    @PutMapping("/doctors/{id}")
    public Result updateDoctor(@PathVariable Long id, @RequestBody DoctorDTO doctorDTO) {
        log.info("修改医生信息：{}", doctorDTO);
        return userService.updateDoctor(id, doctorDTO);
    }
    /**
     * 根据科室查询医生
     */
    @GetMapping("/doctors/department/{departmentId}")
    public Result<List<Map<String,Object>>> listDoctorsByDepartmentId(@PathVariable Long departmentId) {
        log.info("根据科室查询医生：{}", departmentId);

        return userService.listDoctorsByDepartmentId(departmentId);
    }

    @GetMapping("/admins")
    public Result<PageResult> listAdmins(
            @RequestParam(required = false,value = "username") String name,
            @RequestParam(required = false) Integer status,
            @RequestParam("page") Long page,
            @RequestParam("size") Long pageSize
    ) {
        log.info("查询管理员列表，当前页：{}，页大小：{}，管理员名称={}", page, pageSize, name);
        PageResult result = userService.listAdmins(name,status, page, pageSize);
        return Result.success(result);
    }
    @PostMapping("/admins")
    public Result registerAdmin(@RequestBody AdminDTO adminDTO) {
        log.info("注册管理员账号：{}", adminDTO);
        userService.registerAdmin(adminDTO);
        return Result.success();
    }

    @PutMapping("/admins/{id}")
    public Result updateAdmin(@PathVariable Long id, @RequestBody AdminDTO adminDTO) {
        log.info("修改管理员信息：{}", adminDTO);
        return userService.updateAdmin(id, adminDTO);
    }

    @DeleteMapping("/admins/{id}")
    public Result deleteAdmin(@PathVariable Long id) {
        log.info("删除管理员账号：{}", id);
        //逻辑删除
        userService.removeById(id);
        userRoleService.remove(new QueryWrapper<UserRole>().eq("user_id", id));
        return Result.success();
    }

}
