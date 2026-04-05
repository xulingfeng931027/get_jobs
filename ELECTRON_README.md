# Get Jobs Electron 桌面应用

## 📦 简介

Get Jobs Electron 版本是一个跨平台的桌面应用程序，基于 Electron + Spring Boot + Next.js 构建。它将前端界面和后端服务打包在一起，提供一键安装、自动更新的便捷体验。

### ✨ 特性

- 🖥️ **跨平台支持**：Windows、macOS、Linux
- 🔄 **自动更新**：应用启动时自动检查并下载更新
- 📱 **一键安装**：提供安装程序，无需配置环境
- 🚀 **开箱即用**：内置 Java 后端，无需单独安装 JDK（提示安装）
- 🎨 **现代化界面**：基于 Next.js + React + TailwindCSS
- 🔒 **安全可靠**：进程隔离，安全的 IPC 通信

## 🚀 快速开始

### 方式一：下载预编译版本（推荐）

1. 前往 [Releases](https://github.com/loks666/get_jobs/releases) 页面
2. 下载对应操作系统的安装包：
   - **Windows**: `Get Jobs Setup X.X.X.exe`（NSIS 安装程序）
   - **macOS**: `Get Jobs X.X.X.dmg`
   - **Linux**: `Get Jobs X.X.X.AppImage` 或 `.deb` 包
3. 运行安装程序，按提示完成安装
4. 启动应用，享受自动化求职体验！

### 方式二：从源码构建

#### 前置要求

- **Node.js** 18+ 和 npm/pnpm
- **JDK 21** 或以上版本
- **Git**

#### 构建步骤

**Windows:**
```bash
# 1. 克隆项目
git clone https://github.com/loks666/get_jobs.git
cd get_jobs

# 2. 运行构建脚本
.\build-electron.bat
```

**macOS/Linux:**
```bash
# 1. 克隆项目
git clone https://github.com/loks666/get_jobs.git
cd get_jobs

# 2. 赋予执行权限
chmod +x build-electron.sh

# 3. 运行构建脚本
./build-electron.sh
```

构建完成后，安装包将生成在 `release/` 目录。

## 📁 项目结构

```
get_jobs/
├── electron/                  # Electron 主进程代码
│   ├── main.js               # 主进程入口
│   ├── preload.js            # 预加载脚本
│   ├── backend-manager.js    # 后端进程管理器
│   └── assets/               # 应用图标等资源
├── front/                     # Next.js 前端应用
│   ├── app/                  # 页面组件
│   ├── lib/
│   │   ├── electron.ts       # Electron 环境检测
│   │   └── update-manager.ts # 更新管理器
│   └── hooks/
│       └── useBackendStatus.ts # 后端状态 Hook
├── src/main/java/            # Spring Boot 后端
├── electron-package.json     # Electron 依赖配置
├── build-electron.bat        # Windows 构建脚本
└── build-electron.sh         # Mac/Linux 构建脚本
```

## 🔧 开发指南

### 开发模式

```bash
# 1. 安装依赖
npm install
cd front && npm install && cd ..

# 2. 构建 Java 后端
./gradlew bootJar

# 3. 启动开发模式
npm run electron:dev
```

这会同时启动：
- Next.js 开发服务器（http://localhost:3000）
- Electron 窗口（加载开发服务器）
- Spring Boot 后端（自动启动）

### 生产构建

```bash
# 构建所有平台
npm run dist

# 仅构建 Windows
npm run dist:win

# 仅构建 macOS
npm run dist:mac

# 仅构建 Linux
npm run dist:linux
```

## 🔄 自动更新

应用使用 `electron-updater` 实现自动更新功能：

### 工作原理

1. **启动时检查**：应用启动时自动检查 GitHub Releases
2. **定时检查**：每 6 小时自动检查一次更新
3. **后台下载**：发现更新后自动下载
4. **提示安装**：下载完成后弹出提示，用户可选择立即安装或稍后

### 配置更新服务器

在 `electron-package.json` 中配置：

```json
{
  "build": {
    "publish": [
      {
        "provider": "github",
        "owner": "loks666",
        "repo": "get_jobs",
        "releaseType": "release"
      }
    ]
  }
}
```

### 发布更新

1. 更新版本号（`electron-package.json`）
2. 构建并发布：
   ```bash
   npm run dist
   ```
3. 自动创建 GitHub Release 并上传安装包

## 📖 使用说明

### 首次启动

1. **Java 环境检测**：应用会检测系统是否安装 JDK 21+
   - 如果未安装，会提示用户下载并安装
2. **后端启动**：自动启动 Spring Boot 后端服务
3. **界面加载**：显示主界面，可以开始配置和使用

### 状态栏

应用底部提供状态栏，显示：
- 🔵 后端服务状态（运行中/启动中/已停止/错误）
- 🔄 更新下载进度
- 📌 应用版本号
- 🔍 检查更新按钮
- 📝 查看日志按钮

### 后端管理

- **重启后端**：点击状态栏的"重启后端"按钮
- **查看日志**：点击"查看日志"打开日志文件夹
- **状态监控**：实时显示后端运行状态

## 🛠️ 技术栈

### 前端
- **Electron** 28.x - 跨平台桌面框架
- **Next.js** 16.x - React 框架
- **React** 19.x - UI 库
- **TailwindCSS** - 样式框架
- **TypeScript** - 类型安全

### 后端
- **Spring Boot** 3.5.7 - Java 后端框架
- **MyBatis-Plus** - ORM 框架
- **MySQL** - 数据库
- **Playwright** - 浏览器自动化

### 打包与更新
- **electron-builder** - 应用打包
- **electron-updater** - 自动更新
- **electron-log** - 日志管理

## 📝 开发规范

### 文件命名
- Electron 主进程：`camelCase.js`
- 前端组件：`PascalCase.tsx`
- 工具函数：`camelCase.ts`

### 代码风格
- 使用 ESLint + Prettier
- 遵循 TypeScript 严格模式
- 组件使用函数式组件 + Hooks

### Git 提交
- 使用 Emoji + 描述
- 示例：`✨ 新增 Electron 自动更新功能`

## 🐛 常见问题

### 1. Java 环境检测失败

**问题**：提示"未检测到 Java 环境"

**解决方案**：
- 下载并安装 [JDK 21](https://adoptium.net/)
- 确保 `java` 命令在系统 PATH 中
- 重启应用

### 2. 后端启动失败

**问题**：状态栏显示"后端错误"

**解决方案**：
- 点击"查看日志"查看错误信息
- 检查端口 8080 是否被占用
- 尝试点击"重启后端"

### 3. 更新检查失败

**问题**：点击"检查更新"无响应

**解决方案**：
- 检查网络连接
- 确认可以访问 GitHub
- 查看日志文件了解详细错误

### 4. 应用卡顿

**问题**：界面响应缓慢

**解决方案**：
- 检查系统资源占用
- 关闭不必要的浏览器标签页
- 重启应用

## 🤝 参与贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m '✨ Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 开源协议

本项目基于 MIT 协议开源。

## 📮 联系方式

- **GitHub**: [loks666/get_jobs](https://github.com/loks666/get_jobs)
- **QQ 群**: [点击加入](https://qm.qq.com/q/qJwmIrqPU)
- **问题反馈**: [Issues](https://github.com/loks666/get_jobs/issues)

## 🙏 致谢

感谢所有为这个项目贡献的开发者和用户！

---

**祝你求职顺利！🎉**
