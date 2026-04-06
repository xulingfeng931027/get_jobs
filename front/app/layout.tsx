"use client";

import "./globals.css";
import Sidebar from "./components/Sidebar";
import ContentArea from "./components/ContentArea";
import {ThemeProvider} from "next-themes";
import dynamic from "next/dynamic";
import { UserProvider } from "@/lib/auth-context";
import { ToastProvider } from "@/components/Toast";

// 动态导入 Electron 状态栏（仅客户端）
const ElectronStatusBar = dynamic(
  () => import("./components/ElectronStatusBar"),
  { ssr: false }
);

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN" suppressHydrationWarning>
      <head>
        <title>Find Jobs - 配置管理中心</title>
        <meta name="description" content="配置管理中心，管理application.yaml和环境变量配置" />
        <link
          rel="icon"
          href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🔍</text></svg>"
          type="image/svg+xml"
        />
      </head>
      <body suppressHydrationWarning className="dark:bg-blacksection">
        <ThemeProvider
          attribute="class"
          defaultTheme="light"
          enableSystem={false}
        >
          <ToastProvider>
            <UserProvider>
              <div className="flex min-h-screen">
                <Sidebar />
                <ContentArea>
                  {children}
                </ContentArea>
              </div>
              {/* Electron 状态栏（仅在 Electron 环境中显示） */}
              <ElectronStatusBar />
            </UserProvider>
          </ToastProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
