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
}

export function SchoolBusFormDialog({
  open,
  onOpenChange,
  title,
  description,
  children,
}: SchoolBusFormDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className='max-h-[90vh] w-[calc(100vw-2rem)] overflow-y-auto rounded-[28px] border-slate-200 bg-white p-6 text-slate-950 shadow-[0_30px_90px_rgba(15,23,42,0.18)] sm:max-w-4xl sm:p-8 lg:max-w-5xl'
        style={schoolBusThemeStyle}
      >
        <DialogHeader>
          <DialogTitle className='text-xl font-semibold tracking-tight text-slate-950'>
            {title}
          </DialogTitle>
          <DialogDescription className='text-slate-500'>
            {description}
          </DialogDescription>
        </DialogHeader>
        {children}
      </DialogContent>
    </Dialog>
  );
}
