"use client";

import { useEffect, useState } from "react";
import { apiRequest, getPackageDefinitions, getUserPackage, activatePackage, cancelPackage } from "@/lib/api";
import { Package, RefreshCw, Search, Calendar, Clock, CheckCircle, XCircle, AlertCircle, Gift, X } from "lucide-react";
import { useToast } from "@/components/Toast";

interface PackageDefinition {
  id: number;
  type: number;
  name: string;
  durationDays: number;
  maxCount: number;
  description: string;
}

interface UserPackage {
  hasActivePackage: boolean;
  packageType?: number;
  packageName?: string;
  totalCount?: number;
  usedCount?: number;
  remainingCount?: number;
  startDate?: string;
  endDate?: string;
}

interface UserPackageHistory {
  id: number;
  userId: number;
  packageType: number;
  status: number;
  totalCount: number;
  usedCount: number;
  startDate: string;
  endDate: string;
  createTime: string;
}

interface UserInfo {
  id: number;
  username: string;
}

const STATUS_MAP: Record<number, { label: string; class: string; icon: any }> = {
  0: { label: "未激活", class: "bg-gray-100 text-gray-600", icon: XCircle },
  1: { label: "激活", class: "bg-green-100 text-green-700", icon: CheckCircle },
  2: { label: "已用完", class: "bg-orange-100 text-orange-700", icon: AlertCircle },
  3: { label: "已过期", class: "bg-red-100 text-red-700", icon: XCircle },
};

const PACKAGE_TYPE_MAP: Record<number, string> = {
  1: "周套餐",
  2: "月套餐",
};

export default function PackagePage() {
  const { showToast } = useToast();
  const [users, setUsers] = useState<UserInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const [packageDefinitions, setPackageDefinitions] = useState<PackageDefinition[]>([]);

  // 开通套餐模态框
  const [showActivateModal, setShowActivateModal] = useState(false);
  const [activateUserId, setActivateUserId] = useState<number | null>(null);
  const [activateUsername, setActivateUsername] = useState("");
  const [selectedPackageType, setSelectedPackageType] = useState<number>(1);
  const [activating, setActivating] = useState(false);

  // 用户套餐详情
  const [userPackage, setUserPackage] = useState<UserPackage | null>(null);
  const [userPackageHistory, setUserPackageHistory] = useState<UserPackageHistory[]>([]);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);

  // 取消套餐
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelInfo, setCancelInfo] = useState<{ userId: number; packageId: number; username: string } | null>(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    fetchUsers();
    fetchPackageDefinitions();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await apiRequest<any>(`/api/admin/users?page=1&size=100`);
      setUsers(data.list || []);
    } catch (err) {
      console.error("获取用户列表失败:", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchPackageDefinitions = async () => {
    try {
      const definitions = await getPackageDefinitions();
      setPackageDefinitions(definitions || []);
    } catch (err) {
      console.error("获取套餐定义失败:", err);
    }
  };

  const handleSearch = () => {
    fetchUsers();
  };

  const filteredUsers = users.filter((user) =>
    user.username?.toLowerCase().includes(keyword.toLowerCase())
  );

  const handleActivateClick = (user: UserInfo) => {
    setActivateUserId(user.id);
    setActivateUsername(user.username);
    setSelectedPackageType(1);
    setShowActivateModal(true);
  };

  const handleActivateSubmit = async () => {
    if (!activateUserId) return;
    setActivating(true);
    try {
      await activatePackage(activateUserId, selectedPackageType);
      showToast("套餐开通成功", "success");
      setShowActivateModal(false);
      fetchUsers();
    } catch (err: any) {
      showToast("开通失败: " + err.message, "error");
    } finally {
      setActivating(false);
    }
  };

  const handleViewPackage = async (userId: number, username: string) => {
    setDetailLoading(true);
    setActivateUsername(username);
    setShowDetailModal(true);
    try {
      const data = await getUserPackage(userId);
      setUserPackage(data);
      setUserPackageHistory(data.history || []);
    } catch (err: any) {
      showToast("获取套餐信息失败: " + err.message, "error");
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCancelClick = (userId: number, packageId: number, username: string) => {
    setCancelInfo({ userId, packageId, username });
    setShowCancelModal(true);
  };

  const handleCancelConfirm = async () => {
    if (!cancelInfo) return;
    setCancelling(true);
    try {
      await cancelPackage(cancelInfo.userId, cancelInfo.packageId);
      showToast("套餐已取消", "success");
      setShowCancelModal(false);
      setCancelInfo(null);
      // 刷新详情
      if (userPackage) {
        const data = await getUserPackage(activateUserId!);
        setUserPackage(data);
        setUserPackageHistory(data.history || []);
      }
    } catch (err: any) {
      showToast("取消失败: " + err.message, "error");
    } finally {
      setCancelling(false);
    }
  };

  const formatDate = (dateStr: string | undefined) => {
    if (!dateStr) return "-";
    return new Date(dateStr).toLocaleDateString("zh-CN");
  };

  const formatDateTime = (dateStr: string | undefined) => {
    if (!dateStr) return "-";
    return new Date(dateStr).toLocaleString("zh-CN");
  };

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">套餐管理</h1>
          <p className="text-gray-600 mt-1">为用户开通周/月投递套餐，套餐期间投递不收费</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-4 py-2 bg-purple-50 text-purple-700 rounded-lg border border-purple-200">
            <Gift className="w-4 h-4" />
            <span className="text-sm font-medium">套餐优先于额度限制</span>
          </div>
        </div>
      </div>

      {/* 套餐类型说明 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">可用套餐类型</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {packageDefinitions.map((pkg) => (
            <div key={pkg.type} className="flex items-center gap-4 p-4 bg-gradient-to-r from-purple-50 to-indigo-50 rounded-lg border border-purple-100">
              <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-xl flex items-center justify-center">
                <Package className="w-6 h-6 text-white" />
              </div>
              <div className="flex-1">
                <div className="font-semibold text-gray-900">{pkg.name}</div>
                <div className="text-sm text-gray-600 flex items-center gap-2">
                  <Clock className="w-3 h-3" />
                  {pkg.durationDays} 天
                  {pkg.maxCount === -1 && " · 无限投递次数"}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 用户筛选 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
        <div className="flex gap-4 flex-wrap">
          <div className="flex items-center gap-2 flex-1 min-w-[200px]">
            <Search className="w-4 h-4 text-gray-400" />
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="搜索用户名"
              className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            />
          </div>
          <button
            onClick={handleSearch}
            className="flex items-center gap-1 bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition-colors text-sm font-medium"
          >
            <RefreshCw className="w-4 h-4" />
            搜索
          </button>
        </div>
      </div>

      {/* 用户列表 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {loading ? (
          <div className="p-16 text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-purple-600 border-t-transparent"></div>
            <p className="mt-4 text-gray-600">加载中...</p>
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-16 text-center">
            <Package className="w-16 h-16 mx-auto text-gray-300 mb-4" />
            <p className="text-gray-500 text-lg">暂无用户数据</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                    用户
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                    套餐状态
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                    使用情况
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                    到期时间
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                    操作
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-full flex items-center justify-center">
                          <span className="text-white font-semibold text-xs">
                            {user.username?.charAt(0)?.toUpperCase() || 'U'}
                          </span>
                        </div>
                        <div>
                          <div className="text-sm font-medium text-gray-900">{user.username}</div>
                          <div className="text-xs text-gray-500">ID: {user.id}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {userPackage && user.id === activateUserId && userPackage.hasActivePackage ? (
                        <span className="inline-flex items-center gap-1 px-3 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-700 border border-green-200">
                          <CheckCircle className="w-3 h-3" />
                          {userPackage.packageName || "套餐中"}
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-3 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-600 border border-gray-200">
                          <XCircle className="w-3 h-3" />
                          无套餐
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {userPackage && user.id === activateUserId && userPackage.hasActivePackage ? (
                        <span>
                          {userPackage.usedCount || 0} / {userPackage.totalCount === -1 ? "∞" : userPackage.totalCount}
                        </span>
                      ) : (
                        <span className="text-gray-400">-</span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2 text-sm text-gray-600">
                        <Calendar className="w-4 h-4 text-gray-400" />
                        {userPackage && user.id === activateUserId && userPackage.endDate
                          ? formatDate(userPackage.endDate)
                          : "-"}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleViewPackage(user.id, user.username)}
                          className="flex items-center gap-1 px-3 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs bg-purple-50 text-purple-700 hover:bg-purple-100 border border-purple-200"
                        >
                          查看详情
                        </button>
                        <button
                          onClick={() => handleActivateClick(user)}
                          className="flex items-center gap-1 px-3 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-200"
                        >
                          <Gift className="w-3 h-3" />
                          开通套餐
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* 开通套餐模态框 */}
      {showActivateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowActivateModal(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 animate-in fade-in zoom-in duration-200">
            <div className="flex items-center justify-between p-6 border-b border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-lg flex items-center justify-center">
                  <Gift className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h2 className="text-xl font-bold text-gray-900">开通套餐</h2>
                  <p className="text-sm text-gray-600">用户: {activateUsername}</p>
                </div>
              </div>
              <button onClick={() => setShowActivateModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4">
              <div className="flex items-start gap-3 p-4 bg-purple-50 rounded-lg border border-purple-200">
                <AlertCircle className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                <div className="text-sm text-purple-800">
                  <p className="font-semibold">套餐说明</p>
                  <ul className="list-disc list-inside mt-1 space-y-1">
                    <li>套餐期间投递不收费，只统计投递次数</li>
                    <li>套餐优先于原有的额度限制</li>
                    <li>套餐到期后自动恢复余额模式</li>
                  </ul>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">选择套餐类型</label>
                <div className="space-y-2">
                  {packageDefinitions.map((pkg) => (
                    <label
                      key={pkg.type}
                      className={`flex items-center gap-3 p-4 rounded-lg border-2 cursor-pointer transition-all ${
                        selectedPackageType === pkg.type
                          ? "border-purple-500 bg-purple-50"
                          : "border-gray-200 hover:border-purple-300"
                      }`}
                    >
                      <input
                        type="radio"
                        name="packageType"
                        value={pkg.type}
                        checked={selectedPackageType === pkg.type}
                        onChange={() => setSelectedPackageType(pkg.type)}
                        className="w-4 h-4 text-purple-600"
                      />
                      <div className="flex-1">
                        <div className="font-medium text-gray-900">{pkg.name}</div>
                        <div className="text-sm text-gray-500">
                          {pkg.durationDays} 天 · {pkg.maxCount === -1 ? "无限投递" : `最多 ${pkg.maxCount} 次`}
                        </div>
                      </div>
                    </label>
                  ))}
                </div>
              </div>
            </div>

            <div className="p-6 border-t border-gray-200 flex gap-3">
              <button
                onClick={() => setShowActivateModal(false)}
                className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium"
                disabled={activating}
              >
                取消
              </button>
              <button
                onClick={handleActivateSubmit}
                disabled={activating}
                className="flex-1 px-4 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-lg hover:from-purple-700 hover:to-indigo-700 transition-all font-medium disabled:opacity-50"
              >
                {activating ? "开通中..." : "确认开通"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 套餐详情模态框 */}
      {showDetailModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowDetailModal(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 max-h-[80vh] overflow-hidden flex flex-col animate-in fade-in zoom-in duration-200">
            <div className="flex items-center justify-between p-6 border-b border-gray-200 flex-shrink-0">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-lg flex items-center justify-center">
                  <Package className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h2 className="text-xl font-bold text-gray-900">用户套餐详情</h2>
                  <p className="text-sm text-gray-600">用户: {activateUsername}</p>
                </div>
              </div>
              <button onClick={() => setShowDetailModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-6">
              {detailLoading ? (
                <div className="p-8 text-center">
                  <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-purple-600 border-t-transparent"></div>
                  <p className="mt-4 text-gray-600">加载中...</p>
                </div>
              ) : (
                <>
                  {/* 当前套餐状态 */}
                  <div className="mb-6">
                    <h3 className="text-sm font-semibold text-gray-700 mb-3">当前套餐</h3>
                    {userPackage?.hasActivePackage ? (
                      <div className="p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg border border-green-200">
                        <div className="flex items-center justify-between mb-3">
                          <div className="flex items-center gap-2">
                            <CheckCircle className="w-5 h-5 text-green-600" />
                            <span className="font-semibold text-green-800">{userPackage.packageName}</span>
                          </div>
                          <span className="text-sm text-green-600">
                            剩余 {userPackage.remainingCount === -1 ? "无限" : userPackage.remainingCount} 次
                          </span>
                        </div>
                        <div className="grid grid-cols-2 gap-4 text-sm">
                          <div>
                            <span className="text-gray-500">开始时间：</span>
                            <span className="text-gray-900">{formatDateTime(userPackage.startDate)}</span>
                          </div>
                          <div>
                            <span className="text-gray-500">到期时间：</span>
                            <span className="text-gray-900">{formatDateTime(userPackage.endDate)}</span>
                          </div>
                          <div>
                            <span className="text-gray-500">已使用：</span>
                            <span className="text-gray-900">{userPackage.usedCount} 次</span>
                          </div>
                          <div>
                            <span className="text-gray-500">总次数：</span>
                            <span className="text-gray-900">{userPackage.totalCount === -1 ? "无限" : userPackage.totalCount}</span>
                          </div>
                        </div>
                        {userPackageHistory.length > 0 && userPackageHistory[0] && (
                          <button
                            onClick={() => handleCancelClick(userPackage!.usedCount!, userPackageHistory[0].id, activateUsername)}
                            className="mt-3 text-sm text-red-600 hover:text-red-700"
                          >
                            取消当前套餐
                          </button>
                        )}
                      </div>
                    ) : (
                      <div className="p-4 bg-gray-50 rounded-lg border border-gray-200 text-center">
                        <XCircle className="w-8 h-8 mx-auto text-gray-400 mb-2" />
                        <p className="text-gray-500">该用户暂无有效套餐</p>
                      </div>
                    )}
                  </div>

                  {/* 历史套餐 */}
                  {userPackageHistory.length > 0 && (
                    <div>
                      <h3 className="text-sm font-semibold text-gray-700 mb-3">套餐历史</h3>
                      <div className="space-y-3">
                        {userPackageHistory.map((pkg) => {
                          const statusInfo = STATUS_MAP[pkg.status] || STATUS_MAP[0];
                          const StatusIcon = statusInfo.icon;
                          return (
                            <div key={pkg.id} className="p-4 bg-gray-50 rounded-lg border border-gray-200">
                              <div className="flex items-center justify-between mb-2">
                                <div className="flex items-center gap-2">
                                  <span className="font-medium text-gray-900">
                                    {PACKAGE_TYPE_MAP[pkg.packageType] || "未知套餐"}
                                  </span>
                                  <span className={`inline-flex items-center gap-1 px-2 py-0.5 text-xs font-semibold rounded-full ${statusInfo.class}`}>
                                    <StatusIcon className="w-3 h-3" />
                                    {statusInfo.label}
                                  </span>
                                </div>
                                {pkg.status === 1 && (
                                  <button
                                    onClick={() => handleCancelClick(pkg.userId, pkg.id, activateUsername)}
                                    className="text-sm text-red-600 hover:text-red-700"
                                  >
                                    取消
                                  </button>
                                )}
                              </div>
                              <div className="grid grid-cols-2 gap-2 text-sm text-gray-600">
                                <div>使用: {pkg.usedCount} / {pkg.totalCount === -1 ? "∞" : pkg.totalCount}</div>
                                <div>有效期: {formatDate(pkg.startDate)} ~ {formatDate(pkg.endDate)}</div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 取消确认模态框 */}
      {showCancelModal && cancelInfo && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowCancelModal(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-sm mx-4 animate-in fade-in zoom-in duration-200">
            <div className="p-6">
              <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <AlertCircle className="w-6 h-6 text-red-600" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 text-center mb-2">确认取消套餐</h3>
              <p className="text-gray-600 text-center mb-6">
                确定要为用户 <strong>{cancelInfo.username}</strong> 取消该套餐吗？取消后立即生效。
              </p>
              <div className="flex gap-3">
                <button
                  onClick={() => setShowCancelModal(false)}
                  className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium"
                  disabled={cancelling}
                >
                  取消
                </button>
                <button
                  onClick={handleCancelConfirm}
                  disabled={cancelling}
                  className="flex-1 px-4 py-2.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-medium disabled:opacity-50"
                >
                  {cancelling ? "取消中..." : "确认取消"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
