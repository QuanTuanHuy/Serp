/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail loading and error states
 */

import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Skeleton,
} from '@/shared/components/ui';

export function InlineError({ error }: { error: unknown }) {
  return (
    <Alert variant='destructive'>
      <AlertTitle>Unable to load data</AlertTitle>
      <AlertDescription>{getErrorMessage(error)}</AlertDescription>
    </Alert>
  );
}

export function ListSkeleton({ rows }: { rows: number }) {
  return (
    <div className='space-y-2'>
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} className='h-16 w-full' />
      ))}
    </div>
  );
}

export function PMWorkItemDetailSkeleton() {
  return (
    <div className='space-y-6 p-6'>
      <div className='flex items-center justify-between'>
        <Skeleton className='h-5 w-40' />
        <div className='flex gap-2'>
          <Skeleton className='h-8 w-8' />
          <Skeleton className='h-8 w-8' />
        </div>
      </div>
      <div className='grid gap-8 lg:grid-cols-[minmax(0,1fr)_320px] xl:grid-cols-[minmax(0,1fr)_360px]'>
        <div className='space-y-6'>
          <Skeleton className='h-9 w-3/4' />
          <Skeleton className='h-24 w-full' />
          <Skeleton className='h-20 w-full' />
        </div>
        <Skeleton className='h-96 w-full' />
      </div>
    </div>
  );
}
