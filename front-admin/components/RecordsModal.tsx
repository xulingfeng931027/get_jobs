"use client";

import { useState, useEffect } from "react";
import { X, History, TrendingUp, TrendingDown, Calendar, DollarSign } from "lucide-react";
import { getUserRechargeLogs, getUserConsumptionLogs } from "@/lib/api";

interface RecordsModalProps {
  userId: number;
  username: string;
  isOpen: boolean;
  onClose: () => void;
}

export default function RecordsModal({ userId, username, isOpen, onClose }: RecordsModalProps) {
  const [activeTab, setActiveTab] = useState<"recharge" | "consumption">("recharge");
  const [rechargeLogs, setRechargeLogs] = useState<any[]>([]);
  const [consumptionLogs, setConsumptionLogs] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && userId) {
      fetchRecords();
    }
  }, [isOpen, userId, activeTab]);

  const fetchRecords = async () => {
    setLoading(true);
    try {
      if (activeTab === "recharge") {
        const data = await getUserRechargeLogs(userId);
        setRechargeLogs(data || []);
      } else {
        const data = await getUserConsumptionLogs(userId);
        setConsumptionLogs(data || []);
      }
    } catch (err: any) {
      console.error("获取记录失败:", err);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const formatDateTime = (dateStr: string) => {
    if (!dateStr) return "-";
    return new Date(dateStr).toLocaleString("zh-CN");
  };

  const getConsumptionTypeText = (type: string) => {
    const typeMap: Record<string, string> = {
      application: "岗位投递",
      ai_match: "AI匹配",
      ai_greet: "AI打招呼",
      report: "数据报告",
    };
    return typeMap[type] || type;
  };

  const logs = activeTab === "recharge" ? rechargeLogs : consumptionLogs;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 遮罩层 */}
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />

      {/* 对话框 */}
      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-4xl mx-4 max-h-[90vh] flex flex-col animate-in fade-in zoom-in duration-200">
        {/* 头部 */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 flex-shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-500 rounded-lg flex items-center justify-center">
              <History className="w-5 h-5 text-white" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">用户记录</h2>
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

        {/* Tab 切换 */}
        <div className="flex border-b border-gray-200 flex-shrink-0">
          <button
            onClick={() => setActiveTab("recharge")}
            className={`flex-1 px-6 py-3 text-sm font-medium transition-colors flex items-center justify-center gap-2 ${
              activeTab === "recharge"
                ? "text-green-700 border-b-2 border-green-600 bg-green-50"
                : "text-gray-600 hover:text-gray-900 hover:bg-gray-50"
            }`}
          >
            <TrendingUp className="w-4 h-4" />
            充值记录 ({rechargeLogs.length})
          </button>
          <button
            onClick={() => setActiveTab("consumption")}
            className={`flex-1 px-6 py-3 text-sm font-medium transition-colors flex items-center justify-center gap-2 ${
              activeTab === "consumption"
                ? "text-red-700 border-b-2 border-red-600 bg-red-50"
                : "text-gray-600 hover:text-gray-900 hover:bg-gray-50"
            }`}
          >
            <TrendingDown className="w-4 h-4" />
            消费记录 ({consumptionLogs.length})
          </button>
        </div>

        {/* 内容区域 */}
        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="p-16 text-center">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent"></div>
              <p className="mt-4 text-gray-600">加载中...</p>
            </div>
          ) : logs.length === 0 ? (
            <div className="p-16 text-center">
              <History className="w-16 h-16 mx-auto text-gray-300 mb-4" />
              <p className="text-gray-500 text-lg">暂无{activeTab === "recharge" ? "充值" : "消费"}记录</p>
            </div>
          ) : (
            <div className="space-y-3">
              {logs.map((log: any, index: number) => (
                <div
                  key={index}
                  className={`p-4 rounded-lg border transition-all hover:shadow-md ${
                    activeTab === "recharge"
                      ? "bg-green-50 border-green-200 hover:border-green-300"
                      : "bg-red-50 border-red-200 hover:border-red-300"
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      {activeTab === "recharge" ? (
                        <>
                          <div className="flex items-center gap-2 mb-2">
                            <TrendingUp className="w-4 h-4 text-green-600" />
                            <span className="font-semibold text-green-900">
                              充值 +{log.amount || 0} 次
                            </span>
                            {log.bonus > 0 && (
                              <span className="text-xs bg-green-200 text-green-800 px-2 py-0.5 rounded-full">
                                赠送 +{log.bonus}
                              </span>
                            )}
                          </div>
                          <div className="text-sm text-green-700 space-y-1">
                            <p>充值码: {log.code}</p>
                            <p className="text-xs text-green-600">
                              余额变化: {log.balanceBefore || 0} → {log.balanceAfter || 0}
                            </p>
                          </div>
                        </>
                      ) : (
                        <>
                          <div className="flex items-center gap-2 mb-2">
                            <TrendingDown className="w-4 h-4 text-red-600" />
                            <span className="font-semibold text-red-900">
                              {getConsumptionTypeText(log.type)} -{log.amount || 0} 次
                            </span>
                          </div>
                          <div className="text-sm text-red-700 space-y-1">
                            {log.jobName && <p>岗位: {log.jobName}</p>}
                            {log.platform && <p>平台: {log.platform}</p>}
                            <p className="text-xs text-red-600">
                              余额变化: {log.balanceBefore || 0} → {log.balanceAfter || 0}
                            </p>
                          </div>
                        </>
                      )}
                    </div>
                    <div className="text-right flex-shrink-0 ml-4">
                      <div className="flex items-center gap-1 text-xs text-gray-500 mb-1">
                        <Calendar className="w-3 h-3" />
                        {formatDateTime(log.createdAt)}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 底部按钮 */}
        <div className="p-6 border-t border-gray-200 flex-shrink-0">
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
