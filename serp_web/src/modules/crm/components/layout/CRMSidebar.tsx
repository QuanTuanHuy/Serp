// CRM Sidebar Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Users,
  Target,
  Building2,
  Activity,
  BarChart3,
  Settings,
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/shared/utils';

interface CRMSidebarProps {
  className?: string;
}

const navigationItems = [
  {
    title: 'Dashboard',
    href: '/crm/dashboard',
    icon: LayoutDashboard,
    description: 'Overview & Analytics',
  },
  {
    title: 'Customers',
    href: '/crm/customers',
    icon: Users,
    description: 'Customer Management',
  },
  {
    title: 'Leads',
    href: '/crm/leads',
    icon: Target,
    description: 'Sales Prospects',
  },
  {
    title: 'Opportunities',
    href: '/crm/opportunities',
    icon: Building2,
    description: 'Sales Pipeline',
  },
  {
    title: 'Activities',
    href: '/crm/activities',
    icon: Activity,
    description: 'Interactions & Tasks',
  },
  {
    title: 'Reports',
    href: '/crm/reports',
    icon: BarChart3,
    description: 'Analytics & Insights',
  },
  {
    title: 'Settings',
    href: '/crm/settings',
    icon: Settings,
    description: 'CRM Configuration',
  },
];

export const CRMSidebar: React.FC<CRMSidebarProps> = ({ className }) => {
  const pathname = usePathname();

  return (
    <div
      className={cn(
        'flex h-full w-64 flex-col border-r border-border bg-background',
        className
      )}
    >
      {/* Header */}
      <div className='flex h-16 items-center border-b border-border px-6'>
        <h2 className='text-lg font-semibold'>CRM System</h2>
      </div>

      {/* Navigation */}
      <nav className='flex-1 space-y-1 p-4'>
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                'hover:bg-accent hover:text-accent-foreground',
                isActive
                  ? 'bg-accent text-accent-foreground'
                  : 'text-muted-foreground'
              )}
            >
              <Icon className='h-5 w-5' />
              <span className='flex-1'>{item.title}</span>
              {isActive && <ChevronRight className='h-4 w-4' />}
            </Link>
          );
        })}
      </nav>

      {/* Footer */}
      <div className='border-t border-border p-4'>
        <div className='text-xs text-muted-foreground'>
          Customer Relationship Management
        </div>
        <div className='text-xs text-muted-foreground'>Version 1.0.0</div>
      </div>
    </div>
  );
};

export default CRMSidebar;
