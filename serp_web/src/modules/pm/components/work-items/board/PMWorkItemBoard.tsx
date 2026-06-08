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
import {
  closestCorners,
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from '@dnd-kit/core';
import { AlertCircle } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
} from '@/shared/components/ui';
import {
  useGetPmWorkItemBoardQuery,
  useLazyGetPmWorkItemTransitionsQuery,
  useTransitionPmWorkItemStatusMutation,
} from '../../../api/workItemApi';
import { useGetPmResolutionsQuery } from '../../../api/settingsApi';
import type {
  PMWorkItemBoardCardApi,
  PMWorkItemTransitionApi,
} from '../../../types/api';
import { PMWorkItemDetailDialog } from '../detail';
import {
  transitionNeedsResolution,
  WorkItemResolutionTransitionDialog,
} from '../resolution-transition';
import { PMWorkItemBoardCard } from './PMWorkItemBoardCard';
import { PMWorkItemBoardColumn } from './PMWorkItemBoardColumn';
import { PMWorkItemBoardEmpty } from './PMWorkItemBoardEmpty';
import { PMWorkItemBoardFilters } from './PMWorkItemBoardFilters';
import { PMWorkItemBoardSkeleton } from './PMWorkItemBoardSkeleton';
import { PMWorkItemBoardToolbar } from './PMWorkItemBoardToolbar';
import {
  type BoardDragData,
  getActiveBoardFilterCount,
  parseNumberList,
} from './pmWorkItemBoard.utils';

interface PMWorkItemBoardProps {
  projectId: number;
}

type BoardCardLookupEntry = {
  item: PMWorkItemBoardCardApi;
  statusId: number;
};

function getBoardDragData(value: unknown): BoardDragData | undefined {
  if (!value || typeof value !== 'object' || !('type' in value)) {
    return undefined;
  }

  const data = value as BoardDragData;
  if (data.type === 'work-item' && data.workItemId && data.statusId) {
    return data;
  }
  if (data.type === 'column' && data.statusId) {
    return data;
  }
  return undefined;
}

export function PMWorkItemBoard({ projectId }: PMWorkItemBoardProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const searchKeyword = searchParams.get('q') ?? '';
  const assigneeIds = parseNumberList(searchParams.get('assigneeIds'));
  const issueTypeIds = parseNumberList(searchParams.get('issueTypeIds'));
  const priorityIds = parseNumberList(searchParams.get('priorityIds'));
  const selectedIssueId = Number(searchParams.get('issueId')) || undefined;
  const [keyword, setKeyword] = useState(searchKeyword);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [activeDragItemId, setActiveDragItemId] = useState<number>();
  const [movingWorkItemId, setMovingWorkItemId] = useState<number>();
  const [pendingResolutionTransition, setPendingResolutionTransition] =
    useState<{
      workItemId: number;
      workItemKey: string;
      transition: PMWorkItemTransitionApi;
    } | null>(null);
  const deferredKeyword = useDeferredValue(keyword.trim());
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 6,
      },
    })
  );
  const [loadTransitions] = useLazyGetPmWorkItemTransitionsQuery();
  const [transitionWorkItem, transitionState] =
    useTransitionPmWorkItemStatusMutation();
  const { data: resolutionResponse, isFetching: isResolutionsFetching } =
    useGetPmResolutionsQuery({
      page: 0,
      pageSize: 100,
      sortBy: 'sequence',
      sortDirection: 'asc',
    });
  const resolutions = resolutionResponse?.data.items ?? [];

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
  const visibleItemIds = useMemo(
    () =>
      board?.columns.flatMap((column) => column.items.map((item) => item.id)) ??
      [],
    [board?.columns]
  );
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

  const cardLookup = useMemo(() => {
    const lookup = new Map<number, BoardCardLookupEntry>();
    for (const column of board?.columns || []) {
      for (const item of column.items) {
        lookup.set(item.id, { item, statusId: column.statusId });
      }
    }
    return lookup;
  }, [board?.columns]);

  const selectedItem = useMemo(
    () => (selectedIssueId ? cardLookup.get(selectedIssueId)?.item : undefined),
    [cardLookup, selectedIssueId]
  );

  const activeDragEntry = useMemo(
    () => (activeDragItemId ? cardLookup.get(activeDragItemId) : undefined),
    [activeDragItemId, cardLookup]
  );

  const handleDragStart = useCallback((event: DragStartEvent) => {
    const activeData = getBoardDragData(event.active.data.current);
    if (activeData?.type === 'work-item') {
      setActiveDragItemId(activeData.workItemId);
    }
  }, []);

  const handleDragEnd = useCallback(
    async (event: DragEndEvent) => {
      setActiveDragItemId(undefined);

      const activeData = getBoardDragData(event.active.data.current);
      const overData = getBoardDragData(event.over?.data.current);
      if (!activeData || activeData.type !== 'work-item' || !overData) {
        return;
      }

      const targetStatusId = overData.statusId;
      if (activeData.statusId === targetStatusId) {
        return;
      }

      setMovingWorkItemId(activeData.workItemId);
      try {
        const transitions = await loadTransitions({
          projectId,
          workItemId: activeData.workItemId,
        }).unwrap();
        const transition = transitions.find(
          (item) => item.targetStatus?.id === targetStatusId
        );

        if (!transition) {
          toast.error('No workflow transition is available for that move.');
          return;
        }

        const activeEntry = cardLookup.get(activeData.workItemId);
        if (transitionNeedsResolution(transition)) {
          setPendingResolutionTransition({
            workItemId: activeData.workItemId,
            workItemKey: activeEntry?.item.key ?? `#${activeData.workItemId}`,
            transition,
          });
          return;
        }

        await transitionWorkItem({
          projectId,
          workItemId: activeData.workItemId,
          body: { transitionId: transition.id },
        }).unwrap();
        toast.success('Work item moved.');
      } catch (dragError) {
        toast.error('Failed to move work item', {
          description: getErrorMessage(dragError),
        });
      } finally {
        setMovingWorkItemId(undefined);
      }
    },
    [cardLookup, loadTransitions, projectId, transitionWorkItem]
  );

  const handleDragCancel = useCallback(() => {
    setActiveDragItemId(undefined);
  }, []);

  const isDragDisabled = transitionState.isLoading || Boolean(movingWorkItemId);

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

  const selectWorkItem = (workItemId: number) => {
    updateUrl({ issueId: String(workItemId) });
  };

  const closeWorkItem = () => {
    updateUrl({ issueId: undefined });
  };

  const optimizeVisibleItems = () => {
    if (!visibleItemIds.length) return;
    router.push(
      `/pm/projects/${projectId}/optimization?selected=${visibleItemIds.join(',')}`
    );
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
        visibleItemCount={visibleItemIds.length}
        isLoading={isLoading}
        isRefreshing={isFetching}
        onKeywordChange={setKeyword}
        onFilterClick={() => setFiltersOpen(true)}
        onRefresh={() => refetch()}
        onOptimizeView={optimizeVisibleItems}
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
          <DndContext
            sensors={sensors}
            collisionDetection={closestCorners}
            onDragCancel={handleDragCancel}
            onDragEnd={handleDragEnd}
            onDragStart={handleDragStart}
          >
            <div className='flex gap-4 overflow-x-auto px-1 pb-2 pt-1 [scrollbar-width:thin]'>
              {board?.columns.map((column) => (
                <PMWorkItemBoardColumn
                  key={column.statusId}
                  column={column}
                  dragDisabled={isDragDisabled}
                  onSelectWorkItem={selectWorkItem}
                />
              ))}
            </div>
            <DragOverlay>
              {activeDragEntry ? (
                <PMWorkItemBoardCard
                  item={activeDragEntry.item}
                  statusId={activeDragEntry.statusId}
                  isDragOverlay
                  onSelect={selectWorkItem}
                />
              ) : null}
            </DragOverlay>
          </DndContext>
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

      <PMWorkItemDetailDialog
        projectId={projectId}
        workItemId={selectedIssueId}
        open={Boolean(selectedIssueId)}
        fallbackItem={selectedItem}
        onOpenChange={(open) => {
          if (!open) closeWorkItem();
        }}
      />
      <WorkItemResolutionTransitionDialog
        open={pendingResolutionTransition !== null}
        transitionLabel={
          pendingResolutionTransition?.transition.targetStatus?.name
        }
        resolutions={resolutions}
        isLoading={isResolutionsFetching}
        isSubmitting={transitionState.isLoading}
        onOpenChange={(open) => {
          if (!open) {
            setPendingResolutionTransition(null);
          }
        }}
        onConfirm={async (resolutionId) => {
          if (!pendingResolutionTransition) {
            return;
          }
          setMovingWorkItemId(pendingResolutionTransition.workItemId);
          try {
            await transitionWorkItem({
              projectId,
              workItemId: pendingResolutionTransition.workItemId,
              body: {
                transitionId: pendingResolutionTransition.transition.id,
                resolutionId,
              },
            }).unwrap();
            toast.success(`${pendingResolutionTransition.workItemKey} moved.`);
            setPendingResolutionTransition(null);
          } catch (error) {
            toast.error('Failed to move work item', {
              description: getErrorMessage(error),
            });
          } finally {
            setMovingWorkItemId(undefined);
          }
        }}
      />
    </div>
  );
}
