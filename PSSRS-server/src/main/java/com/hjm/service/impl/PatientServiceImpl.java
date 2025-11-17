package com.hjm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjm.constant.RedisConstants;
import com.hjm.exception.AccountNotFoundException;
import com.hjm.mapper.PatientMapper;
import com.hjm.pojo.DTO.PatientDTO;
import com.hjm.pojo.DTO.PatientLoginDTO;
import com.hjm.pojo.DTO.PatientRegisterDTO;
import com.hjm.pojo.Entity.Patient;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IPatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 患者用户表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
@Slf4j
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        //检验手机号格式
        if (phone.isEmpty()) {
            return Result.error("手机号为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式错误");
        }
        //生成验证码
        String s = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, s, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //发送验证码
        log.info("发送验证码成功: {}",s);
        return Result.success();
    }

    @Override
    public Result login(PatientLoginDTO patientLoginDTO) {
        if (patientLoginDTO.getPhone().isEmpty() || patientLoginDTO.getPassword().isEmpty()) {
            throw new AccountNotFoundException("参数错误");
        }
        if (!patientLoginDTO.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new AccountNotFoundException("手机号格式错误");
        }
        Patient patient = query().eq("phone", patientLoginDTO.getPhone()).one();
        if (patient == null) {
            throw new AccountNotFoundException("用户不存在");
        }
        if (!patient.getPassword().equals(DigestUtils.md5DigestAsHex(patientLoginDTO.getPassword().getBytes()))) {
            throw new AccountNotFoundException("密码错误");
        }
        // 保存用户信息到redis
        // 生成token,作为登录凭证
        String token = UUID.randomUUID().toString();
        // 将Patient转为hash存储
        Map<String, Object> patientMap = new HashMap<>();
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setName(patient.getName());
        patientDTO.setPhone(patient.getPhone());
        patientDTO.setId(patient.getId());
        patientMap = BeanUtil.beanToMap(patientDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 存储
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, patientMap);
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 返回 Token
        return Result.success(token);
    }

    @Override
    public Result patientRegister(PatientRegisterDTO patientRegisterDTO) {
        if (patientRegisterDTO == null) {
            return Result.error("参数错误");
        }
        if (patientRegisterDTO.getName().isEmpty() || patientRegisterDTO.getPassword().isEmpty() || patientRegisterDTO.getPhone().isEmpty() || patientRegisterDTO.getCode().isEmpty() || patientRegisterDTO.getIdCard().isEmpty()) {
            return Result.error("参数错误");
        }
        if (!patientRegisterDTO.getPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式错误");
        }
        if (!patientRegisterDTO.getPassword().equals(patientRegisterDTO.getConfirmPassword()))
            return Result.error("密码不一致");
        if (!patientRegisterDTO.getIdCard().matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"))
            return Result.error("身份证号格式错误");
        if (!patientRegisterDTO.getCode().equals(stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + patientRegisterDTO.getPhone()))) {
            return Result.error("验证码错误");
        }
        if (query().eq("phone", patientRegisterDTO.getPhone()).one() != null) {
            return Result.error("手机号已存在");
        }
        Patient patient = new Patient();
        BeanUtil.copyProperties(patientRegisterDTO, patient);
        String password = patient.getPassword();
        patient.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        patient.setAge(getAgeByIdCard(patient.getIdCard()));
        save(patient);
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + patientRegisterDTO.getPhone());
        return Result.success("注册成功");
    }

    @Override
    public PageResult list(Integer page, Integer size, String name, String phone, String gender) {
        Page pageParam = new Page<>(page, size);
        Page<Patient> pageResult = page(pageParam, new QueryWrapper<Patient>()
                        .like(name != null, "name", name)
                        .like(phone != null, "phone", phone)
                        .like(gender != null, "gender", gender));
        PageResult pageResult1 = new PageResult(pageResult.getTotal(), pageResult.getRecords());
        return pageResult1;
    }

    public static int getAgeByIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }

        // 提取出生年月日
        String birth = idCard.substring(6, 14); // 第7到14位
        int year = Integer.parseInt(birth.substring(0, 4));
        int month = Integer.parseInt(birth.substring(4, 6));
        int day = Integer.parseInt(birth.substring(6, 8));

        // 当前日期
        LocalDate now = LocalDate.now();
        LocalDate birthDate = LocalDate.of(year, month, day);

        // 计算年龄
        return Period.between(birthDate, now).getYears();
    }

}
