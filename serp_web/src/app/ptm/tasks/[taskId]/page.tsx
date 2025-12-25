/*
Author: QuanTuanHuy
Description: Part of Serp Project - Task Detail Page (Refactored)
*/

'use client';

import { useSearchParams } from 'next/navigation';
import { use, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useGetTaskQuery } from '@/modules/ptm/api';
import { useTaskDialogs } from '@/modules/ptm/hooks';
import { TaskDetailHeader } from '@/modules/ptm/components/tasks/detail/TaskDetailHeader';
import { TaskDetailTabs } from '@/modules/ptm/components/tasks/detail/TaskDetailTabs';
import { EditTaskDialog } from '@/modules/ptm/components/tasks/dialogs/EditTaskDialog';
import { DeleteTaskDialog } from '@/modules/ptm/components/tasks/dialogs/DeleteTaskDialog';
import { LoadingSkeleton } from '@/modules/ptm/components/tasks/detail/LoadingSkeleton';

export default function TaskDetailPage({
  params,
}: {
  params: Promise<{ taskId: string }>;
}) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const unwrappedParams = use(params);
  const taskId = parseInt(unwrappedParams.taskId, 10);

  const { data: task, isLoading } = useGetTaskQuery(taskId);
  const { editDialog, deleteDialog } = useTaskDialogs();

  // URL params for navigation
  const editParam = searchParams.get('editTask');

  // Auto-open edit dialog from URL param
  useEffect(() => {
    if (editParam === taskId.toString()) {
      editDialog.openEdit(taskId);
    }
  }, [editParam, taskId, editDialog]);

  if (isLoading) {
    return <LoadingSkeleton />;
  }

  if (!task) {
    return (
      <div className='container mx-auto py-6 text-center'>
        <p className='text-muted-foreground'>Task not found</p>
      </div>
    );
  }

  return (
    <div className='container mx-auto py-6 space-y-6'>
      <TaskDetailHeader
        task={task}
        onEdit={() => editDialog.openEdit(taskId)}
        onDelete={() => deleteDialog.openDelete(taskId, task.title)}
        onBack={() => router.push('/ptm/tasks')}
      />

      <TaskDetailTabs taskId={taskId} task={task} />

      {/* Dialogs */}
      <EditTaskDialog
        taskId={editDialog.taskId}
        open={editDialog.open}
        onOpenChange={editDialog.onOpenChange}
      />
      <DeleteTaskDialog
        taskId={deleteDialog.taskId}
        taskTitle={deleteDialog.taskTitle}
        open={deleteDialog.open}
        onOpenChange={deleteDialog.onOpenChange}
      />
    </div>
  );
}
