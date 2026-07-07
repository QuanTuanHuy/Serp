'use client';

import React, { useState, useCallback, useMemo } from 'react';
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { toast } from 'sonner';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import {
  PlanningContextPanel,
  type ContextFormState,
} from '../components/planning/PlanningContextPanel';
import { PlanningSessionPanel } from '../components/planning/PlanningSessionPanel';
import { PlanningResultsPanel } from '../components/planning/PlanningResultsPanel';
import { DispatchKpiCards } from '../components/planning/DispatchKpiCards';
import { PlanningMap } from '../components/map/PlanningMap';
import { MapToolbar } from '../components/map/MapToolbar';
import { MapMarkerVisibilityProvider } from '../components/map/MapMarkerVisibilityContext';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
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
  usePublishPlanningSessionMutation,
  useCancelPlanningSessionMutation,
  useGetSessionRoutesQuery,
  useCreateRouteInSessionMutation,
  useGetSessionEligibleStudentsQuery,
  useGetRouteMapQuery,
  useGetSchoolByIdQuery,
  useGetDepotsQuery,
} from '../api/schoolBusApi';
import type {
  SchoolBusPlanningSession,
  SchoolBusPlanningPreview,
  CreateRouteInSessionRequest,
  PlanningPointResponse,
  SchoolBusPlanningPickupPoint,
} from '../types';
import { SCHOOL_BUS_OPTION_QUERY } from '../utils';

// -- Map empty-state component -------------------------------------------------

type MapEmptyStep = 'pick-school' | 'preview' | 'no-coords' | null;

function MapEmptyState({ step }: { step: MapEmptyStep }) {
  const steps: { label: string; done: boolean }[] = [
    {
      label: 'Chọn trường trong thông tin lập kế hoạch',
      done: step !== 'pick-school',
    },
    {
      label: 'Bấm Xem trước nhu cầu để tải điểm đón/trả',
      done: step === null,
    },
  ];

  return (
    <div className='flex h-full flex-col items-center justify-center gap-5 bg-[#f8fafc] px-6'>
      {/* Map pin icon */}
      <div className='flex h-16 w-16 items-center justify-center rounded-2xl border-2 border-dashed border-slate-200 bg-white'>
        <svg
          xmlns='http://www.w3.org/2000/svg'
          className='h-8 w-8 text-slate-300'
          viewBox='0 0 24 24'
          fill='none'
          stroke='currentColor'
          strokeWidth='1.5'
        >
          <path d='M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0' />
          <circle cx='12' cy='10' r='3' />
        </svg>
      </div>

      <div className='text-center'>
        <p className='text-sm font-semibold text-slate-600'>Bản đồ đã sẵn sàng</p>
        <p className='mt-1 text-xs text-slate-400 max-w-[220px]'>
          {step === 'no-coords'
            ? 'Trường học chưa có tọa độ. Hãy bổ sung vĩ độ/kinh độ để hiển thị trên bản đồ.'
            : 'Hoàn tất các bước dưới đây để tải marker.'}
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
                    : 'bg-slate-100 text-slate-700 ring-1 ring-slate-200'
                }`}
              >
                {s.done ? 'OK' : i + 1}
              </span>
              <span
                className={`text-xs leading-5 ${s.done ? 'text-slate-400 line-through' : 'font-medium text-slate-600'}`}
              >
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
  const router = useRouter();
  const pathname = usePathname();

  const [form, setForm] = useState<ContextFormState>({
    schoolId: '',
    serviceDate: new Date().toISOString().slice(0, 10),
    routeDirection: 'OUTBOUND',
  });

  const searchParams = useSearchParams();
  const querySessionId = searchParams.get('sessionId');
  const queryRouteId = searchParams.get('routeId');

  const [sessionError, setSessionError] = useState<string | null>(null);
  const [routeError, setRouteError] = useState<string | null>(null);

  const [isHydrated, setIsHydrated] = useState(false);
  const [preview, setPreview] = useState<SchoolBusPlanningPreview | null>(null);
  const [rightPanelTab, setRightPanelTab] = useState<
    'demand-preview' | 'route-builder'
  >('demand-preview');
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [activeSessionId, setActiveSessionId] = useState<number | null>(null);
  const [selectedRouteId, setSelectedRouteId] = useState<number | null>(null);
  const [isMapExpanded, setIsMapExpanded] = useState(false);

  // -- Fit controls -----------------------------------------------------------
  const [fitTarget, setFitTarget] = useState<'all' | 'route'>('all');
  const [fitKey, setFitKey] = useState(0);

  const handleFitAll = useCallback(() => {
    setFitTarget('all');
    setFitKey((k) => k + 1);
  }, []);

  // -- API mutations ----------------------------------------------------------
  const [previewMutation, { isLoading: previewing }] =
    usePreviewPlanningDemandMutation();
  const [createSession, { isLoading: creating }] =
    useCreatePlanningSessionMutation();
  const [publishSession, { isLoading: publishing }] =
    usePublishPlanningSessionMutation();
  const [cancelSession, { isLoading: cancelling }] =
    useCancelPlanningSessionMutation();
  const [createRouteInSession, { isLoading: creatingRoute }] =
    useCreateRouteInSessionMutation();

  // -- Session queries --------------------------------------------------------
  const { data: sessionsData } = useGetPlanningSessionsQueryQuery();
  const sessions = sessionsData?.data || [];

  const { data: liveSessionData, refetch: refetchSession } =
    useGetPlanningSessionQuery(activeSessionId || 0, {
      skip: !activeSessionId,
      pollingInterval: 0,
    });
  const activeSession: SchoolBusPlanningSession | null =
    liveSessionData?.data ||
    sessions.find((s) => s.id === activeSessionId) ||
    null;

  const {
    data: sessionRoutesData,
    isFetching: fetchingRoutes,
    refetch: refetchRoutes,
  } = useGetSessionRoutesQuery(activeSessionId || 0, {
    skip: !activeSessionId,
  });
  const sessionRoutes = sessionRoutesData?.data || [];

  const { data: eligibleStudentsData, isFetching: fetchingEligible } =
    useGetSessionEligibleStudentsQuery(activeSessionId || 0, {
      skip: !activeSessionId,
    });
  const eligibleStudents = eligibleStudentsData?.data || [];

  // -- School + depots for map ------------------------------------------------
  const schoolIdNum = Number(form.schoolId) || 0;
  const { data: schoolData } = useGetSchoolByIdQuery(schoolIdNum, {
    skip: !schoolIdNum,
  });
  const school = schoolData?.data || null;

  const { data: depotsData } = useGetDepotsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
  });
  const depots = depotsData?.data?.items || [];

  // -- Route map detail (stops + polyline) ------------------------------------
  const { data: selectedRouteMapData } = useGetRouteMapQuery(
    selectedRouteId || 0,
    { skip: !selectedRouteId, refetchOnMountOrArgChange: true }
  );
  const selectedRouteStops = selectedRouteMapData?.data?.stops || [];
  const selectedRoute = selectedRouteMapData?.data?.route || null;
  const selectedRoutePath = selectedRouteMapData?.data?.path || null;

  const selectedRouteInActiveSession = useMemo(
    () =>
      selectedRouteId
        ? sessionRoutes.find((route) => route.id === selectedRouteId) || null
        : null,
    [selectedRouteId, sessionRoutes]
  );
  const hasValidSelectedRoute =
    !!activeSessionId && !!selectedRouteId && !!selectedRouteInActiveSession;
  const mapSelectedRouteStops = hasValidSelectedRoute
    ? selectedRouteStops
    : [];
  const mapSelectedRoutePath = hasValidSelectedRoute ? selectedRoutePath : null;
  const mapSelectedRoute = hasValidSelectedRoute ? selectedRoute : null;
  const canFitSelectedRoute =
    hasValidSelectedRoute && mapSelectedRouteStops.length > 0;

  const handleFitRoute = useCallback(() => {
    if (!canFitSelectedRoute) return;
    setFitTarget('route');
    setFitKey((k) => k + 1);
  }, [canFitSelectedRoute]);

  const clearSessionQueryParams = useCallback(() => {
    const params = new URLSearchParams(searchParams.toString());
    let changed = false;
    if (params.has('sessionId')) {
      params.delete('sessionId');
      changed = true;
    }
    if (params.has('routeId')) {
      params.delete('routeId');
      changed = true;
    }
    if (changed) {
      const queryString = params.toString();
      router.push(queryString ? `${pathname}?${queryString}` : pathname);
    }
  }, [router, pathname, searchParams]);

  const preserveSessionQueryParams = useCallback(
    (sessionId: number, routeId?: number | null) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('sessionId', String(sessionId));
      if (routeId) {
        params.set('routeId', String(routeId));
      } else {
        params.delete('routeId');
      }
      const queryString = params.toString();
      router.replace(queryString ? `${pathname}?${queryString}` : pathname);
    },
    [router, pathname, searchParams]
  );

  // Check session context alignment when planning context changes
  React.useEffect(() => {
    if (!isHydrated) return;

    if (activeSession && activeSession.id === activeSessionId) {
      const isContextMatch =
        Number(form.schoolId) === activeSession.schoolId &&
        form.serviceDate === activeSession.serviceDate &&
        form.routeDirection === activeSession.routeDirection;

      if (!isContextMatch) {
        setActiveSessionId(null);
        setSelectedRouteId(null);
        setPreview(null);
        clearSessionQueryParams();
      }
    }
  }, [
    form.schoolId,
    form.serviceDate,
    form.routeDirection,
    activeSession,
    activeSessionId,
    isHydrated,
    clearSessionQueryParams,
  ]);

  // Reset selected route when active session changes
  React.useEffect(() => {
    setSelectedRouteId(null);
  }, [activeSessionId]);

  // Auto-hydrate session from query params
  React.useEffect(() => {
    if (sessions.length > 0) {
      if (querySessionId) {
        const sessionIdNum = Number(querySessionId);
        const matchedSession = sessions.find((s) => s.id === sessionIdNum);
        if (matchedSession) {
          setActiveSessionId(sessionIdNum);
          setRightPanelTab('route-builder');
          setForm({
            schoolId: String(matchedSession.schoolId),
            serviceDate: matchedSession.serviceDate,
            routeDirection: matchedSession.routeDirection,
          });
          setSessionError(null);
        } else {
          setSessionError('Không tìm thấy phiên lập kế hoạch hoặc phiên không còn khả dụng.');
        }
      }
      setIsHydrated(true);
    }
  }, [querySessionId, sessions]);

  // Auto-hydrate selected route from query params
  React.useEffect(() => {
    if (queryRouteId && activeSessionId) {
      if (!fetchingRoutes) {
        const routeIdNum = Number(queryRouteId);
        const matchedRoute = sessionRoutes.find((r) => r.id === routeIdNum);
        if (matchedRoute) {
          setSelectedRouteId(routeIdNum);
          setRightPanelTab('route-builder');
          // Focus/scroll into route card
          setTimeout(() => {
            const cardEl = document.getElementById(`route-card-${routeIdNum}`);
            if (cardEl) {
              cardEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
              cardEl.classList.add(
                'ring-2',
                'ring-indigo-500',
                'ring-offset-2'
              );
              setTimeout(() => {
                cardEl.classList.remove(
                  'ring-2',
                  'ring-indigo-500',
                  'ring-offset-2'
                );
              }, 3000);
            }
          }, 300);
          setRouteError(null);
        } else {
          setRouteError(
            'Tuyến xe không thuộc về phiên lập kế hoạch đã chọn.'
          );
        }
      }
    } else {
      setRouteError(null);
    }
  }, [queryRouteId, sessionRoutes, fetchingRoutes, activeSessionId]);

  // -- Handlers ---------------------------------------------------------------
  const handlePreview = useCallback(async () => {
    if (!form.schoolId || !form.serviceDate) {
      toast.error('Vui lòng chọn trường học và ngày phục vụ');
      return;
    }
    try {
      setSelectedRouteId(null);
      const res = await previewMutation({
        schoolId: Number(form.schoolId),
        serviceDate: form.serviceDate,
        routeDirection: form.routeDirection,
      }).unwrap();
      setPreview(res.data);
      setRightPanelTab('demand-preview');
      // Auto-fit to show all pickup points
      setFitTarget('all');
      setFitKey((k) => k + 1);
      toast.success('Đã tải bản xem trước nhu cầu');
    } catch {
      toast.error('Không thể tải bản xem trước');
    }
  }, [form, previewMutation]);

  const handleCreateSession = useCallback(async () => {
    try {
      setSelectedRouteId(null);
      setFitTarget('all');
      const res = await createSession({
        schoolId: Number(form.schoolId),
        serviceDate: form.serviceDate,
        routeDirection: form.routeDirection,
      }).unwrap();
      setActiveSessionId(res.data.id);
      setRightPanelTab('route-builder');
      toast.success(`Đã tạo phiên #${res.data.id}`);
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message || 'Không thể tạo phiên');
    }
  }, [form, createSession]);

  const handleCreateManualRoute = useCallback(
    async (req: CreateRouteInSessionRequest) => {
      if (!activeSession) return false;
      try {
        const res = await createRouteInSession({
          sessionId: activeSession.id,
          body: req,
        }).unwrap();
        toast.success('Đã tạo tuyến xe');

        // Invalidate/refetch session summary and routes list to reflect changes immediately
        refetchSession();
        refetchRoutes();

        if (res.data?.id) {
          setSelectedRouteId(res.data.id);
          setFitTarget('route');
          setFitKey((k) => k + 1);
        }
        return true;
      } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message || 'Không thể tạo tuyến');
        return false;
      }
    },
    [activeSession, createRouteInSession, refetchSession, refetchRoutes]
  );

  const handlePublish = useCallback(async () => {
    if (!activeSession) return;
    const sessionId = activeSession.id;
    const routeId = selectedRouteId;
    try {
      const response = await publishSession(sessionId).unwrap();
      const publishedSession = response.data;
      setActiveSessionId(sessionId);
      setRightPanelTab('route-builder');
      setSelectedRouteId(routeId);
      if (publishedSession) {
        setForm({
          schoolId: String(publishedSession.schoolId),
          serviceDate: publishedSession.serviceDate,
          routeDirection: publishedSession.routeDirection,
        });
      }
      preserveSessionQueryParams(sessionId, routeId);
      void refetchSession();
      void refetchRoutes();
      toast.success('Đã phát hành phiên lập kế hoạch!');
    } catch (e: unknown) {
      const err = e as {
        data?: {
          message?: string;
          code?: string;
          data?: {
            blockingRouteCount: number;
            totalBlockingIssues: number;
            blockingRoutes: Array<{
              routeId: number;
              routeCode: string;
              routeName: string;
              issues: Array<{
                issueType: string;
                message: string;
                stopName?: string;
                studentName?: string;
                suggestedFix?: string;
              }>;
            }>;
          };
        };
      };

      const publishVal = err.data?.data;
      if (err.data?.code === 'SESSION_BLOCKING_ISSUES' && publishVal) {
        toast.error(
          <div className='flex flex-col gap-2 max-w-[380px] text-xs leading-normal'>
            <div className='font-bold text-slate-900 flex items-center gap-1.5'>
              <span className='h-2 w-2 rounded-full bg-rose-600' />
              Không thể phát hành: {publishVal.blockingRouteCount} tuyến không đạt
              yêu cầu xác thực
            </div>
            <p className='text-[11px] text-slate-500 font-semibold'>
              Phát hiện tổng cộng {publishVal.totalBlockingIssues} lỗi nghiêm trọng:
            </p>
            <div className='max-h-[220px] overflow-y-auto space-y-2.5 pr-1 border-t border-slate-100 pt-2'>
              {publishVal.blockingRoutes.map((route) => (
                <div key={route.routeId} className='space-y-1'>
                  <div className='font-bold text-rose-800 flex items-center justify-between'>
                    <span>
                      {route.routeCode} - {route.routeName}
                    </span>
                  </div>
                  <ul className='space-y-1.5 pl-3 list-disc text-[11px] text-slate-700 font-medium'>
                    {route.issues.map((issue, idx) => (
                      <li key={idx} className='leading-relaxed'>
                        <span className='font-bold text-slate-800'>
                          {issue.issueType}:
                        </span>{' '}
                        {issue.message}
                        {issue.suggestedFix && (
                          <div className='text-rose-900 font-bold mt-0.5 bg-rose-50/50 px-1.5 py-0.5 rounded border border-rose-100/45'>
                             Sửa: {issue.suggestedFix}
                          </div>
                        )}
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>,
          { duration: 10000 }
        );
      } else {
        toast.error(err?.data?.message || 'Phát hành thất bại');
      }
    }
  }, [
    activeSession,
    selectedRouteId,
    publishSession,
    preserveSessionQueryParams,
    refetchSession,
    refetchRoutes,
  ]);

  const handleCancel = useCallback(() => {
    if (!activeSession) return;
    setCancelDialogOpen(true);
  }, [activeSession]);

  const confirmCancel = useCallback(async () => {
    if (!activeSession) return;
    try {
      await cancelSession(activeSession.id).unwrap();
      setActiveSessionId(null);
      setPreview(null);
      toast.success('Đã hủy phiên lập kế hoạch');
    } catch {
      toast.error('Không thể hủy session');
    } finally {
      setCancelDialogOpen(false);
    }
  }, [activeSession, cancelSession]);

  // -- Route selection with auto-focus map -----------------------------------
  const handleSelectRoute = useCallback((id: number | null) => {
    setSelectedRouteId((prev) => (prev === id ? null : id));
    if (id) {
      setFitTarget('route');
      setFitKey((k) => k + 1);
    }
  }, []);

  const handleSelectSession = useCallback(
    (s: SchoolBusPlanningSession) => {
      setActiveSessionId(s.id);
      setRightPanelTab('route-builder');
      setForm({
        schoolId: String(s.schoolId),
        serviceDate: s.serviceDate,
        routeDirection: s.routeDirection,
      });
      const params = new URLSearchParams(searchParams.toString());
      params.set('sessionId', String(s.id));
      params.delete('routeId');
      const queryString = params.toString();
      router.push(queryString ? `${pathname}?${queryString}` : pathname);
    },
    [router, pathname, searchParams]
  );

  const handleOpenSession = useCallback(
    (id: number) => {
      setActiveSessionId(id);
      setRightPanelTab('route-builder');

      const matchedSession = sessions.find((s) => s.id === id);
      if (matchedSession) {
        setForm({
          schoolId: String(matchedSession.schoolId),
          serviceDate: matchedSession.serviceDate,
          routeDirection: matchedSession.routeDirection,
        });
      }

      const params = new URLSearchParams(searchParams.toString());
      params.set('sessionId', String(id));
      params.delete('routeId');
      const queryString = params.toString();
      router.push(queryString ? `${pathname}?${queryString}` : pathname);
    },
    [sessions, router, pathname, searchParams]
  );

  const handleNewSession = useCallback(() => {
    // Reset service date to today, keep schoolId and routeDirection
    setForm((prev) => ({
      ...prev,
      serviceDate: new Date().toISOString().slice(0, 10),
    }));
    setActiveSessionId(null);
    setSelectedRouteId(null);
    setPreview(null);
    setFitTarget('all');
    setFitKey((k) => k + 1);
    clearSessionQueryParams();
    toast.success('Đã đặt lại không gian làm việc về thông tin lập kế hoạch phiên mới');
  }, [clearSessionQueryParams]);

  // -- Derived values ---------------------------------------------------------
  const unassignedStudents = activeSession?.totalUnassignedStudents || 0;
  const isPreviewContextMatch =
    preview !== null &&
    Number(preview.schoolId) === Number(form.schoolId) &&
    preview.serviceDate === form.serviceDate &&
    preview.routeDirection === form.routeDirection;
  const blockingTotal = 0;
  const hasRoutes = sessionRoutes.length > 0;
  const canPublish =
    !!activeSession &&
    activeSession.status !== 'PUBLISHED' &&
    activeSession.status !== 'CANCELLED' &&
    hasRoutes &&
    unassignedStudents === 0;

  // Map pickup points from preview (or from greedy result when preview was skipped)
  const rawPickupPoints = useMemo<
    (PlanningPointResponse | SchoolBusPlanningPickupPoint)[]
  >(
    () => (preview?.points ? preview.points : (preview?.eligiblePickupPoints || [])),
    [preview?.eligiblePickupPoints, preview?.points]
  );
  const mapPickupPoints = useMemo(
    () =>
      rawPickupPoints.map((pp) => ({
        pickupPointId: 'pointId' in pp ? pp.pointId : pp.pickupPointId,
        pickupPointName: 'pointName' in pp ? pp.pointName : pp.pickupPointName,
        latitude: pp.latitude,
        longitude: pp.longitude,
        studentCount: pp.studentCount,
        pointRole: 'pointRole' in pp ? pp.pointRole : undefined,
      })),
    [rawPickupPoints]
  );

  const hasMapData =
    (school != null && typeof school.latitude === 'number') ||
    mapPickupPoints.some((pp) => typeof pp.latitude === 'number') ||
    depots.some((d) => typeof d.latitude === 'number');

  // Enable Fit All only when there are actual coordinate-bearing markers
  const canFitAll =
    typeof school?.latitude === 'number' ||
    mapPickupPoints.some((pp) => typeof pp.latitude === 'number') ||
    depots.some((d) => typeof d.latitude === 'number');

  // Map empty-state context
  const mapEmptyStep: 'pick-school' | 'preview' | 'no-coords' | null =
    !hasMapData
      ? !form.schoolId
        ? 'pick-school'
        : school && typeof school.latitude !== 'number'
          ? 'no-coords'
          : 'preview'
      : null;

  const cancelDialog = (
    <AlertDialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
      <AlertDialogContent className='school-bus-shell'>
        <AlertDialogHeader>
          <AlertDialogTitle>Hủy phiên lập tuyến?</AlertDialogTitle>
          <AlertDialogDescription>
            Bạn có chắc chắn muốn hủy phiên này không? Các tuyến nháp sẽ bị xóa
            mềm và thao tác này không thể hoàn tác.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={cancelling}>Đóng</AlertDialogCancel>
          <AlertDialogAction
            onClick={confirmCancel}
            disabled={cancelling}
            className='bg-destructive text-white hover:bg-destructive/90'
          >
            Xác nhận hủy
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );

  // -- EXPANDED MODE ----------------------------------------------------------
  if (isMapExpanded) {
    return (
      <MapMarkerVisibilityProvider>
        {/* Fixed full-viewport overlay - prevents any body scroll */}
        <div className='school-bus-shell fixed inset-0 z-[900] flex overflow-hidden bg-background/90 text-foreground backdrop-blur-[1px]'>
          {/* Left panel - own scroll, no page scroll */}
          <div className='flex h-full w-[320px] shrink-0 flex-col overflow-hidden border-r border-border bg-card text-card-foreground shadow-xl'>
            <div className='shrink-0 border-b border-border bg-muted/40 px-4 py-3'>
              <p className='text-[10px] font-bold uppercase tracking-widest text-muted-foreground'>
                Nền tảng xe bus trường học
              </p>
              <h2 className='mt-0.5 text-base font-bold text-foreground'>
                Lập tuyến xe
              </h2>
            </div>
            {/* Scrollable inner area */}
            <div className='flex-1 space-y-4 overflow-y-auto bg-muted/20 p-4'>
              <PlanningContextPanel
                form={form}
                onFormChange={setForm}
                onPreview={handlePreview}
                onCreateSession={handleCreateSession}
                previewing={previewing}
                creating={creating}
                canCreate={isPreviewContextMatch ? !!preview?.canCreate : false}
                createDisabledReason={
                  isPreviewContextMatch
                    ? preview?.createDisabledReason || undefined
                    : undefined
                }
                isPreviewContextMatch={isPreviewContextMatch}
                onNewSession={handleNewSession}
              />
              <PlanningSessionPanel
                activeSession={activeSession}
                sessions={sessions}
                blockingTotal={blockingTotal}
                canPublish={canPublish}
                publishing={publishing}
                cancelling={cancelling}
                onPublish={handlePublish}
                onCancel={handleCancel}
                onSelectSession={handleSelectSession}
                hidePastSessions
              />
            </div>
          </div>

          {/* Map - fills all remaining space */}
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
            <SchoolBusMapWorkspace
              flat
              mapHeightClassName='h-full'
              isExpanded={true}
              onToggleExpand={() => setIsMapExpanded(false)}
              map={
                <PlanningMap
                  school={school}
                  pickupPoints={mapPickupPoints}
                  selectedRouteStops={mapSelectedRouteStops}
                  selectedRoutePath={mapSelectedRoutePath}
                  selectedRoute={mapSelectedRoute}
                  depots={depots}
                  fitTarget={fitTarget}
                  fitKey={fitKey}
                  isMapExpanded={true}
                  className='h-full w-full'
                />
              }
              legend={
                hasMapData ? (
                  <SchoolBusMapLegend defaultCollapsed={false} />
                ) : undefined
              }
              onFitAll={handleFitAll}
              onFitRoute={handleFitRoute}
              canFitAll={canFitAll}
              canFitRoute={canFitSelectedRoute}
              fitRouteLabel='Thu phóng tuyến'
            />
          </div>
        </div>
        {cancelDialog}
      </MapMarkerVisibilityProvider>
    );
  }

  // -- NORMAL MODE - 3-column dispatch workspace ------------------------------
  return (
    <MapMarkerVisibilityProvider>
      <SchoolBusPageShell
        compact
        title='Lập tuyến'
        description='Xây dựng tuyến học sinh theo điểm đón/trả, sức chứa xe và ngày phục vụ.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
              { label: 'Điều phối', href: '/school-bus/dispatch' },
              { label: 'Lập tuyến', current: true },
            ]}
          />
        }
      >
        {(sessionError || routeError) && (
          <div className='mb-4 flex flex-col gap-2'>
            {sessionError && (
              <div className='flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-xs font-semibold text-red-800 shadow-sm'>
                <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-100 text-red-700 text-[10px] font-extrabold border border-red-200'>
                  Cảnh báo:
                </span>
                <span>{sessionError}</span>
              </div>
            )}
            {routeError && (
              <div className='flex items-center gap-2.5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-xs font-semibold text-amber-850 shadow-sm'>
                <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-amber-100 text-amber-700 text-[10px] font-extrabold border border-amber-200'>
                  Cảnh báo:
                </span>
                <span>{routeError}</span>
              </div>
            )}
          </div>
        )}

        {/* Workspace container - fills remaining viewport height below the compact hero */}
        <div
          className='flex overflow-hidden rounded-2xl border border-slate-200 bg-slate-100 shadow-[0_4px_20px_rgba(15,23,42,0.06)]'
          style={{ height: 'calc(100vh - 175px)', minHeight: '580px' }}
        >
          {/* -- Left panel: Context + Session (340px) ------------------------ */}
          <div className='flex w-[340px] shrink-0 flex-col overflow-hidden border-r border-slate-200 bg-white'>
            <div className='shrink-0 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white px-4 py-2.5'>
              <p className='text-[10px] font-extrabold uppercase tracking-widest text-slate-400'>
                Bảng điều khiển
              </p>
            </div>
            {/* Scrollable inner area - left panel scrolls independently */}
            <div className='flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/20'>
              <PlanningContextPanel
                form={form}
                onFormChange={setForm}
                onPreview={handlePreview}
                onCreateSession={handleCreateSession}
                previewing={previewing}
                creating={creating}
                canCreate={isPreviewContextMatch ? !!preview?.canCreate : false}
                createDisabledReason={
                  isPreviewContextMatch
                    ? preview?.createDisabledReason || undefined
                    : undefined
                }
                isPreviewContextMatch={isPreviewContextMatch}
                onNewSession={handleNewSession}
              />
              <PlanningSessionPanel
                activeSession={activeSession}
                sessions={sessions}
                blockingTotal={blockingTotal}
                canPublish={canPublish}
                publishing={publishing}
                cancelling={cancelling}
                onPublish={handlePublish}
                onCancel={handleCancel}
                onSelectSession={handleSelectSession}
              />
            </div>
          </div>

          {/* -- Center: Map (flex-1) ------------------------------------------- */}
          <div className='relative flex flex-1 flex-col overflow-hidden'>
            {/* KPI bar - only when session is active */}
            {activeSession && (
              <DispatchKpiCards
                eligible={activeSession.totalEligibleStudents}
                planned={activeSession.totalPlannedStudents}
                unassigned={activeSession.totalUnassignedStudents}
                routes={activeSession.totalRoutes}
              />
            )}

            {/* Map area with toolbar overlay */}
            {/* Map area with workspace wrapper */}
            <div className='relative flex-1 overflow-hidden'>
              <SchoolBusMapWorkspace
                flat
                mapHeightClassName='h-full'
                isExpanded={false}
                onToggleExpand={() => setIsMapExpanded(true)}
                map={
                  <PlanningMap
                    school={school}
                    pickupPoints={mapPickupPoints}
                    selectedRouteStops={mapSelectedRouteStops}
                    selectedRoutePath={mapSelectedRoutePath}
                    selectedRoute={mapSelectedRoute}
                    depots={depots}
                    fitTarget={fitTarget}
                    fitKey={fitKey}
                    isMapExpanded={false}
                    className='h-full w-full'
                  />
                }
                legend={
                  hasMapData ? (
                    <SchoolBusMapLegend defaultCollapsed={true} />
                  ) : undefined
                }
                onFitAll={handleFitAll}
                onFitRoute={handleFitRoute}
                canFitAll={canFitAll}
                canFitRoute={canFitSelectedRoute}
                fitRouteLabel='Thu phóng tuyến'
              />
              {!hasMapData && (
                <div className='absolute inset-0 z-[500]'>
                  <MapEmptyState step={mapEmptyStep} />
                </div>
              )}
            </div>
          </div>

          {/* -- Right panel: Routes + Detail (400px) -------------------------- */}
          <div className='flex w-[400px] shrink-0 flex-col overflow-hidden border-l border-slate-200 bg-white'>
            <div className='shrink-0 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white px-4 py-2.5'>
              <p className='text-[10px] font-extrabold uppercase tracking-widest text-slate-400'>
                Tuyến &amp; Chi tiết
              </p>
            </div>
            {/* Scrollable right panel */}
            <div className='flex-1 overflow-y-auto'>
              <PlanningResultsPanel
                preview={preview}
                sessionRoutes={sessionRoutes}
                activeSession={activeSession}
                eligibleStudents={eligibleStudents}
                loadingEligible={fetchingEligible}
                selectedRouteId={selectedRouteId}
                onSelectRoute={handleSelectRoute}
                onCreateManualRoute={handleCreateManualRoute}
                creatingRoute={creatingRoute}
                refreshingRoutes={fetchingRoutes}
                form={form}
                rightPanelTab={rightPanelTab}
                onTabChange={setRightPanelTab}
                onPreviewDemandClick={handlePreview}
                previewing={previewing}
                onOpenSession={handleOpenSession}
              />
            </div>
          </div>
        </div>

        {cancelDialog}
      </SchoolBusPageShell>
    </MapMarkerVisibilityProvider>
  );
}
