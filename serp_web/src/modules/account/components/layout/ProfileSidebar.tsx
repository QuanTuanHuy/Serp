/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Profile sidebar navigation
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  User,
  Settings,
  ChevronRight,
  PanelLeftClose,
  PanelLeftOpen,
  ArrowLeft,
  LayoutDashboard,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useProfileSidebar } from '../../contexts/ProfileSidebarContext';
import { Button } from '@/shared/components/ui/button';

interface NavigationItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  description?: string;
}

const navigationItems: NavigationItem[] = [
  {
    name: 'Overview',
    href: '/profile',
    icon: LayoutDashboard,
    description: 'Profile overview',
  },
  {
    name: 'Edit Profile',
    href: '/profile/edit',
    icon: User,
    description: 'Edit your profile information',
  },
  // Future expansion
  // {
  //   name: 'Settings',
  //   href: '/profile/settings',
  //   icon: Settings,
  //   description: 'Account settings',
  // },
];

export const ProfileSidebar: React.FC = () => {
  const pathname = usePathname();
  const { isCollapsed, toggleSidebar } = useProfileSidebar();
  const [isHomeHovered, setIsHomeHovered] = React.useState(false);

  const isActive = (href: string) => {
    if (href === '/profile') {
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
          onMouseEnter={() => setIsHomeHovered(true)}
          onMouseLeave={() => setIsHomeHovered(false)}
        >
          {/* Icon that swaps on hover */}
          <div className='flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground flex-shrink-0 transition-all group-hover:bg-primary/90'>
            {isHomeHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : (
              <User className='h-5 w-5' />
            )}
          </div>

          {/* Module name */}
          {!isCollapsed && (
            <span className='text-sm font-semibold group-hover:text-primary transition-colors'>
              My Profile
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
    </aside>
  );
};
