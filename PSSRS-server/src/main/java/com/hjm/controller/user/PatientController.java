package com.hjm.controller.user;


import cn.hutool.core.bean.BeanUtil;
import com.hjm.context.PatientContext;
import com.hjm.context.UserPatientContext;
import com.hjm.exception.PatientException;
import com.hjm.pojo.DTO.*;
import com.hjm.pojo.Entity.Patient;
import com.hjm.pojo.DTO.UserPatientDTO;
import com.hjm.pojo.Entity.UserPatient;
import com.hjm.result.Result;
import com.hjm.service.IPatientService;
import com.hjm.service.IUserPatientService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final IUserPatientService userPatientService;
    /**
     * 发送手机验证码
     */
    @RequestMapping("/sms/sendRegisterCode")
    public Result sendCode(@RequestParam("phone") String phone) {
        patientService.sendCode(phone);
        return Result.success("发送成功");
    }

    /**
     * 登录功能
     * @param patientLoginDTO 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/user/login")
    public Result login(@RequestBody PatientLoginDTO patientLoginDTO){
        return Result.error("不支持该登录方式，请使用微信登录");
    }

    /**
     * 用户注册
     */
    @PostMapping("/user/register")
    public Result register(@RequestBody PatientRegisterDTO patientRegisterDTO) {
        return Result.error("不支持注册，请直接使用微信登录");
    }

    @GetMapping("/user/info")
    public Result<UserPatient> getCurrentPatient() {
        Long userpId = UserPatientContext.get().getId();
        return Result.success(userPatientService.getById(userpId));
    }

    @PutMapping("/user/update")
    public Result update(@RequestBody PatientUpdateDTO patientUpdateDTO) {
        Patient patient = BeanUtil.copyProperties(patientUpdateDTO, Patient.class);
        patient.setId(PatientContext.getPatient().getId());
        patientService.updateById(patient);
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String token = null;
            if (attrs != null) {
                token = attrs.getRequest().getHeader("authorization");
                if (token == null || token.isBlank()) token = attrs.getRequest().getHeader("Authorization");
            }
            if (token != null && !token.isBlank()) {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(PatientContext.getPatient().getId()));
                map.put("name", patient.getName() == null ? "" : String.valueOf(patient.getName()));
                map.put("phone", patient.getPhone() == null ? "" : String.valueOf(patient.getPhone()));
                stringRedisTemplate.opsForHash().putAll(com.hjm.constant.RedisConstants.LOGIN_USER_KEY + token, map);
                stringRedisTemplate.expire(com.hjm.constant.RedisConstants.LOGIN_USER_KEY + token, com.hjm.constant.RedisConstants.LOGIN_USER_TTL, java.util.concurrent.TimeUnit.MINUTES);
            }
        } catch (Exception ignored) {}
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
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String token = null;
            if (attrs != null) {
                token = attrs.getRequest().getHeader("authorization");
                if (token == null || token.isBlank()) token = attrs.getRequest().getHeader("Authorization");
            }
            if (token != null && !token.isBlank()) {
                stringRedisTemplate.delete(com.hjm.constant.RedisConstants.LOGIN_USER_KEY + token);
            }
        } catch (Exception ignored) {}
        return Result.success("登出成功");
    }

    @PostMapping("/sms/sendResetPwdCode")
    public Result sendResetPwdCode(@RequestParam String phone) {
        // 发送短信验证码并保存验证码
        return patientService.sendCode(phone);
    }
    @PostMapping("/user/resetPassword")
    public Result resetPwd(@RequestBody PatientResetPwdDTO patientResetPwdDTO) {
        return Result.error("不支持密码重置，请使用微信登录");
    }

}
