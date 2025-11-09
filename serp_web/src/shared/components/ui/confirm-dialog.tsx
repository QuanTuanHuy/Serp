/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Reusable Confirmation Dialog
 */

'use client';

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { AlertTriangle, Loader2 } from 'lucide-react';

interface ConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title?: string;
  description?: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel?: () => void;
  isLoading?: boolean;
  variant?: 'default' | 'destructive';
}

export const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  open,
  onOpenChange,
  title = 'Are you sure?',
  description = 'This action cannot be undone.',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  onConfirm,
  onCancel,
  isLoading = false,
  variant = 'default',
}) => {
  const handleConfirm = () => {
    onConfirm();
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    } else {
      onOpenChange(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[425px]'>
        <DialogHeader>
          <div className='flex items-center gap-3'>
            {variant === 'destructive' && (
              <div className='h-10 w-10 rounded-full bg-red-100 dark:bg-red-950/20 flex items-center justify-center flex-shrink-0'>
                <AlertTriangle className='h-5 w-5 text-red-600 dark:text-red-400' />
              </div>
            )}
            <div className='flex-1'>
              <DialogTitle>{title}</DialogTitle>
              <DialogDescription className='mt-1.5'>
                {description}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <DialogFooter className='gap-2 sm:gap-0'>
          <Button variant='outline' onClick={handleCancel} disabled={isLoading}>
            {cancelText}
          </Button>
          <Button
            variant={variant === 'destructive' ? 'destructive' : 'default'}
            onClick={handleConfirm}
            disabled={isLoading}
          >
            {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
            {confirmText}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
