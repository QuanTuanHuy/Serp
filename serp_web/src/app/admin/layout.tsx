/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin layout wrapper
 */

import { AdminLayout } from '@/modules/admin';

export default function Layout({ children }: { children: React.ReactNode }) {
  return <AdminLayout>{children}</AdminLayout>;
}
