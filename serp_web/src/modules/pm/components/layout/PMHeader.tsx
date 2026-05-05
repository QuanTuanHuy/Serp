/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM module top header with search and notifications
 */

'use client';

import React, { useMemo, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { ChevronDown, LogOut, Search, User } from 'lucide-react';
import { useUser } from '@/modules/account';
import { NotificationButton } from '@/modules/notifications';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  ThemeToggle,
} from '@/shared/components';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { cn } from '@/shared/utils';

function titleFromPath(pathname: string): string {
  if (pathname.startsWith('/pm/dashboard')) return 'Dashboard';
  if (pathname.startsWith('/pm/my-work')) return 'My work';
  if (pathname.startsWith('/pm/projects')) return 'Projects';
  if (pathname.startsWith('/pm/settings')) return 'Settings';
  return 'Project Management';
}

interface PMHeaderProps {
  className?: string;
}

export function PMHeader({ className }: PMHeaderProps) {
  const router = useRouter();
  const pathname = usePathname();
  const title = useMemo(() => titleFromPath(pathname), [pathname]);
  const [searchQuery, setSearchQuery] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const { getDisplayName, getInitials, user } = useUser();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Reserved for PM-wide search once APIs exist.
    if (!searchQuery.trim()) return;
  };

  const handleProfileClick = () => {
    setShowUserMenu(false);
    router.push('/profile');
  };

  const handleLogout = () => {
    setShowUserMenu(false);
    router.push('/auth');
  };

  return (
    <header
      className={cn(
        'sticky top-0 z-30 flex flex-col gap-4 border-b bg-background/95 px-6 py-4 backdrop-blur supports-[backdrop-filter]:bg-background/80 lg:flex-row lg:items-center lg:justify-between',
        className
      )}
    >
      <h2 className='text-xl font-bold tracking-tight text-blue-950 dark:text-blue-100'>
        {title}
      </h2>

      <div className='flex flex-1 flex-wrap items-center justify-end gap-3 lg:mx-8 lg:max-w-xl'>
        <form
          onSubmit={handleSearch}
          className='relative min-w-[200px] flex-1'
          role='search'
        >
          <Search
            className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground'
            aria-hidden
          />
          <Input
            type='search'
            placeholder='Search by name or key…'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className='border-muted-foreground/20 bg-muted/40 pl-9'
            aria-label='Search projects'
          />
        </form>
      </div>

      <div className='flex items-center gap-1'>
        <NotificationButton />
        <ThemeToggle />

        <div className='relative'>
          <button
            type='button'
            onClick={() => setShowUserMenu((current) => !current)}
            className='flex items-center gap-2 rounded-lg p-2 transition-colors hover:bg-muted'
            aria-haspopup='menu'
            aria-expanded={showUserMenu}
          >
            <Avatar className='h-8 w-8'>
              {user?.avatarUrl && (
                <AvatarImage src={user.avatarUrl} alt={getDisplayName()} />
              )}
              <AvatarFallback>{getInitials()}</AvatarFallback>
            </Avatar>
            <div className='hidden text-left sm:block'>
              <p className='text-sm font-medium'>{getDisplayName()}</p>
              <p className='text-xs text-muted-foreground'>{user?.email}</p>
            </div>
            <ChevronDown className='h-4 w-4 text-muted-foreground' />
          </button>

          {showUserMenu && (
            <div className='absolute right-0 top-full z-50 mt-2 w-52 rounded-md border bg-background shadow-lg'>
              <div className='p-2'>
                <Button
                  type='button'
                  variant='ghost'
                  className='w-full justify-start'
                  onClick={handleProfileClick}
                >
                  <User className='mr-2 h-4 w-4' />
                  Profile
                </Button>

                <div className='mt-2 border-t pt-2'>
                  <Button
                    type='button'
                    variant='ghost'
                    className='w-full justify-start text-destructive hover:text-destructive'
                    onClick={handleLogout}
                  >
                    <LogOut className='mr-2 h-4 w-4' />
                    Logout
                  </Button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {showUserMenu && (
        <div
          className='fixed inset-0 z-40'
          onClick={() => setShowUserMenu(false)}
        />
      )}
    </header>
  );
}
