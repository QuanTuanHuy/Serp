/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board column
 */

import { Badge } from '@/shared/components/ui';
import type { PMWorkItemBoardColumnApi } from '../../../types/api';
import { PMWorkItemBoardCard } from './PMWorkItemBoardCard';

interface PMWorkItemBoardColumnProps {
  column: PMWorkItemBoardColumnApi;
}

export function PMWorkItemBoardColumn({ column }: PMWorkItemBoardColumnProps) {
  return (
    <section className='flex max-h-[calc(100vh-17rem)] min-h-[28rem] w-[20rem] shrink-0 flex-col rounded-xl border bg-muted/35'>
      <div className='sticky top-0 z-10 rounded-t-xl border-b bg-muted/80 px-3 py-3 backdrop-blur'>
        <div className='flex items-start justify-between gap-3'>
          <div className='min-w-0'>
            <h2 className='truncate text-sm font-semibold text-foreground'>
              {column.statusName}
            </h2>
            {column.statusCategory?.name ? (
              <p className='mt-0.5 truncate text-xs text-muted-foreground'>
                {column.statusCategory.name}
              </p>
            ) : null}
          </div>
          <Badge variant='secondary' className='shrink-0'>
            {column.total}
          </Badge>
        </div>
      </div>

      <div className='min-h-0 flex-1 space-y-3 overflow-y-auto p-3'>
        {column.items.length > 0 ? (
          column.items.map((item) => (
            <PMWorkItemBoardCard key={item.id} item={item} />
          ))
        ) : (
          <div className='flex h-32 items-center justify-center rounded-lg border border-dashed bg-background/60 px-4 text-center text-sm text-muted-foreground'>
            No work items in this status.
          </div>
        )}
      </div>
    </section>
  );
}
