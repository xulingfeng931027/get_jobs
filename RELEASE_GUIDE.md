# 发布新版本指南

## 📋 发布前检查清单

### 1. 更新版本号

编辑 `package.json`，更新版本号：

```json
{
  "name": "get-jobs-desktop",
  "version": "1.0.0",  // 修改这里
  ...
}
```

版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)：
- **主版本号 (Major)**：不兼容的 API 修改
- **次版本号 (Minor)**：向下兼容的功能性新增
- **修订号 (Patch)**：向下兼容的问题修正

### 2. 更新更新日志

在 `ELECTRON_README.md` 或创建 `CHANGELOG.md`，记录本次版本的变更。

### 3. 测试构建

```bash
# 本地测试构建
npm run electron:build

# 或测试特定平台
npm run dist:win     # Windows
npm run dist:mac     # macOS
npm run dist:linux   # Linux
```

### 4. 验证安装包

- [ ] 安装程序能正常运行
- [ ] 应用能正常启动
- [ ] 后端能正常启动
- [ ] 基本功能正常
- [ ] 自动更新配置正确

## 🚀 发布方式

### 方式一：使用 Git Tag（推荐）

```bash
# 1. 提交所有更改
git add .
git commit -m "🔖 发布 v1.0.0"

# 2. 创建标签
git tag -a v1.0.0 -m "发布版本 1.0.0"

# 3. 推送标签（触发 GitHub Actions 自动发布）
git push origin main
git push origin v1.0.0
```

GitHub Actions 会自动：
- 在 Windows、macOS、Linux 上构建
- 创建 GitHub Release
- 上传所有平台的安装包

### 方式二：手动触发工作流

1. 访问：https://github.com/loks666/get_jobs/actions/workflows/electron-release.yml
2. 点击 "Run workflow"
3. 输入版本号（如：1.0.0）
4. 点击 "Run workflow"

### 方式三：本地手动发布

```bash
# 1. 构建所有平台
npm run dist

# 2. 手动创建 GitHub Release
# 访问：https://github.com/loks666/get_jobs/releases/new

# 3. 上传 release/ 目录中的所有文件
# - Get Jobs Setup 1.0.0.exe (Windows)
# - Get Jobs 1.0.0.dmg (macOS)
# - Get Jobs 1.0.0.AppImage (Linux)
# - get-jobs_1.0.0_amd64.deb (Linux)
```

## 📝 发布说明模板

```markdown
## 🎉 Get Jobs Desktop v1.0.0

### ✨ 新功能
- 功能1描述
- 功能2描述

### 🐛 修复
- 修复问题1
- 修复问题2

### 📦 安装包

#### Windows
- **Get Jobs Setup 1.0.0.exe** - NSIS 安装程序（推荐）

#### macOS
- **Get Jobs 1.0.0.dmg** - DMG 安装包

#### Linux
- **Get Jobs 1.0.0.AppImage** - AppImage（通用）
- **get-jobs_1.0.0_amd64.deb** - Debian/Ubuntu

### 📋 系统要求
- Windows 10 或更高版本
- macOS 10.13 或更高版本
- Ubuntu 18.04+ / Debian 10+ 或同类发行版
- JDK 21（如系统未自带）

### 🔗 相关链接
- [完整更新日志](CHANGELOG.md)
- [使用文档](ELECTRON_README.md)
- [问题反馈](https://github.com/loks666/get_jobs/issues)
```

## 🔍 发布后验证

### 1. 检查 GitHub Release

访问：https://github.com/loks666/get_jobs/releases

确认：
- [ ] Release 已创建
- [ ] 所有平台的安装包已上传
- [ ] 发布说明完整
- [ ] 版本号正确

### 2. 测试自动更新

```bash
# 1. 安装当前版本
# 2. 修改 package.json 版本号为更高的版本号
# 3. 发布新版本
# 4. 在已安装的应用中检查更新
```

### 3. 下载测试

从 GitHub Releases 页面下载各平台安装包，测试安装和运行。

## 🐛 常见问题

### Q: GitHub Actions 构建失败

**解决方案：**
1. 检查 Actions 日志
2. 确认依赖配置正确
3. 检查图标文件是否存在
4. 验证 package.json 格式

### Q: 自动更新不工作

**解决方案：**
1. 检查 `package.json` 中的 `publish` 配置
2. 确认 GitHub Token 权限正确
3. 查看应用日志（electron-log）
4. 验证 Release 是正式发布（非 Draft）

### Q: 某个平台构建失败

**解决方案：**
- **Windows**：检查 NSIS 配置和图标
- **macOS**：检查代码签名配置
- **Linux**：检查依赖和权限

## 📊 版本管理策略

### 开发阶段
- 使用 `0.x.x` 版本号
- 频繁发布小更新

### 正式发布
- 从 `1.0.0` 开始
- 遵循语义化版本

### 更新频率
- **Patch**：每周或按需
- **Minor**：每月
- **Major**：每季度或重要更新时

## 🎯 最佳实践

1. **测试先行**：发布前充分测试
2. **更新日志**：详细记录每次变更
3. **语义化版本**：严格遵循 semver
4. **自动化**：使用 CI/CD 自动构建
5. **回滚准备**：保留旧版本安装包
6. **用户通知**：在 Release 中说明重要变更

## 📞 需要帮助？

- 查看 [Electron Builder 文档](https://www.electron.build/)
- 提交 [Issue](https://github.com/loks666/get_jobs/issues)
- 加入 QQ 群讨论

---

**祝发布顺利！🎉**
