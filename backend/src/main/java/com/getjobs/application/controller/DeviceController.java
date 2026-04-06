package com.getjobs.application.controller;

import com.getjobs.application.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户设备管理控制器
 */
@RestController
@RequestMapping("/api/admin/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 查询用户绑定的设备
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDevices(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", deviceService.getUserDevices(userId));
        result.put("count", deviceService.getDeviceCount(userId));
        return ResponseEntity.ok(result);
    }

    /**
     * 解绑用户设备
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> unbindDevice(
            @PathVariable Long userId,
            @RequestParam String deviceFingerprint) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = deviceService.unbindDevice(userId, deviceFingerprint);
            result.put("success", success);
            result.put("message", success ? "设备已解绑" : "解绑失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
