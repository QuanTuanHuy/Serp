/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Jira-style top tabs for PM projects
 */

'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/shared/utils';

type TabItem = {
  key: string;
  label: string;
  href: string;
};

const TABS: TabItem[] = [
  { key: 'summary', label: 'Summary', href: '/pm/projects/summary' },
  { key: 'timeline', label: 'Timeline', href: '/pm/projects/timeline' },
  { key: 'kanban', label: 'Kanban board', href: '/pm/projects/kanban' },
  { key: 'calendar', label: 'Calendar', href: '/pm/projects/calendar' },
  { key: 'list', label: 'List', href: '/pm/projects/list' },
  { key: 'components', label: 'Components', href: '/pm/projects/components' },
];

function getActiveTabKey(pathname: string): string {
  const parts = pathname.split('/').filter(Boolean);
  const maybeKey = parts[parts.length - 1] || 'list';
  if (TABS.some((t) => t.key === maybeKey)) return maybeKey;
  return 'list';
}

export function PMProjectsTopTabs() {
  const pathname = usePathname();
  const activeKey = getActiveTabKey(pathname);

  return (
    <div className='-mx-1 overflow-x-auto border-b bg-background'>
      <nav
        className='flex min-w-max items-center gap-1 px-1'
        aria-label='Projects views'
      >
        {TABS.map((tab) => {
          const active = tab.key === activeKey;
          return (
            <Link
              key={tab.key}
              href={tab.href}
              className={cn(
                'relative px-3 py-3 text-sm font-medium text-muted-foreground transition-colors',
                'hover:text-foreground',
                active && 'text-foreground'
              )}
              aria-current={active ? 'page' : undefined}
            >
              <span className='whitespace-nowrap'>{tab.label}</span>
              <span
                className={cn(
                  'absolute inset-x-2 bottom-0 h-0.5 rounded-full',
                  active ? 'bg-primary' : 'bg-transparent'
                )}
                aria-hidden
              />
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
