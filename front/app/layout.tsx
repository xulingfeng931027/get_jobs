'use client'

import { usePathname } from 'next/navigation'
import { useEffect, useState } from 'react'
import { useUser } from '@/lib/auth-context'
import { useRouter } from 'next/navigation'
import './globals.css'
import Sidebar from './components/Sidebar'
import ContentArea from './components/ContentArea'
import {ThemeProvider} from 'next-themes'
import dynamic from 'next/dynamic'
import { UserProvider } from '@/lib/auth-context'
import { ToastProvider } from '@/components/Toast'

// 动态导入 Electron 状态栏（仅客户端）
const ElectronStatusBar = dynamic(
  () => import('./components/ElectronStatusBar'),
  { ssr: false }
)

/**
 * 加载中组件
 */
function LoadingScreen() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
      <div className="text-center">
        <div className="text-6xl mb-4 animate-pulse">🍀</div>
        <p className="text-gray-600 dark:text-gray-400 text-lg">正在验证登录状态...</p>
      </div>
    </div>
  )
}

/**
 * 未登录重定向组件
 */
function RedirectToLogin({ pathname }: { pathname: string }) {
  const router = useRouter()

  useEffect(() => {
    const redirectPath = encodeURIComponent(pathname)
    router.replace(`/auth/login?redirect=${redirectPath}`)
  }, [pathname, router])

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

/**
 * 内部布局组件 - 需要在 UserProvider 内渲染
 */
function InnerLayout({ children }: { children: React.ReactNode }) {
  const { isLoading, isAuthenticated } = useUser()
  const pathname = usePathname()
  const [isAuthChecked, setIsAuthChecked] = useState(false)

  // 认证检查完成标志
  useEffect(() => {
    if (!isLoading) {
      setIsAuthChecked(true)
    }
  }, [isLoading])

  // 判断是否是 auth 相关路由（不需要认证的路由）
  const isAuthRoute = pathname?.startsWith('/auth') || pathname === '/'

  // 如果是 auth 路由或已登录，直接显示内容
  // 如果是受保护的路由但未登录，显示重定向
  const showAuthGuard = !isAuthRoute

  return (
    <>
      {isAuthRoute ? (
        /* auth 路由直接显示 children（使用 auth/layout.tsx 的独立布局） */
        <>{children}</>
      ) : showAuthGuard ? (
        <>
          {/* 受保护的路由需要认证 */}
          {!isLoading && !isAuthenticated && (
            <RedirectToLogin pathname={pathname} />
          )}
          {isLoading && <LoadingScreen />}
          {!isLoading && isAuthenticated && (
            <div className="flex min-h-screen">
              <Sidebar />
              <ContentArea>
                {children}
              </ContentArea>
            </div>
          )}
        </>
      ) : null}
      {/* Electron 状态栏（仅在 Electron 环境中显示） */}
      <ElectronStatusBar />
    </>
  )
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="zh-CN" suppressHydrationWarning>
      <head>
        <title>Find Jobs - 配置管理中心</title>
        <meta name="description" content="配置管理中心，管理application.yaml和环境变量配置" />
        <link
          rel="icon"
          href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🔍</text></svg>"
          type="image/svg+xml"
        />
      </head>
      <body suppressHydrationWarning className="dark:bg-blacksection">
        <ThemeProvider
          attribute="class"
          defaultTheme="light"
          enableSystem={false}
        >
          <ToastProvider>
            <UserProvider>
              <InnerLayout>{children}</InnerLayout>
            </UserProvider>
          </ToastProvider>
        </ThemeProvider>
      </body>
    </html>
  )
}
