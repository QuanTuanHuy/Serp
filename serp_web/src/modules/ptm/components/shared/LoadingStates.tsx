/**
 * PTM v2 - Loading States Components
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Skeleton loaders
 */

import { Skeleton } from '@/shared/components/ui/skeleton';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { cn } from '@/shared/utils';

export function TaskCardSkeleton({ className }: { className?: string }) {
  return (
    <Card className={cn('animate-pulse', className)}>
      <CardContent className='p-4'>
        <div className='space-y-3'>
          <Skeleton className='h-4 w-3/4' />
          <Skeleton className='h-3 w-full' />
          <div className='flex gap-2'>
            <Skeleton className='h-6 w-16' />
            <Skeleton className='h-6 w-20' />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

export function ProjectCardSkeleton({ className }: { className?: string }) {
  return (
    <Card className={cn('animate-pulse', className)}>
      <CardHeader>
        <div className='flex items-start justify-between'>
          <Skeleton className='h-8 w-8 rounded-lg' />
          <Skeleton className='h-4 w-16' />
        </div>
      </CardHeader>
      <CardContent>
        <div className='space-y-3'>
          <Skeleton className='h-5 w-2/3' />
          <Skeleton className='h-3 w-full' />
          <Skeleton className='h-2 w-full rounded-full' />
          <div className='flex gap-4 text-sm'>
            <Skeleton className='h-4 w-20' />
            <Skeleton className='h-4 w-20' />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

export function CalendarEventSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn('p-2 rounded-md bg-muted animate-pulse', className)}>
      <Skeleton className='h-3 w-3/4 mb-1' />
      <Skeleton className='h-2 w-1/2' />
    </div>
  );
}

export function DashboardSkeleton() {
  return (
    <div className='space-y-6'>
      {/* Stats Grid */}
      <div className='grid grid-cols-3 gap-4'>
        {[1, 2, 3].map((i) => (
          <Card key={i} className='animate-pulse'>
            <CardContent className='p-6'>
              <div className='space-y-2'>
                <Skeleton className='h-4 w-20' />
                <Skeleton className='h-8 w-16' />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Schedule */}
      <Card className='animate-pulse'>
        <CardHeader>
          <Skeleton className='h-6 w-40' />
        </CardHeader>
        <CardContent>
          <div className='space-y-3'>
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className='h-20 w-full' />
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
