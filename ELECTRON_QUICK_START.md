# Get Jobs Electron 快速开始指南

## 🎯 5分钟快速上手

### 步骤 1：安装依赖

```bash
# 在项目根目录执行
npm install
```

### 步骤 2：构建 Java 后端

```bash
# Windows
.\gradlew bootJar

# Mac/Linux
./gradlew bootJar
```

### 步骤 3：构建前端

```bash
cd front
npm install
npm run build:prod
cd ..
```

### 步骤 4：启动 Electron 应用

```bash
# 开发模式（热重载）
npm run electron:dev

# 或直接运行
npx electron .
```

## 📦 打包分发

### Windows

```bash
npm run dist:win
```

输出：`release/Get Jobs Setup X.X.X.exe`

### macOS

```bash
npm run dist:mac
```

输出：`release/Get Jobs X.X.X.dmg`

### Linux

```bash
npm run dist:linux
```

输出：
- `release/Get Jobs X.X.X.AppImage`
- `release/get-jobs_X.X.X_amd64.deb`

## 🔧 配置自动更新

1. 编辑 `electron-package.json`
2. 修改 GitHub 仓库信息：

```json
{
  "publish": [
    {
      "provider": "github",
      "owner": "你的GitHub用户名",
      "repo": "你的仓库名"
    }
  ]
}
```

3. 发布时会自动上传到 GitHub Releases

## 🎨 自定义应用图标

将图标文件放在 `electron/assets/` 目录：

- `icon.ico` - Windows 图标（256x256）
- `icon.icns` - macOS 图标
- `icon.png` - Linux 图标（512x512）

## 📝 修改应用信息

编辑 `electron-package.json`：

```json
{
  "name": "get-jobs-desktop",
  "version": "1.0.0",
  "description": "你的应用描述",
  "author": "你的名字"
}
```

## 🚀 发布流程

1. **更新版本号**
   ```bash
   # 修改 electron-package.json 中的 version
   ```

2. **构建并发布**
   ```bash
   npm run dist
   ```

3. **创建 GitHub Release**
   - 自动创建 Draft Release
   - 手动编辑发布说明
   - 发布

## 💡 开发技巧

### 调试主进程

```bash
# 在 electron/main.js 中添加
console.log('调试信息');

# 查看日志
# Windows: %APPDATA%\get-jobs-desktop\logs\
# Mac: ~/Library/Logs/get-jobs-desktop/
# Linux: ~/.config/get-jobs-desktop/logs/
```

### 调试渲染进程

开发模式会自动打开 DevTools：
```javascript
mainWindow.webContents.openDevTools();
```

### 查看后端日志

```bash
# 状态栏点击"查看日志"
# 或手动查看
tail -f electron/logs/main.log
```

## 🐛 故障排除

### 问题：Electron 窗口空白

**原因**：前端未正确构建

**解决**：
```bash
cd front
npm run build:prod
cd ..
```

### 问题：后端启动失败

**原因**：端口被占用或 Java 环境问题

**解决**：
1. 检查端口：`netstat -ano | findstr :8080`
2. 修改端口：编辑 `electron/main.js` 中的 `port` 参数
3. 检查 Java：`java -version`

### 问题：打包后应用无法运行

**原因**：资源文件路径错误

**解决**：
检查 `electron-package.json` 中的 `extraResources` 配置

## 📚 更多资源

- [Electron 官方文档](https://www.electronjs.org/docs)
- [electron-builder 文档](https://www.electron.build/)
- [electron-updater 文档](https://github.com/electron-userland/electron-builder/tree/master/packages/electron-updater)
- [完整 README](./ELECTRON_README.md)

---

祝你使用愉快！🎉
