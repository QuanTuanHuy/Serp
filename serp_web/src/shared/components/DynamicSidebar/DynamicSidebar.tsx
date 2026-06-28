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
import { useSidebarContext } from './SidebarContext';

interface DynamicSidebarProps {
  moduleCode: string;
  className?: string;
}

export const DynamicSidebar: React.FC<DynamicSidebarProps> = ({
  moduleCode,
  className,
}) => {
  const { isCollapsed, toggleSidebar } = useSidebarContext();
  const [isModuleHovered, setIsModuleHovered] = useState(false);

  const { menuItems, currentModule, isLoading, error, hasMenus, refetch } =
    useModuleSidebar(moduleCode);

  const moduleIcon = getModuleIcon(moduleCode);
  const ModuleIcon = moduleIcon?.icon;

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-30 h-screen border-r bg-background flex flex-col transition-[width] duration-300 ease-in-out',
        isCollapsed ? 'w-16' : 'w-64',
        className
      )}
    >
      {/* Logo/Brand */}
      <div
        className={cn(
          'flex h-16 items-center border-b px-3 transition-all duration-300',
          isCollapsed ? 'justify-center' : 'justify-between'
        )}
      >
        <Link
          href='/home'
          className={cn(
            'flex items-center group transition-colors overflow-hidden',
            !isCollapsed && 'space-x-3'
          )}
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
          <span
            className={cn(
              'text-sm font-semibold group-hover:text-primary transition-all duration-300 ease-in-out overflow-hidden whitespace-nowrap',
              isCollapsed
                ? 'max-w-0 opacity-0 pointer-events-none'
                : 'max-w-[150px] opacity-100'
            )}
          >
            {currentModule?.moduleName || moduleCode}
          </span>
        </Link>

        {/* Toggle Button - Repositioned to header */}
        <Button
          variant='ghost'
          size='icon'
          className={cn(
            'h-8 w-8 transition-all duration-300',
            isCollapsed ? 'hidden' : 'block' // Show normally in header when open
          )}
          onClick={toggleSidebar}
          title='Collapse sidebar'
        >
          <PanelLeftClose className='h-4 w-4' />
        </Button>
      </div>

      {/* Navigation */}
      <nav className='flex-1 space-y-1 p-2 overflow-y-auto'>
        {/* Expand button when collapsed, centered */}
        {isCollapsed && (
          <div className='flex justify-center py-2'>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8'
              onClick={toggleSidebar}
              title='Expand sidebar'
            >
              <PanelLeftOpen className='h-4 w-4' />
            </Button>
          </div>
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

      {/* Footer Info with Transition */}
      <div
        className={cn(
          'border-t p-4 transition-all duration-300 ease-in-out overflow-hidden',
          isCollapsed
            ? 'max-h-0 p-0 opacity-0 border-t-0'
            : 'max-h-32 opacity-100'
        )}
      >
        {currentModule && (
          <div className='rounded-lg bg-muted p-3 text-xs text-muted-foreground'>
            <p className='font-medium'>{currentModule.moduleName}</p>
            <p className='mt-1 overflow-hidden text-ellipsis'>
              {currentModule.moduleDescription || 'Module Description'}
            </p>
          </div>
        )}
      </div>
    </aside>
  );
};
