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
  AvatarImage,
  ThemeToggle,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { useAuth, useModules, useUser } from '@/modules/account';
import { getModuleIcon, getModuleRoute } from '@/shared/constants/moduleIcons';

export function Header() {
  const { isAuthenticated, user, logout } = useAuth();
  const { getInitials, getDisplayName } = useUser();
  const { modules: myModules } = useModules();

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
            {isAuthenticated && (
              <NavigationMenuItem>
                <NavigationMenuLink href='/home'>Home</NavigationMenuLink>
              </NavigationMenuItem>
            )}
            <NavigationMenuItem>
              {isAuthenticated && myModules.length > 0 ? (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <button className='group inline-flex h-10 w-max items-center justify-center rounded-md bg-background px-4 py-2 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground focus:outline-none'>
                      My Apps
                    </button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align='start' className='w-56'>
                    {myModules.slice(0, 6).map((module) => {
                      const iconConfig = getModuleIcon(module.code);
                      const IconComponent = iconConfig?.icon;
                      return (
                        <DropdownMenuItem
                          key={module.code}
                          onClick={() =>
                            router.push(getModuleRoute(module.code))
                          }
                          className='cursor-pointer'
                        >
                          <div className='flex items-center gap-3 w-full'>
                            {IconComponent && (
                              <div
                                className={`w-8 h-8 rounded flex items-center justify-center ${
                                  iconConfig?.bgColor || 'bg-gray-100'
                                }`}
                              >
                                <IconComponent
                                  className={`w-4 h-4 ${
                                    iconConfig?.color || 'text-gray-600'
                                  }`}
                                />
                              </div>
                            )}
                            <span className='text-sm'>{module.name}</span>
                          </div>
                        </DropdownMenuItem>
                      );
                    })}
                    {myModules.length > 6 && (
                      <>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          onClick={() => router.push('/apps')}
                          className='cursor-pointer text-primary'
                        >
                          View all applications →
                        </DropdownMenuItem>
                      </>
                    )}
                  </DropdownMenuContent>
                </DropdownMenu>
              ) : (
                <NavigationMenuLink href='/apps'>Apps</NavigationMenuLink>
              )}
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
              <NavigationMenuLink href='/subscription'>
                Pricing
              </NavigationMenuLink>
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
              <div className='relative group'>
                <Avatar className='h-8 w-8 cursor-pointer'>
                  {user?.avatarUrl && (
                    <AvatarImage src={user.avatarUrl} alt={getDisplayName()} />
                  )}
                  {user?.avatarUrl ? null : (
                    <AvatarFallback>{getInitials()}</AvatarFallback>
                  )}
                </Avatar>
                {/* Simple dropdown on hover */}
                <div className='absolute left-0 top-full mt-2 w-48 bg-background border rounded-md shadow-md opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200'>
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
              <div className='hidden sm:block text-left'>
                <p className='text-sm font-medium'>
                  {getDisplayName() || 'User'}
                </p>
                <p className='text-xs text-muted-foreground'>{user?.email}</p>
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
