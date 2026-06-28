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
          className={cn(
            'flex items-center group transition-colors overflow-hidden',
            !isCollapsed && 'space-x-3'
          )}
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
          <span
            className={cn(
              'text-sm font-semibold group-hover:text-primary transition-all duration-300 ease-in-out overflow-hidden whitespace-nowrap',
              isCollapsed
                ? 'max-w-0 opacity-0 pointer-events-none'
                : 'max-w-[150px] opacity-100'
            )}
          >
            Admin
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
              <div
                className={cn('flex items-center', !isCollapsed && 'space-x-3')}
              >
                <Icon
                  className={cn(
                    'h-5 w-5 transition-colors flex-shrink-0',
                    active
                      ? 'text-primary'
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
                <ChevronRight className='h-4 w-4 text-primary' />
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
        <div className='rounded-lg bg-muted p-3 text-xs text-muted-foreground'>
          <p className='font-medium'>System Admin Panel</p>
          <p className='mt-1'>
            Logged in as{' '}
            <span className='font-semibold'>System Administrator</span>
          </p>
        </div>
      </div>
    </aside>
  );
};
