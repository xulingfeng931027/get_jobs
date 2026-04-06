import type {Metadata} from "next";
import {Inter} from "next/font/google";
import "./globals.css";
import AdminLayout from "@/components/AdminLayout";
import {ToastProvider} from "@/components/Toast";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Get Jobs - 后台管理系统",
  description: "Get Jobs 自动化求职平台后台管理系统",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body className={inter.className}>
        <ToastProvider>
          <AdminLayout>{children}</AdminLayout>
        </ToastProvider>
      </body>
    </html>
  );
}
