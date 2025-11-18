/**
 * PTM v2 - Subtask List Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Hierarchical subtask management
 */

'use client';

import { useState } from 'react';
import {
  Plus,
  ChevronRight,
  ChevronDown,
  CheckCircle2,
  Circle,
  MoreVertical,
  Trash2,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Checkbox } from '@/shared/components/ui/checkbox';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { Progress } from '@/shared/components/ui/progress';
import { cn } from '@/shared/utils';
import {
  useGetTasksQuery,
  useCreateTaskMutation,
  useUpdateTaskMutation,
  useDeleteTaskMutation,
} from '../../services/taskApi';
import type { Task } from '../../types';
import { toast } from 'sonner';

interface SubtaskListProps {
  parentTaskId: string;
  className?: string;
}

export function SubtaskList({ parentTaskId, className }: SubtaskListProps) {
  const [isAdding, setIsAdding] = useState(false);
  const [newSubtaskTitle, setNewSubtaskTitle] = useState('');
  const [isExpanded, setIsExpanded] = useState(true);

  const { data: allTasks = [] } = useGetTasksQuery({});
  const [createTask] = useCreateTaskMutation();
  const [updateTask] = useUpdateTaskMutation();
  const [deleteTask] = useDeleteTaskMutation();

  // Filter subtasks
  const subtasks = allTasks.filter(
    (task) => task.parentTaskId === parentTaskId
  );
  const completedSubtasks = subtasks.filter(
    (task) => task.status === 'DONE'
  ).length;
  const totalSubtasks = subtasks.length;
  const completionRate =
    totalSubtasks > 0 ? (completedSubtasks / totalSubtasks) * 100 : 0;

  const handleAddSubtask = async () => {
    if (!newSubtaskTitle.trim()) return;

    try {
      await createTask({
        title: newSubtaskTitle,
        parentTaskId,
      }).unwrap();

      toast.success('Subtask added');
      setNewSubtaskTitle('');
      setIsAdding(false);
    } catch (error) {
      toast.error('Failed to add subtask');
    }
  };

  const handleToggleSubtask = async (subtask: Task) => {
    try {
      await updateTask({
        id: subtask.id,
        status: subtask.status === 'DONE' ? 'TODO' : 'DONE',
        progressPercentage: subtask.status === 'DONE' ? 0 : 100,
      }).unwrap();
    } catch (error) {
      toast.error('Failed to update subtask');
    }
  };

  const handleDeleteSubtask = async (subtaskId: string) => {
    if (!confirm('Are you sure you want to delete this subtask?')) return;

    try {
      await deleteTask(subtaskId).unwrap();
      toast.success('Subtask deleted');
    } catch (error) {
      toast.error('Failed to delete subtask');
    }
  };

  return (
    <div className={cn('space-y-3', className)}>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className='flex items-center gap-2 text-sm font-medium hover:text-primary transition-colors'
        >
          {isExpanded ? (
            <ChevronDown className='h-4 w-4' />
          ) : (
            <ChevronRight className='h-4 w-4' />
          )}
          <span>
            Subtasks ({completedSubtasks}/{totalSubtasks})
          </span>
        </button>
        <Button variant='ghost' size='sm' onClick={() => setIsAdding(true)}>
          <Plus className='h-4 w-4 mr-1' />
          Add
        </Button>
      </div>

      {/* Progress Bar */}
      {totalSubtasks > 0 && (
        <div className='space-y-1'>
          <Progress value={completionRate} className='h-2' />
          <p className='text-xs text-muted-foreground text-right'>
            {Math.round(completionRate)}% complete
          </p>
        </div>
      )}

      {/* Subtasks List */}
      {isExpanded && (
        <div className='space-y-2'>
          {/* Add New Subtask Input */}
          {isAdding && (
            <div className='flex items-center gap-2 p-2 border rounded-lg bg-muted/50'>
              <Input
                value={newSubtaskTitle}
                onChange={(e) => setNewSubtaskTitle(e.target.value)}
                placeholder='Subtask title...'
                className='h-8 text-sm'
                autoFocus
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleAddSubtask();
                  if (e.key === 'Escape') {
                    setIsAdding(false);
                    setNewSubtaskTitle('');
                  }
                }}
              />
              <Button size='sm' onClick={handleAddSubtask}>
                Add
              </Button>
              <Button
                size='sm'
                variant='ghost'
                onClick={() => {
                  setIsAdding(false);
                  setNewSubtaskTitle('');
                }}
              >
                Cancel
              </Button>
            </div>
          )}

          {/* Subtask Items */}
          {subtasks.length === 0 ? (
            <div className='text-center py-8 text-sm text-muted-foreground'>
              No subtasks yet. Click "Add" to create one.
            </div>
          ) : (
            <div className='space-y-1'>
              {subtasks.map((subtask) => (
                <div
                  key={subtask.id}
                  className='flex items-center gap-2 p-2 rounded-lg hover:bg-muted/50 transition-colors group'
                >
                  <Checkbox
                    checked={subtask.status === 'DONE'}
                    onCheckedChange={() => handleToggleSubtask(subtask)}
                  />
                  <span
                    className={cn(
                      'flex-1 text-sm',
                      subtask.status === 'DONE' &&
                        'line-through text-muted-foreground'
                    )}
                  >
                    {subtask.title}
                  </span>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button
                        variant='ghost'
                        size='icon'
                        className='h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity'
                      >
                        <MoreVertical className='h-3.5 w-3.5' />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align='end'>
                      <DropdownMenuItem
                        onClick={() => handleDeleteSubtask(subtask.id)}
                        className='text-red-600'
                      >
                        <Trash2 className='mr-2 h-4 w-4' />
                        Delete
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
