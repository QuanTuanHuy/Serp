/**
 * CRM Layout - Customer Relationship Management Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM layout with sidebar navigation
 */

import React from 'react';
import { CRMSidebar, CRMHeader } from '@/modules/crm/components/layout';

interface CRMLayoutProps {
  children: React.ReactNode;
}

export default function CRMLayout({ children }: CRMLayoutProps) {
  return (
    <div className='flex h-screen bg-background'>
      {/* Sidebar */}
      <CRMSidebar className='fixed left-0 top-0 z-40 h-full' />

      {/* Main Content */}
      <div className='flex-1 ml-64 flex flex-col'>
        {/* CRM Header */}
        <CRMHeader />

        {/* Page Content */}
        <main className='flex-1 overflow-y-auto'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
}
