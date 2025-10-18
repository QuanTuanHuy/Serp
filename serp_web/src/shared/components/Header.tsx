/**
 * Header Component
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  Button,
  NavigationMenu,
  NavigationMenuList,
  NavigationMenuItem,
  NavigationMenuLink,
  Avatar,
  AvatarFallback,
  ThemeToggle,
} from '@/shared/components/ui';
import { useAuth, useUser } from '@/modules/account';

export function Header() {
  const { isAuthenticated, user, logout } = useAuth();
  const { getInitials, getDisplayName } = useUser();

  const router = useRouter();

  const handleLogin = () => {
    router.push('/auth');
  };

  const handleSignup = () => {
    router.push('/auth');
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <header className='sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60'>
      <div className='container mx-auto px-4 flex h-16 items-center justify-between'>
        {/* Logo */}
        <div className='flex items-center space-x-2'>
          <Link href='/' className='flex items-center space-x-2'>
            <div className='h-8 w-8 bg-primary rounded-md flex items-center justify-center'>
              <span className='text-primary-foreground font-bold text-sm'>
                S
              </span>
            </div>
            <span className='font-bold text-xl'>SERP</span>
          </Link>
        </div>

        {/* Navigation */}
        <NavigationMenu className='hidden md:flex'>
          <NavigationMenuList>
            <NavigationMenuItem>
              <NavigationMenuLink href='/apps'>Apps</NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
              <NavigationMenuLink href='/industries'>
                Industries
              </NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
              <NavigationMenuLink href='/community'>
                Community
              </NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
              <NavigationMenuLink href='/subscription'>Pricing</NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
              <NavigationMenuLink href='/help'>Help</NavigationMenuLink>
            </NavigationMenuItem>
          </NavigationMenuList>
        </NavigationMenu>

        {/* Right Section */}
        <div className='flex items-center space-x-4'>
          <ThemeToggle />

          {isAuthenticated ? (
            <div className='flex items-center space-x-3'>
              <div className='hidden sm:block text-right'>
                <p className='text-sm font-medium'>
                  {getDisplayName() || 'User'}
                </p>
                <p className='text-xs text-muted-foreground'>{user?.email}</p>
              </div>
              <div className='relative group'>
                <Avatar className='cursor-pointer'>
                  <AvatarFallback>{getInitials()}</AvatarFallback>
                </Avatar>
                {/* Simple dropdown on hover */}
                <div className='absolute right-0 top-full mt-2 w-48 bg-background border rounded-md shadow-md opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200'>
                  <div className='p-2'>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => router.push('/profile')}
                    >
                      Profile
                    </Button>
                    <Button
                      variant='ghost'
                      className='w-full justify-start'
                      onClick={() => router.push('/settings')}
                    >
                      Settings
                    </Button>
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
            </div>
          ) : (
            <div className='flex items-center space-x-2'>
              <Button variant='ghost' size='sm' onClick={handleLogin}>
                Login
              </Button>
              <Button size='sm' onClick={handleSignup}>
                Sign Up
              </Button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
