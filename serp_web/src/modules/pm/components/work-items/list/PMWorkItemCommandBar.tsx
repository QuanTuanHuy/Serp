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
  Sparkles,
} from 'lucide-react';
import { Badge, Button, Input } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { WorkItemListViewMode } from './pmWorkItemList.utils';

interface PMWorkItemCommandBarProps {
  keyword: string;
  view: WorkItemListViewMode;
  activeFilterCount: number;
  selectedCount: number;
  isRefreshing: boolean;
  onKeywordChange: (value: string) => void;
  onViewChange: (nextView: WorkItemListViewMode) => void;
  onRefresh: () => void;
  onFilterClick: () => void;
  onOptimizeSelected: () => void;
}

export function PMWorkItemCommandBar({
  keyword,
  view,
  activeFilterCount,
  selectedCount,
  isRefreshing,
  onKeywordChange,
  onViewChange,
  onRefresh,
  onFilterClick,
  onOptimizeSelected,
}: PMWorkItemCommandBarProps) {
  return (
    <div className='sticky top-0 z-20 rounded-xl border bg-background/95 p-3 shadow-sm backdrop-blur supports-[backdrop-filter]:bg-background/80'>
      <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
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

          <Button
            type='button'
            variant='outline'
            onClick={onOptimizeSelected}
            disabled={selectedCount === 0}
          >
            <Sparkles className='mr-2 h-4 w-4' />
            Optimize selected
            {selectedCount > 0 ? (
              <Badge variant='secondary' className='ml-2 h-5 px-1.5 text-xs'>
                {selectedCount}
              </Badge>
            ) : null}
          </Button>

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
    </div>
  );
}
