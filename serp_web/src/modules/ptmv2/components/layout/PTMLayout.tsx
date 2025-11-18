/**
 * PTM v2 - Main Layout Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Layout wrapper for PTM module
 */

'use client';

import { ReactNode } from 'react';
import { useSelector } from 'react-redux';
import { PTMSidebar } from './PTMSidebar';
import { PTMHeader } from './PTMHeader';
import { PTMCommandPalette } from './PTMCommandPalette';
import { cn } from '@/shared/utils';
import { LAYOUT_CONSTANTS } from '../../constants/colors';
import type { PTMState } from '../../store';

interface PTMLayoutProps {
  children: ReactNode;
}

export function PTMLayout({ children }: PTMLayoutProps) {
  const { sidebarOpen, sidebarCollapsed } = useSelector(
    (state: { ptm: PTMState }) => state.ptm.ui
  );

  return (
    <div className='flex h-screen bg-background overflow-hidden'>
      {/* Sidebar */}
      <PTMSidebar />

      {/* Main Content */}
      <div className='flex-1 flex flex-col overflow-hidden'>
        {/* Header */}
        <PTMHeader />

        {/* Content Area */}
        <main
          className={cn(
            'flex-1 overflow-auto transition-all duration-300',
            'bg-muted/30'
          )}
        >
          <div className='container mx-auto p-6 max-w-7xl'>{children}</div>
        </main>
      </div>

      {/* Command Palette */}
      <PTMCommandPalette />
    </div>
  );
}
