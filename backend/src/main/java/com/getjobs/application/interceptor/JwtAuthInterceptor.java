package com.getjobs.application.interceptor;

import com.getjobs.application.service.UserAuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT认证拦截器
 * 解析Authorization头中的Bearer Token，验证后将userId设置到RequestAttribute
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final UserAuthService userAuthService;
    @Value("${user.jwt.secret:GetJobs-User-Secret-Key-2026!@#$}")
    private String jwtSecret;

    public JwtAuthInterceptor(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许OPTIONS请求通过（CORS预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 没有token，允许请求继续（由Controller决定是否必须登录）
            return true;
        }

        String token = authHeader.substring(7);
        
        try {
            // 验证token并提取userId
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userIdStr = claims.getSubject();
            Long userId = Long.parseLong(userIdStr);
            
            // 将userId设置到RequestAttribute，供Controller使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", claims.get("username", String.class));
            
            log.debug("JWT认证成功 - userId: {}, username: {}", userId, claims.get("username", String.class));
            
            return true;
        } catch (Exception e) {
            log.warn("JWT认证失败: {}", e.getMessage());
            // Token无效，允许请求继续（由Controller处理未认证情况）
            return true;
        }
    }
}
