'use client'

import { useState, useCallback } from 'react'
import { isElectron, getElectronAPI } from '@/lib/electron'
import { authFetch } from '@/lib/auth-fetch'
import { API_PATHS } from '@/lib/api-config'

export interface CloudConfig {
  boss: any
  liepin: any
  zhilian: any
  job51: any
  aiConfig: any
  lastSyncTime: string | null
}

export function useConfigSync() {
  const [config, setConfig] = useState<CloudConfig | null>(null)
  const [isSyncing, setIsSyncing] = useState(false)
  const [lastSyncTime, setLastSyncTime] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  // 从云端同步配置
  const syncFromCloud = useCallback(async () => {
    setIsSyncing(true)
    setError(null)

    try {
      if (isElectron()) {
        // Electron 模式：通过客户端同步配置
        const api = getElectronAPI()
        if (!api?.config) {
          throw new Error('Electron API 不可用')
        }

        const result = await api.config.sync()
        if (result.success) {
          setConfig(result.config)
          setLastSyncTime(result.config?.lastSyncTime || new Date().toISOString())
          return result.config
        } else {
          throw new Error(result.message || '配置同步失败')
        }
      } else {
        // 后端 API 模式
        const response = await authFetch(API_PATHS.config)
        const data = await response.json()

        if (data.success) {
          const cloudConfig: CloudConfig = {
            boss: data.data,
            liepin: data.data,
            zhilian: data.data,
            job51: data.data,
            aiConfig: data.data,
            lastSyncTime: new Date().toISOString(),
          }
          setConfig(cloudConfig)
          setLastSyncTime(cloudConfig.lastSyncTime)
          return cloudConfig
        } else {
          throw new Error(data.message || '获取配置失败')
        }
      }
    } catch (err: any) {
      setError(err.message)
      throw err
    } finally {
      setIsSyncing(false)
    }
  }, [])

  // 获取本地缓存的配置
  const getLocalConfig = useCallback(async () => {
    if (isElectron()) {
      const api = getElectronAPI()
      if (!api?.config) return null

      try {
        const localConfig = await api.config.get()
        return localConfig
      } catch (error) {
        return null
      }
    } else {
      return null
    }
  }, [])

  return {
    config,
    isSyncing,
    lastSyncTime,
    error,
    syncFromCloud,
    getLocalConfig,
  }
}
