package com.getjobs.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getjobs.application.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置同步控制器
 * 向 Electron 客户端提供统一的配置数据
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ConfigSyncController {

    private final ConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 同步配置给客户端
     * GET /api/config/sync
     * 需要 JWT 认证
     */
    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncConfig() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取各平台配置
            Map<String, Object> bossConfig = configService.getBossConfig() != null
                    ? objectMapper.convertValue(configService.getBossConfig(), Map.class)
                    : new HashMap<>();

            Map<String, Object> liepinConfig = configService.getLiepinConfig() != null
                    ? objectMapper.convertValue(configService.getLiepinConfig(), Map.class)
                    : new HashMap<>();

            Map<String, Object> zhilianConfig = configService.getZhilianConfig() != null
                    ? objectMapper.convertValue(configService.getZhilianConfig(), Map.class)
                    : new HashMap<>();

            Map<String, Object> job51Config = configService.getJob51Config() != null
                    ? objectMapper.convertValue(configService.getJob51Config(), Map.class)
                    : new HashMap<>();

            // 获取 AI 配置
            Map<String, String> aiConfig = configService.getAiConfigs();

            // 构建响应
            Map<String, Object> configData = new HashMap<>();
            configData.put("boss", bossConfig);
            configData.put("liepin", liepinConfig);
            configData.put("zhilian", zhilianConfig);
            configData.put("job51", job51Config);

            // 获取选项配置
            Map<String, Object> optionsData = new HashMap<>();
            optionsData.put("boss", getBossOptions());
            optionsData.put("liepin", getLiepinOptions());
            optionsData.put("zhilian", getZhilianOptions());
            optionsData.put("job51", getJob51Options());

            response.put("success", true);
            response.put("timestamp", Instant.now().toString());
            response.put("config", configData);
            response.put("options", optionsData);
            response.put("aiConfig", aiConfig);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[配置同步] 获取配置失败", e);
            response.put("success", false);
            response.put("message", "获取配置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Map<String, Object> getBossOptions() {
        Map<String, Object> options = new HashMap<>();
        // 从数据库读取 Boss 选项配置
        return options;
    }

    private Map<String, Object> getLiepinOptions() {
        Map<String, Object> options = new HashMap<>();
        // 从数据库读取 Liepin 选项配置
        return options;
    }

    private Map<String, Object> getZhilianOptions() {
        Map<String, Object> options = new HashMap<>();
        // 从数据库读取 Zhilian 选项配置
        return options;
    }

    private Map<String, Object> getJob51Options() {
        Map<String, Object> options = new HashMap<>();
        // 从数据库读取 Job51 选项配置
        return options;
    }
}