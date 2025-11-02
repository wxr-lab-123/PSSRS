package com.hjm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.pojo.Entity.User;
import com.hjm.pojo.VO.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 系统用户表（医生/管理员） Mapper 接口
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询医生列表（分页）
     */
    Page<UserVO> listUsers(
            Page<UserVO> page,
            @Param("name") String name,
            @Param("departmentId") Integer departmentId
    );

}
