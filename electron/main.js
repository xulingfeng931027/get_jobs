const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('path');
const { autoUpdater } = require('electron-updater');
const log = require('electron-log');
const BackendManager = require('./backend-manager');
const AuthManager = require('./auth');

// 配置日志
log.transports.file.level = 'info';
log.info('Get Jobs Desktop 应用启动');

// 保持对窗口对象的全局引用，如果不这样做，当 JavaScript 对象被垃圾回收时，窗口会自动关闭
let mainWindow;
let backendManager;
let authManager;

// 检查是否为开发模式
const isDev = process.env.NODE_ENV === 'development' || !app.isPackaged;

// 创建主窗口
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
    title: 'Get Jobs - 自动化求职平台',
    icon: path.join(__dirname, 'assets', 'icon.png'),
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false
    }
  });

  // 加载应用
  if (isDev) {
    mainWindow.loadURL('http://localhost:3000');
    // 打开开发者工具
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(path.join(__dirname, '..', 'front', 'out', 'index.html'));
  }

  // 窗口关闭时触发
  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  // 阻止外部导航
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    require('electron').shell.openExternal(url);
    return { action: 'deny' };
  });
}

// 初始化后端服务
async function initBackend() {
  try {
    log.info('初始化后端服务...');
    
    backendManager = new BackendManager({
      jarPath: getJarPath(),
      resourcesPath: getResourcesPath(),
      port: 8080
    });

    // 监听后端状态
    backendManager.on('starting', () => {
      log.info('后端服务启动中...');
      mainWindow.webContents.send('backend-status', { status: 'starting' });
    });

    backendManager.on('started', (port) => {
      log.info(`后端服务已启动，端口: ${port}`);
      mainWindow.webContents.send('backend-status', { status: 'started', port });
    });

    backendManager.on('error', (error) => {
      log.error('后端服务启动失败:', error);
      mainWindow.webContents.send('backend-status', { status: 'error', error: error.message });
      
      dialog.showErrorBox('后端启动失败', 
        `后端服务启动失败: ${error.message}\n\n请检查Java环境是否正确安装。`);
    });

    backendManager.on('log', (message) => {
      log.info('后端日志:', message);
      mainWindow.webContents.send('backend-log', message);
    });

    // 启动后端
    await backendManager.start();
    
    // 初始化授权管理器（在后端启动后）
    initAuth();
    
  } catch (error) {
    log.error('后端初始化失败:', error);
    throw error;
  }
}

// 初始化授权
function initAuth() {
  authManager = new AuthManager(mainWindow);
  authManager.init(app.getPath('userData'));
  authManager.setupIpcHandlers();
  
  log.info('授权管理器初始化完成');
}

// 获取JAR文件路径
function getJarPath() {
  if (isDev) {
    return path.join(__dirname, '..', 'build', 'libs', 'get-jobs.jar');
  } else {
    // 打包后的路径
    const resourcePath = process.resourcesPath;
    return path.join(resourcePath, 'app', 'get-jobs.jar');
  }
}

// 获取资源路径
function getResourcesPath() {
  if (isDev) {
    return path.join(__dirname, '..', 'src', 'main', 'resources');
  } else {
    const resourcePath = process.resourcesPath;
    return path.join(resourcePath, 'resources');
  }
}

// 配置自动更新
function setupAutoUpdater() {
  if (isDev) return; // 开发模式不检查更新

  autoUpdater.logger = log;
  autoUpdater.autoDownload = true;
  autoUpdater.autoInstallOnAppQuit = true;

  // 检查更新
  autoUpdater.checkForUpdatesAndNotify();

  // 定时检查更新（每6小时）
  setInterval(() => {
    autoUpdater.checkForUpdates();
  }, 6 * 60 * 60 * 1000);

  // 更新可用
  autoUpdater.on('update-available', (info) => {
    log.info('发现新版本:', info.version);
    mainWindow.webContents.send('update-available', info);
  });

  // 下载进度
  autoUpdater.on('download-progress', (progressObj) => {
    const percent = Math.round(progressObj.percent);
    log.info(`下载进度: ${percent}%`);
    mainWindow.webContents.send('update-progress', {
      percent,
      transferred: progressObj.transferred,
      total: progressObj.total
    });
  });

  // 更新下载完成
  autoUpdater.on('update-downloaded', (info) => {
    log.info('更新下载完成，准备安装');
    mainWindow.webContents.send('update-downloaded', info);
    
    // 询问用户是否立即安装
    dialog.showMessageBox(mainWindow, {
      type: 'info',
      title: '更新已就绪',
      message: '发现新版本，是否立即安装并重启？',
      buttons: ['立即安装', '稍后'],
      defaultId: 0
    }).then(result => {
      if (result.response === 0) {
        autoUpdater.quitAndInstall();
      }
    });
  });

  // 更新错误
  autoUpdater.on('error', (error) => {
    log.error('更新失败:', error);
    mainWindow.webContents.send('update-error', error.message);
  });
}

// IPC 事件处理
function setupIpcHandlers() {
  // 重启后端
  ipcMain.handle('restart-backend', async () => {
    try {
      await backendManager.restart();
      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  });

  // 停止后端
  ipcMain.handle('stop-backend', async () => {
    try {
      await backendManager.stop();
      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  });

  // 获取后端状态
  ipcMain.handle('get-backend-status', async () => {
    return backendManager.getStatus();
  });

  // 手动检查更新
  ipcMain.handle('check-for-updates', async () => {
    if (isDev) {
      return { success: false, message: '开发模式不支持更新检查' };
    }
    try {
      await autoUpdater.checkForUpdates();
      return { success: true };
    } catch (error) {
      return { success: false, message: error.message };
    }
  });

  // 获取应用信息
  ipcMain.handle('get-app-info', () => {
    return {
      version: app.getVersion(),
      isDev,
      platform: process.platform,
      arch: process.arch
    };
  });

  // 打开日志文件夹
  ipcMain.handle('open-logs-folder', () => {
    require('electron').shell.openPath(log.transports.file.getFile().path);
  });
}

// 应用准备就绪
app.whenReady().then(async () => {
  // 设置 IPC 处理程序
  setupIpcHandlers();
  
  // 创建窗口
  createWindow();

  // 初始化后端
  try {
    await initBackend();
  } catch (error) {
    log.error('后端初始化失败，但应用继续运行:', error);
  }

  // 设置自动更新
  setupAutoUpdater();

  // macOS 特殊处理
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

// 所有窗口关闭时
app.on('window-all-closed', async () => {
  // 先停止后端
  if (backendManager) {
    log.info('正在停止后端服务...');
    await backendManager.stop();
  }
  
  // macOS 以外的平台退出应用
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

// 应用退出前清理
app.on('before-quit', async (event) => {
  if (backendManager && backendManager.isRunning()) {
    event.preventDefault();
    log.info('正在停止后端服务...');
    await backendManager.stop();
    app.quit();
  }
});

// 获取应用版本（用于更新服务器）
app.on('ready', () => {
  log.info(`Get Jobs Desktop v${app.getVersion()} 启动完成`);
});
