/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM sidebar navigation
 */

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
  PanelLeftClose,
  PanelLeftOpen,
  ArrowLeft,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useCRMSidebar } from '../../contexts/CRMSidebarContext';
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
    href: '/crm/dashboard',
    icon: LayoutDashboard,
    description: 'Overview & Analytics',
  },
  {
    name: 'Customers',
    href: '/crm/customers',
    icon: Users,
    description: 'Customer Management',
  },
  {
    name: 'Leads',
    href: '/crm/leads',
    icon: Target,
    description: 'Sales Prospects',
  },
  {
    name: 'Opportunities',
    href: '/crm/opportunities',
    icon: Building2,
    description: 'Sales Pipeline',
  },
  {
    name: 'Activities',
    href: '/crm/activities',
    icon: Activity,
    description: 'Interactions & Tasks',
  },
  {
    name: 'Reports',
    href: '/crm/reports',
    icon: BarChart3,
    description: 'Analytics & Insights',
  },
  {
    name: 'Settings',
    href: '/crm/settings',
    icon: Settings,
    description: 'CRM Configuration',
  },
];

export const CRMSidebar: React.FC = () => {
  const pathname = usePathname();
  const { isCollapsed, toggleSidebar } = useCRMSidebar();
  const [isModuleHovered, setIsModuleHovered] = React.useState(false);

  // Get CRM module icon from shared configuration
  const CRMIcon = MODULE_ICONS.CRM.icon;

  const isActive = (href: string) => {
    return pathname === href || pathname.startsWith(href);
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
              <CRMIcon className='h-5 w-5' />
            )}
          </div>

          {/* Module name */}
          {!isCollapsed && (
            <span className='text-sm font-semibold group-hover:text-primary transition-colors'>
              CRM
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
            <p className='font-medium'>Customer Relationship Management</p>
            <p className='mt-1'>Version 1.0.0</p>
          </div>
        </div>
      )}
    </aside>
  );
};

export default CRMSidebar;
