// API 基础配置
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:18888";

// 通用请求函数
export async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("admin_token") : null;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
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

// 用户管理 API
export async function createUser(data: {
  username: string;
  email?: string;
  phone?: string;
  password: string;
}) {
  return apiRequest("/api/admin/users", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateUser(
  id: number,
  data: {
    email?: string;
    phone?: string;
    status?: number;
  }
) {
  return apiRequest(`/api/admin/users/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteUser(id: number) {
  return apiRequest(`/api/admin/users/${id}`, {
    method: "DELETE",
  });
}

export async function getUserDetail(id: number) {
  return apiRequest<any>(`/api/admin/users/${id}`);
}

// 用户余额管理 API
export async function updateUserBalance(
  id: number,
  data: {
    applicationCount?: number;
    aiMatchCount?: number;
    aiGreetCount?: number;
    reportCount?: number;
    reason: string;
  }
) {
  return apiRequest(`/api/admin/users/${id}/balance`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

// 充值记录 API
export async function getUserRechargeLogs(id: number) {
  return apiRequest<any[]>(`/api/admin/users/${id}/recharge-logs`);
}

// 消费记录 API
export async function getUserConsumptionLogs(id: number) {
  return apiRequest<any[]>(`/api/admin/users/${id}/consumption-logs`);
}
