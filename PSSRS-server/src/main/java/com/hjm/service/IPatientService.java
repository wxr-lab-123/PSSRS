package com.hjm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjm.pojo.DTO.PatientLoginDTO;
import com.hjm.pojo.DTO.PatientRegisterDTO;
import com.hjm.pojo.DTO.PatientResetPwdDTO;
import com.hjm.pojo.Entity.Patient;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 患者用户表 服务类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public interface IPatientService extends IService<Patient> {

    Result sendCode(String phone);

    Result login(PatientLoginDTO patientLoginDTO);

    Result patientRegister(PatientRegisterDTO patientRegisterDTO);

    PageResult list(Integer page, Integer size, String name, String phone, String gender);

    Result resetPwd(PatientResetPwdDTO patientResetPwdDTO);

    Result createPatient(PatientRegisterDTO p);
}
