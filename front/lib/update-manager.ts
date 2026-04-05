import {getElectronAPI, isElectron} from '@/lib/electron';

export interface UpdateInfo {
  version: string;
  releaseDate: string;
  releaseNotes?: string;
}

export interface UpdateProgress {
  percent: number;
  transferred: number;
  total: number;
}

export class UpdateManager {
  private static instance: UpdateManager;
  private api: any;

  private constructor() {
    this.api = getElectronAPI();
  }

  static getInstance(): UpdateManager {
    if (!UpdateManager.instance) {
      UpdateManager.instance = new UpdateManager();
    }
    return UpdateManager.instance;
  }

  /**
   * 检查是否有可用更新
   */
  async checkForUpdates(): Promise<{ success: boolean; message?: string }> {
    if (!isElectron() || !this.api) {
      return { success: false, message: '不在 Electron 环境中' };
    }

    try {
      return await this.api.checkForUpdates();
    } catch (error: any) {
      return { success: false, message: error.message };
    }
  }

  /**
   * 监听更新可用事件
   */
  onUpdateAvailable(callback: (info: UpdateInfo) => void) {
    if (!isElectron() || !this.api) return;
    
    this.api.onUpdateAvailable(callback);
  }

  /**
   * 监听下载进度
   */
  onUpdateProgress(callback: (progress: UpdateProgress) => void) {
    if (!isElectron() || !this.api) return;
    
    this.api.onUpdateProgress(callback);
  }

  /**
   * 监听更新下载完成
   */
  onUpdateDownloaded(callback: (info: UpdateInfo) => void) {
    if (!isElectron() || !this.api) return;
    
    this.api.onUpdateDownloaded(callback);
  }

  /**
   * 监听更新错误
   */
  onUpdateError(callback: (error: string) => void) {
    if (!isElectron() || !this.api) return;
    
    this.api.onUpdateError(callback);
  }

  /**
   * 移除所有监听器
   */
  removeAllListeners() {
    if (!isElectron() || !this.api) return;
    
    this.api.removeAllListeners('update-available');
    this.api.removeAllListeners('update-progress');
    this.api.removeAllListeners('update-downloaded');
    this.api.removeAllListeners('update-error');
  }
}

export const updateManager = UpdateManager.getInstance();
