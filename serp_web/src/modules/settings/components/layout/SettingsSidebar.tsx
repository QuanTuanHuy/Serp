/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings sidebar navigation
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  Settings,
  Users,
  Puzzle,
  Layers,
  ChevronRight,
  PanelLeftClose,
  PanelLeftOpen,
  ArrowLeft,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useSettingsSidebar } from '../../contexts/SettingsSidebarContext';
import { Button } from '@/shared/components/ui/button';
import {
  Tooltip,
  TooltipTrigger,
  TooltipContent,
} from '@/shared/components/ui/tooltip';

interface NavigationItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  description?: string;
}

const navigationItems: NavigationItem[] = [
  {
    name: 'General',
    href: '/settings',
    icon: Settings,
    description: 'Organization profile and preferences',
  },
  {
    name: 'Users',
    href: '/settings/users',
    icon: Users,
    description: 'Manage organization users',
  },
  {
    name: 'Departments',
    href: '/settings/departments',
    icon: Layers,
    description: 'Manage departments and teams',
  },
  {
    name: 'Modules',
    href: '/settings/modules',
    icon: Puzzle,
    description: 'Manage module access',
  },
];

export const SettingsSidebar: React.FC = () => {
  const pathname = usePathname();
  const { isCollapsed, toggleSidebar } = useSettingsSidebar();
  const [isModuleHovered, setIsModuleHovered] = React.useState(false);

  const isActive = (href: string) => {
    if (href === '/settings') {
      return pathname === href;
    }
    return pathname.startsWith(href);
  };

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-30 h-screen border-r bg-background flex flex-col transition-[width] duration-300 ease-in-out',
        isCollapsed ? 'w-16' : 'w-64'
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
          className='flex items-center space-x-3 group transition-colors overflow-hidden'
          onMouseEnter={() => setIsModuleHovered(true)}
          onMouseLeave={() => setIsModuleHovered(false)}
        >
          {/* Icon that swaps on hover */}
          <div className='flex h-8 w-8 items-center justify-center rounded-lg bg-purple-600 text-white flex-shrink-0 transition-all group-hover:bg-purple-700'>
            {isModuleHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : (
              <Settings className='h-5 w-5' />
            )}
          </div>

          {/* Module name */}
          <span
            className={cn(
              'text-sm font-semibold group-hover:text-purple-600 transition-all duration-300 ease-in-out overflow-hidden whitespace-nowrap',
              isCollapsed
                ? 'max-w-0 opacity-0 pointer-events-none'
                : 'max-w-[150px] opacity-100'
            )}
          >
            Settings
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
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.href);

          const itemLink = (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'group flex items-center justify-between rounded-lg px-3 py-2.5 text-sm font-medium transition-all hover:bg-accent hover:text-accent-foreground',
                active
                  ? 'bg-accent text-accent-foreground shadow-sm'
                  : 'text-muted-foreground',
                isCollapsed && 'justify-center'
              )}
            >
              <div className='flex items-center space-x-3'>
                <Icon
                  className={cn(
                    'h-5 w-5 transition-colors flex-shrink-0',
                    active
                      ? 'text-purple-600'
                      : 'text-muted-foreground group-hover:text-accent-foreground'
                  )}
                />
                <span
                  className={cn(
                    'transition-all duration-300 ease-in-out overflow-hidden whitespace-nowrap',
                    isCollapsed
                      ? 'max-w-0 opacity-0 pointer-events-none'
                      : 'max-w-[200px] opacity-100'
                  )}
                >
                  {item.name}
                </span>
              </div>

              {active && !isCollapsed && (
                <ChevronRight className='h-4 w-4 text-purple-600' />
              )}
            </Link>
          );

          if (isCollapsed) {
            return (
              <Tooltip key={item.href}>
                <TooltipTrigger asChild>{itemLink}</TooltipTrigger>
                <TooltipContent side='right' sideOffset={12}>
                  {item.name}
                </TooltipContent>
              </Tooltip>
            );
          }

          return itemLink;
        })}
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
        <div className='rounded-lg bg-purple-50 dark:bg-purple-950 p-3 text-xs text-muted-foreground'>
          <p className='font-medium text-purple-900 dark:text-purple-100'>
            Organization Settings
          </p>
          <p className='mt-1'>
            Logged in as{' '}
            <span className='font-semibold'>Organization Admin</span>
          </p>
        </div>
      </div>
    </aside>
  );
};
