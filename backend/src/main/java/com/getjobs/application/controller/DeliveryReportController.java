package com.getjobs.application.controller;


import com.getjobs.application.service.DeliveryReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 投递上报控制器
 * 接收 Electron 客户端的投递结果上报
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DeliveryReportController {

    private final DeliveryReportService deliveryReportService;

    /**
     * 接收客户端投递结果上报
     * POST /api/delivery/report
     */
    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> reportDelivery(
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
            @SuppressWarnings("unchecked")
            Map<String, Object> deliveredJobs = (Map<String, Object>) request.get("deliveredJobs");
            @SuppressWarnings("unchecked")
            Map<String, Object> filteredJobs = (Map<String, Object>) request.get("filteredJobs");
            @SuppressWarnings("unchecked")
            Map<String, Object> failedJobs = (Map<String, Object>) request.get("failedJobs");
            Integer totalCount = (Integer) request.get("totalCount");
            String deviceId = (String) request.get("deviceId");
            String clientVersion = (String) request.get("clientVersion");

            log.info("[投递上报] 用户:{}, 平台:{}, 投递数:{}, 过滤数:{}, 失败数:{}",
                    userId, platform,
                    deliveredJobs != null ? deliveredJobs.size() : 0,
                    filteredJobs != null ? filteredJobs.size() : 0,
                    failedJobs != null ? failedJobs.size() : 0);

            // 保存投递报告
            Map<String, Object> reportResult = deliveryReportService.saveDeliveryReport(
                    userId, platform, deliveredJobs, filteredJobs, failedJobs, totalCount, deviceId, clientVersion);

            // 注意：扣费已在平台控制器（BossController/LiepinController等）的投递任务完成时处理
            // 此处仅记录投递详情，不再重复扣费
            reportResult.put("deductionApplied", 0);
            reportResult.put("deductedCount", 0);
            reportResult.put("note", "计费由投递平台控制器处理");

            response.put("success", true);
            response.put("reportId", reportResult.get("reportId"));
            response.put("serverReceivedCount", reportResult.get("receivedCount"));
            response.put("deductionApplied", reportResult.get("deductionApplied"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[投递上报] 处理上报请求失败", e);
            response.put("success", false);
            response.put("message", "处理上报请求失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}