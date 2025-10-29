/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings main layout with sidebar and header
 */

'use client';

import React from 'react';
import { SettingsSidebar } from './SettingsSidebar';
import { SettingsHeader } from './SettingsHeader';
import { SettingsAuthGuard } from '../SettingsAuthGuard';
import {
  SettingsSidebarProvider,
  useSettingsSidebar,
} from '../../contexts/SettingsSidebarContext';
import { cn } from '@/shared/utils';

interface SettingsLayoutProps {
  children: React.ReactNode;
}

/**
 * SettingsLayout - Main layout wrapper for settings pages
 *
 * Usage:
 * ```tsx
 * export default function Layout({ children }) {
 *   return <SettingsLayout>{children}</SettingsLayout>;
 * }
 * ```
 */
const SettingsLayoutContent: React.FC<SettingsLayoutProps> = ({ children }) => {
  const { isCollapsed } = useSettingsSidebar();

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Fixed Sidebar - 64px or 256px width */}
      <SettingsSidebar />

      {/* Main Content Area */}
      <div
        className={cn(
          'flex flex-1 flex-col transition-all duration-300',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Header */}
        <SettingsHeader />

        {/* Page Content */}
        <main className='flex-1 overflow-y-auto'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
};

export const SettingsLayout: React.FC<SettingsLayoutProps> = ({ children }) => {
  return (
    <SettingsAuthGuard>
      <SettingsSidebarProvider>
        <SettingsLayoutContent>{children}</SettingsLayoutContent>
      </SettingsSidebarProvider>
    </SettingsAuthGuard>
  );
};
