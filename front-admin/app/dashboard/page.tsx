"use client";

import {useEffect, useState} from "react";
import {CreditCard, DollarSign, TrendingUp, Users} from "lucide-react";
import {apiRequest} from "@/lib/api";

export default function DashboardPage() {
  const [stats, setStats] = useState<any>({
    totalUsers: 0,
    activeUsers: 0,
    totalCodes: 0,
    activatedCodes: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await apiRequest("/api/admin/stats/dashboard");
        setStats(data);
      } catch (err) {
        console.error("获取统计数据失败:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  const statCards = [
    {
      title: "总用户数",
      value: stats.totalUsers,
      icon: Users,
      color: "bg-blue-500",
    },
    {
      title: "活跃用户",
      value: stats.activeUsers,
      icon: TrendingUp,
      color: "bg-green-500",
    },
    {
      title: "充值码总数",
      value: stats.totalCodes,
      icon: CreditCard,
      color: "bg-purple-500",
    },
    {
      title: "已激活充值码",
      value: stats.activatedCodes,
      icon: DollarSign,
      color: "bg-orange-500",
    },
  ];

  if (loading) {
    return <div className="flex items-center justify-center h-64">加载中...</div>;
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">数据仪表盘</h1>
        <p className="text-gray-600 mt-2">欢迎使用 Get Jobs 后台管理系统</p>
      </div>

      {/* 统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statCards.map((stat) => {
          const Icon = stat.icon;
          return (
            <div key={stat.title} className="bg-white rounded-xl shadow p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">{stat.title}</p>
                  <p className="text-3xl font-bold text-gray-900 mt-2">{stat.value}</p>
                </div>
                <div className={`${stat.color} p-4 rounded-lg`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* 图表区域 - 待实现 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">用户增长趋势</h2>
          <div className="h-64 flex items-center justify-center text-gray-400">
            图表待实现
          </div>
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">充值分布</h2>
          <div className="h-64 flex items-center justify-center text-gray-400">
            图表待实现
          </div>
        </div>
      </div>
    </div>
  );
}
