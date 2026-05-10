/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board loading skeleton
 */

import { Skeleton } from '@/shared/components/ui';

export function PMWorkItemBoardSkeleton() {
  return (
    <div className='flex gap-4 overflow-x-auto pb-2'>
      {Array.from({ length: 4 }).map((_, columnIndex) => (
        <div
          key={columnIndex}
          className='min-h-[28rem] w-[20rem] shrink-0 rounded-xl border bg-muted/35 p-3'
        >
          <div className='mb-4 flex items-center justify-between'>
            <Skeleton className='h-5 w-32' />
            <Skeleton className='h-5 w-8 rounded-full' />
          </div>
          <div className='space-y-3'>
            {Array.from({ length: 4 }).map((__, cardIndex) => (
              <Skeleton key={cardIndex} className='h-32 rounded-lg' />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
