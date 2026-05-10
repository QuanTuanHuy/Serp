/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item command bar
 */

'use client';

import {
  LayoutList,
  ListChecks,
  RefreshCw,
  Search,
  SlidersHorizontal,
} from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Input,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { WorkItemListViewMode } from './pmWorkItemList.utils';

interface PMWorkItemCommandBarProps {
  keyword: string;
  view: WorkItemListViewMode;
  activeFilterCount: number;
  isRefreshing: boolean;
  onKeywordChange: (value: string) => void;
  onViewChange: (nextView: WorkItemListViewMode) => void;
  onRefresh: () => void;
  onFilterClick: () => void;
}

export function PMWorkItemCommandBar({
  keyword,
  view,
  activeFilterCount,
  isRefreshing,
  onKeywordChange,
  onViewChange,
  onRefresh,
  onFilterClick,
}: PMWorkItemCommandBarProps) {
  return (
    <Card className='shadow-sm'>
      <CardContent className='p-3 sm:p-4'>
        <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
          <div className='flex min-w-0 flex-1 items-center gap-3'>
            <div className='min-w-0'>
              <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
                Project work items
              </p>
              <p className='mt-1 truncate text-sm text-muted-foreground'>
                Browse and manage issues in current project.
              </p>
            </div>
            {activeFilterCount > 0 ? (
              <Badge variant='secondary' className='shrink-0'>
                {activeFilterCount} filters
              </Badge>
            ) : null}
          </div>

          <div className='flex flex-col gap-2 sm:flex-row sm:items-center'>
            <div className='relative sm:w-80'>
              <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                value={keyword}
                onChange={(event) => onKeywordChange(event.target.value)}
                placeholder='Search key or summary'
                className='pl-9'
              />
            </div>

            <div className='inline-flex rounded-md border bg-background p-1'>
              <Button
                type='button'
                variant={view === 'list' ? 'secondary' : 'ghost'}
                size='sm'
                onClick={() => onViewChange('list')}
              >
                <ListChecks className='mr-2 h-4 w-4' />
                List view
              </Button>
              <Button
                type='button'
                variant={view === 'detail' ? 'secondary' : 'ghost'}
                size='sm'
                onClick={() => onViewChange('detail')}
              >
                <LayoutList className='mr-2 h-4 w-4' />
                Detail view
              </Button>
            </div>

            <Button type='button' variant='outline' onClick={onFilterClick}>
              <SlidersHorizontal className='mr-2 h-4 w-4' />
              Filter
              {activeFilterCount > 0 ? (
                <Badge variant='secondary' className='ml-2 h-5 px-1.5 text-xs'>
                  {activeFilterCount}
                </Badge>
              ) : null}
            </Button>

            <Button
              type='button'
              variant='outline'
              onClick={onRefresh}
              disabled={isRefreshing}
            >
              <RefreshCw
                className={cn('mr-2 h-4 w-4', isRefreshing && 'animate-spin')}
              />
              Refresh
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
