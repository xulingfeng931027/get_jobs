package com.getjobs.application.filter;

import com.getjobs.application.service.UserAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 用户 JWT 认证过滤器
 */
@Slf4j
@Component
public class UserJwtFilter extends OncePerRequestFilter {

    @Autowired
    private UserAuthService userAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 跳过不需要认证的路径
        if (path.startsWith("/api/user/register") ||
            path.startsWith("/api/user/login") ||
            path.startsWith("/api/admin/") ||
            path.startsWith("/api/health") ||
            path.startsWith("/api/51job/") ||
            path.startsWith("/api/boss/") ||
            path.startsWith("/api/liepin/") ||
            path.startsWith("/api/zhilian/") ||
            path.startsWith("/api/cookie/") ||
            path.startsWith("/api/ai/") ||
            path.startsWith("/assets/") ||
            path.startsWith("/dist/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // /api/config/sync 需要认证（但其他 /api/config/* 不需要）
        if (path.equals("/api/config/sync")) {
            // 继续进行 JWT 验证
        } else if (path.startsWith("/api/config/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 Authorization Header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未提供认证令牌\"}");
            return;
        }

        String token = authHeader.substring(7);
        Map<String, Object> validationResult = userAuthService.validateToken(token);

        if (!(Boolean) validationResult.get("valid")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + validationResult.get("message") + "\"}");
            return;
        }

        // 将用户信息设置到请求属性中
        request.setAttribute("userId", validationResult.get("userId"));
        request.setAttribute("username", validationResult.get("username"));

        filterChain.doFilter(request, response);
    }
}
