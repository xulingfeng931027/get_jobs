'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getToken } from '@/lib/auth-api'

export default function HomeRedirect() {
  const router = useRouter()

  useEffect(() => {
    // 检查是否已登录
    const token = getToken()
    if (!token) {
      // 未登录,跳转到登录页
      router.replace('/auth/login')
    } else {
      // 已登录,跳转到环境配置页
      router.replace('/env-config')
    }
  }, [router])

  // 显示加载状态
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
      <div className="text-center">
        <div className="text-6xl mb-4 animate-pulse">🍀</div>
        <p className="text-gray-600 dark:text-gray-400 text-lg">正在加载...</p>
      </div>
    </div>
  )
}