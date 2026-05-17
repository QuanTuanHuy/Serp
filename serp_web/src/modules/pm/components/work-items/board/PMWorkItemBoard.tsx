/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board
 */

'use client';

import {
  startTransition,
  useCallback,
  useDeferredValue,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { AlertCircle } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
} from '@/shared/components/ui';
import { useGetPmWorkItemBoardQuery } from '../../../api';
import { PMWorkItemBoardColumn } from './PMWorkItemBoardColumn';
import { PMWorkItemBoardEmpty } from './PMWorkItemBoardEmpty';
import { PMWorkItemBoardFilters } from './PMWorkItemBoardFilters';
import { PMWorkItemBoardSkeleton } from './PMWorkItemBoardSkeleton';
import { PMWorkItemBoardToolbar } from './PMWorkItemBoardToolbar';
import {
  getActiveBoardFilterCount,
  parseNumberList,
} from './pmWorkItemBoard.utils';

interface PMWorkItemBoardProps {
  projectId: number;
}

export function PMWorkItemBoard({ projectId }: PMWorkItemBoardProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const searchKeyword = searchParams.get('q') ?? '';
  const assigneeIds = parseNumberList(searchParams.get('assigneeIds'));
  const issueTypeIds = parseNumberList(searchParams.get('issueTypeIds'));
  const priorityIds = parseNumberList(searchParams.get('priorityIds'));
  const [keyword, setKeyword] = useState(searchKeyword);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const deferredKeyword = useDeferredValue(keyword.trim());

  const updateUrl = useCallback(
    (updates: Record<string, string | undefined>) => {
      const nextParams = new URLSearchParams(searchParams.toString());
      for (const [key, value] of Object.entries(updates)) {
        if (value) {
          nextParams.set(key, value);
        } else {
          nextParams.delete(key);
        }
      }

      const queryString = nextParams.toString();
      startTransition(() => {
        router.replace(queryString ? `${pathname}?${queryString}` : pathname, {
          scroll: false,
        });
      });
    },
    [pathname, router, searchParams]
  );

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
      assigneeIds,
      issueTypeIds,
      priorityIds,
    },
  });

  const hasColumns = Boolean(board?.columns.length);
  const hasCards = Boolean(
    board?.columns.some((column) => column.items.length > 0)
  );
  const columnCount = board?.columns.length ?? 0;
  const totalItems =
    board?.columns.reduce((total, column) => total + column.total, 0) ?? 0;
  const activeFilterCount = getActiveBoardFilterCount({
    assigneeIds,
    issueTypeIds,
    priorityIds,
  });

  const activeFilterChips = useMemo(
    () =>
      [
        assigneeIds.length
          ? { key: 'assigneeIds', label: `Assignee: ${assigneeIds.length}` }
          : null,
        issueTypeIds.length
          ? { key: 'issueTypeIds', label: `Work type: ${issueTypeIds.length}` }
          : null,
        priorityIds.length
          ? { key: 'priorityIds', label: `Priority: ${priorityIds.length}` }
          : null,
      ].filter(Boolean) as Array<{ key: string; label: string }>,
    [assigneeIds.length, issueTypeIds.length, priorityIds.length]
  );

  const updateFilter = (updates: Record<string, string | undefined>) => {
    updateUrl(updates);
  };

  const clearFilters = () => {
    updateFilter({
      assigneeIds: undefined,
      issueTypeIds: undefined,
      priorityIds: undefined,
    });
  };

  const removeFilter = (key: string) => {
    updateFilter({ [key]: undefined });
  };

  return (
    <div className='space-y-4 pb-2'>
      <PMWorkItemBoardFilters
        projectId={projectId}
        open={filtersOpen}
        assigneeIds={assigneeIds}
        issueTypeIds={issueTypeIds}
        priorityIds={priorityIds}
        onOpenChange={setFiltersOpen}
        onUpdate={updateFilter}
        onClear={clearFilters}
      />

      <PMWorkItemBoardToolbar
        keyword={keyword}
        columnCount={columnCount}
        totalItems={totalItems}
        activeFilterCount={activeFilterCount}
        isLoading={isLoading}
        isRefreshing={isFetching}
        onKeywordChange={setKeyword}
        onFilterClick={() => setFiltersOpen(true)}
        onRefresh={() => refetch()}
      />

      {activeFilterChips.length > 0 ? (
        <div className='flex flex-wrap items-center gap-2'>
          {activeFilterChips.map((chip) => (
            <Badge
              key={chip.key}
              variant='secondary'
              className='inline-flex items-center gap-2 px-2 py-1'
            >
              <span>{chip.label}</span>
              <button
                type='button'
                className='text-xs text-muted-foreground hover:text-foreground'
                onClick={() => removeFilter(chip.key)}
              >
                x
              </button>
            </Badge>
          ))}
          <Button
            type='button'
            variant='ghost'
            size='sm'
            onClick={clearFilters}
          >
            Clear all
          </Button>
        </div>
      ) : null}

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
        <div className='rounded-2xl border border-border/60 bg-muted/10 p-2 shadow-sm sm:p-3'>
          <div className='flex gap-4 overflow-x-auto px-1 pb-2 pt-1 [scrollbar-width:thin]'>
            {board?.columns.map((column) => (
              <PMWorkItemBoardColumn key={column.statusId} column={column} />
            ))}
          </div>
          {!hasCards ? (
            <div className='px-3 pb-2 pt-3'>
              <PMWorkItemBoardEmpty
                title='No work items match current filters'
                description='Clear some filters or try a broader keyword to bring work items back into this board.'
              />
            </div>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
