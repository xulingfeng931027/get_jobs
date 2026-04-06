export {};

declare global {
  interface Window {
    electronAPI?: {
      // ========== 投递相关 ==========
      delivery: {
        /** 开始投递 */
        start: (platform: string) => Promise<{ success: boolean; message?: string }>;
        /** 停止投递 */
        stop: (platform: string) => Promise<{ success: boolean; message?: string }>;
        /** 获取投递状态 */
        getStatus: () => Promise<Record<string, DeliveryStatus>>;
        /** 监听投递进度 */
        onProgress: (callback: (message: ProgressMessage) => void) => () => void;
      };

      // ========== 配置相关 ==========
      config: {
        /** 从云端同步配置 */
        sync: () => Promise<{ success: boolean; config?: CloudConfig; message?: string }>;
        /** 获取本地配置 */
        get: () => Promise<CloudConfig>;
      };

      // ========== 计费相关 ==========
      billing: {
        /** 投递预检查 */
        preCheck: (platform: string, expectedCount: number) => Promise<PreCheckResult>;
      };

      // ========== 浏览器相关 ==========
      browser: {
        /** 执行平台登录 */
        login: (platform: string) => Promise<{ success: boolean; message?: string }>;
        /** 登出平台 */
        logout: (platform: string) => Promise<{ success: boolean; message?: string }>;
        /** 检查是否已登录 */
        isLoggedIn: (platform: string) => Promise<boolean>;
      };

      // ========== 设备相关 ==========
      device: {
        /** 获取设备ID */
        getId: () => Promise<string>;
      };

      // ========== 后端管理（原有） ==========
      restartBackend: () => Promise<{ success: boolean; error?: string }>;
      stopBackend: () => Promise<{ success: boolean; error?: string }>;
      getBackendStatus: () => Promise<BackendStatus>;

      // ========== 更新管理 ==========
      checkForUpdates: () => Promise<{ success: boolean; message?: string }>;

      // ========== 应用信息 ==========
      getAppInfo: () => Promise<AppInfo>;

      // ========== 日志 ==========
      openLogsFolder: () => Promise<void>;

      // ========== 事件监听 ==========
      onBackendStatus: (callback: (data: BackendStatus) => void) => () => void;
      onBackendLog: (callback: (message: string) => void) => () => void;
      onUpdateAvailable: (callback: (info: any) => void) => () => void;
      onUpdateProgress: (callback: (data: any) => void) => () => void;
      onUpdateDownloaded: (callback: (info: any) => void) => () => void;
      onUpdateError: (callback: (error: string) => void) => () => void;
    };
  }
}

// ========== 类型定义 ==========

export interface ProgressMessage {
  platform: string;
  type: 'info' | 'warning' | 'error' | 'success';
  message: string;
  data?: any;
}

export interface DeliveryStatus {
  platform: string;
  isRunning: boolean;
  progress: number;
  total: number;
  message: string;
}

export interface CloudConfig {
  boss: BossConfig;
  liepin: LiepinConfig;
  zhilian: ZhilianConfig;
  job51: Job51Config;
  aiConfig: AiConfig;
  lastSyncTime: string | null;
}

export interface BossConfig {
  sayHi?: string;
  debugger?: boolean;
  keywords: string[];
  cityCode: string[];
  customCityCode?: Record<string, string>;
  industry?: string[];
  experience?: string[];
  jobType?: string;
  salary?: string[];
  degree?: string[];
  scale?: string[];
  stage?: string[];
  enableAI?: boolean;
  filterDeadHR?: boolean;
  sendImgResume?: boolean;
  expectedSalary?: number[];
  waitTime?: string;
  deadStatus?: string[];
}

export interface LiepinConfig {
  keywords: string[];
  cityCode: string;
  salary?: string;
  experience?: string;
  degree?: string;
  enableAI?: boolean;
  sayHi?: string;
}

export interface ZhilianConfig {
  keywords: string[];
  cityCode: string[];
  salary?: string;
  experience?: string;
  degree?: string;
  jobType?: string;
  enableAI?: boolean;
  sayHi?: string;
}

export interface Job51Config {
  keywords: string[];
  cityCode: string[];
  salary?: string;
  experience?: string;
  degree?: string;
  jobType?: string;
  enableAI?: boolean;
  sayHi?: string;
}

export interface AiConfig {
  BASE_URL: string;
  API_KEY: string;
  MODEL: string;
}

export interface PreCheckResult {
  success: boolean;
  allowed: boolean;
  remainingCount: number;
  hasSubscription: boolean;
  subscriptionEndDate?: string;
  reason?: string;
  billingInfo?: BillingInfo;
}

export interface BillingInfo {
  applicationCount: number;
  hasSubscription: boolean;
  subscriptionEndDate?: string;
  aiMatchCount: number;
  aiGreetCount: number;
  reportCount: number;
  totalRecharge: number;
  totalConsumption: number;
}

export interface BackendStatus {
  status: 'stopped' | 'starting' | 'started' | 'error';
  running: boolean;
  pid: number | null;
  port: number | null;
}

export interface AppInfo {
  version: string;
  isDev: boolean;
  platform: string;
  arch: string;
}