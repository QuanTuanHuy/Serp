/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM module sidebar navigation
 */

'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  ArrowLeft,
  Briefcase,
  ClipboardList,
  FolderKanban,
  LayoutDashboard,
  Settings,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { cn } from '@/shared/utils';
import React from 'react';

const NAV_ITEMS: { href: string; label: string; icon: LucideIcon }[] = [
  { href: '/pm/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/pm/my-work', label: 'My work', icon: ClipboardList },
  { href: '/pm/projects', label: 'Projects', icon: FolderKanban },
  { href: '/pm/settings', label: 'Settings', icon: Settings },
];

export function PMSidebar() {
  const pathname = usePathname();
  const [isModuleHovered, setIsModuleHovered] = React.useState(false);

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-40 flex h-screen w-64 flex-col',
        'border-r border-zinc-800 bg-zinc-950 text-zinc-400'
      )}
    >
      <div className='border-b border-zinc-800 px-5 py-6'>
        <Link
          href='/home'
          className='group flex items-center space-x-3 transition-colors'
          onMouseEnter={() => setIsModuleHovered(true)}
          onMouseLeave={() => setIsModuleHovered(false)}
        >
          <div className='flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground transition-colors group-hover:bg-primary/90'>
            {isModuleHovered ? (
              <ArrowLeft className='h-5 w-5' />
            ) : (
              <Briefcase className='h-5 w-5' />
            )}
          </div>

          <div>
            <h1 className='text-lg font-bold tracking-tight text-white'>
              Project Management
            </h1>
            <p className='mt-0.5 text-xs text-zinc-500'>Enterprise PM</p>
          </div>
        </Link>
      </div>

      <nav
        className='flex flex-1 flex-col gap-0.5 p-3'
        aria-label='PM navigation'
      >
        {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
          const active = pathname === href || pathname.startsWith(`${href}/`);

          return (
            <Link
              key={href}
              href={href}
              className={cn(
                'flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors',
                active
                  ? 'border-l-[3px] border-blue-500 bg-blue-950/40 text-white'
                  : 'border-l-[3px] border-transparent hover:bg-zinc-900 hover:text-zinc-200'
              )}
            >
              <Icon className='h-5 w-5 shrink-0 opacity-90' aria-hidden />
              {label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
