# Get Jobs 部署指南

## 目录

- [环境要求](#环境要求)
- [目录结构](#目录结构)
- [安装步骤](#安装步骤)
- [配置说明](#配置说明)
- [安全配置](#安全配置)
- [故障排查](#故障排查)

---

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21+ | JDK 21 或更高版本 |
| Node.js | 18+ | 用于安装 Playwright 浏览器 |
| npm | 9+ | Node.js 自带 |
| Windows | 10+ | 或 Linux/macOS |

---

## 目录结构

```
getjobs/
├── get-jobs.jar              # 主程序 JAR
├── license.key               # 许可证文件（联系管理员获取）
├── config/
│   └── application-secure.yml # 密钥配置文件
├── .env                      # 环境变量文件
├── logs/                     # 日志目录
├── data/                     # 数据目录
├── browsers/                 # 浏览器目录（可选）
│   └── ...
└── start.bat                 # 启动脚本
```

---

## 安装步骤

### 1. 安装 Java

下载并安装 Eclipse Temurin (Adoptium) JDK 21:
- https://adoptium.net/

验证安装:
```batch
java -version
```

### 2. 安装 Playwright 浏览器

**方式一: 使用安装脚本（推荐）**

```batch
cd scripts\windows
install-playwright.bat
```

**方式二: 手动安装**

```batch
# 安装 Chromium（主要浏览器）
npx playwright install chromium

# 安装备用浏览器（可选）
npx playwright install firefox
npx playwright install webkit
```

**方式三: 使用国内镜像（推荐国内用户）**

```batch
# 设置镜像
set PLAYWRIGHT_DOWNLOAD_HOST=https://playwright.cnpmjs.org

# 安装浏览器
npx playwright install chromium
```

浏览器默认安装到:
- Windows: `%USERPROFILE%\AppData\Local\ms-playwright`
- macOS: `~/Library/Caches/ms-playwright`
- Linux: `~/.cache/ms-playwright`

### 3. 获取许可证

联系管理员获取 `license.key` 文件。

### 4. 配置密钥

复制环境变量模板并配置:

```batch
copy scripts\.env.example .env
```

编辑 `.env` 文件，填入实际的密钥值。

### 5. 启动应用

```batch
start.bat
```

---

## 配置说明

### .env 环境变量

| 变量 | 说明 | 示例 |
|------|------|------|
| `API_KEY` | API 请求签名密钥 | `your_api_key_here` |
| `API_ENDPOINT` | API 服务器地址 | `https://api.getjobs.com` |
| `DB_PASSWORD` | 数据库密码 | `your_db_password` |
| `ADMIN_JWT_SECRET` | 管理员 JWT 密钥（32+字符） | `your_admin_secret_here` |
| `USER_JWT_SECRET` | 用户 JWT 密钥（32+字符） | `your_user_secret_here` |
| `JASYPT_ENCRYPTOR_PASSWORD` | 配置加密密钥 | `your_jasypt_password` |
| `LICENSE_FILE` | 许可证文件路径 | `license.key` |
| `DEV_MODE` | 开发模式（跳过安全检查） | `false` |

### config/application-secure.yml

此文件包含敏感配置，不应提交到版本控制。

```yaml
secure:
  api:
    key: ${API_KEY}
    endpoint: ${API_ENDPOINT}

admin:
  jwt:
    secret: ${ADMIN_JWT_SECRET}

jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
```

---

## 安全配置

### 1. 许可证绑定

每个 `license.key` 文件绑定到特定机器的硬件指纹（CPU + 主板 + MAC），不可复制到其他机器使用。

### 2. JAR 签名验证

应用启动时自动验证 JAR 签名，确保代码未被篡改。

### 3. 请求签名

所有外发 HTTP 请求都带有签名头，防止抓包和重放攻击。

### 4. 日志脱敏

日志中自动脱敏以下敏感信息:
- 手机号: `138****5678`
- 身份证号: `********1234`
- 密码: `****`
- API Key: `****`

### 5. 反调试检测

检测以下环境:
- 调试器附加（`jdwp`）
- IDE 环境（IDEA/Eclipse/VS Code）
- 虚拟机/Docker 环境

---

## 故障排查

### 1. Chromium 未安装

```
错误: Chromium 可执行文件不存在
解决: 运行 install-playwright.bat 安装浏览器
```

### 2. 许可证无效

```
错误: 许可证文件不存在或格式错误
解决: 联系管理员获取有效的 license.key
```

```
错误: 机器指纹不匹配
解决: 许可证文件只能在本机使用，不可复制
```

### 3. 端口被占用

```
错误: 端口 18888 已被占用
解决: 修改 config/application.yaml 中的 server.port
```

### 4. 浏览器启动失败

检查浏览器路径和权限:

```batch
# 查看浏览器安装位置
dir "%USERPROFILE%\AppData\Local\ms-playwright"

# 检查 chromium.exe 是否存在
dir "%USERPROFILE%\AppData\Local\ms-playwright\chromium-1161\chrome-win"
```

### 5. 网络请求失败

检查 API 配置和防火墙:

```yaml
# config/application-secure.yml
secure:
  api:
    endpoint: https://api.getjobs.com  # 确认 API 地址正确
```

---

## 更新日志

### 版本 0.0.2+

新增安全特性:
- 许可证机器绑定
- JAR 签名验证
- 请求签名
- 日志脱敏
- 反调试检测
