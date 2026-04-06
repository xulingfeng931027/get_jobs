"use client";

import {X, User, Mail, Phone, Calendar, DollarSign, Activity, TrendingUp, Wallet, History} from "lucide-react";

interface UserDetailModalProps {
  user: any;
  isOpen: boolean;
  onClose: () => void;
  onBalanceClick?: () => void;
  onRecordsClick?: () => void;
}

export default function UserDetailModal({ user, isOpen, onClose, onBalanceClick, onRecordsClick }: UserDetailModalProps) {
  if (!isOpen || !user) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 遮罩层 */}
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />

      {/* 对话框 */}
      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-lg mx-4 animate-in fade-in zoom-in duration-200">
        {/* 头部 */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-bold text-gray-900">用户详情</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 内容 */}
        <div className="p-6 space-y-6">
          {/* 用户基本信息 */}
          <div className="flex items-center gap-4 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg border border-blue-200">
            <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-500 rounded-full flex items-center justify-center">
              <User className="w-8 h-8 text-white" />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-bold text-gray-900">{user.username}</h3>
              <p className="text-sm text-gray-600">ID: {user.id}</p>
            </div>
            <span
              className={`px-3 py-1 text-xs font-semibold rounded-full ${
                user.status === 1
                  ? "bg-green-100 text-green-700 border border-green-200"
                  : "bg-red-100 text-red-700 border border-red-200"
              }`}
            >
              {user.status === 1 ? "正常" : "禁用"}
            </span>
          </div>

          {/* 联系信息 */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wider">联系信息</h4>
            <div className="space-y-2">
              <div className="flex items-center gap-3 text-sm">
                <Mail className="w-4 h-4 text-gray-400" />
                <span className="text-gray-600">邮箱:</span>
                <span className="text-gray-900 font-medium">{user.email || "未设置"}</span>
              </div>
              <div className="flex items-center gap-3 text-sm">
                <Phone className="w-4 h-4 text-gray-400" />
                <span className="text-gray-600">手机号:</span>
                <span className="text-gray-900 font-medium">{user.phone || "未设置"}</span>
              </div>
              <div className="flex items-center gap-3 text-sm">
                <Calendar className="w-4 h-4 text-gray-400" />
                <span className="text-gray-600">注册时间:</span>
                <span className="text-gray-900 font-medium">
                  {new Date(user.createdAt).toLocaleString("zh-CN")}
                </span>
              </div>
            </div>
          </div>

          {/* 财务信息 */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wider">财务信息</h4>
              <div className="flex gap-2">
                {onBalanceClick && (
                  <button
                    onClick={onBalanceClick}
                    className="flex items-center gap-1 px-2 py-1 text-xs bg-green-50 text-green-700 rounded hover:bg-green-100 transition-colors"
                  >
                    <Wallet className="w-3 h-3" />
                    修改余额
                  </button>
                )}
                {onRecordsClick && (
                  <button
                    onClick={onRecordsClick}
                    className="flex items-center gap-1 px-2 py-1 text-xs bg-purple-50 text-purple-700 rounded hover:bg-purple-100 transition-colors"
                  >
                    <History className="w-3 h-3" />
                    查看记录
                  </button>
                )}
              </div>
            </div>
            <div className="grid grid-cols-3 gap-4">
              <div className="p-4 bg-green-50 rounded-lg border border-green-200">
                <div className="flex items-center gap-2 mb-2">
                  <DollarSign className="w-4 h-4 text-green-600" />
                  <span className="text-xs text-green-700">投递次数</span>
                </div>
                <p className="text-xl font-bold text-green-900">
                  {user.balance || 0}
                </p>
              </div>
              <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                <div className="flex items-center gap-2 mb-2">
                  <TrendingUp className="w-4 h-4 text-blue-600" />
                  <span className="text-xs text-blue-700">累计充值</span>
                </div>
                <p className="text-xl font-bold text-blue-900">
                  {user.totalRecharge || 0}
                </p>
              </div>
              <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                <div className="flex items-center gap-2 mb-2">
                  <Activity className="w-4 h-4 text-purple-600" />
                  <span className="text-xs text-purple-700">累计消费</span>
                </div>
                <p className="text-xl font-bold text-purple-900">
                  {user.totalConsumption || 0}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* 底部按钮 */}
        <div className="p-6 border-t border-gray-200">
          <button
            onClick={onClose}
            className="w-full px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium cursor-pointer"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  );
}
