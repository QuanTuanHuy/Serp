/*
Author: QuanTuanHuy
Description: Part of Serp Project
Unscheduled Tasks Panel - Shows tasks that need to be scheduled

ARCHITECTURE NOTE:
- Each schedule plan has its own snapshot of tasks
- Tasks shown here belong to the ACTIVE plan only
- When PTM Task changes → syncs to active plan → triggers reschedule
- Proposed/archived plans maintain their own snapshots for comparison
*/

'use client';

import { useState } from 'react';
import {
  useGetScheduleTasksQuery,
  useGetActiveSchedulePlanQuery,
} from '../../api';
import type { ScheduleTask } from '../../types';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  Clock,
  AlertCircle,
  Calendar,
  Flame,
  Settings,
  ChevronDown,
  ChevronRight,
  Ban,
  Brain,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { format } from 'date-fns';

interface UnscheduledTasksPanelProps {
  onEditTask?: (task: ScheduleTask) => void;
  className?: string;
}

export function UnscheduledTasksPanel({
  onEditTask,
  className,
}: UnscheduledTasksPanelProps) {
  const [expandedStatus, setExpandedStatus] = useState<{
    pending: boolean;
    excluded: boolean;
  }>({
    pending: true,
    excluded: false,
  });

  // Get active plan info
  const { data: activePlan, isLoading: loadingPlan } =
    useGetActiveSchedulePlanQuery();

  // Get tasks with PENDING status (not yet scheduled) from ACTIVE plan
  const { data: pendingTasks = [], isLoading: loadingPending } =
    useGetScheduleTasksQuery({
      // planId not provided → defaults to active plan in backend
      status: 'PENDING',
    });

  // Get tasks with EXCLUDED status (parent tasks with subtasks) from ACTIVE plan
  const { data: excludedTasks = [], isLoading: loadingExcluded } =
    useGetScheduleTasksQuery({
      status: 'EXCLUDED',
    });

  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
    }
    return `${mins}m`;
  };

  const formatDeadline = (deadlineMs?: number) => {
    if (!deadlineMs) return null;
    const date = new Date(deadlineMs);
    const now = new Date();
    const diffDays = Math.ceil(
      (date.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
    );

    if (diffDays < 0) {
      return (
        <span className='text-red-600 dark:text-red-400 text-xs'>
          Overdue {Math.abs(diffDays)}d
        </span>
      );
    }
    if (diffDays === 0) {
      return (
        <span className='text-red-600 dark:text-red-400 text-xs'>Today</span>
      );
    }
    if (diffDays === 1) {
      return (
        <span className='text-amber-600 dark:text-amber-400 text-xs'>
          Tomorrow
        </span>
      );
    }
    if (diffDays <= 7) {
      return (
        <span className='text-amber-600 dark:text-amber-400 text-xs'>
          {diffDays}d left
        </span>
      );
    }
    return (
      <span className='text-muted-foreground text-xs'>
        {date.toLocaleDateString()}
      </span>
    );
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'URGENT':
        return 'bg-red-500/10 text-red-600 dark:text-red-400 border-red-200 dark:border-red-800';
      case 'HIGH':
        return 'bg-orange-500/10 text-orange-600 dark:text-orange-400 border-orange-200 dark:border-orange-800';
      case 'MEDIUM':
        return 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-200 dark:border-blue-800';
      case 'LOW':
        return 'bg-gray-500/10 text-gray-600 dark:text-gray-400 border-gray-200 dark:border-gray-800';
      default:
        return 'bg-gray-500/10 text-gray-600 dark:text-gray-400';
    }
  };

  const renderTaskCard = (task: ScheduleTask) => (
    <Card
      key={task.id}
      className='hover:bg-accent/50 transition-colors cursor-pointer'
    >
      <CardContent className='p-3 space-y-2'>
        {/* Title & Priority */}
        <div className='flex items-start justify-between gap-2'>
          <div className='flex-1 space-y-1'>
            <div className='font-medium text-sm line-clamp-2'>{task.title}</div>
            {task.category && (
              <div className='text-xs text-muted-foreground'>
                {task.category}
              </div>
            )}
          </div>
          <Badge
            variant='outline'
            className={cn('text-xs shrink-0', getPriorityColor(task.priority))}
          >
            {task.priority}
          </Badge>
        </div>

        {/* Metadata */}
        <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
          <div className='flex items-center gap-1'>
            <Clock className='h-3 w-3' />
            <span>{formatDuration(task.durationMin)}</span>
          </div>

          {task.deadlineMs && (
            <div className='flex items-center gap-1'>
              <Calendar className='h-3 w-3' />
              {formatDeadline(task.deadlineMs)}
            </div>
          )}

          {task.isDeepWork && (
            <Badge
              variant='outline'
              className='text-[10px] px-1.5 py-0 h-5 bg-purple-50 dark:bg-purple-950/30 border-purple-200 dark:border-purple-800 text-purple-700 dark:text-purple-400'
            >
              <Flame className='h-2.5 w-2.5 mr-0.5' />
              Focus
            </Badge>
          )}

          {task.hasSubtasks && (
            <Badge variant='outline' className='text-[10px] px-1.5 py-0 h-5'>
              {task.completedSubtaskCount}/{task.totalSubtaskCount} subtasks
            </Badge>
          )}
        </div>

        {/* Actions */}
        <div className='flex items-center gap-2 pt-1'>
          <Button
            variant='ghost'
            size='sm'
            className='h-7 text-xs'
            onClick={() => onEditTask?.(task)}
          >
            <Settings className='h-3 w-3 mr-1' />
            Edit Constraints
          </Button>
        </div>
      </CardContent>
    </Card>
  );

  return (
    <Card className={cn('flex flex-col', className)}>
      <CardHeader className='pb-3'>
        <div className='flex items-start justify-between'>
          <div className='space-y-1'>
            <CardTitle className='text-base'>Tasks to Schedule</CardTitle>
            <CardDescription className='text-xs'>
              {activePlan ? (
                <>
                  Active Plan:{' '}
                  <strong>{activePlan.name || `#${activePlan.id}`}</strong>
                  {' • '}Tasks synced from PTM Task
                </>
              ) : (
                'Loading plan...'
              )}
            </CardDescription>
          </div>
          {loadingPlan && (
            <div className='h-4 w-4 border-2 border-primary border-t-transparent rounded-full animate-spin' />
          )}
        </div>
      </CardHeader>

      <CardContent className='flex-1 p-0 overflow-hidden'>
        <ScrollArea className='h-full px-4 pb-4'>
          <div className='space-y-4'>
            {/* Pending Tasks Section */}
            <div className='space-y-2'>
              <button
                onClick={() =>
                  setExpandedStatus((prev) => ({
                    ...prev,
                    pending: !prev.pending,
                  }))
                }
                className='flex items-center justify-between w-full text-sm font-semibold hover:bg-accent/50 p-2 rounded transition-colors'
              >
                <div className='flex items-center gap-2'>
                  {expandedStatus.pending ? (
                    <ChevronDown className='h-4 w-4' />
                  ) : (
                    <ChevronRight className='h-4 w-4' />
                  )}
                  <AlertCircle className='h-4 w-4 text-amber-600 dark:text-amber-400' />
                  <span>Pending ({pendingTasks.length})</span>
                </div>
                {loadingPending && (
                  <div className='h-4 w-4 border-2 border-primary border-t-transparent rounded-full animate-spin' />
                )}
              </button>

              {expandedStatus.pending && (
                <div className='space-y-2 pl-6'>
                  {pendingTasks.length === 0 ? (
                    <div className='text-sm text-muted-foreground py-4 text-center'>
                      No pending tasks
                    </div>
                  ) : (
                    pendingTasks.map(renderTaskCard)
                  )}
                </div>
              )}
            </div>

            {/* Excluded Tasks Section */}
            <div className='space-y-2'>
              <button
                onClick={() =>
                  setExpandedStatus((prev) => ({
                    ...prev,
                    excluded: !prev.excluded,
                  }))
                }
                className='flex items-center justify-between w-full text-sm font-semibold hover:bg-accent/50 p-2 rounded transition-colors'
              >
                <div className='flex items-center gap-2'>
                  {expandedStatus.excluded ? (
                    <ChevronDown className='h-4 w-4' />
                  ) : (
                    <ChevronRight className='h-4 w-4' />
                  )}
                  <Ban className='h-4 w-4 text-muted-foreground' />
                  <span>Excluded ({excludedTasks?.length})</span>
                </div>
                {loadingExcluded && (
                  <div className='h-4 w-4 border-2 border-primary border-t-transparent rounded-full animate-spin' />
                )}
              </button>

              {expandedStatus.excluded && (
                <div className='space-y-2 pl-6'>
                  {excludedTasks.length === 0 ? (
                    <div className='text-sm text-muted-foreground py-4 text-center'>
                      No excluded tasks
                    </div>
                  ) : (
                    <>
                      <div className='text-xs text-muted-foreground mb-2 px-2'>
                        Parent tasks with subtasks are excluded from scheduling.
                        Schedule the subtasks instead.
                      </div>
                      {excludedTasks.map(renderTaskCard)}
                    </>
                  )}
                </div>
              )}
            </div>
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  );
}
