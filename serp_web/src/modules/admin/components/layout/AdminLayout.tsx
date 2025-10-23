/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin main layout with sidebar and header
 */

'use client';

import React from 'react';
import { AdminSidebar } from './AdminSidebar';
import { AdminHeader } from './AdminHeader';
import { AdminAuthGuard } from '../AdminAuthGuard';
import {
  AdminSidebarProvider,
  useAdminSidebar,
} from '../../contexts/AdminSidebarContext';
import { cn } from '@/shared/utils';

interface AdminLayoutProps {
  children: React.ReactNode;
}

/**
 * AdminLayout - Main layout wrapper for admin pages
 *
 * Usage
 * ```tsx
 * export default function Layout({ children }) {
 *   return <AdminLayout>{children}</AdminLayout>;
 * }
 * ```
 */
const AdminLayoutContent: React.FC<AdminLayoutProps> = ({ children }) => {
  const { isCollapsed } = useAdminSidebar();

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Fixed Sidebar - 64px or 256px width */}
      <AdminSidebar />

      {/* Main Content Area */}
      <div
        className={cn(
          'flex flex-1 flex-col transition-all duration-300',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Header */}
        <AdminHeader />

        {/* Page Content */}
        <main className='flex-1 overflow-y-auto'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
};

export const AdminLayout: React.FC<AdminLayoutProps> = ({ children }) => {
  return (
    <AdminAuthGuard>
      <AdminSidebarProvider>
        <AdminLayoutContent>{children}</AdminLayoutContent>
      </AdminSidebarProvider>
    </AdminAuthGuard>
  );
};
