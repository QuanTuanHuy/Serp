/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Delete Department Confirmation Dialog
 */

'use client';

import React from 'react';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import type { Department } from '@/modules/settings';

interface DeleteDepartmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  department: Department | null;
  onConfirm: (departmentId: number) => Promise<void>;
  isLoading?: boolean;
}

export const DeleteDepartmentDialog: React.FC<DeleteDepartmentDialogProps> = ({
  open,
  onOpenChange,
  department,
  onConfirm,
  isLoading,
}) => {
  const handleConfirm = async () => {
    if (!department) return;
    await onConfirm(department.id);
    onOpenChange(false);
  };

  const memberWarning =
    department?.memberCount && department.memberCount > 0 ? (
      <div className='mt-3 p-3 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-md'>
        <p className='text-sm text-amber-800 dark:text-amber-300'>
          ⚠️ This department has {department.memberCount} member
          {department.memberCount > 1 ? 's' : ''}. They will be removed from
          this department.
        </p>
      </div>
    ) : null;

  return (
    <ConfirmDialog
      open={open}
      onOpenChange={onOpenChange}
      title='Delete Department'
      description={
        <div className='space-y-3'>
          <p>
            Are you sure you want to delete{' '}
            <span className='font-semibold text-foreground'>
              {department?.name}
            </span>
            ?
          </p>
          <div className='space-y-2'>
            <p className='text-sm text-muted-foreground'>
              This action cannot be undone. This will:
            </p>
            <ul className='list-disc list-inside space-y-1 text-sm text-muted-foreground ml-2'>
              <li>Remove all department members</li>
              <li>Remove department hierarchy relationships</li>
              <li>Delete all department data permanently</li>
            </ul>
          </div>
          {memberWarning}
        </div>
      }
      confirmText='Delete Department'
      cancelText='Cancel'
      onConfirm={handleConfirm}
      isLoading={isLoading}
      variant='destructive'
    />
  );
};
