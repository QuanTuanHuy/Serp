/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Profile main layout with sidebar
 */

'use client';

import React, { useRef } from 'react';
import { ProfileSidebar } from './ProfileSidebar';
import {
  ProfileSidebarProvider,
  useProfileSidebar,
} from '../../contexts/ProfileSidebarContext';
import { cn } from '@/shared/utils';

interface ProfileLayoutProps {
  children: React.ReactNode;
}

const ProfileLayoutContent: React.FC<ProfileLayoutProps> = ({ children }) => {
  const { isCollapsed } = useProfileSidebar();
  const containerRef = useRef<HTMLDivElement | null>(null);

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Fixed Sidebar */}
      <ProfileSidebar />

      {/* Main Content Area */}
      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Page Content */}
        <main className='flex-1'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
};

export const ProfileLayout: React.FC<ProfileLayoutProps> = ({ children }) => {
  return (
    <ProfileSidebarProvider>
      <ProfileLayoutContent>{children}</ProfileLayoutContent>
    </ProfileSidebarProvider>
  );
};
