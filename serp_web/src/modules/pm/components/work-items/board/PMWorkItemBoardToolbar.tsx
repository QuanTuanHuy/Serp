/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board toolbar
 */

'use client';

import { RefreshCw, Search, SlidersHorizontal, Sparkles } from 'lucide-react';
import { Badge, Button, Input } from '@/shared/components/ui';
import { cn } from '@/shared/utils';

interface PMWorkItemBoardToolbarProps {
  keyword: string;
  columnCount: number;
  totalItems: number;
  activeFilterCount: number;
  visibleItemCount: number;
  isLoading: boolean;
  isRefreshing: boolean;
  onKeywordChange: (value: string) => void;
  onFilterClick: () => void;
  onRefresh: () => void;
  onOptimizeView: () => void;
}

export function PMWorkItemBoardToolbar({
  keyword,
  columnCount,
  totalItems,
  activeFilterCount,
  visibleItemCount,
  isLoading,
  isRefreshing,
  onKeywordChange,
  onFilterClick,
  onRefresh,
  onOptimizeView,
}: PMWorkItemBoardToolbarProps) {
  const summary = isLoading
    ? 'Loading board...'
    : `${columnCount} columns · ${totalItems} work items`;

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
            onClick={onOptimizeView}
            disabled={visibleItemCount === 0}
          >
            <Sparkles className='mr-2 h-4 w-4' />
            Optimize view
            <Badge variant='secondary' className='ml-2 h-5 px-1.5 text-xs'>
              {visibleItemCount}
            </Badge>
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
