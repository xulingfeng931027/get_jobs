# 计费拦截集成指南

## 概述

本文档说明如何在投递控制器中集成计费检查逻辑。

## 需要修改的控制器

1. `BossController.java` - Boss直聘
2. `JobController.java` - 51job
3. `LiepinController.java` - 猎聘
4. `ZhilianController.java` - 智联招聘

## 修改步骤

### 1. 在控制器中添加 BillingService 依赖

```java
// 在类的顶部添加依赖注入
private final BillingService billingService;

// 如果使用 @RequiredArgsConstructor，在构造函数中添加
// 或者使用 @Autowired
@Autowired
private BillingService billingService;
```

### 2. 在 start/execute 方法中添加计费检查

在每个平台的 `start` 或 `execute` 方法开头添加以下检查：

```java
@PostMapping("/start")
public ResponseEntity<Map<String, Object>> startBoss(@RequestAttribute(value = "userId", required = false) Long userId) {
    Map<String, Object> response = new HashMap<>();
    
    // ========== 计费检查开始 ==========
    if (userId == null) {
        response.put("success", false);
        response.put("message", "未登录，请先登录");
        return ResponseEntity.status(401).body(response);
    }
    
    Map<String, Object> billingCheck = billingService.checkBeforeDelivery(userId);
    if (!(Boolean) billingCheck.get("allowed")) {
        response.put("success", false);
        response.put("message", billingCheck.get("reason"));
        response.put("billingInfo", billingCheck);
        return ResponseEntity.badRequest().body(response);
    }
    // ========== 计费检查结束 ==========
    
    // 原有逻辑...
    if (!playwrightManager.isLoggedIn("boss")) {
        response.put("success", false);
        response.put("message", "请先登录Boss直聘");
        return ResponseEntity.badRequest().body(response);
    }
    
    // ... 其他原有逻辑
}
```

### 3. 在投递完成后调用扣费方法

在投递任务完成的回调中或 `@Scheduled` 定期检查中添加：

```java
// 投递完成后扣费
@PostMapping("/execute")
public ResponseEntity<Map<String, Object>> executeBoss(@RequestAttribute(value = "userId", required = false) Long userId) {
    // ... 原有逻辑
    
    // 投递完成后（在 CompletableFuture 的回调中）
    CompletableFuture.runAsync(() -> {
        try {
            // 执行投递
            int deliveredCount = bossJobService.executeDelivery(this::sendBossProgress);
            
            // 扣费
            if (userId != null) {
                billingService.deductAfterDelivery(userId, deliveredCount, "boss");
            }
        } catch (Exception e) {
            log.error("Boss投递任务执行失败", e);
        }
    });
    
    return ResponseEntity.ok(Map.of("status", "started"));
}
```

## 各控制器需要修改的方法

### BossController
- `startBoss()` - 添加计费检查
- `executeBoss()` - 添加计费检查 + 投递后扣费

### JobController (51job)
- `start51jobJob()` - 添加计费检查
- `execute51jobJob()` - 添加计费检查 + 投递后扣费

### LiepinController
- `startLiepin()` - 添加计费检查  
- `executeLiepin()` - 添加计费检查 + 投递后扣费

### ZhilianController
- `startZhilian()` - 添加计费检查
- `executeZhilian()` - 添加计费检查 + 投递后扣费

## 完整示例 (BossController)

```java
@Slf4j
@RestController
@RequestMapping("/api/boss")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BossController {
    private final BossJobService bossJobService;
    private final PlaywrightManager playwrightManager;
    private final CookieService cookieService;
    private final BillingService billingService;  // 新增

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startBoss(@RequestAttribute(value = "userId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        // 计费检查
        if (userId == null) {
            response.put("success", false);
            response.put("message", "未登录，请先登录");
            return ResponseEntity.status(401).body(response);
        }
        
        Map<String, Object> billingCheck = billingService.checkBeforeDelivery(userId);
        if (!(Boolean) billingCheck.get("allowed")) {
            response.put("success", false);
            response.put("message", billingCheck.get("reason"));
            response.put("billingInfo", billingCheck);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 原有逻辑
        try {
            if (!playwrightManager.isLoggedIn("boss")) {
                response.put("success", false);
                response.put("message", "请先登录Boss直聘");
                response.put("status", "not_logged_in");
                return ResponseEntity.badRequest().body(response);
            }
            if (bossJobService.isRunning()) {
                response.put("success", false);
                response.put("message", "Boss任务已在运行中");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 启动投递任务（传递 userId 用于扣费）
            final Long finalUserId = userId;
            CompletableFuture.runAsync(() -> {
                int deliveredCount = bossJobService.executeDelivery(this::sendBossProgress);
                // 投递完成后扣费
                billingService.deductAfterDelivery(finalUserId, deliveredCount, "boss");
            });
            
            response.put("success", true);
            response.put("message", "Boss投递任务已启动");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "启动Boss投递任务失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
```

## 注意事项

1. **userId 获取**：确保 `UserJwtFilter` 已将 `userId` 设置到 request attribute 中
2. **异步扣费**：投递完成后在异步回调中扣费，确保投递成功才扣费
3. **失败不扣费**：如果投递失败，不应扣费
4. **订阅用户**：有有效订阅的用户不扣费，`BillingService.deductAfterDelivery` 已处理此逻辑
5. **日志记录**：每次扣费都会记录日志，便于追踪
