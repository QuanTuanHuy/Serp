/**
 * LoadingSkeleton Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Loading state for task detail page
 */

'use client';

import { Skeleton } from '@/shared/components/ui/skeleton';

export function LoadingSkeleton() {
  return (
    <div className='container mx-auto py-6'>
      <div className='space-y-6'>
        {/* Header skeleton */}
        <div className='flex justify-between items-start'>
          <div className='space-y-3 flex-1'>
            <Skeleton className='h-10 w-3/4' />
            <Skeleton className='h-5 w-1/2' />
          </div>
          <div className='flex gap-2'>
            <Skeleton className='h-9 w-20' />
            <Skeleton className='h-9 w-24' />
          </div>
        </div>

        {/* Tabs skeleton */}
        <Skeleton className='h-10 w-64' />

        {/* Content skeleton */}
        <div className='grid grid-cols-2 md:grid-cols-4 gap-4'>
          <Skeleton className='h-24' />
          <Skeleton className='h-24' />
          <Skeleton className='h-24' />
          <Skeleton className='h-24' />
        </div>

        <Skeleton className='h-96' />
      </div>
    </div>
  );
}
