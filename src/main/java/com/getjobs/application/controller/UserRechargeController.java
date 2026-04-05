package com.getjobs.application.controller;

import com.getjobs.application.service.BillingService;
import com.getjobs.application.service.RechargeCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户充值控制器
 */
@RestController
@RequestMapping("/api/billing")
public class UserRechargeController {

    @Autowired
    private RechargeCodeService rechargeCodeService;

    @Autowired
    private BillingService billingService;

    /**
     * 激活充值码
     */
    @PostMapping("/recharge")
    public ResponseEntity<Map<String, Object>> activateCode(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        String code = request.get("code");
        if (code == null || code.isEmpty()) {
            result.put("success", false);
            result.put("message", "充值码不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            result = rechargeCodeService.activateCode(userId, code);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 查询余额信息
     */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@RequestAttribute(value = "userId", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        Map<String, Object> billingInfo = billingService.getUserBillingInfo(userId);
        if (billingInfo == null) {
            result.put("success", false);
            result.put("message", "用户账户不存在");
            return ResponseEntity.badRequest().body(result);
        }

        result.put("success", true);
        result.put("data", billingInfo);
        return ResponseEntity.ok(result);
    }
}
