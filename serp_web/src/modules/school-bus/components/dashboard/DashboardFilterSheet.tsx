'use client';

import { useEffect, useState } from 'react';
import { Filter } from 'lucide-react';
import {
  Button,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui';
import type { SchoolBusDropdownOption } from '../../types';
import { SchoolBusDatePicker } from '../ui/SchoolBusDatePicker';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';

export interface DashboardFilters {
  serviceDate: string;
  schoolId?: number;
  direction: '' | 'OUTBOUND' | 'RETURN';
}

interface DashboardFilterSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  filters: DashboardFilters;
  schools: SchoolBusDropdownOption[];
  onApply: (filters: DashboardFilters) => void;
  onReset: () => void;
}

export const EMPTY_DASHBOARD_FILTERS: DashboardFilters = {
  serviceDate: '',
  schoolId: undefined,
  direction: '',
};

export function DashboardFilterSheet({
  open,
  onOpenChange,
  filters,
  schools,
  onApply,
  onReset,
}: DashboardFilterSheetProps) {
  const [draft, setDraft] = useState<DashboardFilters>(filters);

  useEffect(() => {
    if (open) {
      setDraft(filters);
    }
  }, [filters, open]);

  const schoolOptions = schools.map((school) => ({
    label: school.label,
    value: school.id,
    description: school.code,
  }));

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        side='right'
        className='school-bus-shell flex h-full w-full flex-col bg-background p-0 text-foreground sm:max-w-[420px]'
      >
        <SheetHeader className='border-b border-border px-6 py-5 text-left'>
          <div className='flex items-center gap-2'>
            <Filter className='h-4 w-4 text-primary' />
            <SheetTitle>Dashboard filters</SheetTitle>
          </div>
          <SheetDescription>
            Apply one filter set to every summary and chart block.
          </SheetDescription>
        </SheetHeader>

        <div className='flex-1 space-y-5 overflow-y-auto px-6 py-6'>
          <SchoolBusDatePicker
            label='Service date'
            value={draft.serviceDate}
            onChange={(serviceDate) =>
              setDraft((current) => ({ ...current, serviceDate }))
            }
            placeholder='Latest available date'
            fullWidth
          />

          <div className='space-y-1.5'>
            <label className='text-xs font-semibold text-foreground'>
              School
            </label>
            <SchoolBusSelect
              value={draft.schoolId}
              onChange={(value) =>
                setDraft((current) => ({
                  ...current,
                  schoolId:
                    value === '' || value == null ? undefined : Number(value),
                }))
              }
              options={schoolOptions}
              placeholder='All accessible schools'
              searchable
              clearable
              fullWidth
              size='md'
            />
          </div>

          <div className='space-y-1.5'>
            <label className='text-xs font-semibold text-foreground'>
              Direction
            </label>
            <SchoolBusSelect
              value={draft.direction}
              onChange={(value) =>
                setDraft((current) => ({
                  ...current,
                  direction:
                    value === 'OUTBOUND' || value === 'RETURN' ? value : '',
                }))
              }
              options={[
                { label: 'Outbound', value: 'OUTBOUND' },
                { label: 'Return', value: 'RETURN' },
              ]}
              placeholder='All directions'
              clearable
              fullWidth
              size='md'
            />
          </div>
        </div>

        <div className='flex gap-3 border-t border-border px-6 py-5'>
          <Button
            type='button'
            variant='outline'
            className='flex-1'
            onClick={() => {
              setDraft(EMPTY_DASHBOARD_FILTERS);
              onReset();
              onOpenChange(false);
            }}
          >
            Reset
          </Button>
          <Button
            type='button'
            className='flex-1'
            onClick={() => {
              onApply(draft);
              onOpenChange(false);
            }}
          >
            Apply filters
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  );
}
