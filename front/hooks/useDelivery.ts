'use client'

import { useState, useEffect, useCallback } from 'react'
import { isElectron, getElectronAPI } from '@/lib/electron'
import { API_PATHS } from '@/lib/api-config'

export interface ProgressMessage {
  platform: string
  type: 'info' | 'warning' | 'error' | 'success'
  message: string
  data?: any
}

export interface DeliveryStatus {
  platform: string
  isRunning: boolean
  progress: number
  total: number
  message: string
}

export interface UseDeliveryOptions {
  platform: 'boss' | 'liepin' | 'job51' | 'zhilian'
}

export function useDelivery(options: UseDeliveryOptions) {
  const { platform } = options
  const [isDelivering, setIsDelivering] = useState(false)
  const [progress, setProgress] = useState<ProgressMessage[]>([])
  const [lastMessage, setLastMessage] = useState<ProgressMessage | null>(null)

  // 添加进度消息
  const addProgress = useCallback((msg: ProgressMessage) => {
    setProgress(prev => [...prev.slice(-50), msg])
    setLastMessage(msg)
  }, [])

  // 开始投递
  const startDelivery = useCallback(async () => {
    if (isElectron()) {
      // Electron 模式
      const api = getElectronAPI()
      if (!api?.delivery) {
        addProgress({ platform, type: 'error', message: 'Electron API 不可用' })
        return false
      }

      try {
        const result = await api.delivery.start(platform)
        if (result.success) {
          setIsDelivering(true)
          addProgress({ platform, type: 'info', message: '投递任务已启动' })
          return true
        } else {
          addProgress({ platform, type: 'error', message: result.message || '启动失败' })
          return false
        }
      } catch (error: any) {
        addProgress({ platform, type: 'error', message: error.message })
        return false
      }
    } else {
      // 后端 API 模式（兼容当前架构）
      try {
        const response = await fetch(API_PATHS[platform].start, {
          method: 'POST',
        })
        const data = await response.json()

        if (data.success) {
          setIsDelivering(true)
          addProgress({ platform, type: 'info', message: '投递任务已启动' })
          return true
        } else {
          addProgress({ platform, type: 'error', message: data.message || '启动失败' })
          return false
        }
      } catch (error: any) {
        addProgress({ platform, type: 'error', message: error.message })
        return false
      }
    }
  }, [platform, isDelivering, addProgress])

  // 停止投递
  const stopDelivery = useCallback(async () => {
    if (isElectron()) {
      // Electron 模式
      const api = getElectronAPI()
      if (!api?.delivery) {
        addProgress({ platform, type: 'error', message: 'Electron API 不可用' })
        return false
      }

      try {
        const result = await api.delivery.stop(platform)
        if (result.success) {
          setIsDelivering(false)
          addProgress({ platform, type: 'warning', message: '投递任务已停止' })
          return true
        } else {
          addProgress({ platform, type: 'error', message: result.message || '停止失败' })
          return false
        }
      } catch (error: any) {
        addProgress({ platform, type: 'error', message: error.message })
        return false
      }
    } else {
      // 后端 API 模式（兼容当前架构）
      try {
        const response = await fetch(API_PATHS[platform].stop, {
          method: 'POST',
        })
        const data = await response.json()

        if (data.success) {
          setIsDelivering(false)
          addProgress({ platform, type: 'warning', message: '投递任务已停止' })
          return true
        } else {
          addProgress({ platform, type: 'error', message: data.message || '停止失败' })
          return false
        }
      } catch (error: any) {
        addProgress({ platform, type: 'error', message: error.message })
        return false
      }
    }
  }, [platform, addProgress])

  // 获取投递状态
  const getStatus = useCallback(async (): Promise<DeliveryStatus | null> => {
    if (isElectron()) {
      const api = getElectronAPI()
      if (!api?.delivery) return null

      try {
        const status = await api.delivery.getStatus()
        return status[platform] || null
      } catch (error) {
        return null
      }
    } else {
      // 后端 API 模式暂不支持
      return null
    }
  }, [platform])

  // 订阅进度事件（Electron 模式）
  useEffect(() => {
    if (!isElectron()) return

    const api = getElectronAPI()
    if (!api?.delivery?.onProgress) return

    const unsubscribe = api.delivery.onProgress((message: ProgressMessage) => {
      if (message.platform === platform) {
        addProgress(message)
        if (message.type === 'success' || message.type === 'error') {
          setIsDelivering(false)
        }
      }
    })

    return () => {
      unsubscribe()
    }
  }, [platform, addProgress])

  // 清除进度
  const clearProgress = useCallback(() => {
    setProgress([])
    setLastMessage(null)
  }, [])

  return {
    isDelivering,
    progress,
    lastMessage,
    startDelivery,
    stopDelivery,
    getStatus,
    clearProgress,
  }
}
