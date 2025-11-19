/**
 * PTM v2 - Dependency List Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Display and manage task dependencies
 */

'use client';

import { useState } from 'react';
import { Link, X, AlertCircle, CheckCircle2, Clock } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { Alert, AlertDescription } from '@/shared/components/ui/alert';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';

import {
  useGetTaskDependenciesQuery,
  useGetTasksQuery,
  useRemoveDependencyMutation,
} from '../../services/taskApi';
import { DependencyPicker } from './DependencyPicker';
import { StatusBadge } from '../shared';
import type { Task } from '../../types';

interface DependencyListProps {
  taskId: number;
  className?: string;
}

export function DependencyList({ taskId, className }: DependencyListProps) {
  const [pickerOpen, setPickerOpen] = useState(false);

  const { data: dependencies = [] } = useGetTaskDependenciesQuery(taskId);
  const { data: allTasks = [] } = useGetTasksQuery({});
  const [removeDependency] = useRemoveDependencyMutation();

  const dependentTasks = dependencies
    .map((dep) => allTasks.find((t) => t.id === dep.dependsOnTaskId))
    .filter(Boolean) as Task[];

  const isBlocked = dependentTasks.some((task) => task.status !== 'DONE');

  const handleRemove = async (dependencyId: number) => {
    try {
      await removeDependency({ taskId, dependencyId }).unwrap();
      toast.success('Dependency removed');
    } catch (error) {
      toast.error('Failed to remove dependency');
    }
  };

  return (
    <div className={cn('space-y-4', className)}>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <h3 className='text-sm font-semibold text-muted-foreground'>
          Dependencies ({dependencies.length})
        </h3>
        <Button variant='ghost' size='sm' onClick={() => setPickerOpen(true)}>
          <Link className='h-4 w-4 mr-1' />
          Add Dependency
        </Button>
      </div>

      {/* Blocked Alert */}
      {isBlocked && (
        <Alert variant='destructive'>
          <AlertCircle className='h-4 w-4' />
          <AlertDescription>
            This task is blocked by{' '}
            {dependentTasks.filter((t) => t.status !== 'DONE').length}{' '}
            incomplete task(s)
          </AlertDescription>
        </Alert>
      )}

      {/* Dependency List */}
      {dependencies.length === 0 ? (
        <Card className='p-6 text-center text-muted-foreground'>
          <p className='text-sm'>No dependencies</p>
          <p className='text-xs mt-1'>This task can start anytime</p>
        </Card>
      ) : (
        <div className='space-y-2'>
          {dependencies.map((dependency) => {
            const task = allTasks.find(
              (t) => t.id === dependency.dependsOnTaskId
            );
            if (!task) return null;

            const isComplete = task.status === 'DONE';

            return (
              <Card
                key={dependency.id}
                className={cn('p-3', isComplete && 'opacity-60')}
              >
                <div className='flex items-start gap-3'>
                  {/* Status icon */}
                  <div className='mt-0.5'>
                    {isComplete ? (
                      <CheckCircle2 className='h-5 w-5 text-green-600' />
                    ) : (
                      <Clock className='h-5 w-5 text-orange-600' />
                    )}
                  </div>

                  {/* Task info */}
                  <div className='flex-1 min-w-0'>
                    <h4
                      className={cn(
                        'font-medium text-sm',
                        isComplete && 'line-through text-muted-foreground'
                      )}
                    >
                      {task.title}
                    </h4>

                    <div className='flex items-center gap-2 mt-1 flex-wrap'>
                      <StatusBadge status={task.status} />
                      <Badge variant='outline' className='text-xs'>
                        {dependency.dependencyType.replace(/_/g, ' → ')}
                      </Badge>

                      {task.deadlineMs && (
                        <span className='text-xs text-muted-foreground'>
                          Due: {new Date(task.deadlineMs).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Remove button */}
                  <Button
                    variant='ghost'
                    size='icon'
                    className='h-7 w-7 shrink-0'
                    onClick={() => handleRemove(dependency.id)}
                  >
                    <X className='h-4 w-4' />
                  </Button>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Picker Dialog */}
      <DependencyPicker
        taskId={taskId}
        currentDependencies={dependencies.map((d) => d.dependsOnTaskId)}
        open={pickerOpen}
        onOpenChange={setPickerOpen}
      />
    </div>
  );
}
