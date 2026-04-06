'use client'

import { useState, useCallback } from 'react'
import { isElectron, getElectronAPI } from '@/lib/electron'

export interface BillingInfo {
  applicationCount: number
  hasSubscription: boolean
  subscriptionEndDate?: string
  aiMatchCount: number
  aiGreetCount: number
  reportCount: number
  totalRecharge: number
  totalConsumption: number
}

export interface PreCheckResult {
  success: boolean
  allowed: boolean
  remainingCount: number
  hasSubscription: boolean
  subscriptionEndDate?: string
  reason?: string
  billingInfo?: BillingInfo
}

export function useBilling() {
  const [billingInfo, setBillingInfo] = useState<BillingInfo | null>(null)
  const [isChecking, setIsChecking] = useState(false)

  // 投递预检查
  const preCheck = useCallback(async (platform: string, expectedCount: number = 1): Promise<PreCheckResult> => {
    setIsChecking(true)

    try {
      if (isElectron()) {
        // Electron 模式：通过客户端调用云端 API
        const api = getElectronAPI()
        if (!api?.billing) {
          return {
            success: false,
            allowed: false,
            remainingCount: 0,
            hasSubscription: false,
            reason: 'Electron API 不可用',
          }
        }

        const result = await api.billing.preCheck(platform, expectedCount)
        if (result.success) {
          setBillingInfo(result.billingInfo || null)
        }
        return result
      } else {
        // 后端 API 模式（暂不支持）
        return {
          success: false,
          allowed: false,
          remainingCount: 0,
          hasSubscription: false,
          reason: '当前模式不支持计费预检查',
        }
      }
    } catch (error: any) {
      return {
        success: false,
        allowed: false,
        remainingCount: 0,
        hasSubscription: false,
        reason: error.message,
      }
    } finally {
      setIsChecking(false)
    }
  }, [])

  // 获取设备ID
  const getDeviceId = useCallback(async (): Promise<string | null> => {
    if (isElectron()) {
      const api = getElectronAPI()
      if (!api?.device) return null

      try {
        return await api.device.getId()
      } catch (error) {
        return null
      }
    }
    return null
  }, [])

  return {
    billingInfo,
    isChecking,
    preCheck,
    getDeviceId,
  }
}
