package com.getjobs.application.controller.admin;

import com.getjobs.application.service.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台统计控制器
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    @Autowired
    private AdminStatsService adminStatsService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", adminStatsService.getDashboardStats());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/user-trend")
    public ResponseEntity<Map<String, Object>> getUserTrend(@RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", adminStatsService.getUserTrend(days));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取充值分布统计
     */
    @GetMapping("/recharge-distribution")
    public ResponseEntity<Map<String, Object>> getRechargeDistribution() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", adminStatsService.getRechargeDistribution());
        return ResponseEntity.ok(result);
    }
}
