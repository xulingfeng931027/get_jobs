"use client";

import {useEffect, useState} from "react";
import {apiRequest} from "@/lib/api";
import {Plus, RefreshCw, CreditCard, Calendar, DollarSign, Gift, Hash} from "lucide-react";
import Link from "next/link";

export default function RechargePage() {
  const [codes, setCodes] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [batchNo, setBatchNo] = useState("");
  const [status, setStatus] = useState<number | null>(null);

  const fetchCodes = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (batchNo) params.set("batchNo", batchNo);
      if (status !== null) params.set("status", status.toString());

      const data = await apiRequest<any>(`/api/admin/recharge-codes?${params}`);
      setCodes(data.list || []);
      setTotal(data.total || 0);
    } catch (err) {
      console.error("获取充值码列表失败:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCodes();
  }, [page, status]);

  const statusMap: Record<number, { label: string; color: string; bgColor: string; borderColor: string }> = {
    0: { label: "未激活", color: "text-gray-700", bgColor: "bg-gray-100", borderColor: "border-gray-200" },
    1: { label: "已激活", color: "text-green-700", bgColor: "bg-green-100", borderColor: "border-green-200" },
    2: { label: "已冻结", color: "text-yellow-700", bgColor: "bg-yellow-100", borderColor: "border-yellow-200" },
    3: { label: "已作废", color: "text-red-700", bgColor: "bg-red-100", borderColor: "border-red-200" },
  };

  const totalPages = Math.ceil(total / 20);

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">充值码管理</h1>
          <p className="text-gray-600 mt-1">管理和生成充值码</p>
        </div>
        <Link
          href="/recharge/generate"
          className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg hover:bg-blue-700 transition-all duration-200 shadow-sm hover:shadow-md font-medium"
        >
          <Plus className="w-4 h-4" />
          批量生成
        </Link>
      </div>

      {/* 筛选 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
        <div className="flex gap-4 flex-wrap">
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">状态:</span>
            <select
              value={status === null ? "" : status}
              onChange={(e) => setStatus(e.target.value === "" ? null : Number(e.target.value))}
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">全部</option>
              <option value={0}>未激活</option>
              <option value={1}>已激活</option>
              <option value={2}>已冻结</option>
              <option value={3}>已作废</option>
            </select>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">批次号:</span>
            <input
              type="text"
              value={batchNo}
              onChange={(e) => setBatchNo(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchCodes()}
              placeholder="输入批次号搜索"
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button
            onClick={fetchCodes}
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
        ) : codes.length === 0 ? (
          <div className="p-16 text-center">
            <CreditCard className="w-16 h-16 mx-auto text-gray-300 mb-4" />
            <p className="text-gray-500 text-lg">暂无充值码数据</p>
            <p className="text-gray-400 text-sm mt-1">试试调整筛选条件或生成新的充值码</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      充值码
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      金额
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      赠送
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      状态
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      批次号
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                      创建时间
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {codes.map((code) => (
                    <tr key={code.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          <Hash className="w-4 h-4 text-gray-400" />
                          <span className="text-sm font-mono font-medium text-gray-900">{code.code}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-1 text-sm font-semibold text-gray-900">
                          <DollarSign className="w-4 h-4 text-gray-400" />
                          ¥{code.amount}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-1 text-sm text-green-600 font-medium">
                          <Gift className="w-4 h-4" />
                          ¥{code.bonus}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center gap-1 px-3 py-1 text-xs font-semibold rounded-full ${statusMap[code.status]?.color} ${statusMap[code.status]?.bgColor} border ${statusMap[code.status]?.borderColor}`}>
                          {statusMap[code.status]?.label}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600 font-mono">
                        {code.batchNo}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <Calendar className="w-4 h-4 text-gray-400" />
                          {new Date(code.createdAt).toLocaleString("zh-CN")}
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
    </div>
  );
}
