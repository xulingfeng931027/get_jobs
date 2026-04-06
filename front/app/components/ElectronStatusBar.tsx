'use client';

import {useEffect, useState} from 'react';
import {useBackendStatus} from '@/hooks/useBackendStatus';
import {getElectronAPI, isElectron} from '@/lib/electron';
import {updateManager} from '@/lib/update-manager';

export default function ElectronStatusBar() {
  const { status, logs, restartBackend } = useBackendStatus();
  const [appInfo, setAppInfo] = useState<any>(null);
  const [updateProgress, setUpdateProgress] = useState<number | null>(null);
  const [checkingUpdate, setCheckingUpdate] = useState(false);

  useEffect(() => {
    if (!isElectron()) return;

    const api = getElectronAPI();
    if (!api) return;

    // 获取应用信息
    api.getAppInfo().then((info: any) => {
      setAppInfo(info);
    });

    // 监听更新进度
    api.onUpdateProgress((data: any) => {
      setUpdateProgress(data.percent);
    });

    api.onUpdateDownloaded(() => {
      setUpdateProgress(null);
    });
  }, []);

  const handleCheckUpdate = async () => {
    setCheckingUpdate(true);
    try {
      await updateManager.checkForUpdates();
    } catch (error) {
      console.error('检查更新失败:', error);
    } finally {
      setCheckingUpdate(false);
    }
  };

  const handleRestartBackend = async () => {
    try {
      await restartBackend();
    } catch (error) {
      console.error('重启后端失败:', error);
    }
  };

  const handleOpenLogs = async () => {
    const api = getElectronAPI();
    if (api) {
      await api.openLogsFolder();
    }
  };

  if (!isElectron()) {
    return null; // 不在 Electron 环境中，不显示
  }

  const getStatusColor = () => {
    switch (status.status) {
      case 'started':
        return 'bg-green-500';
      case 'starting':
        return 'bg-yellow-500';
      case 'error':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  };

  const getStatusText = () => {
    switch (status.status) {
      case 'started':
        return '后端运行中';
      case 'starting':
        return '后端启动中';
      case 'error':
        return '后端错误';
      default:
        return '后端已停止';
    }
  };

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 px-4 py-2 text-sm z-50">
      <div className="flex items-center justify-between max-w-7xl mx-auto">
        {/* 左侧：后端状态 */}
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <div className={`w-2 h-2 rounded-full ${getStatusColor()} animate-pulse`} />
            <span className="text-gray-700 dark:text-gray-300">{getStatusText()}</span>
            {status.port && (
              <span className="text-gray-500 dark:text-gray-400 text-xs">
                (端口: {status.port})
              </span>
            )}
          </div>

          {status.running && (
            <button
              onClick={handleRestartBackend}
              className="px-2 py-1 text-xs bg-blue-500 text-white rounded hover:bg-blue-600 transition"
            >
              重启后端
            </button>
          )}

          <button
            onClick={handleOpenLogs}
            className="px-2 py-1 text-xs bg-gray-500 text-white rounded hover:bg-gray-600 transition"
          >
            查看日志
          </button>
        </div>

        {/* 中间：更新进度 */}
        {updateProgress !== null && (
          <div className="flex items-center gap-2">
            <span className="text-gray-600 dark:text-gray-400">更新下载中...</span>
            <div className="w-32 bg-gray-200 dark:bg-gray-700 rounded-full h-2">
              <div
                className="bg-blue-500 h-2 rounded-full transition-all"
                style={{ width: `${updateProgress}%` }}
              />
            </div>
            <span className="text-gray-600 dark:text-gray-400 text-xs">
              {updateProgress}%
            </span>
          </div>
        )}

        {/* 右侧：版本信息 */}
        <div className="flex items-center gap-4">
          {appInfo && (
            <span className="text-gray-500 dark:text-gray-400 text-xs">
              v{appInfo.version} {appInfo.isDev ? '(开发版)' : ''}
            </span>
          )}

          <button
            onClick={handleCheckUpdate}
            disabled={checkingUpdate}
            className="px-3 py-1 text-xs bg-green-500 text-white rounded hover:bg-green-600 transition disabled:opacity-50"
          >
            {checkingUpdate ? '检查中...' : '检查更新'}
          </button>
        </div>
      </div>
    </div>
  );
}
