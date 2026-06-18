/**
 * School Bus - Topbar Component
 *
 * Part of Serp Project - Top header with search, notifications, theme toggle, and user menu
 */

'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import {
  Button,
  Avatar,
  AvatarFallback,
  AvatarImage,
  ThemeToggle,
  Input,
} from '@/shared/components';
import { Search, Settings, ChevronDown, Home } from 'lucide-react';
import { NotificationButton } from '@/modules/notifications';
import { cn } from '@/shared/utils';
import { useUser } from '@/modules/account';

interface SchoolBusTopbarProps {
  className?: string;
  scrollContainerRef?: React.RefObject<HTMLElement | null>;
}

export function SchoolBusTopbar({
  scrollContainerRef,
  className,
}: SchoolBusTopbarProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [hidden, setHidden] = useState(false);

  const router = useRouter();
  const pathname = usePathname();

  const { getInitials, getDisplayName, user } = useUser();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(
        `/school-bus/trips?search=${encodeURIComponent(searchQuery)}`
      );
    }
  };

  const handleLogout = () => {
    router.push('/auth');
  };

  const getBreadcrumbs = () => {
    const decodedPath = decodeURIComponent(pathname);
    const segments = decodedPath.split('/').filter(Boolean);
    return segments.map((segment, index) => {
      let name = segment.replace(/-/g, ' ');
      if (name.includes('&')) {
        name = name.split('&')[0];
      }
      if (segment.toLowerCase() === 'school-bus') {
        name = 'School Bus';
      } else {
        name = name.charAt(0).toUpperCase() + name.slice(1);
      }
      return {
        name,
        href: '/' + segments.slice(0, index + 1).join('/'),
        isLast: index === segments.length - 1,
      };
    });
  };

  useEffect(() => {
    const container = scrollContainerRef?.current ?? null;
    let last = container
      ? container.scrollTop
      : typeof window !== 'undefined'
        ? window.scrollY
        : 0;
    let ticking = false;
    const threshold = 8;

    const onScroll = () => {
      const current = container ? container.scrollTop : window.scrollY;
      if (!ticking) {
        window.requestAnimationFrame(() => {
          if (current <= 0) {
            setHidden(false);
          } else if (current > last + threshold) {
            setHidden(true);
          } else if (current < last - threshold) {
            setHidden(false);
          }
          last = current;
          ticking = false;
        });
        ticking = true;
      }
    };

    const el: HTMLElement | Window = container ?? window;
    el.addEventListener('scroll', onScroll, {
      passive: true,
    } as EventListenerOptions);

    return () => {
      el.removeEventListener('scroll', onScroll as EventListener);
    };
  }, [scrollContainerRef]);

  return (
    <header
      className={cn(
        'sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 transition-transform duration-200',
        hidden ? '-translate-y-16' : 'translate-y-0',
        className
      )}
      aria-hidden={hidden}
    >
      <div className='flex h-16 items-center justify-between px-6'>
        {/* Left Section - Breadcrumbs */}
        <div className='flex items-center space-x-4'>
          <nav className='hidden md:flex items-center space-x-2 text-sm'>
            {getBreadcrumbs().map((breadcrumb, index) => (
              <React.Fragment key={breadcrumb.href}>
                {index > 0 && <span className='text-muted-foreground'>/</span>}
                {breadcrumb.isLast ? (
                  <span className='font-medium text-foreground'>
                    {breadcrumb.name}
                  </span>
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
              placeholder='Search trips, students, routes...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-10 pr-4'
            />
          </form>
        </div>

        {/* Right Section - Actions & User */}
        <div className='flex items-center space-x-3'>
          {/* Notifications */}
          <NotificationButton
            settingsPath='/notifications/settings'
            allNotificationsPath='/notifications'
          />

          {/* Theme Toggle */}
          <ThemeToggle />

          {/* User Menu */}
          <div className='relative'>
            <button
              onClick={() => setShowUserMenu(!showUserMenu)}
              className='flex items-center gap-2 rounded-lg p-2 hover:bg-muted transition-colors'
            >
              <Avatar className='h-8 w-8'>
                {user?.avatarUrl && (
                  <AvatarImage src={user.avatarUrl} alt={getDisplayName()} />
                )}
                <AvatarFallback>{getInitials()}</AvatarFallback>
              </Avatar>
              <div className='hidden sm:block text-left'>
                <p className='text-sm font-medium text-foreground'>
                  {getDisplayName()}
                </p>
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
                      onClick={() => router.push('/school-bus/settings')}
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
