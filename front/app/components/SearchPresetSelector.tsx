'use client'

import {useCallback, useEffect, useRef, useState} from 'react'
import {createPortal} from 'react-dom'
import {BiBookmark, BiPlus, BiX} from 'react-icons/bi'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {cn} from '@/lib/utils'
import {API_PATHS} from '@/lib/api-config'

interface SearchPreset {
  id: number
  name: string
  keywords: string
  /** 城市名称（跨平台统一存名称，非编码） */
  city: string
}

interface SearchPresetSelectorProps {
  currentKeywords: string
  /** 当前城市名称（用于显示和保存预设） */
  currentCity: string
  /** 选择预设时的回调，preset.city 为城市名称 */
  onSelect: (preset: { keywords: string; city: string }) => void
}

export default function SearchPresetSelector({
  currentKeywords,
  currentCity,
  onSelect,
}: SearchPresetSelectorProps) {
  const [presets, setPresets] = useState<SearchPreset[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [mounted, setMounted] = useState(false)
  const [showSaveInput, setShowSaveInput] = useState(false)
  const [saveName, setSaveName] = useState('')
  const [selectedPresetId, setSelectedPresetId] = useState<number | null>(null)

  const wrapperRef = useRef<HTMLDivElement>(null)
  const buttonRef = useRef<HTMLButtonElement>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const saveInputRef = useRef<HTMLInputElement>(null)
  const [dropdownPosition, setDropdownPosition] = useState({ top: 0, left: 0, width: 0 })

  // 确保组件已挂载（解决 SSR 问题）
  useEffect(() => {
    setMounted(true)
  }, [])

  // 组件挂载时获取预设列表
  useEffect(() => {
    fetchPresets()
  }, [])

  // 显示保存输入框时自动聚焦
  useEffect(() => {
    if (showSaveInput && saveInputRef.current) {
      saveInputRef.current.focus()
    }
  }, [showSaveInput])

  // 获取预设列表
  const fetchPresets = async () => {
    try {
      setLoading(true)
      const response = await fetch(API_PATHS.searchPreset)
      if (response.ok) {
        const data = await response.json()
        setPresets(data || [])
      } else {
        console.error('获取搜索预设失败:', response.statusText)
      }
    } catch (error) {
      console.error('获取搜索预设失败:', error)
    } finally {
      setLoading(false)
    }
  }

  // 删除预设
  const handleDelete = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation() // 阻止事件冒泡，避免触发选择
    try {
      const response = await fetch(API_PATHS.searchPresetById(id), {
        method: 'DELETE',
      })
      if (response.ok) {
        // 如果删除的是当前选中的预设，清空选中状态
        if (selectedPresetId === id) {
          setSelectedPresetId(null)
        }
        fetchPresets() // 刷新列表
      } else {
        console.error('删除预设失败:', response.statusText)
      }
    } catch (error) {
      console.error('删除预设失败:', error)
    }
  }

  // 保存新预设
  const handleSave = async () => {
    if (!saveName.trim()) return

    try {
      const response = await fetch(API_PATHS.searchPreset, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: saveName.trim(),
          keywords: currentKeywords,
          city: currentCity,
        }),
      })

      if (response.ok) {
        setSaveName('')
        setShowSaveInput(false)
        fetchPresets() // 刷新列表
      } else {
        console.error('保存预设失败:', response.statusText)
      }
    } catch (error) {
      console.error('保存预设失败:', error)
    }
  }

  // 选择预设
  const handleSelect = (preset: SearchPreset) => {
    setSelectedPresetId(preset.id)
    onSelect({ keywords: preset.keywords, city: preset.city })
    setOpen(false)
  }

  // 计算下拉框位置
  const updatePosition = useCallback(() => {
    if (buttonRef.current) {
      const rect = buttonRef.current.getBoundingClientRect()
      setDropdownPosition({
        top: rect.bottom + 8,
        left: rect.left,
        width: rect.width,
      })
    }
  }, [])

  // 打开时计算位置
  useEffect(() => {
    if (open) {
      updatePosition()
      const handleUpdate = () => updatePosition()
      window.addEventListener('scroll', handleUpdate, true)
      window.addEventListener('resize', handleUpdate)
      return () => {
        window.removeEventListener('scroll', handleUpdate, true)
        window.removeEventListener('resize', handleUpdate)
      }
    }
  }, [open, updatePosition])

  // 点击外部关闭下拉框
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Node
      const clickedButton = wrapperRef.current?.contains(target)
      const clickedDropdown = dropdownRef.current?.contains(target)

      if (!clickedButton && !clickedDropdown) {
        setOpen(false)
        setShowSaveInput(false)
        setSaveName('')
      }
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
        setShowSaveInput(false)
        setSaveName('')
      }
    }

    if (open) {
      setTimeout(() => {
        document.addEventListener('mousedown', handleClickOutside)
        document.addEventListener('keydown', handleEscape)
      }, 0)
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [open])

  // 获取选中预设的名称
  const selectedPresetName = selectedPresetId
    ? presets.find((p) => p.id === selectedPresetId)?.name
    : null

  return (
    <div ref={wrapperRef} className="relative w-full">
      <button
        ref={buttonRef}
        type="button"
        onClick={() => setOpen((v) => !v)}
        disabled={loading}
        className={cn(
          "flex h-10 w-full rounded-full px-4 py-2 text-sm pr-8",
          "border border-white/40 bg-white/5 shadow-[inset_0_1px_0_rgba(255,255,255,.25)]",
          "transition-all duration-200 hover:bg-white/10 hover:shadow-md",
          "focus:outline-none focus:ring-2 focus:ring-emerald-400/40 focus:border-emerald-300/60",
          "bg-[url('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"%23a1a1aa\" stroke-width=\"2\"><path d=\"M6 9l6 6 6-6\"/></svg>')] bg-no-repeat bg-[length:16px_16px] bg-[position:right_12px_center]",
          loading && "opacity-50 cursor-not-allowed"
        )}
      >
        <span className="flex items-center gap-2 truncate">
          <BiBookmark className="text-emerald-500 flex-shrink-0" />
          <span className="truncate">
            {selectedPresetName || '选择搜索预设'}
          </span>
        </span>
      </button>

      {open && mounted && createPortal(
        <div
          ref={dropdownRef}
          className="dropdown-panel"
          style={{
            top: `${dropdownPosition.top}px`,
            left: `${dropdownPosition.left}px`,
            width: `${dropdownPosition.width}px`,
          }}
        >
          <div className="max-h-64 overflow-y-auto">
            {presets.length === 0 ? (
              <div className="px-3 py-4 text-sm text-muted-foreground text-center">
                暂无搜索预设
              </div>
            ) : (
              <ul className="py-1">
                {presets.map((preset) => {
                  const active = selectedPresetId === preset.id
                  return (
                    <li
                      key={preset.id}
                      className={cn(
                        "group flex items-center justify-between gap-2 px-3 py-2 cursor-pointer transition-all border-b border-white/12 last:border-b-0",
                        active
                          ? "bg-gradient-to-r from-emerald-500/12 to-cyan-500/12"
                          : "hover:bg-white/12"
                      )}
                      onClick={() => handleSelect(preset)}
                    >
                      <span className="flex items-center gap-3 flex-1 min-w-0">
                        <span
                          className={cn(
                            "inline-flex h-4 w-4 items-center justify-center rounded-md border border-white/30 bg-white/10 shadow-inner transition-all flex-shrink-0",
                            active && "bg-emerald-400/60 border-emerald-300/80"
                          )}
                        />
                        <span className="text-sm truncate">{preset.name}</span>
                      </span>
                      <button
                        type="button"
                        onClick={(e) => handleDelete(e, preset.id)}
                        className="flex-shrink-0 p-1 rounded-full text-muted-foreground hover:text-red-500 hover:bg-red-50 transition-all opacity-0 group-hover:opacity-100"
                        title="删除预设"
                      >
                        <BiX className="text-lg" />
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>

          {/* 保存新预设区域 */}
          <div className="border-t border-white/20 p-2">
            {showSaveInput ? (
              <div className="flex gap-2">
                <Input
                  ref={saveInputRef}
                  value={saveName}
                  onChange={(e) => setSaveName(e.target.value)}
                  placeholder="输入预设名称"
                  className="h-8 text-sm"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      handleSave()
                    } else if (e.key === 'Escape') {
                      setShowSaveInput(false)
                      setSaveName('')
                    }
                  }}
                />
                <Button
                  size="sm"
                  onClick={handleSave}
                  disabled={!saveName.trim()}
                  className="h-8 px-3 rounded-full bg-gradient-to-r from-emerald-500 to-cyan-500 hover:from-emerald-600 hover:to-cyan-600 text-white"
                >
                  保存
                </Button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => setShowSaveInput(true)}
                className="w-full flex items-center justify-center gap-2 px-3 py-2 text-sm text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 rounded-full transition-all"
              >
                <BiPlus className="text-lg" />
                保存当前为新预设
              </button>
            )}
          </div>
        </div>,
        document.body
      )}
    </div>
  )
}
