/**
 * Navigation Menu Component
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

'use client';

import * as React from 'react';
import { cn } from '@/shared/utils';

interface NavigationMenuProps extends React.HTMLAttributes<HTMLElement> {
  children: React.ReactNode;
}

const NavigationMenu = React.forwardRef<HTMLElement, NavigationMenuProps>(
  ({ className, children, ...props }, ref) => (
    <nav
      ref={ref}
      className={cn(
        'relative z-10 flex max-w-max flex-1 items-center justify-center',
        className
      )}
      {...props}
    >
      {children}
    </nav>
  )
);
NavigationMenu.displayName = 'NavigationMenu';

interface NavigationMenuListProps
  extends React.HTMLAttributes<HTMLUListElement> {
  children: React.ReactNode;
}

const NavigationMenuList = React.forwardRef<
  HTMLUListElement,
  NavigationMenuListProps
>(({ className, children, ...props }, ref) => (
  <ul
    ref={ref}
    className={cn(
      'group flex flex-1 list-none items-center justify-center space-x-1',
      className
    )}
    {...props}
  >
    {children}
  </ul>
));
NavigationMenuList.displayName = 'NavigationMenuList';

interface NavigationMenuItemProps extends React.HTMLAttributes<HTMLLIElement> {
  children: React.ReactNode;
}

const NavigationMenuItem = React.forwardRef<
  HTMLLIElement,
  NavigationMenuItemProps
>(({ className, children, ...props }, ref) => (
  <li ref={ref} className={cn('relative', className)} {...props}>
    {children}
  </li>
));
NavigationMenuItem.displayName = 'NavigationMenuItem';

interface NavigationMenuLinkProps
  extends React.AnchorHTMLAttributes<HTMLAnchorElement> {
  children: React.ReactNode;
}

const NavigationMenuLink = React.forwardRef<
  HTMLAnchorElement,
  NavigationMenuLinkProps
>(({ className, children, ...props }, ref) => (
  <a
    ref={ref}
    className={cn(
      'group inline-flex h-10 w-max items-center justify-center rounded-md bg-background px-4 py-2 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground focus:outline-none disabled:pointer-events-none disabled:opacity-50 data-[active]:bg-accent/50 data-[state=open]:bg-accent/50',
      className
    )}
    {...props}
  >
    {children}
  </a>
));
NavigationMenuLink.displayName = 'NavigationMenuLink';

export {
  NavigationMenu,
  NavigationMenuList,
  NavigationMenuItem,
  NavigationMenuLink,
};
