'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  FolderKanban,
  Brain,
  Calendar,
  FileText,
  Bot,
  Bell,
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/shared/utils';

interface SidebarProps {
  className?: string;
}

const navigationItems = [
  {
    title: 'Dashboard',
    href: '/ptm/dashboard',
    icon: LayoutDashboard,
    description: 'Overview of your tasks and projects',
  },
  {
    title: 'Project Management',
    href: '/ptm/projects',
    icon: FolderKanban,
    description: 'Manage your projects and teams',
  },
  {
    title: 'Optimize Your Tasks',
    href: '/ptm/schedule',
    icon: Brain,
    description: 'AI-powered task optimization',
  },
  {
    title: 'Daily Calendar',
    href: '/ptm/calendar',
    icon: Calendar,
    description: 'Your daily schedule and events',
  },
  {
    title: 'Notifications',
    href: '/ptm/notifications',
    icon: Bell,
    description: 'Alerts and notifications',
  },
  {
    title: 'Note',
    href: '/ptm/note-dashboard',
    icon: FileText,
    description: 'Personal notes and documentation',
  },
  {
    title: 'Chat Bot',
    href: '/ptm/chat',
    icon: Bot,
    description: 'AI assistant for productivity',
  },
];

export const Sidebar: React.FC<SidebarProps> = ({ className }) => {
  const pathname = usePathname();

  return (
    <div
      className={cn(
        'flex h-full w-64 flex-col border-r border-border bg-background',
        className
      )}
    >
      {/* Header */}
      <div className='flex h-16 items-center border-b border-border px-6'>
        <h2 className='text-lg font-semibold'>PTM Dashboard</h2>
      </div>

      {/* Navigation */}
      <nav className='flex-1 space-y-1 p-4'>
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                'hover:bg-accent hover:text-accent-foreground',
                isActive
                  ? 'bg-accent text-accent-foreground'
                  : 'text-muted-foreground'
              )}
            >
              <Icon className='h-5 w-5' />
              <span className='flex-1'>{item.title}</span>
              {isActive && <ChevronRight className='h-4 w-4' />}
            </Link>
          );
        })}
      </nav>

      {/* Footer */}
      <div className='border-t border-border p-4'>
        <div className='text-xs text-muted-foreground'>
          Personal Task Management
        </div>
        <div className='text-xs text-muted-foreground'>Version 1.0.0</div>
      </div>
    </div>
  );
};

export default Sidebar;
