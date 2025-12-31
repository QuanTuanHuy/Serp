/**
 * TaskDetailHeader Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Header for task detail page with actions
 */

'use client';

import { ArrowLeft, Edit, Trash2 } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import type { Task } from '../../../types';

interface TaskDetailHeaderProps {
  task: Task;
  onEdit: () => void;
  onDelete: () => void;
  onBack: () => void;
}

export function TaskDetailHeader({
  task,
  onEdit,
  onDelete,
  onBack,
}: TaskDetailHeaderProps) {
  return (
    <div className='space-y-4'>
      <div className='flex flex-col md:flex-row md:items-start md:justify-between gap-4'>
        <div className='flex-1 space-y-3'>
          <h1 className='text-3xl font-bold'>{task.title}</h1>
          {task.description && (
            <p className='text-muted-foreground text-lg'>{task.description}</p>
          )}
        </div>
        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm' onClick={onBack}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Back
          </Button>
          <Button variant='default' size='sm' onClick={onEdit}>
            <Edit className='h-4 w-4 mr-2' />
            Edit
          </Button>
          <Button variant='destructive' size='sm' onClick={onDelete}>
            <Trash2 className='h-4 w-4 mr-2' />
            Delete
          </Button>
        </div>
      </div>
    </div>
  );
}
