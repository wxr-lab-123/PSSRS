package com.hjm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.constant.MessageConstant;
import com.hjm.constant.PasswordConstant;
import com.hjm.constant.StatusConstant;
import com.hjm.exception.AccountLockedException;
import com.hjm.exception.PasswordErrorException;
import com.hjm.mapper.DoctorProfileMapper;
import com.hjm.mapper.UserMapper;
import com.hjm.pojo.DTO.AdminDTO;
import com.hjm.pojo.DTO.DoctorDTO;
import com.hjm.pojo.DTO.UserLoginDTO;
import com.hjm.pojo.Entity.DoctorProfile;
import com.hjm.pojo.Entity.User;
import com.hjm.pojo.Entity.UserRole;
import com.hjm.pojo.VO.DoctorVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IDoctorProfileService;
import com.hjm.service.IUserRoleService;
import com.hjm.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.security.auth.login.AccountNotFoundException;

import java.util.*;

import static com.hjm.constant.MyConstant.*;

/**
 * <p>
 * 系统用户表（医生/管理员） 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private IDoctorProfileService doctorProfileService;

    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private UserMapper baseMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DoctorProfileMapper doctorProfileMapper;

    /**
     * 用户登录功能
     * 根据用户名查询用户信息，并进行密码比对和状态校验
     *
     * @param userLoginDTO 登录传输对象，包含用户名和密码
     * @return 查询到的用户实体对象
     * @throws AccountNotFoundException 当用户不存在时抛出该异常
     */
    @Override
    public User userLogin(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        User user = getOne(new QueryWrapper<User>().eq("username", username));

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            throw new com.hjm.exception.AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //明文密码 MD5 处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return user;
    }

    /**
     * 注册医生账号
     * 包括创建用户基本信息、医生档案以及分配医生角色
     *
     * @param doctorDTO 医生注册传输对象
     */
    @Override
    @Transactional
    public void registerDoctor(DoctorDTO doctorDTO) {
        if (doctorDTO == null) {
            throw new RuntimeException("医生信息不能为空");
        }
        
        // 1. 创建用户基本信息
        User user = new User();
        user.setUsername(doctorDTO.getUsername());
        user.setPhone(doctorDTO.getPhone());
        user.setGender(doctorDTO.getGender());
        user.setStatus(doctorDTO.getStatus());
        user.setName(Default_Name + getRandomCode(4));
        user.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        save(user);

        // 2. 创建医生档案
        DoctorProfile doctorProfile = new DoctorProfile();
        BeanUtil.copyProperties(doctorDTO, doctorProfile);
        User savedUser = getOne(new QueryWrapper<User>().eq("username", doctorDTO.getUsername()));
        doctorProfile.setUserId(savedUser.getId());
        doctorProfileService.save(doctorProfile);

        // 3. 分配医生角色
        UserRole userRole = new UserRole();
        userRole.setUserId(savedUser.getId());
        userRole.setRoleId(ROLE_DOCTOR_ID);
        userRoleService.save(userRole);
    }

    /**
     * 注册管理员账号
     * 包括创建用户基本信息并分配管理员角色
     *
     * @param adminDTO 管理员注册传输对象
     */
    @Override
    @Transactional
    public void registerAdmin(AdminDTO adminDTO) {
        if (adminDTO == null) {
            throw new RuntimeException("管理员信息不能为空");
        }
        
        // 1. 创建用户基本信息
        User user = new User();
        user.setUsername(adminDTO.getUsername());
        user.setName(adminDTO.getName());
        user.setPhone(adminDTO.getPhone());
        user.setGender(adminDTO.getGender());
        user.setStatus(adminDTO.getStatus());
        user.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        save(user);

        // 2. 分配管理员角色

        User savedUser = getOne(new QueryWrapper<User>().eq("username", adminDTO.getUsername()));

        List<UserRole> userRoleList = new ArrayList<>();
        List<String> roles = adminDTO.getRoles();

        for (String role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUserId(savedUser.getId());
            if (role.equals("ADMIN")){
                userRole.setRoleId(ROLE_ADMIN_ID);
            }
            if (role.equals("DOCTOR"))
                userRole.setRoleId(ROLE_DOCTOR_ID);
            userRoleList.add(userRole);
        }
        userRoleService.saveBatch(userRoleList);
    }

    /**
     * 查询医生列表（分页）
     */
    @Override
    public PageResult listDoctors(String name, Integer departmentId, Long page, Long pageSize) {
        Page<DoctorVO> pageInfo = new Page<>(page, pageSize);
        Page<DoctorVO> list = userMapper.listUsers(pageInfo, name, departmentId);
        PageResult pageResult = new PageResult(list.getTotal(), list.getRecords());
        return pageResult;
    }

    @Override
    @Transactional
    public Result updateDoctor(Long id, DoctorDTO doctorDTO) {
        User user = new User();
        user.setId(id);
        user.setName(doctorDTO.getUsername());
        user.setPhone(doctorDTO.getPhone());
        user.setGender(doctorDTO.getGender());
        user.setStatus(doctorDTO.getStatus());
        updateById(user);
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setTitle(doctorDTO.getTitle());
        doctorProfile.setDepartmentId(doctorDTO.getDepartmentId());
        doctorProfile.setUserId(id);
        doctorProfile.setDescription(doctorDTO.getDescription());
        doctorProfile.setImage(doctorDTO.getImage());
        doctorProfileMapper.updateByUserId(doctorProfile);
        return Result.success();
    }

    @Override
    public Result<List<Map<String,Object>>> listDoctorsByDepartmentId(Long departmentId) {

        List<DoctorProfile> doctorProfiles = doctorProfileMapper.getDoctorByDepartmentId(departmentId);
        List<Map<String,Object>> list = new ArrayList<>();
        for (DoctorProfile doctorProfile : doctorProfiles) {
            User userInfo = getById(doctorProfile.getUserId());
            if (userInfo.getIsDeleted()==0){
            Map<String,Object> map = new HashMap<>();
            map.put("id", doctorProfile.getUserId());
            map.put("username", userInfo.getName());
            list.add(map);
            }
        }
        return Result.success(list);
    }

    @Override
    public PageResult listAdmins(String name, Integer status, Long page, Long pageSize) {
        Page<User> pageInfo = new Page<>(page, pageSize);
        List<User> list = userMapper.listAdmins(pageInfo, name, status);
        return new PageResult(pageInfo.getTotal(), list);
    }

    @Override
    @Transactional
    public Result updateAdmin(Long id, AdminDTO adminDTO) {

        User user = new User();
        user.setId(id);
        user.setName(adminDTO.getName());
        user.setPhone(adminDTO.getPhone());
        user.setGender(adminDTO.getGender());
        user.setStatus(adminDTO.getStatus());
        updateById(user);
        List<UserRole> userRoleList = new ArrayList<>();
        List<String> roles = adminDTO.getRoles();
        userRoleService.remove(new QueryWrapper<UserRole>().eq("user_id", id));
        for (String role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUserId(id);
            if (role.equals("ADMIN")){
                userRole.setRoleId(ROLE_ADMIN_ID);
            }
            if (role.equals("DOCTOR"))
                userRole.setRoleId(ROLE_DOCTOR_ID);
            userRoleList.add(userRole);
        }
        userRoleService.saveBatch(userRoleList);
        return Result.success();
    }



    /**
     * 生成指定长度的随机字符串
     * 字符集包括大写字母和数字
     *
     * @param length 随机字符串长度
     * @return 生成的随机字符串
     */
    private String getRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
