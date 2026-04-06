"use client";

import { useState, useEffect } from "react";
import { X, Wallet, AlertCircle } from "lucide-react";
import { updateUserBalance } from "@/lib/api";
import { useToast } from "@/components/Toast";

interface BalanceModalProps {
  userId: number;
  username: string;
  currentBalance: any;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function BalanceModal({
  userId,
  username,
  currentBalance,
  isOpen,
  onClose,
  onSuccess,
}: BalanceModalProps) {
  const { showToast } = useToast();
  const [applicationCount, setApplicationCount] = useState("");
  const [aiMatchCount, setAiMatchCount] = useState("");
  const [aiGreetCount, setAiGreetCount] = useState("");
  const [reportCount, setReportCount] = useState("");
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && currentBalance) {
      setApplicationCount(currentBalance.applicationCount?.toString() || "");
      setAiMatchCount(currentBalance.aiMatchCount?.toString() || "");
      setAiGreetCount(currentBalance.aiGreetCount?.toString() || "");
      setReportCount(currentBalance.reportCount?.toString() || "");
      setReason("");
    }
  }, [isOpen, currentBalance]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await updateUserBalance(userId, {
        applicationCount: applicationCount ? parseInt(applicationCount) : undefined,
        aiMatchCount: aiMatchCount ? parseInt(aiMatchCount) : undefined,
        aiGreetCount: aiGreetCount ? parseInt(aiGreetCount) : undefined,
        reportCount: reportCount ? parseInt(reportCount) : undefined,
        reason: reason || "管理员手动调整",
      });

      showToast("余额修改成功", "success");
      onSuccess();
      onClose();
    } catch (err: any) {
      showToast("修改失败: " + err.message, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 遮罩层 */}
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />

      {/* 对话框 */}
      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 animate-in fade-in zoom-in duration-200">
        {/* 头部 */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-emerald-500 rounded-lg flex items-center justify-center">
              <Wallet className="w-5 h-5 text-white" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">修改用户余额</h2>
              <p className="text-sm text-gray-600">用户: {username}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 内容 */}
        <form onSubmit={handleSubmit}>
          <div className="p-6 space-y-6">
            {/* 提示信息 */}
            <div className="flex items-start gap-3 p-4 bg-yellow-50 rounded-lg border border-yellow-200">
              <AlertCircle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
              <div className="text-sm text-yellow-800">
                <p className="font-semibold mb-1">注意事项</p>
                <ul className="list-disc list-inside space-y-1 text-yellow-700">
                  <li>修改余额会立即生效，请谨慎操作</li>
                  <li>建议填写修改原因以便后续追溯</li>
                  <li>留空的字段将保持原值不变</li>
                </ul>
              </div>
            </div>

            {/* 余额字段 */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  投递次数
                </label>
                <input
                  type="number"
                  value={applicationCount}
                  onChange={(e) => setApplicationCount(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="留空保持不变"
                  min="0"
                />
                {currentBalance && (
                  <p className="text-xs text-gray-500 mt-1">
                    当前: {currentBalance.applicationCount || 0}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  AI匹配次数
                </label>
                <input
                  type="number"
                  value={aiMatchCount}
                  onChange={(e) => setAiMatchCount(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="留空保持不变"
                  min="0"
                />
                {currentBalance && (
                  <p className="text-xs text-gray-500 mt-1">
                    当前: {currentBalance.aiMatchCount || 0}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  AI打招呼次数
                </label>
                <input
                  type="number"
                  value={aiGreetCount}
                  onChange={(e) => setAiGreetCount(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="留空保持不变"
                  min="0"
                />
                {currentBalance && (
                  <p className="text-xs text-gray-500 mt-1">
                    当前: {currentBalance.aiGreetCount || 0}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  数据报告次数
                </label>
                <input
                  type="number"
                  value={reportCount}
                  onChange={(e) => setReportCount(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="留空保持不变"
                  min="0"
                />
                {currentBalance && (
                  <p className="text-xs text-gray-500 mt-1">
                    当前: {currentBalance.reportCount || 0}
                  </p>
                )}
              </div>
            </div>

            {/* 修改原因 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                修改原因 <span className="text-gray-400">(选填)</span>
              </label>
              <textarea
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                rows={3}
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent"
                placeholder="例如：用户投诉补偿、活动赠送等"
              />
            </div>
          </div>

          {/* 底部按钮 */}
          <div className="p-6 border-t border-gray-200 flex gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium cursor-pointer"
              disabled={loading}
            >
              取消
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2.5 bg-gradient-to-r from-green-600 to-emerald-600 text-white rounded-lg hover:from-green-700 hover:to-emerald-700 transition-all font-medium cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "修改中..." : "确认修改"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
