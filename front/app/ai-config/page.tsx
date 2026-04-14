'use client'

import {useEffect, useState} from 'react'
import {BiBrain, BiCodeAlt, BiInfoCircle, BiKey, BiSave} from 'react-icons/bi'
import {Button} from '@/components/ui/button'
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {Textarea} from '@/components/ui/textarea'
import PageHeader from '@/app/components/PageHeader'
import { authFetch } from '@/lib/auth-fetch'
import {API_PATHS} from '@/lib/api-config'
import {useToast} from '@/components/Toast'

export default function AiConfigPage() {
  const { showToast } = useToast();

  // AI 提示词配置
  const [aiConfig, setAiConfig] = useState({
    introduce: '',
    prompt: '',
  })

  // API 配置
  const [apiConfig, setApiConfig] = useState({
    baseUrl: '',
    apiKey: '',
    model: '',
  })

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [showApiKey, setShowApiKey] = useState(false)
  // 是否启用AI（映射 boss_config.enable_ai）
  const [enableAi, setEnableAi] = useState<number>(0)

  // 加载配置
  useEffect(() => {
    fetchAiConfig()
    fetchEnableAi()
    fetchApiConfig()
  }, [])

  const fetchAiConfig = async () => {
    try {
      const response = await authFetch(API_PATHS.aiConfig, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const result = await response.json()
      if (result.success && result.data) {
        setAiConfig({
          introduce: result.data.introduce || '',
          prompt: result.data.prompt || '',
        })
      }
    } catch (error) {
      console.error('加载AI配置失败:', error)
      let msg = '加载AI配置失败'
      if (error instanceof Error) {
        msg = error.message
      }
      showToast(msg, 'error')
      // 如果加载失败，使用默认值，不影响用户使用
      console.log('使用默认配置')
    }
  }

  // 加载 API 配置
  const fetchApiConfig = async () => {
    try {
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
        setApiConfig({
          baseUrl: result.data.BASE_URL || '',
          apiKey: result.data.API_KEY || '',
          model: result.data.MODEL || '',
        })
      }
    } catch (error) {
      console.error('加载API配置失败:', error)
      let msg = '加载API配置失败'
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

  // 加载 boss_config 的 enable_ai 字段
  const fetchEnableAi = async () => {
    try {
      const response = await authFetch(API_PATHS.boss.config, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const result = await response.json()
      const raw = result?.config?.enableAi
      const val = String(raw ?? '').trim().toLowerCase()
      setEnableAi(val === '1' || val === 'true' || val === 'on' ? 1 : Number(raw) === 1 ? 1 : 0)
    } catch (e) {
      console.error('加载enable_ai失败:', e)
      let msg = '加载enable_ai失败'
      if (response && !response.ok) {
        try {
          const errData = await response.clone().json()
          msg = errData.message || msg
        } catch {}
      } else if (e instanceof Error) {
        msg = e.message
      }
      showToast(msg, 'error')
    }
  }

  // 切换 AI 开关并保存到 boss_config
  const toggleEnableAi = async () => {
    try {
      const next = enableAi ? 0 : 1
      setEnableAi(next)
      const response = await authFetch(API_PATHS.boss.config, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ enableAi: next }),
      })
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      // 可选：校验返回体
      // const updated = await response.json()
    } catch (e) {
      console.error('更新enable_ai失败:', e)
      // 回滚
      setEnableAi((prev) => (prev ? 0 : 1))
      showToast('切换失败，请检查后端服务连接', 'error')
    }
  }

  const handleSave = async () => {
    setSaving(true)
    try {
      // 保存AI配置
      const aiResponse = await authFetch(API_PATHS.aiConfig, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(aiConfig),
      })

      // 保存 API 配置
      const apiConfigMap = {
        BASE_URL: apiConfig.baseUrl,
        API_KEY: apiConfig.apiKey,
        MODEL: apiConfig.model,
      }
      const apiResponse = await authFetch(API_PATHS.config, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(apiConfigMap),
      })

      const aiResult = await aiResponse.json()
      const apiResult = await apiResponse.json()

      if (aiResult.success && apiResult.success) {
        showToast('配置已保存！', 'success')
      } else {
        showToast('保存失败: ' + (aiResult.message || apiResult.message), 'error')
      }
    } catch (error) {
      console.error('保存配置失败:', error)
      let msg = '保存失败，请检查服务器连接'
      if (error instanceof Error) {
        msg = error.message
      }
      showToast(msg, 'error')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        icon={<BiBrain className="text-2xl" />}
        title="AI配置"
        subtitle="配置 API 和 AI 提示词"
        iconClass="text-white"
        accentBgClass="bg-purple-500"
        actions={
          <Button
            onClick={handleSave}
            size="sm"
            className="rounded-full bg-gradient-to-r from-blue-500 to-indigo-500 hover:from-blue-600 hover:to-indigo-600 text-white px-4 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105"
            type="button"
            disabled={loading}
          >
            <BiSave className="mr-1" /> 保存配置
          </Button>
        }
      />

      <div className="space-y-6">
        {/* API 配置 */}
        <Card className="animate-in fade-in slide-in-from-bottom-5 duration-700">
          <CardHeader className="flex items-start gap-4">
            <div className="min-w-0 space-y-2">
              <CardTitle className="flex items-center gap-2">
                <BiCodeAlt className="text-primary" />
                API 配置
              </CardTitle>
              <CardDescription>配置 API 服务器地址、AI模型和访问密钥</CardDescription>
            </div>
            <div>
              <button
                type="button"
                aria-label="AI启用开关"
                onClick={toggleEnableAi}
                className={`relative inline-flex h-7 w-14 rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-emerald-400/40 border border-white/30 shadow-[inset_0_1px_0_rgba(255,255,255,.25)] ${enableAi ? 'bg-emerald-500/80 hover:bg-emerald-500' : 'bg-white/10 hover:bg-white/15'}`}
              >
                <span
                  className={`absolute top-1 left-1 h-5 w-5 rounded-full bg-white shadow transition-transform ${enableAi ? 'translate-x-7' : 'translate-x-0'}`}
                />
              </button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <Label htmlFor="baseUrl">API Base URL</Label>
                <Input
                  id="baseUrl"
                  type="text"
                  value={apiConfig.baseUrl}
                  onChange={(e) => setApiConfig({ ...apiConfig, baseUrl: e.target.value })}
                  placeholder="https://api.ruyun.fun"
                />
                <p className="text-xs text-muted-foreground">API服务器地址</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="model">AI模型</Label>
                <Input
                  id="model"
                  type="text"
                  value={apiConfig.model}
                  onChange={(e) => setApiConfig({ ...apiConfig, model: e.target.value })}
                  placeholder="gpt-5-nano-2025-08-07"
                />
                <p className="text-xs text-muted-foreground">使用的AI模型名称</p>
              </div>
            </div>

            <div className="space-y-2 mt-6">
              <Label htmlFor="apiKey">API Key</Label>
              <div className="relative">
                <Input
                  id="apiKey"
                  type={showApiKey ? 'text' : 'password'}
                  value={apiConfig.apiKey}
                  onChange={(e) => setApiConfig({ ...apiConfig, apiKey: e.target.value })}
                  placeholder="sk-xxxxxxxxxxxxxxxxx"
                />
                <Button
                  onClick={() => setShowApiKey(!showApiKey)}
                  variant="ghost"
                  size="sm"
                  className="absolute right-1 top-1/2 -translate-y-1/2 h-7"
                  type="button"
                >
                  {showApiKey ? '隐藏' : '显示'}
                </Button>
              </div>
              <p className="text-xs text-muted-foreground">
                API密钥将被安全存储，请妥善保管
              </p>
            </div>
          </CardContent>
        </Card>

        {/* AI提示词配置 */}
        <Card className="animate-in fade-in slide-in-from-bottom-6 duration-700">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BiBrain className="text-primary" />
              AI提示词配置
            </CardTitle>
            <CardDescription>配置AI相关的技能介绍和提示词，用于生成个性化求职内容</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="introduce">技能介绍</Label>
                <Textarea
                  id="introduce"
                  value={aiConfig.introduce}
                  onChange={(e) => setAiConfig({ ...aiConfig, introduce: e.target.value })}
                  placeholder="请输入您的技能介绍，例如：我熟练使用Java、Python等语言进行开发..."
                  className="min-h-[150px] resize-y"
                />
                <p className="text-xs text-muted-foreground">
                  详细描述您的技能、经验和专业背景，AI将使用这些信息生成个性化的求职文本
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="prompt">AI提示词</Label>
                <Textarea
                  id="prompt"
                  value={aiConfig.prompt}
                  onChange={(e) => setAiConfig({ ...aiConfig, prompt: e.target.value })}
                  placeholder="请输入AI提示词模板，例如：我目前在找工作，%s，我期望的岗位方向是【%s】..."
                  className="min-h-[150px] resize-y"
                />
                <p className="text-xs text-muted-foreground">
                  AI使用的提示词模板，支持使用 %s 作为占位符，用于动态插入内容
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* ��用说明 */}
        <Card className="border-primary/20 bg-primary/5 animate-in fade-in slide-in-from-bottom-6 duration-700">
          <CardContent className="pt-6">
            <div className="flex gap-3">
              <BiInfoCircle className="h-5 w-5 text-primary flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-sm text-foreground mb-2">
                  <strong className="font-semibold">使用说明：</strong>
                </p>
                <ul className="text-sm text-muted-foreground space-y-2">
                  <li className="flex items-start gap-2">
                    <span className="text-primary mt-0.5">•</span>
                    <span><strong>技能介绍：</strong>用于AI了解您的专业技能、工作经验和技术背景，是生成个性化内容的基础</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-primary mt-0.5">•</span>
                    <span><strong>AI提示词：</strong>定义AI生成内容的模板和风格，支持使用 <code className="bg-muted px-1 py-0.5 rounded text-xs">%s</code> 作为占位符</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-primary mt-0.5">•</span>
                    <span><strong>效果：</strong>配置保存后，AI将在自动投递时使用这些信息生成匹配度高的求职沟通内容</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-primary mt-0.5">•</span>
                    <span><strong>提示：</strong>建议定期更新技能介绍以反映最新的技能和经验，提高匹配成功率</span>
                  </li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 操作按钮（已迁移到右上角 PageHeader.actions，保持与环境配置一致） */}
      </div>
    </div>
  )
}
