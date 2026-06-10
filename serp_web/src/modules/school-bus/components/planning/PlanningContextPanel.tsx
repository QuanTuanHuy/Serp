'use client';

import React from 'react';
import { Eye, Plus, Loader2, Settings } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../../theme';
import {
  useGetSchoolsQuery,
} from '../../api/schoolBusApi';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../../utils';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';
import { SchoolBusDatePicker } from '../ui/SchoolBusDatePicker';

export interface ContextFormState {
  schoolId: string;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
  planningMethod: 'GREEDY' | 'MANUAL';
}

interface PlanningContextPanelProps {
  form: ContextFormState;
  onFormChange: (updater: (prev: ContextFormState) => ContextFormState) => void;
  onPreview: () => void;
  onCreateSession: () => void;
  previewing: boolean;
  creating: boolean;
  sessionActive: boolean;
}

const fieldLabel = 'block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5 mt-3.5 first:mt-0';

export function PlanningContextPanel({
  form, onFormChange, onPreview, onCreateSession, previewing, creating, sessionActive,
}: PlanningContextPanelProps) {
  const { data: schoolsData } = useGetSchoolsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const schools = getPageItems(schoolsData?.data);

  return (
    <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-4'>
      <div className='border-b border-slate-100 pb-2.5'>
        <h3 className='text-sm font-bold text-slate-900 flex items-center gap-2'>
          <Settings className='h-4 w-4 text-[#C81E3A]' />
          Planning Context
        </h3>
        <p className='text-[11px] text-slate-400 mt-0.5'>Select parameters to scope the planning session.</p>
      </div>

      <div className='space-y-0.5'>
        <label className={fieldLabel}>School *</label>
        <SchoolBusSelect
          fullWidth
          value={form.schoolId}
          onChange={val => onFormChange(f => ({ ...f, schoolId: val || '' }))}
          placeholder='— Select school —'
          options={schools.map(s => ({ label: s.name, value: String(s.id) }))}
          searchable
        />

        <label className={fieldLabel}>Service Date *</label>
        <SchoolBusDatePicker
          fullWidth
          value={form.serviceDate}
          onChange={val => onFormChange(f => ({ ...f, serviceDate: val }))}
        />

        <label className={fieldLabel}>Route Direction</label>
        <SchoolBusSelect
          fullWidth
          value={form.routeDirection}
          onChange={val => onFormChange(f => ({ ...f, routeDirection: val as 'OUTBOUND' | 'RETURN' }))}
          options={[
            { label: 'Outbound (To school)', value: 'OUTBOUND' },
            { label: 'Return (From school)', value: 'RETURN' }
          ]}
        />

      </div>

      <div className='mt-5 flex flex-wrap gap-2 pt-2 border-t border-slate-100'>
        <Button onClick={onPreview} disabled={previewing} variant='outline' className={cn('flex-1 justify-center', schoolBusUi.outlineButton)}>
          {previewing ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Eye className='mr-1.5 h-3.5 w-3.5' />}
          Preview
        </Button>
        <Button onClick={onCreateSession} disabled={creating || sessionActive} className={cn('flex-1 justify-center', schoolBusUi.primaryButton)}>
          {creating ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Plus className='mr-1.5 h-3.5 w-3.5' />}
          Create
        </Button>
      </div>
    </div>
  );
}
