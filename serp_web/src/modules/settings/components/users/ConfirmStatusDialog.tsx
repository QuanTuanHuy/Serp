'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Confirm status change dialog
 */

import React from 'react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import type { UserStatus } from '@/modules/admin/types';

const statusConfig: Record<
  string,
  { title: string; description: string; action: string; destructive?: boolean }
> = {
  ACTIVE: {
    title: 'Activate User',
    description:
      'This will restore access for this user. They will be able to log in and use the system.',
    action: 'Activate',
  },
  INACTIVE: {
    title: 'Deactivate User',
    description:
      'This will remove access for this user. They will not be able to log in until reactivated.',
    action: 'Deactivate',
    destructive: true,
  },
  SUSPENDED: {
    title: 'Suspend User',
    description:
      'This will temporarily suspend this user. They will not be able to log in until the suspension is lifted.',
    action: 'Suspend',
    destructive: true,
  },
};

export interface ConfirmStatusDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  userName: string;
  targetStatus: UserStatus | string;
  onConfirm: () => void | Promise<void>;
}

export function ConfirmStatusDialog({
  open,
  onOpenChange,
  userName,
  targetStatus,
  onConfirm,
}: ConfirmStatusDialogProps) {
  const config = statusConfig[targetStatus] || {
    title: `Change Status to ${targetStatus}`,
    description: `Are you sure you want to change the status of this user to ${targetStatus}?`,
    action: 'Confirm',
  };

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{config.title}</AlertDialogTitle>
          <AlertDialogDescription>
            {config.description}
            <br />
            <span className='font-medium text-foreground mt-2 inline-block'>
              User: {userName}
            </span>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancel</AlertDialogCancel>
          <AlertDialogAction
            onClick={onConfirm}
            className={
              config.destructive
                ? 'bg-destructive text-destructive-foreground hover:bg-destructive/90'
                : ''
            }
          >
            {config.action}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

export default ConfirmStatusDialog;
