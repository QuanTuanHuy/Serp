/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings sidebar
 */

'use client';

import {
  Blocks,
  FolderKanban,
  Settings2,
  ShieldCheck,
  UserRoundCog,
} from 'lucide-react';

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';

export type PMProjectSettingsSectionKey =
  | 'general'
  | 'people'
  | 'components'
  | 'schemes'
  | 'permissions';

export const DEFAULT_PROJECT_SETTINGS_SECTION: PMProjectSettingsSectionKey =
  'general';

export const PROJECT_SETTINGS_SECTION_KEYS: PMProjectSettingsSectionKey[] = [
  'general',
  'people',
  'components',
  'schemes',
  'permissions',
];

const PROJECT_SETTINGS_SECTION_ITEMS: Array<{
  key: PMProjectSettingsSectionKey;
  title: string;
  description: string;
  icon: typeof Settings2;
}> = [
  {
    key: 'general',
    title: 'General',
    description: 'Project profile and state',
    icon: Settings2,
  },
  {
    key: 'people',
    title: 'People',
    description: 'Lead, members, and roles',
    icon: UserRoundCog,
  },
  {
    key: 'components',
    title: 'Components',
    description: 'Project component preview',
    icon: FolderKanban,
  },
  {
    key: 'schemes',
    title: 'Schemes',
    description: 'Configuration assignments',
    icon: Blocks,
  },
  {
    key: 'permissions',
    title: 'Permissions',
    description: 'Project access grants',
    icon: ShieldCheck,
  },
];

interface PMProjectSettingsSidebarProps {
  activeSection: PMProjectSettingsSectionKey;
  onSectionChange: (section: PMProjectSettingsSectionKey) => void;
}

export function PMProjectSettingsSidebar({
  activeSection,
  onSectionChange,
}: PMProjectSettingsSidebarProps) {
  return (
    <aside className='lg:sticky lg:top-4 lg:h-fit'>
      <Card className='border-border/60 bg-background/90 shadow-sm'>
        <CardHeader className='border-b py-4'>
          <CardTitle className='text-sm'>Project settings</CardTitle>
        </CardHeader>
        <CardContent className='p-2'>
          <nav
            className='flex gap-2 overflow-x-auto p-1 lg:block lg:space-y-1 lg:overflow-visible'
            aria-label='Project settings sections'
          >
            {PROJECT_SETTINGS_SECTION_ITEMS.map((item) => {
              const Icon = item.icon;
              const active = item.key === activeSection;

              return (
                <button
                  key={item.key}
                  type='button'
                  onClick={() => onSectionChange(item.key)}
                  className={cn(
                    'flex min-w-44 shrink-0 items-start gap-3 rounded-md px-3 py-2 text-left transition-colors hover:bg-muted lg:w-full lg:min-w-0',
                    active && 'bg-primary/10 text-primary'
                  )}
                  aria-current={active ? 'page' : undefined}
                >
                  <Icon className='mt-0.5 h-4 w-4 shrink-0' />
                  <span className='min-w-0'>
                    <span className='block text-sm font-medium'>
                      {item.title}
                    </span>
                    <span className='block text-xs text-muted-foreground'>
                      {item.description}
                    </span>
                  </span>
                </button>
              );
            })}
          </nav>
        </CardContent>
      </Card>
    </aside>
  );
}
