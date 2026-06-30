'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Shared admin filter dialog shell
 */

import type React from 'react';
import { CheckCircle, ChevronRight, SlidersHorizontal } from 'lucide-react';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';

export interface AdminFilterCriterion {
  id: string;
  label: string;
  count: number;
}

export interface AdminFilterDialogProps {
  open: boolean;
  title: string;
  description: string;
  criteria: AdminFilterCriterion[];
  selectedCriterion: string;
  onSelectCriterion: (criterion: string) => void;
  onOpenChange: (open: boolean) => void;
  onClear: () => void;
  children: React.ReactNode;
}

export function AdminFilterDialog({
  open,
  title,
  description,
  criteria,
  selectedCriterion,
  onSelectCriterion,
  onOpenChange,
  onClear,
  children,
}: AdminFilterDialogProps) {
  const activeCount = criteria.reduce((total, item) => total + item.count, 0);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='gap-0 p-0 sm:max-h-[85vh] sm:max-w-5xl'>
        <DialogHeader className='border-b px-5 py-4'>
          <DialogTitle className='flex items-center gap-2 text-base'>
            <SlidersHorizontal className='h-4 w-4' />
            {title}
            {activeCount > 0 ? (
              <Badge variant='secondary'>{activeCount}</Badge>
            ) : null}
          </DialogTitle>
          <p className='text-sm text-muted-foreground'>{description}</p>
        </DialogHeader>

        <div className='grid min-h-[520px] grid-cols-[220px_minmax(0,1fr)]'>
          <aside className='border-r bg-muted/20 p-3'>
            <div className='space-y-1'>
              {criteria.map((criterion) => (
                <button
                  key={criterion.id}
                  type='button'
                  onClick={() => onSelectCriterion(criterion.id)}
                  className={cn(
                    'flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-background',
                    selectedCriterion === criterion.id &&
                      'bg-background shadow-sm'
                  )}
                >
                  <span>{criterion.label}</span>
                  <span className='flex items-center gap-2'>
                    {criterion.count > 0 ? (
                      <Badge variant='secondary'>{criterion.count}</Badge>
                    ) : null}
                    <ChevronRight className='h-4 w-4 text-muted-foreground' />
                  </span>
                </button>
              ))}
            </div>

            <Separator className='my-4' />
            <Button
              type='button'
              variant='ghost'
              className='w-full justify-start'
              onClick={onClear}
              disabled={activeCount === 0}
            >
              Clear all
            </Button>
          </aside>

          <section className='flex min-h-0 flex-col'>{children}</section>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export interface FilterPaneProps {
  title: string;
  children: React.ReactNode;
  className?: string;
}

export function FilterPane({ title, children, className }: FilterPaneProps) {
  return (
    <div className='flex min-h-0 flex-1 flex-col p-4'>
      <div className='mb-3'>
        <h3 className='text-sm font-semibold'>{title}</h3>
        <p className='text-sm text-muted-foreground'>Select one value.</p>
      </div>
      <div className={cn('flex-1 overflow-y-auto space-y-2 pr-1', className)}>
        {children}
      </div>
    </div>
  );
}

export interface FilterOptionProps {
  label: string;
  selected: boolean;
  onSelect: () => void;
}

export function FilterOption({ label, selected, onSelect }: FilterOptionProps) {
  return (
    <button
      type='button'
      onClick={onSelect}
      title={label}
      className={cn(
        'flex w-full min-w-0 items-center justify-between gap-2 rounded-md px-3 py-2 text-left text-sm hover:bg-muted transition-colors',
        selected && 'bg-muted font-medium'
      )}
    >
      <span className='min-w-0 flex-1 truncate'>{label}</span>
      {selected ? (
        <CheckCircle className='h-4 w-4 shrink-0 text-primary' />
      ) : null}
    </button>
  );
}
