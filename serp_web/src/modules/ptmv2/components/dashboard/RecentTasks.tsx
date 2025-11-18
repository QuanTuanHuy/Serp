/**
 * PTM v2 - Recent Tasks Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - List of recent tasks with quick actions
 */

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  CheckCircle2,
  Circle,
  Clock,
  MoreVertical,
  Trash2,
  PlayCircle,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { cn } from '@/shared/utils';
import {
  useGetTasksQuery,
  useUpdateTaskMutation,
  useDeleteTaskMutation,
} from '../../services/taskApi';
import { StatusBadge, PriorityBadge } from '../shared';
import type { Task, TaskStatus } from '../../types';

export function RecentTasks() {
  const router = useRouter();
  const { data: allTasks = [], isLoading } = useGetTasksQuery({});
  const [updateTask] = useUpdateTaskMutation();
  const [deleteTask] = useDeleteTaskMutation();

  // Get recent tasks (sort by updatedAt, limit to 10)
  const recentTasks = useMemo(() => {
    return [...allTasks]
      .sort(
        (a, b) =>
          new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      )
      .slice(0, 10);
  }, [allTasks]);

  const handleToggleComplete = async (task: Task) => {
    const newStatus: TaskStatus = task.status === 'DONE' ? 'TODO' : 'DONE';
    await updateTask({
      id: task.id,
      status: newStatus,
    });
  };

  const handleStartTask = async (task: Task) => {
    await updateTask({
      id: task.id,
      status: 'IN_PROGRESS',
    });
  };

  const handleDelete = async (taskId: string) => {
    if (confirm('Are you sure you want to delete this task?')) {
      await deleteTask(taskId);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className='flex items-center justify-between'>
          <CardTitle className='flex items-center gap-2'>
            <Clock className='h-5 w-5' />
            Recent Tasks
          </CardTitle>
          <Button
            variant='ghost'
            size='sm'
            onClick={() => router.push('/ptmv2/tasks')}
          >
            View All
          </Button>
        </div>
      </CardHeader>

      <CardContent>
        {isLoading ? (
          <div className='space-y-3'>
            {[1, 2, 3, 4, 5].map((i) => (
              <RecentTaskSkeleton key={i} />
            ))}
          </div>
        ) : recentTasks.length === 0 ? (
          <div className='flex flex-col items-center justify-center py-8 text-center'>
            <Clock className='h-12 w-12 text-muted-foreground mb-3' />
            <p className='text-sm font-medium mb-1'>No tasks yet</p>
            <p className='text-xs text-muted-foreground'>
              Create your first task to get started
            </p>
          </div>
        ) : (
          <ScrollArea className='h-[500px] pr-4'>
            <div className='space-y-2'>
              {recentTasks.map((task) => (
                <TaskItem
                  key={task.id}
                  task={task}
                  onToggleComplete={() => handleToggleComplete(task)}
                  onStart={() => handleStartTask(task)}
                  onDelete={() => handleDelete(task.id)}
                />
              ))}
            </div>
          </ScrollArea>
        )}
      </CardContent>
    </Card>
  );
}

interface TaskItemProps {
  task: Task;
  onToggleComplete: () => void;
  onStart: () => void;
  onDelete: () => void;
}

function TaskItem({
  task,
  onToggleComplete,
  onStart,
  onDelete,
}: TaskItemProps) {
  const isCompleted = task.status === 'DONE';
  const isOverdue =
    task.deadlineMs && task.deadlineMs < Date.now() && !isCompleted;

  const formatDate = (timestamp?: number) => {
    if (!timestamp) return null;
    const date = new Date(timestamp);
    const now = new Date();
    const diffDays = Math.floor(
      (date.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
    );

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Tomorrow';
    if (diffDays === -1) return 'Yesterday';
    if (diffDays < -1) return `${Math.abs(diffDays)} days ago`;
    if (diffDays > 1) return `in ${diffDays} days`;

    return date.toLocaleDateString();
  };

  return (
    <div
      className={cn(
        'group flex items-start gap-3 p-3 rounded-lg border transition-all hover:bg-accent/50',
        isCompleted && 'opacity-60'
      )}
    >
      {/* Checkbox */}
      <button
        onClick={onToggleComplete}
        className='mt-0.5 shrink-0 hover:scale-110 transition-transform'
      >
        {isCompleted ? (
          <CheckCircle2 className='h-5 w-5 text-green-600 dark:text-green-400' />
        ) : (
          <Circle className='h-5 w-5 text-muted-foreground hover:text-primary' />
        )}
      </button>

      {/* Task content */}
      <div className='flex-1 min-w-0'>
        <div className='flex items-start justify-between gap-2'>
          <div className='flex-1 min-w-0'>
            <h4
              className={cn(
                'font-medium text-sm truncate',
                isCompleted && 'line-through text-muted-foreground'
              )}
            >
              {task.title}
            </h4>

            {task.description && (
              <p className='text-xs text-muted-foreground line-clamp-1 mt-0.5'>
                {task.description}
              </p>
            )}
          </div>

          {/* Actions menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant='ghost'
                size='icon'
                className='h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity'
              >
                <MoreVertical className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              {task.status !== 'IN_PROGRESS' && (
                <DropdownMenuItem onClick={onStart}>
                  <PlayCircle className='h-4 w-4 mr-2' />
                  Start Task
                </DropdownMenuItem>
              )}
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={onDelete} className='text-red-600'>
                <Trash2 className='h-4 w-4 mr-2' />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* Metadata */}
        <div className='flex items-center gap-2 mt-2 flex-wrap'>
          <PriorityBadge priority={task.priority} showLabel={false} />
          <StatusBadge status={task.status} />

          {task.estimatedDurationHours > 0 && (
            <span className='text-xs text-muted-foreground flex items-center gap-1'>
              <Clock className='h-3 w-3' />
              {task.estimatedDurationHours}h
            </span>
          )}

          {task.deadlineMs && (
            <span
              className={cn(
                'text-xs',
                isOverdue
                  ? 'text-red-600 dark:text-red-400 font-medium'
                  : 'text-muted-foreground'
              )}
            >
              {isOverdue && '⚠️ '}
              {formatDate(task.deadlineMs)}
            </span>
          )}

          {task.tags.length > 0 && (
            <div className='flex gap-1'>
              {task.tags.slice(0, 2).map((tag) => (
                <span
                  key={tag}
                  className='text-xs px-1.5 py-0.5 rounded bg-muted text-muted-foreground'
                >
                  {tag}
                </span>
              ))}
              {task.tags.length > 2 && (
                <span className='text-xs px-1.5 py-0.5 rounded bg-muted text-muted-foreground'>
                  +{task.tags.length - 2}
                </span>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function RecentTaskSkeleton() {
  return (
    <div className='flex items-start gap-3 p-3 rounded-lg border'>
      <Skeleton className='h-5 w-5 rounded-full mt-0.5 shrink-0' />
      <div className='flex-1 space-y-2'>
        <Skeleton className='h-4 w-3/4' />
        <Skeleton className='h-3 w-1/2' />
        <div className='flex gap-2'>
          <Skeleton className='h-5 w-16' />
          <Skeleton className='h-5 w-20' />
          <Skeleton className='h-5 w-12' />
        </div>
      </div>
    </div>
  );
}
