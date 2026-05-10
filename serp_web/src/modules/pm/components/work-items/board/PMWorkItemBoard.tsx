/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board
 */

'use client';

import { useDeferredValue, useState } from 'react';
import { AlertCircle, RefreshCw, Search } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Input,
} from '@/shared/components/ui';
import { useGetPmWorkItemBoardQuery } from '../../../api';
import { PMWorkItemBoardColumn } from './PMWorkItemBoardColumn';
import { PMWorkItemBoardEmpty } from './PMWorkItemBoardEmpty';
import { PMWorkItemBoardSkeleton } from './PMWorkItemBoardSkeleton';

interface PMWorkItemBoardProps {
  projectId: number;
}

export function PMWorkItemBoard({ projectId }: PMWorkItemBoardProps) {
  const [keyword, setKeyword] = useState('');
  const deferredKeyword = useDeferredValue(keyword.trim());
  const {
    data: board,
    error,
    isFetching,
    isLoading,
    refetch,
  } = useGetPmWorkItemBoardQuery({
    projectId,
    params: {
      keyword: deferredKeyword || undefined,
    },
  });

  const hasColumns = Boolean(board?.columns.length);
  const hasCards = Boolean(
    board?.columns.some((column) => column.items.length > 0)
  );

  return (
    <div className='space-y-4'>
      <div className='rounded-xl border bg-card p-4 shadow-sm'>
        <div className='flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'>
          <div>
            <p className='text-sm font-medium text-muted-foreground'>
              Project board
            </p>
            <h1 className='mt-1 text-2xl font-semibold tracking-tight text-foreground'>
              Work items by status
            </h1>
          </div>
          <div className='flex flex-col gap-2 sm:flex-row sm:items-center'>
            <div className='relative sm:w-72'>
              <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder='Search key or summary'
                className='pl-9'
              />
            </div>
            <Button
              type='button'
              variant='outline'
              onClick={() => refetch()}
              disabled={isFetching}
            >
              <RefreshCw
                className={
                  isFetching ? 'mr-2 h-4 w-4 animate-spin' : 'mr-2 h-4 w-4'
                }
              />
              Refresh
            </Button>
          </div>
        </div>
      </div>

      {error ? (
        <Alert variant='destructive'>
          <AlertCircle className='h-4 w-4' />
          <AlertTitle>Board unavailable</AlertTitle>
          <AlertDescription>{getErrorMessage(error)}</AlertDescription>
        </Alert>
      ) : null}

      {isLoading ? <PMWorkItemBoardSkeleton /> : null}

      {!isLoading && !error && !hasColumns ? (
        <PMWorkItemBoardEmpty
          title='No board columns found'
          description='Project workflow has no published statuses available for board rendering.'
        />
      ) : null}

      {!isLoading && !error && hasColumns ? (
        <div className='rounded-xl border bg-card p-3 shadow-sm'>
          <div className='flex gap-4 overflow-x-auto pb-2'>
            {board?.columns.map((column) => (
              <PMWorkItemBoardColumn key={column.statusId} column={column} />
            ))}
          </div>
          {!hasCards ? (
            <p className='px-2 pb-1 pt-3 text-sm text-muted-foreground'>
              No work items match current board filters.
            </p>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
