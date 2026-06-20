/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM module top header with search and notifications
 */

'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { ChevronDown, LogOut, Plus, Search, User } from 'lucide-react';
import { useLazyGetPmGlobalSearchQuery } from '@/modules/pm/api';
import type { PMGlobalSearchItem } from '@/modules/pm/types/api';
import { PMGlobalSearchDropdown } from '../search';
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
import { CreateWorkItemDialog } from '../work-items';

function titleFromPath(pathname: string): string {
  if (pathname.startsWith('/pm/dashboard')) return 'Dashboard';
  if (pathname.startsWith('/pm/my-work')) return 'My work';
  if (pathname.startsWith('/pm/settings')) return 'Settings';
  if (pathname.includes('/optimization-runs/')) return 'Optimization run';
  if (pathname.includes('/optimization')) return 'Optimization';
  if (pathname.includes('/settings')) return 'Project settings';
  if (pathname.startsWith('/pm/projects')) return 'Projects';
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
  const [isCreateWorkItemOpen, setIsCreateWorkItemOpen] = useState(false);
  const { getDisplayName, getInitials, user } = useUser();

  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [triggerSearch, searchResult] = useLazyGetPmGlobalSearchQuery();
  const trimmedSearchQuery = searchQuery.trim();

  const initialProjectId = useMemo(() => {
    const match = pathname.match(/^\/pm\/projects\/(\d+)/);
    if (!match) {
      return undefined;
    }

    const projectId = Number(match[1]);
    return Number.isNaN(projectId) ? undefined : projectId;
  }, [pathname]);

  useEffect(() => {
    if (trimmedSearchQuery.length < 2) {
      setIsSearchOpen(false);
      return;
    }

    const timeoutId = window.setTimeout(() => {
      triggerSearch({
        q: trimmedSearchQuery,
        limit: 5,
        currentProjectId: initialProjectId,
      });
      setIsSearchOpen(true);
    }, 300);

    return () => window.clearTimeout(timeoutId);
  }, [initialProjectId, triggerSearch, trimmedSearchQuery]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!trimmedSearchQuery) return;

    const firstResult = searchResult.data?.groups
      .flatMap((group) => group.items)
      .at(0);

    if (firstResult) {
      router.push(firstResult.url);
      setIsSearchOpen(false);
      return;
    }

    router.push(`/pm/search?q=${encodeURIComponent(trimmedSearchQuery)}`);
    setIsSearchOpen(false);
  };

  const handleSearchItemSelect = (item: PMGlobalSearchItem) => {
    router.push(item.url);
    setSearchQuery('');
    setIsSearchOpen(false);
  };

  const handleViewAllSearchResults = () => {
    if (!trimmedSearchQuery) return;
    router.push(`/pm/search?q=${encodeURIComponent(trimmedSearchQuery)}`);
    setIsSearchOpen(false);
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
            placeholder='Search by name or key...'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onFocus={() => {
              if (trimmedSearchQuery.length >= 2) {
                setIsSearchOpen(true);
              }
            }}
            onKeyDown={(event) => {
              if (event.key === 'Escape') {
                setIsSearchOpen(false);
              }
            }}
            className='border-muted-foreground/20 bg-muted/40 pl-9'
            aria-label='Search projects'
          />
          {isSearchOpen && trimmedSearchQuery.length >= 2 && (
            <PMGlobalSearchDropdown
              groups={searchResult.data?.groups ?? []}
              isLoading={searchResult.isFetching}
              isError={searchResult.isError}
              query={trimmedSearchQuery}
              onSelect={handleSearchItemSelect}
              onViewAll={handleViewAllSearchResults}
            />
          )}
        </form>
      </div>

      <div className='flex items-center gap-1'>
        <Button
          type='button'
          className='mr-2 hidden shadow-sm sm:inline-flex'
          onClick={() => setIsCreateWorkItemOpen(true)}
        >
          <Plus className='h-4 w-4' />
          Create
        </Button>

        <Button
          type='button'
          size='icon'
          className='mr-2 sm:hidden'
          onClick={() => setIsCreateWorkItemOpen(true)}
          aria-label='Create work item'
        >
          <Plus className='h-4 w-4' />
        </Button>

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

      <CreateWorkItemDialog
        open={isCreateWorkItemOpen}
        onOpenChange={setIsCreateWorkItemOpen}
        initialProjectId={initialProjectId}
      />
    </header>
  );
}
