# Get Jobs 自动化求职系统 - 分发版本

## 快速开始

### 系统要求

- **Java 运行环境**: Java 21 或更高版本
  - 下载地址: https://adoptium.net/
  - 推荐: Eclipse Temurin 21 LTS

- **数据库**: MySQL 8.0+
  - 需要预先安装并创建数据库

### 安装步骤

#### Windows

1. 解压 `get-jobs-windows.zip` 到目标目录
2. 配置数据库连接（见下方配置说明）
3. 双击运行 `start.bat` 或在命令行执行

#### Mac / Linux

1. 解压 `get-jobs-unix.tar.gz`:
   ```bash
   tar -xzf get-jobs-unix.tar.gz
   ```

2. 赋予脚本执行权限（如需要）:
   ```bash
   chmod +x start.sh stop.sh
   ```

3. 配置数据库连接（见下方配置说明）
4. 运行启动脚本:
   ```bash
   ./start.sh
   ```

### 配置说明

#### 数据库配置

编辑 `config/application.yaml` 文件，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/get_jobs?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

#### 端口配置

默认服务端口为 `18888`，可在配置文件中修改：

```yaml
server:
  port: 18888
```

#### 日志配置

日志文件保存在 `logs/` 目录下，可在配置文件中调整日志级别和路径。

### 运行控制

#### Windows

- 启动: 双击 `start.bat` 或命令行执行 `start.bat`
- 停止: 双击 `stop.bat` 或命令行执行 `stop.bat`

#### Mac / Linux

- 启动: `./start.sh`
- 停止: `./stop.sh`

### 目录结构

```
get-jobs/
├── lib/                    # 应用程序库
│   └── get-jobs.jar       # 主应用程序
├── config/                 # 配置文件目录
│   └── application.yaml   # 应用配置
├── logs/                   # 日志目录（自动生成）
│   ├── get-jobs.log       # 应用日志
│   └── console.log        # 控制台输出
├── data/                   # 数据目录（自动生成）
├── start.bat / start.sh   # 启动脚本
└── stop.bat / stop.sh     # 停止脚本
```

### 常见问题

#### Java 未找到

确保已安装 Java 21+ 并配置了 JAVA_HOME 环境变量：

```bash
# 检查 Java 版本
java -version

# 设置 JAVA_HOME (Windows)
set JAVA_HOME=C:\Program Files\Java\jdk-21

# 设置 JAVA_HOME (Mac/Linux)
export JAVA_HOME=/usr/lib/jvm/java-21
```

#### 端口被占用

修改 `config/application.yaml` 中的 `server.port` 为其他端口。

#### 数据库连接失败

1. 检查 MySQL 服务是否运行
2. 确认数据库 `get_jobs` 已创建
3. 验证用户名和密码是否正确
4. 检查防火墙设置

### 技术支持

- GitHub: https://github.com/loks666/get_jobs
- 提交 Issue: https://github.com/loks666/get_jobs/issues

### 许可证

本项目遵循原项目许可证条款。
