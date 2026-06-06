'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Shared status confirmation dialog
 */

import { AlertTriangle } from 'lucide-react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui';
import { Button } from '@/shared/components/ui/button';

export interface AdminConfirmStatusDialogProps {
  open: boolean;
  title: string;
  description: string;
  impactText?: string;
  confirmLabel: string;
  confirmVariant?: 'default' | 'destructive';
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void | Promise<void>;
  loading?: boolean;
}

export function AdminConfirmStatusDialog({
  open,
  title,
  description,
  impactText,
  confirmLabel,
  confirmVariant = 'destructive',
  onOpenChange,
  onConfirm,
  loading,
}: AdminConfirmStatusDialogProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle className='flex items-center gap-2'>
            <AlertTriangle className='h-4 w-4 text-destructive' />
            {title}
          </AlertDialogTitle>
          <AlertDialogDescription className='space-y-2'>
            <span>{description}</span>
            {impactText ? (
              <span className='block font-medium text-foreground'>
                {impactText}
              </span>
            ) : null}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel asChild>
            <Button type='button' variant='outline' disabled={loading}>
              Cancel
            </Button>
          </AlertDialogCancel>
          <AlertDialogAction asChild>
            <Button
              type='button'
              variant={confirmVariant}
              onClick={onConfirm}
              disabled={loading}
            >
              {confirmLabel}
            </Button>
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
