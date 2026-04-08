'use client'

import Link from 'next/link'
import {usePathname} from 'next/navigation'
import {useEffect, useState} from 'react'
import {BiBrain, BiBriefcase, BiCog, BiEnvelope, BiMoon, BiSearch, BiSun, BiTask, BiUserCircle, BiLogIn, BiDollar, BiUser} from 'react-icons/bi'
import {motion} from 'framer-motion'
import {useTheme} from 'next-themes'
import {API_PATHS} from '@/lib/api-config'
import { useUser } from '@/lib/auth-context'

export default function Sidebar() {
  const pathname = usePathname()
  const { theme, setTheme } = useTheme()
  const { user, billing, isAuthenticated } = useUser()
  const [mounted, setMounted] = useState(false)

  // 健康检查状态：up / degraded / down / unknown
  const [health, setHealth] = useState<'up' | 'degraded' | 'down' | 'unknown'>('unknown')
  const [checking, setChecking] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | null = null

    const check = async () => {
      if (checking) return
      setChecking(true)
      const controller = new AbortController()
      const timeout = setTimeout(() => controller.abort(), 3000)
      try {
        // 先尝试自定义健康接口
        let res = await fetch(API_PATHS.health, { signal: controller.signal })
        if (res.status === 404) {
          // 回退到 Spring Boot Actuator
          res = await fetch(API_PATHS.actuatorHealth, { signal: controller.signal })
        }
        if (!res.ok) throw new Error(`status ${res.status}`)
        const data = await res.json()
        const statusRaw = (data.status || data.state || '').toString().toUpperCase()
        if (statusRaw === 'UP' || statusRaw === 'HEALTHY') {
          setHealth('up')
        } else if (statusRaw === 'DEGRADED' || statusRaw === 'WARN') {
          setHealth('degraded')
        } else {
          setHealth('down')
        }
      } catch (e) {
        setHealth('unknown')
      } finally {
        clearTimeout(timeout)
        setChecking(false)
      }
    }

    // 首次检查 + 轮询
    check()
    interval = setInterval(check, 30000)
    return () => {
      if (interval) clearInterval(interval)
    }
  }, [])

  const envGroup = [
    { href: '/env-config', icon: BiEnvelope, label: '环境配置', color: 'text-cyan-600 dark:text-cyan-400' },
    { href: '/ai-config', icon: BiBrain, label: 'AI配置', color: 'text-purple-600 dark:text-purple-400' },
    { href: '/common-config', icon: BiCog, label: '公共配置', color: 'text-amber-600 dark:text-amber-400' },
  ]

  const platformGroup = [
    // Boss直聘暂时屏蔽
    // { href: '/boss', icon: BiBriefcase, label: 'Boss直聘', color: 'text-indigo-600 dark:text-indigo-400' },
    { href: '/liepin', icon: BiSearch, label: '猎聘', color: 'text-purple-600 dark:text-purple-400' },
    { href: '/51job', icon: BiTask, label: '51job', color: 'text-blue-600 dark:text-blue-400' },
    { href: '/zhilian', icon: BiUserCircle, label: '智联招聘', color: 'text-cyan-600 dark:text-cyan-400' },
  ]

  return (
    <motion.div
      initial={{ x: -100, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
      className="fixed left-0 top-0 h-full w-64 bg-card dark:bg-card shadow-bento z-50 border-r border-border flex flex-col"
    >
      {/* 侧边栏头部 */}
      <motion.div
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.2, duration: 0.5 }}
        className="p-6 border-b border-border shrink-0"
      >
        <div className="flex items-center gap-3 mb-2">
          <span className="text-4xl leading-none">🔍</span>
          <div>
            <h1 className="text-xl font-bold text-foreground">Find Jobs</h1>
            <p className="text-muted-foreground text-sm">配置管理中心</p>
          </div>
        </div>

        {/* 状态指示器（动态健康检查） */}
        <div className="mt-4 flex items-center gap-2 text-muted-foreground text-sm">
          <div
            className={`w-2 h-2 rounded-full animate-pulse ${
              health === 'up'
                ? 'bg-green-400'
                : health === 'degraded'
                ? 'bg-yellow-400'
                : health === 'down'
                ? 'bg-red-500'
                : 'bg-gray-400'
            }`}
          ></div>
          <span className="text-muted-foreground">
            {health === 'up'
              ? '系统运行正常'
              : health === 'degraded'
              ? '服务降级'
              : health === 'down'
              ? '服务异常'
              : '未连接'}
          </span>
        </div>

        {/* 主题切换器 */}
        {mounted && (
          <motion.button
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.4, type: "spring", stiffness: 200 }}
            onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
            className="mt-4 w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-accent hover:bg-accent/80 text-foreground transition-all duration-300 shadow-bento-sm"
          >
            {theme === 'dark' ? (
              <>
                <BiSun className="text-lg" />
                <span className="text-sm">切换到浅色</span>
              </>
            ) : (
              <>
                <BiMoon className="text-lg" />
                <span className="text-sm">切换到深色</span>
              </>
            )}
          </motion.button>
        )}
      </motion.div>

      {/* 导航菜单 - flex-1 占满剩余空间 */}
      <nav className="flex-1 p-4 overflow-y-auto min-h-0">
        {/* 未登录时显示提示信息 */}
        {!isAuthenticated && (
          <div className="text-center py-8">
            <div className="text-4xl mb-3">🔒</div>
            <p className="text-muted-foreground text-sm mb-2">请先登录</p>
            <p className="text-muted-foreground/70 text-xs">登录后查看配置</p>
          </div>
        )}

        {/* 已登录时显示配置菜单 */}
        {isAuthenticated && (
          <>
            {/* 环境配置分组 */}
            <div>
              <div className="px-4 py-2 text-muted-foreground text-xs uppercase tracking-wide">环境配置</div>
              <div className="space-y-2">
                {envGroup.map((item, index) => {
                  const Icon = item.icon
                  const isActive = pathname === item.href
                  return (
                    <motion.div
                      key={item.href}
                      initial={{ x: -20, opacity: 0 }}
                      animate={{ x: 0, opacity: 1 }}
                      transition={{ delay: 0.1 * index + 0.3, duration: 0.3 }}
                    >
                      <Link
                        href={item.href}
                        className={`
                          group flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300
                          ${isActive
                            ? 'bg-primary/10 text-primary font-semibold shadow-bento-sm rounded-xl'
                            : 'text-foreground/70 hover:bg-accent hover:translate-x-1'
                          }
                        `}
                      >
                        <Icon className={`text-xl ${isActive ? 'text-primary' : item.color} group-hover:scale-110 transition-transform`} />
                        <span className="font-medium">{item.label}</span>
                        {isActive && (
                          <div className="ml-auto">
                            <div className="w-2 h-2 bg-primary rounded-full animate-pulse"></div>
                          </div>
                        )}
                      </Link>
                    </motion.div>
                  )
                })}
              </div>
            </div>

            {/* 平台配置分组 */}
            <div className="mt-6">
              <div className="px-4 py-2 text-muted-foreground text-xs uppercase tracking-wide">平台配置</div>
              <div className="space-y-2">
                {platformGroup.map((item, index) => {
                  const Icon = item.icon
                  const isActive = pathname === item.href
                  return (
                    <motion.div
                      key={item.href}
                      initial={{ x: -20, opacity: 0 }}
                      animate={{ x: 0, opacity: 1 }}
                      transition={{ delay: 0.1 * index + 0.5, duration: 0.3 }}
                    >
                      <Link
                        href={item.href}
                        className={`
                          group flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300
                          ${isActive
                            ? 'bg-primary/10 text-primary font-semibold shadow-bento-sm rounded-xl'
                            : 'text-foreground/70 hover:bg-accent hover:translate-x-1'
                          }
                        `}
                      >
                        <Icon className={`text-xl ${isActive ? 'text-primary' : item.color} group-hover:scale-110 transition-transform`} />
                        <span className="font-medium">{item.label}</span>
                        {isActive && (
                          <div className="ml-auto">
                            <div className="w-2 h-2 bg-primary rounded-full animate-pulse"></div>
                          </div>
                        )}
                      </Link>
                    </motion.div>
                  )
                })}
              </div>
            </div>
          </>
        )}
      </nav>

      {/* 底部信息 - shrink-0 不压缩 */}
      <motion.div
        initial={{ y: 20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.8, duration: 0.5 }}
        className="shrink-0 p-4 border-t border-border"
      >
        {/* 用户状态 */}
        {isAuthenticated && user ? (
          <div className="mb-3">
            {/* 订阅有效期提示（优先级高于次数展示） */}
            {billing?.hasSubscription && billing?.subscriptionEndDate ? (
              <div className="mb-2 text-xs bg-gradient-to-r from-green-500/10 to-emerald-500/10 dark:from-green-500/20 dark:to-emerald-500/20 border border-green-500/20 px-3 py-2 rounded-lg">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-green-600 dark:text-green-400 font-medium">VIP 会员</span>
                  <span className="text-green-600 dark:text-green-400">
                    剩余 {Math.max(0, Math.ceil((new Date(billing.subscriptionEndDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24)))} 天
                  </span>
                </div>
                <div className="text-green-600/70 dark:text-green-400/70">
                  到期: {new Date(billing.subscriptionEndDate).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })}
                </div>
              </div>
            ) : (
              <div className="mb-2 flex items-center justify-between text-xs text-muted-foreground px-3 py-1.5 rounded-lg bg-muted/50">
                <span>剩余投递</span>
                <span className="font-medium text-foreground">{billing?.applicationCount ?? 0} 次</span>
              </div>
            )}
            {/* 用户管理 */}
            <Link
              href="/user/profile"
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-all duration-300 mb-2 ${
                pathname === '/user/profile'
                  ? 'bg-primary/10 text-primary'
                  : 'text-foreground/70 hover:bg-accent'
              }`}
            >
              <BiUser className="text-lg" />
              <span>用户管理</span>
            </Link>
            {/* 账户充值 */}
            <Link
              href="/billing"
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-all duration-300 ${
                pathname === '/billing'
                  ? 'bg-primary/10 text-primary'
                  : 'text-foreground/70 hover:bg-accent'
              }`}
            >
              <BiDollar className="text-lg" />
              <span>账户充值</span>
            </Link>
          </div>
        ) : (
          <div className="mb-3">
            <Link
              href="/auth/login"
              className="flex items-center gap-2 px-3 py-2 rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 text-sm transition-all duration-300"
            >
              <BiLogIn className="text-lg" />
              <span>登录</span>
            </Link>
          </div>
        )}

        {/* 版本信息 */}
        <div className="text-center">
          <p className="text-muted-foreground text-xs">v1.1.0</p>
        </div>
      </motion.div>
    </motion.div>
  )
}
