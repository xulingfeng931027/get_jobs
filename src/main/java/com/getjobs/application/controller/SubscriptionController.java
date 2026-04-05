package com.getjobs.application.controller;

import com.getjobs.application.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 订阅管理控制器
 */
@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * 查询订阅状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestAttribute(value = "userId", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        result.put("success", true);
        result.put("data", subscriptionService.getSubscriptionStatus(userId));
        return ResponseEntity.ok(result);
    }

    /**
     * 查询订阅历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        result.put("success", true);
        result.put("data", subscriptionService.getSubscriptionHistory(userId, limit));
        return ResponseEntity.ok(result);
    }

    /**
     * 取消订阅
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        boolean success = subscriptionService.cancelSubscription(userId, id);
        result.put("success", success);
        result.put("message", success ? "订阅已取消" : "取消失败");
        return ResponseEntity.ok(result);
    }
}
