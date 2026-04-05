'use client'

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { getToken, clearAuth, getUserInfo, setUserInfo, getUserProfile } from '@/lib/auth-api'

interface UserInfo {
  userId: number
  username: string
  email?: string
  phone?: string
}

interface BillingInfo {
  applicationCount: number
  hasSubscription: boolean
  subscriptionEndDate?: string
  aiMatchCount: number
  aiGreetCount: number
  reportCount: number
  totalRecharge: number
  totalConsumption: number
}

interface UserContextType {
  user: UserInfo | null
  billing: BillingInfo | null
  isLoading: boolean
  isAuthenticated: boolean
  refreshUser: () => Promise<void>
  logout: () => void
}

const UserContext = createContext<UserContextType | undefined>(undefined)

export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [billing, setBilling] = useState<BillingInfo | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const isAuthenticated = !!user && !!getToken()

  // 初始化时检查登录状态
  useEffect(() => {
    checkAuth()
  }, [])

  async function checkAuth() {
    const token = getToken()
    const storedUser = getUserInfo()

    if (!token || !storedUser) {
      setIsLoading(false)
      return
    }

    try {
      await refreshUser()
    } catch (error) {
      console.error('Auth check failed:', error)
      clearAuth()
      setUser(null)
      setBilling(null)
    } finally {
      setIsLoading(false)
    }
  }

  async function refreshUser() {
    try {
      const profileData = await getUserProfile()
      if (profileData.success) {
        const userInfo = {
          userId: profileData.data.id,
          username: profileData.data.username,
          email: profileData.data.email,
          phone: profileData.data.phone,
        }
        setUser(userInfo)
        setUserInfo(userInfo)
        setBilling(profileData.data.billing)
      }
    } catch (error) {
      throw error
    }
  }

  function logout() {
    clearAuth()
    setUser(null)
    setBilling(null)
    // 重定向到登录页
    window.location.href = '/auth/login'
  }

  return (
    <UserContext.Provider value={{ user, billing, isLoading, isAuthenticated, refreshUser, logout }}>
      {children}
    </UserContext.Provider>
  )
}

export function useUser() {
  const context = useContext(UserContext)
  if (context === undefined) {
    throw new Error('useUser must be used within a UserProvider')
  }
  return context
}
