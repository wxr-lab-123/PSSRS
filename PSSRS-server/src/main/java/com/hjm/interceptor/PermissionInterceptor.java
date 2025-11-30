package com.hjm.interceptor;

import com.hjm.context.BaseContext;
import com.hjm.security.Logical;
import com.hjm.security.RequiresPermissions;
import com.hjm.service.IPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Resource
    private IPermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        RequiresPermissions rp = hm.getMethodAnnotation(RequiresPermissions.class);
        if (rp == null) {
            rp = hm.getBeanType().getAnnotation(RequiresPermissions.class);
        }
        if (rp == null) {
            return true;
        }
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            response.setStatus(401);
            return false;
        }
        List<String> codes = permissionService.getUserPermissionCodes(userId);
        String[] need = rp.value();
        if (rp.logical() == Logical.AND) {
            for (String n : need) {
                if (!codes.contains(n)) {
                    response.setStatus(403);
                    return false;
                }
            }
            return true;
        } else {
            for (String n : need) {
                if (codes.contains(n)) {
                    return true;
                }
            }
            response.setStatus(403);
            return false;
        }
    }
}