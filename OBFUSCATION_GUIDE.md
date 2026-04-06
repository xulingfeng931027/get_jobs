# 代码混淆构建指南

## 概述

本项目已集成前后端代码混淆机制,用于保护源码安全:

- **后端 (Java)**: 使用 ProGuard 进行字节码混淆
- **前端 (JavaScript)**: 使用 javascript-obfuscator 进行代码混淆

## 快速开始

### Windows 用户

```bash
# 构建前后端（含混淆）
.\build-obfuscated.bat

# 仅构建后端
.\build-obfuscated.bat backend

# 仅构建前端
.\build-obfuscated.bat front
```

### Linux/Mac 用户

```bash
# 添加执行权限
chmod +x build-obfuscated.sh

# 构建前后端（含混淆）
./build-obfuscated.sh

# 仅构建后端
./build-obfuscated.sh backend

# 仅构建前端
./build-obfuscated.sh front
```

## 手动构建

### 后端混淆构建

```bash
cd backend

# 清理、编译、打包并混淆
mvn clean package -DskipTests

# 输出文件:
# - target/get_jobs-0.0.1-SNAPSHOT.jar (原始版本)
# - target/get_jobs-0.0.1-SNAPSHOT-obfuscated.jar (混淆版本)
```

### 前端混淆构建

```bash
cd front

# 构建并混淆
pnpm run build:obfuscate

# 或者: 构建生产版本并混淆
pnpm run build:prod:obfuscate

# 输出目录: out/ (已混淆的静态文件)
```

## 可用的 npm 脚本

在 `front` 目录下:

| 命令 | 说明 |
|------|------|
| `pnpm run build` | 普通构建（不混淆） |
| `pnpm run build:prod` | 生产构建 + 复制 dist（不混淆） |
| `pnpm run build:obfuscate` | 构建 + 混淆 |
| `pnpm run build:prod:obfuscate` | 生产构建 + 复制 dist + 混淆 |
| `pnpm run obfuscate` | 仅对现有 out/ 目录进行混淆 |

## 混淆配置

### 后端 ProGuard 配置

配置文件: `backend/proguard.conf`

**保留的规则:**
- ✅ Spring Boot 启动类和注解
- ✅ Controller 方法和路由
- ✅ MyBatis-Plus 实体类和 Mapper
- ✅ JSON 序列化 getter/setter
- ✅ Playwright 相关类
- ✅ 数据库驱动

**混淆的内容:**
- 🔒 业务逻辑类名和方法名
- 🔒 变量名
- 🔒 字符串常量（部分）
- 🔒 控制流（扁平化）

### 前端混淆配置

配置文件: `front/scripts/obfuscate.js`

**混淆选项:**
- 🔒 控制流扁平化 (75%)
- 🔒 死代码注入 (40%)
- 🔒 字符串数组编码 (Base64)
- 🔒 十六进制标识符
- 🔒 对象键转换

**保留的标识符:**
- ✅ Next.js 框架变量 (__NEXT_DATA__, props, query 等)
- ✅ 全局 API 对象

## 混淆效果

### 后端混淆示例

**混淆前:**
```java
public class UserServiceImpl implements UserService {
    public UserDTO getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}
```

**混淆后:**
```java
public class a implements b {
    public c d(Long e) {
        return f.g(e);
    }
}
```

### 前端混淆示例

**混淆前:**
```javascript
function calculateSalary(base, bonus) {
    return base + bonus;
}
```

**混淆后:**
```javascript
var _0x1a2b=['\x63\x61\x6c\x63\x75\x6c\x61\x74\x65\x53\x61\x6c\x61\x72\x79',...];
function _0x3c4d(_0x5e6f,_0x7g8h){return _0x5e6f+_0x7g8h;}
```

## 性能影响

### 构建时间

- **后端**: 增加约 30-60 秒（ProGuard 混淆阶段）
- **前端**: 增加约 10-30 秒（取决于项目大小）

### 运行性能

- **后端**: 无影响（JVM 会优化字节码）
- **前端**: 轻微影响（控制流扁平化可能增加 5-10% 执行时间）

### 文件大小

- **后端**: 减少约 10-20%（ProGuard 会移除未使用代码）
- **前端**: 增加约 20-40%（死代码注入和字符串编码）

## 调试混淆代码

### 后端

ProGuard 会生成映射文件: `target/proguard_map.txt`

可以使用该文件还原堆栈跟踪:

```bash
java -jar proguard/retrace.jar proguard_map.txt stacktrace.txt
```

### 前端

混淆时已禁用 sourceMap（生产安全考虑）。

如需调试,临时修改 `front/scripts/obfuscate.js`:

```javascript
const obfuscatorOptions = {
  // ...
  sourceMap: true,  // 改为 true
  sourceMapMode: 'separate',
};
```

## 注意事项

⚠️ **重要提示:**

1. **测试充分**: 混淆后务必进行全面测试,确保功能正常
2. **反射调用**: 所有使用反射的代码必须在 `proguard.conf` 中保留
3. **第三方库**: 部分第三方库可能不兼容混淆,已配置跳过
4. **性能测试**: 混淆后进行性能测试,确保满足要求
5. **版本控制**: 不要将混淆后的文件提交到 Git

## 故障排除

### 后端构建失败

**问题**: ProGuard 报错找不到类

**解决**: 在 `proguard.conf` 中添加保留规则:
```
-keep class com.example.** { *; }
```

**问题**: 运行时 ClassNotFoundException

**解决**: 检查是否过度混淆,添加必要的保留规则

### 前端构建失败

**问题**: 混淆后页面白屏

**解决**: 检查 `reservedNames` 配置,确保 Next.js 关键变量未被混淆

**问题**: 混淆时间过长

**解决**: 降低混淆强度:
```javascript
controlFlowFlatteningThreshold: 0.5,  // 从 0.75 降低
deadCodeInjectionThreshold: 0.2,      // 从 0.4 降低
```

## 进阶配置

### 提高混淆强度

修改 `front/scripts/obfuscate.js`:

```javascript
const obfuscatorOptions = {
  controlFlowFlattening: true,
  controlFlowFlatteningThreshold: 1.0,  // 最高
  deadCodeInjection: true,
  deadCodeInjectionThreshold: 0.6,      // 增加
  debugProtection: true,                // 启用调试保护
  stringArrayThreshold: 1.0,            // 全部字符串混淆
};
```

### 自定义保留规则

编辑 `backend/proguard.conf`,添加需要保留的类:

```
# 保留特定包
-keep class com.getjobs.specific.package.** { *; }

# 保留特定类
-keep class com.getjobs.ImportantClass { *; }

# 保留特定方法
-keepclassmembers class * {
    public void importantMethod(...);
}
```

## 相关文件

- `backend/pom.xml` - Maven 构建配置（含 ProGuard 插件）
- `backend/proguard.conf` - ProGuard 混淆配置
- `front/package.json` - npm 脚本配置
- `front/scripts/obfuscate.js` - 前端混淆脚本
- `build-obfuscated.bat` - Windows 构建脚本
- `build-obfuscated.sh` - Unix/Linux/Mac 构建脚本

## 技术支持

如遇问题,请检查:
1. 混淆配置是否正确
2. 是否有遗漏的保留规则
3. 构建日志中的错误信息
