/**
 * TaskDetailContent Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task detail content with meta information
 */

'use client';

import { useRouter } from 'next/navigation';
import {
  Calendar,
  Clock,
  Tag,
  FolderOpen,
  Zap,
  Repeat,
  Activity,
  AlertCircle,
} from 'lucide-react';
import { Card } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Separator } from '@/shared/components/ui/separator';
import { RecurringBadge } from '../RecurringBadge';
import type { Task } from '../../../types';
import {
  formatDate,
  formatDateTime,
  formatDuration,
  getRecurrenceDisplay,
} from '../../../utils';

interface TaskDetailContentProps {
  task: Task;
}

export function TaskDetailContent({ task }: TaskDetailContentProps) {
  const router = useRouter();

  // Parse and format recurrence config
  const recurrenceInfo = getRecurrenceDisplay(task);

  return (
    <div className='space-y-6'>
      {/* Description Section */}
      {task.description && (
        <div className='space-y-3'>
          <h3 className='text-sm font-semibold text-muted-foreground uppercase tracking-wide'>
            Description
          </h3>
          <Card className='p-4'>
            <p className='text-sm whitespace-pre-wrap'>{task.description}</p>
          </Card>
        </div>
      )}

      {/* Task Meta Grid */}
      <div>
        <h3 className='text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-3'>
          Task Details
        </h3>
        <div className='grid grid-cols-2 md:grid-cols-4 gap-4'>
          <Card className='p-4'>
            <div className='space-y-2'>
              <p className='text-xs text-muted-foreground font-medium'>
                Status
              </p>
              <Badge variant='outline' className='text-sm'>
                {task.status.replace('_', ' ')}
              </Badge>
            </div>
          </Card>

          <Card className='p-4'>
            <div className='space-y-2'>
              <p className='text-xs text-muted-foreground font-medium'>
                Priority
              </p>
              <Badge
                className='text-sm'
                variant={
                  task.priority === 'HIGH'
                    ? 'destructive'
                    : task.priority === 'MEDIUM'
                      ? 'default'
                      : 'secondary'
                }
              >
                {task.priority}
              </Badge>
            </div>
          </Card>

          <Card className='p-4'>
            <div className='space-y-2'>
              <p className='text-xs text-muted-foreground font-medium'>
                Estimated
              </p>
              <div className='flex items-center gap-2'>
                <Clock className='h-4 w-4 text-muted-foreground' />
                <p className='text-sm font-semibold'>
                  {formatDuration(task.estimatedDurationMin)}
                </p>
              </div>
            </div>
          </Card>

          {task.actualDurationMin !== undefined && (
            <Card className='p-4'>
              <div className='space-y-2'>
                <p className='text-xs text-muted-foreground font-medium'>
                  Actual Time
                </p>
                <div className='flex items-center gap-2'>
                  <Activity className='h-4 w-4 text-muted-foreground' />
                  <p className='text-sm font-semibold'>
                    {formatDuration(task.actualDurationMin)}
                  </p>
                </div>
              </div>
            </Card>
          )}

          {task.deadlineMs && (
            <Card className='p-4'>
              <div className='space-y-2'>
                <p className='text-xs text-muted-foreground font-medium'>
                  Deadline
                </p>
                <div className='flex items-center gap-2'>
                  <Calendar className='h-4 w-4 text-muted-foreground' />
                  <p className='text-sm font-semibold'>
                    {formatDate(task.deadlineMs)}
                  </p>
                </div>
              </div>
            </Card>
          )}

          {task.preferredStartDateMs && (
            <Card className='p-4'>
              <div className='space-y-2'>
                <p className='text-xs text-muted-foreground font-medium'>
                  Start Date
                </p>
                <div className='flex items-center gap-2'>
                  <Calendar className='h-4 w-4 text-muted-foreground' />
                  <p className='text-sm font-semibold'>
                    {formatDate(task.preferredStartDateMs)}
                  </p>
                </div>
              </div>
            </Card>
          )}

          {task.isDeepWork && (
            <Card className='p-4 border-primary/50 bg-primary/5'>
              <div className='space-y-2'>
                <p className='text-xs text-muted-foreground font-medium'>
                  Deep Work
                </p>
                <div className='flex items-center gap-2'>
                  <Zap className='h-4 w-4 text-primary' />
                  <p className='text-sm font-semibold text-primary'>
                    Focus Required
                  </p>
                </div>
              </div>
            </Card>
          )}
        </div>
      </div>

      <Separator />

      {/* Additional Metadata */}
      <div className='space-y-4'>
        {/* Category */}
        {task.category && (
          <div className='flex items-start gap-3'>
            <FolderOpen className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium text-muted-foreground mb-1'>
                Category
              </p>
              <Badge variant='outline'>{task.category}</Badge>
            </div>
          </div>
        )}

        {/* Tags */}
        {task.tags && task.tags.length > 0 && (
          <div className='flex items-start gap-3'>
            <Tag className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium text-muted-foreground mb-2'>
                Tags
              </p>
              <div className='flex flex-wrap gap-2'>
                {task.tags.map((tag, index) => (
                  <Badge key={index} variant='secondary'>
                    {tag}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Recurring Config */}
        {task.isRecurring && recurrenceInfo && (
          <div className='flex items-start gap-3'>
            <Repeat className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium text-muted-foreground mb-2'>
                Recurring Task
              </p>
              <Badge variant='outline' className='mb-2'>
                {recurrenceInfo.displayText}
              </Badge>
              {recurrenceInfo.endDate && (
                <p className='text-xs text-muted-foreground mt-2'>
                  Until {new Date(recurrenceInfo.endDate).toLocaleDateString()}
                </p>
              )}
              {/* Raw config for debugging */}
              {task.recurrenceConfig && (
                <details className='mt-2'>
                  <summary className='text-xs text-muted-foreground cursor-pointer'>
                    View raw config
                  </summary>
                  <p className='font-mono text-xs bg-muted p-2 rounded mt-1'>
                    {task.recurrenceConfig}
                  </p>
                </details>
              )}
            </div>
          </div>
        )}

        {/* Dependency Info */}
        {task.dependentTaskIds && task.dependentTaskIds.length > 0 && (
          <div className='flex items-start gap-3'>
            <AlertCircle className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium text-muted-foreground mb-1'>
                Dependencies
              </p>
              <p className='text-sm text-muted-foreground'>
                This task depends on {task.dependentTaskIds.length} other
                task(s)
              </p>
            </div>
          </div>
        )}

        {/* Subtasks Info */}
        {task.hasSubtasks && (
          <div className='flex items-start gap-3'>
            <FolderOpen className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium text-muted-foreground mb-1'>
                Subtasks
              </p>
              <p className='text-sm text-muted-foreground'>
                {task.completedSubtaskCount || 0} of{' '}
                {task.totalSubtaskCount || 0} completed
              </p>
            </div>
          </div>
        )}

        {/* Project Link */}
        {task.projectId && (
          <div className='flex items-start gap-3'>
            <FolderOpen className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium text-muted-foreground mb-1'>
                Project
              </p>
              <Button
                variant='link'
                className='h-auto p-0'
                onClick={() => router.push(`/ptm/projects/${task.projectId}`)}
              >
                View Project →
              </Button>
            </div>
          </div>
        )}

        {/* Parent Task Link */}
        {task.parentTaskId && (
          <div className='flex items-start gap-3'>
            <AlertCircle className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium text-muted-foreground mb-1'>
                Parent Task
              </p>
              <Button
                variant='link'
                className='h-auto p-0'
                onClick={() => router.push(`/ptm/tasks/${task.parentTaskId}`)}
              >
                View Parent Task →
              </Button>
            </div>
          </div>
        )}
      </div>

      <Separator />

      {/* Timestamps */}
      <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
        <div>
          <p className='text-xs text-muted-foreground font-medium mb-1'>
            Created At
          </p>
          <p className='text-sm'>{formatDateTime(task.createdAt)}</p>
        </div>
        <div>
          <p className='text-xs text-muted-foreground font-medium mb-1'>
            Last Updated
          </p>
          <p className='text-sm'>{formatDateTime(task.updatedAt)}</p>
        </div>
        {task.completedAt && (
          <div>
            <p className='text-xs text-muted-foreground font-medium mb-1'>
              Completed At
            </p>
            <p className='text-sm'>{formatDateTime(task.completedAt)}</p>
          </div>
        )}
        {task.source && (
          <div>
            <p className='text-xs text-muted-foreground font-medium mb-1'>
              Source
            </p>
            <Badge variant='outline' className='text-xs'>
              {task.source.replace('_', ' ').toUpperCase()}
            </Badge>
          </div>
        )}
      </div>
    </div>
  );
}
