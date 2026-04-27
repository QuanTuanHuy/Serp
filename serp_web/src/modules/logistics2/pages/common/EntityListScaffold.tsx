/*
Author: QuanTuanHuy
Description: Part of Serp Project - Reusable list scaffold for Logistics2 pages
*/

'use client';

import { useMemo } from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Input,
} from '@/shared/components/ui';
import {
  ChevronLeft,
  ChevronRight,
  RefreshCcw,
  RotateCcw,
  Search,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { cn } from '@/shared/utils';

interface EntityListScaffoldProps {
  title: string;
  description: string;
  searchValue: string;
  searchPlaceholder: string;
  onSearchValueChange: (value: string) => void;
  onSearch: () => void;
  onReset: () => void;
  onRefresh?: () => void;
  isLoading: boolean;
  error?: unknown;
  items: object[];
  totalItems: number;
  currentPage: number;
  totalPages: number;
  onPreviousPage: () => void;
  onNextPage: () => void;
  className?: string;
  emptyMessage?: string;
}

export const EntityListScaffold: React.FC<EntityListScaffoldProps> = ({
  title,
  description,
  searchValue,
  searchPlaceholder,
  onSearchValueChange,
  onSearch,
  onReset,
  onRefresh,
  isLoading,
  error,
  items,
  totalItems,
  currentPage,
  totalPages,
  onPreviousPage,
  onNextPage,
  className,
  emptyMessage,
}) => {
  const previewItems = useMemo(() => items.slice(0, 10), [items]);
  const errorMessage = useMemo(
    () => (error ? getErrorMessage(error) : ''),
    [error]
  );

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col md:flex-row md:items-center md:justify-between gap-3'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>{title}</h1>
          <p className='text-muted-foreground'>{description}</p>
        </div>
        <Badge variant='outline' className='w-fit'>
          Total: {totalItems}
        </Badge>
      </div>

      <Card>
        <CardContent className='pt-6 space-y-4'>
          <div className='flex flex-col lg:flex-row gap-3'>
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                value={searchValue}
                placeholder={searchPlaceholder}
                className='pl-9'
                onChange={(event) => onSearchValueChange(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    onSearch();
                  }
                }}
              />
            </div>

            <div className='flex items-center gap-2'>
              <Button onClick={onSearch}>Search</Button>
              <Button variant='outline' onClick={onReset}>
                <RotateCcw className='mr-2 h-4 w-4' />
                Reset
              </Button>
              {onRefresh && (
                <Button variant='ghost' onClick={onRefresh}>
                  <RefreshCcw className='mr-2 h-4 w-4' />
                  Refresh
                </Button>
              )}
            </div>
          </div>

          <div className='flex items-center justify-between text-sm text-muted-foreground'>
            <span>
              Page {currentPage + 1}
              {totalPages > 0 ? ` / ${totalPages}` : ''}
            </span>
            <div className='flex items-center gap-2'>
              <Button
                variant='outline'
                size='sm'
                onClick={onPreviousPage}
                disabled={currentPage <= 0 || isLoading}
              >
                <ChevronLeft className='h-4 w-4' />
              </Button>
              <Button
                variant='outline'
                size='sm'
                onClick={onNextPage}
                disabled={
                  isLoading || totalPages <= 1 || currentPage >= totalPages - 1
                }
              >
                <ChevronRight className='h-4 w-4' />
              </Button>
            </div>
          </div>

          <div className='rounded-lg border bg-muted/20 p-4'>
            {isLoading ? (
              <div className='text-sm text-muted-foreground'>
                Loading data...
              </div>
            ) : error ? (
              <div className='text-sm text-destructive'>
                Failed to load data: {errorMessage}
              </div>
            ) : items.length === 0 ? (
              <div className='text-sm text-muted-foreground'>
                {emptyMessage || 'No data found'}
              </div>
            ) : (
              <div className='space-y-2'>
                <div className='text-xs text-muted-foreground'>
                  Showing first {previewItems.length} items from current page
                </div>
                <pre className='max-h-[28rem] overflow-auto rounded-md bg-background p-4 text-xs leading-relaxed'>
                  {JSON.stringify(previewItems, null, 2)}
                </pre>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
