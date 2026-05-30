'use client';

import * as React from 'react';
import { Gauge, Pause, Play, RotateCcw, Square } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useCreateDemoSessionMutation,
  useGetDemoSessionByTripQuery,
  useGetDemoSessionEventsQuery,
  useGetTripsQuery,
  usePauseDemoSessionMutation,
  useResumeDemoSessionMutation,
  useStartDemoSessionMutation,
  useStopDemoSessionMutation,
  useTickDemoSessionMutation,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { schoolBusUi } from '../theme';
import { getPageItems } from '../utils';
import { SchoolBusTimeline, mapDemoEventsToTimeline } from '../components/history';

export function SchoolBusDemoPage() {
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

  const { data: sessionData } = useGetDemoSessionByTripQuery(selectedTripId as number, {
    skip: !selectedTripId,
  });
  const demo = sessionData?.data;
  const sessionId = demo?.id;

  const { data: eventsData } = useGetDemoSessionEventsQuery(sessionId as number, {
    skip: !sessionId,
  });
  const events = eventsData?.data || demo?.events || [];

  const [createSession] = useCreateDemoSessionMutation();
  const [startDemo] = useStartDemoSessionMutation();
  const [pauseDemo] = usePauseDemoSessionMutation();
  const [resumeDemo] = useResumeDemoSessionMutation();
  const [stopDemo] = useStopDemoSessionMutation();
  const [tickDemo] = useTickDemoSessionMutation();

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
    // Create session if none exists or current is terminated
    if (!sessionId || demo?.status === 'COMPLETED' || demo?.status === 'STOPPED') {
      await action('Create session', () =>
        createSession({ tripId: selectedTripId }).unwrap()
      );
      return;
    }
    await action('Start demo', () => startDemo(sessionId).unwrap());
  };

  return (
    <SchoolBusPageShell
      title='Simulation demo'
      description='Demo mode runs on trip execution data. It does not replace real GPS, but gives a controlled operations walkthrough for school-bus demos.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Simulation demo', current: true },
          ]}
        />
      }
    >
      <div className='grid gap-4 md:grid-cols-3'>
        <SchoolBusMetricCard
          label='Demo status'
          value={demo?.status || 'READY'}
          hint='Current simulated session state'
          icon={Gauge}
          tone='info'
        />
        <SchoolBusMetricCard
          label='Speed'
          value={`x${demo?.speedMultiplier || 1}`}
          hint='Simulation playback multiplier'
          icon={Play}
          tone='success'
        />
        <SchoolBusMetricCard
          label='Progress'
          value={`${Math.round(demo?.progressPercent || 0)}%`}
          hint='Current progress along the trip'
          icon={RotateCcw}
          tone='warning'
        />
      </div>

      <div className='grid gap-6 xl:grid-cols-[0.75fr_1.25fr]'>
        <SchoolBusSection
          title='Trip selector'
          description='Choose a trip snapshot to drive a demo session.'
        >
          {trips.length === 0 ? (
            <SchoolBusEmptyState
              title='No trips available'
              description='Create trips from dispatched routes before simulation can start.'
              icon={Gauge}
            />
          ) : (
            <div className='space-y-2'>
              {trips.map((trip) => (
                <button
                  key={trip.id}
                  type='button'
                  onClick={() => setSelectedTripId(trip.id)}
                  className={`${schoolBusUi.interactiveCard} w-full text-left ${
                    selectedTripId === trip.id ? 'border-rose-300 bg-rose-50' : ''
                  }`}
                >
                  <div className='flex items-center justify-between gap-3'>
                    <div>
                      <p className='font-semibold text-slate-950'>{trip.tripCode}</p>
                      <p className='text-sm text-slate-500'>
                        {trip.routeCode} - {trip.routeDirection}
                      </p>
                    </div>
                    <SchoolBusStatusBadge status={trip.status} />
                  </div>
                </button>
              ))}
            </div>
          )}
        </SchoolBusSection>

        <SchoolBusSection
          title='Demo controls'
          description='Operate a demo session and inspect its event trail.'
        >
          <div className='space-y-5'>
            <div className='flex flex-wrap gap-2'>
              <Button
                disabled={!selectedTripId}
                onClick={handleStart}
              >
                <Play className='mr-2 h-4 w-4' />
                {!sessionId || demo?.status === 'COMPLETED' || demo?.status === 'STOPPED'
                  ? 'New Session'
                  : 'Start'}
              </Button>
              <Button
                variant='outline'
                disabled={!sessionId || demo?.status !== 'RUNNING'}
                onClick={() => action('Pause demo', () => pauseDemo(sessionId!).unwrap())}
              >
                <Pause className='mr-2 h-4 w-4' />
                Pause
              </Button>
              <Button
                variant='outline'
                disabled={!sessionId || demo?.status !== 'PAUSED'}
                onClick={() => action('Resume demo', () => resumeDemo(sessionId!).unwrap())}
              >
                <RotateCcw className='mr-2 h-4 w-4' />
                Resume
              </Button>
              <Button
                variant='outline'
                disabled={!sessionId || demo?.status === 'COMPLETED' || demo?.status === 'STOPPED'}
                onClick={() => action('Stop demo', () => stopDemo(sessionId!).unwrap())}
              >
                <Square className='mr-2 h-4 w-4' />
                Stop
              </Button>
              <Button
                variant='outline'
                disabled={!sessionId || demo?.status !== 'RUNNING'}
                onClick={() => action('Tick', () => tickDemo(sessionId!).unwrap())}
              >
                <RotateCcw className='mr-2 h-4 w-4' />
                Tick
              </Button>
            </div>

            <div className='rounded-[24px] border border-rose-100 bg-gradient-to-br from-rose-50 via-white to-sky-50 p-6'>
              <div className='flex items-center justify-between'>
                <div>
                  <p className='text-sm uppercase tracking-[0.28em] text-rose-500'>
                    Simulated bus position
                  </p>
                  <p className='mt-2 text-3xl font-semibold text-slate-950'>
                    {Math.round(demo?.progressPercent || 0)}%
                  </p>
                </div>
                <SchoolBusStatusBadge status={demo?.status || 'READY'} />
              </div>
              <div className='mt-5 h-3 overflow-hidden rounded-full bg-slate-200'>
                <div
                  className='h-full rounded-full bg-rose-600 transition-all duration-500'
                  style={{ width: `${Math.min(100, demo?.progressPercent || 0)}%` }}
                />
              </div>
            </div>

            <div>
              <p className='mb-2 text-sm font-semibold text-slate-950'>Nhật ký sự kiện</p>
              <SchoolBusTimeline
                events={mapDemoEventsToTimeline(events)}
                mode='compact'
                maxHeight='360px'
              />
            </div>
          </div>
        </SchoolBusSection>
      </div>
    </SchoolBusPageShell>
  );
}
