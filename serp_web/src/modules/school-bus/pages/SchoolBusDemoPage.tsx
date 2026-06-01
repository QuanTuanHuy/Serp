'use client';

import * as React from 'react';
import {
  Bus,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Pause,
  Play,
  RotateCcw,
  Square,
  Timer,
  Zap,
  Gauge,
  MapPin,
  CalendarDays,
  Info,
  Clock,
  ArrowRight,
  AlertTriangle,
  Warehouse,
  GraduationCap,
  Bell,
  Wifi,
  WifiOff,
} from 'lucide-react';
import { toast } from 'sonner';
import { Client } from '@stomp/stompjs';
import { Button, Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useCreateDemoSessionMutation,
  useGetDemoSessionByTripQuery,
  useGetDemoSessionEventsQuery,
  useGetRouteByIdQuery,
  useGetRoutePathQuery,
  useGetTripsQuery,
  useJumpDemoSessionToStartMutation,
  useJumpDemoSessionToEndMutation,
  useJumpDemoSessionToStopMutation,
  usePauseDemoSessionMutation,
  useResumeDemoSessionMutation,
  useStartDemoSessionMutation,
  useStopDemoSessionMutation,
  useTickDemoSessionMutation,
  useUpdateDemoAutomationSettingsMutation,
} from '../api/schoolBusApi';
import {
  connectSchoolBusDemoSocket,
  disconnectDemoSocket,
  subscribeDemoPosition,
  subscribeDemoEvents,
  type DemoPositionMessage,
  type DemoEventMessage,
} from '../api/schoolBusDemoSocket';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { SchoolBusCheckbox } from '../components/ui/SchoolBusCheckbox';
import { DemoMap } from '../components/map/DemoMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { MapMarkerVisibilityProvider } from '../components/map/MapMarkerVisibilityContext';
import { getPageItems } from '../utils';

// ── Event type config ─────────────────────────────────────────────────────────

const EVENT_DISPLAY_CONFIG: Record<string, { label: string; className: string }> = {
  DEMO_CREATED: { label: 'Session created', className: 'bg-blue-50 text-blue-700 border-blue-100 border' },
  DEMO_STARTED: { label: 'Simulation started', className: 'bg-emerald-50 text-emerald-705 border-emerald-100 border' },
  DEMO_TICK: { label: 'Position updated', className: 'bg-slate-50 text-slate-600 border-slate-200 border' },
  DEMO_PAUSED: { label: 'Simulation paused', className: 'bg-amber-50 text-amber-700 border-amber-100 border' },
  DEMO_RESUMED: { label: 'Simulation resumed', className: 'bg-blue-55 text-blue-750 border-blue-150 border' },
  DEMO_JUMPED: { label: 'Jumped to stop', className: 'bg-violet-50 text-violet-700 border-violet-100 border' },
  DEMO_COMPLETED: { label: 'Simulation completed', className: 'bg-emerald-50 text-emerald-700 border-emerald-200 border' },
  DEMO_STOPPED: { label: 'Simulation stopped', className: 'bg-slate-100 text-slate-650 border-slate-250 border' },
  DEMO_ERROR: { label: 'Simulation error', className: 'bg-red-50 text-red-700 border-red-100 border' },
  DEMO_AUTO_ARRIVED_STOP: { label: 'Auto arrived stop', className: 'bg-sky-50 text-sky-700 border-sky-100 border' },
  DEMO_AUTO_DEPARTED_STOP: { label: 'Auto departed stop', className: 'bg-teal-50 text-teal-700 border-teal-105 border' },
  DEMO_AUTO_ATTENDANCE: { label: 'Auto attendance logged', className: 'bg-purple-50 text-purple-700 border-purple-100 border' },
  DEMO_AUTOMATION_SKIPPED: { label: 'Automation skipped stop', className: 'bg-orange-50 text-orange-700 border-orange-105 border' },
  DEMO_AUTOMATION_ERROR: { label: 'Automation error', className: 'bg-red-50 text-red-750 border-red-150 border' },
};

const sessionStatusMap: Record<string, { label: string; className: string }> = {
  READY: { label: 'Ready', className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50' },
  RUNNING: { label: 'Running', className: 'border-blue-200 bg-blue-50 text-blue-700 hover:bg-blue-50' },
  PAUSED: { label: 'Paused', className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50' },
  STOPPED: { label: 'Stopped', className: 'border-slate-200 bg-slate-100 text-slate-500 hover:bg-slate-100' },
  COMPLETED: { label: 'Completed', className: 'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  ERROR: { label: 'Error', className: 'border-red-200 bg-red-55 text-red-700 hover:bg-red-55' },
};

const renderFriendlyBadge = (status: string) => {
  const normalized = (status || '').toUpperCase();
  const config = sessionStatusMap[normalized] || {
    label: normalized,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge className={cn('rounded-full px-2.5 py-0.5 text-[11px] font-bold shadow-none border shrink-0 hover:bg-transparent', config.className)}>
      {config.label}
    </Badge>
  );
};

// ── Main Page ─────────────────────────────────────────────────────────────────

export function SchoolBusDemoPage() {
  // ─── Trip selection ───────────────────────────────────────────────
  const { data: tripsData } = useGetTripsQuery({
    page: 0,
    size: 50,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const trips = getPageItems(tripsData?.data);
  const [selectedTripId, setSelectedTripId] = React.useState<number | null>(null);

  React.useEffect(() => {
    if (!selectedTripId && trips.length > 0) {
      setSelectedTripId(trips[0].id);
    }
  }, [selectedTripId, trips]);

  const selectedTrip = trips.find((t) => t.id === selectedTripId) || null;

  // ─── Demo session ────────────────────────────────────────────────
  const { data: sessionData } = useGetDemoSessionByTripQuery(selectedTripId as number, {
    skip: !selectedTripId,
  });
  const demo = sessionData?.data;
  const sessionId = demo?.id;

  const { data: eventsData } = useGetDemoSessionEventsQuery(sessionId as number, {
    skip: !sessionId,
  });

  // ─── Route data (for map) ───────────────────────────────────────
  const routeId = selectedTrip?.routeId;
  const { data: routeDetailData } = useGetRouteByIdQuery(routeId as number, {
    skip: !routeId,
  });
  const routeStops = routeDetailData?.data?.stops ?? [];

  const { data: routePathData } = useGetRoutePathQuery(routeId as number, {
    skip: !routeId,
  });
  const routePath = routePathData?.data ?? null;

  // ─── WebSocket real-time overlay & status state ──────────────────
  const [wsPosition, setWsPosition] = React.useState<DemoPositionMessage | null>(null);
  const [wsEvents, setWsEvents] = React.useState<DemoEventMessage[]>([]);
  const [wsState, setWsState] = React.useState<'Live' | 'Offline'>('Offline');
  const clientRef = React.useRef<Client | null>(null);

  React.useEffect(() => {
    setWsPosition(null);
    setWsEvents([]);
  }, [sessionId]);

  React.useEffect(() => {
    if (!sessionId) return;

    const client = connectSchoolBusDemoSocket();
    clientRef.current = client;

    // Listen STOMP connections
    const originalOnConnect = client.onConnect;
    client.onConnect = (frame) => {
      originalOnConnect?.(frame);
      setWsState('Live');
    };

    const originalOnWebSocketClose = client.onWebSocketClose;
    client.onWebSocketClose = (evt) => {
      originalOnWebSocketClose?.(evt);
      setWsState('Offline');
    };

    subscribeDemoPosition(client, sessionId, (msg) => {
      setWsPosition(msg);
    });

    subscribeDemoEvents(client, sessionId, (msg) => {
      setWsEvents((prev) => [msg, ...prev]);
    });

    return () => {
      disconnectDemoSocket(client);
      clientRef.current = null;
      setWsState('Offline');
    };
  }, [sessionId]);

  // ─── State merge ────────────────────────────────────────────────
  const currentStatus = wsPosition?.status || demo?.status;
  const currentProgress = wsPosition?.progressPercent ?? demo?.progressPercent ?? 0;
  const currentLat = wsPosition?.currentLatitude ?? demo?.currentLatitude ?? null;
  const currentLng = wsPosition?.currentLongitude ?? demo?.currentLongitude ?? null;
  const currentStopOrder = wsPosition?.currentStopOrder ?? demo?.currentStopOrder ?? null;
  const currentSpeed = demo?.speedMultiplier || 1;

  const busPosition = currentLat != null && currentLng != null
    ? { lat: currentLat, lng: currentLng }
    : null;

  // Merge events — dedupe by composite key (eventType + eventTime + progress + stopOrder)
  const restEvents = eventsData?.data || demo?.events || [];
  const mergedEvents = React.useMemo(() => {
    if (wsEvents.length === 0) return restEvents;
    const wsConverted = wsEvents.map((e) => ({
      id: 0,
      demoSessionId: sessionId || 0,
      eventType: e.eventType,
      eventTime: e.eventTime,
      payloadJson: e.payloadJson,
      progressPercent: e.progressPercent,
      currentStopOrder: e.currentStopOrder,
    }));
    const eventKey = (e: any) =>
      `${e.eventType}|${e.eventTime}|${e.progressPercent ?? ''}|${e.currentStopOrder ?? ''}`;
    const restKeys = new Set(restEvents.map(eventKey));
    const newWs = wsConverted.filter((e) => !restKeys.has(eventKey(e)));
    return [...newWs, ...restEvents];
  }, [wsEvents, restEvents, sessionId]);

  // ─── Mutations ──────────────────────────────────────────────────
  const [createSession] = useCreateDemoSessionMutation();
  const [startDemo] = useStartDemoSessionMutation();
  const [pauseDemo] = usePauseDemoSessionMutation();
  const [resumeDemo] = useResumeDemoSessionMutation();
  const [stopDemo] = useStopDemoSessionMutation();
  const [tickDemo, { isLoading: isTicking }] = useTickDemoSessionMutation();
  const [jumpToStart] = useJumpDemoSessionToStartMutation();
  const [jumpToEnd] = useJumpDemoSessionToEndMutation();
  const [jumpToStop] = useJumpDemoSessionToStopMutation();
  const [updateAutomation] = useUpdateDemoAutomationSettingsMutation();

  const action = async (label: string, fn: () => Promise<any>) => {
    try {
      const response = await fn();
      toast.success(response.message || `${label} completed`);
    } catch (error: any) {
      toast.error(error?.data?.message || `${label} failed`);
    }
  };

  const handleStart = async () => {
    if (!selectedTripId) return;
    if (!sessionId || currentStatus === 'COMPLETED' || currentStatus === 'STOPPED') {
      await action('Create session', () =>
        createSession({ tripId: selectedTripId }).unwrap()
      );
      return;
    }
    await action('Start demo', () => startDemo(sessionId).unwrap());
  };

  // ─── Jump navigation ──────────────────────────────────────────
  const canJump =
    !!sessionId &&
    currentStatus !== 'COMPLETED' &&
    currentStatus !== 'STOPPED' &&
    !!currentStatus;

  const handleJump = async (dir: 'start' | 'end' | 'prev' | 'next') => {
    if (!sessionId) return;
    setAutoTick(false); // Turn off auto-tick on any jump
    switch (dir) {
      case 'start':
        await action('Jump to start', () => jumpToStart(sessionId).unwrap());
        break;
      case 'end':
        await action('Jump to end', () => jumpToEnd(sessionId).unwrap());
        break;
      case 'prev':
        if (currentStopOrder && currentStopOrder > 1) {
          await action('Jump prev stop', () =>
            jumpToStop({ sessionId, stopOrder: currentStopOrder - 1 }).unwrap()
          );
        }
        break;
      case 'next':
        if (currentStopOrder) {
          await action('Jump next stop', () =>
            jumpToStop({ sessionId, stopOrder: currentStopOrder + 1 }).unwrap()
          );
        }
        break;
    }
  };

  // ─── Auto tick ─────────────────────────────────────────────────
  const [autoTick, setAutoTick] = React.useState(false);
  const autoTickRef = React.useRef(false);
  const tickingRef = React.useRef(false);
  autoTickRef.current = autoTick;

  // Clear auto tick when status changes to non-RUNNING
  React.useEffect(() => {
    if (currentStatus !== 'RUNNING') {
      setAutoTick(false);
    }
  }, [currentStatus]);

  React.useEffect(() => {
    if (!autoTick || !sessionId || currentStatus !== 'RUNNING') return;

    const interval = setInterval(async () => {
      if (!autoTickRef.current || tickingRef.current) return;
      tickingRef.current = true;
      try {
        await tickDemo(sessionId).unwrap();
      } catch {
        setAutoTick(false);
      } finally {
        tickingRef.current = false;
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [autoTick, sessionId, currentStatus, tickDemo]);

  // ─── Map fit key ───────────────────────────────────────────────
  const [fitKey, setFitKey] = React.useState(0);
  React.useEffect(() => {
    if (routeStops.length > 0) setFitKey((k) => k + 1);
  }, [routeStops.length]);

  return (
    <MapMarkerVisibilityProvider>
      <SchoolBusPageShell
        title='Demo Simulation Workspace'
        description='Real-time telemetry playback and WebSocket automation simulation.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Demo Simulation', current: true },
            ]}
          />
        }
      >
        <div className='flex flex-col gap-5'>
          {/* A: Demo Session Header */}
          <div className='flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm'>
            <div className='flex flex-wrap items-center gap-3.5 min-w-0'>
              <div className='w-72'>
                <SchoolBusSelect
                  value={selectedTripId ?? ''}
                  onChange={(val) => setSelectedTripId(val ? Number(val) : null)}
                  placeholder='Select trip to simulate...'
                  options={trips.map((trip) => ({
                    label: `${trip.tripCode} — ${trip.routeCode} (${trip.routeDirection === 'RETURN' ? 'Return' : 'Outbound'})`,
                    value: trip.id
                  }))}
                  searchable
                />
              </div>

              {selectedTrip && (
                <div className='flex flex-wrap items-center gap-2.5 text-xs text-slate-500 font-semibold border-l border-slate-100 pl-4'>
                  <span className='flex items-center gap-1.5 bg-slate-50 border border-slate-150 rounded-lg px-2 py-1'>
                    <CalendarDays className='h-3.5 w-3.5 text-slate-400' />
                    {selectedTrip.serviceDate}
                  </span>
                  <span className='flex items-center gap-1.5 bg-slate-50 border border-slate-150 rounded-lg px-2 py-1'>
                    <Gauge className='h-3.5 w-3.5 text-slate-450' />
                    Speed: x{currentSpeed}
                  </span>
                  {sessionId && (
                    <span className={cn(
                      'flex items-center gap-1 border rounded-lg px-2.5 py-1 text-[11px] font-bold shadow-xs',
                      wsState === 'Live'
                        ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
                        : 'border-slate-200 bg-slate-50 text-slate-400'
                    )}>
                      {wsState === 'Live' ? <Wifi className='h-3 w-3 shrink-0' /> : <WifiOff className='h-3 w-3 shrink-0' />}
                      {wsState} connection
                    </span>
                  )}
                </div>
              )}
            </div>

            {selectedTrip && (
              <div className='flex items-center gap-2.5 shrink-0'>
                {renderFriendlyBadge(currentStatus || 'NO_SESSION')}
                <span className='text-xs font-extrabold text-slate-800 bg-slate-100 border border-slate-200/80 rounded-lg px-2 py-1 shadow-2xs'>
                  Progress: {Math.round(currentProgress)}%
                </span>
              </div>
            )}
          </div>

          {/* Banners for end states */}
          {(currentStatus === 'STOPPED' || currentStatus === 'COMPLETED') && (
            <div className='flex items-center gap-2.5 bg-slate-50 border border-slate-200 text-slate-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
              <Info className='h-4.5 w-4.5 text-slate-500 shrink-0' />
              <span>Demo session ended. Create a new session to run the simulation again.</span>
            </div>
          )}

          {/* B: Main workspace: map + controls panels */}
          <div className='grid gap-5 xl:grid-cols-[1fr_390px] min-h-[600px] items-start'>
            {/* Map Column */}
            <div className='relative h-[650px] overflow-hidden rounded-2xl border border-slate-200 shadow-xs bg-slate-50'>
              {routeStops.length > 0 ? (
                <>
                  <DemoMap
                    stops={routeStops}
                    routePath={routePath}
                    busPosition={busPosition}
                    currentStopOrder={currentStopOrder}
                    className='h-full w-full rounded-2xl'
                    fitKey={fitKey}
                  />

                  {/* Floating Legend */}
                  <SchoolBusMapLegend className='absolute bottom-3 left-3 z-[1000] shadow-sm max-w-[190px]' />

                  {/* Floating center bus button */}
                  {busPosition && (
                    <button
                      type='button'
                      className='absolute bottom-3 right-3 z-[1000] rounded-full border border-slate-200 bg-white p-2.5 shadow-md hover:bg-slate-50 transition-all outline-none'
                      title='Center on bus'
                      onClick={() => setFitKey((k) => k + 1)}
                    >
                      <Bus className='h-4.5 w-4.5 text-[#C81E3A]' />
                    </button>
                  )}
                </>
              ) : (
                <div className='flex h-full flex-col items-center justify-center gap-3 text-center p-6'>
                  <div className='flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100 text-slate-400 border border-slate-200/80 shadow-2xs'>
                    <MapPin className='h-6 w-6' />
                  </div>
                  <h3 className='font-bold text-slate-800 text-sm'>No Route Loaded</h3>
                  <p className='text-xs text-slate-400 max-w-xs'>
                    Select a trip from the dropdown above to load the route stops, path coordinates, and simulation controls.
                  </p>
                </div>
              )}
            </div>

            {/* Right Control Panels Column */}
            <div className='flex flex-col gap-4 max-h-[650px] overflow-y-auto pr-1'>
              {/* 1. Session Controls */}
              <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-4'>
                <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                  Session Controls
                </p>

                <div className='flex flex-wrap gap-2'>
                  {/* Create / Start Playback */}
                  <Button
                    size='sm'
                    disabled={
                      !selectedTripId ||
                      currentStatus === 'RUNNING' ||
                      currentStatus === 'PAUSED'
                    }
                    onClick={handleStart}
                    className='rounded-full font-bold shadow-none'
                  >
                    <Play className='mr-1.5 h-3.5 w-3.5' />
                    {!sessionId || currentStatus === 'COMPLETED' || currentStatus === 'STOPPED'
                      ? 'New demo session'
                      : currentStatus === 'READY'
                        ? 'Start'
                        : 'Restart demo'}
                  </Button>

                  {/* Step once (Tick) */}
                  <Button
                    size='sm'
                    variant='outline'
                    disabled={!sessionId || currentStatus !== 'RUNNING' || isTicking}
                    onClick={() => action('Step once', () => tickDemo(sessionId!).unwrap())}
                    className='rounded-full font-bold shadow-none'
                  >
                    <ChevronRight className='mr-1.5 h-3.5 w-3.5' />
                    Step once
                  </Button>

                  {/* Auto Play toggle */}
                  <Button
                    size='sm'
                    variant={autoTick ? 'default' : 'outline'}
                    disabled={!sessionId || currentStatus !== 'RUNNING'}
                    onClick={() => setAutoTick((v) => !v)}
                    className={cn(
                      'rounded-full font-bold shadow-none',
                      autoTick ? 'bg-emerald-600 hover:bg-emerald-700 border-0 text-white' : ''
                    )}
                  >
                    <Zap className='mr-1.5 h-3.5 w-3.5' />
                    {autoTick ? 'Auto playing' : 'Auto play'}
                  </Button>

                  {/* Pause */}
                  <Button
                    size='sm'
                    variant='outline'
                    disabled={!sessionId || currentStatus !== 'RUNNING'}
                    onClick={() => action('Pause', () => pauseDemo(sessionId!).unwrap())}
                    className='rounded-full font-bold shadow-none'
                  >
                    <Pause className='mr-1.5 h-3.5 w-3.5' />
                    Pause
                  </Button>

                  {/* Resume */}
                  <Button
                    size='sm'
                    variant='outline'
                    disabled={!sessionId || currentStatus !== 'PAUSED'}
                    onClick={() => action('Resume', () => resumeDemo(sessionId!).unwrap())}
                    className='rounded-full font-bold shadow-none'
                  >
                    <RotateCcw className='mr-1.5 h-3.5 w-3.5' />
                    Resume
                  </Button>

                  {/* Stop */}
                  <Button
                    size='sm'
                    variant='outline'
                    disabled={
                      !sessionId ||
                      currentStatus === 'COMPLETED' ||
                      currentStatus === 'STOPPED' ||
                      !currentStatus
                    }
                    onClick={() => action('Stop', () => stopDemo(sessionId!).unwrap())}
                    className='rounded-full font-bold border-red-200 text-red-650 hover:bg-red-50 shadow-none'
                  >
                    <Square className='mr-1.5 h-3.5 w-3.5' />
                    Stop
                  </Button>
                </div>

                {/* Progress bar */}
                {sessionId && (
                  <div className='space-y-1.5 pt-1.5 border-t border-slate-100'>
                    <div className='flex items-center justify-between text-xs text-slate-500 font-semibold'>
                      <span>Playback Progress</span>
                      <span className='font-bold text-slate-700'>{Math.round(currentProgress)}%</span>
                    </div>
                    <div className='h-2 w-full overflow-hidden rounded-full bg-slate-100 border border-slate-200/50'>
                      <div
                        className={cn(
                          'h-full rounded-full transition-all duration-300',
                          currentStatus === 'COMPLETED'
                            ? 'bg-emerald-500'
                            : currentStatus === 'STOPPED' || currentStatus === 'ERROR'
                            ? 'bg-slate-400'
                            : 'bg-[#C81E3A]'
                        )}
                        style={{ width: `${Math.min(100, currentProgress)}%` }}
                      />
                    </div>
                  </div>
                )}
              </div>

              {/* 2. Quick Navigation */}
              {sessionId && (
                <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-3.5'>
                  <div>
                    <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                      Quick Navigation
                    </p>
                    <p className='text-[10px] text-slate-400 mt-1 font-semibold leading-relaxed'>
                      Jump moves the bus marker. If automation is enabled, trip logs and attendance may also update.
                    </p>
                  </div>

                  <div className='grid grid-cols-4 gap-1.5'>
                    <Button
                      size='sm'
                      variant='outline'
                      disabled={!canJump}
                      onClick={() => handleJump('start')}
                      className='rounded-xl text-[11px] font-bold shadow-none px-1 h-8'
                      title='Jump to start'
                    >
                      <ChevronsLeft className='h-3.5 w-3.5 mr-0.5' />
                      Start
                    </Button>
                    <Button
                      size='sm'
                      variant='outline'
                      disabled={!canJump || !currentStopOrder || currentStopOrder <= 1}
                      onClick={() => handleJump('prev')}
                      className='rounded-xl text-[11px] font-bold shadow-none px-1 h-8'
                      title='Previous stop'
                    >
                      <ChevronLeft className='h-3.5 w-3.5 mr-0.5' />
                      Prev
                    </Button>
                    <Button
                      size='sm'
                      variant='outline'
                      disabled={!canJump || !currentStopOrder || currentStopOrder >= routeStops.length}
                      onClick={() => handleJump('next')}
                      className='rounded-xl text-[11px] font-bold shadow-none px-1 h-8'
                      title='Next stop'
                    >
                      Next
                      <ChevronRight className='h-3.5 w-3.5 ml-0.5' />
                    </Button>
                    <Button
                      size='sm'
                      variant='outline'
                      disabled={!canJump}
                      onClick={() => handleJump('end')}
                      className='rounded-xl text-[11px] font-bold shadow-none px-1 h-8'
                      title='Jump to end'
                    >
                      End
                      <ChevronsRight className='h-3.5 w-3.5 ml-0.5' />
                    </Button>
                  </div>
                </div>
              )}

              {/* 3. Automation Settings */}
              {sessionId && (
                <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-3.5'>
                  <div>
                    <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                      Automation settings
                    </p>
                    <p className='text-[10px] text-amber-600 bg-amber-50 border border-amber-100 rounded-lg p-2 mt-2 font-semibold leading-relaxed'>
                      ⚠️ Automation can mutate trip stop logs and attendance records.
                    </p>
                  </div>

                  <div className='flex flex-col gap-2.5 text-xs font-semibold'>
                    <label className='flex items-center gap-2 cursor-pointer select-none'>
                      <SchoolBusCheckbox
                        checked={!!demo?.autoAdvanceStops}
                        disabled={currentStatus === 'COMPLETED' || currentStatus === 'STOPPED'}
                        onCheckedChange={(checked) => {
                          const val = Boolean(checked);
                          updateAutomation({
                            sessionId: sessionId!,
                            autoAdvanceStops: val,
                            autoAttendance: val ? demo?.autoAttendance ?? false : false,
                          });
                        }}
                      />
                      <span className='text-slate-700'>Auto-advance stops</span>
                    </label>

                    <label className='flex items-center gap-2 cursor-pointer select-none'>
                      <SchoolBusCheckbox
                        checked={!!demo?.autoAttendance}
                        disabled={
                          currentStatus === 'COMPLETED' ||
                          currentStatus === 'STOPPED' ||
                          !demo?.autoAdvanceStops
                        }
                        onCheckedChange={(checked) => {
                          updateAutomation({
                            sessionId: sessionId!,
                            autoAttendance: Boolean(checked),
                          });
                        }}
                      />
                      <span className={cn('text-slate-700', !demo?.autoAdvanceStops && 'opacity-40')}>
                        Auto attendance
                      </span>
                    </label>
                  </div>
                </div>
              )}

              {/* 4. Live Telemetry */}
              {sessionId && (
                <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-3.5'>
                  <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                    Telemetry
                  </p>

                  <div className='grid grid-cols-2 gap-3.5 text-xs font-semibold'>
                    <div className='bg-slate-50 border border-slate-150 rounded-xl p-2.5'>
                      <span className='text-slate-400 text-[10px] uppercase font-bold block'>Latitude</span>
                      <p className='font-mono text-slate-700 mt-0.5'>
                        {currentLat?.toFixed(5) ?? '—'}
                      </p>
                    </div>
                    <div className='bg-slate-50 border border-slate-150 rounded-xl p-2.5'>
                      <span className='text-slate-400 text-[10px] uppercase font-bold block'>Longitude</span>
                      <p className='font-mono text-slate-700 mt-0.5'>
                        {currentLng?.toFixed(5) ?? '—'}
                      </p>
                    </div>
                    <div className='bg-slate-50 border border-slate-150 rounded-xl p-2.5'>
                      <span className='text-slate-400 text-[10px] uppercase font-bold block'>Stop Order</span>
                      <p className='font-mono text-slate-750 mt-0.5'>
                        {currentStopOrder ?? '—'}
                      </p>
                    </div>
                    <div className='bg-slate-50 border border-slate-150 rounded-xl p-2.5'>
                      <span className='text-slate-400 text-[10px] uppercase font-bold block'>Speed</span>
                      <p className='font-mono text-slate-750 mt-0.5'>x{currentSpeed}</p>
                    </div>
                  </div>
                </div>
              )}

              {/* 5. Timeline Sequence */}
              {routeStops.length > 0 && (
                <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-3'>
                  <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                    Stop Timeline
                  </p>

                  <div className='max-h-[200px] overflow-y-auto pr-1 relative pl-3 space-y-3.5 before:absolute before:left-[15px] before:top-2 before:bottom-2 before:w-[1.5px] before:bg-slate-100/70'>
                    {[...routeStops]
                      .sort((a, b) => a.stopOrder - b.stopOrder)
                      .map((stop) => {
                        const isCurrent = currentStopOrder != null && stop.stopOrder === currentStopOrder;
                        const isPassed = currentStopOrder != null && stop.stopOrder < currentStopOrder;
                        const isUpcoming = currentStopOrder != null && stop.stopOrder > currentStopOrder;

                        const StopIcon = stop.locationType === 'SCHOOL'
                          ? GraduationCap
                          : stop.locationType === 'DEPOT'
                          ? Warehouse
                          : MapPin;

                        const iconColor = stop.locationType === 'SCHOOL'
                          ? 'text-red-500 bg-red-50 border border-red-100'
                          : stop.locationType === 'DEPOT'
                          ? 'text-orange-500 bg-orange-50 border border-orange-100'
                          : stop.stopPurpose === 'PICKUP'
                          ? 'text-blue-500 bg-blue-50 border border-blue-105'
                          : 'text-emerald-500 bg-emerald-50 border border-emerald-105';

                        return (
                          <div
                            key={stop.id}
                            className={cn(
                              'relative flex items-center gap-3 text-xs font-semibold group transition-all',
                              isCurrent && 'text-[#C81E3A]',
                              isPassed && 'text-slate-400 opacity-70',
                              isUpcoming && 'text-slate-650'
                            )}
                          >
                            {/* Circle bullet node with proper colors */}
                            <div className={cn(
                              'relative z-10 flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white border shadow-2xs transition-all',
                              isCurrent
                                ? 'border-[#C81E3A] ring-4 ring-red-500/10'
                                : isPassed
                                ? 'border-emerald-300 bg-emerald-50'
                                : 'border-slate-200'
                            )}>
                              <span className={cn(
                                'h-2 w-2 rounded-full',
                                isCurrent
                                  ? 'bg-[#C81E3A] animate-pulse'
                                  : isPassed
                                  ? 'bg-emerald-500'
                                  : 'bg-slate-350'
                              )} />
                            </div>

                            <div className='flex-1 min-w-0 flex items-center justify-between gap-1.5'>
                              <div className='min-w-0 flex-1'>
                                <div className='flex items-center gap-1.5 min-w-0'>
                                  <div className={cn('flex h-4.5 w-4.5 items-center justify-center rounded shrink-0', iconColor)}>
                                    <StopIcon className='h-3 w-3' />
                                  </div>
                                  <p className='truncate text-[11px] font-bold'>
                                    {stop.stopOrder}. {stop.displayName || stop.pickupPointName || stop.depotName || stop.schoolName || `Stop #${stop.id}`}
                                  </p>
                                </div>
                              </div>
                              <span className='shrink-0 text-[9px] font-extrabold uppercase bg-slate-50 border border-slate-200 text-slate-400 rounded px-1.5 py-0.2'>
                                {stop.stopPurpose === 'START_TERMINAL' ? 'Start' : stop.stopPurpose === 'END_TERMINAL' ? 'End' : stop.stopPurpose === 'PICKUP' ? 'Pickup' : 'Drop'}
                              </span>
                            </div>
                          </div>
                        );
                      })}
                  </div>
                </div>
              )}

              {/* 6. Event Feed Log */}
              <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-sm space-y-3'>
                <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                  Activity Log Feed
                </p>

                <div className='max-h-[220px] overflow-y-auto pr-1 space-y-2'>
                  {mergedEvents.length === 0 ? (
                    <div className='flex flex-col items-center justify-center py-6 text-center text-slate-400 gap-1.5'>
                      <Bell className='h-7 w-7 text-slate-300' />
                      <span className='text-[11px] font-semibold'>No demo events yet.</span>
                    </div>
                  ) : (
                    mergedEvents.slice(0, 50).map((event: any, idx: number) => {
                      const display = EVENT_DISPLAY_CONFIG[event.eventType] || {
                        label: event.eventType,
                        className: 'bg-slate-50 text-slate-600 border border-slate-200'
                      };

                      return (
                        <div
                          key={`${event.eventTime}-${event.eventType}-${idx}`}
                          className='flex items-start gap-2.5 rounded-xl border border-slate-100 bg-white p-2.5 shadow-2xs hover:shadow-xs transition-all'
                        >
                          <Timer className='mt-0.5 h-3.5 w-3.5 shrink-0 text-slate-400' />
                          <div className='min-w-0 flex-1 space-y-1'>
                            <div className='flex flex-wrap items-center gap-1.5'>
                              <span className={cn('rounded-full px-2 py-0.2 text-[9px] font-extrabold uppercase', display.className)}>
                                {display.label}
                              </span>
                              {event.progressPercent != null && (
                                <span className='text-[10px] text-slate-400 font-semibold'>{Math.round(event.progressPercent)}%</span>
                              )}
                              {event.currentStopOrder != null && (
                                <span className='text-[10px] text-slate-400 font-semibold'>stop {event.currentStopOrder}</span>
                              )}
                            </div>
                            <p className='text-[9px] text-slate-400 font-semibold flex items-center gap-1'>
                              <Clock className='h-3 w-3 text-slate-350' />
                              {formatEventTime(event.eventTime)}
                            </p>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </SchoolBusPageShell>
    </MapMarkerVisibilityProvider>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatEventTime(isoTime?: string | null): string {
  if (!isoTime) return '';
  try {
    return new Date(isoTime).toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  } catch {
    return isoTime;
  }
}
