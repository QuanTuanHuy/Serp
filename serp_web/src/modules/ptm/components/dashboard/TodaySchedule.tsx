/**
 * PTM v2 - Today's Schedule Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Timeline view of today's tasks
 */

'use client';

import { useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { Clock, AlertCircle, Sparkles } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import { useGetScheduleEventsQuery } from '../../api';
import { StatusBadge, PriorityBadge } from '../shared';
import type { ScheduleEvent } from '../../types';

export function TodaySchedule() {
  const router = useRouter();
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  const {
    data: events = [],
    isLoading,
    error,
  } = useGetScheduleEventsQuery({
    startDateMs: today.getTime(),
    endDateMs: tomorrow.getTime(),
  });

  const sortedEvents = useMemo(() => {
    return [...events].sort((a, b) => a.startMin - b.startMin);
  }, [events]);

  const currentTime = new Date();
  const currentMinutes = currentTime.getHours() * 60 + currentTime.getMinutes();

  const formatTime = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    const period = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours > 12 ? hours - 12 : hours === 0 ? 12 : hours;
    return `${displayHours}:${mins.toString().padStart(2, '0')} ${period}`;
  };

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Clock className='h-5 w-5' />
            Today's Schedule
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className='flex flex-col items-center justify-center py-8 text-center'>
            <AlertCircle className='h-12 w-12 text-muted-foreground mb-3' />
            <p className='text-sm text-muted-foreground'>
              Failed to load schedule
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader
        className='cursor-pointer hover:bg-accent/50 transition-colors rounded-t-lg'
        onClick={() => router.push('/ptm/schedule')}
      >
        <div className='flex items-center justify-between'>
          <CardTitle className='flex items-center gap-2'>
            <Clock className='h-5 w-5' />
            Today's Schedule
          </CardTitle>
          <Badge variant='outline' className='text-xs'>
            {events.length} event{events.length !== 1 ? 's' : ''}
          </Badge>
        </div>
      </CardHeader>

      <CardContent>
        {isLoading ? (
          <div className='space-y-3'>
            {[1, 2, 3].map((i) => (
              <TodayScheduleEventSkeleton key={i} />
            ))}
          </div>
        ) : sortedEvents.length === 0 ? (
          <div className='flex flex-col items-center justify-center py-8 text-center'>
            <Clock className='h-12 w-12 text-muted-foreground mb-3' />
            <p className='text-sm font-medium mb-1'>No events scheduled</p>
            <p className='text-xs text-muted-foreground'>
              Your schedule is clear for today
            </p>
          </div>
        ) : (
          <ScrollArea className='h-[400px] pr-4'>
            <div className='space-y-3'>
              {sortedEvents.map((event) => (
                <ScheduleEventCard
                  key={event.id}
                  event={event}
                  currentMinutes={currentMinutes}
                  formatTime={formatTime}
                />
              ))}
            </div>
          </ScrollArea>
        )}
      </CardContent>
    </Card>
  );
}

interface ScheduleEventCardProps {
  event: ScheduleEvent;
  currentMinutes: number;
  formatTime: (minutes: number) => string;
}

function ScheduleEventCard({
  event,
  currentMinutes,
  formatTime,
}: ScheduleEventCardProps) {
  const isNow =
    currentMinutes >= event.startMin && currentMinutes < event.endMin;
  const isPast = currentMinutes >= event.endMin;
  const isFuture = currentMinutes < event.startMin;

  return (
    <div
      className={cn(
        'relative flex gap-3 p-3 rounded-lg border transition-all',
        isNow &&
          'bg-blue-50 dark:bg-blue-950/30 border-blue-200 dark:border-blue-800 shadow-sm',
        isPast && 'opacity-60',
        isFuture && 'bg-card hover:bg-accent/50'
      )}
    >
      {/* Timeline indicator */}
      <div className='flex flex-col items-center'>
        <div
          className={cn(
            'w-2 h-2 rounded-full mt-1.5',
            isNow && 'bg-blue-500 animate-pulse',
            isPast && 'bg-gray-300 dark:bg-gray-700',
            isFuture && 'bg-primary'
          )}
        />
        <div className='w-px flex-1 bg-border mt-1' />
      </div>

      {/* Event content */}
      <div className='flex-1 min-w-0'>
        <div className='flex items-start justify-between gap-2 mb-2'>
          <div className='flex-1 min-w-0'>
            <h4
              className={cn(
                'font-medium text-sm truncate',
                isPast && 'line-through text-muted-foreground'
              )}
            >
              {event.title || 'Untitled Task'}
            </h4>
            <div className='flex items-center gap-2 mt-1'>
              <span className='text-xs text-muted-foreground'>
                {formatTime(event.startMin)} - {formatTime(event.endMin)}
              </span>
              <span className='text-xs text-muted-foreground'>
                ({event.durationMin}m)
              </span>
            </div>
          </div>

          {isNow && (
            <Badge className='bg-blue-500 text-white text-xs shrink-0'>
              Now
            </Badge>
          )}
        </div>

        {/* Metadata */}
        <div className='flex items-center gap-2 flex-wrap'>
          {event.priority && (
            <PriorityBadge priority={event.priority as any} showLabel={false} />
          )}

          {event.isDeepWork && (
            <Badge
              variant='outline'
              className='text-xs bg-purple-50 dark:bg-purple-950/30 border-purple-200 dark:border-purple-800 text-purple-700 dark:text-purple-400'
            >
              <Sparkles className='h-3 w-3 mr-1' />
              Focus
            </Badge>
          )}

          {event.taskPart && event.totalParts && event.totalParts > 1 && (
            <Badge variant='outline' className='text-xs'>
              Part {event.taskPart}/{event.totalParts}
            </Badge>
          )}

          {event.status && (
            <StatusBadge status={event.status.toUpperCase() as any} />
          )}
        </div>
      </div>
    </div>
  );
}

function TodayScheduleEventSkeleton() {
  return (
    <div className='flex gap-3 p-3 rounded-lg border'>
      <div className='flex flex-col items-center'>
        <Skeleton className='w-2 h-2 rounded-full mt-1.5' />
        <Skeleton className='w-px h-16 mt-1' />
      </div>
      <div className='flex-1 space-y-2'>
        <Skeleton className='h-4 w-3/4' />
        <Skeleton className='h-3 w-1/2' />
        <div className='flex gap-2'>
          <Skeleton className='h-5 w-16' />
          <Skeleton className='h-5 w-16' />
        </div>
      </div>
    </div>
  );
}
