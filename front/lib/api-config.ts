// API 配置文件 - 统一管理所有 API 地址
// 不要直接修改此文件中的 URL，如需修改请在 .env 文件中设置 API_BASE_URL

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:18888'

// API 路径配置
export const API_PATHS = {
  // 健康检查
  health: `${API_BASE_URL}/api/health`,
  actuatorHealth: `${API_BASE_URL}/actuator/health`,

  // 登录状态 SSE
  loginStatusStream: `${API_BASE_URL}/api/jobs/login-status/stream`,

  // Cookie 管理
  cookieSave: (platform: string) => `${API_BASE_URL}/api/cookie/save?platform=${platform}`,

  // 环境配置
  config: `${API_BASE_URL}/api/config`,

  // AI 配置
  aiConfig: `${API_BASE_URL}/api/ai/config`,

  // 公共选项
  commonOption: `${API_BASE_URL}/api/common-option`,
  commonOptionById: (id: number | string) => `${API_BASE_URL}/api/common-option/${id}`,

  // 搜索预设
  searchPreset: `${API_BASE_URL}/api/search-preset`,
  searchPresetById: (id: number | string) => `${API_BASE_URL}/api/search-preset/${id}`,

  // Boss 直聘
  boss: {
    config: `${API_BASE_URL}/api/boss/config`,
    start: `${API_BASE_URL}/api/boss/start`,
    stop: `${API_BASE_URL}/api/boss/stop`,
    logout: `${API_BASE_URL}/api/boss/logout`,
    blacklist: `${API_BASE_URL}/api/boss/config/blacklist`,
    blacklistById: (id: number | string) => `${API_BASE_URL}/api/boss/config/blacklist/${id}`,
    list: `${API_BASE_URL}/api/boss/list`,
    stats: `${API_BASE_URL}/api/boss/stats`,
    reload: `${API_BASE_URL}/api/boss/reload`,
  },

  // 智联招聘
  zhilian: {
    config: `${API_BASE_URL}/api/zhilian/config`,
    start: `${API_BASE_URL}/api/zhilian/start`,
    stop: `${API_BASE_URL}/api/zhilian/stop`,
    logout: `${API_BASE_URL}/api/zhilian/logout`,
    list: `${API_BASE_URL}/api/zhilian/list`,
    stats: `${API_BASE_URL}/api/zhilian/stats`,
  },

  // 51job
  job51: {
    config: `${API_BASE_URL}/api/51job/config`,
    start: `${API_BASE_URL}/api/51job/start`,
    stop: `${API_BASE_URL}/api/51job/stop`,
    logout: `${API_BASE_URL}/api/51job/logout`,
    list: `${API_BASE_URL}/api/51job/list`,
    stats: `${API_BASE_URL}/api/51job/stats`,
    reload: `${API_BASE_URL}/api/51job/reload`,
  },

  // 猎聘
  liepin: {
    config: `${API_BASE_URL}/api/liepin/config`,
    start: `${API_BASE_URL}/api/liepin/start`,
    stop: `${API_BASE_URL}/api/liepin/stop`,
    logout: `${API_BASE_URL}/api/liepin/logout`,
    list: `${API_BASE_URL}/api/liepin/list`,
    stats: `${API_BASE_URL}/api/liepin/stats`,
  },
} as const

// 导出基础 URL（用于需要动态构建 URL 的场景）
export { API_BASE_URL }
