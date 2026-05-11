/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board
 */

'use client';

import { startTransition, useDeferredValue, useEffect, useState } from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { AlertCircle } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { Alert, AlertDescription, AlertTitle } from '@/shared/components/ui';
import { useGetPmWorkItemBoardQuery } from '../../../api';
import { PMWorkItemBoardColumn } from './PMWorkItemBoardColumn';
import { PMWorkItemBoardEmpty } from './PMWorkItemBoardEmpty';
import { PMWorkItemBoardSkeleton } from './PMWorkItemBoardSkeleton';
import { PMWorkItemBoardToolbar } from './PMWorkItemBoardToolbar';

interface PMWorkItemBoardProps {
  projectId: number;
}

export function PMWorkItemBoard({ projectId }: PMWorkItemBoardProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const searchKeyword = searchParams.get('q') ?? '';
  const [keyword, setKeyword] = useState(searchKeyword);
  const deferredKeyword = useDeferredValue(keyword.trim());

  useEffect(() => {
    setKeyword(searchKeyword);
  }, [searchKeyword]);

  useEffect(() => {
    if (deferredKeyword === searchKeyword) return;

    const nextParams = new URLSearchParams(searchParams.toString());
    if (deferredKeyword) {
      nextParams.set('q', deferredKeyword);
    } else {
      nextParams.delete('q');
    }

    const queryString = nextParams.toString();
    startTransition(() => {
      router.replace(queryString ? `${pathname}?${queryString}` : pathname, {
        scroll: false,
      });
    });
  }, [deferredKeyword, pathname, router, searchKeyword, searchParams]);

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
  const columnCount = board?.columns.length ?? 0;
  const totalItems =
    board?.columns.reduce((total, column) => total + column.total, 0) ?? 0;

  return (
    <div className='space-y-4'>
      <PMWorkItemBoardToolbar
        keyword={keyword}
        columnCount={columnCount}
        totalItems={totalItems}
        isLoading={isLoading}
        isRefreshing={isFetching}
        onKeywordChange={setKeyword}
        onRefresh={() => refetch()}
      />

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
