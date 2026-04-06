# 数据库配置加密使用说明

## 概述

项目已集成 **Jasypt (Java Simplified Encryption)** 配置加密框架,对敏感配置(如数据库密码)进行加密存储,防止明文泄露。

## 加密效果

### 加密前 (明文)
```yaml
spring:
  datasource:
    username: app_user
    password: 7hxUKgrA7x6D!FZwHLFz  # ❌ 明文密码
```

### 加密后 (密文)
```yaml
spring:
  datasource:
    username: app_user
    password: ENC(tmoqXZMC+8WPanVy6m1ZmBfr1toBiLHMuRxupskKRZg=)  # ✅ 加密密码
```

## 快速使用

### 1. 加密配置值

#### Windows
```bash
# 使用默认密钥
.\encrypt-config.bat "your_password"

# 使用自定义密钥
set JASYPT_ENCRYPTOR_PASSWORD=my-secret-key
.\encrypt-config.bat "your_password"
```

#### Linux/Mac
```bash
# 添加执行权限
chmod +x encrypt-config.sh

# 使用默认密钥
./encrypt-config.sh "your_password"

# 使用自定义密钥
export JASYPT_ENCRYPTOR_PASSWORD=my-secret-key
./encrypt-config.sh "your_password"
```

### 2. 将加密结果复制到配置文件

```yaml
spring:
  datasource:
    password: ENC(加密后的字符串)
```

### 3. 启动应用

#### 方式一:使用默认密钥(开发环境)
```bash
# 直接启动,使用 application.yaml 中的默认密钥
java -jar get_jobs.jar
```

#### 方式二:通过环境变量设置密钥(推荐)
```bash
# Windows
set JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
java -jar get_jobs.jar

# Linux/Mac
export JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
java -jar get_jobs.jar
```

#### 方式三:通过 JVM 参数设置密钥
```bash
java -Djasypt.encryptor.password=your-secret-key -jar get_jobs.jar
```

## 加密密钥管理

### 密钥优先级
1. **环境变量** `JASYPT_ENCRYPTOR_PASSWORD` (最高优先级)
2. **JVM 参数** `-Djasypt.encryptor.password=xxx`
3. **配置文件** `application.yaml` 中的默认值 (仅开发环境)

### 生成强密钥
```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[System.Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

### 生产环境部署

```bash
# 1. 设置环境变量(不要硬编码)
export JASYPT_ENCRYPTOR_PASSWORD="production-secret-key-2026"

# 2. 使用加密后的配置
# application.yaml 中:
# password: ENC(encrypted_value)

# 3. 启动应用
java -jar get_jobs.jar
```

## 加密工具类

### ConfigEncryptor - Java API

在代码中加密/解密:

```java
import com.getjobs.common.util.ConfigEncryptor;

// 加密
String encrypted = ConfigEncryptor.encryptForYaml("my_password", "encryption-key");
// 输出: ENC(xxxxxx)

// 解密
String decrypted = ConfigEncryptor.decrypt("encrypted_string", "encryption-key");
```

### EncryptionTool - 命令行工具

```bash
cd backend

# 编译
mvn compile

# 运行加密工具
mvn exec:java \
    -Dexec.mainClass="com.getjobs.common.util.EncryptionTool" \
    -Dexec.args="your_password" \
    -Djasypt.encryptor.password="your-key"
```

## 可加密的配置项

任何敏感配置都可以使用 `ENC(...)` 格式加密:

```yaml
spring:
  datasource:
    password: ENC(encrypted_db_password)

admin:
  jwt:
    secret: ENC(encrypted_jwt_secret)

# 其他任何配置项
custom:
  api-key: ENC(encrypted_api_key)
  token: ENC(encrypted_token)
```

## 安全建议

### ✅ 推荐做法

1. **生产环境使用自定义密钥**
   ```bash
   export JASYPT_ENCRYPTOR_PASSWORD="$(openssl rand -base64 32)"
   ```

2. **不要将加密密钥提交到 Git**
   ```bash
   # .gitignore
   .env
   application-local.yaml
   ```

3. **使用环境变量管理服务**
   - AWS Parameter Store
   - HashiCorp Vault
   - Kubernetes Secrets
   - Docker Secrets

4. **定期更换加密密钥**
   - 重新加密所有配置
   - 更新部署配置

### ❌ 避免做法

1. **不要使用默认密钥生产环境**
   ```yaml
   # ❌ 危险
   jasypt:
     encryptor:
       password: get-jobs-encryption-key-2026
   ```

2. **不要在代码中硬编码密钥**
   ```java
   // ❌ 危险
   String key = "my-secret-key";
   ```

3. **不要将密钥写在配置文件中**
   ```yaml
   # ❌ 危险
   jasypt:
     encryptor:
       password: super-secret
   ```

## 故障排除

### 问题1: 启动时报解密错误

**错误信息:**
```
org.jasypt.exceptions.EncryptionOperationNotPossibleException
```

**原因:** 加密密钥不正确

**解决:**
```bash
# 检查是否正确设置密钥
echo $JASYPT_ENCRYPTOR_PASSWORD

# 重新加密密码
./encrypt-config.sh "your_password"
```

### 问题2: ENC() 格式错误

**错误格式:**
```yaml
# ❌ 错误 - 缺少 ENC()
password: tmoqXZMC+8WPanVy6m1ZmBfr1toBiLHMuRxupskKRZg=

# ❌ 错误 - 括号不匹配
password: ENC(tmoqXZMC+8WPanVy6m1ZmBfr1toBiLHMuRxupskKRZg

# ✅ 正确
password: ENC(tmoqXZMC+8WPanVy6m1ZmBfr1toBiLHMuRxupskKRZg=)
```

### 问题3: 环境变量未生效

**检查步骤:**
```bash
# Linux/Mac
echo $JASYPT_ENCRYPTOR_PASSWORD

# Windows
echo %JASYPT_ENCRYPTOR_PASSWORD%

# PowerShell
echo $env:JASYPT_ENCRYPTOR_PASSWORD
```

## 相关文件

- `backend/pom.xml` - Jasypt 依赖配置
- `backend/src/main/java/com/getjobs/application/config/JasyptConfig.java` - Jasypt 配置类
- `backend/src/main/java/com/getjobs/common/util/ConfigEncryptor.java` - 加密工具类
- `backend/src/main/java/com/getjobs/common/util/EncryptionTool.java` - 命令行加密工具
- `backend/src/main/resources/application.yaml` - 应用配置文件
- `encrypt-config.bat` - Windows 加密脚本
- `encrypt-config.sh` - Unix/Linux/Mac 加密脚本

## 加密算法说明

- **算法**: PBEWithMD5AndDES
- **迭代次数**: 1000
- **盐值**: 随机生成 (8字节)
- **输出格式**: Base64

## 性能影响

- **解密开销**: 每次应用启动时解密,运行时缓存
- **启动延迟**: 约增加 100-200ms
- **运行性能**: 无影响

## 升级指南

### 从明文迁移到加密

1. **备份当前配置**
   ```bash
   cp application.yaml application.yaml.backup
   ```

2. **加密密码**
   ```bash
   ./encrypt-config.sh "your_db_password"
   ```

3. **更新配置文件**
   ```yaml
   # 替换明文密码为 ENC(...) 格式
   password: ENC(encrypted_value)
   ```

4. **测试启动**
   ```bash
   java -jar get_jobs.jar
   ```

5. **删除备份**
   ```bash
   rm application.yaml.backup
   ```

---

**安全提示**: 配置加密只是安全的一层,还应配合其他安全措施(网络隔离、访问控制、审计日志等)共同保护系统安全。
