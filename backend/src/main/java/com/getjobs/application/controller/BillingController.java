package com.getjobs.application.controller;


import com.getjobs.application.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 计费控制器
 * 提供计费预检查等接口给 Electron 客户端
 */
@Slf4j
@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    /**
     * 投递预检查
     * POST /api/billing/pre-check
     * 验证用户是否有足够的余额或有效订阅
     */
    @PostMapping("/pre-check")
    public ResponseEntity<Map<String, Object>> preCheck(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "未提供认证令牌");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String platform = (String) request.get("platform");
            Integer expectedDeliveryCount = (Integer) request.get("expectedDeliveryCount");

            if (expectedDeliveryCount == null) {
                expectedDeliveryCount = 1;
            }

            log.info("[计费预检查] 用户:{}, 平台:{}, 期望投递数:{}", userId, platform, expectedDeliveryCount);

            // 调用 BillingService 进行检查
            Map<String, Object> checkResult = billingService.checkBeforeDelivery(userId);

            // 获取用户计费信息
            Map<String, Object> billingInfo = billingService.getUserBillingInfo(userId);

            response.put("success", true);
            response.put("allowed", checkResult.get("allowed"));
            response.put("remainingCount", checkResult.get("applicationCount"));
            response.put("hasSubscription", checkResult.get("hasSubscription"));
            response.put("subscriptionEndDate", checkResult.get("subscriptionEndDate"));
            response.put("reason", checkResult.get("reason"));
            response.put("billingInfo", billingInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[计费预检查] 检查失败", e);
            response.put("success", false);
            response.put("message", "预检查失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}