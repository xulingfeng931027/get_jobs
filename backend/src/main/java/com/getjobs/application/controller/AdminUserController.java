package com.getjobs.application.controller;

import com.getjobs.application.entity.RechargeLogEntity;
import com.getjobs.application.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 查询用户列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", adminUserService.listUsers(page, size, status, keyword));
        return ResponseEntity.ok(result);
    }

    /**
     * 查询用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("data", adminUserService.getUserDetail(id));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(404).body(result);
        }
    }

    /**
     * 禁用用户
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = adminUserService.toggleUserStatus(id, 0);
            result.put("success", success);
            result.put("message", success ? "已禁用" : "操作失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 启用用户
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = adminUserService.toggleUserStatus(id, 1);
            result.put("success", success);
            result.put("message", success ? "已启用" : "操作失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 查询用户的充值记录
     */
    @GetMapping("/{id}/recharge-logs")
    public ResponseEntity<Map<String, Object>> getUserRechargeLogs(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        List<RechargeLogEntity> logs = adminUserService.getUserRechargeLogs(id);

        result.put("success", true);
        result.put("data", logs);
        return ResponseEntity.ok(result);
    }

    /**
     * 创建C端用户
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String username = request.get("username");
            String email = request.get("email");
            String phone = request.get("phone");
            String password = request.get("password");

            if (username == null || username.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            if (password == null || password.isEmpty()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            Long userId = adminUserService.createUser(username, email, phone, password);
            result.put("success", true);
            result.put("message", "用户创建成功");
            result.put("userId", userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新C端用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String email = request.get("email") != null ? request.get("email").toString() : null;
            String phone = request.get("phone") != null ? request.get("phone").toString() : null;
            Integer status = request.get("status") != null ? Integer.parseInt(request.get("status").toString()) : null;

            adminUserService.updateUser(id, email, phone, status);
            result.put("success", true);
            result.put("message", "用户更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除C端用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            adminUserService.deleteUser(id);
            result.put("success", true);
            result.put("message", "用户删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
