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

/**
 * Session ID 过滤器
 * 从 X-Session-Id header 中提取 userId
 * 用于前端在无法使用 JWT 时通过 sessionId 获取用户身份
 */
@Slf4j
@Component
public class SessionIdFilter extends OncePerRequestFilter {

    @Autowired
    private UserAuthService userAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 只处理需要 userId 的路径
        String path = request.getRequestURI();

        // 如果已经有 userId（从 JWT Filter 设置），直接放行
        Object existingUserId = request.getAttribute("userId");
        if (existingUserId != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 尝试从 X-Session-Id header 获取 userId
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId != null && !sessionId.isBlank()) {
            Long userId = userAuthService.getUserIdBySession(sessionId);
            if (userId != null) {
                request.setAttribute("userId", userId);
                log.debug("[SessionId] 从 X-Session-Id 获取到 userId: {}", userId);
            }
        }

        filterChain.doFilter(request, response);
    }
}
