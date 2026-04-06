'use client'

import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useUser } from '@/lib/auth-context'
import { getToken } from '@/lib/auth-api'

/**
 * 认证拦截器组件
 * 用于保护需要登录才能访问的页面
 * 未登录用户将被重定向到登录页
 */
export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useUser()
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    // 如果还在加载认证状态,不做任何操作
    if (isLoading) {
      return
    }

    // 检查是否已登录
    const token = getToken()
    if (!isAuthenticated || !token) {
      // 未登录,重定向到登录页,并记录原始路径用于登录后返回
      const redirectPath = encodeURIComponent(pathname)
      router.replace(`/auth/login?redirect=${redirectPath}`)
    }
  }, [isAuthenticated, isLoading, pathname, router])

  // 加载中或已认证时显示内容
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
        <div className="text-center">
          <div className="text-6xl mb-4 animate-pulse">🍀</div>
          <p className="text-gray-600 dark:text-gray-400 text-lg">正在验证登录状态...</p>
        </div>
      </div>
    )
  }

  // 如果未认证,显示空白(即将跳转)
  if (!isAuthenticated || !getToken()) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
        <div className="text-center">
          <div className="text-6xl mb-4">🔒</div>
          <p className="text-gray-600 dark:text-gray-400 text-lg">请先登录</p>
          <p className="text-gray-500 dark:text-gray-500 text-sm mt-2">正在跳转到登录页...</p>
        </div>
      </div>
    )
  }

  // 已认证,显示子组件
  return <>{children}</>
}
