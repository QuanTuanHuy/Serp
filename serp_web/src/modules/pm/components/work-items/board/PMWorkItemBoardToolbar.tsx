/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board toolbar
 */

'use client';

import { RefreshCw, Search } from 'lucide-react';
import { Button, Input } from '@/shared/components/ui';
import { cn } from '@/shared/utils';

interface PMWorkItemBoardToolbarProps {
  keyword: string;
  columnCount: number;
  totalItems: number;
  isLoading: boolean;
  isRefreshing: boolean;
  onKeywordChange: (value: string) => void;
  onRefresh: () => void;
}

export function PMWorkItemBoardToolbar({
  keyword,
  columnCount,
  totalItems,
  isLoading,
  isRefreshing,
  onKeywordChange,
  onRefresh,
}: PMWorkItemBoardToolbarProps) {
  const summary = isLoading
    ? 'Loading board...'
    : `${columnCount} columns · ${totalItems} work items`;

  return (
    <div className='sticky top-0 z-20 rounded-xl border bg-background/95 p-3 shadow-sm backdrop-blur supports-[backdrop-filter]:bg-background/80'>
      <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
        <div className='min-w-0'>
          <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
            Project board
          </p>
          <p className='mt-1 truncate text-sm text-muted-foreground'>
            {summary}
          </p>
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
