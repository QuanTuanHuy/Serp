/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin header with breadcrumbs, search, notifications
 */

'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import {
  Button,
  Avatar,
  AvatarFallback,
  ThemeToggle,
  Input,
} from '@/shared/components';
import {
  Search,
  Bell,
  Settings,
  User,
  ChevronDown,
  Home,
  Shield,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useAuth } from '@/modules/account';

interface AdminHeaderProps {
  className?: string;
}

export const AdminHeader: React.FC<AdminHeaderProps> = ({ className }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [notifications, setNotifications] = useState(0);

  const { logout } = useAuth();

  const router = useRouter();
  const pathname = usePathname();

  const getBreadcrumbs = () => {
    const segments = pathname.split('/').filter(Boolean);
    return segments.map((segment, index) => ({
      name:
        segment === 'admin'
          ? 'Admin'
          : segment.charAt(0).toUpperCase() + segment.slice(1),
      href: '/' + segments.slice(0, index + 1).join('/'),
      isLast: index === segments.length - 1,
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      // TODO: Implement global admin search
      console.log('Searching:', searchQuery);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/auth');
  };

  return (
    <header
      className={cn(
        'sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60',
        className
      )}
    >
      <div className='flex h-16 items-center justify-between px-6'>
        {/* Left Section - Breadcrumbs */}
        <div className='flex items-center space-x-4'>
          {/* Breadcrumbs */}
          <nav className='flex items-center space-x-2 text-sm'>
            {getBreadcrumbs().map((breadcrumb, index) => (
              <React.Fragment key={breadcrumb.href}>
                {index > 0 && <span className='text-muted-foreground'>/</span>}
                {breadcrumb.isLast ? (
                  <span className='font-medium'>{breadcrumb.name}</span>
                ) : (
                  <Link
                    href={breadcrumb.href}
                    className='text-muted-foreground hover:text-foreground transition-colors'
                  >
                    {breadcrumb.name}
                  </Link>
                )}
              </React.Fragment>
            ))}
          </nav>
        </div>

        {/* Center Section - Search */}
        <div className='flex-1 max-w-md mx-4'>
          <form onSubmit={handleSearch} className='relative'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search organizations, users, subscriptions...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-10 pr-4'
            />
          </form>
        </div>

        {/* Right Section - Actions & User */}
        <div className='flex items-center space-x-3'>
          {/* Notifications */}
          <div className='relative'>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => {
                // TODO: Open notifications panel
                console.log('Open notifications');
              }}
              className='relative'
            >
              <Bell className='h-5 w-5' />
              {notifications > 0 && (
                <span className='absolute -top-1 -right-1 h-5 w-5 rounded-full bg-red-500 text-xs font-medium text-white flex items-center justify-center'>
                  {notifications > 9 ? '9+' : notifications}
                </span>
              )}
            </Button>
          </div>

          {/* Theme Toggle */}
          <ThemeToggle />

          {/* User Menu */}
          <div className='relative'>
            <button
              onClick={() => setShowUserMenu(!showUserMenu)}
              className='flex items-center gap-2 rounded-lg p-2 hover:bg-muted transition-colors'
            >
              <Avatar className='h-8 w-8'>
                <AvatarFallback className='bg-primary text-primary-foreground'>
                  <Shield className='h-4 w-4' />
                </AvatarFallback>
              </Avatar>
              <div className='hidden sm:block text-left'>
                <p className='text-sm font-medium'>System Admin</p>
                <p className='text-xs text-muted-foreground'>SUPER_ADMIN</p>
              </div>
              <ChevronDown className='h-4 w-4 text-muted-foreground' />
            </button>

            {/* User Dropdown */}
            {showUserMenu && (
              <div className='absolute right-0 top-full mt-2 w-56 bg-background border rounded-md shadow-lg z-50'>
                <div className='p-2'>
                  <div className='px-2 py-2 border-b'>
                    <p className='font-medium'>System Administrator</p>
                    <p className='text-xs text-muted-foreground'>SUPER_ADMIN</p>
                  </div>

                  <div className='py-2'>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => {
                        setShowUserMenu(false);
                        router.push('/admin/profile');
                      }}
                    >
                      <User className='mr-2 h-4 w-4' />
                      Profile
                    </Button>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => {
                        setShowUserMenu(false);
                        router.push('/admin/settings');
                      }}
                    >
                      <Settings className='mr-2 h-4 w-4' />
                      Settings
                    </Button>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => {
                        setShowUserMenu(false);
                        router.push('/');
                      }}
                    >
                      <Home className='mr-2 h-4 w-4' />
                      Back to SERP
                    </Button>
                  </div>

                  <div className='border-t pt-2'>
                    <Button
                      variant='ghost'
                      className='w-full justify-start text-destructive hover:text-destructive'
                      onClick={handleLogout}
                    >
                      Logout
                    </Button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Close user menu when clicking outside */}
      {showUserMenu && (
        <div
          className='fixed inset-0 z-40'
          onClick={() => setShowUserMenu(false)}
        />
      )}
    </header>
  );
};
