package com.hjm.config;

import com.hjm.interceptor.JwtTokenUserInterceptor;
import com.hjm.interceptor.PermissionInterceptor;
import com.hjm.interceptor.LoginInterceptor;
import com.hjm.interceptor.ReflsahInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    @Autowired
    private PermissionInterceptor permissionInterceptor;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册自定义拦截器...");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");

        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/admin/**");

    }
}
