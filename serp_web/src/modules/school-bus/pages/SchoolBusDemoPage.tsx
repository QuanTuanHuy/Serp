'use client';

import * as React from 'react';
import {
  Bus,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Circle,
  Gauge,
  MapPin,
  Pause,
  Play,
  RotateCcw,
  Square,
  Timer,
  Zap,
} from 'lucide-react';
import { toast } from 'sonner';
import { Client } from '@stomp/stompjs';
import { Button } from '@/shared/components/ui';
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
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { DemoMap } from '../components/map/DemoMap';
import { getPageItems } from '../utils';

// ── Event type config ─────────────────────────────────────────────────────────

const EVENT_COLORS: Record<string, string> = {
  DEMO_CREATED: 'bg-sky-100 text-sky-700',
  DEMO_STARTED: 'bg-emerald-100 text-emerald-700',
  DEMO_TICK: 'bg-slate-100 text-slate-600',
  DEMO_PAUSED: 'bg-amber-100 text-amber-700',
  DEMO_RESUMED: 'bg-blue-100 text-blue-700',
  DEMO_JUMPED: 'bg-violet-100 text-violet-700',
  DEMO_COMPLETED: 'bg-green-100 text-green-700',
  DEMO_STOPPED: 'bg-red-100 text-red-700',
  DEMO_ERROR: 'bg-red-100 text-red-700',
  DEMO_AUTO_ARRIVED_STOP: 'bg-teal-100 text-teal-700',
  DEMO_AUTO_DEPARTED_STOP: 'bg-indigo-100 text-indigo-700',
  DEMO_AUTO_ATTENDANCE: 'bg-purple-100 text-purple-700',
  DEMO_AUTOMATION_SKIPPED: 'bg-orange-100 text-orange-700',
  DEMO_AUTOMATION_ERROR: 'bg-rose-100 text-rose-700',
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

  // ─── WebSocket real-time overlay ────────────────────────────────
  const [wsPosition, setWsPosition] = React.useState<DemoPositionMessage | null>(null);
  const [wsEvents, setWsEvents] = React.useState<DemoEventMessage[]>([]);
  const clientRef = React.useRef<Client | null>(null);

  React.useEffect(() => {
    setWsPosition(null);
    setWsEvents([]);
  }, [sessionId]);

  React.useEffect(() => {
    if (!sessionId) return;

    const client = connectSchoolBusDemoSocket();
    clientRef.current = client;

    subscribeDemoPosition(client, sessionId, (msg) => {
      setWsPosition(msg);
    });

    subscribeDemoEvents(client, sessionId, (msg) => {
      setWsEvents((prev) => [msg, ...prev]);
    });

    return () => {
      disconnectDemoSocket(client);
      clientRef.current = null;
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

  // ─── Render ────────────────────────────────────────────────────
  return (
    <SchoolBusPageShell
      title='Demo Simulation'
      description='Real-time bus simulation with map visualization.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus', href: '/school-bus/dispatch' },
            { label: 'Demo Simulation', current: true },
          ]}
        />
      }
    >
      {/* ── Header bar ─────────────────────────────────────────── */}
      <div className='flex flex-wrap items-center gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3'>
        {/* Trip selector */}
        <select
          className='rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-800'
          value={selectedTripId ?? ''}
          onChange={(e) => setSelectedTripId(e.target.value ? Number(e.target.value) : null)}
        >
          <option value=''>Select trip...</option>
          {trips.map((trip) => (
            <option key={trip.id} value={trip.id}>
              {trip.tripCode} — {trip.routeCode} ({trip.routeDirection})
            </option>
          ))}
        </select>

        {selectedTrip && (
          <>
            <span className='text-xs text-slate-400'>|</span>
            <span className='text-xs text-slate-600'>{selectedTrip.serviceDate}</span>
            <span className='text-xs text-slate-400'>|</span>
            <SchoolBusStatusBadge status={currentStatus || 'NO_SESSION'} />
            <span className='text-xs text-slate-400'>|</span>
            <span className='text-xs font-medium text-slate-700'>
              {Math.round(currentProgress)}%
            </span>
          </>
        )}

        <div className='ml-auto flex items-center gap-1 text-xs text-slate-500'>
          <Gauge className='h-3.5 w-3.5' />
          x{currentSpeed}
        </div>
      </div>

      {/* ── Main workspace: map + side panel ───────────────────── */}
      <div className='grid gap-4 xl:grid-cols-[1fr_360px]' style={{ minHeight: '560px' }}>
        {/* Map */}
        <div className='relative overflow-hidden rounded-2xl border border-slate-200'>
          {routeStops.length > 0 ? (
            <DemoMap
              stops={routeStops}
              routePath={routePath}
              busPosition={busPosition}
              currentStopOrder={currentStopOrder}
              className='h-full w-full rounded-2xl'
              fitKey={fitKey}
            />
          ) : (
            <div className='flex h-full min-h-[400px] flex-col items-center justify-center gap-3 bg-slate-50'>
              <MapPin className='h-10 w-10 text-slate-300' />
              <p className='text-sm text-slate-500'>Select a trip to view route on map</p>
            </div>
          )}

          {/* Floating center bus button */}
          {busPosition && (
            <button
              type='button'
              className='absolute bottom-3 right-3 rounded-full border border-slate-300 bg-white p-2 shadow-md hover:bg-slate-50'
              title='Center on bus'
              onClick={() => setFitKey((k) => k + 1)}
            >
              <Bus className='h-4 w-4 text-indigo-600' />
            </button>
          )}
        </div>

        {/* Right panel */}
        <div className='flex flex-col gap-3 overflow-y-auto'>
          {/* Controls */}
          <div className='rounded-2xl border border-slate-200 bg-white p-4'>
            <p className='mb-3 text-xs font-semibold uppercase tracking-wider text-slate-500'>
              Session Controls
            </p>
            <div className='flex flex-wrap gap-2'>
              {/* Create / Start */}
              <Button
                size='sm'
                disabled={
                  !selectedTripId ||
                  currentStatus === 'RUNNING' ||
                  currentStatus === 'PAUSED'
                }
                onClick={handleStart}
              >
                <Play className='mr-1 h-3.5 w-3.5' />
                {!sessionId || currentStatus === 'COMPLETED' || currentStatus === 'STOPPED'
                  ? 'Create Demo'
                  : currentStatus === 'READY'
                    ? 'Start'
                    : 'New Session'}
              </Button>

              {/* Tick */}
              <Button
                size='sm'
                variant='outline'
                disabled={!sessionId || currentStatus !== 'RUNNING' || isTicking}
                onClick={() => action('Tick', () => tickDemo(sessionId!).unwrap())}
              >
                <ChevronRight className='mr-1 h-3.5 w-3.5' />
                Tick
              </Button>

              {/* Auto tick toggle */}
              <Button
                size='sm'
                variant={autoTick ? 'default' : 'outline'}
                disabled={!sessionId || currentStatus !== 'RUNNING'}
                onClick={() => setAutoTick((v) => !v)}
                className={autoTick ? 'bg-emerald-600 hover:bg-emerald-700' : ''}
              >
                <Zap className='mr-1 h-3.5 w-3.5' />
                {autoTick ? 'Auto ●' : 'Auto'}
              </Button>

              {/* Pause */}
              <Button
                size='sm'
                variant='outline'
                disabled={!sessionId || currentStatus !== 'RUNNING'}
                onClick={() => action('Pause', () => pauseDemo(sessionId!).unwrap())}
              >
                <Pause className='mr-1 h-3.5 w-3.5' />
                Pause
              </Button>

              {/* Resume */}
              <Button
                size='sm'
                variant='outline'
                disabled={!sessionId || currentStatus !== 'PAUSED'}
                onClick={() => action('Resume', () => resumeDemo(sessionId!).unwrap())}
              >
                <RotateCcw className='mr-1 h-3.5 w-3.5' />
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
              >
                <Square className='mr-1 h-3.5 w-3.5' />
                Stop
              </Button>
            </div>

            {/* Progress bar */}
            {sessionId && (
              <div className='mt-3'>
                <div className='flex items-center justify-between text-xs text-slate-500'>
                  <span>Progress</span>
                  <span className='font-medium text-slate-700'>{Math.round(currentProgress)}%</span>
                </div>
                <div className='mt-1 h-2 overflow-hidden rounded-full bg-slate-200'>
                  <div
                    className='h-full rounded-full bg-rose-500 transition-all duration-300'
                    style={{ width: `${Math.min(100, currentProgress)}%` }}
                  />
                </div>
              </div>
            )}
          </div>

          {/* Current position info */}
          {sessionId && (
            <div className='rounded-2xl border border-slate-200 bg-white p-4'>
              <p className='mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500'>
                Quick Navigation
              </p>
              <p className='mb-2 text-[10px] text-slate-400'>
                Jump moves the bus marker. If auto stops/attendance are on, trip lifecycle will also trigger.
              </p>
              <div className='flex flex-wrap gap-2'>
                <Button
                  size='sm'
                  variant='outline'
                  disabled={!canJump}
                  onClick={() => handleJump('start')}
                >
                  <ChevronsLeft className='mr-1 h-3.5 w-3.5' />
                  Start
                </Button>
                <Button
                  size='sm'
                  variant='outline'
                  disabled={!canJump || !currentStopOrder || currentStopOrder <= 1}
                  onClick={() => handleJump('prev')}
                >
                  <ChevronLeft className='mr-1 h-3.5 w-3.5' />
                  Prev Stop
                </Button>
                <Button
                  size='sm'
                  variant='outline'
                  disabled={!canJump || !currentStopOrder || currentStopOrder >= routeStops.length}
                  onClick={() => handleJump('next')}
                >
                  Next Stop
                  <ChevronRight className='ml-1 h-3.5 w-3.5' />
                </Button>
                <Button
                  size='sm'
                  variant='outline'
                  disabled={!canJump}
                  onClick={() => handleJump('end')}
                >
                  End
                  <ChevronsRight className='ml-1 h-3.5 w-3.5' />
                </Button>
              </div>
            </div>
          )}

          {/* Automation settings */}
          {sessionId && (
            <div className='rounded-2xl border border-slate-200 bg-white p-4'>
              <p className='mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500'>
                Automation Settings
              </p>
              <p className='mb-2 text-[10px] text-slate-400'>
                Auto stops/attendance will mutate trip stop logs and attendance records.
              </p>
              <div className='flex flex-col gap-2'>
                <label className='flex items-center gap-2 text-xs cursor-pointer'>
                  <input
                    type='checkbox'
                    checked={!!demo?.autoAdvanceStops}
                    disabled={currentStatus === 'COMPLETED' || currentStatus === 'STOPPED'}
                    onChange={(e) => {
                      const val = e.target.checked;
                      updateAutomation({
                        sessionId: sessionId!,
                        autoAdvanceStops: val,
                        autoAttendance: val ? demo?.autoAttendance ?? false : false,
                      });
                    }}
                    className='rounded border-slate-300'
                  />
                  <span className='text-slate-700'>Auto advance stops</span>
                </label>
                <label className='flex items-center gap-2 text-xs cursor-pointer'>
                  <input
                    type='checkbox'
                    checked={!!demo?.autoAttendance}
                    disabled={
                      currentStatus === 'COMPLETED' ||
                      currentStatus === 'STOPPED' ||
                      !demo?.autoAdvanceStops
                    }
                    onChange={(e) => {
                      updateAutomation({
                        sessionId: sessionId!,
                        autoAttendance: e.target.checked,
                      });
                    }}
                    className='rounded border-slate-300'
                  />
                  <span className={cn('text-slate-700', !demo?.autoAdvanceStops && 'opacity-50')}>
                    Auto attendance
                  </span>
                </label>
              </div>
            </div>
          )}

          {/* Current position info */}
          {sessionId && (
            <div className='rounded-2xl border border-slate-200 bg-white p-4'>
              <p className='mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500'>
                Current Position
              </p>
              <div className='grid grid-cols-2 gap-2 text-xs'>
                <div>
                  <span className='text-slate-400'>Lat</span>
                  <p className='font-mono text-slate-700'>
                    {currentLat?.toFixed(5) ?? '—'}
                  </p>
                </div>
                <div>
                  <span className='text-slate-400'>Lng</span>
                  <p className='font-mono text-slate-700'>
                    {currentLng?.toFixed(5) ?? '—'}
                  </p>
                </div>
                <div>
                  <span className='text-slate-400'>Stop Order</span>
                  <p className='font-mono text-slate-700'>
                    {currentStopOrder ?? '—'}
                  </p>
                </div>
                <div>
                  <span className='text-slate-400'>Speed</span>
                  <p className='font-mono text-slate-700'>x{currentSpeed}</p>
                </div>
              </div>
            </div>
          )}

          {/* Stop timeline */}
          {routeStops.length > 0 && (
            <div className='rounded-2xl border border-slate-200 bg-white p-4'>
              <p className='mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500'>
                Stop Timeline
              </p>
              <div className='max-h-[180px] space-y-1 overflow-y-auto'>
                {[...routeStops]
                  .sort((a, b) => a.stopOrder - b.stopOrder)
                  .map((stop) => {
                    const isCurrent = currentStopOrder != null && stop.stopOrder === currentStopOrder;
                    const isPassed = currentStopOrder != null && stop.stopOrder < currentStopOrder;
                    return (
                      <div
                        key={stop.id}
                        className={cn(
                          'flex items-center gap-2 rounded-lg px-2 py-1 text-xs',
                          isCurrent && 'bg-rose-50 font-medium text-rose-700',
                          isPassed && 'text-slate-400',
                          !isCurrent && !isPassed && 'text-slate-600'
                        )}
                      >
                        <Circle
                          className={cn(
                            'h-2.5 w-2.5 shrink-0',
                            isCurrent && 'fill-rose-500 text-rose-500',
                            isPassed && 'fill-slate-300 text-slate-300',
                            !isCurrent && !isPassed && 'text-slate-300'
                          )}
                        />
                        <span className='truncate'>
                          {stop.stopOrder}. {stop.displayName || stop.pickupPointName || stop.depotName || stop.schoolName || `Stop #${stop.id}`}
                        </span>
                        <span className='ml-auto shrink-0 text-[10px] text-slate-400'>
                          {stop.stopPurpose}
                        </span>
                      </div>
                    );
                  })}
              </div>
              {/* TODO Phase 6: auto-advance stop lifecycle when bus reaches stop. */}
            </div>
          )}

          {/* Event feed */}
          <div className='rounded-2xl border border-slate-200 bg-white p-4'>
            <p className='mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500'>
              Event Feed
            </p>
            <div className='max-h-[240px] space-y-1.5 overflow-y-auto'>
              {mergedEvents.length === 0 ? (
                <p className='py-4 text-center text-xs text-slate-400'>
                  No events yet. Start a demo session.
                </p>
              ) : (
                mergedEvents.slice(0, 50).map((event: any, idx: number) => (
                  <div
                    key={`${event.eventTime}-${event.eventType}-${idx}`}
                    className='flex items-start gap-2 text-xs'
                  >
                    <Timer className='mt-0.5 h-3 w-3 shrink-0 text-slate-400' />
                    <div className='min-w-0 flex-1'>
                      <div className='flex items-center gap-1.5'>
                        <span
                          className={cn(
                            'rounded px-1.5 py-0.5 text-[10px] font-medium',
                            EVENT_COLORS[event.eventType] || 'bg-slate-100 text-slate-600'
                          )}
                        >
                          {event.eventType}
                        </span>
                        {event.progressPercent != null && (
                          <span className='text-slate-400'>{Math.round(event.progressPercent)}%</span>
                        )}
                        {event.currentStopOrder != null && (
                          <span className='text-slate-400'>stop {event.currentStopOrder}</span>
                        )}
                      </div>
                      <p className='mt-0.5 text-[10px] text-slate-400'>
                        {formatEventTime(event.eventTime)}
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>
            {/* TODO Phase 6: auto-attendance when autoAttendance is enabled. */}
          </div>
        </div>
      </div>
    </SchoolBusPageShell>
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
