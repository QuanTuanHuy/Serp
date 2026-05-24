/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Jira-style top tabs for PM projects
 */

'use client';

import Link from 'next/link';
import { useParams, usePathname } from 'next/navigation';
import { cn } from '@/shared/utils';

type TabItem = {
  key: string;
  label: string;
  href: (projectId: string) => string;
};

const TABS: TabItem[] = [
  {
    key: 'summary',
    label: 'Summary',
    href: (projectId) => `/pm/projects/${projectId}/summary`,
  },
  {
    key: 'timeline',
    label: 'Timeline',
    href: (projectId) => `/pm/projects/${projectId}/timeline`,
  },
  {
    key: 'board',
    label: 'Board',
    href: (projectId) => `/pm/projects/${projectId}/board`,
  },
  {
    key: 'list',
    label: 'List',
    href: (projectId) => `/pm/projects/${projectId}/list`,
  },
  {
    key: 'calendar',
    label: 'Calendar',
    href: (projectId) => `/pm/projects/${projectId}/calendar`,
  },
  {
    key: 'components',
    label: 'Components',
    href: (projectId) => `/pm/projects/${projectId}/components`,
  },
  {
    key: 'optimization',
    label: 'Optimization',
    href: (projectId) => `/pm/projects/${projectId}/optimization`,
  },
  {
    key: 'settings',
    label: 'Settings',
    href: (projectId) => `/pm/projects/${projectId}/settings`,
  },
];

function getActiveTabKey(pathname: string): string {
  if (pathname.includes('/optimization-runs/')) return 'optimization';
  const parts = pathname.split('/').filter(Boolean);
  const maybeKey = parts[parts.length - 1] || 'summary';
  if (TABS.some((t) => t.key === maybeKey)) return maybeKey;
  if (pathname.includes('/optimization')) return 'optimization';
  return 'summary';
}

export function PMProjectsTopTabs() {
  const params = useParams<{ projectId?: string | string[] }>();
  const pathname = usePathname();
  const activeKey = getActiveTabKey(pathname);
  const projectId = Array.isArray(params.projectId)
    ? params.projectId[0]
    : params.projectId;

  if (!projectId) {
    return null;
  }

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
              href={tab.href(projectId)}
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
