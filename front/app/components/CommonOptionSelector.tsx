'use client'

import {useCallback, useEffect, useRef, useState} from 'react'
import {createPortal} from 'react-dom'
import {BiCheck, BiCollection} from 'react-icons/bi'
import {Button} from '@/components/ui/button'
import {cn} from '@/lib/utils'
import {API_PATHS} from '@/lib/api-config'

interface CommonOption {
  id: number
  type: string
  label: string
  value: string
  sortOrder: number
}

interface CommonOptionSelectorProps {
  type: 'keyword' | 'city' | 'salary_range'
  mode: 'single' | 'multi'
  onSelect: (values: string[]) => void
  currentValues?: string[]
  /** 自定义按钮文字，默认根据 type 自动生成 */
  buttonText?: string
}

export default function CommonOptionSelector({
  type,
  mode,
  onSelect,
  currentValues = [],
  buttonText,
}: CommonOptionSelectorProps) {
  const [options, setOptions] = useState<CommonOption[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [mounted, setMounted] = useState(false)
  const [tempSelected, setTempSelected] = useState<string[]>([])

  const wrapperRef = useRef<HTMLDivElement>(null)
  const buttonRef = useRef<HTMLButtonElement>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const [dropdownPosition, setDropdownPosition] = useState({ top: 0, left: 0, width: 0 })

  // 确保组件已挂载（解决 SSR 问题）
  useEffect(() => {
    setMounted(true)
  }, [])

  // 获取公共选项列表
  const fetchOptions = async () => {
    try {
      setLoading(true)
      const response = await fetch(`${API_PATHS.commonOption}?type=${type}`)
      if (response.ok) {
        const data = await response.json()
        // 按 sortOrder 排序
        const sorted = (data || []).sort((a: CommonOption, b: CommonOption) => 
          (a.sortOrder ?? 0) - (b.sortOrder ?? 0)
        )
        setOptions(sorted)
      } else {
        console.error('获取公共选项失败:', response.statusText)
        setOptions([])
      }
    } catch (error) {
      console.error('获取公共选项失败:', error)
      setOptions([])
    } finally {
      setLoading(false)
    }
  }

  // 打开时获取数据并初始化临时选中状态
  useEffect(() => {
    if (open) {
      fetchOptions()
      setTempSelected([...currentValues])
    }
  }, [open])

  // 计算下拉框位置
  const updatePosition = useCallback(() => {
    if (buttonRef.current) {
      const rect = buttonRef.current.getBoundingClientRect()
      const viewportHeight = window.innerHeight
      const dropdownHeight = 320 // 预估高度
      
      // 判断是向上还是向下展开
      const spaceBelow = viewportHeight - rect.bottom
      const showAbove = spaceBelow < dropdownHeight && rect.top > dropdownHeight
      
      setDropdownPosition({
        top: showAbove ? rect.top - dropdownHeight - 8 : rect.bottom + 8,
        left: rect.left,
        width: Math.max(rect.width, 240),
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
      }
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
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

  // 单选模式：点击直接选中并关闭
  const handleSingleSelect = (value: string) => {
    onSelect([value])
    setOpen(false)
  }

  // 多选模式：切换选中状态
  const handleMultiToggle = (value: string) => {
    setTempSelected((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
    )
  }

  // 多选确认
  const handleMultiConfirm = () => {
    onSelect(tempSelected)
    setOpen(false)
  }

  // 获取默认按钮文字
  const getDefaultButtonText = () => {
    switch (type) {
      case 'keyword':
        return '公共关键词'
      case 'city':
        return '公共城市'
      case 'salary_range':
        return '公共薪资'
      default:
        return '公共配置'
    }
  }

  const displayText = buttonText || getDefaultButtonText()

  return (
    <div ref={wrapperRef} className="relative inline-flex">
      <button
        ref={buttonRef}
        type="button"
        onClick={() => setOpen((v) => !v)}
        disabled={loading}
        className={cn(
          "inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium rounded-full",
          "border border-emerald-300/40 bg-emerald-500/10 text-emerald-700 dark:text-emerald-400",
          "hover:bg-emerald-500/20 hover:border-emerald-400/60 transition-all duration-200",
          "focus:outline-none focus:ring-2 focus:ring-emerald-400/40",
          loading && "opacity-50 cursor-not-allowed"
        )}
      >
        <BiCollection className="text-sm" />
        <span>{displayText}</span>
      </button>

      {open && mounted && createPortal(
        <div
          ref={dropdownRef}
          className="dropdown-panel"
          style={{
            top: `${dropdownPosition.top}px`,
            left: `${dropdownPosition.left}px`,
            width: `${dropdownPosition.width}px`,
            minWidth: '240px',
          }}
        >
          {loading ? (
            <div className="px-3 py-4 text-sm text-muted-foreground text-center">
              加载中...
            </div>
          ) : options.length === 0 ? (
            <div className="px-3 py-4 text-sm text-muted-foreground text-center">
              暂无公共配置，请先到公共配置页添加
            </div>
          ) : (
            <>
              <div className="max-h-56 overflow-y-auto">
                <ul className="py-1">
                  {options.map((option) => {
                    const isSelected = mode === 'multi'
                      ? tempSelected.includes(option.value)
                      : currentValues.includes(option.value)
                    
                    return (
                      <li
                        key={option.id}
                        className={cn(
                          "group flex items-center gap-3 px-3 py-2 cursor-pointer transition-all border-b border-white/12 last:border-b-0",
                          isSelected
                            ? "bg-gradient-to-r from-emerald-500/12 to-cyan-500/12"
                            : "hover:bg-white/12"
                        )}
                        onClick={() =>
                          mode === 'single'
                            ? handleSingleSelect(option.value)
                            : handleMultiToggle(option.value)
                        }
                      >
                        <span
                          className={cn(
                            "inline-flex h-4 w-4 items-center justify-center rounded-md border border-white/30 bg-white/10 shadow-inner transition-all flex-shrink-0",
                            isSelected && "bg-emerald-400/60 border-emerald-300/80"
                          )}
                        >
                          {isSelected && <BiCheck className="text-white text-xs" />}
                        </span>
                        <span className="flex flex-col flex-1 min-w-0">
                          <span className="text-sm truncate">{option.label}</span>
                          {option.value !== option.label && (
                            <span className="text-xs text-muted-foreground truncate">
                              {option.value}
                            </span>
                          )}
                        </span>
                      </li>
                    )
                  })}
                </ul>
              </div>

              {/* 多选模式下的确认按钮 */}
              {mode === 'multi' && (
                <div className="border-t border-white/20 p-2">
                  <Button
                    size="sm"
                    onClick={handleMultiConfirm}
                    className="w-full h-8 rounded-full bg-gradient-to-r from-emerald-500 to-cyan-500 hover:from-emerald-600 hover:to-cyan-600 text-white"
                  >
                    确认选择 ({tempSelected.length})
                  </Button>
                </div>
              )}
            </>
          )}
        </div>,
        document.body
      )}
    </div>
  )
}
