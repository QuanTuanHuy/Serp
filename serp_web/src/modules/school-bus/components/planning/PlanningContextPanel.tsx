'use client';

import React from 'react';
import { Eye, Plus, Loader2, Settings } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../../theme';
import { useGetSchoolDropdownOptionsQuery } from '../../api/schoolBusApi';
import { getPageItems } from '../../utils';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';
import { SchoolBusDatePicker } from '../ui/SchoolBusDatePicker';

export interface ContextFormState {
  schoolId: string;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
}

interface PlanningContextPanelProps {
  form: ContextFormState;
  onFormChange: (updater: (prev: ContextFormState) => ContextFormState) => void;
  onPreview: () => void;
  onCreateSession: () => void;
  previewing: boolean;
  creating: boolean;
  canCreate: boolean;
  createDisabledReason?: string;
  isPreviewContextMatch: boolean;
  onNewSession?: () => void;
}

const fieldLabel =
  'block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5 mt-3.5 first:mt-0';

export function PlanningContextPanel({
  form,
  onFormChange,
  onPreview,
  onCreateSession,
  previewing,
  creating,
  canCreate,
  createDisabledReason,
  isPreviewContextMatch,
  onNewSession,
}: PlanningContextPanelProps) {
  const { data: schoolsData } = useGetSchoolDropdownOptionsQuery();
  const schools = schoolsData?.data || [];

  return (
    <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-4'>
      <div className='border-b border-slate-100 pb-2.5 flex items-center justify-between'>
        <div>
          <h3 className='text-sm font-bold text-slate-900 flex items-center gap-2'>
            <Settings className='h-4 w-4 text-[#C81E3A]' />
            Thông tin lập tuyến
          </h3>
          <p className='text-[11px] text-slate-400 mt-0.5'>
            Chọn tham số để xác định phạm vi phiên lập kế hoạch.
          </p>
        </div>
        {onNewSession && (
          <Button
            variant='ghost'
            size='sm'
            onClick={onNewSession}
            className='h-7 px-2 text-[10px] font-bold text-[#C81E3A] hover:bg-red-50 hover:text-[#C81E3A] gap-1'
          >
            <Plus className='h-3 w-3' />
            Phiên mới
          </Button>
        )}
      </div>

      <div className='space-y-0.5'>
        <label className={fieldLabel}>Trường học *</label>
        <SchoolBusSelect
          fullWidth
          value={form.schoolId}
          onChange={(val) =>
            onFormChange((f) => ({ ...f, schoolId: val || '' }))
          }
          placeholder='- Chọn trường -'
          options={schools.map((s) => ({
            label: s.label,
            value: String(s.id),
          }))}
          searchable
        />

        <label className={fieldLabel}>Ngày phục vụ *</label>
        <SchoolBusDatePicker
          fullWidth
          value={form.serviceDate}
          onChange={(val) => onFormChange((f) => ({ ...f, serviceDate: val }))}
        />

        <label className={fieldLabel}>Chiều tuyến</label>
        <SchoolBusSelect
          fullWidth
          value={form.routeDirection}
          onChange={(val) =>
            onFormChange((f) => ({
              ...f,
              routeDirection: val as 'OUTBOUND' | 'RETURN',
            }))
          }
          options={[
            { label: 'Đến trường', value: 'OUTBOUND' },
            { label: 'Về nhà', value: 'RETURN' },
          ]}
        />
      </div>

      <div className='mt-5 flex flex-wrap gap-2 pt-2 border-t border-slate-100'>
        <Button
          onClick={onPreview}
          disabled={previewing}
          variant='outline'
          className={cn('flex-1 justify-center', schoolBusUi.outlineButton)}
        >
          {previewing ? (
            <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' />
          ) : (
            <Eye className='mr-1.5 h-3.5 w-3.5' />
          )}
          Xem trước
        </Button>
        <Button
          onClick={onCreateSession}
          disabled={creating || !isPreviewContextMatch || !canCreate}
          className={cn('flex-1 justify-center', schoolBusUi.primaryButton)}
        >
          {creating ? (
            <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' />
          ) : (
            <Plus className='mr-1.5 h-3.5 w-3.5' />
          )}
          Tạo phiên
        </Button>
      </div>

      {isPreviewContextMatch && !canCreate && createDisabledReason && (
        <div className='text-[10px] font-semibold text-rose-600 bg-rose-50 border border-rose-100 rounded-xl p-2 flex items-start gap-1.5'>
          <span className='mt-0.5'>Cảnh báo:</span>
          <span>{createDisabledReason}</span>
        </div>
      )}

      {!isPreviewContextMatch && (
        <div className='text-[10px] text-slate-400 text-center italic'>
          Vui lòng bấm Xem trước để kiểm tra nhu cầu trước khi tạo phiên mới.
        </div>
      )}
    </div>
  );
}

