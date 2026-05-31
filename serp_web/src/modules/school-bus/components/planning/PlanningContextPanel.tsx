'use client';

import React from 'react';
import { Eye, Plus, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SchoolBusSection } from '../SchoolBusSection';
import { schoolBusUi } from '../../theme';
import {
  useGetSchoolsQuery,
  useGetActiveSchoolSchedulesQuery,
} from '../../api/schoolBusApi';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../../utils';
import type { PlanningMethod, SchoolBusDepot } from '../../types';

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
}

const fieldLabel = 'block text-xs font-medium text-slate-500 mb-1.5 mt-3 first:mt-0';
const fieldInput = 'w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-rose-300 focus:outline-none focus:ring-2 focus:ring-rose-100 disabled:bg-slate-50 disabled:text-slate-400';

export function PlanningContextPanel({
  form, onFormChange, onPreview, onCreateSession, previewing, creating, sessionActive, depots,
}: PlanningContextPanelProps) {
  const { data: schoolsData } = useGetSchoolsQuery({ ...SCHOOL_BUS_OPTION_QUERY, sortBy: 'name' });
  const schools = getPageItems(schoolsData?.data);
  const selectedSchoolId = Number(form.schoolId) || 0;
  const { data: schedulesData } = useGetActiveSchoolSchedulesQuery(selectedSchoolId, { skip: !selectedSchoolId });
  const schedules = schedulesData?.data ?? [];

  return (
    <div className='space-y-4'>
      <SchoolBusSection title='Planning Context' description='Select parameters to scope the planning session.'>
        <div className='space-y-0.5'>
          <label className={fieldLabel}>School *</label>
          <select className={fieldInput} value={form.schoolId}
            onChange={e => onFormChange(f => ({ ...f, schoolId: e.target.value, schoolScheduleId: '' }))}>
            <option value=''>— Select school —</option>
            {schools.map(s => <option key={s.id} value={String(s.id)}>{s.name}</option>)}
          </select>

          <label className={fieldLabel}>School Schedule *</label>
          <select className={fieldInput} value={form.schoolScheduleId}
            onChange={e => onFormChange(f => ({ ...f, schoolScheduleId: e.target.value }))}
            disabled={!selectedSchoolId}>
            <option value=''>{selectedSchoolId ? '— Select schedule —' : '— Select school first —'}</option>
            {schedules.map(sc => (
              <option key={sc.id} value={String(sc.id)}>
                {sc.scheduleName} ({sc.shiftType}{sc.daysOfWeek ? ` · ${sc.daysOfWeek.join(',')}` : ''})
              </option>
            ))}
          </select>

          <label className={fieldLabel}>Service Date *</label>
          <input type='date' className={fieldInput} value={form.serviceDate}
            onChange={e => onFormChange(f => ({ ...f, serviceDate: e.target.value }))} />

          <label className={fieldLabel}>Direction</label>
          <select className={fieldInput} value={form.routeDirection}
            onChange={e => onFormChange(f => ({ ...f, routeDirection: e.target.value as 'OUTBOUND' | 'RETURN' }))}>
            <option value='OUTBOUND'>OUTBOUND (Home → School)</option>
            <option value='RETURN'>RETURN (School → Home)</option>
          </select>

          <label className={fieldLabel}>Planning Method</label>
          <select className={fieldInput} value={form.planningMethod}
            onChange={e => onFormChange(f => ({ ...f, planningMethod: e.target.value as PlanningMethod }))}>
            <option value='GREEDY'>Greedy (Auto-generate)</option>
            <option value='MANUAL'>Manual</option>
          </select>

          {form.planningMethod === 'GREEDY' && (
            <>
              <label className={fieldLabel}>Default Bus Capacity</label>
              <input type='number' className={fieldInput} value={form.defaultBusCapacity}
                onChange={e => onFormChange(f => ({ ...f, defaultBusCapacity: e.target.value }))}
                min={1} max={100} placeholder='30' />

              <label className={fieldLabel}>Depot (Bus Garage) *</label>
              <select className={fieldInput} value={form.depotId}
                onChange={e => onFormChange(f => ({ ...f, depotId: e.target.value }))}>
                <option value=''>— Select depot —</option>
                {depots.map(d => (
                  <option key={d.id} value={String(d.id)}>
                    {d.name}{d.address ? ` (${d.address})` : ''}
                  </option>
                ))}
              </select>
              {form.depotId === '' && (
                <p className='mt-1 text-[11px] text-rose-500'>Depot is required for greedy generation.</p>
              )}
            </>
          )}
        </div>

        <div className='mt-5 flex flex-wrap gap-2'>
          <Button onClick={onPreview} disabled={previewing} variant='outline' className={schoolBusUi.outlineButton}>
            {previewing ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Eye className='mr-1.5 h-3.5 w-3.5' />}
            Preview Demand
          </Button>
          <Button onClick={onCreateSession} disabled={creating || sessionActive} className={schoolBusUi.primaryButton}>
            {creating ? <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' /> : <Plus className='mr-1.5 h-3.5 w-3.5' />}
            Create Session
          </Button>
        </div>
      </SchoolBusSection>
    </div>
  );
}
