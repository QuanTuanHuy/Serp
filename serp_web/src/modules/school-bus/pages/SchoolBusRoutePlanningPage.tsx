'use client';

import React, { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { PlanningContextPanel, type ContextFormState } from '../components/planning/PlanningContextPanel';
import { PlanningSessionPanel } from '../components/planning/PlanningSessionPanel';
import { PlanningResultsPanel } from '../components/planning/PlanningResultsPanel';
import { DispatchKpiCards } from '../components/planning/DispatchKpiCards';
import { PlanningMap } from '../components/map/PlanningMap';
import { MapToolbar } from '../components/map/MapToolbar';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui';
import {
  usePreviewPlanningDemandMutation,
  useCreatePlanningSessionMutation,
  useGetPlanningSessionsQueryQuery,
  useGetPlanningSessionQuery,
  useGenerateGreedyForSessionMutation,
  usePublishPlanningSessionMutation,
  useCancelPlanningSessionMutation,
  useGetSessionRoutesQuery,
  useCreateRouteInSessionMutation,
  useGetSessionEligibleStudentsQuery,
  useGetRouteByIdQuery,
  useGetRoutePathQuery,
  useGetSchoolByIdQuery,
  useGetDepotsQuery,
} from '../api/schoolBusApi';
import type {
  SchoolBusPlanningSession,
  SchoolBusGreedyGenerateResult,
  SchoolBusPlanningPreview,
  CreateRouteInSessionRequest,
} from '../types';
import { SCHOOL_BUS_OPTION_QUERY } from '../utils';

// ── Map empty-state component ─────────────────────────────────────────────────

type MapEmptyStep = 'pick-school' | 'preview' | 'no-coords' | null;

function MapEmptyState({ step }: { step: MapEmptyStep }) {
  const steps: { label: string; done: boolean }[] = [
    { label: 'Select a school in Planning Context', done: step !== 'pick-school' },
    { label: 'Click Preview Demand to load pickup points', done: step === null },
  ];

  return (
    <div className='flex h-full flex-col items-center justify-center gap-5 bg-[#f8fafc] px-6'>
      {/* Map pin icon */}
      <div className='flex h-16 w-16 items-center justify-center rounded-2xl border-2 border-dashed border-slate-200 bg-white'>
        <svg xmlns='http://www.w3.org/2000/svg' className='h-8 w-8 text-slate-300' viewBox='0 0 24 24' fill='none' stroke='currentColor' strokeWidth='1.5'>
          <path d='M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0' />
          <circle cx='12' cy='10' r='3' />
        </svg>
      </div>

      <div className='text-center'>
        <p className='text-sm font-semibold text-slate-600'>Map is ready</p>
        <p className='mt-1 text-xs text-slate-400 max-w-[220px]'>
          {step === 'no-coords'
            ? 'School has no coordinates — add lat/lng to the school record to see it on the map.'
            : 'Complete the steps below to load markers.'}
        </p>
      </div>

      {/* Step checklist */}
      {step !== 'no-coords' && (
        <ol className='space-y-2 text-left'>
          {steps.map((s, i) => (
            <li key={i} className='flex items-start gap-2.5'>
              <span
                className={`mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold ${
                  s.done
                    ? 'bg-emerald-100 text-emerald-600'
                    : 'bg-rose-50 text-rose-500 ring-1 ring-rose-200'
                }`}
              >
                {s.done ? '✓' : i + 1}
              </span>
              <span className={`text-xs leading-5 ${s.done ? 'text-slate-400 line-through' : 'font-medium text-slate-600'}`}>
                {s.label}
              </span>
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}

export default function SchoolBusRoutePlanningPage() {

  const [form, setForm] = useState<ContextFormState>({
    schoolId: '', schoolScheduleId: '',
    serviceDate: new Date().toISOString().slice(0, 10),
    routeDirection: 'OUTBOUND', planningMethod: 'GREEDY', defaultBusCapacity: '30',
    depotId: '',
  });

  const [preview, setPreview] = useState<SchoolBusPlanningPreview | null>(null);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [activeSessionId, setActiveSessionId] = useState<number | null>(null);
  const [greedyResult, setGreedyResult] = useState<SchoolBusGreedyGenerateResult | null>(null);
  const [selectedRouteId, setSelectedRouteId] = useState<number | null>(null);
  const [isMapExpanded, setIsMapExpanded] = useState(false);

  // ── Fit controls ───────────────────────────────────────────────────────────
  const [fitTarget, setFitTarget] = useState<'all' | 'route'>('all');
  const [fitKey, setFitKey] = useState(0);

  const handleFitAll = useCallback(() => {
    setFitTarget('all');
    setFitKey((k) => k + 1);
  }, []);

  const handleFitRoute = useCallback(() => {
    setFitTarget('route');
    setFitKey((k) => k + 1);
  }, []);

  // ── API mutations ──────────────────────────────────────────────────────────
  const [previewMutation, { isLoading: previewing }] = usePreviewPlanningDemandMutation();
  const [createSession, { isLoading: creating }] = useCreatePlanningSessionMutation();
  const [generateGreedy, { isLoading: generating }] = useGenerateGreedyForSessionMutation();
  const [publishSession, { isLoading: publishing }] = usePublishPlanningSessionMutation();
  const [cancelSession, { isLoading: cancelling }] = useCancelPlanningSessionMutation();
  const [createRouteInSession, { isLoading: creatingRoute }] = useCreateRouteInSessionMutation();

  // ── Session queries ────────────────────────────────────────────────────────
  const { data: sessionsData } = useGetPlanningSessionsQueryQuery();
  const sessions = sessionsData?.data ?? [];

  const { data: liveSessionData } = useGetPlanningSessionQuery(
    activeSessionId ?? 0,
    { skip: !activeSessionId, pollingInterval: 0 }
  );
  const activeSession: SchoolBusPlanningSession | null =
    liveSessionData?.data ?? sessions.find(s => s.id === activeSessionId) ?? null;

  const { data: sessionRoutesData } = useGetSessionRoutesQuery(
    activeSessionId ?? 0,
    { skip: !activeSessionId }
  );
  const sessionRoutes = sessionRoutesData?.data ?? [];

  const { data: eligibleStudentsData } = useGetSessionEligibleStudentsQuery(
    activeSessionId ?? 0,
    { skip: !activeSessionId || activeSession?.planningMethod !== 'MANUAL' }
  );
  const eligibleStudents = eligibleStudentsData?.data ?? [];

  // ── School + depots for map ────────────────────────────────────────────────
  const schoolIdNum = Number(form.schoolId) || 0;
  const { data: schoolData } = useGetSchoolByIdQuery(schoolIdNum, { skip: !schoolIdNum });
  const school = schoolData?.data ?? null;

  const { data: depotsData } = useGetDepotsQuery({ ...SCHOOL_BUS_OPTION_QUERY });
  const depots = depotsData?.data?.items ?? [];

  // ── Route detail for map (stops + polyline) ────────────────────────────────
  const { data: selectedRouteDetailData } = useGetRouteByIdQuery(
    selectedRouteId ?? 0,
    { skip: !selectedRouteId }
  );
  const selectedRouteStops = selectedRouteDetailData?.data?.stops ?? [];

  // ── Route path (actual road geometry) ─────────────────────────────────────
  const { data: selectedRoutePathData } = useGetRoutePathQuery(
    selectedRouteId ?? 0,
    { skip: !selectedRouteId }
  );
  const selectedRoutePath = selectedRoutePathData?.data ?? null;

  // ── Handlers ───────────────────────────────────────────────────────────────
  const handlePreview = useCallback(async () => {
    if (!form.schoolId || !form.schoolScheduleId || !form.serviceDate) {
      toast.error('Please fill School, Schedule and Service Date'); return;
    }
    try {
      const res = await previewMutation({
        schoolId: Number(form.schoolId), schoolScheduleId: Number(form.schoolScheduleId),
        serviceDate: form.serviceDate, routeDirection: form.routeDirection,
      }).unwrap();
      setPreview(res.data);
      // Auto-fit to show all pickup points
      setFitTarget('all');
      setFitKey((k) => k + 1);
      toast.success('Demand preview loaded');
    } catch { toast.error('Failed to load preview'); }
  }, [form, previewMutation]);

  const handleCreateSession = useCallback(async () => {
    try {
      const res = await createSession({
        schoolId: Number(form.schoolId), schoolScheduleId: Number(form.schoolScheduleId),
        serviceDate: form.serviceDate, routeDirection: form.routeDirection,
        planningMethod: form.planningMethod,
      }).unwrap();
      setActiveSessionId(res.data.id);
      toast.success(`Session #${res.data.id} created`);
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? 'Failed to create session');
    }
  }, [form, createSession]);

  const handleGenerate = useCallback(async () => {
    if (!activeSession) return;
    if (!form.depotId) { toast.error('Please select a depot before generating routes'); return; }
    try {
      const res = await generateGreedy({
        id: activeSession.id,
        body: { defaultBusCapacity: Number(form.defaultBusCapacity) || 30, depotId: Number(form.depotId) },
      }).unwrap();
      setGreedyResult(res.data);
      setFitTarget('all');
      setFitKey((k) => k + 1);
      toast.success(`Generated ${res.data.routes.length} route(s). ${res.data.totalUnassignedStudents} unassigned.`);
    } catch { toast.error('Greedy generation failed'); }
  }, [activeSession, form.defaultBusCapacity, form.depotId, generateGreedy]);

  const handleCreateManualRoute = useCallback(async (req: CreateRouteInSessionRequest) => {
    if (!activeSession) return;
    try {
      await createRouteInSession({ sessionId: activeSession.id, body: req }).unwrap();
      toast.success('Route created');
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? 'Failed to create route');
    }
  }, [activeSession, createRouteInSession]);

  const handlePublish = useCallback(async () => {
    if (!activeSession) return;
    try {
      await publishSession(activeSession.id).unwrap();
      toast.success('Session published!');
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? 'Publish failed');
    }
  }, [activeSession, publishSession]);

  const handleCancel = useCallback(() => {
    if (!activeSession) return;
    setCancelDialogOpen(true);
  }, [activeSession]);

  const confirmCancel = useCallback(async () => {
    if (!activeSession) return;
    try {
      await cancelSession(activeSession.id).unwrap();
      setActiveSessionId(null); setGreedyResult(null); setPreview(null);
      toast.success('Session cancelled');
    } catch { toast.error('Failed to cancel session'); } finally {
      setCancelDialogOpen(false);
    }
  }, [activeSession, cancelSession]);

  // ── Route selection with auto-focus map ───────────────────────────────────
  const handleSelectRoute = useCallback((id: number | null) => {
    setSelectedRouteId((prev) => (prev === id ? null : id));
    if (id) {
      setFitTarget('route');
      setFitKey((k) => k + 1);
    }
  }, []);

  // ── Derived values ─────────────────────────────────────────────────────────
  const unassignedStudents =
    activeSession?.totalUnassignedStudents ?? greedyResult?.totalUnassignedStudents ?? 0;
  const blockingTotal =
    greedyResult?.routes.reduce((s, r) => s + r.blockingIssueCount, 0) ?? 0;
  const hasRoutes = sessionRoutes.length > 0 || (greedyResult?.routes.length ?? 0) > 0;
  const canPublish =
    !!activeSession &&
    activeSession.status !== 'PUBLISHED' &&
    activeSession.status !== 'CANCELLED' &&
    blockingTotal === 0 &&
    hasRoutes &&
    unassignedStudents === 0;

  // Map pickup points from preview
  const mapPickupPoints = (preview?.eligiblePickupPoints ?? []).map((pp) => ({
    pickupPointId: pp.pickupPointId,
    pickupPointName: pp.pickupPointName,
    latitude: pp.latitude,
    longitude: pp.longitude,
    studentCount: pp.studentCount,
  }));

  const hasMapData =
    (school != null && typeof school.latitude === 'number') ||
    mapPickupPoints.some((pp) => typeof pp.latitude === 'number') ||
    depots.some((d) => typeof d.latitude === 'number');

  // Enable Fit All only when there are actual coordinate-bearing markers
  const canFitAll =
    (typeof school?.latitude === 'number') ||
    mapPickupPoints.some((pp) => typeof pp.latitude === 'number') ||
    depots.some((d) => typeof d.latitude === 'number');

  // Map empty-state context
  const mapEmptyStep: 'pick-school' | 'preview' | 'no-coords' | null = !hasMapData
    ? !form.schoolId
      ? 'pick-school'
      : school && typeof school.latitude !== 'number'
        ? 'no-coords'
        : 'preview'
    : null;

  const cancelDialog = (
    <AlertDialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Cancel Session?</AlertDialogTitle>
          <AlertDialogDescription>
            Are you sure you want to cancel this session? Draft routes will be soft-deleted. This action cannot be undone.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={cancelling}>Close</AlertDialogCancel>
          <AlertDialogAction
            onClick={confirmCancel}
            disabled={cancelling}
            className='bg-destructive text-white hover:bg-destructive/90'
          >
            Confirm Cancel
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );

  // ── EXPANDED MODE ──────────────────────────────────────────────────────────
  if (isMapExpanded) {
    return (
      <>
        {/* Fixed full-viewport overlay — prevents any body scroll */}
        <div className='fixed inset-0 z-[900] flex overflow-hidden bg-slate-900/10 backdrop-blur-[1px]'>
          {/* Left panel — own scroll, no page scroll */}
          <div className='flex h-full w-[320px] shrink-0 flex-col overflow-hidden border-r border-slate-200 bg-white shadow-xl'>
            <div className='shrink-0 border-b border-slate-100 bg-gradient-to-r from-rose-50 to-white px-4 py-3'>
              <p className='text-[10px] font-bold uppercase tracking-widest text-rose-600'>Bus Dispatch System</p>
              <h2 className='mt-0.5 text-base font-bold text-slate-900'>Route Planning</h2>
            </div>
            {/* Scrollable inner area */}
            <div className='flex-1 overflow-y-auto p-4 space-y-4'>
              <PlanningContextPanel
                form={form} onFormChange={setForm}
                onPreview={handlePreview} onCreateSession={handleCreateSession}
                previewing={previewing} creating={creating}
                sessionActive={!!activeSession && activeSession.status !== 'CANCELLED'}
                depots={depots}
              />
              <PlanningSessionPanel
                activeSession={activeSession} sessions={sessions}
                planningMethod={form.planningMethod} blockingTotal={blockingTotal}
                canPublish={canPublish} generating={generating} publishing={publishing}
                cancelling={cancelling} hasRoutes={hasRoutes}
                onGenerate={handleGenerate} onPublish={handlePublish} onCancel={handleCancel}
                onSelectSession={(s) => setActiveSessionId(s.id)}
                hidePastSessions
              />
            </div>
          </div>

          {/* Map — fills all remaining space */}
          <div className='relative flex flex-1 flex-col overflow-hidden'>
            {/* KPI bar */}
            {activeSession && (
              <DispatchKpiCards
                eligible={activeSession.totalEligibleStudents}
                planned={activeSession.totalPlannedStudents}
                unassigned={activeSession.totalUnassignedStudents}
                routes={activeSession.totalRoutes}
              />
            )}
            {/* Toolbar overlay */}
            <div
              className='absolute right-4 z-[1000]'
              style={{ top: activeSession ? '52px' : '12px' }}
            >
              <MapToolbar
                onFitAll={handleFitAll}
                onFitRoute={handleFitRoute}
                canFitAll={canFitAll}
                canFitRoute={!!selectedRouteId && selectedRouteStops.length > 0}
                isExpanded={true}
                onToggleExpand={() => setIsMapExpanded(false)}
              />
            </div>
            <PlanningMap
              school={school}
              pickupPoints={mapPickupPoints}
              selectedRouteStops={selectedRouteStops}
              selectedRoutePath={selectedRoutePath}
              depots={depots}
              fitTarget={fitTarget}
              fitKey={fitKey}
              isMapExpanded={true}
              className='h-full w-full'
            />
          </div>
        </div>
        {cancelDialog}
      </>
    );
  }

  // ── NORMAL MODE — 3-column dispatch workspace ──────────────────────────────
  return (
    <SchoolBusPageShell
      compact
      title='Route Planning Workspace'
      description='Preview demand · auto-generate or build routes manually · publish.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Dispatch', href: '/school-bus/dispatch' },
            { label: 'Route Planning', current: true },
          ]}
        />
      }
    >
      {/* Workspace container — fills remaining viewport height below the compact hero */}
      <div
        className='flex overflow-hidden rounded-2xl border border-slate-200 bg-slate-100 shadow-[0_4px_20px_rgba(15,23,42,0.08)]'
        style={{ height: 'calc(100vh - 175px)', minHeight: '580px' }}
      >
        {/* ── Left panel: Context + Session (340px) ──────────────────────── */}
        <div className='flex w-[340px] shrink-0 flex-col overflow-hidden border-r border-slate-200 bg-white'>
          <div className='shrink-0 border-b border-slate-100 bg-gradient-to-r from-rose-50 to-white px-4 py-2.5'>
            <p className='text-[10px] font-bold uppercase tracking-widest text-rose-600'>
              Planning Context
            </p>
          </div>
          {/* Scrollable inner area — left panel scrolls independently */}
          <div className='flex-1 overflow-y-auto p-4 space-y-4'>
            <PlanningContextPanel
              form={form} onFormChange={setForm}
              onPreview={handlePreview} onCreateSession={handleCreateSession}
              previewing={previewing} creating={creating}
              sessionActive={!!activeSession && activeSession.status !== 'CANCELLED'}
              depots={depots}
            />
            <PlanningSessionPanel
              activeSession={activeSession} sessions={sessions}
              planningMethod={form.planningMethod} blockingTotal={blockingTotal}
              canPublish={canPublish} generating={generating} publishing={publishing}
              cancelling={cancelling} hasRoutes={hasRoutes}
              onGenerate={handleGenerate} onPublish={handlePublish} onCancel={handleCancel}
              onSelectSession={(s) => setActiveSessionId(s.id)}
            />
          </div>
        </div>

        {/* ── Center: Map (flex-1) ─────────────────────────────────────────── */}
        <div className='relative flex flex-1 flex-col overflow-hidden'>
          {/* KPI bar — only when session is active */}
          {activeSession && (
            <DispatchKpiCards
              eligible={activeSession.totalEligibleStudents}
              planned={activeSession.totalPlannedStudents}
              unassigned={activeSession.totalUnassignedStudents}
              routes={activeSession.totalRoutes}
            />
          )}

          {/* Map area with toolbar overlay */}
          <div className='relative flex-1 overflow-hidden'>
            {/* Toolbar — top-right overlay */}
            <div className='absolute right-3 top-3 z-[1000]'>
              <MapToolbar
                onFitAll={handleFitAll}
                onFitRoute={handleFitRoute}
                canFitAll={canFitAll}
                canFitRoute={!!selectedRouteId && selectedRouteStops.length > 0}
                isExpanded={false}
                onToggleExpand={() => setIsMapExpanded(true)}
              />
            </div>

            {/* Map is always rendered so Leaflet initialises; empty-state overlays when no data yet */}
            <PlanningMap
              school={school}
              pickupPoints={mapPickupPoints}
              selectedRouteStops={selectedRouteStops}
              selectedRoutePath={selectedRoutePath}
              depots={depots}
              fitTarget={fitTarget}
              fitKey={fitKey}
              isMapExpanded={false}
              className='h-full w-full'
            />
            {!hasMapData && (
              <div className='absolute inset-0 z-[500]'>
                <MapEmptyState step={mapEmptyStep} />
              </div>
            )}
          </div>
        </div>

        {/* ── Right panel: Routes + Detail (400px) ────────────────────────── */}
        <div className='flex w-[400px] shrink-0 flex-col overflow-hidden border-l border-slate-200 bg-white'>
          <div className='shrink-0 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white px-4 py-2.5'>
            <p className='text-[10px] font-bold uppercase tracking-widest text-slate-500'>
              Routes &amp; Detail
            </p>
          </div>
          {/* Scrollable right panel */}
          <div className='flex-1 overflow-y-auto'>
            <PlanningResultsPanel
              preview={preview}
              greedyResult={greedyResult}
              sessionRoutes={sessionRoutes}
              activeSession={activeSession}
              eligibleStudents={eligibleStudents}
              selectedRouteId={selectedRouteId}
              onSelectRoute={handleSelectRoute}
              onCreateManualRoute={handleCreateManualRoute}
              creatingRoute={creatingRoute}
            />
          </div>
        </div>
      </div>

      {cancelDialog}
    </SchoolBusPageShell>
  );
}
