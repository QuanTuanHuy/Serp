/**
 * PTM v2 - Dependency Picker Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Dialog for selecting task dependencies
 */

'use client';

import { useState, useMemo } from 'react';
import { Search, Link, AlertCircle, Check } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Alert, AlertDescription } from '@/shared/components/ui/alert';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';

import {
  useGetTasksQuery,
  useAddDependencyMutation,
  useValidateDependencyMutation,
} from '../../services/taskApi';
import { StatusBadge, PriorityBadge } from '../shared';
import type { Task } from '../../types';

interface DependencyPickerProps {
  taskId: number;
  currentDependencies: number[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function DependencyPicker({
  taskId,
  currentDependencies,
  open,
  onOpenChange,
}: DependencyPickerProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTaskId, setSelectedTaskId] = useState<number | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const { data: paginatedTasks } = useGetTasksQuery({});
  const allTasks = paginatedTasks?.data?.items || [];
  const [addDependency, { isLoading: isAdding }] = useAddDependencyMutation();
  const [validateDependency] = useValidateDependencyMutation();

  // Filter out: current task, already dependent tasks, and subtasks
  const availableTasks = useMemo(() => {
    return allTasks.filter(
      (task) =>
        task.id !== taskId &&
        !currentDependencies.includes(task.id) &&
        task.parentTaskId !== taskId // Can't depend on own subtask
    );
  }, [allTasks, taskId, currentDependencies]);

  // Search filter
  const filteredTasks = useMemo(() => {
    if (!searchQuery) return availableTasks;

    const query = searchQuery.toLowerCase();
    return availableTasks.filter(
      (task) =>
        task.title.toLowerCase().includes(query) ||
        task.description?.toLowerCase().includes(query) ||
        task.tags.some((tag) => tag.toLowerCase().includes(query))
    );
  }, [availableTasks, searchQuery]);

  const handleSelectTask = async (task: Task) => {
    setSelectedTaskId(task.id);
    setValidationError(null);

    // Validate dependency
    try {
      const result = await validateDependency({
        taskId,
        dependsOnTaskId: task.id,
      }).unwrap();

      if (!result.isValid) {
        setValidationError(
          result.wouldCreateCycle
            ? '⚠️ This would create a circular dependency!'
            : result.errors.join(', ')
        );
      }
    } catch (error) {
      setValidationError('Failed to validate dependency');
    }
  };

  const handleAddDependency = async () => {
    if (!selectedTaskId) return;

    try {
      await addDependency({
        taskId,
        dependsOnTaskId: selectedTaskId,
        dependencyType: 'FINISH_TO_START',
      }).unwrap();

      toast.success('Dependency added');
      onOpenChange(false);
      setSelectedTaskId(null);
      setSearchQuery('');
      setValidationError(null);
    } catch (error) {
      toast.error('Failed to add dependency');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl max-h-[80vh]'>
        <DialogHeader>
          <DialogTitle>Add Task Dependency</DialogTitle>
        </DialogHeader>

        {/* Search */}
        <div className='relative'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder='Search tasks...'
            className='pl-9'
          />
        </div>

        {/* Validation Error */}
        {validationError && (
          <Alert variant='destructive'>
            <AlertCircle className='h-4 w-4' />
            <AlertDescription>{validationError}</AlertDescription>
          </Alert>
        )}

        {/* Task List */}
        <div className='space-y-2 max-h-96 overflow-y-auto'>
          {filteredTasks.length === 0 ? (
            <div className='text-center py-8 text-muted-foreground'>
              <p>No available tasks found</p>
            </div>
          ) : (
            filteredTasks.map((task) => (
              <button
                key={task.id}
                onClick={() => handleSelectTask(task)}
                className={cn(
                  'w-full p-3 rounded-lg border text-left transition-colors hover:bg-accent',
                  selectedTaskId === task.id && 'border-primary bg-accent'
                )}
              >
                <div className='flex items-start gap-3'>
                  {/* Selection indicator */}
                  <div
                    className={cn(
                      'w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 mt-0.5',
                      selectedTaskId === task.id
                        ? 'border-primary bg-primary'
                        : 'border-muted-foreground'
                    )}
                  >
                    {selectedTaskId === task.id && (
                      <Check className='h-3 w-3 text-primary-foreground' />
                    )}
                  </div>

                  {/* Task info */}
                  <div className='flex-1 min-w-0'>
                    <h4 className='font-medium text-sm truncate'>
                      {task.title}
                    </h4>

                    {task.description && (
                      <p className='text-xs text-muted-foreground line-clamp-1 mt-1'>
                        {task.description}
                      </p>
                    )}

                    <div className='flex items-center gap-2 mt-2 flex-wrap'>
                      <StatusBadge status={task.status} />
                      <PriorityBadge
                        priority={task.priority}
                        showLabel={false}
                      />

                      {task.deadlineMs && (
                        <span className='text-xs text-muted-foreground'>
                          Due: {new Date(task.deadlineMs).toLocaleDateString()}
                        </span>
                      )}

                      {task.isBlocked && (
                        <Badge variant='destructive' className='text-xs'>
                          Blocked
                        </Badge>
                      )}
                    </div>
                  </div>
                </div>
              </button>
            ))
          )}
        </div>

        {/* Actions */}
        <div className='flex justify-end gap-2 pt-4 border-t'>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleAddDependency}
            disabled={!selectedTaskId || !!validationError || isAdding}
          >
            <Link className='h-4 w-4 mr-2' />
            Add Dependency
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
