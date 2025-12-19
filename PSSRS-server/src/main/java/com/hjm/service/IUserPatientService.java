package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.Entity.UserPatient;
import com.hjm.result.Result;

public interface IUserPatientService extends IService<UserPatient> {
    Result<String> wechatLogin(String code);
}

