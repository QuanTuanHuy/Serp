/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module showcase with larger, clearer workspace presentation
 */

'use client';

import Link from 'next/link';
import { useMemo, useState } from 'react';
import { getModuleIcon } from '@/shared/constants/moduleIcons';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent } from '@/shared/components/ui/card';
import { cn } from '@/shared/utils';
import {
  ArrowUpRight,
  Grid3x3,
  List,
  Loader2,
  Lock,
  Sparkles,
} from 'lucide-react';
import type { ModuleDisplayItem } from '../types/module';

interface ModuleShowcaseProps {
  modules: ModuleDisplayItem[];
  isLoading?: boolean;
  className?: string;
}

type ViewMode = 'grid' | 'list';
type FilterMode = 'all' | 'active' | 'admin' | 'standard';

interface ModuleMetric {
  label: string;
  value: string;
}

function sortModules(a: ModuleDisplayItem, b: ModuleDisplayItem) {
  if (a.isActive !== b.isActive) {
    return Number(b.isActive) - Number(a.isActive);
  }

  if (a.isAdmin !== b.isAdmin) {
    return Number(Boolean(b.isAdmin)) - Number(Boolean(a.isAdmin));
  }

  return a.name.localeCompare(b.name);
}

function filterModules(modules: ModuleDisplayItem[], filterMode: FilterMode) {
  switch (filterMode) {
    case 'active':
      return modules.filter((module) => module.isActive);
    case 'admin':
      return modules.filter((module) => module.isAdmin);
    case 'standard':
      return modules.filter((module) => !module.isAdmin);
    default:
      return modules;
  }
}

function ModuleSurface({
  module,
  viewMode,
}: {
  module: ModuleDisplayItem;
  viewMode: ViewMode;
}) {
  const iconConfig = getModuleIcon(module.code);
  const Icon = iconConfig?.icon ?? Sparkles;
  const isInteractive = module.isActive;

  const content = (
    <Card
      className={cn(
        'h-full border-border/60 bg-background/95 transition-all duration-200',
        isInteractive &&
          'hover:-translate-y-1 hover:border-primary/35 hover:shadow-xl',
        !isInteractive && 'opacity-75',
        module.isAdmin && 'shadow-[0_0_0_1px_rgba(59,130,246,0.08)]'
      )}
    >
      <CardContent className={cn(viewMode === 'grid' ? 'p-5' : 'p-4 sm:p-5')}>
        <div
          className={cn(
            viewMode === 'grid'
              ? 'flex h-full flex-col'
              : 'flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'
          )}
        >
          <div className='flex items-start gap-4'>
            <div
              className={cn(
                'flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-2xl border border-white/30 shadow-sm',
                iconConfig?.bgColor ?? 'bg-primary/10'
              )}
            >
              <Icon
                className={cn('h-7 w-7', iconConfig?.color ?? 'text-primary')}
              />
            </div>

            <div className='min-w-0 flex-1'>
              <div className='flex flex-wrap items-center gap-2'>
                <Badge
                  variant={module.isAdmin ? 'default' : 'secondary'}
                  className='rounded-full px-2.5 py-1 text-[11px]'
                >
                  {module.isAdmin ? 'Admin access' : 'Workspace tool'}
                </Badge>
                <Badge
                  variant='outline'
                  className='rounded-full px-2.5 py-1 text-[11px]'
                >
                  {module.isActive ? 'Ready to open' : 'Unavailable'}
                </Badge>
              </div>

              <h3 className='mt-3 text-xl font-semibold tracking-tight'>
                {module.name}
              </h3>
              <p className='mt-2 text-sm leading-6 text-muted-foreground'>
                {module.description ||
                  'Open this workspace to continue your tasks.'}
              </p>
              <p className='mt-3 text-xs font-medium uppercase tracking-[0.18em] text-muted-foreground'>
                {module.code}
              </p>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );

  if (!isInteractive) {
    return <div className='h-full'>{content}</div>;
  }

  return (
    <Link href={module.href} className='block h-full'>
      {content}
    </Link>
  );
}

export function ModuleShowcase({
  modules,
  isLoading = false,
  className,
}: ModuleShowcaseProps) {
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [filterMode, setFilterMode] = useState<FilterMode>('all');

  const sortedModules = useMemo(
    () => [...modules].sort(sortModules),
    [modules]
  );

  const filterOptions = useMemo(
    () => [
      { label: 'All', value: 'all' as const, count: sortedModules.length },
      {
        label: 'Ready',
        value: 'active' as const,
        count: sortedModules.filter((module) => module.isActive).length,
      },
      {
        label: 'Admin',
        value: 'admin' as const,
        count: sortedModules.filter((module) => module.isAdmin).length,
      },
      {
        label: 'Standard',
        value: 'standard' as const,
        count: sortedModules.filter((module) => !module.isAdmin).length,
      },
    ],
    [sortedModules]
  );

  const filteredModules = useMemo(
    () => filterModules(sortedModules, filterMode),
    [filterMode, sortedModules]
  );

  if (isLoading) {
    return (
      <div
        className={cn(
          'rounded-[2rem] border border-border/60 bg-background/90 p-8 shadow-lg',
          className
        )}
      >
        <div className='flex min-h-[420px] flex-col items-center justify-center gap-4 text-center'>
          <div className='rounded-full border border-primary/15 bg-primary/10 p-4'>
            <Loader2 className='h-8 w-8 animate-spin text-primary' />
          </div>
          <div className='space-y-2'>
            <h3 className='text-2xl font-semibold tracking-tight'>
              Loading your modules
            </h3>
            <p className='max-w-md text-sm leading-6 text-muted-foreground'>
              We are preparing the tools available in your workspace so you can
              jump in quickly.
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (!sortedModules.length) {
    return (
      <div
        className={cn(
          'rounded-[2rem] border border-dashed border-primary/30 bg-primary/5 p-8 shadow-sm',
          className
        )}
      >
        <div className='mx-auto flex max-w-2xl flex-col items-center text-center'>
          <div className='flex h-16 w-16 items-center justify-center rounded-full bg-primary/10 text-primary'>
            <Sparkles className='h-8 w-8' />
          </div>
          <h3 className='mt-6 text-3xl font-semibold tracking-tight'>
            No modules are active yet
          </h3>
          <p className='mt-3 text-sm leading-6 text-muted-foreground'>
            Once your organization enables modules, they will appear here as the
            main entry points for your day-to-day work.
          </p>
          <div className='mt-6 flex flex-col gap-3 sm:flex-row'>
            <Button asChild>
              <Link href='/subscription'>View pricing plans</Link>
            </Button>
            <Button asChild variant='outline'>
              <Link href='/apps'>Browse app catalog</Link>
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      className={cn(
        'rounded-[2rem] border border-border/60 bg-background/90 p-6 shadow-lg md:p-8',
        className
      )}
    >
      <div className='flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between'>
        <div className='flex flex-wrap gap-2'>
          {filterOptions.map((option) => (
            <Button
              key={option.value}
              type='button'
              size='sm'
              variant={filterMode === option.value ? 'default' : 'outline'}
              aria-pressed={filterMode === option.value}
              className='rounded-full px-4'
              onClick={() => setFilterMode(option.value)}
            >
              {option.label}
              <span className='text-xs opacity-80'>{option.count}</span>
            </Button>
          ))}
        </div>

        <div className='flex w-full items-center justify-between gap-3 rounded-2xl border border-border/60 bg-muted/20 p-2 xl:w-auto'>
          <div>
            <p className='text-sm font-medium'>Browse your modules</p>
            <p className='text-xs text-muted-foreground'>
              {filteredModules.length} shown in this view
            </p>
          </div>

          <div className='flex items-center gap-2 rounded-xl border border-border/60 bg-background p-1'>
            <Button
              type='button'
              size='sm'
              variant={viewMode === 'grid' ? 'default' : 'ghost'}
              aria-pressed={viewMode === 'grid'}
              className='gap-2 rounded-lg'
              onClick={() => setViewMode('grid')}
            >
              <Grid3x3 className='h-4 w-4' />
            </Button>
            <Button
              type='button'
              size='sm'
              variant={viewMode === 'list' ? 'default' : 'ghost'}
              aria-pressed={viewMode === 'list'}
              className='gap-2 rounded-lg'
              onClick={() => setViewMode('list')}
            >
              <List className='h-4 w-4' />
            </Button>
          </div>
        </div>
      </div>

      {filteredModules.length === 0 ? (
        <div className='mt-8 rounded-3xl border border-dashed border-border/70 bg-muted/20 p-10 text-center'>
          <h3 className='text-xl font-semibold tracking-tight'>
            No modules match this filter
          </h3>
          <p className='mt-2 text-sm leading-6 text-muted-foreground'>
            Try another filter to see the rest of the modules available in your
            workspace.
          </p>
        </div>
      ) : viewMode === 'grid' ? (
        <div className='mt-8 grid gap-5 sm:grid-cols-2 xl:grid-cols-3 [content-visibility:auto]'>
          {filteredModules.map((module) => (
            <ModuleSurface key={module.code} module={module} viewMode='grid' />
          ))}
        </div>
      ) : (
        <div className='mt-8 space-y-4 [content-visibility:auto]'>
          {filteredModules.map((module) => (
            <ModuleSurface key={module.code} module={module} viewMode='list' />
          ))}
        </div>
      )}
    </div>
  );
}
