/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin sidebar navigation
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Building2,
  CreditCard,
  Package,
  Puzzle,
  Users,
  ShieldCheck,
  ChevronRight,
  PanelLeftClose,
  PanelLeftOpen,
  ArrowLeft,
  Menu,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useAdminSidebar } from '../../contexts/AdminSidebarContext';
import { Button } from '@/shared/components/ui/button';
import { MODULE_ICONS } from '@/shared/constants/moduleIcons';

interface NavigationItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  description?: string;
}

const navigationItems: NavigationItem[] = [
  {
    name: 'Dashboard',
    href: '/admin',
    icon: LayoutDashboard,
    description: 'System overview and statistics',
  },
  {
    name: 'Organizations',
    href: '/admin/organizations',
    icon: Building2,
    description: 'Manage organizations and tenants',
  },
  {
    name: 'Subscriptions',
    href: '/admin/subscriptions',
    icon: CreditCard,
    description: 'View and manage organization subscriptions',
  },
  {
    name: 'Subscription Plans',
    href: '/admin/plans',
    icon: Package,
    description: 'Configure subscription plans and pricing',
  },
  {
    name: 'Modules',
    href: '/admin/modules',
    icon: Puzzle,
    description: 'Manage system modules and features',
  },
  {
    name: 'Menu Displays',
    href: '/admin/menu-displays',
    icon: Menu,
    description: 'Manage navigation menus and hierarchies',
  },
  {
    name: 'Users',
    href: '/admin/users',
    icon: Users,
    description: 'System-wide user management',
  },
  {
    name: 'Roles',
    href: '/admin/roles',
    icon: ShieldCheck,
    description: 'Role and permission management',
  },
];

export const AdminSidebar: React.FC = () => {
  const pathname = usePathname();
  const { isCollapsed, toggleSidebar } = useAdminSidebar();
  const [isModuleHovered, setIsModuleHovered] = React.useState(false);

  const AdminIcon = MODULE_ICONS.ADMIN.icon;

  const isActive = (href: string) => {
    if (href === '/admin') {
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
          <div className='flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground flex-shrink-0 transition-all group-hover:bg-primary/90'>
            {isModuleHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : (
              <AdminIcon className='h-5 w-5' />
            )}
          </div>

          {/* Module name */}
          {!isCollapsed && (
            <span className='text-sm font-semibold group-hover:text-primary transition-colors'>
              Admin
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
                      ? 'text-primary'
                      : 'text-muted-foreground group-hover:text-accent-foreground'
                  )}
                />
                {!isCollapsed && <span>{item.name}</span>}
              </div>

              {active && !isCollapsed && (
                <ChevronRight className='h-4 w-4 text-primary' />
              )}
            </Link>
          );
        })}
      </nav>

      {/* Footer Info */}
      {!isCollapsed && (
        <div className='border-t p-4'>
          <div className='rounded-lg bg-muted p-3 text-xs text-muted-foreground'>
            <p className='font-medium'>System Admin Panel</p>
            <p className='mt-1'>
              Logged in as{' '}
              <span className='font-semibold'>System Administrator</span>
            </p>
          </div>
        </div>
      )}
    </aside>
  );
};
