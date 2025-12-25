/**
 * PTM v2 - Task Card Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task card for list display (Refactored)
 */

'use client';

import {
  Clock,
  Calendar,
  Tag,
  MoreVertical,
  Play,
  Pause,
  Check,
  ListTodo,
  Brain,
  ExternalLink,
  Link as LinkIcon,
  AlertCircle,
  Edit,
  Trash2,
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import { Card } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { Checkbox } from '@/shared/components/ui/checkbox';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/shared/components/ui/dropdown-menu';
import { Button } from '@/shared/components/ui/button';
import { Progress } from '@/shared/components/ui/progress';
import { cn } from '@/shared/utils';
import { StatusBadge } from '../shared/StatusBadge';
import { PriorityBadge } from '../shared/PriorityBadge';
import { RecurringBadge } from './RecurringBadge';
import { useGetTasksQuery } from '../../api';
import type { Task, TaskStatus } from '../../types';

interface TaskCardProps {
  task: Task;
  onClick?: (taskId: number) => void;
  onNavigate?: boolean;
  onToggleComplete?: (taskId: number, currentStatus: TaskStatus) => void;
  onStart?: (taskId: number) => void;
  onPause?: (taskId: number) => void;
  onEdit?: (taskId: number) => void;
  onDelete?: (task: Task) => void;
  className?: string;
}

export function TaskCard({
  task,
  onClick,
  onNavigate = false,
  onToggleComplete,
  onStart,
  onPause,
  onEdit,
  onDelete,
  className,
}: TaskCardProps) {
  const router = useRouter();

  // Fetch all tasks to calculate subtasks count
  const { data: paginatedTasks } = useGetTasksQuery({});
  const allTasks = paginatedTasks?.data?.items || [];

  const subtasks = allTasks.filter((t) => t.parentTaskId === task.id);
  const completedSubtasks = subtasks.filter((t) => t.status === 'DONE').length;
  const totalSubtasks = subtasks.length;

  const isOverdue =
    task.status !== 'DONE' && task.deadlineMs && task.deadlineMs < Date.now();

  const handleToggleComplete = () => {
    onToggleComplete?.(task.id, task.status);
  };

  const handleStart = () => {
    onStart?.(task.id);
  };

  const handlePause = () => {
    onPause?.(task.id);
  };

  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit?.(task.id);
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete?.(task);
  };

  const handleCardClick = () => {
    if (onNavigate) {
      router.push(`/ptm/tasks/${task.id}`);
    } else {
      onClick?.(task.id);
    }
  };

  return (
    <Card
      className={cn(
        'group relative p-4 cursor-pointer transition-all',
        'hover:shadow-lg hover:scale-[1.01] hover:border-primary/50',
        'active:scale-[0.99]',
        task.status === 'DONE' && 'opacity-60',
        isOverdue &&
          'border-red-300 dark:border-red-800 shadow-red-100 dark:shadow-red-900/20',
        task.isDeepWork && 'border-l-4 border-l-purple-500',
        className
      )}
      onClick={handleCardClick}
    >
      <div className='flex items-start gap-3'>
        {/* Checkbox */}
        <div onClick={(e) => e.stopPropagation()}>
          <Checkbox
            checked={task.status === 'DONE'}
            onCheckedChange={handleToggleComplete}
            className='mt-0.5'
          />
        </div>

        {/* Content */}
        <div className='flex-1 min-w-0 space-y-2'>
          {/* Title & Priority */}
          <div className='flex items-start justify-between gap-2'>
            <h3
              className={cn(
                'font-medium text-sm leading-tight',
                task.status === 'DONE' && 'line-through text-muted-foreground'
              )}
            >
              {task.title}
            </h3>
            <PriorityBadge priority={task.priority} />
          </div>

          {/* Description */}
          {task.description && (
            <p className='text-xs text-muted-foreground line-clamp-2'>
              {task.description}
            </p>
          )}

          {/* Meta Info */}
          <div className='flex flex-wrap items-center gap-3 text-xs text-muted-foreground'>
            {/* Duration */}
            {task.estimatedDurationHours > 0 && (
              <div className='flex items-center gap-1'>
                <Clock className='h-3 w-3' />
                <span>{task.estimatedDurationHours}h</span>
              </div>
            )}

            {/* Deadline */}
            {task.deadlineMs && (
              <div
                className={cn(
                  'flex items-center gap-1',
                  isOverdue && 'text-red-600 dark:text-red-400 font-medium'
                )}
              >
                <Calendar className='h-3 w-3' />
                <span>
                  {isOverdue && '⚠️ '}
                  {new Date(task.deadlineMs).toLocaleDateString()}
                </span>
              </div>
            )}

            {/* Status */}
            <StatusBadge status={task.status} />

            {/* Recurring Badge */}
            {task.repeatConfig && (
              <RecurringBadge repeatConfig={task.repeatConfig} />
            )}

            {/* Deep Work Indicator */}
            {task.isDeepWork && (
              <div className='flex items-center gap-1 text-purple-600 dark:text-purple-400'>
                <Brain className='h-3 w-3' />
                <span className='font-medium'>Deep Work</span>
              </div>
            )}

            {/* Subtasks Indicator */}
            {totalSubtasks > 0 && (
              <div className='flex items-center gap-1'>
                <ListTodo className='h-3 w-3' />
                <span>
                  {completedSubtasks}/{totalSubtasks}
                </span>
              </div>
            )}

            {/* Dependency Indicators */}
            {task.isBlocked && (
              <Badge variant='destructive' className='text-xs'>
                <AlertCircle className='h-3 w-3 mr-1' />
                Blocked
              </Badge>
            )}

            {task.dependentTaskIds && task.dependentTaskIds.length > 0 && (
              <Badge variant='outline' className='text-xs'>
                <LinkIcon className='h-3 w-3 mr-1' />
                Depends on {task.dependentTaskIds.length}
              </Badge>
            )}

            {task.blockingTasksCount && task.blockingTasksCount > 0 && (
              <Badge variant='secondary' className='text-xs'>
                Blocking {task.blockingTasksCount}
              </Badge>
            )}
          </div>

          {/* Progress Bar */}
          {task.progressPercentage > 0 && task.status !== 'DONE' && (
            <div className='space-y-1'>
              <div className='flex items-center justify-between text-xs'>
                <span className='text-muted-foreground'>Progress</span>
                <span className='font-medium'>{task.progressPercentage}%</span>
              </div>
              <Progress value={task.progressPercentage} className='h-1.5' />
            </div>
          )}

          {/* Tags */}
          {task.tags && task.tags.length > 0 && (
            <div className='flex flex-wrap items-center gap-1'>
              <Tag className='h-3 w-3 text-muted-foreground' />
              {task.tags.slice(0, 3).map((tag, index) => (
                <span
                  key={index}
                  className='px-1.5 py-0.5 bg-muted rounded text-xs text-muted-foreground'
                >
                  {tag}
                </span>
              ))}
              {task.tags.length > 3 && (
                <span className='text-xs text-muted-foreground'>
                  +{task.tags.length - 3} more
                </span>
              )}
            </div>
          )}
        </div>

        {/* Actions Dropdown */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8 flex-shrink-0'
            >
              <MoreVertical className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuItem
              onClick={(e) => {
                e.stopPropagation();
                router.push(`/ptm/tasks/${task.id}`);
              }}
            >
              <ExternalLink className='mr-2 h-4 w-4' />
              Open Detail
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            {task.status === 'TODO' && onStart && (
              <DropdownMenuItem onClick={handleStart}>
                <Play className='mr-2 h-4 w-4' />
                Start
              </DropdownMenuItem>
            )}
            {task.status === 'IN_PROGRESS' && onPause && (
              <DropdownMenuItem onClick={handlePause}>
                <Pause className='mr-2 h-4 w-4' />
                Pause
              </DropdownMenuItem>
            )}
            {task.status !== 'DONE' && onToggleComplete && (
              <DropdownMenuItem onClick={handleToggleComplete}>
                <Check className='mr-2 h-4 w-4' />
                Mark Complete
              </DropdownMenuItem>
            )}
            {onEdit && (
              <DropdownMenuItem onClick={handleEdit}>
                <Edit className='mr-2 h-4 w-4' />
                Edit
              </DropdownMenuItem>
            )}
            {(onStart || onPause || onToggleComplete || onEdit) && onDelete && (
              <DropdownMenuSeparator />
            )}
            {onDelete && (
              <DropdownMenuItem onClick={handleDelete} className='text-red-600'>
                <Trash2 className='mr-2 h-4 w-4' />
                Delete
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </Card>
  );
}
