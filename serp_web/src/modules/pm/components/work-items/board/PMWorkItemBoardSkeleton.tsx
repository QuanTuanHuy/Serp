/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board loading skeleton
 */

import { Skeleton } from '@/shared/components/ui';

export function PMWorkItemBoardSkeleton() {
  return (
    <div className='rounded-2xl border border-border/60 bg-muted/15 p-2'>
      <div className='flex gap-4 overflow-x-auto pb-2'>
        {Array.from({ length: 4 }).map((_, columnIndex) => (
          <div
            key={columnIndex}
            className='min-h-[28rem] w-[19.5rem] shrink-0 rounded-2xl border border-border/60 bg-muted/20 p-3 shadow-sm'
          >
            <Skeleton className='mb-3 h-1 w-full rounded-full' />
            <div className='mb-4 flex items-start justify-between gap-3'>
              <div className='space-y-2'>
                <Skeleton className='h-4 w-28' />
                <Skeleton className='h-3 w-20' />
              </div>
              <Skeleton className='h-7 w-10 rounded-full' />
            </div>
            <div className='space-y-3'>
              {Array.from({ length: 4 }).map((__, cardIndex) => (
                <Skeleton key={cardIndex} className='h-32 rounded-lg' />
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
