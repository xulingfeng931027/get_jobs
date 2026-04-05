// 用户认证 API
import {API_BASE_URL} from './api-config'

const API_USER = `${API_BASE_URL}/api/user`
const API_BILLING = `${API_BASE_URL}/api/billing`
const API_SUBSCRIPTION = `${API_BASE_URL}/api/subscription`

// 获取存储的 token
export function getToken(): string | null {
  if (typeof window === 'undefined') return null
  return localStorage.getItem('user_token')
}

// 设置 token
export function setToken(token: string) {
  if (typeof window === 'undefined') return
  localStorage.setItem('user_token', token)
}

// 清除 token 和用户信息
export function clearAuth() {
  if (typeof window === 'undefined') return
  localStorage.removeItem('user_token')
  localStorage.removeItem('user_info')
}

// 获取用户信息
export function getUserInfo(): any | null {
  if (typeof window === 'undefined') return null
  const info = localStorage.getItem('user_info')
  return info ? JSON.parse(info) : null
}

// 设置用户信息
export function setUserInfo(info: any) {
  if (typeof window === 'undefined') return
  localStorage.setItem('user_info', JSON.stringify(info))
}

// 通用请求函数
async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const token = getToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(url, {
    ...options,
    headers,
  })

  const data = await response.json()

  if (!response.ok) {
    throw new Error(data.message || '请求失败')
  }

  return data as T
}

// 用户注册
export async function register(username: string, password: string, email?: string, phone?: string) {
  const data = await request<any>(`${API_USER}/register`, {
    method: 'POST',
    body: JSON.stringify({ username, password, email, phone }),
  })
  return data
}

// 用户登录
export async function login(username: string, password: string) {
  const data = await request<any>(`${API_USER}/login`, {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  if (data.success && data.token) {
    setToken(data.token)
    setUserInfo({
      userId: data.userId,
      username: data.username,
      email: data.email,
      phone: data.phone,
    })
  }
  return data
}

// 获取用户 profile（含计费信息）
export async function getUserProfile() {
  const data = await request<any>(`${API_USER}/profile`)
  return data
}

// 更新用户信息
export async function updateProfile(email?: string, phone?: string) {
  const data = await request<any>(`${API_USER}/profile`, {
    method: 'PUT',
    body: JSON.stringify({ email, phone }),
  })
  return data
}

// 修改密码
export async function changePassword(oldPassword: string, newPassword: string) {
  const data = await request<any>(`${API_USER}/change-password`, {
    method: 'POST',
    body: JSON.stringify({ oldPassword, newPassword }),
  })
  return data
}

// 查询余额
export async function getBalance() {
  const data = await request<any>(`${API_BILLING}/balance`)
  return data
}

// 激活充值码
export async function activateRechargeCode(code: string) {
  const data = await request<any>(`${API_BILLING}/recharge`, {
    method: 'POST',
    body: JSON.stringify({ code }),
  })
  return data
}

// 查询订阅状态
export async function getSubscriptionStatus() {
  const data = await request<any>(`${API_SUBSCRIPTION}/status`)
  return data
}

// 查询订阅历史
export async function getSubscriptionHistory(limit = 20) {
  const data = await request<any>(`${API_SUBSCRIPTION}/history?limit=${limit}`)
  return data
}
