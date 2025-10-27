/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM main layout with sidebar and header
 */

'use client';

import React from 'react';
import { CRMSidebar } from './CRMSidebar';
import { CRMHeader } from './CRMHeader';
import { CRMAuthGuard } from '../CRMAuthGuard';
import {
  CRMSidebarProvider,
  useCRMSidebar,
} from '../../contexts/CRMSidebarContext';
import { cn } from '@/shared/utils';

interface CRMLayoutProps {
  children: React.ReactNode;
}

/**
 * CRMLayout - Main layout wrapper for CRM pages
 */
const CRMLayoutContent: React.FC<CRMLayoutProps> = ({ children }) => {
  const { isCollapsed } = useCRMSidebar();

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Fixed Sidebar - 64px or 256px width */}
      <CRMSidebar />

      {/* Main Content Area */}
      <div
        className={cn(
          'flex flex-1 flex-col transition-all duration-300',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Header */}
        <CRMHeader />

        {/* Page Content */}
        <main className='flex-1 overflow-y-auto'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
};

export const CRMLayout: React.FC<CRMLayoutProps> = ({ children }) => {
  return (
    <CRMAuthGuard>
      <CRMSidebarProvider>
        <CRMLayoutContent>{children}</CRMLayoutContent>
      </CRMSidebarProvider>
    </CRMAuthGuard>
  );
};
