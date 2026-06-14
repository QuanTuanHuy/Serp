'use client';

import type { ReactNode } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { schoolBusThemeStyle } from '../theme';

interface SchoolBusFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  children: ReactNode;
  stickyFooter?: boolean;
}

export function SchoolBusFormDialog({
  open,
  onOpenChange,
  title,
  description,
  children,
  stickyFooter = false,
}: SchoolBusFormDialogProps) {
  if (stickyFooter) {
    return (
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent
          className='school-bus-shell max-h-[85vh] h-[720px] w-[calc(100vw-2rem)] flex flex-col rounded-[28px] border-border bg-background p-0 text-foreground shadow-[0_30px_90px_rgba(15,23,42,0.18)] sm:max-w-4xl lg:max-w-5xl overflow-hidden'
          style={schoolBusThemeStyle}
        >
          <DialogHeader className='px-6 pt-6 pb-2 sm:px-8 sm:pt-8 shrink-0'>
            <DialogTitle className='text-xl font-semibold tracking-tight text-foreground'>
              {title}
            </DialogTitle>
            <DialogDescription className='text-muted-foreground'>
              {description}
            </DialogDescription>
          </DialogHeader>
          {children}
        </DialogContent>
      </Dialog>
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className='school-bus-shell max-h-[90vh] w-[calc(100vw-2rem)] overflow-y-auto rounded-[28px] border-border bg-background p-6 text-foreground shadow-[0_30px_90px_rgba(15,23,42,0.18)] sm:max-w-4xl sm:p-8 lg:max-w-5xl'
        style={schoolBusThemeStyle}
      >
        <DialogHeader>
          <DialogTitle className='text-xl font-semibold tracking-tight text-foreground'>
            {title}
          </DialogTitle>
          <DialogDescription className='text-muted-foreground'>
            {description}
          </DialogDescription>
        </DialogHeader>
        {children}
      </DialogContent>
    </Dialog>
  );
}
