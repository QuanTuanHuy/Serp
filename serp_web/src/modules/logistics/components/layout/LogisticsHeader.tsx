/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Header Component
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
import {
  Search,
  Bell,
  Settings,
  User,
  ChevronDown,
  Home,
  Package,
  Truck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useUser } from '@/modules/account';

interface LogisticsHeaderProps {
  className?: string;
  scrollContainerRef?: React.RefObject<HTMLElement | null>;
}

export const LogisticsHeader: React.FC<LogisticsHeaderProps> = ({
  className,
  scrollContainerRef,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [notifications, setNotifications] = useState(2);
  const [hidden, setHidden] = useState(false);

  const { getInitials, getDisplayName, user } = useUser();

  const router = useRouter();
  const pathname = usePathname();

  const getPageTitle = () => {
    const path = pathname.split('/').pop();
    switch (path) {
      case 'orders':
        return 'Đơn hàng';
      case 'shipments':
        return 'Lô hàng';
      case 'products':
        return 'Sản phẩm';
      case 'categories':
        return 'Danh mục';
      case 'facilities':
        return 'Cơ sở';
      case 'inventory-items':
        return 'Tồn kho';
      case 'suppliers':
        return 'Nhà cung cấp';
      case 'customers':
        return 'Khách hàng';
      case 'settings':
        return 'Cài đặt';
      default:
        return 'Logistics';
    }
  };

  const getBreadcrumbs = () => {
    const segments = pathname.split('/').filter(Boolean);
    return segments.map((segment, index) => ({
      name:
        segment === 'logistics'
          ? 'Logistics'
          : segment.charAt(0).toUpperCase() +
            segment.slice(1).replace('-', ' '),
      href: '/' + segments.slice(0, index + 1).join('/'),
      isLast: index === segments.length - 1,
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      // Navigate to search results or filter current page
      console.log('Search:', searchQuery);
    }
  };

  const handleLogout = () => {
    router.push('/auth');
  };

  const quickActions = [
    {
      label: 'Sản phẩm mới',
      icon: Package,
      href: '/logistics/products?action=new',
    },
    {
      label: 'Lô hàng mới',
      icon: Truck,
      href: '/logistics/shipments?action=new',
    },
  ];

  // Auto-hide header on scroll down
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

        {/* Center Section - Search Bar */}
        <div className='flex-1 max-w-md mx-4'>
          <form onSubmit={handleSearch} className='relative'>
            <Search className='absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground' />
            <Input
              type='search'
              placeholder='Tìm kiếm đơn hàng, sản phẩm, lô hàng...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-9 pr-4'
            />
          </form>
        </div>

        {/* Right Section - Actions & User */}
        <div className='flex items-center space-x-2'>
          {/* Quick Actions */}
          <div className='hidden lg:flex items-center space-x-1 mr-2'>
            {quickActions.map((action) => (
              <Button
                key={action.label}
                variant='ghost'
                size='sm'
                onClick={() => router.push(action.href)}
                className='text-sm'
              >
                <action.icon className='h-4 w-4 mr-2' />
                {action.label}
              </Button>
            ))}
          </div>

          {/* Notifications */}
          <div className='relative'>
            <Button variant='ghost' size='icon' className='relative'>
              <Bell className='h-5 w-5' />
              {notifications > 0 && (
                <span className='absolute top-1 right-1 h-4 w-4 bg-destructive text-destructive-foreground text-xs rounded-full flex items-center justify-center'>
                  {notifications}
                </span>
              )}
            </Button>
          </div>

          {/* Theme Toggle */}
          <ThemeToggle />

          {/* Settings */}
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics/settings')}
          >
            <Settings className='h-5 w-5' />
          </Button>

          {/* User Menu */}
          <div className='relative'>
            <Button
              variant='ghost'
              className='flex items-center space-x-2'
              onClick={() => setShowUserMenu(!showUserMenu)}
            >
              <Avatar className='h-8 w-8'>
                {user?.avatarUrl && (
                  <AvatarImage src={user.avatarUrl} alt={getDisplayName()} />
                )}
                <AvatarFallback>{getInitials()}</AvatarFallback>
              </Avatar>
              <span className='hidden md:inline-block text-sm font-medium'>
                {getDisplayName()}
              </span>
              <ChevronDown className='h-4 w-4' />
            </Button>

            {showUserMenu && (
              <>
                <div
                  className='fixed inset-0 z-40'
                  onClick={() => setShowUserMenu(false)}
                />
                <div className='absolute right-0 mt-2 w-56 bg-background border rounded-lg shadow-lg z-50'>
                  <div className='p-4 border-b'>
                    <p className='text-sm font-medium'>{getDisplayName()}</p>
                    <p className='text-xs text-muted-foreground'>
                      {user?.email}
                    </p>
                  </div>

                  <div className='py-2'>
                    <button
                      onClick={() => {
                        router.push('/home');
                        setShowUserMenu(false);
                      }}
                      className='w-full px-4 py-2 text-left text-sm hover:bg-accent flex items-center space-x-2'
                    >
                      <Home className='h-4 w-4' />
                      <span>Trang chủ</span>
                    </button>
                    <button
                      onClick={() => {
                        router.push('/settings/profile');
                        setShowUserMenu(false);
                      }}
                      className='w-full px-4 py-2 text-left text-sm hover:bg-accent flex items-center space-x-2'
                    >
                      <User className='h-4 w-4' />
                      <span>Hồ sơ</span>
                    </button>
                    <button
                      onClick={() => {
                        router.push('/settings');
                        setShowUserMenu(false);
                      }}
                      className='w-full px-4 py-2 text-left text-sm hover:bg-accent flex items-center space-x-2'
                    >
                      <Settings className='h-4 w-4' />
                      <span>Cài đặt</span>
                    </button>
                  </div>

                  <div className='border-t py-2'>
                    <button
                      onClick={handleLogout}
                      className='w-full px-4 py-2 text-left text-sm text-destructive hover:bg-accent'
                    >
                      Đăng xuất
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
