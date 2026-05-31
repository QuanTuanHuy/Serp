/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list filters
 */

'use client';

import type React from 'react';
import { useState } from 'react';
import { Check, ChevronRight, SlidersHorizontal } from 'lucide-react';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  ScrollArea,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  PMProjectCategoryFilter,
  PMProjectStatusFilter,
} from './PMProjectListToolbar';

type ProjectFilterCriterion = 'status' | 'category';

interface PMProjectCategoryOption {
  id: string;
  name: string;
}

interface PMProjectListFiltersProps {
  open: boolean;
  categoryFilter: PMProjectCategoryFilter;
  categoryOptions: PMProjectCategoryOption[];
  statusFilter: PMProjectStatusFilter;
  onOpenChange: (open: boolean) => void;
  onCategoryFilterChange: (value: PMProjectCategoryFilter) => void;
  onStatusFilterChange: (value: PMProjectStatusFilter) => void;
  onClear: () => void;
}

const STATUS_OPTIONS: Array<{
  value: PMProjectStatusFilter;
  label: string;
  description: string;
}> = [
  {
    value: 'ALL',
    label: 'All projects',
    description: 'Show active and archived projects.',
  },
  {
    value: 'ACTIVE',
    label: 'Active',
    description: 'Show projects that are currently available.',
  },
  {
    value: 'ARCHIVED',
    label: 'Archived',
    description: 'Show archived projects only.',
  },
];

export function PMProjectListFilters({
  open,
  categoryFilter,
  categoryOptions,
  statusFilter,
  onOpenChange,
  onCategoryFilterChange,
  onStatusFilterChange,
  onClear,
}: PMProjectListFiltersProps) {
  const [selectedCriterion, setSelectedCriterion] =
    useState<ProjectFilterCriterion>('status');

  const statusActiveCount = statusFilter === 'ALL' ? 0 : 1;
  const categoryActiveCount = categoryFilter === 'ALL' ? 0 : 1;
  const activeCount = statusActiveCount + categoryActiveCount;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-4xl gap-0 p-0 sm:max-h-[80vh] sm:max-w-4xl'>
        <DialogHeader className='border-b px-5 py-4'>
          <div className='flex items-center justify-between gap-3'>
            <div>
              <DialogTitle className='flex items-center gap-2 text-base'>
                <SlidersHorizontal className='h-4 w-4' />
                Filters
                {activeCount > 0 ? (
                  <Badge variant='secondary'>{activeCount}</Badge>
                ) : null}
              </DialogTitle>
              <p className='mt-1 text-sm text-muted-foreground'>
                Pick a criterion on the left, then choose a value.
              </p>
            </div>
            <Button
              type='button'
              variant='ghost'
              size='sm'
              onClick={() => onOpenChange(false)}
            >
              Close
            </Button>
          </div>
        </DialogHeader>

        <div className='grid min-h-[430px] grid-cols-[220px_minmax(0,1fr)]'>
          <aside className='border-r bg-muted/20 p-3'>
            <div className='space-y-1'>
              <CriterionButton
                label='Status'
                active={selectedCriterion === 'status'}
                count={statusActiveCount}
                onClick={() => setSelectedCriterion('status')}
              />
              <CriterionButton
                label='Category'
                active={selectedCriterion === 'category'}
                count={categoryActiveCount}
                onClick={() => setSelectedCriterion('category')}
              />
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

          <section className='flex min-h-0 flex-col'>
            {selectedCriterion === 'status' ? (
              <CriterionPane title='Status'>
                <ScrollArea className='h-[320px] rounded-md border'>
                  <div className='space-y-1 p-2'>
                    {STATUS_OPTIONS.map((option) => (
                      <FilterOption
                        key={option.value}
                        label={option.label}
                        description={option.description}
                        checked={statusFilter === option.value}
                        onClick={() => onStatusFilterChange(option.value)}
                      />
                    ))}
                  </div>
                </ScrollArea>
              </CriterionPane>
            ) : null}

            {selectedCriterion === 'category' ? (
              <CriterionPane title='Category'>
                <ScrollArea className='h-[320px] rounded-md border'>
                  <div className='space-y-1 p-2'>
                    <FilterOption
                      label='All categories'
                      description='Show projects from every category.'
                      checked={categoryFilter === 'ALL'}
                      onClick={() => onCategoryFilterChange('ALL')}
                    />
                    {categoryOptions.map((category) => (
                      <FilterOption
                        key={category.id}
                        label={category.name}
                        checked={categoryFilter === category.id}
                        onClick={() => onCategoryFilterChange(category.id)}
                      />
                    ))}
                    {categoryOptions.length === 0 ? (
                      <p className='px-2 py-1 text-xs text-muted-foreground'>
                        No categories
                      </p>
                    ) : null}
                  </div>
                </ScrollArea>
              </CriterionPane>
            ) : null}
          </section>
        </div>
      </DialogContent>
    </Dialog>
  );
}

function CriterionButton({
  label,
  active,
  count,
  onClick,
}: {
  label: string;
  active: boolean;
  count: number;
  onClick: () => void;
}) {
  return (
    <button
      type='button'
      onClick={onClick}
      className={cn(
        'flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-background',
        active && 'bg-background shadow-sm'
      )}
    >
      <span>{label}</span>
      <span className='flex items-center gap-2'>
        {count > 0 ? <Badge variant='secondary'>{count}</Badge> : null}
        <ChevronRight className='h-4 w-4 text-muted-foreground' />
      </span>
    </button>
  );
}

function CriterionPane({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className='flex min-h-0 flex-1 flex-col p-4'>
      <div className='mb-3'>
        <h3 className='text-sm font-semibold'>{title}</h3>
        <p className='text-sm text-muted-foreground'>
          Pick one value for this project list.
        </p>
      </div>
      {children}
    </div>
  );
}

function FilterOption({
  label,
  description,
  checked,
  onClick,
}: {
  label: string;
  description?: string;
  checked: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type='button'
      onClick={onClick}
      className='flex w-full items-start gap-3 rounded px-2 py-2 text-left text-sm hover:bg-muted'
    >
      <span
        className={cn(
          'mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded border text-[10px]',
          checked && 'border-primary bg-primary text-primary-foreground'
        )}
      >
        {checked ? <Check className='h-3 w-3' /> : null}
      </span>
      <span className='min-w-0 space-y-0.5'>
        <span className='block line-clamp-1 font-medium'>{label}</span>
        {description ? (
          <span className='block text-xs text-muted-foreground'>
            {description}
          </span>
        ) : null}
      </span>
    </button>
  );
}
