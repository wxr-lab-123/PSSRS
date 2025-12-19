package com.hjm.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hjm.constant.RedisConstants;
import com.hjm.context.PatientContext;
import com.hjm.pojo.DTO.PatientDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReflsahInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public ReflsahInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的 token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            token = request.getHeader("Authorization");
        }
        if (StrUtil.isBlank(token)) {
            return true;
        }

        // 2. 获取 Redis 中的用户信息
        Map<Object, Object> patientMap = stringRedisTemplate.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + token);
        if (patientMap.isEmpty()) {
            return true;
        }

        // 3. 将 HashMap 转为 PatientDTO
        PatientDTO patientDTO = BeanUtil.fillBeanWithMap(patientMap, new PatientDTO(), false);
        
        // 4. 保存到 ThreadLocal
        PatientContext.saveP(patientDTO);

        try {
            com.hjm.pojo.DTO.UserPatientDTO up = new com.hjm.pojo.DTO.UserPatientDTO();
            Object upid = patientMap.get("upid");
            if (upid != null) {
                try { up.setId(Long.valueOf(String.valueOf(upid))); } catch (Exception ignored2) {}
            }
            Object openid = patientMap.get("u_openid");
            if (openid != null) up.setOpenid(String.valueOf(openid));
            Object nickname = patientMap.get("u_nickname");
            if (nickname != null) up.setNickname(String.valueOf(nickname));
            Object uphone = patientMap.get("u_phone");
            if (uphone != null) up.setPhone(String.valueOf(uphone));
            Object uavatar = patientMap.get("u_avatar");
            if (uavatar != null) up.setAvatarUrl(String.valueOf(uavatar));
            com.hjm.context.UserPatientContext.save(up);
        } catch (Exception ignored) {}

        // 5. 刷新 token 的有效期
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        return true; // 校验通过放行
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal
        PatientContext.removePatient();
        com.hjm.context.UserPatientContext.remove();
    }
}
