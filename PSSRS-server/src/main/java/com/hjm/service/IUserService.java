package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.DTO.AdminDTO;
import com.hjm.pojo.DTO.DoctorDTO;
import com.hjm.pojo.DTO.UserLoginDTO;
import com.hjm.pojo.Entity.User;
import com.hjm.pojo.VO.UserVO;
import com.hjm.result.PageResult;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

/**
 * <p>
 * 系统用户表（医生/管理员） 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public interface IUserService extends IService<User> {

    /**
     * 用户登录
     */
    User userLogin(UserLoginDTO userLoginDTO) throws AccountNotFoundException;

    /**
     * 注册医生账号
     */
    void registerDoctor(DoctorDTO doctorDTO);

    /**
     * 注册管理员账号
     */
    void registerAdmin(AdminDTO adminDTO);

    /**
     * 查询医生列表（分页）
     */
    PageResult listDoctors(String name, Integer departmentId, Long page, Long pageSize);

}
