/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM main layout with sidebar and header
 */

'use client';

import React from 'react';
import { DynamicSidebar, RouteGuard } from '@/shared/components';
import { CRMHeader } from './CRMHeader';
import { CRMAuthGuard } from '../CRMAuthGuard';

interface CRMLayoutProps {
  children: React.ReactNode;
}

/**
 * CRMLayout - Main layout wrapper for CRM pages
 */
const CRMLayoutContent: React.FC<CRMLayoutProps> = ({ children }) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Dynamic Sidebar */}
      <DynamicSidebar moduleCode='CRM' />

      {/* Main Content Area */}
      <div
        ref={containerRef}
        className='flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto pl-64'
      >
        {/* Header */}
        <CRMHeader scrollContainerRef={containerRef} />

        {/* Page Content */}
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='CRM'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const CRMLayout: React.FC<CRMLayoutProps> = ({ children }) => {
  return (
    <CRMAuthGuard>
      <CRMLayoutContent>{children}</CRMLayoutContent>
    </CRMAuthGuard>
  );
};
