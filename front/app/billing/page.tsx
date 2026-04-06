'use client'

import { useState, useEffect } from 'react'
import { getBalance, activateRechargeCode } from '@/lib/auth-api'
import { useUser } from '@/lib/auth-context'

export default function BillingPage() {
  const { user, billing, refreshUser } = useUser()
  const [rechargeCode, setRechargeCode] = useState('')
  const [message, setMessage] = useState('')
  const [msgType, setMsgType] = useState<'success' | 'error'>('success')
  const [isRecharging, setIsRecharging] = useState(false)
  const [isRefreshing, setIsRefreshing] = useState(false)

  // 刷新余额信息
  const handleRefresh = async () => {
    setIsRefreshing(true)
    await refreshUser()
    setIsRefreshing(false)
  }

  // 激活充值码
  const handleRecharge = async (e: React.FormEvent) => {
    e.preventDefault()
    setMessage('')
    setIsRecharging(true)

    try {
      const result = await activateRechargeCode(rechargeCode)
      if (result.success) {
        setMsgType('success')
        setMessage(`充值成功！${result.type === 'count' ? `增加 ${result.applicationCount} 次投递次数` : `订阅 ${result.subscriptionDays} 天`}`)
        setRechargeCode('')
        await refreshUser()
      } else {
        setMsgType('error')
        setMessage(result.message || '充值失败')
      }
    } catch (err: any) {
      setMsgType('error')
      setMessage(err.message || '充值失败，请检查充值码')
    } finally {
      setIsRecharging(false)
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
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">账户与充值</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            {user ? `欢迎回来，${user.username}` : '加载中...'}
          </p>
        </div>
        <button
          onClick={handleRefresh}
          disabled={isRefreshing}
          className="px-4 py-2 text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-all disabled:opacity-50"
        >
          {isRefreshing ? '刷新中...' : '刷新'}
        </button>
      </div>

      {/* 余额卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {/* 投递次数 */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-200 dark:border-gray-700">
          <div className="text-sm text-gray-500 dark:text-gray-400 mb-1">剩余投递次数</div>
          <div className="text-3xl font-bold text-blue-600">{billing?.applicationCount ?? 0}</div>
          {billing?.hasSubscription && (
            <div className="text-xs text-green-600 mt-2">订阅有效期内不限次数</div>
          )}
        </div>

        {/* 订阅状态 */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-200 dark:border-gray-700">
          <div className="text-sm text-gray-500 dark:text-gray-400 mb-1">订阅状态</div>
          {billing?.hasSubscription ? (
            <>
              <div className="text-3xl font-bold text-green-600">有效</div>
              <div className="text-sm text-gray-600 dark:text-gray-400 mt-2">
                {daysLeft !== null ? `剩余 ${daysLeft} 天` : '计算中...'}
              </div>
            </>
          ) : (
            <>
              <div className="text-3xl font-bold text-gray-400">未订阅</div>
              <div className="text-xs text-gray-500 mt-2">订阅后不限投递次数</div>
            </>
          )}
        </div>

        {/* AI 匹配 */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-200 dark:border-gray-700">
          <div className="text-sm text-gray-500 dark:text-gray-400 mb-1">AI 匹配次数</div>
          <div className="text-3xl font-bold text-purple-600">{billing?.aiMatchCount ?? 0}</div>
        </div>

        {/* AI 打招呼 */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-200 dark:border-gray-700">
          <div className="text-sm text-gray-500 dark:text-gray-400 mb-1">AI 打招呼次数</div>
          <div className="text-3xl font-bold text-indigo-600">{billing?.aiGreetCount ?? 0}</div>
        </div>
      </div>

      {/* 统计信息 */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-200 dark:border-gray-700 mb-8">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">使用统计</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
          <div>
            <div className="text-gray-500 dark:text-gray-400">累计充值</div>
            <div className="text-lg font-semibold text-gray-900 dark:text-white">{billing?.totalRecharge ?? 0} 次</div>
          </div>
          <div>
            <div className="text-gray-500 dark:text-gray-400">累计消费</div>
            <div className="text-lg font-semibold text-gray-900 dark:text-white">{billing?.totalConsumption ?? 0} 次</div>
          </div>
          <div>
            <div className="text-gray-500 dark:text-gray-400">数据报告</div>
            <div className="text-lg font-semibold text-gray-900 dark:text-white">{billing?.reportCount ?? 0} 次</div>
          </div>
          <div>
            <div className="text-gray-500 dark:text-gray-400">账户状态</div>
            <div className="text-lg font-semibold text-green-600">正常</div>
          </div>
        </div>
      </div>

      {/* 充值表单 */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-200 dark:border-gray-700">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">充值</h2>
        
        {message && (
          <div className={`mb-4 p-4 rounded-lg text-sm ${
            msgType === 'success' 
              ? 'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800' 
              : 'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-800'
          }`}>
            {message}
          </div>
        )}

        <form onSubmit={handleRecharge} className="flex gap-4">
          <input
            type="text"
            value={rechargeCode}
            onChange={(e) => setRechargeCode(e.target.value.toUpperCase())}
            placeholder="请输入充值码 (如: GJ-CNT-XXXX...)"
            className="flex-1 px-4 py-3 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all uppercase"
          />
          <button
            type="submit"
            disabled={isRecharging || !rechargeCode}
            className="px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-medium rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl whitespace-nowrap"
          >
            {isRecharging ? '充值中...' : '立即充值'}
          </button>
        </form>

        <div className="mt-4 text-xs text-gray-500 dark:text-gray-400">
          <p>充值码说明：</p>
          <ul className="list-disc list-inside mt-1 space-y-1">
            <li><code className="bg-gray-100 dark:bg-gray-700 px-1 rounded">GJ-CNT-</code> 开头的为次数码，增加投递次数</li>
            <li><code className="bg-gray-100 dark:bg-gray-700 px-1 rounded">GJ-SUB-</code> 开头的为订阅码，激活后不限投递次数</li>
          </ul>
        </div>
      </div>
    </div>
  )
}
