/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Enhanced Activity Feed with Infinite Scroll
 */

'use client';

import { useState, useCallback, useRef, useEffect } from 'react';
import { Bell, Filter, Loader2 } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { cn } from '@/shared/utils';
import { useGetActivityFeedQuery } from '../../services/activityApi';
import type { ActivityEventType } from '../../types';
import { ActivityItem } from './ActivityItem';

interface Props {
  maxItems?: number;
  className?: string;
  highlightActivityId?: string;
}

export function ActivityFeedEnhanced({
  maxItems = 50,
  className,
  highlightActivityId,
}: Props) {
  const [filterType, setFilterType] = useState<ActivityEventType | 'all'>(
    'all'
  );
  const [page, setPage] = useState(0);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useRef<HTMLDivElement | null>(null);

  const { data, isLoading, isFetching, error } = useGetActivityFeedQuery({
    types: filterType === 'all' ? undefined : [filterType],
    page,
    size: 20,
  });

  // Infinite scroll
  const handleObserver = useCallback(
    (entries: IntersectionObserverEntry[]) => {
      const [target] = entries;
      if (target.isIntersecting && data?.hasMore && !isFetching) {
        setPage((prev) => prev + 1);
      }
    },
    [data?.hasMore, isFetching]
  );

  useEffect(() => {
    const element = loadMoreRef.current;
    if (!element) return;

    observerRef.current = new IntersectionObserver(handleObserver, {
      threshold: 0.5,
    });
    observerRef.current.observe(element);

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [handleObserver]);

  // Reset page when filter changes
  useEffect(() => {
    setPage(0);
  }, [filterType]);

  return (
    <div className={cn('space-y-4', className)}>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Bell className='h-5 w-5 text-blue-600' />
          <h3 className='font-semibold'>Activity Feed</h3>
          <Badge variant='secondary' className='ml-2'>
            {data?.totalCount ?? 0} activities
          </Badge>
        </div>

        {/* Filter */}
        <Select
          value={filterType}
          onValueChange={(v) => setFilterType(v as any)}
        >
          <SelectTrigger className='w-[200px]'>
            <Filter className='h-4 w-4 mr-2' />
            <SelectValue placeholder='Filter' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='all'>All Activities</SelectItem>
            <SelectItem value='task_created'>Task Created</SelectItem>
            <SelectItem value='task_updated'>Task Updated</SelectItem>
            <SelectItem value='task_completed'>Task Completed</SelectItem>
            <SelectItem value='schedule_optimized'>
              Schedule Optimized
            </SelectItem>
            <SelectItem value='algorithm_executed'>
              Algorithm Executed
            </SelectItem>
            <SelectItem value='deadline_risk_detected'>
              Deadline Risks
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Feed */}
      <Card>
        <CardContent className='p-0'>
          {isLoading && page === 0 ? (
            <div className='p-12 text-center'>
              <Loader2 className='h-8 w-8 animate-spin mx-auto text-muted-foreground' />
              <p className='text-sm text-muted-foreground mt-2'>
                Loading activities...
              </p>
            </div>
          ) : error ? (
            <div className='p-8 text-center text-destructive'>
              Failed to load activities
            </div>
          ) : data?.activities.length === 0 ? (
            <div className='p-8 text-center text-muted-foreground'>
              No activities yet
            </div>
          ) : (
            <>
              <div className='divide-y'>
                {data?.activities.map((activity, index) => (
                  <ActivityItem
                    key={activity.id}
                    activity={activity}
                    isLatest={index === 0}
                    highlightId={highlightActivityId}
                  />
                ))}
              </div>

              {/* Infinite scroll trigger */}
              {data?.hasMore && (
                <div ref={loadMoreRef} className='p-4 text-center border-t'>
                  {isFetching ? (
                    <Loader2 className='h-5 w-5 animate-spin mx-auto text-muted-foreground' />
                  ) : (
                    <Button
                      variant='ghost'
                      size='sm'
                      onClick={() => setPage((p) => p + 1)}
                    >
                      Load More
                    </Button>
                  )}
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
