'use client';

import * as React from 'react';
import { Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { schoolBusUi } from '../theme';
import { SchoolBusFormDialog } from './SchoolBusFormDialog';

export interface SchoolBusDetailField {
  label: string;
  value?: React.ReactNode;
  fullWidth?: boolean;
}

export interface SchoolBusDetailSection {
  title: string;
  fields: SchoolBusDetailField[];
}

interface SchoolBusReadOnlyDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  sections: SchoolBusDetailSection[];
  isLoading?: boolean;
  isError?: boolean;
}

export function SchoolBusReadOnlyDetailDialog({
  open,
  onOpenChange,
  title,
  description,
  sections,
  isLoading = false,
  isError = false,
}: SchoolBusReadOnlyDetailDialogProps) {
  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={title}
      description={description}
    >
      {isLoading ? (
        <div className='flex min-h-[220px] items-center justify-center rounded-2xl border border-dashed border-border bg-muted/20 text-sm text-muted-foreground'>
          <Loader2 className='mr-2 h-4 w-4 animate-spin' />
          Đang tải chi tiết...
        </div>
      ) : isError ? (
        <div className='rounded-2xl border border-destructive/20 bg-destructive/5 p-4 text-sm text-destructive'>
          Không thể tải thông tin chi tiết. Vui lòng đóng và thử lại.
        </div>
      ) : (
        <div className='space-y-6'>
          {sections.map((section) => (
            <section key={section.title} className='space-y-3'>
              <h3 className='text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground'>
                {section.title}
              </h3>
              <div className='grid grid-cols-1 gap-3 md:grid-cols-2'>
                {section.fields.map((field) => (
                  <ReadOnlyDetailField key={field.label} field={field} />
                ))}
              </div>
            </section>
          ))}
          <div className='flex justify-end border-t border-border pt-4'>
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={() => onOpenChange(false)}
            >
              Đóng
            </Button>
          </div>
        </div>
      )}
    </SchoolBusFormDialog>
  );
}

function ReadOnlyDetailField({ field }: { field: SchoolBusDetailField }) {
  return (
    <div
      className={
        field.fullWidth
          ? 'rounded-2xl border border-border bg-muted/20 p-3 md:col-span-2'
          : 'rounded-2xl border border-border bg-muted/20 p-3'
      }
    >
      <p className='text-[11px] font-medium uppercase tracking-wide text-muted-foreground'>
        {field.label}
      </p>
      <div className='mt-1 whitespace-pre-wrap break-words text-sm font-medium text-foreground'>
        {hasDisplayValue(field.value) ? field.value : '-'}
      </div>
    </div>
  );
}

function hasDisplayValue(value: React.ReactNode) {
  if (value === null || value === undefined || value === false) {
    return false;
  }
  if (typeof value === 'string') {
    return value.trim().length > 0;
  }
  return true;
}

