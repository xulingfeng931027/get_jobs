package com.getjobs.application.service;

import com.getjobs.application.entity.AdminUserEntity;
import com.getjobs.application.mapper.AdminUserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理员认证服务
 */
@Service
public class AdminAuthService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Value("${admin.jwt.secret}")
    private String secret;

    @Value("${admin.jwt.expiration}")
    private Long expiration;

    @Value("${admin.jwt.issuer}")
    private String issuer;

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "test";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("加密后的密码: " + encodedPassword);

        // 验证密码
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证: " + matches);
    }

    /**
     * 管理员登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return JWT Token
     */
    public String login(String username, String password) {
        // 查询管理员
        AdminUserEntity admin = adminUserMapper.selectByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查状态
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        if (!BCrypt.checkpw(password, admin.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 Token
        return generateToken(admin.getId(), admin.getUsername());
    }

    /**
     * 生成 JWT Token
     */
    private String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 验证并解析 Token
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("username", String.class);
    }

    /**
     * 检查 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return true;
        return claims.getExpiration().before(new Date());
    }
}
