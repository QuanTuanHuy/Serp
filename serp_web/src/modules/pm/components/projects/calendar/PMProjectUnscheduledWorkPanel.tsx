/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project unscheduled work panel
 */

'use client';

import { useDraggable } from '@dnd-kit/core';
import { GripVertical, ListFilter, SearchX } from 'lucide-react';

import { Badge, ScrollArea, Skeleton } from '@/shared/components/ui';
import { cn } from '@/shared/utils';

import type { PMWorkItemSearchApi } from '../../../types/api';

interface PMProjectUnscheduledWorkPanelProps {
  items: PMWorkItemSearchApi[];
  totalItems: number;
  isLoading: boolean;
  isFetching: boolean;
  activeWorkItemId?: number;
  onOpenWorkItem: (workItemId: number) => void;
}

export function PMProjectUnscheduledWorkPanel({
  items,
  totalItems,
  isLoading,
  isFetching,
  activeWorkItemId,
  onOpenWorkItem,
}: PMProjectUnscheduledWorkPanelProps) {
  return (
    <aside className='rounded-lg border bg-card text-card-foreground'>
      <div className='border-b p-4'>
        <div className='flex items-center justify-between gap-3'>
          <div>
            <h2 className='text-base font-semibold'>Unscheduled work</h2>
            <p className='mt-1 text-sm text-muted-foreground'>
              Drag work onto the calendar to set its schedule.
            </p>
          </div>
          <Badge variant='secondary'>{totalItems}</Badge>
        </div>
      </div>

      <div className='flex items-center gap-2 border-b px-4 py-3 text-xs font-medium text-muted-foreground'>
        <ListFilter className='h-4 w-4' />
        Current calendar filters apply
        {isFetching && !isLoading ? (
          <span className='ml-auto'>Refreshing</span>
        ) : null}
      </div>

      <ScrollArea className='h-[560px]'>
        <div className='space-y-2 p-3'>
          {isLoading ? (
            Array.from({ length: 5 }).map((_, index) => (
              <Skeleton key={index} className='h-[74px] rounded-md' />
            ))
          ) : items.length ? (
            items.map((item) => (
              <UnscheduledWorkItemRow
                key={item.id}
                item={item}
                isDragging={activeWorkItemId === item.id}
                onOpenWorkItem={onOpenWorkItem}
              />
            ))
          ) : (
            <div className='flex min-h-[220px] flex-col items-center justify-center rounded-md border border-dashed px-4 text-center text-sm text-muted-foreground'>
              <SearchX className='mb-2 h-5 w-5' />
              No unscheduled work matches current filters.
            </div>
          )}
        </div>
      </ScrollArea>
    </aside>
  );
}

function UnscheduledWorkItemRow({
  item,
  isDragging,
  onOpenWorkItem,
}: {
  item: PMWorkItemSearchApi;
  isDragging: boolean;
  onOpenWorkItem: (workItemId: number) => void;
}) {
  const { attributes, listeners, setNodeRef, transform } = useDraggable({
    id: `unscheduled-work-item-${item.id}`,
    data: {
      type: 'unscheduled-work-item',
      workItemId: item.id,
    },
  });

  const style = transform
    ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
      }
    : undefined;

  return (
    <button
      ref={setNodeRef}
      type='button'
      style={style}
      className={cn(
        'w-full rounded-md border bg-background p-3 text-left shadow-sm transition-colors hover:bg-muted/40',
        isDragging && 'opacity-50'
      )}
      onClick={() => onOpenWorkItem(item.id)}
      {...listeners}
      {...attributes}
    >
      <div className='flex min-w-0 items-start gap-2'>
        <GripVertical className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
        <div className='min-w-0 flex-1'>
          <div className='flex min-w-0 items-center gap-2'>
            <span className='shrink-0 text-xs font-semibold text-primary'>
              {item.key}
            </span>
            <span className='truncate text-sm font-medium'>{item.summary}</span>
          </div>
          <div className='mt-2 flex flex-wrap items-center gap-1.5 text-[11px] text-muted-foreground'>
            {item.statusName ? (
              <Badge variant='outline'>{item.statusName}</Badge>
            ) : null}
            {item.assigneeName ? (
              <span className='truncate'>{item.assigneeName}</span>
            ) : null}
            {!item.assigneeId ? <span>Unassigned</span> : null}
          </div>
        </div>
      </div>
    </button>
  );
}
