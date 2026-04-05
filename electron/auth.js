/**
 * Electron 客户端授权验证模块
 * 启动时验证用户登录状态和授权密钥
 */

const { ipcMain, dialog } = require('electron');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

class AuthManager {
  constructor(mainWindow, apiBaseUrl = 'http://localhost:18888') {
    this.mainWindow = mainWindow;
    this.apiBaseUrl = apiBaseUrl;
    this.userDataPath = null;
    this.authFilePath = null;
    this.isAuthorized = false;
    this.currentUser = null;
  }

  // 初始化
  init(userDataPath) {
    this.userDataPath = userDataPath;
    this.authFilePath = path.join(userDataPath, 'auth.json');
  }

  // 获取设备指纹
  getDeviceFingerprint() {
    // 使用机器名 + CPU 信息生成设备指纹
    const hostname = require('os').hostname();
    const cpus = require('os').cpus();
    const cpuModel = cpus.length > 0 ? cpus[0].model : 'unknown';
    const platform = require('os').platform();
    
    const raw = `${hostname}-${cpuModel}-${platform}`;
    return crypto.createHash('sha256').update(raw).digest('hex').substring(0, 64);
  }

  // 加载本地授权信息
  loadAuth() {
    try {
      if (fs.existsSync(this.authFilePath)) {
        const data = JSON.parse(fs.readFileSync(this.authFilePath, 'utf8'));
        this.currentUser = data.user;
        this.isAuthorized = data.isAuthorized;
        return data;
      }
    } catch (error) {
      console.error('加载授权信息失败:', error);
    }
    return null;
  }

  // 保存授权信息
  saveAuth(authData) {
    try {
      fs.writeFileSync(this.authFilePath, JSON.stringify(authData, null, 2), 'utf8');
    } catch (error) {
      console.error('保存授权信息失败:', error);
    }
  }

  // 清除授权信息（退出登录）
  clearAuth() {
    try {
      if (fs.existsSync(this.authFilePath)) {
        fs.unlinkSync(this.authFilePath);
      }
      this.currentUser = null;
      this.isAuthorized = false;
    } catch (error) {
      console.error('清除授权信息失败:', error);
    }
  }

  // 验证服务器上的授权状态
  async verifyAuthOnServer() {
    if (!this.currentUser || !this.currentUser.token) {
      return { valid: false, message: '未登录' };
    }

    try {
      const response = await fetch(`${this.apiBaseUrl}/api/user/profile`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${this.currentUser.token}`,
          'Content-Type': 'application/json',
        },
        // 设置较短的超时时间
        signal: AbortSignal.timeout(5000),
      });

      if (!response.ok) {
        return { valid: false, message: '授权已过期，请重新登录' };
      }

      const data = await response.json();
      if (data.success) {
        // 更新用户信息
        this.currentUser = {
          ...this.currentUser,
          ...data.data,
        };
        this.saveAuth({ user: this.currentUser, isAuthorized: true });
        return { valid: true, user: data.data };
      }

      return { valid: false, message: data.message || '授权验证失败' };
    } catch (error) {
      // 服务器不可达时，允许本地继续使用（离线模式）
      console.warn('服务器不可达，使用本地授权:', error.message);
      return { valid: true, offline: true, message: '离线模式' };
    }
  }

  // 启动时验证授权
  async checkAuth() {
    const auth = this.loadAuth();
    
    if (!auth || !auth.isAuthorized) {
      // 未登录，需要登录
      return { authorized: false, reason: '未登录' };
    }

    // 验证服务器上的授权
    const verifyResult = await this.verifyAuthOnServer();
    
    if (verifyResult.valid) {
      this.isAuthorized = true;
      return { authorized: true, user: this.currentUser, offline: verifyResult.offline };
    }

    // 授权无效
    this.clearAuth();
    return { authorized: false, reason: verifyResult.message };
  }

  // 设置 IPC 事件处理
  setupIpcHandlers() {
    // 检查授权状态
    ipcMain.handle('check-auth', async () => {
      return await this.checkAuth();
    });

    // 登录
    ipcMain.handle('login', async (event, { username, password }) => {
      try {
        const response = await fetch(`${this.apiBaseUrl}/api/user/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password }),
          signal: AbortSignal.timeout(10000),
        });

        const data = await response.json();
        if (data.success) {
          this.currentUser = {
            userId: data.userId,
            username: data.username,
            email: data.email,
            phone: data.phone,
            token: data.token,
          };
          this.isAuthorized = true;
          this.saveAuth({ user: this.currentUser, isAuthorized: true });
          
          // 获取设备指纹并绑定
          const deviceFingerprint = this.getDeviceFingerprint();
          await this.bindDevice(deviceFingerprint);
        }
        return data;
      } catch (error) {
        return { success: false, message: `登录失败: ${error.message}` };
      }
    });

    // 退出登录
    ipcMain.handle('logout', () => {
      this.clearAuth();
      return { success: true };
    });

    // 获取用户信息
    ipcMain.handle('get-user-info', () => {
      return this.currentUser;
    });

    // 获取设备指纹
    ipcMain.handle('get-device-fingerprint', () => {
      return this.getDeviceFingerprint();
    });

    // 激活充值码
    ipcMain.handle('activate-recharge-code', async (event, code) => {
      if (!this.currentUser || !this.currentUser.token) {
        return { success: false, message: '请先登录' };
      }

      try {
        const response = await fetch(`${this.apiBaseUrl}/api/billing/recharge`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${this.currentUser.token}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ code }),
          signal: AbortSignal.timeout(10000),
        });

        return await response.json();
      } catch (error) {
        return { success: false, message: `充值失败: ${error.message}` };
      }
    });

    // 查询余额
    ipcMain.handle('get-balance', async () => {
      if (!this.currentUser || !this.currentUser.token) {
        return { success: false, message: '请先登录' };
      }

      try {
        const response = await fetch(`${this.apiBaseUrl}/api/billing/balance`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${this.currentUser.token}`,
            'Content-Type': 'application/json',
          },
          signal: AbortSignal.timeout(5000),
        });

        return await response.json();
      } catch (error) {
        return { success: false, message: `查询失败: ${error.message}` };
      }
    });
  }

  // 绑定设备
  async bindDevice(deviceFingerprint) {
    if (!this.currentUser || !this.currentUser.token) return;

    try {
      await fetch(`${this.apiBaseUrl}/api/user/device/bind`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.currentUser.token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ deviceFingerprint, deviceName: require('os').hostname() }),
        signal: AbortSignal.timeout(5000),
      });
    } catch (error) {
      console.warn('设备绑定失败:', error.message);
    }
  }
}

module.exports = AuthManager;
