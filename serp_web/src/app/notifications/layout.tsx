/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notifications area layout (authenticated)
 */

'use client';

import { ProtectedRoute } from '@/modules/account/components/ProtectedRoute';

export default function NotificationsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <ProtectedRoute>{children}</ProtectedRoute>;
}
