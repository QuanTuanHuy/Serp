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
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/shared/utils';

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
    name: 'Users',
    href: '/admin/users',
    icon: Users,
    description: 'System-wide user management',
  },
];

export const AdminSidebar: React.FC = () => {
  const pathname = usePathname();

  const isActive = (href: string) => {
    if (href === '/admin') {
      return pathname === href;
    }
    return pathname.startsWith(href);
  };

  return (
    <aside className='fixed left-0 top-0 z-30 h-screen w-64 border-r bg-background'>
      {/* Logo/Brand */}
      <div className='flex h-16 items-center border-b px-6'>
        <Link href='/admin' className='flex items-center space-x-2'>
          <div className='flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground'>
            <span className='text-lg font-bold'>S</span>
          </div>
          <div className='flex flex-col'>
            <span className='text-sm font-semibold'>SERP</span>
            <span className='text-xs text-muted-foreground'>System Admin</span>
          </div>
        </Link>
      </div>

      {/* Navigation */}
      <nav className='flex-1 space-y-1 p-4'>
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
                  : 'text-muted-foreground'
              )}
            >
              <div className='flex items-center space-x-3'>
                <Icon
                  className={cn(
                    'h-5 w-5 transition-colors',
                    active
                      ? 'text-primary'
                      : 'text-muted-foreground group-hover:text-accent-foreground'
                  )}
                />
                <span>{item.name}</span>
              </div>

              {active && <ChevronRight className='h-4 w-4 text-primary' />}
            </Link>
          );
        })}
      </nav>

      {/* Footer Info */}
      <div className='border-t p-4'>
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
