'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { ChevronUp, ChevronDown, Trash2, AlertTriangle, Info, Ban, MapPin, Users, Plus, Route, Clock, Calendar, CheckCircle2, XCircle, AlertCircle, Loader2, Navigation } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDate } from '../../utils';
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
  useGetSchoolByIdQuery,
  useGetSchoolScheduleByIdQuery,
  useGetLatestRouteCalculationTraceQuery,
  useGetRouteObjectiveScoreQuery,
  useRecalculateRouteObjectiveScoreMutation,
  useGetSessionObjectiveScoreQuery,
  useRecalculateSessionObjectiveScoreMutation,
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

function CreateRoutePanel({
  session,
  onSubmit,
  submitting,
  contextDepotId,
}: {
  session: SchoolBusPlanningSession;
  onSubmit: (req: CreateRouteInSessionRequest) => void;
  submitting?: boolean;
  contextDepotId?: number | '';
}) {
  const [open, setOpen] = useState(false);

  const initDir = (session.routeDirection as DirectionType) ?? 'OUTBOUND';
  const [form, setForm] = useState<CreateRouteFormState>({
    routeName: '',
    routeDirection: initDir,
    startLocationType: initDir === 'OUTBOUND' ? 'DEPOT' : 'SCHOOL',
    startDepotId: initDir === 'OUTBOUND' && contextDepotId ? Number(contextDepotId) : '',
    endLocationType: initDir === 'OUTBOUND' ? 'SCHOOL' : 'DEPOT',
    endDepotId: initDir === 'RETURN' && contextDepotId ? Number(contextDepotId) : '',
    planningNotes: '',
  });

  useEffect(() => {
    if (initDir === 'OUTBOUND') {
      setForm(p => ({
        ...p,
        routeDirection: initDir,
        startLocationType: 'DEPOT',
        endLocationType: 'SCHOOL',
        startDepotId: p.startDepotId === '' && contextDepotId ? Number(contextDepotId) : p.startDepotId,
        endDepotId: ''
      }));
    } else {
      setForm(p => ({
        ...p,
        routeDirection: initDir,
        startLocationType: 'SCHOOL',
        endLocationType: 'DEPOT',
        startDepotId: '',
        endDepotId: p.endDepotId === '' && contextDepotId ? Number(contextDepotId) : p.endDepotId
      }));
    }
  }, [initDir, contextDepotId]);

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
                placeholder={initDir === 'OUTBOUND' ? 'e.g. Morning Outbound Route A' : 'e.g. Afternoon Return Route A'}
                required
              />
            </div>
            <div>
              <label className={labelCls}>Direction</label>
              <div className='w-full rounded-xl border border-slate-150 bg-slate-50/50 px-3 py-2 text-xs font-semibold text-slate-700 shadow-none'>
                {initDir === 'OUTBOUND' ? (
                  <span>OUTBOUND — Depot → School <span className='text-[10px] font-medium text-slate-400 ml-1'>(Inherited from session context)</span></span>
                ) : (
                  <span>RETURN — School → Depot <span className='text-[10px] font-medium text-slate-400 ml-1'>(Inherited from session context)</span></span>
                )}
              </div>
            </div>
          </div>

          {/* Group 2: Start & End Terminals */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>2. Start & End Terminals</p>
            
            {/* Start location */}
            <div className='space-y-2 pt-1.5'>
              <label className={labelCls}>Start Location ({initDir === 'OUTBOUND' ? 'DEPOT' : 'SCHOOL'})</label>
              {initDir === 'OUTBOUND' ? (
                hasDepots ? (
                  <div className='mt-1'>
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
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100 font-medium'>
                  Starts at school: <span className='font-semibold text-slate-700'>{session.schoolName}</span> (ID: {session.schoolId})
                </p>
              )}
            </div>

            {/* End location */}
            <div className='space-y-2 pt-3 border-t border-slate-100'>
              <label className={labelCls}>End Location ({initDir === 'OUTBOUND' ? 'SCHOOL' : 'DEPOT'})</label>
              {initDir === 'RETURN' ? (
                hasDepots ? (
                  <div className='mt-1'>
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
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100 font-medium'>
                  Ends at school: <span className='font-semibold text-slate-700'>{session.schoolName}</span> (ID: {session.schoolId})
                </p>
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
              <p className='text-[11px] text-amber-600 font-semibold'>⚠️ Depot is required to create this route.</p>
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
  const isAssigned = ['ASSIGNED', 'TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status);

  return (
    <div
      id={`route-card-${route.id}`}
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
            <span>{route.routeDirection === 'RETURN' ? 'Chiều về' : 'Chiều đi'}</span>
          </p>
        </div>
        <div className='flex flex-col items-end gap-1.5 shrink-0'>
          <span className={cn(
            'rounded-full px-2.5 py-0.5 text-[10px] font-semibold border shadow-none text-center',
            route.status === 'PUBLISHED' ? 'bg-emerald-50 text-emerald-700 border-emerald-250' :
            route.status === 'CANCELLED' ? 'bg-slate-50 text-slate-500 border-slate-200' : 'bg-amber-50 text-amber-700 border-amber-250'
          )}>{route.status === 'PUBLISHED' ? 'Published' : route.status === 'CANCELLED' ? 'Cancelled' : 'Draft'}</span>
          <span className={cn(
            'rounded-full px-2 py-0.5 text-[9px] font-bold border shadow-none text-center',
            isAssigned
              ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
              : 'bg-amber-50 text-amber-700 border-amber-200'
          )}>
            {isAssigned ? 'Ready for trip' : 'Needs assignment'}
          </span>
        </div>
      </div>
      {((route.blockingIssueCount ?? 0) > 0 || ((route.issueCount ?? 0) - (route.blockingIssueCount ?? 0)) > 0) && (
        <div className='flex flex-wrap gap-1.5 text-[10px]'>
          {(route.blockingIssueCount ?? 0) > 0 && (
            <span className='inline-flex items-center gap-1 rounded bg-red-50 text-red-700 px-1.5 py-0.5 border border-red-100 font-bold shadow-none'>
              🚫 {route.blockingIssueCount} blocking
            </span>
          )}
          {((route.issueCount ?? 0) - (route.blockingIssueCount ?? 0)) > 0 && (
            <span className='inline-flex items-center gap-1 rounded bg-amber-50 text-amber-700 px-1.5 py-0.5 border border-amber-100 font-bold shadow-none'>
              ⚠️ {route.issueCount! - route.blockingIssueCount!} warning(s)
            </span>
          )}
        </div>
      )}
      <div className='flex items-center justify-between border-t border-slate-100/50 pt-2 text-[10px] text-slate-450'>
        <span>{route.serviceDate}</span>
        {isSelected && <span className='font-extrabold text-[#C81E3A] flex items-center gap-1'>▶ Selected</span>}
      </div>

      {isSelected && (
        <div className='flex flex-wrap gap-2 border-t border-slate-100/50 pt-2.5 mt-1' onClick={e => e.stopPropagation()}>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.id}?assign=true`}>
              Assign resources
            </Link>
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.id}`}>
              View route detail
            </Link>
          </Button>
          <Button
            size='sm'
            variant='ghost'
            className='rounded-full h-8 text-xs font-semibold text-slate-500 hover:bg-slate-100'
            onClick={() => onSelect(route.id)}
          >
            Continue planning
          </Button>
        </div>
      )}
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

  const isAssigned = ['ASSIGNED', 'TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(route.status);

  return (
    <div id={`route-card-${route.routeId}`} onClick={() => onSelect(route.routeId)}
      className={cn(
        'cursor-pointer rounded-2xl border p-4 transition-all duration-200 flex flex-col gap-2.5',
        isSelected
          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-350 border-r-slate-355 bg-slate-50/50 shadow-sm'
          : schoolBusUi.interactiveCard
      )}>
      <div className='flex items-start justify-between gap-3'>
        <div className='min-w-0'>
          <p className='text-xs font-bold text-slate-900 truncate'>{route.routeName}</p>
          <p className='text-[10px] font-bold text-slate-555 mt-0.5 flex items-center gap-1.5'>
            <span className='px-1.5 py-0.5 bg-indigo-50 border border-indigo-100 rounded text-indigo-700 text-[9px]'>{route.routeCode}</span>
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <div className='flex flex-col items-end gap-1 shrink-0'>
            <span className={cn(
              'rounded-full px-2 py-0.5 text-[9px] font-bold border shadow-none text-center',
              isAssigned
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : 'bg-amber-50 text-amber-700 border-amber-200'
            )}>
              {isAssigned ? 'Ready for trip' : 'Needs assignment'}
            </span>
          </div>
          <div className={cn('flex h-9 w-9 shrink-0 items-center justify-center rounded-full border text-[11px] font-extrabold shadow-sm', scoreColor)}>
            {score.toFixed(0)}
          </div>
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
        <div className='flex flex-wrap gap-2 border-t border-slate-100/50 pt-2.5 mt-1' onClick={e => e.stopPropagation()}>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.routeId}?assign=true`}>
              Assign resources
            </Link>
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.routeId}`}>
              View route detail
            </Link>
          </Button>
          <Button
            size='sm'
            variant='ghost'
            className='rounded-full h-8 text-xs font-semibold text-slate-500 hover:bg-slate-100'
            onClick={() => onSelect(route.routeId)}
          >
            Continue planning
          </Button>
        </div>
      )}
    </div>
  );
}

function SessionObjectiveScoreWidget({ sessionId }: { sessionId: number }) {
  const { data: scoreData, refetch, isLoading } = useGetSessionObjectiveScoreQuery(sessionId);
  const [recalculate, { isLoading: isRecalculating }] = useRecalculateSessionObjectiveScoreMutation();

  const handleRecalculate = async () => {
    try {
      await recalculate(sessionId).unwrap();
      toast.success('Recalculated session objective score');
      refetch();
    } catch (err: any) {
      toast.error(err?.data?.message || 'Failed to recalculate session score');
    }
  };

  if (isLoading) {
    return <div className="text-xs text-slate-450 animate-pulse py-2">Loading solution score...</div>;
  }

  const score = scoreData?.data;
  if (!score) {
    return (
      <div className="text-xs text-slate-450 py-2">
        No solution score calculated. <Button variant="link" onClick={handleRecalculate} className="p-0 h-auto text-xs font-semibold text-[#C81E3A]">Calculate now</Button>
      </div>
    );
  }

  return (
    <div className="p-4 rounded-2xl border border-indigo-100 bg-indigo-50/20 space-y-3">
      <div className="flex items-center justify-between">
        <div>
          <h4 className="text-xs font-bold text-indigo-800 uppercase tracking-wider">Solution Quality Score</h4>
          <p className="text-[10px] text-indigo-600 mt-0.5">Overall session cost (includes unassigned student penalties)</p>
        </div>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={handleRecalculate} 
          disabled={isRecalculating} 
          className="h-6 text-[10px] font-bold text-indigo-700 hover:bg-indigo-105/50 p-1 px-2 rounded-full"
        >
          {isRecalculating ? 'Recalculating...' : 'Recalculate'}
        </Button>
      </div>

      <div className="flex items-center justify-between">
        <div>
          <p className="text-[10px] font-medium text-slate-500 leading-none">Normalized Score</p>
          <div className="flex items-baseline gap-1 mt-1">
            <span className={cn(
              "text-lg font-black",
              score.feasible ? "text-emerald-700" : "text-rose-700"
            )}>
              {score.displayScore?.toFixed(2)}
            </span>
            <span className="text-slate-450 text-[10px]">/ 100</span>
          </div>
        </div>
        <div className="text-right">
          <p className="text-[10px] font-medium text-slate-500 leading-none">Total Objective Value</p>
          <p className="text-sm font-extrabold text-slate-700 mt-1">{score.objectiveValue?.toFixed(2)}</p>
        </div>
      </div>

      {/* Mini breakdown of session cost */}
      <div className="grid grid-cols-2 gap-x-4 gap-y-1.5 text-[11px] text-slate-650 border-t border-indigo-100/40 pt-2.5">
        <div className="flex justify-between">
          <span>Distance cost:</span>
          <span className="font-bold text-slate-750">{score.distanceCost?.toFixed(0)}</span>
        </div>
        <div className="flex justify-between">
          <span>Duration:</span>
          <span className="font-bold text-slate-750">{score.durationCost?.toFixed(0)}</span>
        </div>
        <div className="flex justify-between">
          <span>Wait time:</span>
          <span className="font-bold text-slate-750">{score.waitTimeCost?.toFixed(0)}</span>
        </div>
        <div className="flex justify-between">
          <span>Routes:</span>
          <span className="font-bold text-slate-750">{score.routeCountCost?.toFixed(0)}</span>
        </div>
        {score.unassignedCost > 0 && (
          <div className="flex justify-between text-rose-600 font-medium col-span-2">
            <span>Unassigned penalty:</span>
            <span>+{score.unassignedCost?.toFixed(0)}</span>
          </div>
        )}
      </div>
    </div>
  );
}

function RouteObjectiveScoreWidget({ routeId }: { routeId: number }) {
  const { data: scoreData, refetch, isLoading } = useGetRouteObjectiveScoreQuery(routeId);
  const [recalculate, { isLoading: isRecalculating }] = useRecalculateRouteObjectiveScoreMutation();

  const handleRecalculate = async () => {
    try {
      await recalculate(routeId).unwrap();
      toast.success('Recalculated route objective score');
      refetch();
    } catch (err: any) {
      toast.error(err?.data?.message || 'Failed to recalculate score');
    }
  };

  if (isLoading) {
    return <div className="text-xs text-slate-450 animate-pulse py-2">Loading score...</div>;
  }

  const score = scoreData?.data;
  if (!score) {
    return (
      <div className="text-xs text-slate-450 py-2">
        No score calculated. <Button variant="link" onClick={handleRecalculate} className="p-0 h-auto text-xs font-bold text-indigo-650">Calculate now</Button>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <div>
          <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Objective Quality Score</h4>
          <p className="text-[10px] text-slate-500 mt-0.5 font-medium">Normalized from travel duration & distance</p>
        </div>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={handleRecalculate} 
          disabled={isRecalculating} 
          className="h-6 text-[10px] font-semibold text-[#C81E3A] hover:bg-[#FDECEF]/50 p-1 px-2 rounded-full"
        >
          {isRecalculating ? 'Recalculating...' : 'Recalculate'}
        </Button>
      </div>

      <div className="p-3 rounded-xl border border-slate-100 bg-slate-50 flex items-center justify-between">
        <div>
          <p className="text-[10px] font-medium text-slate-400 leading-none">Normalized Score</p>
          <div className="flex items-baseline gap-1 mt-1">
            <span className={cn(
              "text-lg font-extrabold",
              score.feasible ? "text-emerald-600" : "text-rose-600"
            )}>
              {score.displayScore?.toFixed(2)}
            </span>
            <span className="text-slate-400 text-[10px]">/ 100</span>
          </div>
        </div>
        <div className="text-right">
          <p className="text-[10px] font-medium text-slate-400 leading-none">Objective Value</p>
          <p className="text-sm font-bold text-slate-700 mt-1">{score.objectiveValue?.toFixed(2)}</p>
        </div>
      </div>

      {/* Breakdown Details */}
      <div className="space-y-1.5 text-xs">
        <div className="flex justify-between items-center text-slate-650">
          <span>Distance cost:</span>
          <span className="font-semibold text-slate-800">{score.distanceCost?.toFixed(2)}</span>
        </div>
        <div className="flex justify-between items-center text-slate-650">
          <span>Duration cost:</span>
          <span className="font-semibold text-slate-800">{score.durationCost?.toFixed(2)}</span>
        </div>
        <div className="flex justify-between items-center text-slate-650">
          <span>Wait time cost:</span>
          <span className="font-semibold text-slate-800">{score.waitTimeCost?.toFixed(2)}</span>
        </div>
        {score.capacityExcessCost > 0 && (
          <div className="flex justify-between items-center text-rose-600 font-medium">
            <span>Capacity excess cost:</span>
            <span>+{score.capacityExcessCost?.toFixed(2)}</span>
          </div>
        )}
        {score.blockingIssueCost > 0 && (
          <div className="flex justify-between items-center text-rose-600 font-medium">
            <span>Blocking penalty:</span>
            <span>+{score.blockingIssueCost?.toFixed(2)}</span>
          </div>
        )}
        {score.warningIssueCost > 0 && (
          <div className="flex justify-between items-center text-amber-600 font-medium">
            <span>Warning penalty:</span>
            <span>+{score.warningIssueCost?.toFixed(2)}</span>
          </div>
        )}
      </div>
    </div>
  );
}

/* ── Route Detail Panel ─────────────────────────────────────────── */

function RouteDetailPanel({ routeId, sessionId }: { routeId: number; sessionId: number }) {
  const { data: routeDetail, isLoading } = useGetRouteByIdQuery(routeId);
  const { data: traceData } = useGetLatestRouteCalculationTraceQuery(routeId, { skip: !routeId });
  const [reorderStops] = useReorderRouteStopsMutation();
  const [removeStop, { isLoading: removingStop }] = useRemoveRouteStopMutation();
  const [removeStudent, { isLoading: removingStudent }] = useRemoveRouteStudentMutation();
  const detail = routeDetail?.data;

  const issues = React.useMemo(() => {
    if (!traceData?.data?.issuesJson) return [];
    try {
      return JSON.parse(traceData.data.issuesJson);
    } catch {
      return [];
    }
  }, [traceData]);

  // ── Confirm dialogs ──────────────────────────────────────────────────────
  const stopConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const stopId = stopConfirm.confirmState.payload as number;
      try { await removeStop({ routeId, stopId, sessionId }).unwrap(); } catch (e: unknown) {
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
      try { await removeStudent({ routeId, studentId, subscriptionId, sessionId }).unwrap(); } catch (e: unknown) {
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
      await reorderStops({ id: routeId, orderedStopIds: ids, sessionId }).unwrap();
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
          <h3 className='text-sm font-bold text-slate-955 flex items-center gap-2'>
            <Route className='h-4 w-4 text-emerald-600 shrink-0' />
            {detail.route.routeName} — Detail
          </h3>
        </div>

        {/* Validation Issues */}
        {issues.length > 0 && (
          <div className='rounded-2xl border border-slate-200 bg-white p-3 space-y-2.5 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>Feasibility Validation</p>
            <div className='flex flex-wrap gap-1.5'>
              {issues.map((issue: SchoolBusPlanningIssue, idx: number) => (
                <div key={idx} className='w-full'>
                  <IssueBadge issue={issue} />
                </div>
              ))}
            </div>
          </div>
        )}
        
        <div>
          {/* Terminal route banner */}
          {(() => {
            const startTerminal = stops.find(s => s.stopPurpose === 'START_TERMINAL');
            const endTerminal = stops.find(s => s.stopPurpose === 'END_TERMINAL');
            if (!startTerminal && !endTerminal) return null;
            return (
              <div className='mb-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-3 text-xs text-slate-600'>
                <div className='flex items-center gap-2 font-bold text-slate-700 mb-2'>
                  <Navigation className='h-3.5 w-3.5 text-blue-600' />
                  <span className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>Terminal Stops</span>
                </div>
                <div className='flex items-center gap-2 pl-5 text-[11px] font-semibold text-slate-700'>
                  <span className='bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded border border-blue-100'>
                    {startTerminal?.depotName || startTerminal?.schoolName || 'Start Depot/School'}
                  </span>
                  <span className='text-slate-400'>➔</span>
                  <span className='bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded border border-blue-100'>
                    {endTerminal?.depotName || endTerminal?.schoolName || 'End Depot/School'}
                  </span>
                </div>
              </div>
            );
          })()}

          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2'>
            Pickup / Drop-off Stops ({middleStops.length})
          </p>
          {middleStops.length === 0 ? (
            <p className='text-xs text-slate-400 italic bg-white border border-dashed border-slate-200 rounded-2xl p-4 text-center'>
              No pickup/drop-off stops created yet. Assign students to auto-generate stops.
            </p>
          ) : (
            <div className='space-y-1.5'>
              {middleStops.map((stop, middleIndex) => {
                const isFirstMiddle = middleIndex === 0;
                const isLastMiddle = middleIndex === middleStops.length - 1;
                return (
                  <div key={stop.id} className='flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs text-slate-700'>
                    <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-650'>
                      {middleIndex + 1}
                    </span>
                    <span className='flex-1 font-semibold truncate'>{stop.pickupPointName || `Stop #${stop.id}`}</span>
                    <span className='text-[10px] text-slate-450'>{stop.estimatedStudentCount ?? 0} students</span>
                    {editable && (
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

        <hr className='border-slate-150 mt-4' />

        {/* Route Objective Score */}
        <div className="pt-2">
          <RouteObjectiveScoreWidget routeId={routeId} />
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
  loadingEligible?: boolean;
  selectedRouteId: number | null;
  onSelectRoute: (id: number | null) => void;
  onCreateManualRoute: (req: CreateRouteInSessionRequest) => void;
  creatingRoute?: boolean;
  form?: any;
  rightPanelTab: 'demand-preview' | 'route-builder';
  onTabChange: (tab: 'demand-preview' | 'route-builder') => void;
  onPreviewDemandClick?: () => void;
  previewing?: boolean;
}

export function PlanningResultsPanel({
  preview,
  greedyResult,
  sessionRoutes,
  activeSession,
  eligibleStudents,
  loadingEligible,
  selectedRouteId,
  onSelectRoute,
  onCreateManualRoute,
  creatingRoute,
  form,
  rightPanelTab,
  onTabChange,
  onPreviewDemandClick,
  previewing,
}: PlanningResultsPanelProps) {

  const isManual = activeSession?.planningMethod === 'MANUAL';

  const [previewTab, setPreviewTab] = useState<'summary' | 'demands' | 'points' | 'context'>('summary');
  const [demandFilter, setDemandFilter] = useState<'all' | 'eligible' | 'blocked'>('all');

  const schoolId = Number(preview?.schoolId ?? form?.schoolId) || 0;
  const { data: schoolData } = useGetSchoolByIdQuery(schoolId, { skip: !schoolId });
  const currentSchool = schoolData?.data ?? null;

  const scheduleId = Number(preview?.schoolScheduleId ?? form?.schoolScheduleId) || 0;
  const { data: scheduleData } = useGetSchoolScheduleByIdQuery(scheduleId, { skip: !scheduleId });
  const currentSchedule = scheduleData?.data ?? null;

  const { data: depotsData } = useGetDepotsQuery({ page: 1, size: 200 } as any);
  const depots = depotsData?.data?.items ?? [];
  const selectedDepot = depots.find((d: any) => String(d.id) === String(preview?.depotId ?? form?.depotId));

  const isPreviewStale = preview ? (
    Number(preview.schoolId) !== Number(form?.schoolId) ||
    Number(preview.schoolScheduleId) !== Number(form?.schoolScheduleId) ||
    preview.serviceDate !== form?.serviceDate ||
    preview.routeDirection !== form?.routeDirection ||
    preview.planningMethod !== form?.planningMethod ||
    (form?.depotId ? Number(preview.depotId) !== Number(form.depotId) : false) ||
    (form?.defaultBusCapacity ? Number(preview.defaultBusCapacity) !== Number(form.defaultBusCapacity) : false)
  ) : false;

  return (
    <div className='flex h-full flex-col bg-white overflow-hidden'>
      {/* Premium Tab Selector */}
      <div className='shrink-0 flex border-b border-slate-200 bg-slate-50/50 p-1.5 gap-1.5'>
        <button
          onClick={() => onTabChange('demand-preview')}
          className={cn(
            'flex-1 flex flex-col items-center justify-center py-2 px-3 rounded-xl border transition-all text-center',
            rightPanelTab === 'demand-preview'
              ? 'bg-white border-slate-200 shadow-sm text-[#C81E3A] font-bold'
              : 'bg-transparent border-transparent text-slate-500 hover:bg-slate-100/50 hover:text-slate-700'
          )}
        >
          <span className='text-xs font-bold'>Demand Preview</span>
          <span className='text-[10px] text-slate-400 mt-0.5 font-medium'>
            {preview && !isPreviewStale
              ? `Total ${preview.summary?.totalSubscriptions ?? preview.totalEligibleStudents} · Eligible ${preview.summary?.eligibleStudents ?? preview.totalEligibleStudents} · Blocked ${preview.summary?.blockedStudents ?? 0}`
              : isPreviewStale
                ? 'Out of date'
                : 'No preview'}
          </span>
        </button>
        <button
          onClick={() => onTabChange('route-builder')}
          className={cn(
            'flex-1 flex flex-col items-center justify-center py-2 px-3 rounded-xl border transition-all text-center',
            rightPanelTab === 'route-builder'
              ? 'bg-white border-slate-200 shadow-sm text-[#C81E3A] font-bold'
              : 'bg-transparent border-transparent text-slate-500 hover:bg-slate-100/50 hover:text-slate-700'
          )}
        >
          <span className='text-xs font-bold'>Route Builder</span>
          <span className='text-[10px] text-slate-400 mt-0.5 font-medium'>
            {activeSession
              ? `${activeSession.planningMethod === 'MANUAL' ? 'Manual' : 'Greedy'} (${sessionRoutes.length || (greedyResult?.routes.length ?? 0)})`
              : 'No session'}
          </span>
        </button>
      </div>

      <div className='flex-1 overflow-y-auto divide-y divide-slate-150'>
        {/* Tab 1: Demand Preview */}
        {rightPanelTab === 'demand-preview' && (
          <>
            {/* Case A: Stale Preview */}
            {isPreviewStale && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-amber-50 border border-amber-100 text-amber-500 mb-3 shadow-sm'>
                  <AlertTriangle className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>Preview is out of date</p>
                <p className='mt-1 text-xs text-slate-450 max-w-[240px] leading-relaxed'>
                  Planning context has changed. Click Preview to refresh.
                </p>
                {onPreviewDemandClick && (
                  <Button
                    size='sm'
                    onClick={onPreviewDemandClick}
                    disabled={previewing}
                    className='mt-4 rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm'
                  >
                    {previewing ? 'Previewing...' : 'Preview Demand'}
                  </Button>
                )}
              </div>
            )}

            {/* Case B: No Preview at all */}
            {!preview && !isPreviewStale && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 mb-3 shadow-sm'>
                  <Users className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>
                  {activeSession ? 'Preview demand for this session' : 'No demand preview yet'}
                </p>
                <p className='mt-1 text-xs text-slate-450 max-w-[240px] leading-relaxed'>
                  {activeSession
                    ? 'Preview demand for this session to inspect eligible and blocked students.'
                    : 'Complete planning context and click Preview to inspect eligible students.'}
                </p>
                {onPreviewDemandClick && (
                  <Button
                    size='sm'
                    onClick={onPreviewDemandClick}
                    disabled={previewing}
                    className='mt-4 rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm'
                  >
                    {previewing ? 'Previewing...' : 'Preview Demand'}
                  </Button>
                )}
              </div>
            )}

            {/* Case C: Active Preview Data */}
            {preview && !isPreviewStale && (() => {
        const summary = preview?.summary;
        const eligibleDemands = preview?.eligibleDemands ?? [];
        const blockedDemands = preview?.blockedDemands ?? [];
        const allDemands = [...eligibleDemands, ...blockedDemands];
        const points = preview?.points ?? [];
        const issues = preview?.issues ?? [];

        // Derived variables for display
        const totalSubs = preview ? (summary?.totalSubscriptions ?? preview.totalEligibleStudents) : '—';
        const eligibleCount = preview ? (summary?.eligibleStudents ?? preview.totalEligibleStudents) : '—';
        const blockedCount = preview ? (summary?.blockedStudents ?? 0) : '—';
        const pointsCount = preview ? (summary?.pointCount ?? preview.totalEligiblePickupPoints) : '—';

        // Filter demands based on filter selection
        const filteredDemands = allDemands.filter(d => {
          if (demandFilter === 'eligible') return d.readinessStatus === 'READY';
          if (demandFilter === 'blocked') return d.readinessStatus === 'BLOCKED';
          return true;
        });

        const currentDirection = preview?.routeDirection ?? form?.routeDirection;

        return (
          <div className='p-5 space-y-4 bg-white rounded-2xl'>
            <div className='border-b border-slate-100 pb-2.5 flex items-center justify-between'>
              <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
                <Users className='h-4 w-4 text-[#C81E3A] shrink-0' />
                Planning Readiness Preview
              </h3>
              <span className='px-2 py-0.5 bg-red-50 border border-red-100 text-[#C81E3A] text-[10px] font-bold rounded-full'>
                {currentDirection === 'RETURN' ? 'Chiều về' : 'Chiều đi'}
              </span>
            </div>

            {/* KPI Summary Cards */}
            <div className='grid grid-cols-4 gap-2 text-xs'>
              <div className='p-2.5 rounded-2xl bg-slate-50 border border-slate-150 shadow-sm flex flex-col justify-between'>
                <span className='text-[9px] font-extrabold text-slate-450 uppercase tracking-wider leading-none'>Total</span>
                <span className='text-sm font-black text-slate-850 mt-1.5'>{totalSubs}</span>
              </div>
              <div className='p-2.5 rounded-2xl bg-emerald-50/50 border border-emerald-100 shadow-sm flex flex-col justify-between'>
                <span className='text-[9px] font-extrabold text-emerald-650 uppercase tracking-wider leading-none'>Eligible</span>
                <span className='text-sm font-black text-emerald-700 mt-1.5'>{eligibleCount}</span>
              </div>
              <div className='p-2.5 rounded-2xl bg-red-50/50 border border-red-100 shadow-sm flex flex-col justify-between'>
                <span className='text-[9px] font-extrabold text-red-650 uppercase tracking-wider leading-none'>Blocked</span>
                <span className='text-sm font-black text-red-700 mt-1.5'>{blockedCount}</span>
              </div>
              <div className='p-2.5 rounded-2xl bg-blue-50/50 border border-blue-100 shadow-sm flex flex-col justify-between'>
                <span className='text-[9px] font-extrabold text-blue-650 uppercase tracking-wider leading-none'>Points</span>
                <span className='text-sm font-black text-blue-700 mt-1.5'>{pointsCount}</span>
              </div>
            </div>

            {/* Tab Navigation */}
            <div className='flex border-b border-slate-150 text-xs font-bold'>
              <button
                onClick={() => setPreviewTab('summary')}
                className={cn(
                  'px-3 py-2 border-b-2 transition-all -mb-px',
                  previewTab === 'summary' ? 'border-[#C81E3A] text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600'
                )}
              >
                Overview
              </button>
              <button
                onClick={() => setPreviewTab('demands')}
                className={cn(
                  'px-3 py-2 border-b-2 transition-all -mb-px',
                  previewTab === 'demands' ? 'border-[#C81E3A] text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600'
                )}
              >
                Students ({preview ? allDemands.length : 0})
              </button>
              <button
                onClick={() => setPreviewTab('points')}
                className={cn(
                  'px-3 py-2 border-b-2 transition-all -mb-px',
                  previewTab === 'points' ? 'border-[#C81E3A] text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600'
                )}
              >
                Points ({preview ? points.length : 0})
              </button>
              <button
                onClick={() => setPreviewTab('context')}
                className={cn(
                  'px-3 py-2 border-b-2 transition-all -mb-px',
                  previewTab === 'context' ? 'border-[#C81E3A] text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600'
                )}
              >
                Context
              </button>
            </div>

            {/* Tab 1: Overview & Diagnostics */}
            {previewTab === 'summary' && (() => {
              if (!preview) {
                return (
                  <div className='flex flex-col items-center justify-center py-12 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 shadow-sm w-full'>
                    <Users className='h-8 w-8 text-slate-350 mb-2' />
                    <p className='text-xs font-bold text-slate-600'>No Preview Data</p>
                    <p className='text-[10px] text-slate-450 mt-1 max-w-[200px]'>
                      Fill context on the left and click "Preview Demand" to load diagnostics.
                    </p>
                  </div>
                );
              }
              return (
                <div className='space-y-4 pt-1'>
                  {summary && (
                    <div className='rounded-2xl border border-slate-150 bg-slate-50/35 p-4 space-y-3 shadow-sm'>
                      <h4 className='text-[10px] font-extrabold text-slate-450 uppercase tracking-wider'>Readiness Diagnostics</h4>
                      <div className='grid grid-cols-2 gap-2 text-xs'>
                        <div className='flex items-center gap-2 text-slate-650'>
                          <span className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold border',
                            summary.pausedCount > 0 ? 'bg-amber-50 text-amber-700 border-amber-100' : 'bg-slate-50 text-slate-400 border-slate-100'
                          )}>⏸️</span>
                          <div className='min-w-0'>
                            <p className='font-bold truncate'>{summary.pausedCount} paused</p>
                          </div>
                        </div>
                        <div className='flex items-center gap-2 text-slate-650'>
                          <span className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold border',
                            summary.inactiveCount > 0 ? 'bg-amber-50 text-amber-700 border-amber-100' : 'bg-slate-50 text-slate-400 border-slate-100'
                          )}>🚫</span>
                          <div className='min-w-0'>
                            <p className='font-bold truncate'>{summary.inactiveCount} inactive</p>
                          </div>
                        </div>
                        <div className='flex items-center gap-2 text-slate-650'>
                          <span className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold border',
                            summary.dayMismatchCount > 0 ? 'bg-amber-50 text-amber-700 border-amber-100' : 'bg-slate-50 text-slate-400 border-slate-100'
                          )}>📅</span>
                          <div className='min-w-0'>
                            <p className='font-bold truncate'>{summary.dayMismatchCount} day mismatch</p>
                          </div>
                        </div>
                        <div className='flex items-center gap-2 text-slate-650'>
                          <span className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold border',
                            summary.outOfEffectiveRangeCount > 0 ? 'bg-amber-50 text-amber-700 border-amber-100' : 'bg-slate-50 text-slate-400 border-slate-100'
                          )}>⏳</span>
                          <div className='min-w-0'>
                            <p className='font-bold truncate'>{summary.outOfEffectiveRangeCount} expired/range</p>
                          </div>
                        </div>
                        <div className='flex items-center gap-2 text-slate-650'>
                          <span className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold border',
                            summary.missingCoordinateCount > 0 ? 'bg-red-50 text-red-700 border-red-100' : 'bg-slate-50 text-slate-400 border-slate-100'
                          )}>📍</span>
                          <div className='min-w-0'>
                            <p className='font-bold truncate'>{summary.missingCoordinateCount} missing coords</p>
                          </div>
                        </div>
                        <div className='flex items-center gap-2 text-slate-650'>
                          <span className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold border',
                            summary.missingWindowCount > 0 ? 'bg-red-50 text-red-700 border-red-100' : 'bg-slate-50 text-slate-400 border-slate-100'
                          )}>🕒</span>
                          <div className='min-w-0'>
                            <p className='font-bold truncate'>{summary.missingWindowCount} missing window</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {issues.length > 0 ? (
                    <div className='space-y-2'>
                      <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>Blocking Issues</p>
                      <div className='flex flex-wrap gap-1.5'>
                        {issues.map((issue, i) => (
                          <IssueBadge key={i} issue={issue} />
                        ))}
                      </div>
                    </div>
                  ) : (
                    <div className='p-4 bg-emerald-50/35 border border-emerald-100 rounded-2xl text-xs flex gap-2.5 shadow-sm'>
                      <CheckCircle2 className='h-4 w-4 text-emerald-600 shrink-0 mt-0.5' />
                      <div>
                        <p className='font-bold text-emerald-800'>All Clear</p>
                        <p className='text-[10px] text-emerald-600/80 mt-0.5'>No blocking issues found for eligible subscriptions.</p>
                      </div>
                    </div>
                  )}
                </div>
              );
            })()}

            {/* Tab 2: Students List */}
            {previewTab === 'demands' && (() => {
              if (!preview) {
                return (
                  <div className='flex flex-col items-center justify-center py-12 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 shadow-sm w-full'>
                    <Users className='h-8 w-8 text-slate-350 mb-2' />
                    <p className='text-xs font-bold text-slate-600'>No Preview Data</p>
                    <p className='text-[10px] text-slate-450 mt-1 max-w-[200px]'>
                      Fill context on the left and click "Preview Demand" to load students.
                    </p>
                  </div>
                );
              }
              return (
                <div className='space-y-3 pt-1'>
                  <div className='flex gap-1.5 p-1 bg-slate-100 rounded-xl text-[10px] font-bold text-slate-500'>
                    {(['all', 'eligible', 'blocked'] as const).map(tab => (
                      <button
                        key={tab}
                        onClick={() => setDemandFilter(tab)}
                        className={cn(
                          'flex-1 py-1 rounded-lg capitalize transition-all',
                          demandFilter === tab ? 'bg-white text-slate-900 shadow-sm' : 'hover:text-slate-850'
                        )}
                      >
                        {tab}
                      </button>
                    ))}
                  </div>

                  {filteredDemands.length === 0 ? (
                    <div className='text-center py-8 text-slate-400 text-xs'>No students found.</div>
                  ) : (
                    <div className='space-y-2 max-h-[380px] overflow-y-auto pr-1'>
                      {filteredDemands.map(d => (
                        <div
                          key={d.subscriptionId}
                          className={cn(
                            'rounded-xl border p-3 flex flex-col gap-1.5 shadow-sm bg-white',
                            d.readinessStatus === 'BLOCKED' ? 'border-red-150 border-l-4 border-l-red-500' : 'border-slate-150'
                          )}
                        >
                          <div className='flex items-start justify-between gap-2'>
                            <div className='min-w-0'>
                              <p className='text-xs font-bold text-slate-800 truncate'>{d.studentName}</p>
                              <p className='text-[10px] text-slate-400 mt-0.5 flex items-center gap-1.5'>
                                <span className='font-bold'>{d.studentCode}</span>
                                <span>•</span>
                                <span>{d.tripOption}</span>
                              </p>
                            </div>
                            <span className={cn(
                              'rounded-full px-2 py-0.5 text-[9px] font-bold border shrink-0',
                              d.readinessStatus === 'READY'
                                ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                                : 'bg-red-50 text-red-700 border-red-100'
                            )}>
                              {d.readinessStatus === 'READY' ? 'Eligible' : 'Blocked'}
                            </span>
                          </div>

                          {d.pointName && (
                            <div className='flex items-center gap-1.5 text-[10px] text-slate-500 bg-slate-50 px-2 py-1 rounded-lg border border-slate-100 mt-0.5'>
                              <MapPin className='h-3 w-3 text-slate-400 shrink-0' />
                              <span className='font-bold truncate'>{d.pointName}</span>
                              {d.windowStart && (
                                <>
                                  <span>•</span>
                                  <Clock className='h-3 w-3 text-slate-400 shrink-0' />
                                  <span className='font-medium'>{d.windowStart.slice(0, 5)} - {d.windowEnd?.slice(0, 5)}</span>
                                </>
                              )}
                            </div>
                          )}

                          {d.readinessStatus === 'BLOCKED' && d.issueLabels && d.issueLabels.length > 0 && (
                            <div className='flex flex-wrap gap-1 mt-1'>
                              {d.issueLabels.map((label, i) => (
                                <span key={i} className='inline-flex items-center gap-0.5 bg-red-50 text-red-700 text-[9px] font-bold px-1.5 py-0.5 rounded border border-red-100'>
                                  <XCircle className='h-2.5 w-2.5 shrink-0' /> {label}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })()}

            {/* Tab 3: Points List */}
            {previewTab === 'points' && (() => {
              if (!preview) {
                return (
                  <div className='flex flex-col items-center justify-center py-12 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 shadow-sm w-full'>
                    <MapPin className='h-8 w-8 text-slate-350 mb-2' />
                    <p className='text-xs font-bold text-slate-600'>No Preview Data</p>
                    <p className='text-[10px] text-slate-450 mt-1 max-w-[200px]'>
                      Fill context on the left and click "Preview Demand" to load points.
                    </p>
                  </div>
                );
              }
              return (
                <div className='space-y-3 pt-1'>
                  {points.length === 0 ? (
                    <div className='text-center py-8 text-slate-400 text-xs'>No points mapped.</div>
                  ) : (
                    <div className='grid gap-2 max-h-[380px] overflow-y-auto pr-1 sm:grid-cols-2'>
                      {points.map(pp => (
                        <div
                          key={pp.pointId}
                          className={cn(
                            'rounded-xl border p-3 flex flex-col justify-between gap-1 shadow-sm bg-white transition-all',
                            pp.readinessStatus === 'BLOCKED' ? 'border-red-200 bg-red-50/10' : 'border-slate-150'
                          )}
                        >
                          <div className='min-w-0'>
                            <p className='text-xs font-bold text-slate-800 flex items-center gap-1.5 truncate'>
                              <MapPin className={cn('h-3.5 w-3.5 shrink-0', pp.readinessStatus === 'BLOCKED' ? 'text-red-500' : 'text-[#C81E3A]')} />
                              {pp.pointName}
                            </p>
                            {pp.pointCode && (
                              <p className='text-[9px] font-bold text-slate-400 mt-0.5'>{pp.pointCode}</p>
                            )}
                          </div>

                          {pp.windowStart && (
                            <div className='flex items-center gap-1 text-[9px] text-slate-500 mt-1'>
                              <Clock className='h-3 w-3 text-slate-400 shrink-0' />
                              <span>{pp.windowStart.slice(0, 5)} - {pp.windowEnd?.slice(0, 5)}</span>
                            </div>
                          )}

                          <div className='flex items-center justify-between mt-2 pt-2 border-t border-slate-100/50 text-[10px] text-slate-500'>
                            <span className='font-bold'>{pp.studentCount} student(s)</span>
                            <span className={cn(
                              'inline-flex items-center rounded px-1.5 py-0.5 font-bold border',
                              pp.readinessStatus === 'READY'
                                ? 'bg-emerald-55/10 text-emerald-700 border-emerald-100'
                                : 'bg-red-55/10 text-red-700 border-red-100'
                            )}>
                              {pp.readinessStatus === 'READY' ? '✅ Ready' : '⚠️ Blocked'}
                            </span>
                          </div>

                          {pp.readinessStatus === 'BLOCKED' && pp.issueLabels && pp.issueLabels.length > 0 && (
                            <div className='flex flex-wrap gap-1 mt-1.5'>
                              {pp.issueLabels.map((lbl, idx) => (
                                <span key={idx} className='text-[8px] font-bold bg-red-55/10 text-red-700 px-1 py-0.5 rounded border border-red-100 truncate max-w-full'>{lbl}</span>
                              ))}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })()}

            {/* Tab 4: Context */}
            {previewTab === 'context' && (() => {
              // 1. School Info
              const schName = preview?.schoolName ?? currentSchool?.name ?? '—';
              const schCode = preview?.schoolCode ?? currentSchool?.code ?? '—';
              const schAddr = preview?.schoolAddress ?? currentSchool?.address ?? '—';

              // 2. Schedule Info
              const sName = preview?.scheduleName ?? currentSchedule?.scheduleName ?? '—';
              const sCode = preview?.scheduleCode ?? currentSchedule?.scheduleCode ?? '—';
              const sShift = preview?.shiftType ?? currentSchedule?.shiftType ?? '—';
              const sArrival = preview?.arrivalDeadline ?? currentSchedule?.arrivalDeadline ?? '—';
              const sDeparture = preview?.departureTime ?? currentSchedule?.departureTime ?? '—';
              const effFrom = preview?.effectiveFrom ?? currentSchedule?.effectiveFrom;
              const effTo = preview?.effectiveTo ?? currentSchedule?.effectiveTo;
              const effRange = effFrom && effTo ? `${formatDate(effFrom)} - ${formatDate(effTo)}` : effFrom ? formatDate(effFrom) : '—';

              // 3. Active Days
              const pActiveDays = preview?.activeDays;
              const rawActiveDays = pActiveDays ?? currentSchedule?.daysOfWeek ?? [];
              const activeDaysSet = new Set(rawActiveDays.map((d: string) => d.toUpperCase()));

              const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
              const dayLabels: Record<string, string> = {
                MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu', FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun'
              };

              // Check if service date matches schedule days
              const sDate = preview?.serviceDate ?? form?.serviceDate;
              let dateMatchText = '';
              let dateMatchColor = '';
              if (sDate) {
                const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
                const dateDayName = dayNames[new Date(sDate).getDay()];
                const isMatch = activeDaysSet.has(dateDayName);
                if (isMatch) {
                  dateMatchText = 'Service date matches schedule day';
                  dateMatchColor = 'text-emerald-700 bg-emerald-55/10 border-emerald-100';
                } else {
                  dateMatchText = 'Service date is not included in schedule days';
                  dateMatchColor = 'text-red-700 bg-red-55/10 border-red-100';
                }
              }

              // 4. Planning Context
              const serviceDateFormatted = sDate ? formatDate(sDate) : '—';
              const dirVal = preview?.direction ?? form?.routeDirection;
              const dirLabel = dirVal === 'OUTBOUND' ? 'Home → School' : dirVal === 'RETURN' ? 'School → Home' : dirVal ?? '—';
              const methodVal = preview?.planningMethod ?? form?.planningMethod;
              const methodLabel = methodVal === 'MANUAL' ? 'Manual' : methodVal === 'GREEDY' ? 'Greedy auto-generate' : methodVal ?? '—';
              const depName = preview?.depotName ?? selectedDepot?.name ?? '—';
              const capVal = preview?.defaultBusCapacity ?? form?.defaultBusCapacity ?? '—';

              const hasContext = !!(form?.schoolId || preview?.schoolId);
              if (!hasContext) {
                return (
                  <div className='flex flex-col items-center justify-center py-10 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 w-full'>
                    <Calendar className='h-8 w-8 text-slate-350 mb-2' />
                    <p className='text-xs font-bold text-slate-650'>Select Context</p>
                    <p className='text-[10px] text-slate-450 mt-1 max-w-[200px]'>
                      Select planning context and preview demand.
                    </p>
                  </div>
                );
              }

              return (
                <div className='space-y-3 pt-1 text-xs text-slate-700 w-full'>
                  {/* School Section */}
                  <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-1.5 shadow-sm'>
                    <p className='text-[9px] font-extrabold text-slate-450 uppercase tracking-wider'>School</p>
                    <div>
                      <p className='font-bold text-slate-800 text-sm leading-snug'>{schName}</p>
                      <p className='text-[10px] text-slate-400 font-semibold mt-0.5'>{schCode}</p>
                      {schAddr && schAddr !== '—' && (
                        <p className='text-[10px] text-slate-500 mt-1 truncate'>{schAddr}</p>
                      )}
                    </div>
                  </div>

                  {/* Schedule Section */}
                  <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-2 shadow-sm'>
                    <div className='flex items-start justify-between'>
                      <div className='space-y-1'>
                        <p className='text-[9px] font-extrabold text-slate-450 uppercase tracking-wider'>Schedule</p>
                        <p className='font-bold text-slate-800 leading-snug'>{sName}</p>
                        <p className='text-[10px] text-slate-400 font-bold mt-0.5'>
                          {sCode} <span className='text-slate-300 mx-1'>·</span> <span className='text-[#C81E3A]'>{sShift}</span>
                        </p>
                      </div>
                    </div>
                    <div className='grid grid-cols-2 gap-2 text-[10px] pt-1.5 border-t border-slate-100/60 text-slate-500'>
                      <div>
                        <span className='font-semibold text-slate-400'>Arrival:</span>{' '}
                        <span className='font-bold text-slate-700'>{sArrival ? sArrival.slice(0, 5) : '—'}</span>
                      </div>
                      <div>
                        <span className='font-semibold text-slate-400'>Departure:</span>{' '}
                        <span className='font-bold text-slate-700'>{sDeparture ? sDeparture.slice(0, 5) : '—'}</span>
                      </div>
                      <div className='col-span-2'>
                        <span className='font-semibold text-slate-400'>Effective:</span>{' '}
                        <span className='font-bold text-slate-700'>{effRange}</span>
                      </div>
                    </div>
                  </div>

                  {/* Active Days Section */}
                  <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-2.5 shadow-sm'>
                    <p className='text-[9px] font-extrabold text-slate-450 uppercase tracking-wider'>Active Days</p>
                    <div className='flex flex-wrap gap-1.5'>
                      {daysOfWeek.map(day => {
                        const isActive = activeDaysSet.has(day);
                        return (
                          <span
                            key={day}
                            className={cn(
                              'px-2 py-0.5 rounded-lg border text-[10px] font-bold transition-all',
                              isActive
                                ? 'bg-red-50 text-[#C81E3A] border-red-100'
                                : 'bg-slate-50 text-slate-400 border-slate-100'
                            )}
                          >
                            {dayLabels[day]}
                          </span>
                        );
                      })}
                    </div>
                    {dateMatchText && (
                      <div className={cn(
                        'flex items-center gap-1.5 rounded-xl border px-2.5 py-1.5 text-[10px] font-bold shadow-sm',
                        dateMatchColor
                      )}>
                        <span>{dateMatchText === 'Service date matches schedule day' ? '✓' : '⚠️'}</span>
                        <span>{dateMatchText}</span>
                      </div>
                    )}
                  </div>

                  {/* Planning Section */}
                  <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-2 shadow-sm'>
                    <p className='text-[9px] font-extrabold text-slate-450 uppercase tracking-wider'>Planning</p>
                    <div className='grid grid-cols-2 gap-x-3 gap-y-2 pt-1 text-[11px]'>
                      <div className='flex flex-col'>
                        <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>Service Date</span>
                        <span className='font-bold text-slate-800 mt-0.5'>{serviceDateFormatted}</span>
                      </div>
                      <div className='flex flex-col'>
                        <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>Direction</span>
                        <span className='font-bold text-slate-800 mt-0.5'>{dirLabel}</span>
                      </div>
                      <div className='flex flex-col'>
                        <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>Planning Method</span>
                        <span className='font-bold text-slate-800 mt-0.5'>{methodLabel}</span>
                      </div>
                      <div className='flex flex-col'>
                        <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>Depot</span>
                        <span className='font-bold text-slate-800 mt-0.5 truncate max-w-full'>{depName}</span>
                      </div>
                      <div className='flex flex-col col-span-2'>
                        <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>Default Bus Capacity</span>
                        <span className='font-bold text-slate-800 mt-0.5'>{capVal}</span>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })()}
          </div>
        );
      })()}
          </>
        )}

        {/* Tab 2: Route Builder */}
        {rightPanelTab === 'route-builder' && (
          <>
            {/* Case A: No active session */}
            {!activeSession && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 mb-3 shadow-sm'>
                  <Route className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>No planning session yet</p>
                <p className='mt-1 text-xs text-slate-450 max-w-[240px] leading-relaxed'>
                  Create a session before building routes.
                </p>
              </div>
            )}

            {/* Case B: Active Session exists */}
            {activeSession && (
              <>
                <div className="px-5 pt-4">
                  <SessionObjectiveScoreWidget sessionId={activeSession.id} />
                </div>
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
            contextDepotId={form?.depotId ? Number(form.depotId) : ''}
          />
          {creatingRoute ? (
            <div className='flex flex-col items-center justify-center py-10 px-4 text-center bg-slate-50/50 border border-slate-200 rounded-2xl shadow-sm space-y-3 min-h-[160px] animate-pulse'>
              <Loader2 className='h-7 w-7 text-blue-600 animate-spin' />
              <div className='space-y-1'>
                <p className='text-xs font-bold text-slate-700'>Creating route...</p>
                <p className='text-[10px] text-slate-450 font-semibold'>Computing initial path and timeline...</p>
              </div>
            </div>
          ) : sessionRoutes.length === 0 ? (
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
          loadingEligible={loadingEligible}
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
              </>
            )}
          </>
        )}

        {selectedRouteId && rightPanelTab === 'route-builder' && (
          <RouteDetailPanel routeId={selectedRouteId} sessionId={activeSession?.id ?? 0} />
        )}
      </div>
    </div>
  );
}
