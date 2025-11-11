/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Dynamic Sidebar component
 */

'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import {
  ArrowLeft,
  PanelLeftClose,
  PanelLeftOpen,
  Loader2,
  AlertCircle,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';
import { useModuleSidebar } from '@/shared/hooks';
import { getModuleIcon } from '@/shared/constants/moduleIcons';
import { SidebarMenuItemComponent } from './SidebarMenuItem';

interface DynamicSidebarProps {
  moduleCode: string;
  className?: string;
}

export const DynamicSidebar: React.FC<DynamicSidebarProps> = ({
  moduleCode,
  className,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [isModuleHovered, setIsModuleHovered] = useState(false);

  const { menuItems, currentModule, isLoading, error, hasMenus, refetch } =
    useModuleSidebar(moduleCode);

  const moduleIcon = getModuleIcon(moduleCode);
  const ModuleIcon = moduleIcon?.icon;

  const toggleSidebar = () => {
    setIsCollapsed(!isCollapsed);
  };

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-30 h-screen border-r bg-background transition-all duration-300',
        isCollapsed ? 'w-16' : 'w-64',
        className
      )}
    >
      {/* Logo/Brand */}
      <div className='flex h-16 items-center border-b px-3 justify-between'>
        <Link
          href='/home'
          className='flex items-center space-x-3 group transition-colors'
          onMouseEnter={() => setIsModuleHovered(true)}
          onMouseLeave={() => setIsModuleHovered(false)}
        >
          {/* Icon that swaps on hover */}
          <div
            className={cn(
              'flex h-8 w-8 items-center justify-center rounded-lg text-primary-foreground flex-shrink-0 transition-all group-hover:opacity-90',
              'bg-primary',
              isModuleHovered && 'bg-primary'
            )}
          >
            {isModuleHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : ModuleIcon ? (
              <ModuleIcon className='h-5 w-5' />
            ) : (
              <span className='text-xs font-bold'>{moduleCode}</span>
            )}
          </div>

          {/* Module name */}
          {!isCollapsed && (
            <span className='text-sm font-semibold group-hover:text-primary transition-colors'>
              {currentModule?.moduleName || moduleCode}
            </span>
          )}
        </Link>

        {/* Toggle Button - Only show when not collapsed */}
        {!isCollapsed && (
          <Button
            variant='ghost'
            size='icon'
            className='h-8 w-8'
            onClick={toggleSidebar}
          >
            <PanelLeftClose className='h-4 w-4' />
          </Button>
        )}
      </div>

      {/* Navigation */}
      <nav className='flex-1 space-y-1 p-2 overflow-y-auto max-h-[calc(100vh-8rem)]'>
        {/* Expand button when collapsed */}
        {isCollapsed && (
          <Button
            variant='ghost'
            size='icon'
            className='w-full mt-4'
            onClick={toggleSidebar}
            title='Expand sidebar'
          >
            <PanelLeftOpen className='h-4 w-4' />
          </Button>
        )}

        {/* Loading State */}
        {isLoading && (
          <div className='flex flex-col items-center justify-center py-8 text-muted-foreground'>
            <Loader2 className='h-6 w-6 animate-spin mb-2' />
            {!isCollapsed && <p className='text-xs'>Loading menus...</p>}
          </div>
        )}

        {/* Error State */}
        {error && !isLoading && (
          <div className='p-4'>
            <div className='flex flex-col items-center text-center space-y-3'>
              <div className='h-12 w-12 rounded-full bg-destructive/10 flex items-center justify-center'>
                <AlertCircle className='h-6 w-6 text-destructive' />
              </div>
              {!isCollapsed && (
                <>
                  <div className='space-y-1'>
                    <p className='text-sm font-medium text-destructive'>
                      Failed to load menus
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      Please try again
                    </p>
                  </div>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => refetch()}
                    className='w-full'
                  >
                    Retry
                  </Button>
                </>
              )}
            </div>
          </div>
        )}

        {/* Empty State */}
        {!isLoading && !error && !hasMenus && (
          <div className='p-4'>
            {!isCollapsed ? (
              <div className='text-center space-y-2'>
                <p className='text-sm font-medium text-muted-foreground'>
                  No menus available
                </p>
                <p className='text-xs text-muted-foreground'>
                  Contact your administrator
                </p>
              </div>
            ) : (
              <div className='text-center'>
                <AlertCircle className='h-6 w-6 text-muted-foreground mx-auto' />
              </div>
            )}
          </div>
        )}

        {/* Menu Items */}
        {!isLoading && !error && hasMenus && (
          <>
            {menuItems.map((item) => (
              <SidebarMenuItemComponent
                key={item.id}
                item={item}
                isCollapsed={isCollapsed}
              />
            ))}
          </>
        )}
      </nav>

      {/* Footer Info */}
      {!isCollapsed && currentModule && (
        <div className='border-t p-4'>
          <div className='rounded-lg bg-muted p-3 text-xs text-muted-foreground'>
            <p className='font-medium'>{currentModule.moduleName}</p>
            <p className='mt-1'>
              {currentModule.moduleDescription || 'Module Description'}
            </p>
          </div>
        </div>
      )}
    </aside>
  );
};
