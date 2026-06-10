'use client';

import Link from 'next/link';
import * as React from 'react';
import {
  ArrowLeft,
  Calendar,
  GraduationCap,
  PauseCircle,
  PlayCircle,
  StopCircle,
  User,
  AlertCircle,
  CheckCircle2,
  Info,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetSchoolBusSubscriptionByIdQuery,
  useActivateSchoolBusSubscriptionMutation,
  usePauseSchoolBusSubscriptionMutation,
  useStopSchoolBusSubscriptionMutation,
  useGetSchoolBusSubscriptionHistoryQuery,
} from '../api/schoolBusApi';
import { SchoolBusTimeline } from '../components/history/SchoolBusTimeline';
import { mapSubscriptionHistoryToTimeline } from '../components/history/mappers';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { formatDate } from '../utils';
import type { TimelineEvent } from '../components/history/SchoolBusTimeline';

const TRIP_OPTION_LABELS: Record<string, string> = {
  MORNING: 'To school only (Morning)',
  AFTERNOON: 'From school only (Afternoon)',
  ROUND_TRIP: 'Round trip',
};

const DAY_KEYS = [
  { key: 'monday', label: 'Mon' },
  { key: 'tuesday', label: 'Tue' },
  { key: 'wednesday', label: 'Wed' },
  { key: 'thursday', label: 'Thu' },
  { key: 'friday', label: 'Fri' },
  { key: 'saturday', label: 'Sat' },
  { key: 'sunday', label: 'Sun' },
] as const;

interface SchoolBusSubscriptionDetailPageProps {
  subscriptionId: number;
}

export function SchoolBusSubscriptionDetailPage({
  subscriptionId,
}: SchoolBusSubscriptionDetailPageProps) {
  const { data, isLoading } = useGetSchoolBusSubscriptionByIdQuery(subscriptionId);
  const [activateSubscription, { isLoading: activating }] = useActivateSchoolBusSubscriptionMutation();
  const [pauseSubscription, { isLoading: pausing }] = usePauseSchoolBusSubscriptionMutation();
  const [stopSubscription, { isLoading: stopping }] = useStopSchoolBusSubscriptionMutation();

  const { data: historyData, isLoading: historyLoading, isError: historyError } =
    useGetSchoolBusSubscriptionHistoryQuery(subscriptionId);

  const sub = data?.data;

  const handleAction = async (action: 'activate' | 'pause' | 'stop') => {
    try {
      const response =
        action === 'activate'
          ? await activateSubscription(subscriptionId).unwrap()
          : action === 'pause'
            ? await pauseSubscription(subscriptionId).unwrap()
            : await stopSubscription(subscriptionId).unwrap();
      toast.success(response.message || `Subscription ${action}d successfully`);
    } catch (error: any) {
      toast.error(error?.data?.message || `Failed to ${action} subscription`);
    }
  };

  const timelineEvents = React.useMemo<TimelineEvent[]>(() => {
    const historyEvents = mapSubscriptionHistoryToTimeline(historyData?.data ?? []);
    return historyEvents.sort(
      (a, b) => new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime()
    );
  }, [historyData]);

  if (isLoading) {
    return (
      <SchoolBusPageShell title='Subscription Detail' description='Loading transport subscription details...'>
        <div className='flex h-[400px] items-center justify-center'>
          <div className='flex flex-col items-center gap-3'>
            <div className='h-8 w-8 animate-spin rounded-full border-2 border-[#C81E3A] border-t-transparent' />
            <p className='text-xs text-slate-500 font-medium'>Fetching subscription contract data...</p>
          </div>
        </div>
      </SchoolBusPageShell>
    );
  }

  if (!sub) {
    return (
      <SchoolBusPageShell title='Subscription Detail' description='Could not view transport subscription'>
        <div className='py-12 flex flex-col items-center gap-4'>
          <SchoolBusEmptyState
            title='Subscription Not Found'
            description='This subscription contract may have been deleted, or you might not have access to it.'
            icon={AlertCircle}
          />
          <Link href='/school-bus/subscriptions'>
            <Button variant='outline' className='gap-2 rounded-xl'>
              <ArrowLeft className='h-4 w-4' />
              Back to Subscriptions
            </Button>
          </Link>
        </div>
      </SchoolBusPageShell>
    );
  }

  // derive readiness checklist
  const isStatusActive = sub.status === 'ACTIVE';
  const isDateValid = (() => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const fromTime = sub.effectiveFrom ? new Date(sub.effectiveFrom).getTime() : 0;
    const toTime = sub.effectiveTo ? new Date(sub.effectiveTo).getTime() : Infinity;
    return today >= fromTime && today <= toTime;
  })();

  const hasDays = sub.monday || sub.tuesday || sub.wednesday || sub.thursday || sub.friday || sub.saturday || sub.sunday;

  const requiresPickup = sub.tripOption === 'MORNING' || sub.tripOption === 'ROUND_TRIP';
  const requiresDropoff = sub.tripOption === 'AFTERNOON' || sub.tripOption === 'ROUND_TRIP';

  const pickupPointConfigured = !requiresPickup || !!sub.pickupPointId;
  const dropoffPointConfigured = !requiresDropoff || !!sub.dropoffPointId;

  const isEligible = isStatusActive && isDateValid && hasDays && pickupPointConfigured && dropoffPointConfigured;

  const isConfigurationIncomplete = !pickupPointConfigured || !dropoffPointConfigured;

  const descriptionNode = (
    <div className='space-y-1.5 mt-1'>
      <p className='text-sm text-slate-500 font-medium'>
        Long-term transport service for student <span className='font-bold text-slate-800'>{sub.studentName}</span>
      </p>
      {/* Metadata row */}
      <div className='flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-slate-400 font-semibold'>
        <SchoolBusStatusBadge status={sub.status} />
        <span>·</span>
        <span>{TRIP_OPTION_LABELS[sub.tripOption] || sub.tripOption}</span>
        <span>·</span>
        <span>Effective: {formatDate(sub.effectiveFrom)} - {sub.effectiveTo ? formatDate(sub.effectiveTo) : 'Ongoing'}</span>
      </div>
    </div>
  );

  const actionsNode = (
    <div className='flex flex-wrap items-center gap-2 md:justify-end shrink-0'>
      <Link href='/school-bus/subscriptions'>
        <Button variant='outline' className='h-9 rounded-xl gap-2 font-bold text-slate-600 border-slate-200 hover:bg-slate-50'>
          <ArrowLeft className='h-4 w-4' />
          Back
        </Button>
      </Link>

      {sub.status === 'ACTIVE' && (
        <>
          <Button
            variant='outline'
            onClick={() => handleAction('pause')}
            disabled={pausing}
            className='h-9 rounded-xl border-amber-200 hover:bg-amber-50 text-amber-700 font-bold gap-1.5'
          >
            <PauseCircle className='h-4 w-4' />
            Pause
          </Button>
          <Button
            variant='outline'
            onClick={() => handleAction('stop')}
            disabled={stopping}
            className='h-9 rounded-xl border-red-200 hover:bg-red-50 text-red-600 font-bold gap-1.5'
          >
            <StopCircle className='h-4 w-4' />
            Stop
          </Button>
        </>
      )}

      {sub.status === 'PAUSED' && (
        <>
          <Button
            variant='outline'
            onClick={() => handleAction('activate')}
            disabled={activating}
            className='h-9 rounded-xl border-emerald-200 hover:bg-emerald-50 text-emerald-700 font-bold gap-1.5'
          >
            <PlayCircle className='h-4 w-4' />
            Resume
          </Button>
          <Button
            variant='outline'
            onClick={() => handleAction('stop')}
            disabled={stopping}
            className='h-9 rounded-xl border-red-200 hover:bg-red-50 text-red-600 font-bold gap-1.5'
          >
            <StopCircle className='h-4 w-4' />
            Stop
          </Button>
        </>
      )}
    </div>
  );

  return (
    <SchoolBusPageShell
      title={`Subscription ${sub.subscriptionCode}`}
      description={descriptionNode}
      actions={actionsNode}
    >
      <div className='flex flex-col gap-6 mt-4'>


        {/* Status / Readiness Banners */}
        <div className='space-y-3'>
          {sub.status === 'ACTIVE' && (
            <div className='flex items-start gap-3 rounded-2xl border border-emerald-200 bg-emerald-50/60 p-4 text-emerald-800 shadow-sm'>
              <CheckCircle2 className='mt-0.5 h-5 w-5 text-emerald-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-emerald-950'>Active subscription</h4>
                <p className='text-xs text-emerald-850 mt-0.5 leading-relaxed font-medium'>
                  This student is eligible for route planning when schedule, service date, active days and pickup/drop-off conditions match.
                </p>
              </div>
            </div>
          )}

          {sub.status === 'PAUSED' && (
            <div className='flex items-start gap-3 rounded-2xl border border-amber-200 bg-amber-50/60 p-4 text-amber-800 shadow-sm'>
              <PauseCircle className='mt-0.5 h-5 w-5 text-amber-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-amber-950'>Paused subscription</h4>
                <p className='text-xs text-amber-850 mt-0.5 leading-relaxed font-medium'>
                  This subscription is excluded from route planning during the pause period.
                </p>
              </div>
            </div>
          )}

          {sub.status === 'STOPPED' && (
            <div className='flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50/60 p-4 text-red-800 shadow-sm'>
              <StopCircle className='mt-0.5 h-5 w-5 text-red-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-red-950'>Stopped subscription</h4>
                <p className='text-xs text-red-850 mt-0.5 leading-relaxed font-medium'>
                  This subscription is no longer used for new route planning sessions.
                </p>
              </div>
            </div>
          )}

          {sub.status === 'EXPIRED' && (
            <div className='flex items-start gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-slate-800 shadow-sm'>
              <AlertCircle className='mt-0.5 h-5 w-5 text-slate-500 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-slate-950'>Expired subscription</h4>
                <p className='text-xs text-slate-700 mt-0.5 leading-relaxed font-medium'>
                  This subscription contract has reached its effective to-date and is expired.
                </p>
              </div>
            </div>
          )}

          {isConfigurationIncomplete && (
            <div className='flex items-start gap-3 rounded-2xl border border-amber-200 bg-amber-50/60 p-4 text-amber-800 shadow-sm'>
              <Info className='mt-0.5 h-5 w-5 text-amber-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-amber-950'>Needs configuration</h4>
                <p className='text-xs text-amber-850 mt-0.5 leading-relaxed font-medium'>
                  Some pickup/drop-off, window or coordinate settings must be configured before planning.
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Main Grid Layout */}
        <div className='grid grid-cols-1 lg:grid-cols-12 gap-6 items-start'>
          {/* Main Column */}
          <div className='lg:col-span-8 space-y-6'>
            {/* Service Overview */}
            <SchoolBusSection title='Service overview'>
              <div className='p-5 bg-white rounded-2xl border border-slate-200 shadow-sm'>
                <div className='grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4'>
                  <div className='space-y-1'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>Student</span>
                    <div className='flex items-center gap-2'>
                      <div className='flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-violet-50 text-violet-600 border border-violet-100'>
                        <User className='h-3.5 w-3.5' />
                      </div>
                      <span className='text-sm font-bold text-slate-900'>{sub.studentName}</span>
                    </div>
                  </div>
                  <div className='space-y-1'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>Student Code</span>
                    <span className='text-sm font-semibold text-slate-700 font-mono'>{sub.studentCode || 'N/A'}</span>
                  </div>
                  <div className='space-y-1'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>Parent</span>
                    <span className='text-sm font-semibold text-slate-700'>{sub.parentName || 'N/A'}</span>
                  </div>
                  <div className='space-y-1'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>School</span>
                    <div className='flex items-center gap-2'>
                      <div className='flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-blue-50 text-blue-600 border border-blue-100'>
                        <GraduationCap className='h-3.5 w-3.5' />
                      </div>
                      <span className='text-sm font-semibold text-slate-700'>{sub.schoolName}</span>
                    </div>
                  </div>
                  <div className='space-y-1 md:col-span-2'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>School Code</span>
                    <span className='text-sm font-semibold text-slate-700 font-mono'>{sub.schoolCode || 'N/A'}</span>
                  </div>
                </div>
              </div>
            </SchoolBusSection>

            {/* Transport Configuration */}
            <SchoolBusSection title='Transport configuration'>
              <div className='p-5 bg-white rounded-2xl border border-slate-200 shadow-sm space-y-4'>
                <div>
                  <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1'>Trip Option</span>
                  <span className='inline-flex items-center rounded-lg border border-blue-100 bg-blue-50/50 px-2.5 py-1 text-xs font-bold text-blue-700'>
                    {TRIP_OPTION_LABELS[sub.tripOption] || sub.tripOption}
                  </span>
                </div>

                <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                  {/* Pickup Point */}
                  <div className='rounded-xl border border-slate-200 bg-slate-50/50 p-4'>
                    <div className='flex items-center gap-1.5 mb-2'>
                      <span className='text-[9px] font-extrabold text-blue-600 uppercase tracking-widest bg-blue-100/60 px-1.5 py-0.5 rounded'>P</span>
                      <h4 className='text-xs font-bold text-slate-500 uppercase tracking-wider'>Pickup Point</h4>
                    </div>
                    {sub.pickupPointName ? (
                      <div className='min-w-0'>
                        <p className='text-sm font-bold text-slate-900 truncate'>{sub.pickupPointName}</p>
                        <p className='text-[10px] text-slate-400 font-mono mt-1'>Code: {sub.pickupPointCode || 'N/A'}</p>
                      </div>
                    ) : (
                      <p className='text-sm font-semibold text-amber-600 italic bg-amber-50/60 px-2.5 py-1.5 rounded-lg border border-amber-100/30 inline-block'>
                        Missing configuration
                      </p>
                    )}
                  </div>

                  {/* Drop-off Point */}
                  <div className='rounded-xl border border-slate-200 bg-slate-50/50 p-4'>
                    <div className='flex items-center gap-1.5 mb-2'>
                      <span className='text-[9px] font-extrabold text-emerald-600 uppercase tracking-widest bg-emerald-100/60 px-1.5 py-0.5 rounded'>D</span>
                      <h4 className='text-xs font-bold text-slate-500 uppercase tracking-wider'>Drop-off Point</h4>
                    </div>
                    {sub.dropoffPointName ? (
                      <div className='min-w-0'>
                        <p className='text-sm font-bold text-slate-900 truncate'>{sub.dropoffPointName}</p>
                        <p className='text-[10px] text-slate-400 font-mono mt-1'>Code: {sub.dropoffPointCode || 'N/A'}</p>
                      </div>
                    ) : (
                      <p className='text-sm font-semibold text-amber-600 italic bg-amber-50/60 px-2.5 py-1.5 rounded-lg border border-amber-100/30 inline-block'>
                        Missing configuration
                      </p>
                    )}
                  </div>
                </div>
              </div>
            </SchoolBusSection>

            {/* Schedule & active days */}
            <SchoolBusSection title='Schedule & active days'>
              <div className='p-5 bg-white rounded-2xl border border-slate-200 shadow-sm space-y-4'>
                

                <div>
                  <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-2'>Active Days</span>
                  <div className='flex flex-wrap gap-1.5'>
                    {DAY_KEYS.map(({ key, label }) => {
                      const isActive = sub[key];
                      return (
                        <span
                          key={key}
                          className={cn(
                            'rounded-lg px-2.5 py-1 text-xs font-bold tracking-wide border shadow-sm transition-all',
                            isActive
                              ? 'bg-[#FDECEF] text-[#C81E3A] border-[#F6CDD5]'
                              : 'bg-slate-50 text-slate-400 border-slate-200/60 line-through'
                          )}
                        >
                          {label}
                        </span>
                      );
                    })}
                  </div>
                </div>
              </div>
            </SchoolBusSection>

            {/* Planning Readiness Checklist */}
            <SchoolBusSection title='Route Planning Readiness'>
              <div className={cn(
                'p-5 rounded-2xl border shadow-sm space-y-4 transition-all',
                isEligible
                  ? 'bg-emerald-50/10 border-emerald-200'
                  : 'bg-amber-50/10 border-amber-200'
              )}>
                <div className='flex items-center justify-between pb-3 border-b border-slate-200/60'>
                  <span className='text-sm text-slate-500 font-semibold'>Visible in route planning:</span>
                  <span
                    className={cn(
                      'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold shadow-sm border',
                      isEligible
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : 'bg-amber-50 text-amber-700 border-amber-200'
                    )}
                  >
                    {isEligible ? 'Yes (Ready)' : 'No (Blocked)'}
                  </span>
                </div>

                <div className='space-y-2.5 text-xs font-medium'>
                  {/* 1. Status Check */}
                  <div className='flex items-center gap-2'>
                    {isStatusActive ? (
                      <span className='text-emerald-600 font-bold'>✓</span>
                    ) : (
                      <span className='text-amber-500 font-bold'>⚠</span>
                    )}
                    <span className='text-slate-500 font-medium'>Subscription status is Active</span>
                    <span className='ml-auto text-slate-700'>{isStatusActive ? 'Active' : 'Inactive'}</span>
                  </div>

                  {/* 2. Effective date check */}
                  <div className='flex items-center gap-2'>
                    {isDateValid ? (
                      <span className='text-emerald-600 font-bold'>✓</span>
                    ) : (
                      <span className='text-amber-500 font-bold'>⚠</span>
                    )}
                    <span className='text-slate-500 font-medium'>Effective date validity</span>
                    <span className='ml-auto text-slate-700'>{isDateValid ? 'Within service period' : 'Outside period'}</span>
                  </div>

                  {/* 3. Days selected check */}
                  <div className='flex items-center gap-2'>
                    {hasDays ? (
                      <span className='text-emerald-600 font-bold'>✓</span>
                    ) : (
                      <span className='text-amber-500 font-bold'>⚠</span>
                    )}
                    <span className='text-slate-500 font-medium'>Active routing days defined</span>
                    <span className='ml-auto text-slate-700'>{hasDays ? 'Yes' : 'No days selected'}</span>
                  </div>

                  {/* 4. Points configured check */}
                  <div className='flex items-center gap-2'>
                    {pickupPointConfigured && dropoffPointConfigured ? (
                      <span className='text-emerald-600 font-bold'>✓</span>
                    ) : (
                      <span className='text-amber-500 font-bold'>⚠</span>
                    )}
                    <span className='text-slate-500 font-medium'>Pickup/drop-off points configured</span>
                    <span className='ml-auto text-slate-700'>
                      {pickupPointConfigured && dropoffPointConfigured ? 'Configured' : 'Missing points'}
                    </span>
                  </div>
                </div>

                <div className='rounded-xl bg-slate-50 p-3 text-slate-500 text-[11px] font-semibold leading-relaxed border border-slate-200/60'>
                  ⚠️ Detailed planning readiness (such as pickup windows, coordinates, and school link compatibility) will be evaluated during the route planning phase.
                </div>
              </div>
            </SchoolBusSection>
          </div>

          {/* Sidebar Column */}
          <div className='lg:col-span-4 space-y-6'>
            {/* Quick Summary Card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <h3 className='text-xs font-bold uppercase tracking-wider text-slate-500 pb-2 border-b border-slate-100'>
                Quick Summary
              </h3>
              <div className='space-y-3.5 text-sm'>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>Subscription Code</span>
                  <span className='font-mono font-bold text-slate-900'>{sub.subscriptionCode}</span>
                </div>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>Status</span>
                  <SchoolBusStatusBadge status={sub.status} />
                </div>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>Effective From</span>
                  <span className='font-semibold text-slate-700'>{formatDate(sub.effectiveFrom)}</span>
                </div>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>Effective To</span>
                  <span className='font-semibold text-slate-700'>{sub.effectiveTo ? formatDate(sub.effectiveTo) : 'Ongoing'}</span>
                </div>
              </div>
            </div>

            {/* Source Request */}
            {sub.sourceRequestId && (
              <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-3'>
                <h3 className='text-xs font-bold uppercase tracking-wider text-slate-500 pb-2 border-b border-slate-100'>
                  Source Request
                </h3>
                <div className='space-y-3'>
                  <div>
                    <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>Request Code</span>
                    <span className='font-semibold text-slate-900'>{sub.sourceRequestCode || `REQ-${sub.sourceRequestId}`}</span>
                  </div>
                  <div>
                    <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>Request ID</span>
                    <span className='font-semibold text-slate-700 font-mono text-xs'>#{sub.sourceRequestId}</span>
                  </div>
                  <div className='pt-1'>
                    <Link href={`/school-bus/requests/${sub.sourceRequestId}`}>
                      <Button variant='outline' className='h-8 text-xs rounded-xl font-bold border-slate-200 text-slate-700 hover:bg-slate-50 w-full'>
                        View source request
                      </Button>
                    </Link>
                  </div>
                </div>
              </div>
            )}

            {/* History Event Log */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <h3 className='text-xs font-bold uppercase tracking-wider text-slate-500 pb-2 border-b border-slate-100'>
                Contract History Log
              </h3>
              <SchoolBusTimeline
                events={timelineEvents}
                mode='compact'
                groupByDate={false}
                isLoading={historyLoading}
                isError={historyError}
                maxHeight='320px'
              />
            </div>
          </div>
        </div>
      </div>
    </SchoolBusPageShell>
  );
}
