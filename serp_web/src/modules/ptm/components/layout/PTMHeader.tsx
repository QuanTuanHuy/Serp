/**
 * PTM Header Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM specific header with navigation and controls
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
  Plus,
  Bell,
  Settings,
  User,
  ChevronDown,
  Home,
} from 'lucide-react';
import { useUser } from '@/modules/account';

export function PTMHeader() {
  const [searchQuery, setSearchQuery] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [notifications, setNotifications] = useState(3); // Mock notification count

  const { getInitials, getDisplayName, user } = useUser();

  const router = useRouter();
  const pathname = usePathname();

  const getPageTitle = () => {
    const path = pathname.split('/').pop();
    switch (path) {
      case 'dashboard':
        return 'Dashboard';
      case 'projects':
        return 'Project Management';
      case 'schedule':
        return 'Optimize Your Tasks';
      case 'calendar':
        return 'Daily Calendar';
      case 'notifications':
        return 'Notifications';
      case 'note-dashboard':
        return 'Note Dashboard';
      case 'chat':
        return 'AI Assistant';
      default:
        return 'PTM';
    }
  };

  const getBreadcrumbs = () => {
    const segments = pathname.split('/').filter(Boolean);
    return segments.map((segment, index) => ({
      name:
        segment === 'ptm'
          ? 'PTM'
          : segment.charAt(0).toUpperCase() + segment.slice(1),
      href: '/' + segments.slice(0, index + 1).join('/'),
      isLast: index === segments.length - 1,
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(`/ptm/dashboard?search=${encodeURIComponent(searchQuery)}`);
    }
  };

  const handleLogout = () => {
    // Handle logout logic
    router.push('/auth');
  };

  return (
    <header className='sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60'>
      <div className='flex h-16 items-center justify-between px-6'>
        {/* Left Section - Breadcrumbs */}
        <div className='flex items-center space-x-4'>
          {/* Breadcrumbs */}
          <nav className='hidden md:flex items-center space-x-2 text-sm'>
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
              placeholder='Search tasks, projects, notes...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-10 pr-4'
            />
          </form>
        </div>

        {/* Right Section - Actions & User */}
        <div className='flex items-center space-x-3'>
          {/* Mobile Quick Add */}
          <Button size='sm' className='lg:hidden'>
            <Plus className='h-4 w-4' />
          </Button>

          {/* Notifications */}
          <div className='relative'>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => router.push('/ptm/notifications')}
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
                <AvatarFallback>{getInitials()}</AvatarFallback>
              </Avatar>
              <div className='hidden sm:block text-left'>
                <p className='text-sm font-medium'>{getDisplayName()}</p>
                <p className='text-xs text-muted-foreground'>{user?.email}</p>
              </div>
              <ChevronDown className='h-4 w-4 text-muted-foreground' />
            </button>

            {/* User Dropdown */}
            {showUserMenu && (
              <div className='absolute right-0 top-full mt-2 w-56 bg-background border rounded-md shadow-lg z-50'>
                <div className='p-2'>
                  <div className='py-2'>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => router.push('/ptm/profile')}
                    >
                      <User className='mr-2 h-4 w-4' />
                      Profile
                    </Button>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => router.push('/ptm/settings')}
                    >
                      <Settings className='mr-2 h-4 w-4' />
                      Settings
                    </Button>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => router.push('/')}
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
}

export default PTMHeader;
