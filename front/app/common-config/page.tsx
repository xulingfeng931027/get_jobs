'use client'

import {useEffect, useState} from 'react'
import {BiCog, BiEdit, BiMap, BiMoney, BiPlus, BiSearch, BiX} from 'react-icons/bi'
import {Button} from '@/components/ui/button'
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card'
import {Input} from '@/components/ui/input'
import PageHeader from '@/app/components/PageHeader'
import {API_PATHS} from '@/lib/api-config'

interface CommonOption {
  id: number
  type: 'keyword' | 'city' | 'salary_range'
  label: string
  value: string
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export default function CommonConfigPage() {
  const [options, setOptions] = useState<CommonOption[]>([])
  const [loading, setLoading] = useState(true)

  // 输入状态
  const [keywordInput, setKeywordInput] = useState('')
  const [cityInput, setCityInput] = useState('')
  const [salaryMin, setSalaryMin] = useState('')
  const [salaryMax, setSalaryMax] = useState('')

  // 错误提示
  const [keywordError, setKeywordError] = useState('')
  const [cityError, setCityError] = useState('')
  const [salaryError, setSalaryError] = useState('')

  // 编辑状态
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editValue, setEditValue] = useState('')
  const [editError, setEditError] = useState('')

  // 获取所有选项
  const fetchOptions = async () => {
    try {
      setLoading(true)
      const response = await fetch(API_PATHS.commonOption)
      if (response.ok) {
        const result = await response.json()
        // 兼容不同的 API 响应格式
        const data = Array.isArray(result) ? result : (result.data || [])
        setOptions(data)
      } else {
        console.error('获取选项失败:', response.statusText)
      }
    } catch (error) {
      console.error('获取选项失败:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOptions()
  }, [])

  // 按类型分组选项
  const keywordOptions = options.filter(o => o.type === 'keyword')
  const cityOptions = options.filter(o => o.type === 'city')
  const salaryOptions = options.filter(o => o.type === 'salary_range')

  // 检查是否存在重复项
  const isDuplicate = (type: string, label: string): boolean => {
    return options.some(o => o.type === type && o.label === label)
  }

  // 添加关键词
  const handleAddKeyword = async () => {
    const keyword = keywordInput.trim()
    if (!keyword) {
      setKeywordError('请输入关键词')
      return
    }

    if (isDuplicate('keyword', keyword)) {
      setKeywordError('该关键词已存在')
      return
    }

    try {
      const response = await fetch(API_PATHS.commonOption, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'keyword',
          label: keyword,
          value: keyword,
          sortOrder: keywordOptions.length,
        }),
      })

      if (response.ok) {
        setKeywordInput('')
        setKeywordError('')
        await fetchOptions()
      } else {
        const errorText = await response.text()
        console.error('添加关键词失败:', response.status, errorText)
        setKeywordError(`添加失败: ${response.status}`)
      }
    } catch (error) {
      console.error('添加关键词失败:', error)
      setKeywordError('网络错误，请检查后端服务')
    }
  }

  // 添加城市
  const handleAddCity = async () => {
    const city = cityInput.trim()
    if (!city) {
      setCityError('请输入城市名称')
      return
    }

    if (isDuplicate('city', city)) {
      setCityError('该城市已存在')
      return
    }

    try {
      const response = await fetch(API_PATHS.commonOption, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'city',
          label: city,
          value: city,
          sortOrder: cityOptions.length,
        }),
      })

      if (response.ok) {
        setCityInput('')
        setCityError('')
        await fetchOptions()
      } else {
        const errorText = await response.text()
        console.error('添加城市失败:', response.status, errorText)
        setCityError(`添加失败: ${response.status}`)
      }
    } catch (error) {
      console.error('添加城市失败:', error)
      setCityError('网络错误，请检查后端服务')
    }
  }

  // 添加薪资范围
  const handleAddSalary = async () => {
    const min = parseInt(salaryMin, 10)
    const max = parseInt(salaryMax, 10)

    if (isNaN(min) || isNaN(max)) {
      setSalaryError('请输入有效的数字')
      return
    }

    if (min < 0 || max < 0) {
      setSalaryError('薪资不能为负数')
      return
    }

    if (min >= max) {
      setSalaryError('最低薪资必须小于最高薪资')
      return
    }

    const label = `${min}k-${max}k`
    const value = JSON.stringify({ min, max })

    if (isDuplicate('salary_range', label)) {
      setSalaryError('该薪资范围已存在')
      return
    }

    try {
      const response = await fetch(API_PATHS.commonOption, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'salary_range',
          label,
          value,
          sortOrder: salaryOptions.length,
        }),
      })

      if (response.ok) {
        setSalaryMin('')
        setSalaryMax('')
        setSalaryError('')
        await fetchOptions()
      } else {
        const errorText = await response.text()
        console.error('添加薪资范围失败:', response.status, errorText)
        setSalaryError(`添加失败: ${response.status}`)
      }
    } catch (error) {
      console.error('添加薪资范围失败:', error)
      setSalaryError('网络错误，请检查后端服务')
    }
  }

  // 删除选项
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(API_PATHS.commonOptionById(id), {
        method: 'DELETE',
      })

      if (response.ok) {
        await fetchOptions()
      } else {
        const errorText = await response.text()
        console.error('删除选项失败:', response.status, errorText)
        alert(`删除失败: ${response.status}`)
      }
    } catch (error) {
      console.error('删除选项失败:', error)
      alert('删除失败: 网络错误')
    }
  }

  // 开始编辑
  const startEdit = (option: CommonOption) => {
    setEditingId(option.id)
    setEditValue(option.label)
    setEditError('')
  }

  // 取消编辑
  const cancelEdit = () => {
    setEditingId(null)
    setEditValue('')
    setEditError('')
  }

  // 保存编辑
  const saveEdit = async (option: CommonOption) => {
    const newValue = editValue.trim()
    if (!newValue) {
      setEditError('内容不能为空')
      return
    }

    // 检查是否重复（排除当前编辑的项）
    const isDup = options.some(o => 
      o.type === option.type && 
      o.label === newValue && 
      o.id !== option.id
    )
    if (isDup) {
      setEditError('该内容已存在')
      return
    }

    try {
      const response = await fetch(API_PATHS.commonOptionById(option.id), {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...option,
          label: newValue,
          value: option.type === 'salary_range' ? option.value : newValue,
        }),
      })

      if (response.ok) {
        setEditingId(null)
        setEditValue('')
        setEditError('')
        await fetchOptions()
      } else {
        const errorText = await response.text()
        console.error('更新失败:', response.status, errorText)
        setEditError(`更新失败: ${response.status}`)
      }
    } catch (error) {
      console.error('更新失败:', error)
      setEditError('网络错误，请检查后端服务')
    }
  }

  // 处理编辑回车
  const handleEditKeyDown = (e: React.KeyboardEvent, option: CommonOption) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      saveEdit(option)
    } else if (e.key === 'Escape') {
      cancelEdit()
    }
  }

  // 处理回车添加
  const handleKeywordKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleAddKeyword()
    }
  }

  const handleCityKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleAddCity()
    }
  }

  const handleSalaryKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleAddSalary()
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        icon={<BiCog className="text-2xl" />}
        title="公共配置"
        subtitle="管理搜索关键词、城市和薪资范围选项池"
      />

      {loading && (
        <Card className="border-blue-500/20 bg-blue-500/5">
          <CardContent className="pt-6">
            <p className="text-center text-sm text-muted-foreground">加载配置中...</p>
          </CardContent>
        </Card>
      )}

      <div className="space-y-6">
        {/* 搜索关键词区域 */}
        <Card className="animate-in fade-in slide-in-from-bottom-5 duration-700">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BiSearch className="text-primary" />
              搜索关键词
            </CardTitle>
            <CardDescription>配置职位搜索的关键词选项池，可在平台配置中快速选择</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* 输入区域 */}
              <div className="flex gap-2">
                <div className="flex-1">
                  <Input
                    value={keywordInput}
                    onChange={(e) => {
                      setKeywordInput(e.target.value)
                      setKeywordError('')
                    }}
                    onKeyDown={handleKeywordKeyDown}
                    placeholder="输入搜索关键词，如：Java开发工程师"
                  />
                </div>
                <Button
                  onClick={handleAddKeyword}
                  className="rounded-full bg-gradient-to-r from-blue-500 to-indigo-500 hover:from-blue-600 hover:to-indigo-600 text-white px-4"
                >
                  <BiPlus className="mr-1" /> 添加
                </Button>
              </div>
              {keywordError && (
                <p className="text-xs text-red-500">{keywordError}</p>
              )}

              {/* 标签列表 */}
              <div className="flex flex-wrap gap-2">
                {keywordOptions.length === 0 ? (
                  <p className="text-sm text-muted-foreground">暂无关键词，请添加</p>
                ) : (
                  keywordOptions.map((option) => (
                    <span
                      key={option.id}
                      className="inline-flex items-center gap-1 px-3 py-1.5 rounded-full text-sm bg-gradient-to-r from-blue-500/10 to-indigo-500/10 border border-blue-500/20 text-blue-700 dark:text-blue-300"
                    >
                      {editingId === option.id ? (
                        <div className="flex items-center gap-1">
                          <Input
                            value={editValue}
                            onChange={(e) => {
                              setEditValue(e.target.value)
                              setEditError('')
                            }}
                            onKeyDown={(e) => handleEditKeyDown(e, option)}
                            className="h-6 w-32 px-2 py-0 text-sm"
                            autoFocus
                          />
                          <button
                            onClick={() => saveEdit(option)}
                            className="p-0.5 rounded-full hover:bg-blue-500/20 transition-colors"
                            title="保存"
                          >
                            <BiPlus className="text-base" />
                          </button>
                          <button
                            onClick={cancelEdit}
                            className="p-0.5 rounded-full hover:bg-blue-500/20 transition-colors"
                            title="取消"
                          >
                            <BiX className="text-base" />
                          </button>
                        </div>
                      ) : (
                        <>
                          <span className="cursor-pointer" onClick={() => startEdit(option)}>
                            {option.label}
                          </span>
                          <button
                            onClick={() => startEdit(option)}
                            className="p-0.5 rounded-full hover:bg-blue-500/20 transition-colors"
                            title="编辑"
                          >
                            <BiEdit className="text-sm" />
                          </button>
                          <button
                            onClick={() => handleDelete(option.id)}
                            className="p-0.5 rounded-full hover:bg-blue-500/20 transition-colors"
                            title="删除"
                          >
                            <BiX className="text-base" />
                          </button>
                        </>
                      )}
                    </span>
                  ))
                )}
              </div>
              {editError && editingId && keywordOptions.some(o => o.id === editingId) && (
                <p className="text-xs text-red-500">{editError}</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* 工作城市区域 */}
        <Card className="animate-in fade-in slide-in-from-bottom-6 duration-700">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BiMap className="text-primary" />
              工作城市
            </CardTitle>
            <CardDescription>配置目标工作城市选项池，可在平台配置中快速选择</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* 输入区域 */}
              <div className="flex gap-2">
                <div className="flex-1">
                  <Input
                    value={cityInput}
                    onChange={(e) => {
                      setCityInput(e.target.value)
                      setCityError('')
                    }}
                    onKeyDown={handleCityKeyDown}
                    placeholder="输入城市名称，如：北京"
                  />
                </div>
                <Button
                  onClick={handleAddCity}
                  className="rounded-full bg-gradient-to-r from-teal-500 to-green-500 hover:from-teal-600 hover:to-green-600 text-white px-4"
                >
                  <BiPlus className="mr-1" /> 添加
                </Button>
              </div>
              {cityError && (
                <p className="text-xs text-red-500">{cityError}</p>
              )}

              {/* 标签列表 */}
              <div className="flex flex-wrap gap-2">
                {cityOptions.length === 0 ? (
                  <p className="text-sm text-muted-foreground">暂无城市，请添加</p>
                ) : (
                  cityOptions.map((option) => (
                    <span
                      key={option.id}
                      className="inline-flex items-center gap-1 px-3 py-1.5 rounded-full text-sm bg-gradient-to-r from-teal-500/10 to-green-500/10 border border-teal-500/20 text-teal-700 dark:text-teal-300"
                    >
                      {editingId === option.id ? (
                        <div className="flex items-center gap-1">
                          <Input
                            value={editValue}
                            onChange={(e) => {
                              setEditValue(e.target.value)
                              setEditError('')
                            }}
                            onKeyDown={(e) => handleEditKeyDown(e, option)}
                            className="h-6 w-32 px-2 py-0 text-sm"
                            autoFocus
                          />
                          <button
                            onClick={() => saveEdit(option)}
                            className="p-0.5 rounded-full hover:bg-teal-500/20 transition-colors"
                            title="保存"
                          >
                            <BiPlus className="text-base" />
                          </button>
                          <button
                            onClick={cancelEdit}
                            className="p-0.5 rounded-full hover:bg-teal-500/20 transition-colors"
                            title="取消"
                          >
                            <BiX className="text-base" />
                          </button>
                        </div>
                      ) : (
                        <>
                          <span className="cursor-pointer" onClick={() => startEdit(option)}>
                            {option.label}
                          </span>
                          <button
                            onClick={() => startEdit(option)}
                            className="p-0.5 rounded-full hover:bg-teal-500/20 transition-colors"
                            title="编辑"
                          >
                            <BiEdit className="text-sm" />
                          </button>
                          <button
                            onClick={() => handleDelete(option.id)}
                            className="p-0.5 rounded-full hover:bg-teal-500/20 transition-colors"
                            title="删除"
                          >
                            <BiX className="text-base" />
                          </button>
                        </>
                      )}
                    </span>
                  ))
                )}
              </div>
              {editError && editingId && cityOptions.some(o => o.id === editingId) && (
                <p className="text-xs text-red-500">{editError}</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* 薪资范围区域 */}
        <Card className="animate-in fade-in slide-in-from-bottom-7 duration-700">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BiMoney className="text-primary" />
              薪资范围
            </CardTitle>
            <CardDescription>配置期望薪资范围选项池，可在平台配置中快速选择</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* 输入区域 */}
              <div className="flex gap-2 items-center">
                <div className="w-24">
                  <Input
                    type="number"
                    value={salaryMin}
                    onChange={(e) => {
                      setSalaryMin(e.target.value)
                      setSalaryError('')
                    }}
                    onKeyDown={handleSalaryKeyDown}
                    placeholder="最低"
                    min={0}
                  />
                </div>
                <span className="text-sm text-muted-foreground">K —</span>
                <div className="w-24">
                  <Input
                    type="number"
                    value={salaryMax}
                    onChange={(e) => {
                      setSalaryMax(e.target.value)
                      setSalaryError('')
                    }}
                    onKeyDown={handleSalaryKeyDown}
                    placeholder="最高"
                    min={0}
                  />
                </div>
                <span className="text-sm text-muted-foreground">K</span>
                <Button
                  onClick={handleAddSalary}
                  className="rounded-full bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white px-4"
                >
                  <BiPlus className="mr-1" /> 添加
                </Button>
              </div>
              {salaryError && (
                <p className="text-xs text-red-500">{salaryError}</p>
              )}

              {/* 标签列表 */}
              <div className="flex flex-wrap gap-2">
                {salaryOptions.length === 0 ? (
                  <p className="text-sm text-muted-foreground">暂无薪资范围，请添加</p>
                ) : (
                  salaryOptions.map((option) => (
                    <span
                      key={option.id}
                      className="inline-flex items-center gap-1 px-3 py-1.5 rounded-full text-sm bg-gradient-to-r from-amber-500/10 to-orange-500/10 border border-amber-500/20 text-amber-700 dark:text-amber-300"
                    >
                      {editingId === option.id ? (
                        <div className="flex items-center gap-1">
                          <Input
                            value={editValue}
                            onChange={(e) => {
                              setEditValue(e.target.value)
                              setEditError('')
                            }}
                            onKeyDown={(e) => handleEditKeyDown(e, option)}
                            className="h-6 w-32 px-2 py-0 text-sm"
                            autoFocus
                          />
                          <button
                            onClick={() => saveEdit(option)}
                            className="p-0.5 rounded-full hover:bg-amber-500/20 transition-colors"
                            title="保存"
                          >
                            <BiPlus className="text-base" />
                          </button>
                          <button
                            onClick={cancelEdit}
                            className="p-0.5 rounded-full hover:bg-amber-500/20 transition-colors"
                            title="取消"
                          >
                            <BiX className="text-base" />
                          </button>
                        </div>
                      ) : (
                        <>
                          <span className="cursor-pointer" onClick={() => startEdit(option)}>
                            {option.label}
                          </span>
                          <button
                            onClick={() => startEdit(option)}
                            className="p-0.5 rounded-full hover:bg-amber-500/20 transition-colors"
                            title="编辑"
                          >
                            <BiEdit className="text-sm" />
                          </button>
                          <button
                            onClick={() => handleDelete(option.id)}
                            className="p-0.5 rounded-full hover:bg-amber-500/20 transition-colors"
                            title="删除"
                          >
                            <BiX className="text-base" />
                          </button>
                        </>
                      )}
                    </span>
                  ))
                )}
              </div>
              {editError && editingId && salaryOptions.some(o => o.id === editingId) && (
                <p className="text-xs text-red-500">{editError}</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* 使用提示 */}
        <Card className="border-primary/20 bg-primary/5 animate-in fade-in slide-in-from-bottom-8 duration-700">
          <CardContent className="pt-6">
            <div className="flex gap-3">
              <BiCog className="h-5 w-5 text-primary flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-sm text-foreground">
                  <strong className="font-semibold">提示：</strong>
                  这些选项将作为公共配置池，在各平台（Boss直聘、猎聘等）的搜索配置中可快速选择使用。
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
