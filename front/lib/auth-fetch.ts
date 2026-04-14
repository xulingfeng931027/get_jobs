import { getToken } from './auth-api'

export async function authFetch(url: string, options: RequestInit = {}): Promise<Response> {
  const token = getToken()
  return fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers as Record<string, string>),
    },
  })
}
