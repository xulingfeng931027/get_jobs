'use client'

import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useUser } from '@/lib/auth-context'
import { getToken } from '@/lib/auth-api'

/**
 * 认证页面布局(登录/注册)
 * 已登录用户访问认证页面时会自动跳转到首页
 */
export default function AuthLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const { isAuthenticated, isLoading } = useUser()
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    // 如果还在加载认证状态,不做任何操作
    if (isLoading) {
      return
    }

    // 如果已登录,重定向到首页或原始页面
    if (isAuthenticated && getToken()) {
      const redirectUrl = new URLSearchParams(window.location.search).get('redirect')
      const targetUrl = redirectUrl ? decodeURIComponent(redirectUrl) : '/env-config'
      router.replace(targetUrl)
    }
  }, [isAuthenticated, isLoading, router])

  // 加载中显示加载状态
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
        <div className="text-center">
          <div className="text-6xl mb-4 animate-pulse">🍀</div>
          <p className="text-gray-600 dark:text-gray-400 text-lg">正在加载...</p>
        </div>
      </div>
    )
  }

  // 如果已登录,显示空白(即将跳转)
  if (isAuthenticated && getToken()) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
        <div className="text-center">
          <div className="text-6xl mb-4">✓</div>
          <p className="text-gray-600 dark:text-gray-400 text-lg">您已登录</p>
          <p className="text-gray-500 dark:text-gray-500 text-sm mt-2">正在跳转到首页...</p>
        </div>
      </div>
    )
  }

  // 未登录,显示认证页面
  return <>{children}</>
}
