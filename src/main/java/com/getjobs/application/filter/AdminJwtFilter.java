package com.getjobs.application.filter;

import com.getjobs.application.service.AdminAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 后台管理员 JWT 认证过滤器
 */
@Component
public class AdminJwtFilter extends OncePerRequestFilter {

    @Autowired
    private AdminAuthService adminAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 跳过登录接口和静态资源
        if (path.startsWith("/api/admin/auth/login") ||
            path.startsWith("/static/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 只对 /api/admin/** 路径进行 JWT 验证
        if (!path.startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 Authorization Header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未登录或Token无效\"}");
            return;
        }

        String token = authHeader.substring(7);

        // 验证 Token
        Long userId = adminAuthService.getUserIdFromToken(token);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Token无效或已过期\"}");
            return;
        }

        // 将用户信息存入 request 属性，供后续使用
        request.setAttribute("adminUserId", userId);
        request.setAttribute("adminUsername", adminAuthService.getUsernameFromToken(token));

        filterChain.doFilter(request, response);
    }
}
