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

        // 5. 刷新 token 的有效期
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        return true; // 校验通过放行
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal
        PatientContext.removePatient();
    }
}
