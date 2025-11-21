/**
 * PTM v2 - Gantt View Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Timeline visualization with dependencies
 */

'use client';

import { useMemo } from 'react';
import { Card } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import {
  useGetTasksQuery,
  useGetTaskDependenciesQuery,
} from '../../services/taskApi';
import type { Task } from '../../types';

interface GanttViewProps {
  projectId?: number;
  className?: string;
}

interface GanttTask extends Task {
  startDate: Date;
  endDate: Date;
  duration: number;
}

export function GanttView({ projectId, className }: GanttViewProps) {
  const { data: allTasks = [] } = useGetTasksQuery(
    projectId ? { projectId } : {}
  );

  // Filter tasks with deadlines and calculate Gantt data
  const ganttTasks: GanttTask[] = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return allTasks
      .filter((task) => task.deadlineMs)
      .map((task) => {
        const endDate = new Date(task.deadlineMs!);
        const durationDays = task.estimatedDurationHours
          ? Math.ceil(task.estimatedDurationHours / 8)
          : 1;
        const startDate = new Date(endDate);
        startDate.setDate(startDate.getDate() - durationDays);

        return {
          ...task,
          startDate,
          endDate,
          duration: durationDays,
        };
      })
      .sort((a, b) => a.startDate.getTime() - b.startDate.getTime());
  }, [allTasks]);

  // Calculate timeline bounds
  const { minDate, maxDate, totalDays } = useMemo(() => {
    if (ganttTasks.length === 0) {
      const today = new Date();
      return {
        minDate: today,
        maxDate: new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000),
        totalDays: 7,
      };
    }

    const min = new Date(
      Math.min(...ganttTasks.map((t) => t.startDate.getTime()))
    );
    const max = new Date(
      Math.max(...ganttTasks.map((t) => t.endDate.getTime()))
    );
    const days =
      Math.ceil((max.getTime() - min.getTime()) / (1000 * 60 * 60 * 24)) + 1;

    return {
      minDate: min,
      maxDate: max,
      totalDays: days,
    };
  }, [ganttTasks]);

  // Generate date headers
  const dateHeaders = useMemo(() => {
    const headers: Date[] = [];
    for (let i = 0; i < totalDays; i++) {
      const date = new Date(minDate);
      date.setDate(date.getDate() + i);
      headers.push(date);
    }
    return headers;
  }, [minDate, totalDays]);

  const getTaskPosition = (task: GanttTask) => {
    const startOffset = Math.floor(
      (task.startDate.getTime() - minDate.getTime()) / (1000 * 60 * 60 * 24)
    );
    const width = task.duration;

    return {
      left: `${(startOffset / totalDays) * 100}%`,
      width: `${(width / totalDays) * 100}%`,
    };
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH':
        return 'bg-red-500';
      case 'MEDIUM':
        return 'bg-yellow-500';
      case 'LOW':
        return 'bg-blue-500';
      default:
        return 'bg-gray-500';
    }
  };

  const getStatusOpacity = (status: string) => {
    switch (status) {
      case 'DONE':
        return 'opacity-60';
      case 'IN_PROGRESS':
        return 'opacity-90';
      default:
        return 'opacity-100';
    }
  };

  if (ganttTasks.length === 0) {
    return (
      <Card className='p-12 text-center'>
        <p className='text-muted-foreground'>No tasks with deadlines</p>
        <p className='text-sm text-muted-foreground mt-2'>
          Add deadlines to tasks to see the timeline
        </p>
      </Card>
    );
  }

  return (
    <div className={cn('space-y-4', className)}>
      {/* Timeline Header */}
      <Card className='overflow-x-auto'>
        <div className='min-w-[800px]'>
          {/* Date Headers */}
          <div className='flex border-b bg-muted/50'>
            <div className='w-48 shrink-0 p-3 font-semibold text-sm border-r'>
              Task
            </div>
            <div className='flex-1 flex'>
              {dateHeaders.map((date, index) => (
                <div
                  key={index}
                  className='flex-1 p-2 text-center text-xs border-r last:border-r-0'
                >
                  <div className='font-medium'>
                    {date.toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                    })}
                  </div>
                  <div className='text-muted-foreground'>
                    {date.toLocaleDateString('en-US', { weekday: 'short' })}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Task Rows */}
          <div className='divide-y'>
            {ganttTasks.map((task) => (
              <div
                key={task.id}
                className='flex hover:bg-accent/50 transition-colors'
              >
                {/* Task Name */}
                <div className='w-48 shrink-0 p-3 border-r'>
                  <div className='font-medium text-sm truncate'>
                    {task.title}
                  </div>
                  <div className='flex items-center gap-1 mt-1'>
                    <Badge
                      variant='outline'
                      className={cn(
                        'text-xs',
                        getPriorityColor(task.priority),
                        'text-white'
                      )}
                    >
                      {task.priority}
                    </Badge>
                    {task.isBlocked && (
                      <Badge variant='destructive' className='text-xs'>
                        Blocked
                      </Badge>
                    )}
                  </div>
                </div>

                {/* Timeline Bar */}
                <div className='flex-1 relative p-2'>
                  <div
                    className={cn(
                      'absolute h-8 rounded-md flex items-center px-2 text-white text-xs font-medium',
                      getPriorityColor(task.priority),
                      getStatusOpacity(task.status)
                    )}
                    style={getTaskPosition(task)}
                  >
                    <span className='truncate'>{task.duration}d</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </Card>

      {/* Legend */}
      <div className='flex items-center gap-6 text-sm'>
        <div className='flex items-center gap-2'>
          <div className='w-4 h-4 rounded bg-red-500'></div>
          <span className='text-muted-foreground'>High Priority</span>
        </div>
        <div className='flex items-center gap-2'>
          <div className='w-4 h-4 rounded bg-yellow-500'></div>
          <span className='text-muted-foreground'>Medium Priority</span>
        </div>
        <div className='flex items-center gap-2'>
          <div className='w-4 h-4 rounded bg-blue-500'></div>
          <span className='text-muted-foreground'>Low Priority</span>
        </div>
        <div className='flex items-center gap-2'>
          <Badge variant='destructive' className='text-xs'>
            Blocked
          </Badge>
          <span className='text-muted-foreground'>
            Has incomplete dependencies
          </span>
        </div>
      </div>
    </div>
  );
}
