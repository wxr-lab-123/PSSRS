package com.hjm.controller.common;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjm.constant.RedisConstants;
import com.hjm.exception.SettingException;
import com.hjm.pojo.Entity.User;
import com.hjm.result.Result;
import com.hjm.service.IUserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class AdminSetting {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final IUserService userService;
    @RequestMapping("/sms/toChangePd")
    public Result sendCode(@RequestParam("phone") String phone) {
        //检验手机号格式
        if (phone.isEmpty()) {
            throw new SettingException("手机号为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new SettingException("手机号格式错误");
        }
        //生成验证码
        String s = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(RedisConstants.ADMIM_PD_CODE + phone, s, RedisConstants.ADMIM_PD_CODE_TTL, TimeUnit.MINUTES);
        //发送验证码
        log.info("发送验证码成功: {}",s);
        return Result.success("发送成功");
    }

    @PostMapping("/admin/changePd")
    public Result changePd(@RequestBody Map<String, String> map) {
        //检验手机号格式
        if (map.get("phone").isEmpty()) {
            throw new SettingException("手机号为空");
        }
        User user = userService.getOne(new QueryWrapper<User>().eq("phone", map.get("phone")));
        if (!map.get("phone").matches("^1[3-9]\\d{9}$")) {
            throw new SettingException("手机号格式错误");
        }
        //检验验证码
        String code = stringRedisTemplate.opsForValue().get(RedisConstants.ADMIM_PD_CODE + map.get("phone"));
        if (code == null || !code.equals(map.get("code"))) {
            throw new SettingException("验证码错误");
    }
        if (map.get("newPd").isEmpty() ) {
            throw new SettingException ("新密码不能为空");
        }
        if (map.get("newPd").length() < 6) {
            throw new SettingException("新密码长度不能小于6");
        }
        if (!map.get("newPd").equals( map.get("configPd"))){
            throw new SettingException("两次输入不同");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(map.get("newPd").getBytes()));
        userService.updateById(user);
        return Result.success("修改成功");
    }
}
