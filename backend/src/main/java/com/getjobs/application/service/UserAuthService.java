package com.getjobs.application.service;

import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.entity.UserEntity;
import com.getjobs.application.mapper.UserBalanceMapper;
import com.getjobs.application.mapper.UserMapper;
import com.getjobs.worker.utils.MachineIdProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户认证服务
 */
@Slf4j
@Service
public class UserAuthService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserBalanceMapper userBalanceMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DeviceService deviceService;
    @Value("${user.jwt.secret:GetJobs-User-Secret-Key-2026!@#$}")
    private String jwtSecret;
    @Value("${user.jwt.expiration:604800000}")
    private long jwtExpiration;
    private static final String SESSION_PREFIX = "user_session:";
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param email 邮箱
     * @param phone 手机号
     * @param password 密码
     * @return 注册结果
     */
    @Transactional
    public Map<String, Object> register(String username, String email, String phone, String password) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        UserEntity existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 检查邮箱是否已存在
        if (email != null && !email.isEmpty()) {
            existingUser = userMapper.selectByEmail(email);
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "邮箱已被注册");
                return result;
            }
        }

        // 检查手机号是否已存在
        if (phone != null && !phone.isEmpty()) {
            existingUser = userMapper.selectByPhone(phone);
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "手机号已被注册");
                return result;
            }
        }

        // 创建用户
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 创建用户余额记录
        UserBalanceEntity balance = new UserBalanceEntity();
        balance.setUserId(user.getId());
        balance.setApplicationCount(0);
        balance.setAiMatchCount(0);
        balance.setAiGreetCount(0);
        balance.setReportCount(0);
        balance.setTotalRecharge(0);
        balance.setTotalConsumption(0);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());
        userBalanceMapper.insert(balance);

        log.info("[用户注册] 新用户注册成功：{}", username);

        result.put("success", true);
        result.put("message", "注册成功");
        result.put("userId", user.getId());
        result.put("username", user.getUsername());

        return result;
    }

    /**
     * 用户登录
     *
     * @param username 用户名/邮箱/手机号
     * @param password 密码
     * @return 登录结果
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        // 查询用户（支持用户名、邮箱、手机号登录）
        UserEntity user = userMapper.selectByUsername(username);
        if (user == null) {
            user = userMapper.selectByEmail(username);
        }
        if (user == null) {
            user = userMapper.selectByPhone(username);
        }

        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "账户已被禁用，请联系管理员");
            return result;
        }

        // ============== 设备数量限制检查（最多2台同时登录）==============
        String machineId = MachineIdProvider.getMachineId();
        boolean isCurrentDeviceBound = deviceService.isDeviceBound(user.getId(), machineId);
        int currentDeviceCount = deviceService.getDeviceCount(user.getId());

        // 如果当前设备未绑定，且已达设备数量上限，则拒绝登录
        if (!isCurrentDeviceBound && currentDeviceCount >= 2) {
            log.warn("[用户登录] 用户{}尝试从新设备登录被拒绝，当前设备数: {}/2", username, currentDeviceCount);
            result.put("success", false);
            result.put("message", "登录设备数量已达上限（2台），请先解绑旧设备后再试");
            result.put("deviceLimitReached", true);
            result.put("currentDeviceCount", currentDeviceCount);
            result.put("maxDevices", 2);
            return result;
        }

        // 如果是新设备，自动绑定
        if (!isCurrentDeviceBound) {
            Map<String, Object> bindResult = deviceService.bindDevice(user.getId(), machineId, getDeviceName());
            if (!(Boolean) bindResult.get("success")) {
                log.warn("[用户登录] 用户{}设备绑定失败: {}", username, bindResult.get("message"));
                // 设备绑定失败不阻止登录，但记录警告
            } else {
                log.info("[用户登录] 用户{}绑定新设备，当前设备数: {}/2", username, bindResult.get("deviceCount"));
            }
        } else {
            // 已绑定设备，更新最后登录时间
            log.debug("[用户登录] 用户{}使用已绑定设备登录: {}", username, machineId);
        }
        // ============== 设备数量限制检查 end ==============

        // 生成 JWT Token
        String token = generateToken(user.getId(), user.getUsername());

        // 生成 Redis Session 并存储登录态
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = SESSION_PREFIX + sessionId;
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("userId", String.valueOf(user.getId()));
        sessionData.put("username", user.getUsername());
        sessionData.put("machineId", machineId);
        sessionData.put("loginTime", LocalDateTime.now().toString());
        redisTemplate.opsForHash().putAll(sessionKey, sessionData);
        redisTemplate.expire(sessionKey, SESSION_TTL);

        log.info("[用户登录] 用户{}登录成功，设备: {}", username, machineId);

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("sessionId", sessionId);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("machineId", machineId);

        return result;
    }

    /**
     * 生成 JWT Token
     */
    private String generateToken(Long userId, String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuer("get-jobs-user")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * 验证 JWT Token
     *
     * @param token JWT Token
     * @return 用户信息
     */
    public Map<String, Object> validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("userId", userId);
            result.put("username", username);

            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "Token 无效或已过期");
            return result;
        }
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public Map<String, Object> getUserProfile(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("status", user.getStatus());
        result.put("createdAt", user.getCreatedAt());

        return result;
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param email 邮箱
     * @param phone 手机号
     * @return 更新结果
     */
    @Transactional
    public Map<String, Object> updateUserProfile(Long userId, String email, String phone) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "更新成功");

        return result;
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Transactional
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "旧密码错误");
            return result;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "密码修改成功");

        return result;
    }

    /**
     * 根据sessionId获取用户ID
     * @param sessionId 会话ID
     * @return userId，如果session无效返回null
     */
    public Long getUserIdBySession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        String sessionKey = SESSION_PREFIX + sessionId;
        Object userIdObj = redisTemplate.opsForHash().get(sessionKey, "userId");
        if (userIdObj == null) {
            return null;
        }
        try {
            return Long.parseLong(userIdObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 根据sessionId删除会话（登出）
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    public boolean invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        String sessionKey = SESSION_PREFIX + sessionId;
        Boolean result = redisTemplate.delete(sessionKey);
        return result != null && result;
    }

    /**
     * 获取当前设备的友好名称
     */
    private String getDeviceName() {
        String os = System.getProperty("os.name", "unknown");
        String computerName = System.getProperty("computerName",
                System.getProperty("host.name", "unknown"));
        return os + " - " + computerName;
    }
}
