import { redirect } from "next/navigation";

/**
 * Admin 后台管理首页
 * 默认重定向到登录页面
 */
export default function AdminHome() {
  // 重定向到登录页面
  redirect("/login");
}
