import Link from 'next/link';
import * as React from 'react';

import { ThemeToggle } from '@/components/theme-toggle';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface HeaderProps {
  className?: string;
}

export function Header({ className }: HeaderProps) {
  return (
    <header
      className={cn(
        'sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60',
        className
      )}
    >
      <div className='container flex h-16 items-center justify-between'>
        <div className='flex items-center space-x-6'>
          <Link href='/' className='flex items-center space-x-2'>
            <span className='font-bold text-xl'>SERP</span>
          </Link>
          <nav className='hidden md:flex space-x-6 text-sm font-medium'>
            <Link
              href='/dashboard'
              className='transition-colors hover:text-foreground/80 text-foreground/60'
            >
              Dashboard
            </Link>
            <Link
              href='/crm'
              className='transition-colors hover:text-foreground/80 text-foreground/60'
            >
              CRM
            </Link>
            <Link
              href='/accounting'
              className='transition-colors hover:text-foreground/80 text-foreground/60'
            >
              Accounting
            </Link>
            <Link
              href='/inventory'
              className='transition-colors hover:text-foreground/80 text-foreground/60'
            >
              Inventory
            </Link>
          </nav>
        </div>
        <div className='flex items-center space-x-4'>
          <ThemeToggle />
          <Button variant='ghost' size='sm'>
            Login
          </Button>
        </div>
      </div>
    </header>
  );
}
