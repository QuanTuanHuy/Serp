'use client';

import React, { useState } from 'react';
import { ChevronUp, ChevronDown, Trash2, AlertTriangle, Info, Ban, MapPin, Users, Plus } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SchoolBusSection } from '../SchoolBusSection';
import { SchoolBusConfirmDialog, useSchoolBusConfirm } from '../SchoolBusConfirmDialog';
import { schoolBusUi } from '../../theme';
import { ManualDemandAssignPanel } from './ManualDemandAssignPanel';
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
    (!needsEndDepot   || form.endDepotId   !== '');

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

  const inputCls = 'mt-1 w-full rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900/10 focus:border-slate-900';
  const labelCls = 'text-xs font-medium text-slate-500';

  const DepotSelector = ({ field, value }: { field: 'startDepotId' | 'endDepotId'; value: number | '' }) => (
    <div className='mt-2'>
      <label className={labelCls}>Depot *</label>
      <select
        className={inputCls}
        value={value}
        onChange={e => setForm(p => ({ ...p, [field]: e.target.value === '' ? '' : Number(e.target.value) }))}
      >
        <option value=''>— Select depot —</option>
        {depots.map((d: import('../../types').SchoolBusDepot) => (
          <option key={d.id} value={d.id}>{d.name}{d.address ? ` (${d.address})` : ''}</option>
        ))}
      </select>
      {value === '' && <p className='mt-1 text-[11px] text-red-500'>Depot selection is required.</p>}
    </div>
  );

  return (
    <div>
      <Button size='sm' variant='outline' onClick={() => setOpen(v => !v)} className='mb-3'>
        <Plus className='mr-1 h-3 w-3' />
        {open ? 'Close' : 'New Route'}
      </Button>
      {open && (
        <form onSubmit={handleSubmit} className='mb-4 rounded-2xl border border-slate-200 bg-slate-50/60 p-4 space-y-3'>

          {/* No-depot warning */}
          {!hasDepots && (
            <div className='flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50/70 px-3 py-2 text-xs font-medium text-amber-700'>
              <AlertTriangle className='mt-0.5 h-3.5 w-3.5 shrink-0' />
              Please create a depot before creating depot-based routes.
            </div>
          )}

          {/* Route Name */}
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

          {/* Direction */}
          <div>
            <label className={labelCls}>Direction</label>
            <select
              className={inputCls}
              value={form.routeDirection}
              onChange={e => handleDirectionChange(e.target.value as DirectionType)}
            >
              <option value='OUTBOUND'>OUTBOUND (Depot → School)</option>
              <option value='RETURN'>RETURN (School → Depot)</option>
            </select>
          </div>

          {/* Start Location */}
          <div className='rounded-xl border border-slate-200 bg-white p-3 space-y-2'>
            <p className='text-xs font-semibold text-slate-600'>🚌 Start Location</p>
            <div className='flex gap-4'>
              {(['DEPOT', 'SCHOOL'] as LocationType[]).map(t => (
                <label key={t} className='flex items-center gap-1.5 cursor-pointer text-sm'>
                  <input
                    type='radio'
                    name='startLocationType'
                    value={t}
                    checked={form.startLocationType === t}
                    onChange={() => setForm(p => ({ ...p, startLocationType: t, startDepotId: '' }))}
                    className='accent-slate-900'
                  />
                  {t === 'DEPOT' ? 'Depot' : 'School'}
                </label>
              ))}
            </div>
            {needsStartDepot
              ? <DepotSelector field='startDepotId' value={form.startDepotId} />
              : <p className='text-xs text-slate-400'>Starts at school (ID: {session.schoolId})</p>
            }
          </div>
 
          {/* End Location */}
          <div className='rounded-xl border border-slate-200 bg-white p-3 space-y-2'>
            <p className='text-xs font-semibold text-slate-600'>🏫 End Location</p>
            <div className='flex gap-4'>
              {(['SCHOOL', 'DEPOT'] as LocationType[]).map(t => (
                <label key={t} className='flex items-center gap-1.5 cursor-pointer text-sm'>
                  <input
                    type='radio'
                    name='endLocationType'
                    value={t}
                    checked={form.endLocationType === t}
                    onChange={() => setForm(p => ({ ...p, endLocationType: t, endDepotId: '' }))}
                    className='accent-slate-900'
                  />
                  {t === 'DEPOT' ? 'Depot' : 'School'}
                </label>
              ))}
            </div>
            {needsEndDepot
              ? <DepotSelector field='endDepotId' value={form.endDepotId} />
              : <p className='text-xs text-slate-400'>Ends at school (ID: {session.schoolId})</p>
            }
          </div>

          {/* Notes */}
          <div>
            <label className={labelCls}>Notes</label>
            <input
              className={inputCls}
              value={form.planningNotes}
              onChange={e => setForm(p => ({ ...p, planningNotes: e.target.value }))}
              placeholder='Optional notes'
            />
          </div>

          <div className='flex justify-end gap-2'>
            <Button type='button' size='sm' variant='ghost' onClick={() => setOpen(false)}>Cancel</Button>
            <Button type='submit' size='sm' disabled={submitting || !canSubmit}>
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
        'cursor-pointer rounded-2xl border p-4 transition',
        isSelected ? 'border-slate-300 bg-slate-50 shadow-sm' : schoolBusUi.interactiveCard
      )}
    >
      <div className='flex items-start justify-between'>
        <div>
          <p className='text-sm font-bold text-slate-900'>{route.routeName}</p>
          <p className='text-xs text-slate-400'>{route.routeCode} · {route.routeDirection}</p>
        </div>
        <span className={cn(
          'rounded-full px-2 py-0.5 text-[10px] font-semibold',
          route.status === 'PUBLISHED' ? 'bg-emerald-100 text-emerald-700' :
          route.status === 'CANCELLED' ? 'bg-slate-100 text-slate-500' : 'bg-amber-100 text-amber-700'
        )}>{route.status}</span>
      </div>
      <p className='mt-1 text-[11px] text-slate-400'>{route.serviceDate}</p>
      {isSelected && <p className='mt-2 text-[11px] font-semibold text-slate-900'>▶ Selected — detail below</p>}
    </div>
  );
}

/* ── Issue badge ────────────────────────────────────────────────── */

const severityStyle: Record<string, string> = {
  BLOCKING: 'border-red-200 bg-red-50 text-red-700',
  WARNING: 'border-amber-200 bg-amber-50 text-amber-700',
  INFO: 'border-sky-200 bg-sky-50 text-sky-700',
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
  const scoreColor = score >= 80 ? 'text-emerald-600 border-emerald-300' : score >= 50 ? 'text-amber-600 border-amber-300' : 'text-red-600 border-red-300';
 
  return (
    <div onClick={() => onSelect(route.routeId)}
      className={cn('cursor-pointer rounded-2xl border p-4 transition', isSelected ? 'border-slate-300 bg-slate-50 shadow-sm' : schoolBusUi.interactiveCard)}>
      <div className='flex items-start justify-between'>
        <div>
          <p className='text-sm font-bold text-slate-900'>{route.routeName}</p>
          <p className='text-xs text-slate-400'>{route.routeCode}</p>
        </div>
        <div className={cn('flex h-11 w-11 items-center justify-center rounded-full border-2 text-sm font-extrabold', scoreColor)}>
          {score.toFixed(0)}
        </div>
      </div>
      <div className='mt-3 flex flex-wrap gap-x-5 gap-y-1 text-xs'>
        {([['Students', route.studentCount], ['Stops', route.stopCount], ['Capacity', route.requiredCapacity ?? '—'],
          ['Dist', route.totalDistanceKm != null ? route.totalDistanceKm.toFixed(1) + ' km' : '—'],
          ['Time', route.totalDurationMin != null ? route.totalDurationMin + ' min' : '—'],
        ] as [string, string | number][]).map(([l, v]) => (
          <div key={l}><span className='text-slate-400'>{l}: </span><span className='font-semibold text-slate-700'>{v}</span></div>
        ))}
        {route.blockingIssueCount > 0 && <span className='font-semibold text-red-600'>🚫 {route.blockingIssueCount} blocking</span>}
        {route.warningIssueCount > 0 && <span className='font-semibold text-amber-600'>⚠️ {route.warningIssueCount} warnings</span>}
      </div>
      {route.issues.length > 0 && (
        <div className='mt-2'>
          <button onClick={e => { e.stopPropagation(); setShowIssues(v => !v); }}
            className='text-[11px] font-medium text-slate-400 hover:text-slate-900'>
            {showIssues ? '▲ Hide' : '▼ Show'} {route.issues.length} issue(s)
          </button>
          {showIssues && <div className='mt-2 flex flex-col gap-1'>{route.issues.map((issue, i) => <IssueBadge key={i} issue={issue} />)}</div>}
        </div>
      )}
      {isSelected && <p className='mt-2 text-[11px] font-semibold text-slate-900'>▶ Selected — detail below</p>}
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
      <SchoolBusSection title={`${detail.route.routeName} — Detail`}>
        <p className='mb-3 text-xs font-medium text-slate-400'>Stops ({stops.length})</p>
        {stops.length === 0 && <p className='text-xs text-slate-300'>No stops</p>}
        <div className='space-y-1'>
          {stops.map((stop, i) => {
            const isTerminal = stop.stopPurpose === 'START_TERMINAL' || stop.stopPurpose === 'END_TERMINAL';
            const middleIndex = isTerminal ? -1 : middleStops.findIndex(s => s.id === stop.id);
            const isFirstMiddle = middleIndex === 0;
            const isLastMiddle = middleIndex === middleStops.length - 1;
            const terminalLabel = stop.stopPurpose === 'START_TERMINAL' ? 'Departure' : stop.stopPurpose === 'END_TERMINAL' ? 'Destination' : null;
            return (
              <div key={stop.id} className={cn('flex items-center gap-2 rounded-xl border px-3 py-2', isTerminal ? 'border-blue-200 bg-blue-50/60' : 'border-slate-100 bg-slate-50/60')}>
                {isTerminal
                  ? <span className='flex h-6 items-center justify-center rounded-full bg-blue-100 px-2 text-[10px] font-bold text-blue-600'>{terminalLabel}</span>
                  : <span className='flex h-6 w-6 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-700'>{stop.stopOrder + 1}</span>
                }
                <span className='flex-1 text-sm text-slate-700'>{stop.pickupPointName || `Stop #${stop.id}`}</span>
                {!isTerminal && <span className='text-[11px] text-slate-400'>{stop.estimatedStudentCount ?? 0} students</span>}
                {editable && !isTerminal && (
                  <div className='flex gap-0.5'>
                    <Button variant='ghost' size='icon' className='h-6 w-6' onClick={() => swap(middleIndex, middleIndex - 1)} disabled={isFirstMiddle}><ChevronUp className='h-3 w-3' /></Button>
                    <Button variant='ghost' size='icon' className='h-6 w-6' onClick={() => swap(middleIndex, middleIndex + 1)} disabled={isLastMiddle}><ChevronDown className='h-3 w-3' /></Button>
                    <Button variant='ghost' size='icon' className='h-6 w-6 text-red-500 hover:text-red-700' onClick={() => delStop(stop.id)}><Trash2 className='h-3 w-3' /></Button>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <p className='mb-3 mt-5 text-xs font-medium text-slate-400'>Students ({students.length})</p>
        {students.length === 0 && <p className='text-xs text-slate-300'>No students</p>}
        <div className='space-y-1'>
          {students.map(ps => (
            <div key={`${ps.studentId}-${ps.subscriptionId}-${ps.serviceAction}`} className='flex items-center gap-2 rounded-xl border border-slate-100 bg-slate-50/60 px-3 py-2'>
              <span className='flex-1 text-sm text-slate-700'>{ps.studentName || `Student #${ps.studentId}`}</span>
              <span className='text-[11px] text-slate-400'>{ps.serviceAction} · {ps.stopName ?? '—'}</span>
              {editable && (
                <Button variant='ghost' size='icon' className='h-6 w-6 text-red-500 hover:text-red-700' onClick={() => delStudent(ps.studentId, ps.subscriptionId)}><Trash2 className='h-3 w-3' /></Button>
              )}
            </div>
          ))}
        </div>
      </SchoolBusSection>

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
        <div className='flex h-14 w-14 items-center justify-center rounded-2xl border-2 border-dashed border-slate-200 bg-slate-50'>
          <MapPin className='h-7 w-7 text-slate-300' />
        </div>
        <div className='text-center'>
          <p className='text-sm font-semibold text-slate-600'>No planning session yet</p>
          <p className='mt-1 text-xs text-slate-400 max-w-[220px]'>
            Complete the Planning Context on the left and click Preview Demand to get started.
          </p>
        </div>
        <ol className='space-y-2 text-left'>
          {(['Fill in school, schedule, date & direction', 'Click Preview Demand', 'Start a session & generate routes'] as const).map((step, i) => (
            <li key={i} className='flex items-start gap-2.5'>
              <span className='mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-400 ring-1 ring-slate-200'>{i + 1}</span>
              <span className='text-xs leading-5 text-slate-500'>{step}</span>
            </li>
          ))}
        </ol>
      </div>
    );
  }

  return (
    <div className='space-y-0 divide-y divide-slate-100'>
      {/* Demand Preview */}
      {preview && !greedyResult && sessionRoutes.length === 0 && (
        <SchoolBusSection title={`Demand Preview — ${preview.totalEligibleStudents} eligible`}>
          <div className='mb-4 flex gap-6'>
            <div><p className='text-[11px] text-slate-400'>Eligible Students</p><p className='text-xl font-bold text-slate-900'>{preview.totalEligibleStudents}</p></div>
            <div><p className='text-[11px] text-slate-400'>Pickup Points</p><p className='text-xl font-bold text-slate-900'>{preview.totalEligiblePickupPoints}</p></div>
          </div>
          {preview.issues.length > 0 && <div className='mb-4 flex flex-wrap gap-1.5'>{preview.issues.map((issue, i) => <IssueBadge key={i} issue={issue} />)}</div>}
          <p className='mb-2 text-xs font-medium text-slate-400'>Pickup Points</p>
          <div className='grid gap-2 sm:grid-cols-2'>
            {preview.eligiblePickupPoints.map(pp => (
              <div key={pp.pickupPointId} className={cn('rounded-xl border p-3', pp.hasWindow ? 'border-emerald-200 bg-emerald-50/40' : 'border-amber-200 bg-amber-50/40')}>
                <p className='text-sm font-semibold text-slate-800'>{pp.pickupPointName}</p>
                <p className='mt-0.5 text-xs text-slate-500'>
                  <Users className='mr-1 inline h-3 w-3' />{pp.studentCount} students · {pp.hasWindow ? '✅ Window' : '⚠️ No window'}
                </p>
              </div>
            ))}
          </div>
        </SchoolBusSection>
      )}

      {/* MANUAL mode: Session routes from backend + create new route + demand assignment */}
      {isManual && activeSession && (
        <SchoolBusSection title={`Manual Routes (${sessionRoutes.length})`}>
          <CreateRoutePanel
            session={activeSession}
            onSubmit={onCreateManualRoute}
            submitting={creatingRoute}
          />
          {sessionRoutes.length === 0 && (
            <p className='text-xs text-slate-400'>No routes yet — create your first route above.</p>
          )}
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
        </SchoolBusSection>
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
        <div className='m-4 rounded-2xl border border-sky-100 bg-sky-50/60 p-5 text-center'>
          <p className='text-sm font-semibold text-sky-700'>Ready to generate routes</p>
          <p className='mt-1 text-xs text-slate-500 leading-5'>
            Click <span className='font-semibold text-slate-700'>"Generate Routes"</span> in the session panel on the left to auto-generate routes using the greedy algorithm.
          </p>
        </div>
      )}

      {/* GREEDY mode: show quality cards from greedyResult, or reload from sessionRoutes */}
      {!isManual && greedyResult && (
        <>
          <SchoolBusSection title={`Generated Routes (${greedyResult.routes.length})`}>
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
          </SchoolBusSection>

          {greedyResult.totalUnassignedStudents > 0 && (
            <SchoolBusSection title={`Unassigned Students (${greedyResult.totalUnassignedStudents})`}>
              <div className='space-y-1'>
                {greedyResult.unassignedStudents.map(s => (
                  <div key={s.studentId} className='flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/80 px-3 py-2'>
                    <span className='text-sm font-medium text-slate-700'>{s.studentName}</span>
                    <span className='text-[11px] text-slate-400'>{s.specialNote ?? 'No relevant point'}</span>
                  </div>
                ))}
              </div>
            </SchoolBusSection>
          )}
        </>
      )}

      {/* GREEDY mode: show session routes if no in-memory greedyResult (page refresh) */}
      {!isManual && !greedyResult && sessionRoutes.length > 0 && (
        <SchoolBusSection title={`Session Routes (${sessionRoutes.length})`}>
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
        </SchoolBusSection>
      )}

      {/* Route detail (shared — works for both GREEDY detail and MANUAL selected route) */}
      {selectedRouteId && <RouteDetailPanel routeId={selectedRouteId} />}
    </div>
  );
}
