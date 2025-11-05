package com.hjm.interceptor;

import com.hjm.context.PatientContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {//判断当前拦截到的是Controller的方法还是其他资源

        if (PatientContext.getPatient() == null) {
             response.setStatus(401);
             return false;
        }
        return true;
    }
}
