import { useState, useEffect, useCallback } from 'react';
import { isElectron, getElectronAPI } from '@/lib/electron';

export interface BackendStatus {
  status: 'stopped' | 'starting' | 'started' | 'error';
  running: boolean;
  pid: number | null;
  port: number | null;
}

export function useBackendStatus() {
  const [status, setStatus] = useState<BackendStatus>({
    status: 'stopped',
    running: false,
    pid: null,
    port: null
  });
  const [logs, setLogs] = useState<string[]>([]);

  const fetchStatus = useCallback(async () => {
    if (!isElectron()) return;
    
    try {
      const api = getElectronAPI();
      if (api) {
        const backendStatus = await api.getBackendStatus();
        setStatus(backendStatus);
      }
    } catch (error) {
      console.error('获取后端状态失败:', error);
    }
  }, []);

  useEffect(() => {
    if (!isElectron()) return;

    const api = getElectronAPI();
    if (!api) return;

    // 监听后端状态变化
    api.onBackendStatus((data: BackendStatus) => {
      setStatus(data);
    });

    // 监听后端日志
    api.onBackendLog((message: string) => {
      setLogs(prev => [...prev.slice(-100), message]);
    });

    // 初始获取状态
    fetchStatus();

    // 定时刷新状态
    const interval = setInterval(fetchStatus, 5000);

    return () => {
      clearInterval(interval);
      api.removeAllListeners('backend-status');
      api.removeAllListeners('backend-log');
    };
  }, [fetchStatus]);

  const restartBackend = useCallback(async () => {
    if (!isElectron()) return;
    
    try {
      const api = getElectronAPI();
      if (api) {
        const result = await api.restartBackend();
        return result;
      }
    } catch (error) {
      console.error('重启后端失败:', error);
      throw error;
    }
  }, []);

  const stopBackend = useCallback(async () => {
    if (!isElectron()) return;
    
    try {
      const api = getElectronAPI();
      if (api) {
        const result = await api.stopBackend();
        return result;
      }
    } catch (error) {
      console.error('停止后端失败:', error);
      throw error;
    }
  }, []);

  return {
    status,
    logs,
    restartBackend,
    stopBackend,
    refreshStatus: fetchStatus
  };
}
