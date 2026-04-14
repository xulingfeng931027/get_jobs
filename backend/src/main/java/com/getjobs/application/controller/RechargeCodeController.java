package com.getjobs.application.controller;

import com.getjobs.application.entity.RechargeCodeEntity;
import com.getjobs.application.service.RechargeCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 充值码管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/recharge-codes")
public class RechargeCodeController {

    @Autowired
    private RechargeCodeService rechargeCodeService;

    /**
     * 批量生成充值码
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateCodes(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            int count = (int) request.get("count");
            String type = (String) request.getOrDefault("type", "count");
            int applicationCount = request.get("applicationCount") != null ? (int) request.get("applicationCount") : 0;
            int subscriptionDays = request.get("subscriptionDays") != null ? (int) request.get("subscriptionDays") : 0;
            int bonus = (int) request.getOrDefault("bonus", 0);
            String createdBy = (String) request.get("createdBy");

            if (count <= 0 || count > 1000) {
                result.put("success", false);
                result.put("message", "生成数量必须在 1-1000 之间");
                return ResponseEntity.badRequest().body(result);
            }

            if ("count".equals(type) && applicationCount <= 0) {
                result.put("success", false);
                result.put("message", "次数码必须指定投递次数");
                return ResponseEntity.badRequest().body(result);
            }

            if ("subscription".equals(type) && subscriptionDays <= 0) {
                result.put("success", false);
                result.put("message", "订阅码必须指定订阅天数");
                return ResponseEntity.badRequest().body(result);
            }

            Map<String, Object> data = rechargeCodeService.generateCodes(count, type, applicationCount, subscriptionDays, bonus, createdBy);
            result.put("success", true);
            result.put("message", "成功生成 " + count + " 个充值码");
            result.put("data", data);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("生成充值码失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 查询充值码列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listCodes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String batchNo) {

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", rechargeCodeService.listCodes(page, size, status, batchNo));
        return ResponseEntity.ok(result);
    }

    /**
     * 查询单个充值码详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCodeDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        RechargeCodeEntity entity = rechargeCodeService.getCodeDetail(id);

        if (entity == null) {
            result.put("success", false);
            result.put("message", "充值码不存在");
            return ResponseEntity.status(404).body(result);
        }

        result.put("success", true);
        result.put("data", entity);
        return ResponseEntity.ok(result);
    }

    /**
     * 冻结充值码
     */
    @PostMapping("/{id}/freeze")
    public ResponseEntity<Map<String, Object>> freezeCode(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = rechargeCodeService.freezeCode(id);
            result.put("success", success);
            result.put("message", success ? "已冻结" : "操作失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("冻结充值码失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 作废充值码
     */
    @PostMapping("/{id}/invalidate")
    public ResponseEntity<Map<String, Object>> invalidateCode(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = rechargeCodeService.invalidateCode(id);
            result.put("success", success);
            result.put("message", success ? "已作废" : "操作失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("作废充值码失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 充值码统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCodeStats(@RequestParam(required = false) String batchNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", rechargeCodeService.getCodeStats(batchNo));
        return ResponseEntity.ok(result);
    }
}
