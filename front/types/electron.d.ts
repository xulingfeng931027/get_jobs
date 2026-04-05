export {};

declare global {
  interface Window {
    electronAPI?: {
      // 后端管理
      restartBackend: () => Promise<{ success: boolean; error?: string }>;
      stopBackend: () => Promise<{ success: boolean; error?: string }>;
      getBackendStatus: () => Promise<{
        status: 'stopped' | 'starting' | 'started' | 'error';
        running: boolean;
        pid: number | null;
        port: number | null;
      }>;
      
      // 更新管理
      checkForUpdates: () => Promise<{ success: boolean; message?: string }>;
      
      // 应用信息
      getAppInfo: () => Promise<{
        version: string;
        isDev: boolean;
        platform: string;
        arch: string;
      }>;
      
      // 日志
      openLogsFolder: () => Promise<void>;
      
      // 事件监听
      onBackendStatus: (callback: (data: any) => void) => void;
      onBackendLog: (callback: (message: string) => void) => void;
      onUpdateAvailable: (callback: (info: any) => void) => void;
      onUpdateProgress: (callback: (data: any) => void) => void;
      onUpdateDownloaded: (callback: (info: any) => void) => void;
      onUpdateError: (callback: (error: string) => void) => void;
      
      // 移除监听器
      removeAllListeners: (channel: string) => void;
    };
  }
}

