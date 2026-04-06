"use client";

import {useEffect, useState} from "react";
import {apiRequest, createUser, updateUser, deleteUser, getUserDetail, getUserRechargeLogs} from "@/lib/api";
import {Ban, CheckCircle, RefreshCw, Search, Users, Mail, Phone, Calendar, DollarSign, Plus, Eye, Edit, Trash2, Wallet, History} from "lucide-react";
import UserFormModal from "@/components/UserFormModal";
import UserDetailModal from "@/components/UserDetailModal";
import DeleteConfirmModal from "@/components/DeleteConfirmModal";
import BalanceModal from "@/components/BalanceModal";
import RecordsModal from "@/components/RecordsModal";
import {useToast} from "@/components/Toast";

export default function UsersPage() {
  const { showToast } = useToast();
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState<number | null>(null);

  // 对话框状态
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showBalanceModal, setShowBalanceModal] = useState(false);
  const [showRecordsModal, setShowRecordsModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState<any>(null);
  const [userBalance, setUserBalance] = useState<any>(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (keyword) params.set("keyword", keyword);
      if (status !== null) params.set("status", status.toString());

      const data = await apiRequest<any>(`/api/admin/users?${params}`);
      setUsers(data.list || []);
      setTotal(data.total || 0);
    } catch (err) {
      console.error("获取用户列表失败:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [page, status]);

  const toggleStatus = async (userId: number, currentStatus: number) => {
    try {
      const action = currentStatus === 1 ? "disable" : "enable";
      await apiRequest(`/api/admin/users/${userId}/${action}`, {
        method: "POST",
      });
      showToast("操作成功", "success");
      fetchUsers();
    } catch (err: any) {
      showToast("操作失败: " + err.message, "error");
    }
  };

  // CRUD 操作函数
  const handleCreate = async (data: any) => {
    await createUser(data);
    showToast("用户创建成功", "success");
    fetchUsers();
  };

  const handleEdit = async (data: any) => {
    if (!selectedUser) return;
    await updateUser(selectedUser.id, data);
    showToast("用户更新成功", "success");
    fetchUsers();
  };

  const handleViewDetail = async (userId: number) => {
    try {
      const detail = await getUserDetail(userId);
      setSelectedUser(detail);
      setShowDetailModal(true);
    } catch (err: any) {
      showToast("获取用户详情失败: " + err.message, "error");
    }
  };

  const handleEditClick = (user: any) => {
    setSelectedUser(user);
    setShowEditModal(true);
  };

  const handleDeleteClick = (user: any) => {
    setSelectedUser(user);
    setShowDeleteModal(true);
  };

  const handleDeleteConfirm = async () => {
    if (!selectedUser) return;
    try {
      await deleteUser(selectedUser.id);
      showToast("用户删除成功", "success");
      setShowDeleteModal(false);
      setSelectedUser(null);
      fetchUsers();
    } catch (err: any) {
      showToast("删除失败: " + err.message, "error");
    }
  };
  
  const handleBalanceClick = async (user: any) => {
    try {
      const detail = await getUserDetail(user.id);
      setSelectedUser(user);
      setUserBalance({
        applicationCount: detail.balance,
        aiMatchCount: detail.aiMatchCount,
        aiGreetCount: detail.aiGreetCount,
        reportCount: detail.reportCount,
      });
      setShowBalanceModal(true);
    } catch (err: any) {
      showToast("获取用户余额失败: " + err.message, "error");
    }
  };
  
  const handleRecordsClick = (user: any) => {
    setSelectedUser(user);
    setShowRecordsModal(true);
  };
  
  const handleBalanceSuccess = () => {
    fetchUsers();
  };

  const totalPages = Math.ceil(total / 20);

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">用户管理</h1>
          <p className="text-gray-600 mt-1">管理系统用户，查看用户信息和状态</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-4 py-2 bg-blue-50 text-blue-700 rounded-lg border border-blue-200">
            <Users className="w-4 h-4" />
            <span className="text-sm font-medium">共 {total} 个用户</span>
          </div>
          <button
            onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg hover:bg-blue-700 transition-all duration-200 shadow-sm hover:shadow-md font-medium cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            新增用户
          </button>
        </div>
      </div>

      {/* 筛选 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
        <div className="flex gap-4 flex-wrap">
          <div className="flex items-center gap-2 flex-1 min-w-[200px]">
            <Search className="w-4 h-4 text-gray-400" />
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchUsers()}
              placeholder="搜索用户名/邮箱/手机号"
              className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">状态:</span>
            <select
              value={status === null ? "" : status}
              onChange={(e) => setStatus(e.target.value === "" ? null : Number(e.target.value))}
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">全部</option>
              <option value={1}>正常</option>
              <option value={0}>禁用</option>
            </select>
          </div>
          <button
            onClick={fetchUsers}
            className="flex items-center gap-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
          >
            <RefreshCw className="w-4 h-4" />
            搜索
          </button>
        </div>
      </div>

      {/* 列表 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {loading ? (
          <div className="p-16 text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent"></div>
            <p className="mt-4 text-gray-600">加载中...</p>
          </div>
        ) : users.length === 0 ? (
          <div className="p-16 text-center">
            <Users className="w-16 h-16 mx-auto text-gray-300 mb-4" />
            <p className="text-gray-500 text-lg">暂无用户数据</p>
            <p className="text-gray-400 text-sm mt-1">试试调整筛选条件</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      ID
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      用户名
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      邮箱
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      手机号
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      余额
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      累计充值
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      状态
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      注册时间
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      操作
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {users.map((user) => (
                    <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                        {user.id}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-indigo-500 rounded-full flex items-center justify-center">
                            <span className="text-white font-semibold text-xs">
                              {user.username?.charAt(0)?.toUpperCase() || 'U'}
                            </span>
                          </div>
                          <span className="text-sm font-medium text-gray-900">{user.username}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <Mail className="w-4 h-4 text-gray-400" />
                          {user.email || "-"}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <Phone className="w-4 h-4 text-gray-400" />
                          {user.phone || "-"}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-1 text-sm font-medium text-gray-900">
                          <DollarSign className="w-4 h-4 text-gray-400" />
                          ¥{(user.balance || 0) / 100}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        ¥{(user.totalRecharge || 0) / 100}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`inline-flex items-center gap-1 px-3 py-1 text-xs font-semibold rounded-full ${
                            user.status === 1
                              ? "bg-green-100 text-green-700 border border-green-200"
                              : "bg-red-100 text-red-700 border border-red-200"
                          }`}
                        >
                          {user.status === 1 ? (
                            <>
                              <CheckCircle className="w-3 h-3" />
                              正常
                            </>
                          ) : (
                            <>
                              <Ban className="w-3 h-3" />
                              禁用
                            </>
                          )}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2 text-sm text-gray-500">
                          <Calendar className="w-4 h-4 text-gray-400" />
                          {new Date(user.createdAt).toLocaleDateString("zh-CN")}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => handleViewDetail(user.id)}
                            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs bg-blue-50 text-blue-700 hover:bg-blue-100 border border-blue-200"
                            title="查看详情"
                          >
                            <Eye className="w-3 h-3" />
                            查看
                          </button>
                          <button
                            onClick={() => handleBalanceClick(user)}
                            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs bg-green-50 text-green-700 hover:bg-green-100 border border-green-200"
                            title="修改余额"
                          >
                            <Wallet className="w-3 h-3" />
                            余额
                          </button>
                          <button
                            onClick={() => handleRecordsClick(user)}
                            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs bg-purple-50 text-purple-700 hover:bg-purple-100 border border-purple-200"
                            title="查看记录"
                          >
                            <History className="w-3 h-3" />
                            记录
                          </button>
                          <button
                            onClick={() => handleEditClick(user)}
                            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-200"
                            title="编辑"
                          >
                            <Edit className="w-3 h-3" />
                            编辑
                          </button>
                          <button
                            onClick={() => handleDeleteClick(user)}
                            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs bg-red-50 text-red-700 hover:bg-red-100 border border-red-200"
                            title="删除"
                          >
                            <Trash2 className="w-3 h-3" />
                            删除
                          </button>
                          <button
                            onClick={() => toggleStatus(user.id, user.status)}
                            className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-all duration-200 font-medium text-xs ${
                              user.status === 1
                                ? "bg-yellow-50 text-yellow-700 hover:bg-yellow-100 border border-yellow-200"
                                : "bg-green-50 text-green-700 hover:bg-green-100 border border-green-200"
                            }`}
                            title={user.status === 1 ? "禁用" : "启用"}
                          >
                            {user.status === 1 ? (
                              <>
                                <Ban className="w-3 h-3" />
                                禁用
                              </>
                            ) : (
                              <>
                                <CheckCircle className="w-3 h-3" />
                                启用
                              </>
                            )}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* 分页 */}
            <div className="bg-gray-50 px-6 py-4 flex justify-between items-center border-t border-gray-200">
              <span className="text-sm text-gray-600">
                共 <span className="font-medium">{total}</span> 条记录，第 <span className="font-medium">{page}</span> / <span className="font-medium">{totalPages || 1}</span> 页
              </span>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                  disabled={page === 1}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm font-medium"
                >
                  上一页
                </button>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page >= totalPages}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm font-medium"
                >
                  下一页
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      {/* 新增用户模态框 */}
      <UserFormModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreate}
        mode="create"
      />

      {/* 编辑用户模态框 */}
      <UserFormModal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        onSubmit={handleEdit}
        initialData={selectedUser}
        mode="edit"
      />

      {/* 用户详情模态框 */}
      <UserDetailModal
        isOpen={showDetailModal}
        onClose={() => setShowDetailModal(false)}
        user={selectedUser}
        onBalanceClick={() => {
          setShowDetailModal(false);
          handleBalanceClick(selectedUser);
        }}
        onRecordsClick={() => {
          setShowDetailModal(false);
          handleRecordsClick(selectedUser);
        }}
      />

      {/* 删除确认模态框 */}
      <DeleteConfirmModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={handleDeleteConfirm}
        userName={selectedUser?.username}
      />
      
      {/* 余额修改模态框 */}
      <BalanceModal
        isOpen={showBalanceModal}
        onClose={() => setShowBalanceModal(false)}
        userId={selectedUser?.id}
        username={selectedUser?.username}
        currentBalance={userBalance}
        onSuccess={handleBalanceSuccess}
      />
      
      {/* 记录查看模态框 */}
      <RecordsModal
        isOpen={showRecordsModal}
        onClose={() => setShowRecordsModal(false)}
        userId={selectedUser?.id}
        username={selectedUser?.username}
      />
    </div>
  );
}
