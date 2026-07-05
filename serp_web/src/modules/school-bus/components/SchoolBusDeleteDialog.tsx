'use client';

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
import { schoolBusThemeStyle, schoolBusUi } from '../theme';

interface SchoolBusDeleteDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  onConfirm: () => void | Promise<void>;
  isLoading?: boolean;
}

export function SchoolBusDeleteDialog({
  open,
  onOpenChange,
  title,
  description,
  onConfirm,
  isLoading = false,
}: SchoolBusDeleteDialogProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent
        className='school-bus-shell rounded-[28px] border-border bg-background text-foreground shadow-[0_30px_90px_rgba(15,23,42,0.18)]'
        style={schoolBusThemeStyle}
      >
        <AlertDialogHeader>
          <AlertDialogTitle className='text-xl font-semibold tracking-tight text-foreground'>
            {title}
          </AlertDialogTitle>
          <AlertDialogDescription className='text-muted-foreground'>
            {description}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel
            className={schoolBusUi.outlineButton}
            disabled={isLoading}
          >
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction
            className={schoolBusUi.dangerButton}
            disabled={isLoading}
            onClick={onConfirm}
          >
            {isLoading ? 'Đang xóa...' : 'Xóa'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
