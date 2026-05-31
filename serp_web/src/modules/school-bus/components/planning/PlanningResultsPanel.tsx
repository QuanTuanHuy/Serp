'use client';

import React, { useState } from 'react';
import { ChevronUp, ChevronDown, Trash2, AlertTriangle, Info, Ban, MapPin, Users, Plus, Route } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SchoolBusConfirmDialog, useSchoolBusConfirm } from '../SchoolBusConfirmDialog';
import { schoolBusUi } from '../../theme';
import { ManualDemandAssignPanel } from './ManualDemandAssignPanel';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';
import {
  useGetRouteByIdQuery,
  useReorderRouteStopsMutation,
  useRemoveRouteStopMutation,
  useRemoveRouteStudentMutation,
  useGetDepotsQuery,
} from '../../api/schoolBusApi';
import type {
  SchoolBusPlanningPreview,
  SchoolBusGreedyGenerateResult,
  SchoolBusPlanningIssue,
  SchoolBusRouteQuality,
  SchoolBusPlanningSession,
  SchoolBusRoute,
  SchoolBusEligibleStudent,
  CreateRouteInSessionRequest,
} from '../../types';

/* ── Create Route Dialog (MANUAL mode) ──────────────────────────────────── */

type LocationType = 'SCHOOL' | 'DEPOT';
type DirectionType = 'OUTBOUND' | 'RETURN';

interface CreateRouteFormState {
  routeName: string;
  routeDirection: DirectionType;
  startLocationType: LocationType;
  startDepotId: number | '';
  endLocationType: LocationType;
  endDepotId: number | '';
  planningNotes: string;
}

/** Smart defaults: OUTBOUND starts at Depot → ends at School; RETURN is reversed. */
function defaultsForDirection(dir: DirectionType): Pick<CreateRouteFormState, 'startLocationType' | 'endLocationType'> {
  return dir === 'OUTBOUND'
    ? { startLocationType: 'DEPOT', endLocationType: 'SCHOOL' }
    : { startLocationType: 'SCHOOL', endLocationType: 'DEPOT' };
}

function CreateRoutePanel({
  session,
  onSubmit,
  submitting,
}: {
  session: SchoolBusPlanningSession;
  onSubmit: (req: CreateRouteInSessionRequest) => void;
  submitting?: boolean;
}) {
  const [open, setOpen] = useState(false);

  const initDir = (session.routeDirection as DirectionType) ?? 'OUTBOUND';
  const [form, setForm] = useState<CreateRouteFormState>({
    routeName: '',
    routeDirection: initDir,
    ...defaultsForDirection(initDir),
    startDepotId: '',
    endDepotId: '',
    planningNotes: '',
  });

  // Fetch depots for selectors
  const { data: depotsData } = useGetDepotsQuery({ page: 0, size: 100 });
  const depots = depotsData?.data?.items ?? [];
  const hasDepots = depots.length > 0;

  const needsStartDepot = form.startLocationType === 'DEPOT';
  const needsEndDepot   = form.endLocationType   === 'DEPOT';

  const canSubmit =
    form.routeName.trim() !== '' &&
    (!needsStartDepot || form.startDepotId !== '') &&
    (!needsEndDepot || form.endDepotId !== '');

  const handleDirectionChange = (dir: DirectionType) => {
    setForm(p => ({ ...p, routeDirection: dir, ...defaultsForDirection(dir), startDepotId: '', endDepotId: '' }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;
    const req: CreateRouteInSessionRequest = {
      schoolId: session.schoolId,
      routeDirection: form.routeDirection,
      startLocationType: form.startLocationType,
      endLocationType: form.endLocationType,
      routeName: form.routeName,
      serviceDate: session.serviceDate,
      schoolScheduleId: session.schoolScheduleId,
      planningNotes: form.planningNotes || undefined,
    };
    if (form.startLocationType === 'SCHOOL') req.startSchoolId = session.schoolId;
    else req.startDepotId = Number(form.startDepotId);
    if (form.endLocationType === 'SCHOOL') req.endSchoolId = session.schoolId;
    else req.endDepotId = Number(form.endDepotId);
    onSubmit(req);
    setOpen(false);
    setForm(p => ({ ...p, routeName: '', startDepotId: '', endDepotId: '', planningNotes: '' }));
  };

  const inputCls = 'w-full rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-900 shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-900/10 focus:border-slate-900 transition-all';
  const labelCls = 'block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1';

  return (
    <div className='mb-4'>
      <Button
        size='sm'
        variant='outline'
        onClick={() => setOpen(v => !v)}
        className='w-full rounded-full border-red-250 text-[#C81E3A] hover:bg-[#FDECEF]/50 gap-1.5 text-xs font-semibold'
      >
        <Plus className='h-3.5 w-3.5' />
        {open ? 'Cancel Route Builder' : 'Open Route Builder'}
      </Button>
      {open && (
        <form onSubmit={handleSubmit} className='mt-3 rounded-2xl border border-slate-200 bg-slate-50/50 p-4 space-y-4 shadow-sm'>
          <div className='border-b border-slate-100 pb-1.5'>
            <h4 className='text-xs font-bold text-slate-700 uppercase tracking-wide'>Route Builder</h4>
          </div>

          {/* Group 1: Route identity */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>1. Route Identity</p>
            <div>
              <label className={labelCls}>Route Name *</label>
              <input
                className={inputCls}
                value={form.routeName}
                onChange={e => setForm(p => ({ ...p, routeName: e.target.value }))}
                placeholder='e.g. Morning Outbound Route A'
                required
              />
            </div>
            <div>
              <label className={labelCls}>Direction</label>
              <SchoolBusSelect
                fullWidth
                value={form.routeDirection}
                onChange={val => handleDirectionChange(val as DirectionType)}
                options={[
                  { label: 'OUTBOUND (Depot → School)', value: 'OUTBOUND' },
                  { label: 'RETURN (School → Depot)', value: 'RETURN' }
                ]}
              />
            </div>
          </div>

          {/* Group 2: Start/End terminal */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>2. Start & End Terminals</p>
            
            {/* Start location */}
            <div className='space-y-2 pt-1.5 border-t border-slate-50 first:border-0 first:pt-0'>
              <label className={labelCls}>Start Location</label>
              <div className='flex gap-4'>
                {(['DEPOT', 'SCHOOL'] as LocationType[]).map(t => (
                  <label key={t} className='flex items-center gap-1.5 cursor-pointer text-xs font-semibold text-slate-600'>
                    <input
                      type='radio'
                      name='startLocationType'
                      value={t}
                      checked={form.startLocationType === t}
                      onChange={() => setForm(p => ({ ...p, startLocationType: t, startDepotId: '' }))}
                      className='accent-[#C81E3A]'
                    />
                    {t === 'DEPOT' ? 'Depot' : 'School'}
                  </label>
                ))}
              </div>
              {needsStartDepot ? (
                hasDepots ? (
                  <div className='mt-2'>
                    <SchoolBusSelect
                      fullWidth
                      value={form.startDepotId}
                      onChange={val => setForm(p => ({ ...p, startDepotId: val === '' ? '' : Number(val) }))}
                      placeholder='— Select depot —'
                      options={depots.map(d => ({
                        label: d.name,
                        value: d.id
                      }))}
                    />
                  </div>
                ) : (
                  <p className='text-[11px] text-amber-600 font-medium'>⚠️ No depots available. Please create a depot first.</p>
                )
              ) : (
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100'>Starts at school (ID: {session.schoolId})</p>
              )}
            </div>

            {/* End location */}
            <div className='space-y-2 pt-3 border-t border-slate-100'>
              <label className={labelCls}>End Location</label>
              <div className='flex gap-4'>
                {(['SCHOOL', 'DEPOT'] as LocationType[]).map(t => (
                  <label key={t} className='flex items-center gap-1.5 cursor-pointer text-xs font-semibold text-slate-600'>
                    <input
                      type='radio'
                      name='endLocationType'
                      value={t}
                      checked={form.endLocationType === t}
                      onChange={() => setForm(p => ({ ...p, endLocationType: t, endDepotId: '' }))}
                      className='accent-[#C81E3A]'
                    />
                    {t === 'DEPOT' ? 'Depot' : 'School'}
                  </label>
                ))}
              </div>
              {needsEndDepot ? (
                hasDepots ? (
                  <div className='mt-2'>
                    <SchoolBusSelect
                      fullWidth
                      value={form.endDepotId}
                      onChange={val => setForm(p => ({ ...p, endDepotId: val === '' ? '' : Number(val) }))}
                      placeholder='— Select depot —'
                      options={depots.map(d => ({
                        label: d.name,
                        value: d.id
                      }))}
                    />
                  </div>
                ) : (
                  <p className='text-[11px] text-amber-600 font-medium'>⚠️ No depots available. Please create a depot first.</p>
                )
              ) : (
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100'>Ends at school (ID: {session.schoolId})</p>
              )}
            </div>
          </div>

          {/* Group 3: Notes & validation warnings */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>3. Additional Information</p>
            <div>
              <label className={labelCls}>Notes</label>
              <input
                className={inputCls}
                value={form.planningNotes}
                onChange={e => setForm(p => ({ ...p, planningNotes: e.target.value }))}
                placeholder='Optional notes'
              />
            </div>
            {((needsStartDepot && form.startDepotId === '') || (needsEndDepot && form.endDepotId === '')) && (
              <p className='text-[11px] text-amber-600 font-semibold'>⚠️ Depot selection is required to build this route.</p>
            )}
          </div>

          <div className='flex justify-end gap-2 pt-2'>
            <Button type='button' size='sm' variant='ghost' onClick={() => setOpen(false)} className='text-slate-500 hover:text-slate-900'>Cancel</Button>
            <Button type='submit' size='sm' disabled={submitting || !canSubmit} className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white'>
              {submitting ? 'Creating…' : 'Create Route'}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}

/* ── Session Route Card (MANUAL or reloaded GREEDY routes) ────────────────── */

function SessionRouteCard({
  route,
  onSelect,
  isSelected,
}: {
  route: SchoolBusRoute;
  onSelect: (id: number) => void;
  isSelected: boolean;
}) {
  return (
    <div
      onClick={() => onSelect(route.id)}
      className={cn(
        'cursor-pointer rounded-2xl border p-4 transition-all duration-200 flex flex-col gap-2.5',
        isSelected
          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-350 border-r-slate-350 bg-slate-50/50 shadow-sm'
          : schoolBusUi.interactiveCard
      )}
    >
      <div className='flex items-start justify-between gap-3'>
        <div className='min-w-0'>
          <p className='text-xs font-bold text-slate-900 truncate'>{route.routeName}</p>
          <p className='text-[10px] font-bold text-slate-500 mt-0.5 flex items-center gap-1.5'>
            <span className='px-1.5 py-0.5 bg-indigo-50 border border-indigo-100 rounded text-indigo-700 text-[9px]'>{route.routeCode}</span>
            <span>{route.routeDirection}</span>
          </p>
        </div>
        <span className={cn(
          'rounded-full px-2.5 py-0.5 text-[10px] font-semibold border shadow-none shrink-0',
          route.status === 'PUBLISHED' ? 'bg-emerald-50 text-emerald-700 border-emerald-250' :
          route.status === 'CANCELLED' ? 'bg-slate-50 text-slate-500 border-slate-200' : 'bg-amber-50 text-amber-700 border-amber-250'
        )}>{route.status === 'PUBLISHED' ? 'Published' : route.status === 'CANCELLED' ? 'Cancelled' : 'Draft'}</span>
      </div>
      <div className='flex items-center justify-between border-t border-slate-100/50 pt-2 text-[10px] text-slate-450'>
        <span>{route.serviceDate}</span>
        {isSelected && <span className='font-extrabold text-[#C81E3A] flex items-center gap-1'>▶ Selected</span>}
      </div>
    </div>
  );
}

/* ── Issue badge ────────────────────────────────────────────────── */

const severityStyle: Record<string, string> = {
  BLOCKING: 'border-red-205 bg-red-50 text-red-700',
  WARNING: 'border-amber-205 bg-amber-50 text-amber-700',
  INFO: 'border-sky-205 bg-sky-50 text-sky-700',
};
const SeverityIcon = ({ s }: { s: string }) => s === 'BLOCKING' ? <Ban className='h-3 w-3' /> : s === 'WARNING' ? <AlertTriangle className='h-3 w-3' /> : <Info className='h-3 w-3' />;

function IssueBadge({ issue }: { issue: SchoolBusPlanningIssue }) {
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-[11px] font-semibold', severityStyle[issue.severity] || severityStyle.INFO)}>
      <SeverityIcon s={issue.severity} /> {issue.message}
    </span>
  );
}

/* ── Route Card ─────────────────────────────────────────────────── */

function RouteCard({ route, onSelect, isSelected }: { route: SchoolBusRouteQuality; onSelect: (id: number) => void; isSelected: boolean }) {
  const [showIssues, setShowIssues] = useState(false);
  const score = route.qualityScore ?? 0;
  const scoreColor = score >= 80 ? 'text-emerald-600 border-emerald-350 bg-emerald-50/50' : score >= 50 ? 'text-amber-600 border-amber-350 bg-amber-50/50' : 'text-red-600 border-red-350 bg-red-50/50';

  return (
    <div onClick={() => onSelect(route.routeId)}
      className={cn(
        'cursor-pointer rounded-2xl border p-4 transition-all duration-200 flex flex-col gap-2.5',
        isSelected
          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-350 border-r-slate-355 bg-slate-50/50 shadow-sm'
          : schoolBusUi.interactiveCard
      )}>
      <div className='flex items-start justify-between gap-3'>
        <div className='min-w-0'>
          <p className='text-xs font-bold text-slate-900 truncate'>{route.routeName}</p>
          <p className='text-[10px] font-bold text-slate-550 mt-0.5 flex items-center gap-1.5'>
            <span className='px-1.5 py-0.5 bg-indigo-50 border border-indigo-100 rounded text-indigo-700 text-[9px]'>{route.routeCode}</span>
          </p>
        </div>
        <div className={cn('flex h-9 w-9 shrink-0 items-center justify-center rounded-full border text-[11px] font-extrabold shadow-sm', scoreColor)}>
          {score.toFixed(0)}
        </div>
      </div>
      
      <div className='grid grid-cols-3 gap-1.5 p-2 rounded-xl bg-slate-50/50 border border-slate-100 text-[10px] text-slate-500'>
        <div>
          <span className='text-slate-400 block'>Students</span>
          <span className='font-bold text-slate-700'>{route.studentCount}</span>
        </div>
        <div>
          <span className='text-slate-400 block'>Stops</span>
          <span className='font-bold text-slate-700'>{route.stopCount}</span>
        </div>
        <div>
          <span className='text-slate-400 block'>Capacity</span>
          <span className='font-bold text-slate-700'>{route.requiredCapacity ?? '—'}</span>
        </div>
        <div className='col-span-1'>
          <span className='text-slate-400 block'>Distance</span>
          <span className='font-bold text-slate-700'>{route.totalDistanceKm != null ? route.totalDistanceKm.toFixed(1) + ' km' : '—'}</span>
        </div>
        <div className='col-span-2'>
          <span className='text-slate-400 block'>Duration</span>
          <span className='font-bold text-slate-700'>{route.totalDurationMin != null ? route.totalDurationMin + ' min' : '—'}</span>
        </div>
      </div>

      {(route.blockingIssueCount > 0 || route.warningIssueCount > 0) && (
        <div className='flex flex-wrap gap-1.5 text-[10px]'>
          {route.blockingIssueCount > 0 && <span className='inline-flex items-center gap-1 rounded bg-red-50 text-red-700 px-1.5 py-0.5 border border-red-100 font-bold'>🚫 {route.blockingIssueCount} blocking</span>}
          {route.warningIssueCount > 0 && <span className='inline-flex items-center gap-1 rounded bg-amber-50 text-amber-700 px-1.5 py-0.5 border border-amber-100 font-bold'>⚠️ {route.warningIssueCount} warning(s)</span>}
        </div>
      )}

      {route.issues.length > 0 && (
        <div className='pt-1 border-t border-slate-100/50'>
          <button onClick={e => { e.stopPropagation(); setShowIssues(v => !v); }}
            className='text-[10px] font-bold text-slate-400 hover:text-slate-900 flex items-center gap-1'>
            {showIssues ? '▲ Hide issues' : '▼ Show issues'} ({route.issues.length})
          </button>
          {showIssues && <div className='mt-2 flex flex-col gap-1.5'>{route.issues.map((issue, i) => <IssueBadge key={i} issue={issue} />)}</div>}
        </div>
      )}
      
      {isSelected && (
        <div className='flex items-center justify-end text-[10px] font-extrabold text-[#C81E3A] border-t border-slate-100/50 pt-2'>
          <span>▶ Selected — detail below</span>
        </div>
      )}
    </div>
  );
}

/* ── Route Detail Panel ─────────────────────────────────────────── */

function RouteDetailPanel({ routeId }: { routeId: number }) {
  const { data: routeDetail, isLoading } = useGetRouteByIdQuery(routeId);
  const [reorderStops] = useReorderRouteStopsMutation();
  const [removeStop, { isLoading: removingStop }] = useRemoveRouteStopMutation();
  const [removeStudent, { isLoading: removingStudent }] = useRemoveRouteStudentMutation();
  const detail = routeDetail?.data;

  // ── Confirm dialogs ──────────────────────────────────────────────────────
  const stopConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const stopId = stopConfirm.confirmState.payload as number;
      try { await removeStop({ routeId, stopId }).unwrap(); } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message ?? 'Failed to remove stop');
      }
    },
    isLoading: removingStop,
  });

  const studentConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const { studentId, subscriptionId } = studentConfirm.confirmState.payload as {
        studentId: number;
        subscriptionId: number;
      };
      try { await removeStudent({ routeId, studentId, subscriptionId }).unwrap(); } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message ?? 'Failed to remove student');
      }
    },
    isLoading: removingStudent,
  });

  if (isLoading || !detail) return <div className='py-8 text-center text-sm text-slate-400'>Loading route detail…</div>;

  const stops = detail.stops ?? [];
  const students = detail.students ?? [];
  const editable = !['COMPLETED', 'IN_PROGRESS', 'TRIP_CREATED', 'CANCELLED'].includes(detail.route.status);

  const swap = async (i: number, j: number) => {
    // Only swap middle stops — find their indices in the full stops array
    const middleStops = stops.filter(s => s.stopPurpose !== 'START_TERMINAL' && s.stopPurpose !== 'END_TERMINAL');
    const ids = middleStops.map(s => s.id);
    [ids[i], ids[j]] = [ids[j], ids[i]];
    try {
      await reorderStops({ id: routeId, orderedStopIds: ids }).unwrap();
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? 'Failed to reorder stops');
    }
  };

  const delStop = (stopId: number) => {
    stopConfirm.requestConfirm({
      title: 'Remove stop?',
      description: 'This stop and all students assigned to it will be removed from the route.',
      confirmLabel: 'Remove stop',
      variant: 'danger',
      payload: stopId,
    });
  };

  const delStudent = (studentId: number, subscriptionId: number) => {
    studentConfirm.requestConfirm({
      title: 'Remove student?',
      description: 'The student will be unassigned from this route and returned to the unassigned pool.',
      confirmLabel: 'Remove',
      variant: 'danger',
      payload: { studentId, subscriptionId },
    });
  };

  const middleStops = stops.filter(s => s.stopPurpose !== 'START_TERMINAL' && s.stopPurpose !== 'END_TERMINAL');

  return (
    <>
      <div className='p-5 space-y-4 border-t border-slate-200 bg-slate-50/20'>
        <div className='border-b border-slate-100 pb-2'>
          <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
            <Route className='h-4 w-4 text-emerald-600 shrink-0' />
            {detail.route.routeName} — Detail
          </h3>
        </div>
        
        <div>
          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2'>Stops ({stops.length})</p>
          {stops.length === 0 ? (
            <p className='text-xs text-slate-300'>No stops</p>
          ) : (
            <div className='space-y-1.5'>
              {stops.map((stop, i) => {
                const isTerminal = stop.stopPurpose === 'START_TERMINAL' || stop.stopPurpose === 'END_TERMINAL';
                const middleIndex = isTerminal ? -1 : middleStops.findIndex(s => s.id === stop.id);
                const isFirstMiddle = middleIndex === 0;
                const isLastMiddle = middleIndex === middleStops.length - 1;
                const terminalLabel = stop.stopPurpose === 'START_TERMINAL' ? 'Departure' : stop.stopPurpose === 'END_TERMINAL' ? 'Destination' : null;
                return (
                  <div key={stop.id} className={cn(
                    'flex items-center gap-2 rounded-xl border px-3 py-2 text-xs',
                    isTerminal ? 'border-blue-105 bg-blue-50/50 text-blue-800' : 'border-slate-200 bg-white text-slate-700'
                  )}>
                    {isTerminal ? (
                      <span className='flex h-5 items-center justify-center rounded bg-blue-100 px-1.5 text-[9px] font-bold text-blue-700'>{terminalLabel}</span>
                    ) : (
                      <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-650'>{stop.stopOrder + 1}</span>
                    )}
                    <span className='flex-1 font-semibold truncate'>{stop.pickupPointName || `Stop #${stop.id}`}</span>
                    {!isTerminal && <span className='text-[10px] text-slate-450'>{stop.estimatedStudentCount ?? 0} students</span>}
                    {editable && !isTerminal && (
                      <div className='flex gap-0.5' onClick={e => e.stopPropagation()}>
                        <Button variant='ghost' size='icon' className='h-6 w-6' onClick={() => swap(middleIndex, middleIndex - 1)} disabled={isFirstMiddle}><ChevronUp className='h-3 w-3' /></Button>
                        <Button variant='ghost' size='icon' className='h-6 w-6' onClick={() => swap(middleIndex, middleIndex + 1)} disabled={isLastMiddle}><ChevronDown className='h-3 w-3' /></Button>
                        <Button variant='ghost' size='icon' className='h-6 w-6 text-red-500 hover:text-red-700' onClick={() => delStop(stop.id)}><Trash2 className='h-3 w-3' /></Button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div>
          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2 mt-4'>Students ({students.length})</p>
          {students.length === 0 ? (
            <p className='text-xs text-slate-300'>No students assigned to stops</p>
          ) : (
            <div className='space-y-1.5'>
              {students.map(ps => (
                <div key={`${ps.studentId}-${ps.subscriptionId}-${ps.serviceAction}`} className='flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs text-slate-700'>
                  <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-indigo-50 text-indigo-650'>
                    <Users className='h-3 w-3' />
                  </span>
                  <span className='flex-1 font-semibold truncate'>{ps.studentName || `Student #${ps.studentId}`}</span>
                  <span className='text-[10px] text-slate-450'>{ps.serviceAction} · {ps.stopName ?? '—'}</span>
                  {editable && (
                    <Button variant='ghost' size='icon' className='h-6 w-6 text-red-500 hover:text-red-700' onClick={() => delStudent(ps.studentId, ps.subscriptionId)}><Trash2 className='h-3 w-3' /></Button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Confirm dialogs */}
      <SchoolBusConfirmDialog {...stopConfirm.dialogProps} />
      <SchoolBusConfirmDialog {...studentConfirm.dialogProps} />
    </>
  );
}

/* ── Main Results Panel ─────────────────────────────────────────── */

interface PlanningResultsPanelProps {
  preview: SchoolBusPlanningPreview | null;
  greedyResult: SchoolBusGreedyGenerateResult | null;
  /** Routes loaded from backend (survives refresh, works for both GREEDY + MANUAL) */
  sessionRoutes: SchoolBusRoute[];
  activeSession: SchoolBusPlanningSession | null;
  eligibleStudents: SchoolBusEligibleStudent[];
  selectedRouteId: number | null;
  onSelectRoute: (id: number | null) => void;
  onCreateManualRoute: (req: CreateRouteInSessionRequest) => void;
  creatingRoute?: boolean;
}

export function PlanningResultsPanel({
  preview,
  greedyResult,
  sessionRoutes,
  activeSession,
  eligibleStudents,
  selectedRouteId,
  onSelectRoute,
  onCreateManualRoute,
  creatingRoute,
}: PlanningResultsPanelProps) {

  const isManual = activeSession?.planningMethod === 'MANUAL';

  /* True empty state — no session, no preview, nothing at all */
  if (!preview && !greedyResult && sessionRoutes.length === 0 && !activeSession) {
    return (
      <div className='flex h-full flex-col items-center justify-center gap-5 p-6'>
        <div className='flex h-14 w-14 items-center justify-center rounded-2xl border border-dashed border-slate-250 bg-slate-50 shadow-sm'>
          <MapPin className='h-7 w-7 text-slate-350' />
        </div>
        <div className='text-center'>
          <p className='text-sm font-semibold text-slate-700'>No planning session yet</p>
          <p className='mt-1 text-xs text-slate-450 max-w-[220px]'>
            Complete the Planning Context on the left and click Preview Demand to get started.
          </p>
        </div>
        <ol className='space-y-2.5 text-left border-t border-slate-100 pt-4 w-full max-w-[260px]'>
          {([
            { text: 'Fill in context values on the left', icon: '1' },
            { text: 'Click Preview to see draft map', icon: '2' },
            { text: 'Start session & configure routes', icon: '3' }
          ] as const).map((step, i) => (
            <li key={i} className='flex items-start gap-2.5'>
              <span className='mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-500 border border-slate-200'>{step.icon}</span>
              <span className='text-xs leading-5 text-slate-500 font-semibold'>{step.text}</span>
            </li>
          ))}
        </ol>
      </div>
    );
  }

  return (
    <div className='space-y-0 divide-y divide-slate-150'>
      {/* Demand Preview */}
      {preview && !greedyResult && sessionRoutes.length === 0 && (
        <div className='p-5 space-y-4'>
          <div className='border-b border-slate-100 pb-2.5'>
            <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
              <Users className='h-4 w-4 text-violet-650 shrink-0' />
              Demand Preview — {preview.totalEligibleStudents} eligible
            </h3>
          </div>
          <div className='grid grid-cols-2 gap-2 text-xs'>
            <div className='p-3 rounded-xl bg-slate-50 border border-slate-100 shadow-sm'>
              <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wide'>Eligible Students</p>
              <p className='text-lg font-extrabold text-slate-800 mt-0.5'>{preview.totalEligibleStudents}</p>
            </div>
            <div className='p-3 rounded-xl bg-slate-50 border border-slate-100 shadow-sm'>
              <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wide'>Pickup Points</p>
              <p className='text-lg font-extrabold text-slate-800 mt-0.5'>{preview.totalEligiblePickupPoints}</p>
            </div>
          </div>
          {preview.issues.length > 0 && <div className='mb-4 flex flex-wrap gap-1.5'>{preview.issues.map((issue, i) => <IssueBadge key={i} issue={issue} />)}</div>}
          
          <div className='pt-2'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2.5'>Pickup Points</p>
            <div className='grid gap-2 sm:grid-cols-2'>
              {preview.eligiblePickupPoints.map(pp => (
                <div key={pp.pickupPointId} className={cn(
                  'rounded-xl border p-3 flex flex-col justify-between gap-1 shadow-sm transition-all',
                  pp.hasWindow ? 'border-emerald-100 bg-emerald-50/20' : 'border-amber-100 bg-amber-50/20'
                )}>
                  <div>
                    <p className='text-xs font-bold text-slate-800 flex items-center gap-1.5 truncate'>
                      <MapPin className='h-3.5 w-3.5 text-sky-600 shrink-0' />
                      {pp.pickupPointName}
                    </p>
                  </div>
                  <div className='flex items-center justify-between mt-2 pt-2 border-t border-slate-100/50 text-[10px] text-slate-500'>
                    <span className='font-bold'>{pp.studentCount} student(s)</span>
                    <span className={cn(
                      'inline-flex items-center rounded px-1.5 py-0.5 font-bold border',
                      pp.hasWindow
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                        : 'bg-amber-50 text-amber-700 border-amber-100'
                    )}>
                      {pp.hasWindow ? '✅ Window' : '⚠️ No window'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* MANUAL mode: Session routes from backend + create new route + demand assignment */}
      {isManual && activeSession && (
        <div className='p-5 space-y-4'>
          <div className='border-b border-slate-100 pb-2.5 flex items-center justify-between'>
            <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
              <Route className='h-4 w-4 text-blue-600 shrink-0' />
              Manual Routes ({sessionRoutes.length})
            </h3>
          </div>
          <CreateRoutePanel
            session={activeSession}
            onSubmit={onCreateManualRoute}
            submitting={creatingRoute}
          />
          {sessionRoutes.length === 0 ? (
            <div className='flex flex-col items-center justify-center p-8 border border-dashed border-slate-200 rounded-2xl bg-slate-50/50 text-center'>
              <Route className='h-8 w-8 text-slate-300 stroke-[1.5] mb-2' />
              <p className='text-xs font-bold text-slate-500'>No routes yet</p>
              <p className='text-[10px] text-slate-400 mt-0.5'>Create your first route above to get started.</p>
            </div>
          ) : (
            <div className='space-y-3'>
              {sessionRoutes.map(r => (
                <SessionRouteCard
                  key={r.id}
                  route={r}
                  onSelect={id => onSelectRoute(selectedRouteId === id ? null : id)}
                  isSelected={selectedRouteId === r.id}
                />
              ))}
            </div>
          )}
        </div>
      )}

      {/* MANUAL mode: Demand assignment panel — shown when a route is selected */}
      {isManual && activeSession && (
        <ManualDemandAssignPanel
          selectedRoute={sessionRoutes.find(r => r.id === selectedRouteId) ?? null}
          eligibleStudents={eligibleStudents}
          sessionId={activeSession.id}
        />
      )}

      {/* GREEDY mode: waiting for user to generate */}
      {!isManual && activeSession && !greedyResult && sessionRoutes.length === 0 && (
        <div className='p-5'>
          <div className='rounded-2xl border border-sky-100 bg-sky-50/40 p-5 text-center shadow-sm'>
            <p className='text-xs font-bold text-sky-700 flex items-center justify-center gap-1.5'>
              <Info className='h-4 w-4' />
              Ready to generate routes
            </p>
            <p className='mt-2 text-xs text-slate-550 leading-5'>
              Click <span className='font-semibold text-slate-800'>"Generate"</span> in the session panel on the left to auto-generate routes using the greedy algorithm.
            </p>
          </div>
        </div>
      )}

      {/* GREEDY mode: show quality cards from greedyResult, or reload from sessionRoutes */}
      {!isManual && greedyResult && (
        <>
          <div className='p-5 space-y-4'>
            <div className='border-b border-slate-100 pb-2.5'>
              <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
                <Route className='h-4 w-4 text-emerald-600 shrink-0' />
                Generated Routes ({greedyResult.routes.length})
              </h3>
            </div>
            {greedyResult.sessionIssues.filter(i => i.severity !== 'INFO').length > 0 && (
              <div className='mb-3 flex flex-wrap gap-1.5'>{greedyResult.sessionIssues.map((issue, i) => <IssueBadge key={i} issue={issue} />)}</div>
            )}
            <div className='space-y-3'>
              {greedyResult.routes.map(r => (
                <RouteCard key={r.routeId} route={r}
                  onSelect={id => onSelectRoute(selectedRouteId === id ? null : id)}
                  isSelected={selectedRouteId === r.routeId} />
              ))}
            </div>
          </div>

          {greedyResult.totalUnassignedStudents > 0 && (
            <div className='p-5 space-y-4'>
              <div className='border-b border-slate-100 pb-2.5'>
                <h3 className='text-sm font-bold text-slate-955 flex items-center gap-2'>
                  <AlertTriangle className='h-4 w-4 text-amber-600 shrink-0' />
                  Unassigned Students ({greedyResult.totalUnassignedStudents})
                </h3>
              </div>
              <div className='space-y-1.5'>
                {greedyResult.unassignedStudents.map(s => (
                  <div key={s.studentId} className='flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/50 px-3 py-2 text-xs text-slate-700 shadow-sm'>
                    <span className='font-semibold text-slate-800'>{s.studentName}</span>
                    <span className='text-[10px] text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded border font-semibold'>{s.specialNote ?? 'No nearby stop'}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {/* GREEDY mode: show session routes if no in-memory greedyResult (page refresh) */}
      {!isManual && !greedyResult && sessionRoutes.length > 0 && (
        <div className='p-5 space-y-4'>
          <div className='border-b border-slate-100 pb-2.5'>
            <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
              <Route className='h-4 w-4 text-blue-600 shrink-0' />
              Session Routes ({sessionRoutes.length})
            </h3>
          </div>
          <div className='space-y-3'>
            {sessionRoutes.map(r => (
              <SessionRouteCard
                key={r.id}
                route={r}
                onSelect={id => onSelectRoute(selectedRouteId === id ? null : id)}
                isSelected={selectedRouteId === r.id}
              />
            ))}
          </div>
        </div>
      )}

      {/* Route detail (shared — works for both GREEDY detail and MANUAL selected route) */}
      {selectedRouteId && <RouteDetailPanel routeId={selectedRouteId} />}
    </div>
  );
}
