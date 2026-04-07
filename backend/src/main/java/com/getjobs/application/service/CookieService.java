package com.getjobs.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.getjobs.application.entity.CookieEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Cookie服务类 - 基于Redis存储
 * 不再写入数据库
 */
@Slf4j
@Service
public class CookieService {

    private static final String COOKIE_KEY_PREFIX = "cookie:";
    private static final Duration DEFAULT_TTL = Duration.ofDays(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CookieService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 根据平台获取Cookie（使用当前用户）
     * @param platform 平台名称（boss/zhilian/job51/liepin）
     * @return Cookie实体
     */
    public CookieEntity getCookieByPlatform(String platform) {
        return getCookieByPlatformAndUserId(platform, null);
    }

    /**
     * 根据平台和用户ID获取Cookie
     * @param platform 平台名称
     * @param userId 用户ID（为null时使用默认key）
     * @return Cookie实体
     */
    public CookieEntity getCookieByPlatformAndUserId(String platform, Long userId) {
        String key = buildKey(platform, userId);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, CookieEntity.class);
            }
        } catch (Exception e) {
            log.warn("从Redis获取Cookie失败: platform={}, userId={}, error={}", platform, userId, e.getMessage());
        }
        return null;
    }

    /**
     * 保存或更新Cookie到Redis（不写数据库）
     * @param platform 平台名称
     * @param cookieValue Cookie值
     * @param remark 备注
     * @return 是否成功
     */
    public boolean saveOrUpdateCookie(String platform, String cookieValue, String remark) {
        return saveOrUpdateCookieWithUserId(platform, null, cookieValue, remark);
    }

    /**
     * 保存或更新Cookie到Redis（指定用户ID）
     * @param platform 平台名称
     * @param userId 用户ID
     * @param cookieValue Cookie值
     * @param remark 备注
     * @return 是否成功
     */
    public boolean saveOrUpdateCookieWithUserId(String platform, Long userId, String cookieValue, String remark) {
        String key = buildKey(platform, userId);
        try {
            CookieEntity existingCookie = getCookieByPlatformAndUserId(platform, userId);

            CookieEntity cookie;
            if (existingCookie != null) {
                cookie = existingCookie;
                cookie.setCookieValue(cookieValue);
                cookie.setRemark(remark);
                cookie.setUpdatedAt(LocalDateTime.now());
            } else {
                cookie = new CookieEntity();
                cookie.setUserId(userId);
                cookie.setPlatform(platform);
                cookie.setCookieValue(cookieValue);
                cookie.setRemark(remark);
                cookie.setCreatedAt(LocalDateTime.now());
                cookie.setUpdatedAt(LocalDateTime.now());
            }

            String json = objectMapper.writeValueAsString(cookie);
            redisTemplate.opsForValue().set(key, json, DEFAULT_TTL);
            log.info("Cookie已保存到Redis: platform={}, userId={}, key={}", platform, userId, key);
            return true;
        } catch (JsonProcessingException e) {
            log.error("Cookie序列化失败: platform={}, userId={}, error={}", platform, userId, e.getMessage());
            return false;
        }
    }

    /**
     * 清空指定平台的所有Cookie值
     * @param platform 平台名称
     * @param remark 备注
     * @return 影响行数
     */
    public boolean clearCookieByPlatform(String platform, String remark) {
        return clearCookieByPlatformAndUserId(platform, null, remark);
    }

    /**
     * 清空指定平台和用户的Cookie值
     * @param platform 平台名称
     * @param userId 用户ID
     * @param remark 备注
     * @return 是否成功
     */
    public boolean clearCookieByPlatformAndUserId(String platform, Long userId, String remark) {
        String key = buildKey(platform, userId);
        try {
            // 先获取现有cookie更新备注
            CookieEntity cookie = getCookieByPlatformAndUserId(platform, userId);
            if (cookie != null) {
                cookie.setCookieValue("");
                cookie.setRemark(remark);
                cookie.setUpdatedAt(LocalDateTime.now());
                String json = objectMapper.writeValueAsString(cookie);
                redisTemplate.opsForValue().set(key, json, DEFAULT_TTL);
            } else {
                // 不存在则创建一个空cookie记录
                CookieEntity newCookie = new CookieEntity();
                newCookie.setUserId(userId);
                newCookie.setPlatform(platform);
                newCookie.setCookieValue("");
                newCookie.setRemark(remark);
                newCookie.setCreatedAt(LocalDateTime.now());
                newCookie.setUpdatedAt(LocalDateTime.now());
                String json = objectMapper.writeValueAsString(newCookie);
                redisTemplate.opsForValue().set(key, json, DEFAULT_TTL);
            }
            return true;
        } catch (Exception e) {
            log.warn("清空Cookie失败: platform={}, userId={}, error={}", platform, userId, e.getMessage());
            return false;
        }
    }

    /**
     * 删除指定平台的Cookie
     * @param platform 平台名称
     * @return 是否成功
     */
    public boolean deleteCookie(String platform) {
        return deleteCookieByUserId(platform, null);
    }

    /**
     * 删除指定平台和用户的Cookie
     * @param platform 平台名称
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean deleteCookieByUserId(String platform, Long userId) {
        String key = buildKey(platform, userId);
        Boolean result = redisTemplate.delete(key);
        return result != null && result;
    }

    /**
     * 构建Redis key
     * @param platform 平台名称
     * @param userId 用户ID
     * @return Redis key
     */
    private String buildKey(String platform, Long userId) {
        if (userId == null) {
            return COOKIE_KEY_PREFIX + platform;
        }
        return COOKIE_KEY_PREFIX + userId + ":" + platform;
    }
}
