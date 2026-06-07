'use client';

import React from 'react';
import { Eye, Plus, Loader2, Settings } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../../theme';
import {
  useGetSchoolsQuery,
  useGetActiveSchoolSchedulesQuery,
} from '../../api/schoolBusApi';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../../utils';
import type { PlanningMethod, SchoolBusDepot } from '../../types';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';
import { SchoolBusDatePicker } from '../ui/SchoolBusDatePicker';

export interface ContextFormState {
  schoolId: string;
  schoolScheduleId: string;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
  planningMethod: PlanningMethod;
  defaultBusCapacity: string;
  depotId: string;
}

interface PlanningContextPanelProps {
  form: ContextFormState;
  onFormChange: (updater: (prev: ContextFormState) => ContextFormState) => void;
  onPreview: () => void;
  onCreateSession: () => void;
  previewing: boolean;
  creating: boolean;
  sessionActive: boolean;
  depots: SchoolBusDepot[];
  hasBlockingIssues?: boolean;
}

const fieldLabel = 'block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5 mt-3.5 first:mt-0';
const fieldInput = 'w-full rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-900 shadow-sm focus:border-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900/10 disabled:bg-slate-50 disabled:text-slate-400 transition-all';

export function PlanningContextPanel({
  form, onFormChange, onPreview, onCreateSession, previewing, creating, sessionActive, depots, hasBlockingIssues,
}: PlanningContextPanelProps) {
  const { data: schoolsData } = useGetSchoolsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const schools = getPageItems(schoolsData?.data);
  const selectedSchoolId = Number(form.schoolId) || 0;
  const { data: schedulesData } = useGetActiveSchoolSchedulesQuery(selectedSchoolId, { skip: !selectedSchoolId });
  const schedules = schedulesData?.data ?? [];

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
          onChange={val => onFormChange(f => ({ ...f, schoolId: val || '', schoolScheduleId: '' }))}
          placeholder='— Select school —'
          options={schools.map(s => ({ label: s.name, value: String(s.id) }))}
          searchable
        />

        <label className={fieldLabel}>School Schedule *</label>
        <SchoolBusSelect
          fullWidth
          value={form.schoolScheduleId}
          onChange={val => onFormChange(f => ({ ...f, schoolScheduleId: val || '' }))}
          placeholder={selectedSchoolId ? '— Select schedule —' : '— Select school first —'}
          disabled={!selectedSchoolId}
          options={schedules.map(sc => ({
            label: `${sc.scheduleName} (${sc.shiftType}${sc.daysOfWeek ? ` · ${sc.daysOfWeek.join(',')}` : ''})`,
            value: String(sc.id)
          }))}
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
            { label: 'OUTBOUND (Home → School)', value: 'OUTBOUND' },
            { label: 'RETURN (School → Home)', value: 'RETURN' }
          ]}
        />

        <label className={fieldLabel}>Planning Method</label>
        <SchoolBusSelect
          fullWidth
          value={form.planningMethod}
          onChange={val => onFormChange(f => ({ ...f, planningMethod: val as PlanningMethod }))}
          options={[
            { label: 'Greedy (Auto-generate)', value: 'GREEDY' },
            { label: 'Manual', value: 'MANUAL' }
          ]}
        />

        {form.planningMethod === 'GREEDY' && (
          <>
            <label className={fieldLabel}>Default Bus Capacity</label>
            <input type='number' className={fieldInput} value={form.defaultBusCapacity}
              onChange={e => onFormChange(f => ({ ...f, defaultBusCapacity: e.target.value }))}
              min={1} max={100} placeholder='30' />

            <label className={fieldLabel}>Depot (Bus Garage) *</label>
            <SchoolBusSelect
              fullWidth
              value={form.depotId}
              onChange={val => onFormChange(f => ({ ...f, depotId: val || '' }))}
              placeholder='— Select depot —'
              options={depots.map(d => ({
                label: d.name + (d.address ? ` (${d.address})` : ''),
                value: String(d.id)
              }))}
              searchable
            />
            {form.depotId === '' && (
              <p className='mt-1 text-[11px] text-red-500 font-semibold'>Depot is required for greedy generation.</p>
            )}
          </>
        )}
      </div>

      <div className='mt-5 flex flex-wrap gap-2 pt-2 border-t border-slate-100'>
        <Button onClick={onPreview} disabled={previewing} variant='outline' className={cn('flex-1 justify-center', schoolBusUi.outlineButton)}>
          {previewing ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Eye className='mr-1.5 h-3.5 w-3.5' />}
          Preview
        </Button>
        <Button onClick={onCreateSession} disabled={creating || sessionActive || hasBlockingIssues} className={cn('flex-1 justify-center', schoolBusUi.primaryButton)}>
          {creating ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Plus className='mr-1.5 h-3.5 w-3.5' />}
          Create
        </Button>
        {hasBlockingIssues && (
          <p className='mt-2.5 w-full text-[10px] text-red-500 font-bold text-center bg-red-50/50 py-1.5 rounded-xl border border-red-100'>
            ⚠️ Disabled due to blocking issues in preview.
          </p>
        )}
      </div>
    </div>
  );
}
