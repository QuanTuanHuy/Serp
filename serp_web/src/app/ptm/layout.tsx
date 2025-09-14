/**
 * PTM Layout - Personal Task Management Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM layout with sidebar navigation
 */

import React from 'react';
import { Sidebar, PTMHeader } from '@/modules/ptm/components';

interface PTMLayoutProps {
  children: React.ReactNode;
}

export default function PTMLayout({ children }: PTMLayoutProps) {
  return (
    <div className='flex h-screen bg-background'>
      {/* Sidebar */}
      <Sidebar className='fixed left-0 top-0 z-40 h-full' />

      {/* Main Content */}
      <div className='flex-1 ml-64 flex flex-col'>
        {/* PTM Header */}
        <PTMHeader />

        {/* Page Content */}
        <main className='flex-1 overflow-y-auto'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
}
