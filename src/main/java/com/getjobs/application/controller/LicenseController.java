package com.getjobs.application.controller;

import com.getjobs.application.entity.LicenseKeyEntity;
import com.getjobs.application.service.LicenseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.getjobs.application.mapper.LicenseKeyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端授权密钥管理控制器
 */
@RestController
@RequestMapping("/api/admin/licenses")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private LicenseKeyMapper licenseKeyMapper;

    /**
     * 生成授权密钥
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateLicense(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String type = (String) request.get("type");
            int value = (int) request.get("value");

            if (!"count".equals(type) && !"subscription".equals(type)) {
                result.put("success", false);
                result.put("message", "授权类型必须是 count 或 subscription");
                return ResponseEntity.badRequest().body(result);
            }

            if (value <= 0) {
                result.put("success", false);
                result.put("message", "授权值必须大于0");
                return ResponseEntity.badRequest().body(result);
            }

            String licenseKey = licenseService.generateLicenseKey(userId, type, value);
            result.put("success", true);
            result.put("message", "授权密钥生成成功");
            result.put("data", Map.of("licenseKey", licenseKey, "type", type, "value", value));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 查询授权密钥列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listLicenses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {

        Map<String, Object> result = new HashMap<>();

        QueryWrapper<LicenseKeyEntity> queryWrapper = new QueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");

        Page<LicenseKeyEntity> pageRequest = new Page<>(page, size);
        Page<LicenseKeyEntity> pageResult = licenseKeyMapper.selectPage(pageRequest, queryWrapper);

        result.put("success", true);
        result.put("data", Map.of(
            "list", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "page", pageResult.getCurrent(),
            "size", pageResult.getSize(),
            "totalPages", pageResult.getPages()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 冻结授权密钥
     */
    @PostMapping("/{id}/freeze")
    public ResponseEntity<Map<String, Object>> freezeLicense(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = licenseService.freezeLicenseKey(id);
            result.put("success", success);
            result.put("message", success ? "已冻结" : "操作失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 授权统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLicenseStats() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<LicenseKeyEntity> queryWrapper = new QueryWrapper<>();
        long total = licenseKeyMapper.selectCount(queryWrapper);
        
        queryWrapper.eq("status", 1);
        long active = licenseKeyMapper.selectCount(queryWrapper);
        
        queryWrapper.eq("status", 0);
        long used = licenseKeyMapper.selectCount(queryWrapper);
        
        queryWrapper.eq("status", 2);
        long frozen = licenseKeyMapper.selectCount(queryWrapper);

        result.put("success", true);
        result.put("data", Map.of(
            "total", total,
            "active", active,
            "used", used,
            "frozen", frozen
        ));

        return ResponseEntity.ok(result);
    }
}
