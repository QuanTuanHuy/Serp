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
            <SheetTitle>Bộ lọc Dashboard</SheetTitle>
          </div>
          <SheetDescription>
            Áp dụng bộ lọc cho toàn bộ chỉ số tổng quan và biểu đồ.
          </SheetDescription>
        </SheetHeader>

        <div className='flex-1 space-y-5 overflow-y-auto px-6 py-6'>
          <SchoolBusDatePicker
            label='Ngày phục vụ'
            value={draft.serviceDate}
            onChange={(serviceDate) =>
              setDraft((current) => ({ ...current, serviceDate }))
            }
            placeholder='Ngày gần nhất có dữ liệu'
            fullWidth
          />

          <div className='space-y-1.5'>
            <label className='text-xs font-semibold text-foreground'>
              Trường học
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
              placeholder='Tất cả trường có quyền truy cập'
              searchable
              clearable
              fullWidth
              size='md'
            />
          </div>

          <div className='space-y-1.5'>
            <label className='text-xs font-semibold text-foreground'>
              Chiều tuyến
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
                { label: 'Chiều đi học', value: 'OUTBOUND' },
                { label: 'Chiều về nhà', value: 'RETURN' },
              ]}
              placeholder='Tất cả chiều tuyến'
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
            Đặt lại
          </Button>
          <Button
            type='button'
            className='flex-1'
            onClick={() => {
              onApply(draft);
              onOpenChange(false);
            }}
          >
            Áp dụng bộ lọc
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  );
}
