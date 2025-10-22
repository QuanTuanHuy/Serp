/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin main layout with sidebar and header
 */

'use client';

import React from 'react';
import { AdminSidebar } from './AdminSidebar';
import { AdminHeader } from './AdminHeader';
import { AdminAuthGuard } from '../AdminAuthGuard';

interface AdminLayoutProps {
  children: React.ReactNode;
}

/**
 * AdminLayout - Main layout wrapper for admin pages
 * Includes:
 * - AdminAuthGuard (requires SUPER_ADMIN/SYSTEM_MODERATOR)
 * - Fixed sidebar navigation (w-64)
 * - Header with breadcrumbs and user menu
 * - Main content area with container
 *
 * Usage in app/admin/layout.tsx:
 * ```tsx
 * export default function Layout({ children }) {
 *   return <AdminLayout>{children}</AdminLayout>;
 * }
 * ```
 */
export const AdminLayout: React.FC<AdminLayoutProps> = ({ children }) => {
  return (
    <AdminAuthGuard>
      <div className='flex min-h-screen bg-background'>
        {/* Fixed Sidebar - 64px width */}
        <AdminSidebar />

        {/* Main Content Area */}
        <div className='flex flex-1 flex-col pl-64'>
          {/* Header */}
          <AdminHeader />

          {/* Page Content */}
          <main className='flex-1 overflow-y-auto'>
            <div className='container mx-auto p-6'>{children}</div>
          </main>
        </div>
      </div>
    </AdminAuthGuard>
  );
};
