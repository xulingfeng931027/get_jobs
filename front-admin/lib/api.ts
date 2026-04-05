// API 基础配置
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:18888";

// 通用请求函数
export async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("admin_token") : null;

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "网络请求失败" }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  const data = await response.json();

  if (!data.success) {
    throw new Error(data.message || "请求失败");
  }

  return data.data;
}

// 登录
export async function login(username: string, password: string) {
  return apiRequest<{ token: string; username: string }>("/api/admin/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

// 验证 Token
export async function verifyToken() {
  return apiRequest<{ userId: number; username: string }>("/api/admin/auth/verify", {
    headers: {
      Authorization: `Bearer ${typeof window !== "undefined" ? localStorage.getItem("admin_token") : ""}`,
    },
  });
}
