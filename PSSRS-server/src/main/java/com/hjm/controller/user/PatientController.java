package com.hjm.controller.user;


import cn.hutool.core.bean.BeanUtil;
import com.hjm.context.PatientContext;
import com.hjm.exception.PatientException;
import com.hjm.pojo.DTO.*;
import com.hjm.pojo.Entity.Patient;
import com.hjm.pojo.VO.PatientInfoVO;
import com.hjm.result.Result;
import com.hjm.service.IPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 患者用户表 前端控制器
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PatientController {

    private final IPatientService patientService;
    /**
     * 发送手机验证码
     */
    @RequestMapping("/sms/sendRegisterCode")
    public Result sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        return patientService.sendCode(phone);
    }

    /**
     * 登录功能
     * @param patientLoginDTO 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/user/login")
    public Result login(@RequestBody PatientLoginDTO patientLoginDTO){
        //  实现登录功能

        return patientService.login(patientLoginDTO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/user/register")
    public Result register(@RequestBody PatientRegisterDTO patientRegisterDTO) {
        return patientService.patientRegister(patientRegisterDTO);
    }

    @GetMapping("/user/info")
    public Result<PatientInfoVO> getCurrentPatient() {
        PatientDTO patient = PatientContext.getPatient();
        Long patientId = patient.getId() ;
        Patient patientInfo = patientService.getById(patientId);
        return Result.success(BeanUtil.copyProperties(patientInfo, PatientInfoVO.class));
    }

    @PutMapping("/user/update")
    public Result update(@RequestBody PatientUpdateDTO patientUpdateDTO) {
        Patient patient = BeanUtil.copyProperties(patientUpdateDTO, Patient.class);
        patient.setId(PatientContext.getPatient().getId());
        patientService.updateById(patient);
        return   Result.success();
    }

    @GetMapping("/user/validate")
    public Result<Map<String,Boolean>> validate() {
        Patient patient =patientService.getById(PatientContext.getPatient().getId()) ;
        if (patient == null) {
            throw new PatientException("用户不存在");
        }
        Boolean isValid = true;
        Boolean nameFilled = true;
        Boolean phoneValid = true;
        if (patient.getName() == null) {
            isValid = false;
            nameFilled = false;
        }
        if (patient.getPhone()== null){
            isValid = false;
            phoneValid = false;
        }
        Map<String,Boolean> map = new HashMap<>();
        map.put("isValid",isValid);
        map.put("nameFilled",nameFilled);
        map.put("phoneValid",phoneValid);
        return Result.success(map);
    }

    @PostMapping("/user/logout")
    public Result logout() {
        PatientContext.removePatient();
        return Result.success("登出成功");
    }

    @PostMapping("/sms/sendResetPwdCode")
    public Result sendResetPwdCode(@RequestParam String phone) {
        // 发送短信验证码并保存验证码
        return patientService.sendCode(phone);
    }
    @PostMapping("/user/resetPassword")
    public Result resetPwd(@RequestBody PatientResetPwdDTO patientResetPwdDTO) {
        return patientService.resetPwd(patientResetPwdDTO);
    }

}
