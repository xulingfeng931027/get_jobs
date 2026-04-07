package com.getjobs.application.controller;

import com.getjobs.application.entity.PackageDefinitionEntity;
import com.getjobs.application.entity.UserPackageEntity;
import com.getjobs.application.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台套餐管理控制器
 */
@RestController
@RequestMapping("/api/admin/package")
public class AdminPackageController {

    @Autowired
    private PackageService packageService;

    /**
     * 开通套餐
     */
    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activatePackage(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Integer packageType = Integer.parseInt(request.get("packageType").toString());

            if (userId == null || packageType == null) {
                result.put("success", false);
                result.put("message", "参数错误");
                return ResponseEntity.badRequest().body(result);
            }

            Map<String, Object> activateResult = packageService.activatePackage(userId, packageType);
            return ResponseEntity.ok(activateResult);
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "参数格式错误");
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 取消用户套餐
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPackage(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long packageId = Long.parseLong(request.get("packageId").toString());

            boolean success = packageService.cancelPackage(userId, packageId);
            result.put("success", success);
            result.put("message", success ? "套餐已取消" : "取消失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 查询用户套餐状态
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPackage(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> packageStatus = packageService.getPackageStatus(userId);
            List<UserPackageEntity> packages = packageService.getUserPackages(userId);

            result.put("success", true);
            result.put("data", packageStatus);
            result.put("history", packages);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 查询所有可用的套餐类型
     */
    @GetMapping("/definitions")
    public ResponseEntity<Map<String, Object>> getPackageDefinitions() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<PackageDefinitionEntity> definitions = packageService.getPackageDefinitions();
            result.put("success", true);
            result.put("data", definitions);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
