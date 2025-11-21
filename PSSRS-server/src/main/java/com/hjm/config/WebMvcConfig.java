package com.hjm.config;


import com.hjm.interceptor.LoginInterceptor;
import com.hjm.interceptor.ReflsahInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册用户自定义拦截器...");
        registry.addInterceptor(new ReflsahInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")
                .order(0);
        registry.addInterceptor(new LoginInterceptor()).
                addPathPatterns("/api/user/**").
                excludePathPatterns("/api/user/login", "/api/user/register", "/api/user/logout","/api/user/resetPassword")
                .order(1);
    }
}
