"use client";

import {useState} from "react";
import {useRouter} from "next/navigation";
import {apiRequest} from "@/lib/api";
import {ArrowLeft, Copy, Download} from "lucide-react";
import {useToast} from "@/components/Toast";

export default function GeneratePage() {
  const router = useRouter();
  const { showToast } = useToast();
  const [count, setCount] = useState(10);
  const [amount, setAmount] = useState(100);
  const [bonus, setBonus] = useState(0);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);

  const handleGenerate = async () => {
    setLoading(true);
    try {
      const username = localStorage.getItem("admin_username") || "admin";
      const data = await apiRequest("/api/admin/recharge-codes/generate", {
        method: "POST",
        body: JSON.stringify({
          count,
          amount,
          bonus,
          createdBy: username,
        }),
      });
      setResult(data);
      showToast("充值码生成成功", "success");
    } catch (err: any) {
      showToast("生成失败: " + err.message, "error");
    } finally {
      setLoading(false);
    }
  };

  const copyCodes = () => {
    if (result?.codes) {
      navigator.clipboard.writeText(result.codes.join("\n"));
      showToast("已复制到剪贴板", "success");
    }
  };

  const downloadCodes = () => {
    if (result?.codes) {
      const content = result.codes.join("\n");
      const blob = new Blob([content], { type: "text/plain" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `recharge_codes_${result.batchNo}.txt`;
      a.click();
      URL.revokeObjectURL(url);
    }
  };

  return (
    <div>
      <div className="mb-8">
        <button
          onClick={() => router.back()}
          className="flex items-center gap-1 text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="w-4 h-4" />
          返回
        </button>
        <h1 className="text-3xl font-bold text-gray-900">批量生成充值码</h1>
        <p className="text-gray-600 mt-2">设置参数并批量生成充值码</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 表单 */}
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-6">生成参数</h2>

          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                生成数量
              </label>
              <input
                type="number"
                value={count}
                onChange={(e) => setCount(Number(e.target.value))}
                min={1}
                max={1000}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <p className="text-xs text-gray-500 mt-1">单次最多生成 1000 个</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                充值金额（元）
              </label>
              <input
                type="number"
                value={amount}
                onChange={(e) => setAmount(Number(e.target.value))}
                min={1}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                赠送金额（元）
              </label>
              <input
                type="number"
                value={bonus}
                onChange={(e) => setBonus(Number(e.target.value))}
                min={0}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="bg-blue-50 p-4 rounded-lg">
              <p className="text-sm text-blue-800">
                <span className="font-medium">总价值:</span> ¥{amount + bonus} / 个
              </p>
              <p className="text-sm text-blue-800 mt-1">
                <span className="font-medium">批次总价:</span> ¥{(amount + bonus) * count}
              </p>
            </div>

            <button
              onClick={handleGenerate}
              disabled={loading}
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "生成中..." : "立即生成"}
            </button>
          </div>
        </div>

        {/* 结果 */}
        <div className="bg-white rounded-xl shadow p-6">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold text-gray-900">生成结果</h2>
            {result && (
              <div className="flex gap-2">
                <button
                  onClick={copyCodes}
                  className="flex items-center gap-1 px-3 py-1 border rounded-lg hover:bg-gray-50"
                >
                  <Copy className="w-4 h-4" />
                  复制
                </button>
                <button
                  onClick={downloadCodes}
                  className="flex items-center gap-1 px-3 py-1 border rounded-lg hover:bg-gray-50"
                >
                  <Download className="w-4 h-4" />
                  下载
                </button>
              </div>
            )}
          </div>

          {!result ? (
            <div className="h-64 flex items-center justify-center text-gray-400">
              暂无生成结果
            </div>
          ) : (
            <>
              <div className="bg-green-50 p-4 rounded-lg mb-4">
                <p className="text-sm text-green-800">
                  <span className="font-medium">批次号:</span> {result.batchNo}
                </p>
                <p className="text-sm text-green-800 mt-1">
                  <span className="font-medium">生成数量:</span> {result.count} 个
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 max-h-64 overflow-y-auto font-mono text-sm">
                {result.codes.map((code: string, index: number) => (
                  <div key={index} className="py-1 text-gray-700">
                    {code}
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
