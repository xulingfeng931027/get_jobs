"use client";

import {useState, useEffect} from "react";
import {X, User, Mail, Phone, Lock} from "lucide-react";

interface UserFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: any) => Promise<void>;
  initialData?: any;
  mode: "create" | "edit";
}

export default function UserFormModal({ isOpen, onClose, onSubmit, initialData, mode }: UserFormModalProps) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (initialData && mode === "edit") {
      setUsername(initialData.username || "");
      setEmail(initialData.email || "");
      setPhone(initialData.phone || "");
      setPassword("");
    } else {
      setUsername("");
      setEmail("");
      setPhone("");
      setPassword("");
    }
    setError("");
  }, [initialData, mode, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (mode === "create" && !password) {
      setError("请输入密码");
      return;
    }

    if (mode === "create" && password.length < 6) {
      setError("密码长度至少6位");
      return;
    }

    setLoading(true);

    try {
      await onSubmit({
        username,
        email,
        phone,
        password: mode === "create" ? password : undefined,
      });
      
      // 重置表单
      setUsername("");
      setEmail("");
      setPhone("");
      setPassword("");
      onClose();
    } catch (err: any) {
      setError(err.message || "操作失败");
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 遮罩层 */}
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />

      {/* 对话框 */}
      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 animate-in fade-in zoom-in duration-200">
        {/* 头部 */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-bold text-gray-900">
            {mode === "create" ? "新增用户" : "编辑用户"}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 表单 */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* 用户名 */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              用户名 <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <User className="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="请输入用户名"
                required
                disabled={mode === "edit"}
              />
            </div>
          </div>

          {/* 邮箱 */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              邮箱
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Mail className="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="请输入邮箱（选填）"
              />
            </div>
          </div>

          {/* 手机号 */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              手机号
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Phone className="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="请输入手机号（选填）"
              />
            </div>
          </div>

          {/* 密码 */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              密码 {mode === "create" && <span className="text-red-500">*</span>}
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock className="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder={mode === "create" ? "请输入密码（至少6位）" : "留空则不修改密码"}
                required={mode === "create"}
              />
            </div>
          </div>

          {/* 按钮 */}
          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium cursor-pointer"
            >
              取消
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed font-medium cursor-pointer"
            >
              {loading ? "提交中..." : mode === "create" ? "创建" : "保存"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
