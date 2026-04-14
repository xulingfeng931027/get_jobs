'use client'

import {useEffect, useState} from 'react'
import {BiCodeAlt, BiInfoCircle, BiLinkExternal, BiSave} from 'react-icons/bi'
import {Button} from '@/components/ui/button'
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import PageHeader from '@/app/components/PageHeader'
import { authFetch } from '@/lib/auth-fetch'
import {API_PATHS} from '@/lib/api-config'
import {useToast} from '@/components/Toast'

export default function EnvConfig() {
  const { showToast } = useToast();
  const [envConfig, setEnvConfig] = useState({
    hookUrl: '',
    botIsSend: 0,
    dingtalkHookUrl: '',
    dingtalkIsSend: 0,
  })

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [showSaveDialog, setShowSaveDialog] = useState(false)
  const [saveResult, setSaveResult] = useState<{ success: boolean; message: string } | null>(null)

  // 从数据库加载配置
  const fetchConfig = async () => {
    try {
      setLoading(true)
      const response = await authFetch(API_PATHS.config, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        throw new Error('获取配置失败')
      }

      const result = await response.json()

      if (result.success && result.data) {
        setEnvConfig({
          hookUrl: result.data.HOOK_URL || '',
          botIsSend: (() => {
            const raw = result.data.BOT_IS_SEND
            const val = String(raw ?? '').trim().toLowerCase()
            return val === '1' || val === 'true' ? 1 : 0
          })(),
          dingtalkHookUrl: result.data.DINGTALK_HOOK_URL || '',
          dingtalkIsSend: (() => {
            const raw = result.data.DINGTALK_IS_SEND
            const val = String(raw ?? '').trim().toLowerCase()
            return val === '1' || val === 'true' ? 1 : 0
          })(),
        })
      }
    } catch (error) {
      console.error('获取配置失败:', error)
      let msg = '获取配置失败，请检查后端服务是否正常运行'
      if (response && !response.ok) {
        try {
          const errData = await response.clone().json()
          msg = errData.message || msg
        } catch {}
      } else if (error instanceof Error) {
        msg = error.message
      }
      showToast(msg, 'error')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchConfig()
  }, [])

  const handleSave = async (silent: boolean = false) => {
    try {
      setSaving(true)

      const configMap = {
        HOOK_URL: envConfig.hookUrl,
        BOT_IS_SEND: String(envConfig.botIsSend ?? 0),
        DINGTALK_HOOK_URL: envConfig.dingtalkHookUrl,
        DINGTALK_IS_SEND: String(envConfig.dingtalkIsSend ?? 0),
      }

      const response = await authFetch(API_PATHS.config, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(configMap),
      })

      if (!response.ok) {
        throw new Error('保存配置失败')
      }

      const result = await response.json()

      if (result.success) {
        if (!silent) {
          setSaveResult({ success: true, message: '保存成功' })
          setShowSaveDialog(true)
        }
      } else {
        throw new Error(result.message || '保存配置失败')
      }
    } catch (error) {
      console.error('保存配置失败:', error)
      let msg = '保存配置失败：网络或服务异常'
      if (response && !response.ok) {
        try {
          const errData = await response.clone().json()
          msg = errData.message || msg
        } catch {}
      } else if (error instanceof Error) {
        msg = error.message
      }
      if (!silent) {
        setSaveResult({ success: false, message: msg })
        setShowSaveDialog(true)
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        icon={<BiCodeAlt className="text-2xl" />}
        title="环境变量配置"
        subtitle="Webhook 通知配置管理"
        actions={
          <Button
            onClick={() => handleSave(false)}
            size="sm"
            className="rounded-full bg-gradient-to-r from-blue-500 to-indigo-500 hover:from-blue-600 hover:to-indigo-600 text-white px-4 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105"
          >
            <BiSave className="mr-1" /> 保存配置
          </Button>
        }
      />

      {loading && (
        <Card className="border-blue-500/20 bg-blue-500/5">
          <CardContent className="pt-6">
            <p className="text-center text-sm text-muted-foreground">加载配置中...</p>
          </CardContent>
        </Card>
      )}

      <div className="space-y-6">
        {/* 企业微信 Webhook */}
        <Card className="animate-in fade-in slide-in-from-bottom-5 duration-700">
          <CardHeader className="flex items-start gap-4">
            <div className="min-w-0 space-y-2">
              <CardTitle className="flex items-center gap-2">
                <BiLinkExternal className="text-primary" />
                企业微信 Webhook
              </CardTitle>
              <CardDescription>配置企业微信群机器人，用于接收通知消息</CardDescription>
            </div>
            <div>
              <button
                type="button"
                aria-label="企业微信发送开关"
                onClick={() => setEnvConfig({ ...envConfig, botIsSend: envConfig.botIsSend ? 0 : 1 })}
                className={`relative inline-flex h-7 w-14 rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-emerald-400/40 border border-white/30 shadow-[inset_0_1px_0_rgba(255,255,255,.25)] ${envConfig.botIsSend ? 'bg-emerald-500/80 hover:bg-emerald-500' : 'bg-white/10 hover:bg-white/15'}`}
              >
                <span
                  className={`absolute top-1 left-1 h-5 w-5 rounded-full bg-white shadow transition-transform ${envConfig.botIsSend ? 'translate-x-7' : 'translate-x-0'}`}
                />
              </button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <Label htmlFor="hookUrl">Webhook URL</Label>
              <Input
                id="hookUrl"
                type="text"
                value={envConfig.hookUrl}
                onChange={(e) => setEnvConfig({ ...envConfig, hookUrl: e.target.value })}
                placeholder="https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=your_key"
              />
              <p className="text-xs text-muted-foreground">
                企业微信群机器人webhook地址，用于接收通知消息
              </p>
            </div>
          </CardContent>
        </Card>

        {/* 钉钉 Webhook */}
        <Card className="animate-in fade-in slide-in-from-bottom-5 duration-700">
          <CardHeader className="flex items-start gap-4">
            <div className="min-w-0 space-y-2">
              <CardTitle className="flex items-center gap-2">
                <BiLinkExternal className="text-primary" />
                钉钉 Webhook
              </CardTitle>
              <CardDescription>配置钉钉群机器人，用于接收通知消息</CardDescription>
            </div>
            <div>
              <button
                type="button"
                aria-label="钉钉发送开关"
                onClick={() => setEnvConfig({ ...envConfig, dingtalkIsSend: envConfig.dingtalkIsSend ? 0 : 1 })}
                className={`relative inline-flex h-7 w-14 rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-emerald-400/40 border border-white/30 shadow-[inset_0_1px_0_rgba(255,255,255,.25)] ${envConfig.dingtalkIsSend ? 'bg-emerald-500/80 hover:bg-emerald-500' : 'bg-white/10 hover:bg-white/15'}`}
              >
                <span
                  className={`absolute top-1 left-1 h-5 w-5 rounded-full bg-white shadow transition-transform ${envConfig.dingtalkIsSend ? 'translate-x-7' : 'translate-x-0'}`}
                />
              </button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <Label htmlFor="dingtalkHookUrl">Webhook URL</Label>
              <Input
                id="dingtalkHookUrl"
                type="text"
                value={envConfig.dingtalkHookUrl}
                onChange={(e) => setEnvConfig({ ...envConfig, dingtalkHookUrl: e.target.value })}
                placeholder="https://oapi.dingtalk.com/robot/send?access_token=your_token"
              />
              <p className="text-xs text-muted-foreground">
                钉钉群机器人webhook地址，用于接收通知消息
              </p>
            </div>
          </CardContent>
        </Card>

        {/* 保存结果弹框 */}
        {showSaveDialog && saveResult && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30" role="dialog" aria-modal="true">
            <div className="bg-white dark:bg-neutral-900 rounded-2xl shadow-2xl w-[92%] max-w-sm border border-gray-200 dark:border-neutral-800 animate-in fade-in zoom-in-95">
              <Card className="border-0">
                <CardHeader className="pb-2">
                  <CardTitle className="text-lg flex items-center gap-2">
                    <BiSave className={saveResult.success ? 'text-green-500' : 'text-red-500'} />
                    {saveResult.success ? '保存成功' : '保存失败'}
                  </CardTitle>
                </CardHeader>
                <CardContent className="pt-0">
                  <p className="text-sm text-muted-foreground mb-4">{saveResult.message}</p>
                  <div className="flex justify-end gap-2">
                    <Button
                      onClick={() => setShowSaveDialog(false)}
                      className={`rounded-full px-4 ${saveResult.success ? 'bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white' : 'bg-gradient-to-r from-red-500 to-rose-600 hover:from-red-600 hover:to-rose-700 text-white'}`}
                    >
                      知道了
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
