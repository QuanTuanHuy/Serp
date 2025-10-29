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
  Building2,
  Users,
  Puzzle,
  Layers,
  CreditCard,
  Shield,
  Webhook,
  ChevronRight,
  PanelLeftClose,
  PanelLeftOpen,
  ArrowLeft,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useSettingsSidebar } from '../../contexts/SettingsSidebarContext';
import { Button } from '@/shared/components/ui/button';

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
    name: 'Roles',
    href: '/settings/roles',
    icon: Shield,
    description: 'Configure roles and permissions',
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
  {
    name: 'Subscription',
    href: '/settings/subscription',
    icon: CreditCard,
    description: 'Subscription and billing',
  },
  {
    name: 'Security',
    href: '/settings/security',
    icon: Shield,
    description: 'Security settings and audit logs',
  },
  {
    name: 'Integrations',
    href: '/settings/integrations',
    icon: Webhook,
    description: 'Third-party integrations',
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
        'fixed left-0 top-0 z-30 h-screen border-r bg-background transition-all duration-300',
        isCollapsed ? 'w-16' : 'w-64'
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
          <div className='flex h-8 w-8 items-center justify-center rounded-lg bg-purple-600 text-white flex-shrink-0 transition-all group-hover:bg-purple-700'>
            {isModuleHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : (
              <Settings className='h-5 w-5' />
            )}
          </div>

          {/* Module name */}
          {!isCollapsed && (
            <span className='text-sm font-semibold group-hover:text-purple-600 transition-colors'>
              Settings
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
      <nav className='flex-1 space-y-1 p-2'>
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
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.href);

          return (
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
              title={isCollapsed ? item.name : undefined}
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
                {!isCollapsed && <span>{item.name}</span>}
              </div>

              {active && !isCollapsed && (
                <ChevronRight className='h-4 w-4 text-purple-600' />
              )}
            </Link>
          );
        })}
      </nav>

      {/* Footer Info */}
      {!isCollapsed && (
        <div className='border-t p-4'>
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
      )}
    </aside>
  );
};
