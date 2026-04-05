const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const EventEmitter = require('events');
const log = require('electron-log');

class BackendManager extends EventEmitter {
  constructor(options) {
    super();
    this.jarPath = options.jarPath;
    this.resourcesPath = options.resourcesPath;
    this.port = options.port || 8080;
    this.process = null;
    this.status = 'stopped'; // stopped, starting, started, error
  }

  /**
   * 启动后端服务
   */
  async start() {
    if (this.isRunning()) {
      log.info('后端服务已在运行');
      return;
    }

    // 检查 JAR 文件是否存在
    if (!fs.existsSync(this.jarPath)) {
      throw new Error(`JAR 文件不存在: ${this.jarPath}`);
    }

    // 检查 Java 是否安装
    if (!this.checkJavaInstalled()) {
      throw new Error('未检测到 Java 环境，请先安装 JDK 21 或以上版本');
    }

    this.status = 'starting';
    this.emit('starting');

    return new Promise((resolve, reject) => {
      try {
        // 构建启动命令
        const javaPath = this.findJavaPath();
        const args = [
          '-jar',
          this.jarPath,
          `--server.port=${this.port}`,
          `--resources.path=${this.resourcesPath}`
        ];

        log.info('启动后端服务:', javaPath, args.join(' '));

        this.process = spawn(javaPath, args, {
          cwd: path.dirname(this.jarPath),
          stdio: ['pipe', 'pipe', 'pipe'],
          env: {
            ...process.env,
            JAVA_OPTS: '-Xmx512m -Xms256m',
            RESOURCES_PATH: this.resourcesPath
          }
        });

        // 监听标准输出
        this.process.stdout.on('data', (data) => {
          const message = data.toString().trim();
          log.info('后端输出:', message);
          this.emit('log', message);

          // 检测启动成功
          if (message.includes('Started GetJobsApplication') || 
              message.includes('Tomcat started on port')) {
            this.status = 'started';
            this.emit('started', this.port);
            resolve();
          }
        });

        // 监听错误输出
        this.process.stderr.on('data', (data) => {
          const message = data.toString().trim();
          log.error('后端错误:', message);
          this.emit('log', message);
        });

        // 监听进程退出
        this.process.on('close', (code) => {
          log.info(`后端进程退出，退出码: ${code}`);
          this.process = null;
          this.status = 'stopped';
          
          if (code !== 0 && code !== null) {
            this.status = 'error';
            this.emit('error', new Error(`后端进程异常退出，退出码: ${code}`));
          }
        });

        // 监听进程错误
        this.process.on('error', (error) => {
          log.error('后端进程错误:', error);
          this.status = 'error';
          this.emit('error', error);
          reject(error);
        });

        // 设置启动超时（30秒）
        setTimeout(() => {
          if (this.status === 'starting') {
            reject(new Error('后端启动超时（30秒）'));
          }
        }, 30000);

      } catch (error) {
        this.status = 'error';
        reject(error);
      }
    });
  }

  /**
   * 停止后端服务
   */
  async stop() {
    if (!this.isRunning()) {
      return;
    }

    return new Promise((resolve) => {
      log.info('正在停止后端服务...');
      
      // Windows 使用 taskkill，其他平台使用 SIGTERM
      if (process.platform === 'win32') {
        spawn('taskkill', ['/pid', this.process.pid, '/f', '/t']);
      } else {
        this.process.kill('SIGTERM');
      }

      this.process = null;
      this.status = 'stopped';
      resolve();
    });
  }

  /**
   * 重启后端服务
   */
  async restart() {
    log.info('重启后端服务...');
    await this.stop();
    
    // 等待一段时间确保进程完全退出
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    return this.start();
  }

  /**
   * 检查后端是否运行
   */
  isRunning() {
    return this.process !== null && !this.process.killed;
  }

  /**
   * 获取后端状态
   */
  getStatus() {
    return {
      status: this.status,
      running: this.isRunning(),
      pid: this.process ? this.process.pid : null,
      port: this.port
    };
  }

  /**
   * 检查 Java 是否安装
   */
  checkJavaInstalled() {
    try {
      const { execSync } = require('child_process');
      const version = execSync('java -version', { encoding: 'utf8' });
      return version.includes('version');
    } catch (error) {
      return false;
    }
  }

  /**
   * 查找 Java 路径
   */
  findJavaPath() {
    return 'java'; // 使用系统 PATH 中的 java
  }
}

module.exports = BackendManager;
