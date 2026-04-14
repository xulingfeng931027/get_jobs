package com.getjobs.application.controller.admin;

import com.getjobs.application.service.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理员认证控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> result = new HashMap<>();

        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || password == null) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            String token = adminAuthService.login(username, password);

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", Map.of(
                    "token", token,
                    "username", username
            ));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(401).body(result);
        }
    }

    /**
     * 验证 Token
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> result = new HashMap<>();

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            result.put("success", false);
            result.put("message", "Token无效");
            return ResponseEntity.status(401).body(result);
        }

        String token = authorization.substring(7);
        Long userId = adminAuthService.getUserIdFromToken(token);

        if (userId == null) {
            result.put("success", false);
            result.put("message", "Token无效或已过期");
            return ResponseEntity.status(401).body(result);
        }

        String username = adminAuthService.getUsernameFromToken(token);
        result.put("success", true);
        result.put("data", Map.of(
                "userId", userId,
                "username", username
        ));

        return ResponseEntity.ok(result);
    }
}
