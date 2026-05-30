/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item subtask actions
 */

'use client';

import { useState } from 'react';
import { PlusSquare } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { CreateWorkItemDialog } from '../CreateWorkItemDialog';

interface PMWorkItemSubtaskActionsProps {
  projectId: number;
  workItemId?: number;
}

export function PMWorkItemSubtaskActions({
  projectId,
  workItemId,
}: PMWorkItemSubtaskActionsProps) {
  const [open, setOpen] = useState(false);

  if (!workItemId) {
    return null;
  }

  return (
    <>
      <Button
        type='button'
        variant='outline'
        size='sm'
        className='gap-2'
        onClick={() => setOpen(true)}
      >
        <PlusSquare className='h-4 w-4' />
        Add subtask
      </Button>

      <CreateWorkItemDialog
        open={open}
        onOpenChange={setOpen}
        initialProjectId={projectId}
        initialParentId={workItemId}
        lockProject
        lockParent
      />
    </>
  );
}
