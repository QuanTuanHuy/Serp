/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board column
 */

import { useDroppable } from '@dnd-kit/core';
import {
  SortableContext,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMWorkItemBoardColumnApi } from '../../../types/api';
import { PMWorkItemBoardCard } from './PMWorkItemBoardCard';
import {
  getBoardCardDndId,
  getBoardColumnDndId,
} from './pmWorkItemBoard.utils';

interface PMWorkItemBoardColumnProps {
  column: PMWorkItemBoardColumnApi;
  dragDisabled?: boolean;
  onSelectWorkItem: (workItemId: number) => void;
}

function getStatusCategoryAccent(key?: string | null): string {
  switch (key) {
    case 'new':
      return 'bg-slate-400 dark:bg-slate-500';
    case 'indeterminate':
      return 'bg-blue-500';
    case 'done':
      return 'bg-emerald-500';
    default:
      return 'bg-border';
  }
}

export function PMWorkItemBoardColumn({
  column,
  dragDisabled = false,
  onSelectWorkItem,
}: PMWorkItemBoardColumnProps) {
  const { isOver, setNodeRef } = useDroppable({
    id: getBoardColumnDndId(column.statusId),
    data: {
      type: 'column',
      statusId: column.statusId,
    },
    disabled: dragDisabled,
  });
  const sortableItems = column.items.map((item) => getBoardCardDndId(item.id));

  return (
    <section
      ref={setNodeRef}
      className={cn(
        'flex max-h-[calc(100vh-15.5rem)] min-h-[28rem] w-[19.5rem] shrink-0 flex-col rounded-2xl border border-border/60 bg-muted/20 shadow-sm',
        isOver && 'border-primary/50 bg-primary/5 ring-2 ring-primary/20'
      )}
    >
      <div className='sticky top-0 z-10 rounded-t-2xl border-b border-border/60 bg-background/92 px-3 py-3 backdrop-blur supports-[backdrop-filter]:bg-background/85'>
        <div className='mb-3 h-1 w-full overflow-hidden rounded-full bg-muted/80'>
          <div
            className={cn(
              'h-full w-16 rounded-full',
              getStatusCategoryAccent(column.statusCategory?.key)
            )}
          />
        </div>

        <div className='flex items-start justify-between gap-3'>
          <div className='min-w-0'>
            <h2 className='truncate text-sm font-semibold uppercase tracking-wide text-foreground'>
              {column.statusName}
            </h2>
            {column.statusCategory?.name ? (
              <p className='mt-1 truncate text-[11px] font-medium text-muted-foreground'>
                {column.statusCategory.name}
              </p>
            ) : null}
          </div>

          <Badge
            variant='secondary'
            className='shrink-0 rounded-full border border-border/60 bg-background px-2.5 py-1 text-xs font-semibold shadow-sm'
          >
            {column.total}
          </Badge>
        </div>
      </div>

      <SortableContext
        items={sortableItems}
        strategy={verticalListSortingStrategy}
      >
        <div className='min-h-0 flex-1 space-y-3 overflow-y-auto px-3 pb-3 pt-2'>
          {column.items.length > 0 ? (
            column.items.map((item) => (
              <PMWorkItemBoardCard
                key={item.id}
                item={item}
                statusId={column.statusId}
                dragDisabled={dragDisabled}
                onSelect={onSelectWorkItem}
              />
            ))
          ) : (
            <div className='flex h-28 items-center justify-center rounded-xl border border-dashed border-border/70 bg-background/70 px-4 text-center text-sm text-muted-foreground'>
              No work items in this lane.
            </div>
          )}
        </div>
      </SortableContext>
    </section>
  );
}
