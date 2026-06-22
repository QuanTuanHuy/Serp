/**
 * Author: Tran Ngoc Hung
 * Description: Part of Serp Project - TTCRS dispatcher header
 */

'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Button,
  ThemeToggle,
} from '@/shared/components/ui';
import { ChevronDown, Home, User } from 'lucide-react';
import { cn } from '@/shared/utils';
import { useAuth, useUser } from '@/modules/account';
import { NotificationButton } from '@/modules/notifications';

// ---------------------------------------------------------------------------
// Breadcrumb label overrides — maps path segments to display names
// ---------------------------------------------------------------------------

const SEGMENT_LABELS: Record<string, string> = {
  ttcrs: 'TTCRS',
  dispatcher: 'Dispatcher',
  locations: 'Locations',
  requests: 'Requests',
  trucks: 'Trucks',
  trailers: 'Trailers',
  drivers: 'Drivers',
  plans: 'Transport Plans',
};

function humanise(segment: string): string {
  return (
    SEGMENT_LABELS[segment] ??
    segment.charAt(0).toUpperCase() + segment.slice(1)
  );
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

interface TtcrsHeaderProps {
  className?: string;
  scrollContainerRef?: React.RefObject<HTMLElement | null>;
}

export const TtcrsHeader: React.FC<TtcrsHeaderProps> = ({
  className,
  scrollContainerRef,
}) => {
  const router = useRouter();
  const pathname = usePathname();
  const { logout } = useAuth();
  const { getDisplayName, user, getUserRole } = useUser();

  const [showUserMenu, setShowUserMenu] = useState(false);
  const [hidden, setHidden] = useState(false);

  // ── Hide-on-scroll ───────────────────────────────────────────────────────
  useEffect(() => {
    const container = scrollContainerRef?.current ?? null;
    let last = container
      ? container.scrollTop
      : typeof window !== 'undefined'
        ? window.scrollY
        : 0;
    let ticking = false;
    const THRESHOLD = 8;

    const onScroll = () => {
      const current = container ? container.scrollTop : window.scrollY;
      if (!ticking) {
        window.requestAnimationFrame(() => {
          if (current <= 0) setHidden(false);
          else if (current > last + THRESHOLD) setHidden(true);
          else if (current < last - THRESHOLD) setHidden(false);
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
    return () => el.removeEventListener('scroll', onScroll as EventListener);
  }, [scrollContainerRef]);

  // ── Breadcrumbs ──────────────────────────────────────────────────────────
  const breadcrumbs = pathname
    .split('/')
    .filter(Boolean)
    .map((segment, index, arr) => ({
      name: humanise(segment),
      href: '/' + arr.slice(0, index + 1).join('/'),
      isLast: index === arr.length - 1,
    }));

  const handleLogout = () => {
    logout();
    router.push('/auth');
  };

  const initials = getDisplayName()
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

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
        {/* ── Breadcrumbs ── */}
        <nav className='flex items-center space-x-2 text-sm'>
          {breadcrumbs.map((crumb, index) => (
            <React.Fragment key={crumb.href}>
              {index > 0 && <span className='text-muted-foreground'>/</span>}
              {crumb.isLast ? (
                <span className='font-medium'>{crumb.name}</span>
              ) : (
                <Link
                  href={crumb.href}
                  className='text-muted-foreground transition-colors hover:text-foreground'
                >
                  {crumb.name}
                </Link>
              )}
            </React.Fragment>
          ))}
        </nav>

        {/* ── Right actions ── */}
        <div className='flex items-center space-x-3'>
          <NotificationButton settingsPath='' />
          <ThemeToggle />

          {/* User menu */}
          <div className='relative'>
            <button
              onClick={() => setShowUserMenu(!showUserMenu)}
              className='flex items-center gap-2 rounded-lg p-2 transition-colors hover:bg-muted'
            >
              <Avatar className='h-8 w-8'>
                {user?.avatarUrl && (
                  <AvatarImage src={user.avatarUrl} alt={getDisplayName()} />
                )}
                <AvatarFallback className='bg-orange-600 text-white text-xs font-semibold'>
                  {initials}
                </AvatarFallback>
              </Avatar>
              <div className='hidden sm:block text-left'>
                <p className='text-sm font-medium leading-none'>
                  {getDisplayName()}
                </p>
                <p className='mt-0.5 text-xs text-muted-foreground'>
                  {getUserRole()}
                </p>
              </div>
              <ChevronDown className='h-4 w-4 text-muted-foreground' />
            </button>

            {showUserMenu && (
              <div className='absolute right-0 top-full mt-2 w-52 rounded-md border bg-background shadow-lg z-50'>
                <div className='p-2'>
                  <Button
                    variant='ghost'
                    className='w-full justify-start'
                    onClick={() => {
                      setShowUserMenu(false);
                      router.push('/profile');
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
                      router.push('/home');
                    }}
                  >
                    <Home className='mr-2 h-4 w-4' />
                    Back to SERP
                  </Button>
                  <div className='my-1 border-t' />
                  <Button
                    variant='ghost'
                    className='w-full justify-start text-destructive hover:text-destructive'
                    onClick={handleLogout}
                  >
                    Logout
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Backdrop to close user menu */}
      {showUserMenu && (
        <div
          className='fixed inset-0 z-40'
          onClick={() => setShowUserMenu(false)}
        />
      )}
    </header>
  );
};
