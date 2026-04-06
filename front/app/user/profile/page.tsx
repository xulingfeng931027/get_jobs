'use client'

import { useState } from 'react'
import { useUser } from '@/lib/auth-context'
import { updateProfile, changePassword } from '@/lib/auth-api'
import { useRouter } from 'next/navigation'
import { BiUser, BiMailSend, BiPhone, BiLock, BiLogOut, BiCheck, BiX } from 'react-icons/bi'

export default function UserProfilePage() {
  const { user, billing, refreshUser, logout } = useUser()
  const router = useRouter()

  // 用户信息编辑状态
  const [email, setEmail] = useState(user?.email || '')
  const [phone, setPhone] = useState(user?.phone || '')
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false)
  const [profileMessage, setProfileMessage] = useState('')
  const [profileMsgType, setProfileMsgType] = useState<'success' | 'error'>('success')

  // 修改密码状态
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [passwordMessage, setPasswordMessage] = useState('')
  const [passwordMsgType, setPasswordMsgType] = useState<'success' | 'error'>('success')

  // 显示密码状态
  const [showOldPassword, setShowOldPassword] = useState(false)
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  // 更新用户信息
  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    setProfileMessage('')
    setIsUpdatingProfile(true)

    try {
      const result = await updateProfile(email || undefined, phone || undefined)
      if (result.success) {
        setProfileMsgType('success')
        setProfileMessage('用户信息更新成功！')
        await refreshUser()
      } else {
        setProfileMsgType('error')
        setProfileMessage(result.message || '更新失败')
      }
    } catch (err: any) {
      setProfileMsgType('error')
      setProfileMessage(err.message || '更新失败')
    } finally {
      setIsUpdatingProfile(false)
    }
  }

  // 修改密码
  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    setPasswordMessage('')

    // 验证新密码
    if (newPassword !== confirmPassword) {
      setPasswordMsgType('error')
      setPasswordMessage('两次输入的新密码不一致')
      return
    }

    if (newPassword.length < 6) {
      setPasswordMsgType('error')
      setPasswordMessage('新密码长度不能少于6位')
      return
    }

    setIsChangingPassword(true)

    try {
      const result = await changePassword(oldPassword, newPassword)
      if (result.success) {
        setPasswordMsgType('success')
        setPasswordMessage('密码修改成功！请重新登录')
        setOldPassword('')
        setNewPassword('')
        setConfirmPassword('')
        // 3秒后自动退出登录
        setTimeout(() => {
          logout()
        }, 3000)
      } else {
        setPasswordMsgType('error')
        setPasswordMessage(result.message || '密码修改失败')
      }
    } catch (err: any) {
      setPasswordMsgType('error')
      setPasswordMessage(err.message || '密码修改失败')
    } finally {
      setIsChangingPassword(false)
    }
  }

  // 退出登录
  const handleLogout = () => {
    if (window.confirm('确定要退出登录吗？')) {
      logout()
    }
  }

  // 计算订阅剩余天数
  const getSubscriptionDaysLeft = () => {
    if (!billing?.hasSubscription || !billing.subscriptionEndDate) return null
    const endDate = new Date(billing.subscriptionEndDate)
    const now = new Date()
    const diff = Math.ceil((endDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
    return diff > 0 ? diff : 0
  }

  const daysLeft = getSubscriptionDaysLeft()

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-foreground">用户管理</h1>
          <p className="text-muted-foreground mt-1">管理您的个人信息和账户设置</p>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg transition-all"
        >
          <BiLogOut className="text-lg" />
          <span>退出登录</span>
        </button>
      </div>

      {/* 用户基本信息卡片 */}
      <div className="bg-card dark:bg-card rounded-xl shadow-bento p-6 border border-border mb-8">
        <h2 className="text-lg font-semibold text-foreground mb-6 flex items-center gap-2">
          <BiUser className="text-xl text-primary" />
          <span>基本信息</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div>
            <label className="text-sm text-muted-foreground mb-1 block">用户名</label>
            <div className="px-4 py-3 bg-accent rounded-lg text-foreground font-medium">
              {user?.username || '-'}
            </div>
          </div>
          <div>
            <label className="text-sm text-muted-foreground mb-1 block">用户ID</label>
            <div className="px-4 py-3 bg-accent rounded-lg text-foreground">
              {user?.userId || '-'}
            </div>
          </div>
        </div>

        {/* 账户统计 */}
        {billing && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-6 border-t border-border">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">{billing.applicationCount}</div>
              <div className="text-xs text-muted-foreground mt-1">剩余投递次数</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">
                {billing.hasSubscription ? (daysLeft !== null ? `${daysLeft}天` : '有效') : '未订阅'}
              </div>
              <div className="text-xs text-muted-foreground mt-1">订阅状态</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">{billing.aiMatchCount}</div>
              <div className="text-xs text-muted-foreground mt-1">AI匹配次数</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-indigo-600">{billing.aiGreetCount}</div>
              <div className="text-xs text-muted-foreground mt-1">AI打招呼次数</div>
            </div>
          </div>
        )}
      </div>

      {/* 编辑用户信息 */}
      <div className="bg-card dark:bg-card rounded-xl shadow-bento p-6 border border-border mb-8">
        <h2 className="text-lg font-semibold text-foreground mb-6 flex items-center gap-2">
          <BiUser className="text-xl text-primary" />
          <span>编辑个人信息</span>
        </h2>

        {profileMessage && (
          <div className={`mb-4 p-4 rounded-lg text-sm flex items-center gap-2 ${
            profileMsgType === 'success' 
              ? 'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800' 
              : 'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-800'
          }`}>
            {profileMsgType === 'success' ? <BiCheck className="text-lg" /> : <BiX className="text-lg" />}
            <span>{profileMessage}</span>
          </div>
        )}

        <form onSubmit={handleUpdateProfile} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-sm text-muted-foreground mb-1 block flex items-center gap-2">
                <BiMailSend className="text-base" />
                <span>邮箱</span>
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="请输入邮箱地址"
                className="w-full px-4 py-3 rounded-lg border border-border bg-background text-foreground focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
              />
            </div>
            <div>
              <label className="text-sm text-muted-foreground mb-1 block flex items-center gap-2">
                <BiPhone className="text-base" />
                <span>手机号</span>
              </label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="请输入手机号"
                className="w-full px-4 py-3 rounded-lg border border-border bg-background text-foreground focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
              />
            </div>
          </div>

          <div className="flex justify-end pt-4">
            <button
              type="submit"
              disabled={isUpdatingProfile}
              className="px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-medium rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl"
            >
              {isUpdatingProfile ? '保存中...' : '保存修改'}
            </button>
          </div>
        </form>
      </div>

      {/* 修改密码 */}
      <div className="bg-card dark:bg-card rounded-xl shadow-bento p-6 border border-border mb-8">
        <h2 className="text-lg font-semibold text-foreground mb-6 flex items-center gap-2">
          <BiLock className="text-xl text-primary" />
          <span>修改密码</span>
        </h2>

        {passwordMessage && (
          <div className={`mb-4 p-4 rounded-lg text-sm flex items-center gap-2 ${
            passwordMsgType === 'success' 
              ? 'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800' 
              : 'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-800'
          }`}>
            {passwordMsgType === 'success' ? <BiCheck className="text-lg" /> : <BiX className="text-lg" />}
            <span>{passwordMessage}</span>
          </div>
        )}

        <form onSubmit={handleChangePassword} className="space-y-4">
          <div>
            <label className="text-sm text-muted-foreground mb-1 block">当前密码</label>
            <div className="relative">
              <input
                type={showOldPassword ? 'text' : 'password'}
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                placeholder="请输入当前密码"
                className="w-full px-4 py-3 pr-12 rounded-lg border border-border bg-background text-foreground focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
                required
              />
              <button
                type="button"
                onClick={() => setShowOldPassword(!showOldPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              >
                {showOldPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <div>
            <label className="text-sm text-muted-foreground mb-1 block">新密码</label>
            <div className="relative">
              <input
                type={showNewPassword ? 'text' : 'password'}
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="请输入新密码（至少6位）"
                className="w-full px-4 py-3 pr-12 rounded-lg border border-border bg-background text-foreground focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
                required
                minLength={6}
              />
              <button
                type="button"
                onClick={() => setShowNewPassword(!showNewPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              >
                {showNewPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <div>
            <label className="text-sm text-muted-foreground mb-1 block">确认新密码</label>
            <div className="relative">
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="请再次输入新密码"
                className="w-full px-4 py-3 pr-12 rounded-lg border border-border bg-background text-foreground focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
                required
                minLength={6}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              >
                {showConfirmPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <div className="flex justify-end pt-4">
            <button
              type="submit"
              disabled={isChangingPassword || !oldPassword || !newPassword || !confirmPassword}
              className="px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-medium rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl"
            >
              {isChangingPassword ? '修改中...' : '修改密码'}
            </button>
          </div>
        </form>
      </div>

      {/* 账户操作 */}
      <div className="bg-card dark:bg-card rounded-xl shadow-bento p-6 border border-border">
        <h2 className="text-lg font-semibold text-foreground mb-6 flex items-center gap-2">
          <BiLogOut className="text-xl text-red-600 dark:text-red-400" />
          <span>账户操作</span>
        </h2>

        <div className="flex flex-col sm:flex-row gap-4">
          <button
            onClick={() => router.push('/billing')}
            className="flex-1 px-6 py-3 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white font-medium rounded-lg transition-all duration-200 shadow-lg hover:shadow-xl flex items-center justify-center gap-2"
          >
            <span>💳</span>
            <span>账户充值</span>
          </button>
          <button
            onClick={handleLogout}
            className="flex-1 px-6 py-3 bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-700 hover:to-rose-700 text-white font-medium rounded-lg transition-all duration-200 shadow-lg hover:shadow-xl flex items-center justify-center gap-2"
          >
            <BiLogOut className="text-lg" />
            <span>退出登录</span>
          </button>
        </div>
      </div>
    </div>
  )
}
