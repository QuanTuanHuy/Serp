/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS list filters with basic/advanced toggle
 */

'use client';

import React from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { ChevronDown, ChevronUp, RefreshCw, SlidersHorizontal } from 'lucide-react';

export type TmsFilterMode = 'basic' | 'advanced';

interface TmsListFilterPanelProps {
  title?: string;
  description?: string;
  filterMode: TmsFilterMode;
  onFilterModeChange: (mode: TmsFilterMode) => void;
  advancedFieldCount?: number;
  disabled?: boolean;
  isFetching?: boolean;
  onApply: (event: React.FormEvent) => void;
  onClear: () => void;
  onRefresh: () => void;
  basicFilters: React.ReactNode;
  advancedFilters: React.ReactNode;
  className?: string;
}

export const TmsListFilterPanel: React.FC<TmsListFilterPanelProps> = ({
  title = 'Search & filters',
  description = 'Use basic search or switch to advanced filters aligned with the API.',
  filterMode,
  onFilterModeChange,
  advancedFieldCount = 0,
  disabled = false,
  isFetching = false,
  onApply,
  onClear,
  onRefresh,
  basicFilters,
  advancedFilters,
  className,
}) => {
  const isAdvanced = filterMode === 'advanced';

  return (
    <Card className={className}>
      <CardHeader className='space-y-3 pb-3'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between'>
          <div className='space-y-1'>
            <CardTitle className='text-base'>{title}</CardTitle>
            <CardDescription>{description}</CardDescription>
          </div>

          <div className='flex flex-wrap items-center gap-2'>
            <div className='inline-flex rounded-md border p-0.5'>
              <Button
                type='button'
                size='sm'
                variant={!isAdvanced ? 'secondary' : 'ghost'}
                className='h-7 px-2.5 text-xs'
                onClick={() => onFilterModeChange('basic')}
                disabled={disabled}
              >
                Basic
              </Button>
              <Button
                type='button'
                size='sm'
                variant={isAdvanced ? 'secondary' : 'ghost'}
                className='h-7 px-2.5 text-xs'
                onClick={() => onFilterModeChange('advanced')}
                disabled={disabled}
              >
                <SlidersHorizontal className='mr-1 h-3.5 w-3.5' />
                Advanced
                {advancedFieldCount > 0 ? (
                  <Badge variant='outline' className='ml-1.5 h-5 px-1 text-[10px]'>
                    {advancedFieldCount}
                  </Badge>
                ) : null}
              </Button>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent>
        <form onSubmit={onApply} className='space-y-4'>
          <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-4'>
            {basicFilters}
          </div>

          {isAdvanced ? (
            <div className='space-y-3 border-t pt-4'>
              <button
                type='button'
                className='flex w-full items-center justify-between text-left text-sm font-medium'
                onClick={() => onFilterModeChange('advanced')}
                aria-expanded={isAdvanced}
              >
                <span>Advanced criteria</span>
                <ChevronUp className='h-4 w-4 text-muted-foreground' />
              </button>
              <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-4'>
                {advancedFilters}
              </div>
            </div>
          ) : (
            <button
              type='button'
              className='flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground'
              onClick={() => onFilterModeChange('advanced')}
              disabled={disabled}
            >
              <ChevronDown className='h-3.5 w-3.5' />
              Show advanced filters
            </button>
          )}

          <div className={cn('flex flex-wrap gap-2 border-t pt-3')}>
            <Button type='submit' size='sm' disabled={disabled}>
              Apply filters
            </Button>
            <Button
              type='button'
              size='sm'
              variant='outline'
              onClick={onClear}
              disabled={disabled}
            >
              Clear
            </Button>
            <Button
              type='button'
              size='sm'
              variant='outline'
              onClick={onRefresh}
              disabled={disabled || isFetching}
            >
              <RefreshCw
                className={cn('mr-2 h-4 w-4', isFetching && 'animate-spin')}
              />
              Refresh
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};
