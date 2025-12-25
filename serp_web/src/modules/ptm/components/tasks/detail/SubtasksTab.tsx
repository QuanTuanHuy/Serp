/**
 * SubtasksTab Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Dedicated tab for managing subtasks with unlimited nesting
 */

'use client';

import { useState } from 'react';
import { Plus, Network } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { SubtaskList } from '../SubtaskList';
import { CreateTaskDialog } from '../dialogs/CreateTaskDialog';
import { useGetTaskTreeQuery } from '../../../api';
import type { Task } from '../../../types';

interface SubtasksTabProps {
  task: Task;
}

export function SubtasksTab({ task }: SubtasksTabProps) {
  const [showCreateDialog, setShowCreateDialog] = useState(false);

  // Fetch full task tree for visualization
  const { data: taskTreeResponse, isLoading: isLoadingTree } =
    useGetTaskTreeQuery(task.id, {
      skip: !task.id,
    });

  const taskTree = taskTreeResponse?.data;
  const subtaskCount = taskTree?.subTasks?.length || 0;
  const totalDescendants = countDescendants(taskTree);

  return (
    <div className='space-y-4'>
      {/* Header with stats */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-3'>
          <Network className='h-5 w-5 text-muted-foreground' />
          <div>
            <h3 className='font-medium'>Subtasks</h3>
            <p className='text-sm text-muted-foreground'>
              {subtaskCount > 0 ? (
                <>
                  {subtaskCount} direct subtask{subtaskCount > 1 ? 's' : ''}
                  {totalDescendants > subtaskCount && (
                    <span className='ml-1'>
                      ({totalDescendants} total including nested)
                    </span>
                  )}
                </>
              ) : (
                'No subtasks yet'
              )}
            </p>
          </div>
        </div>

        <Button onClick={() => setShowCreateDialog(true)} size='sm'>
          <Plus className='h-4 w-4 mr-2' />
          Add Subtask
        </Button>
      </div>

      {/* Info for nested hierarchy */}
      {totalDescendants > 0 && (
        <div className='rounded-lg border bg-muted/50 p-4'>
          <div className='flex items-start gap-3'>
            <Network className='h-5 w-5 text-muted-foreground mt-0.5' />
            <div className='flex-1'>
              <p className='text-sm font-medium'>Nested Hierarchy</p>
              <p className='text-sm text-muted-foreground mt-1'>
                This task has {totalDescendants} total subtask
                {totalDescendants > 1 ? 's' : ''} including nested levels. Drag
                and drop to reorder or change nesting.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Subtask tree with unlimited nesting */}
      <SubtaskList
        parentTaskId={task.id}
        allowNesting={true}
        showFullDetails={true}
        enableDragDrop={true}
        className='mt-6'
      />

      {/* Create subtask dialog */}
      <CreateTaskDialog
        open={showCreateDialog}
        onOpenChange={setShowCreateDialog}
        parentTaskId={task.id}
        projectId={task.projectId}
      />
    </div>
  );
}

// Helper to count all descendants recursively
function countDescendants(task?: Task): number {
  if (!task?.subTasks || task.subTasks.length === 0) return 0;

  let count = task.subTasks.length;
  task.subTasks.forEach((subtask) => {
    count += countDescendants(subtask);
  });

  return count;
}
