// Purchase Header Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
import { Search, Plus, Settings, User, ChevronDown, Home } from 'lucide-react';
import { NotificationButton } from '@/modules/notifications';
import { cn } from '@/shared/utils';
import { useUser } from '@/modules/account';

interface PurchaseHeaderProps {
  className?: string;
  scrollContainerRef?: React.RefObject<HTMLElement | null>;
}

export const PurchaseHeader: React.FC<PurchaseHeaderProps> = ({
  className,
  scrollContainerRef,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [hidden, setHidden] = useState(false);

  const { getInitials, getDisplayName, user } = useUser();

  const router = useRouter();
  const pathname = usePathname();

  const getPageTitle = () => {
    const path = pathname.split('/').pop();
    switch (path) {
      case 'dashboard':
        return 'Dashboard';
      case 'suppliers':
        return 'Suppliers';
      case 'products':
        return 'Products';
      case 'purchase-orders':
        return 'Purchase Orders';
      case 'facilities':
        return 'Facilities';
      case 'categories':
        return 'Categories';
      case 'shipments':
        return 'Shipments';
      case 'reports':
        return 'Reports';
      case 'settings':
        return 'Settings';
      default:
        return 'Purchase';
    }
  };

  const getBreadcrumbs = () => {
    const segments = pathname.split('/').filter(Boolean);
    return segments.map((segment, index) => ({
      name:
        segment === 'purchase'
          ? 'Purchase'
          : segment.charAt(0).toUpperCase() +
            segment.slice(1).replace(/-/g, ' '),
      href: '/' + segments.slice(0, index + 1).join('/'),
      isLast: index === segments.length - 1,
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(
        `/purchase/dashboard?search=${encodeURIComponent(searchQuery)}`
      );
    }
  };

  const handleLogout = () => {
    router.push('/auth');
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
      const current = container
        ? container.scrollTop
        : typeof window !== 'undefined'
          ? window.scrollY
          : 0;
      const delta = current - last;
      last = current;

      if (delta > threshold) {
        setHidden(true);
      } else if (delta < -threshold) {
        setHidden(false);
      }
    };

    const handleScroll = () => {
      if (!ticking) {
        requestAnimationFrame(() => {
          onScroll();
          ticking = false;
        });
        ticking = true;
      }
    };

    if (container) {
      container.addEventListener('scroll', handleScroll);
      return () => container.removeEventListener('scroll', handleScroll);
    } else if (typeof window !== 'undefined') {
      window.addEventListener('scroll', handleScroll);
      return () => window.removeEventListener('scroll', handleScroll);
    }
  }, [scrollContainerRef]);

  return (
    <header
      className={cn(
        'sticky top-0 z-40 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 transition-transform duration-300',
        hidden && '-translate-y-full',
        className
      )}
    >
      <div className='flex h-16 items-center px-4 gap-4'>
        {/* Breadcrumbs */}
        <div className='flex items-center gap-2 flex-1'>
          <nav className='flex items-center space-x-1 text-sm text-muted-foreground'>
            <Link
              href='/home'
              className='hover:text-foreground transition-colors'
            >
              <Home className='h-4 w-4' />
            </Link>
            {getBreadcrumbs().map((crumb, index) => (
              <React.Fragment key={crumb.href}>
                <span>/</span>
                {crumb.isLast ? (
                  <span className='font-medium text-foreground'>
                    {crumb.name}
                  </span>
                ) : (
                  <Link
                    href={crumb.href}
                    className='hover:text-foreground transition-colors'
                  >
                    {crumb.name}
                  </Link>
                )}
              </React.Fragment>
            ))}
          </nav>
        </div>

        {/* Search Bar */}
        <form
          onSubmit={handleSearch}
          className='hidden md:flex flex-1 max-w-md'
        >
          <div className='relative w-full'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              type='search'
              placeholder='Search suppliers, products, orders...'
              className='pl-10 w-full'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </form>

        {/* Right Section */}
        <div className='flex items-center gap-2'>
          <ThemeToggle />
          <NotificationButton />

          {/* User Menu */}
          <div className='relative'>
            <Button
              variant='ghost'
              className='flex items-center gap-2'
              onClick={() => setShowUserMenu(!showUserMenu)}
            >
              <Avatar className='h-8 w-8'>
                <AvatarImage src={user?.avatarUrl} alt={getDisplayName()} />
                <AvatarFallback>{getInitials()}</AvatarFallback>
              </Avatar>
              <span className='hidden md:inline-block text-sm font-medium'>
                {getDisplayName()}
              </span>
              <ChevronDown className='h-4 w-4' />
            </Button>

            {showUserMenu && (
              <div className='absolute right-0 mt-2 w-48 rounded-md bg-popover p-1 shadow-lg border'>
                <Link href='/profile'>
                  <Button variant='ghost' className='w-full justify-start'>
                    <User className='mr-2 h-4 w-4' />
                    Profile
                  </Button>
                </Link>
                <Link href='/purchase/settings'>
                  <Button variant='ghost' className='w-full justify-start'>
                    <Settings className='mr-2 h-4 w-4' />
                    Settings
                  </Button>
                </Link>
                <Button
                  variant='ghost'
                  className='w-full justify-start text-destructive'
                  onClick={handleLogout}
                >
                  Logout
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile Search */}
      <div className='md:hidden px-4 pb-3'>
        <form onSubmit={handleSearch}>
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              type='search'
              placeholder='Search...'
              className='pl-10 w-full'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </form>
      </div>
    </header>
  );
};
