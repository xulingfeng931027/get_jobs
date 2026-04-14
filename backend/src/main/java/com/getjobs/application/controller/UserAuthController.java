package com.getjobs.application.controller;


import com.getjobs.application.service.BillingService;
import com.getjobs.application.service.DeviceService;
import com.getjobs.application.service.UserAuthService;
import com.getjobs.worker.utils.MachineIdProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserAuthController {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private BillingService billingService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String phone = request.get("phone");
        String password = request.get("password");

        if (username == null || password == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        if (password.length() < 6) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "密码长度不能少于6位");
            return ResponseEntity.badRequest().body(result);
        }

        Map<String, Object> result = userAuthService.register(username, email, phone, password);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        Map<String, Object> result = userAuthService.login(username, password);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestAttribute(value = "userId", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        Map<String, Object> profile = userAuthService.getUserProfile(userId);
        if (profile == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(result);
        }

        // 添加计费信息
        Map<String, Object> billingInfo = billingService.getUserBillingInfo(userId);
        profile.put("billing", billingInfo);

        result.put("success", true);
        result.put("data", profile);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        String email = request.get("email");
        String phone = request.get("phone");
        result = userAuthService.updateUserProfile(userId, email, phone);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            result.put("success", false);
            result.put("message", "旧密码和新密码不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        result = userAuthService.changePassword(userId, oldPassword, newPassword);

        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 用户登出（同时解绑当前设备）
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        String sessionId = request.get("sessionId");
        String machineId = MachineIdProvider.getMachineId();

        // 1. 使 session 失效
        if (sessionId != null && !sessionId.isBlank()) {
            userAuthService.invalidateSession(sessionId);
        }

        // 2. 解绑当前设备（释放设备槽位）
        boolean unbound = deviceService.unbindDevice(userId, machineId);
        if (unbound) {
            log.info("[用户登出] 用户{}已解绑设备: {}", userId, machineId);
            result.put("deviceUnbound", true);
        } else {
            result.put("deviceUnbound", false);
        }

        result.put("success", true);
        result.put("message", "登出成功");
        return ResponseEntity.ok(result);
    }
}
