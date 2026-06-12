/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile header
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Button,
  Input,
  ThemeToggle,
} from '@/shared/components';
import { ChevronDown, Home, Search, Settings, User } from 'lucide-react';
import { NotificationButton } from '@/modules/notifications';
import { useUser } from '@/modules/account';
import { cn } from '@/shared/utils';

interface FirstMileHeaderProps {
  className?: string;
  scrollContainerRef?: React.RefObject<HTMLElement | null>;
}

export const FirstMileHeader: React.FC<FirstMileHeaderProps> = ({
  className,
}) => {
  const pathname = usePathname();
  const router = useRouter();
  const [searchQuery, setSearchQuery] = React.useState('');
  const [showUserMenu, setShowUserMenu] = React.useState(false);

  const { user, getDisplayName, getInitials } = useUser();

  const breadcrumbs = React.useMemo(() => {
    const segments = pathname.split('/').filter(Boolean);
    return segments.map((segment, index) => ({
      name:
        segment === 'first-mile'
          ? 'First Mile'
          : segment.charAt(0).toUpperCase() +
            segment.slice(1).replace(/-/g, ' '),
      href: '/' + segments.slice(0, index + 1).join('/'),
      isLast: index === segments.length - 1,
    }));
  }, [pathname]);

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    router.push(
      `/first-mile/network/post-offices?search=${encodeURIComponent(searchQuery.trim())}`
    );
  };

  const handleLogout = () => {
    router.push('/auth');
  };

  return (
    <header
      className={cn(
        'sticky top-0 z-40 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60',
        className
      )}
    >
      <div className='flex h-16 items-center px-4 gap-4'>
        <div className='flex items-center gap-2 flex-1'>
          <nav className='flex items-center space-x-1 text-sm text-muted-foreground'>
            <Link
              href='/home'
              className='hover:text-foreground transition-colors'
            >
              <Home className='h-4 w-4' />
            </Link>
            {breadcrumbs.map((crumb) => (
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

        <form
          onSubmit={handleSearch}
          className='hidden md:flex flex-1 max-w-md'
        >
          <div className='relative w-full'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              type='search'
              placeholder='Search post offices, product types...'
              className='pl-10 w-full'
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
            />
          </div>
        </form>

        <div className='flex items-center gap-2'>
          <ThemeToggle />
          <NotificationButton />

          <div className='relative'>
            <Button
              variant='ghost'
              className='flex items-center gap-2'
              onClick={() => setShowUserMenu((prev) => !prev)}
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
              <div className='absolute right-0 mt-2 w-48 rounded-md bg-popover p-1 shadow-lg border z-50'>
                <Link href='/profile'>
                  <Button variant='ghost' className='w-full justify-start'>
                    <User className='mr-2 h-4 w-4' />
                    Profile
                  </Button>
                </Link>
                <Link href='/first-mile/network/post-offices'>
                  <Button variant='ghost' className='w-full justify-start'>
                    <Settings className='mr-2 h-4 w-4' />
                    First Mile Settings
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

      <div className='md:hidden px-4 pb-3'>
        <form onSubmit={handleSearch}>
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              type='search'
              placeholder='Search...'
              className='pl-10 w-full'
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
            />
          </div>
        </form>
      </div>
    </header>
  );
};
