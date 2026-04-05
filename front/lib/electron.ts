/**
 * Electron 环境检测工具
 * 用于判断当前是否运行在 Electron 环境中
 */

export function isElectron(): boolean {
  // Renderer process
  if (typeof window !== 'undefined' && typeof window.process === 'object' && (window.process as any).type === 'renderer') {
    return true;
  }

  // Main process
  if (typeof process !== 'undefined' && typeof process.versions === 'object' && !!(process.versions as any).electron) {
    return true;
  }

  // Detect the user agent when the `nodeIntegration` option is NOT set
  if (
    typeof navigator !== 'undefined' &&
    typeof navigator.userAgent === 'string' &&
    navigator.userAgent.indexOf('Electron') >= 0
  ) {
    return true;
  }

  return false;
}

/**
 * 获取 Electron API（如果可用）
 */
export function getElectronAPI() {
  if (typeof window !== 'undefined' && (window as any).electronAPI) {
    return (window as any).electronAPI;
  }
  return null;
}

/**
 * 安全地调用 Electron API
 */
export function callElectronAPI(method: string, ...args: any[]): Promise<any> {
  const api = getElectronAPI();
  if (api && typeof api[method] === 'function') {
    return api[method](...args);
  }
  throw new Error(`Electron API 不可用: ${method}`);
}
