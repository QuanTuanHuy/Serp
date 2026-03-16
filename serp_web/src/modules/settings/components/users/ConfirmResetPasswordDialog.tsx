/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Confirm reset password dialog
 */

'use client';

import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import { Button } from '@/shared/components/ui/button';

export interface ConfirmResetPasswordDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  userName: string;
  userEmail: string;
  onConfirm: () => void | Promise<void>;
  isSubmitting?: boolean;
}

export function ConfirmResetPasswordDialog({
  open,
  onOpenChange,
  userName,
  userEmail,
  onConfirm,
  isSubmitting = false,
}: ConfirmResetPasswordDialogProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Send password reset email?</AlertDialogTitle>
          <AlertDialogDescription>
            SERP will generate a secure reset link and send it to this user so
            they can choose a new password.
            <br />
            <span className='mt-2 inline-block font-medium text-foreground'>
              User: {userName}
            </span>
            <br />
            <span className='inline-block text-foreground/80'>{userEmail}</span>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isSubmitting}>Cancel</AlertDialogCancel>
          <Button onClick={onConfirm} disabled={isSubmitting}>
            {isSubmitting ? 'Sending...' : 'Send reset email'}
          </Button>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

export default ConfirmResetPasswordDialog;
