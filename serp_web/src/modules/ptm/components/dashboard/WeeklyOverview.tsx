/**
 * PTM v2 - Weekly Overview Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Weekly productivity chart
 */

'use client';

import { useMemo } from 'react';
import { TrendingUp, BarChart3 } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { cn } from '@/shared/utils';
import { useGetTasksQuery } from '../../services/taskApi';
import type { Task } from '../../types';

interface DayData {
  day: string;
  completed: number;
  total: number;
  focusTime: number;
}

export function WeeklyOverview() {
  const { data: paginatedData, isLoading } = useGetTasksQuery({});
  const tasks = paginatedData?.data?.items || [];

  const weekData = useMemo<DayData[]>(() => {
    const today = new Date();
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const data: DayData[] = [];

    // Get last 7 days
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      date.setHours(0, 0, 0, 0);

      const nextDay = new Date(date);
      nextDay.setDate(nextDay.getDate() + 1);

      // Filter tasks completed on this day
      const dayTasks = tasks.filter((task) => {
        if (!task.completedAt) return false;
        const completedDate = new Date(task.completedAt);
        return completedDate >= date && completedDate < nextDay;
      });

      // Calculate focus time (sum of deep work tasks)
      const focusTime = dayTasks
        .filter((t) => t.isDeepWork)
        .reduce(
          (acc, t) => acc + (t.actualDurationHours || t.estimatedDurationHours),
          0
        );

      data.push({
        day: dayNames[date.getDay()],
        completed: dayTasks.length,
        total: tasks.filter((t: Task) => {
          const createdDate = new Date(t.createdAt);
          return createdDate <= nextDay;
        }).length,
        focusTime,
      });
    }

    return data;
  }, [tasks]);

  // Calculate previous week data for trend comparison
  const previousWeekData = useMemo(() => {
    const today = new Date();
    let prevWeekCompleted = 0;
    let prevWeekFocusTime = 0;

    for (let i = 13; i >= 7; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      date.setHours(0, 0, 0, 0);

      const nextDay = new Date(date);
      nextDay.setDate(nextDay.getDate() + 1);

      const dayTasks = tasks.filter((task) => {
        if (!task.completedAt) return false;
        const completedDate = new Date(task.completedAt);
        return completedDate >= date && completedDate < nextDay;
      });

      prevWeekCompleted += dayTasks.length;
      prevWeekFocusTime += dayTasks
        .filter((t) => t.isDeepWork)
        .reduce(
          (acc, t) => acc + (t.actualDurationHours || t.estimatedDurationHours),
          0
        );
    }

    return { completed: prevWeekCompleted, focusTime: prevWeekFocusTime };
  }, [tasks]);

  const maxCompleted = Math.max(...weekData.map((d) => d.completed), 1);
  const maxFocusTime = Math.max(...weekData.map((d) => d.focusTime), 1);

  const totalCompleted = weekData.reduce((acc, d) => acc + d.completed, 0);
  const totalFocusTime = weekData.reduce((acc, d) => acc + d.focusTime, 0);

  // Calculate trends
  const completedTrend =
    previousWeekData.completed > 0
      ? ((totalCompleted - previousWeekData.completed) /
          previousWeekData.completed) *
        100
      : 0;

  const focusTimeTrend =
    previousWeekData.focusTime > 0
      ? ((totalFocusTime - previousWeekData.focusTime) /
          previousWeekData.focusTime) *
        100
      : 0;

  return (
    <Card>
      <CardHeader>
        <div className='flex items-center justify-between'>
          <CardTitle className='flex items-center gap-2'>
            <BarChart3 className='h-5 w-5' />
            Weekly Overview
          </CardTitle>
          <div className='flex items-center gap-4 text-xs text-muted-foreground'>
            <div className='flex items-center gap-1.5'>
              <div className='w-3 h-3 rounded bg-blue-500' />
              <span>Tasks</span>
            </div>
            <div className='flex items-center gap-1.5'>
              <div className='w-3 h-3 rounded bg-purple-500' />
              <span>Focus Time</span>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent>
        {isLoading ? (
          <WeeklyOverviewSkeleton />
        ) : (
          <div className='space-y-4'>
            {/* Chart */}
            <div className='flex items-end justify-between gap-2 h-48'>
              {weekData.map((day, index) => (
                <div
                  key={index}
                  className='flex-1 flex flex-col items-center gap-2'
                >
                  {/* Bars container */}
                  <div className='flex-1 w-full flex items-end justify-center gap-1'>
                    {/* Tasks bar */}
                    <div
                      className='w-full bg-blue-500 rounded-t transition-all hover:opacity-80 cursor-pointer relative group'
                      style={{
                        height: `${(day.completed / maxCompleted) * 100}%`,
                        minHeight: day.completed > 0 ? '8px' : '2px',
                      }}
                    >
                      {/* Tooltip on hover */}
                      <div className='absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 bg-popover border rounded shadow-lg text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10'>
                        {day.completed} task{day.completed !== 1 ? 's' : ''}
                      </div>
                    </div>

                    {/* Focus time bar */}
                    <div
                      className='w-full bg-purple-500 rounded-t transition-all hover:opacity-80 cursor-pointer relative group'
                      style={{
                        height: `${(day.focusTime / maxFocusTime) * 100}%`,
                        minHeight: day.focusTime > 0 ? '8px' : '2px',
                      }}
                    >
                      {/* Tooltip on hover */}
                      <div className='absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 bg-popover border rounded shadow-lg text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10'>
                        {day.focusTime.toFixed(1)}h focus
                      </div>
                    </div>
                  </div>

                  {/* Day label */}
                  <span className='text-xs font-medium text-muted-foreground'>
                    {day.day}
                  </span>
                </div>
              ))}
            </div>

            {/* Summary stats */}
            <div className='grid grid-cols-2 gap-4 pt-4 border-t'>
              <div className='space-y-1'>
                <p className='text-xs text-muted-foreground'>Tasks Completed</p>
                <p className='text-2xl font-bold'>{totalCompleted}</p>
                {previousWeekData.completed > 0 && (
                  <div className='flex items-center gap-1 text-xs text-green-600 dark:text-green-400'>
                    <TrendingUp className='h-3 w-3' />
                    <span>
                      {completedTrend > 0 ? '+' : ''}
                      {completedTrend.toFixed(1)}% from last week
                    </span>
                  </div>
                )}
              </div>

              <div className='space-y-1'>
                <p className='text-xs text-muted-foreground'>
                  Total Focus Time
                </p>
                <p className='text-2xl font-bold'>
                  {totalFocusTime.toFixed(1)}h
                </p>
                {previousWeekData.focusTime > 0 && (
                  <div className='flex items-center gap-1 text-xs text-purple-600 dark:text-purple-400'>
                    <TrendingUp className='h-3 w-3' />
                    <span>
                      {focusTimeTrend > 0 ? '+' : ''}
                      {focusTimeTrend.toFixed(1)}% from last week
                    </span>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function WeeklyOverviewSkeleton() {
  return (
    <div className='space-y-4'>
      <div className='flex items-end justify-between gap-2 h-48'>
        {[1, 2, 3, 4, 5, 6, 7].map((i) => (
          <div key={i} className='flex-1 flex flex-col items-center gap-2'>
            <div className='flex-1 w-full flex items-end justify-center gap-1'>
              <Skeleton
                className='w-full'
                style={{ height: `${Math.random() * 80 + 20}%` }}
              />
              <Skeleton
                className='w-full'
                style={{ height: `${Math.random() * 80 + 20}%` }}
              />
            </div>
            <Skeleton className='h-3 w-8' />
          </div>
        ))}
      </div>
      <div className='grid grid-cols-2 gap-4 pt-4 border-t'>
        <div className='space-y-2'>
          <Skeleton className='h-3 w-24' />
          <Skeleton className='h-8 w-16' />
          <Skeleton className='h-3 w-32' />
        </div>
        <div className='space-y-2'>
          <Skeleton className='h-3 w-24' />
          <Skeleton className='h-8 w-16' />
          <Skeleton className='h-3 w-32' />
        </div>
      </div>
    </div>
  );
}
