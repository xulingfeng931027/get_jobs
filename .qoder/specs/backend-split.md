# 后端拆分方案：投递逻辑迁移到用户端

## Context

当前项目采用单体 Spring Boot 后端架构，所有投递逻辑在云端执行，导致：
1. 云端服务器负载高（Playwright 浏览器自动化消耗资源大）
2. 用户必须保持与云端的稳定连接
3. 无法支持离线投递场景
4. 服务器成本高

**目标**：将投递逻辑和上报逻辑移到 Electron 桌面客户端，云端仅保留控制和管理的核心功能。

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                     云端 Spring Boot                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ 用户认证   │  │ 计费控制    │  │ 配置管理    │     │
│  │ 保留       │  │ 保留        │  │ 保留        │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 新增：投递上报 API (/api/delivery/report)        │   │
│  │ 新增：配置同步 API (/api/config/sync)            │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                           │ HTTP API
                           ▼
┌─────────────────────────────────────────────────────────┐
│              Electron 桌面客户端 (新增)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Playwright  │  │ 投递执行器   │  │ 云端API     │     │
│  │ 浏览器引擎  │  │ 本地执行    │  │ 上报/同步   │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
```

---

## 功能划分

### 保留在云端 (Spring Boot)

| 模块 | 说明 | 关键文件 |
|------|------|----------|
| 用户认证 | 注册、登录、JWT签发 | `UserAuthController.java` |
| 计费控制 | 余额检查、扣费、订阅管理 | `BillingService.java` |
| 配置管理 | 配置CRUD、选项字典 | `*ConfigController.java` |
| 数据统计 | 投递分析、图表统计 | `*AnalyticsController.java` |
| **投递上报接收** | 新增：接收客户端投递结果 | `DeliveryReportController.java` |
| **配置同步** | 新增：下发配置给客户端 | `ConfigSyncController.java` |

### 迁移到客户端 (Electron)

| 模块 | 说明 | 迁移后位置 |
|------|------|-----------|
| PlaywrightManager | 浏览器生命周期管理 | `electron-client/src/core/browser/BrowserManager.ts` |
| Boss投递器 | Boss平台投递执行 | `electron-client/src/core/delivery/BossDelivery.ts` |
| Liepin投递器 | 猎聘平台投递执行 | `electron-client/src/core/delivery/LiepinDelivery.ts` |
| Job51投递器 | 51job平台投递执行 | `electron-client/src/core/delivery/Job51Delivery.ts` |
| ZhiLian投递器 | 智联平台投递执行 | `electron-client/src/core/delivery/ZhilianDelivery.ts` |
| BossJobService | 投递任务编排 | `electron-client/src/core/delivery/DeliveryManager.ts` |
| CookieService | Cookie管理 | `electron-client/src/core/browser/CookieManager.ts` |

---

## API 设计

### 1. 投递上报 API (新增)

```
POST /api/delivery/report
Content-Type: application/json
Authorization: Bearer <jwt_token>

Request:
{
  "platform": "boss",
  "deliveredJobs": [
    {
      "encryptId": "xxx",
      "encryptUserId": "yyy",
      "jobName": "Java开发工程师",
      "companyName": "字节跳动",
      "salary": "25-40K",
      "location": "北京",
      "deliveredAt": "2026-04-06T10:30:00Z"
    }
  ],
  "filteredJobs": [...],
  "failedJobs": [...],
  "totalCount": 10,
  "deviceId": "uuid-xxx",
  "clientVersion": "1.0.0"
}

Response:
{
  "success": true,
  "reportId": "rpt_123456",
  "serverReceivedCount": 10,
  "deductionApplied": 1
}
```

### 2. 配置同步 API (新增)

```
GET /api/config/sync
Authorization: Bearer <jwt_token>

Response:
{
  "success": true,
  "timestamp": "2026-04-06T10:00:00Z",
  "config": {
    "boss": { ... },
    "liepin": { ... },
    "job51": { ... },
    "zhilian": { ... }
  },
  "options": {
    "boss": { ... },
    ...
  },
  "blacklist": { ... },
  "aiConfig": { ... }
}
```

### 3. 计费预检查 API (新增)

```
POST /api/billing/pre-check
Authorization: Bearer <jwt_token>

Request:
{
  "platform": "boss",
  "expectedDeliveryCount": 10
}

Response:
{
  "allowed": true,
  "remainingCount": 50,
  "hasSubscription": true,
  "subscriptionEndDate": "2026-05-01"
}
```

---

## Implementation Steps

### Phase 1: 云端改造 (1-2周)

#### 1.1 新增投递上报 Controller
- 文件：`backend/src/main/java/com/getjobs/application/controller/DeliveryReportController.java`
- 端点：`POST /api/delivery/report`
- 功能：接收客户端投递结果，批量写入数据库，执行扣费

#### 1.2 新增配置同步 Controller
- 文件：`backend/src/main/java/com/getjobs/application/controller/ConfigSyncController.java`
- 端点：`GET /api/config/sync`
- 功能：合并所有平台配置为统一响应

#### 1.3 新增计费预检查
- 文件：`backend/src/main/java/com/getjobs/application/controller/BillingController.java`
- 端点：`POST /api/billing/pre-check`
- 功能：投递前验证余额/订阅

#### 1.4 数据库调整
- `boss_data` 表增加字段：`report_id`, `client_id`
- 新建 `delivery_report` 表记录上报历史

#### 1.5 安全配置
- 修改 `SecurityConfig.java`，开放新API端点

### Phase 2: Electron客户端基础 (2-3周)

#### 2.1 项目初始化
```
electron-client/
├── src/
│   ├── main/                      # Electron主进程
│   │   ├── index.ts              # 入口
│   │   ├── ipc-handlers.ts       # IPC处理
│   │   └── preload.ts            # 预加载脚本
│   ├── core/                     # 核心业务
│   │   ├── browser/              # 浏览器管理
│   │   ├── delivery/             # 投递执行器
│   │   ├── config/               # 配置加载
│   │   └── api/                  # 云端API
│   └── shared/                   # 类型定义
├── package.json
└── tsconfig.json
```

#### 2.2 关键依赖
```json
{
  "dependencies": {
    "electron": "^28.0.0",
    "playwright": "^1.51.0",
    "electron-store": "^8.1.0",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    "electron-builder": "^24.9.0",
    "typescript": "^5.3.0"
  }
}
```

#### 2.3 浏览器管理器
- 文件：`electron-client/src/core/browser/BrowserManager.ts`
- 功能：Playwright浏览器生命周期管理

### Phase 3: 投递执行器迁移 (3-4周)

#### 3.1 类型定义迁移
| Java类 | TypeScript文件 |
|--------|---------------|
| `BossConfig.java` | `BossConfig.ts` |
| `Locators.java` | `BossLocators.ts` |
| `JobProgressMessage.java` | `ProgressMessage.ts` |
| `Boss.java` | `BossDelivery.ts` |
| `Liepin.java` | `LiepinDelivery.ts` |
| `Job51.java` | `Job51Delivery.ts` |
| `ZhiLian.java` | `ZhilianDelivery.ts` |

#### 3.2 投递编排器
- 文件：`electron-client/src/core/delivery/DeliveryManager.ts`
- 功能：任务启动/停止、进度回调

#### 3.3 进度事件系统
- 文件：`electron-client/src/core/delivery/ProgressEmitter.ts`
- 功能：IPC事件发送到渲染进程

### Phase 4: 云端API集成 (2周)

#### 4.1 API客户端
- 文件：`electron-client/src/core/api/CloudAPI.ts`
- 功能：JWT管理、自动刷新、错误处理

#### 4.2 配置同步
- 文件：`electron-client/src/core/config/ConfigLoader.ts`
- 功能：云端配置拉取、本地缓存

#### 4.3 投递上报
- 文件：`electron-client/src/core/api/ReportAPI.ts`
- 功能：投递结果上报、重试机制

### Phase 5: 前端集成 (2周)

#### 5.1 IPC封装
- 扩展 `front/types/electron.d.ts`
- 新增投递相关API

#### 5.2 进度显示迁移
- 从SSE切换到IPC接收进度

#### 5.3 配置页面优化
- 配置保存同步到云端

### Phase 6: 测试与优化 (2周)

- 投递流程测试
- 计费扣费测试
- 异常处理测试

---

## Critical Files

### 云端需修改
- `backend/src/main/java/com/getjobs/application/controller/DeliveryReportController.java` (新增)
- `backend/src/main/java/com/getjobs/application/controller/ConfigSyncController.java` (新增)
- `backend/src/main/java/com/getjobs/application/controller/BillingController.java` (新增)
- `backend/src/main/java/com/getjobs/application/config/SecurityConfig.java` (修改)

### 客户端新增
- `electron-client/src/main/index.ts`
- `electron-client/src/core/delivery/DeliveryManager.ts`
- `electron-client/src/core/delivery/BossDelivery.ts`
- `electron-client/src/core/api/CloudAPI.ts`

### 前端需修改
- `front/types/electron.d.ts`
- `front/app/boss/page.tsx` (进度接收方式)

---

## Verification

### 功能验证清单

1. **投递执行**
   - [ ] 客户端能启动Playwright浏览器
   - [ ] Boss平台投递正常执行
   - [ ] 投递进度实时显示在前端

2. **云端上报**
   - [ ] 投递完成后数据正确写入云端数据库
   - [ ] 数据能在前端分析页面查看

3. **计费控制**
   - [ ] 余额不足时阻止投递
   - [ ] 投递完成后正确扣费

4. **配置同步**
   - [ ] 客户端能获取云端配置
   - [ ] 配置变更能同步到客户端

### 测试命令

```bash
# 1. 启动云端后端
cd backend && mvn spring-boot:run

# 2. 启动Electron客户端
cd electron-client && npm run dev

# 3. 测试投递流程
# - 前端登录
# - 配置关键词
# - 点击"开始投递"
# - 观察进度
# - 检查云端数据库
```

---

## Timeline Summary

| 阶段 | 内容 | 预计时间 |
|------|------|----------|
| Phase 1 | 云端改造 | 1-2周 |
| Phase 2 | 客户端基础架构 | 2-3周 |
| Phase 3 | 投递执行器迁移 | 3-4周 |
| Phase 4 | 云端API集成 | 2周 |
| Phase 5 | 前端集成 | 2周 |
| Phase 6 | 测试优化 | 2周 |
| **总计** | - | **12-15周** |
