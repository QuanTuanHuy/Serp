'use client';

import Link from 'next/link';
import * as React from 'react';
import {
  AlertCircle,
  AlertTriangle,
  CheckCircle2,
  MapPin,
  Pencil,
  XCircle,
  Clock,
  Check,
  AlertOctagon,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useApproveTransportRequestMutation,
  useCancelTransportRequestMutation,
  useGetTransportRequestByIdQuery,
  useGetTransportRequestHistoryQuery,
  useRejectTransportRequestMutation,
} from '../api/schoolBusApi';
import {
  SchoolBusTimeline,
  mapRequestHistoryToTimeline,
} from '../components/history';
import { RejectTransportRequestDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { OperationsMap } from '../components/map/OperationsMap';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import type { StudentMapMarker } from '../components/map/OperationsMapClient';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime } from '../utils';
import type { SchoolBusRequestStudent } from '../types';
import { useSchoolBusAccess } from '../security/schoolBusAccess';

// ─── helpers ────────────────────────────────────────────────────────────────

const TRIP_OPTION_LABELS: Record<string, string> = {
  MORNING: 'To school only',
  AFTERNOON: 'From school only',
  ROUND_TRIP: 'Round trip',
};

const REQUEST_TYPE_LABELS: Record<string, string> = {
  NEW_SERVICE: 'New service',
  CHANGE_SERVICE: 'Change service',
  PAUSE_SERVICE: 'Pause service',
  RESUME_SERVICE: 'Resume service',
  STOP_SERVICE: 'Stop service',
  RENEW_SERVICE: 'Renew service',
};

const DAY_KEYS: { key: keyof SchoolBusRequestStudent; label: string }[] = [
  { key: 'monday', label: 'Mon' },
  { key: 'tuesday', label: 'Tue' },
  { key: 'wednesday', label: 'Wed' },
  { key: 'thursday', label: 'Thu' },
  { key: 'friday', label: 'Fri' },
  { key: 'saturday', label: 'Sat' },
  { key: 'sunday', label: 'Sun' },
];

/** Row-level completeness check derived purely from snapshot data on the detail page. */
function deriveEligibility(
  s: SchoolBusRequestStudent,
  requiresRouting: boolean
): {
  state: 'complete' | 'needs-config' | 'blocking';
  label: string;
  issues: string[];
} {
  const issues: string[] = [];
  if (!s.studentId) issues.push('Missing student selection');
  if (requiresRouting) {
    if (!s.tripOption) issues.push('Missing trip option');
    const opt = (s.tripOption || '').toUpperCase();
    const needsPickup = opt === 'MORNING' || opt === 'ROUND_TRIP';
    const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';
    if (needsPickup && !s.pickupPointId) issues.push('Missing pickup point');
    if (needsDropoff && !s.dropoffPointId)
      issues.push('Missing drop-off point');
  }

  // Check if at least one day is selected
  const hasDays =
    s.monday ||
    s.tuesday ||
    s.wednesday ||
    s.thursday ||
    s.friday ||
    s.saturday ||
    s.sunday;
  if (!hasDays) {
    issues.push('Missing selected days');
  }

  // Check coordinates if points are selected
  if (
    s.pickupPointId &&
    (typeof s.pickupPointLatitude !== 'number' ||
      typeof s.pickupPointLongitude !== 'number')
  ) {
    issues.push('Missing coordinates for pickup point');
  }
  if (
    s.dropoffPointId &&
    (typeof s.dropoffPointLatitude !== 'number' ||
      typeof s.dropoffPointLongitude !== 'number')
  ) {
    issues.push('Missing coordinates for drop-off point');
  }

  if (issues.length > 0)
    return { state: 'blocking', label: 'Needs configuration', issues };
  return { state: 'complete', label: 'Complete', issues: [] };
}

// ─── sub-components ─────────────────────────────────────────────────────────

function Pill({ label, className }: { label: string; className: string }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold shadow-sm',
        className
      )}
    >
      {label}
    </span>
  );
}

function DayBadges({ student }: { student: SchoolBusRequestStudent }) {
  return (
    <div className='flex flex-wrap gap-1.5'>
      {DAY_KEYS.map(({ key, label }) => (
        <span
          key={key}
          className={cn(
            'rounded-md px-2 py-0.5 text-[10px] font-bold transition-all',
            student[key]
              ? 'bg-[#FDECEF] text-[#C81E3A] border border-[#F6CDD5]'
              : 'bg-slate-50 text-slate-400 border border-slate-200/60 line-through'
          )}
        >
          {label}
        </span>
      ))}
    </div>
  );
}

function InfoRow({
  label,
  value,
  className,
}: {
  label: string;
  value: React.ReactNode;
  className?: string;
}) {
  return (
    <div
      className={cn(
        'flex items-start justify-between py-2.5 border-b border-slate-100 last:border-0 text-sm gap-4',
        className
      )}
    >
      <span className='text-slate-500 font-medium shrink-0'>{label}</span>
      <span className='text-slate-900 font-semibold text-right break-words max-w-[70%]'>
        {value}
      </span>
    </div>
  );
}

// ─── main page ───────────────────────────────────────────────────────────────

interface SchoolBusRequestDetailPageProps {
  requestId: number;
}

export function SchoolBusRequestDetailPage({
  requestId,
}: SchoolBusRequestDetailPageProps) {
  const access = useSchoolBusAccess();
  const { data, isLoading } = useGetTransportRequestByIdQuery(requestId);
  const [approveTransportRequest, { isLoading: approving }] =
    useApproveTransportRequestMutation();
  const [rejectTransportRequest, { isLoading: rejecting }] =
    useRejectTransportRequestMutation();
  const [cancelTransportRequest] = useCancelTransportRequestMutation();
  const {
    data: historyData,
    isLoading: historyLoading,
    isError: historyError,
  } = useGetTransportRequestHistoryQuery(requestId);
  const [rejectOpen, setRejectOpen] = React.useState(false);
  const [fitAllKey, setFitAllKey] = React.useState(0);
  const detail = data?.data;

  const handleApprove = async () => {
    try {
      const res = await approveTransportRequest(requestId).unwrap();
      toast.success(res.message || 'Transport request approved');
    } catch (e: any) {
      toast.error(e?.data?.message || 'Failed to approve');
    }
  };
  const handleCancel = async () => {
    try {
      const res = await cancelTransportRequest(requestId).unwrap();
      toast.success(res.message || 'Transport request cancelled');
    } catch (e: any) {
      toast.error(e?.data?.message || 'Failed to cancel');
    }
  };
  const handleReject = async (values: { reason: string }) => {
    try {
      const res = await rejectTransportRequest({
        id: requestId,
        body: values,
      }).unwrap();
      toast.success(res.message || 'Transport request rejected');
      setRejectOpen(false);
    } catch (e: any) {
      toast.error(e?.data?.message || 'Failed to reject');
    }
  };

  if (isLoading || !detail) {
    return (
      <SchoolBusPageShell
        title='Transport Request Detail'
        description='Loading…'
      >
        <SchoolBusEmptyState
          title='Loading request detail'
          description='Fetching data…'
        />
      </SchoolBusPageShell>
    );
  }

  const request = detail.request;
  const students = detail.students;
  const requiresRouting = [
    'NEW_SERVICE',
    'CHANGE_SERVICE',
    'RENEW_SERVICE',
  ].includes(request.requestType);
  const isSubmitted = request.status === 'SUBMITTED';

  // ── Eligibility analysis
  const rowEligibility = students.map((s) =>
    deriveEligibility(s, requiresRouting)
  );
  const blockingIssues = rowEligibility.flatMap((r, i) =>
    r.issues.map((issue) => `${students[i].studentName}: ${issue}`)
  );
  const allReady = blockingIssues.length === 0;

  // ── Map data
  const studentMarkers: StudentMapMarker[] = students.reduce<
    StudentMapMarker[]
  >((acc, s) => {
    if (
      s.pickupPointId &&
      typeof s.pickupPointLatitude === 'number' &&
      typeof s.pickupPointLongitude === 'number'
    ) {
      acc.push({
        key: `pickup-${s.id}`,
        studentName: s.studentName,
        pointName: s.pickupPointName || 'Pickup',
        latitude: s.pickupPointLatitude,
        longitude: s.pickupPointLongitude,
        role: 'pickup',
      });
    }
    if (
      s.dropoffPointId &&
      typeof s.dropoffPointLatitude === 'number' &&
      typeof s.dropoffPointLongitude === 'number' &&
      (s.dropoffPointId !== s.pickupPointId ||
        s.dropoffPointLatitude !== s.pickupPointLatitude)
    ) {
      acc.push({
        key: `dropoff-${s.id}`,
        studentName: s.studentName,
        pointName: s.dropoffPointName || 'Drop-off',
        latitude: s.dropoffPointLatitude,
        longitude: s.dropoffPointLongitude,
        role: 'dropoff',
      });
    }
    return acc;
  }, []);

  const uniquePickupPoints = students
    .filter(
      (s) =>
        s.pickupPointId &&
        typeof s.pickupPointLatitude === 'number' &&
        typeof s.pickupPointLongitude === 'number'
    )
    .reduce<
      {
        id: number;
        name: string;
        address: string;
        latitude: number;
        longitude: number;
      }[]
    >((acc, s) => {
      if (!acc.find((p) => p.id === s.pickupPointId)) {
        acc.push({
          id: s.pickupPointId!,
          name: s.pickupPointName || 'Pickup',
          address: s.pickupPointAddress || '',
          latitude: s.pickupPointLatitude!,
          longitude: s.pickupPointLongitude!,
        });
      }
      return acc;
    }, []);

  const uniqueDropoffPoints = students
    .filter(
      (s) =>
        s.dropoffPointId &&
        typeof s.dropoffPointLatitude === 'number' &&
        typeof s.dropoffPointLongitude === 'number'
    )
    .reduce<
      {
        id: number;
        name: string;
        address: string;
        latitude: number;
        longitude: number;
      }[]
    >((acc, s) => {
      if (!acc.find((p) => p.id === s.dropoffPointId)) {
        acc.push({
          id: s.dropoffPointId!,
          name: s.dropoffPointName || 'Drop-off',
          address: s.dropoffPointAddress || '',
          latitude: s.dropoffPointLatitude!,
          longitude: s.dropoffPointLongitude!,
        });
      }
      return acc;
    }, []);

  // ── Approval Checklist metrics
  const checklist = [
    {
      label: 'Students selected',
      passed: students.length > 0 && students.every((s) => s.studentId),
    },
    {
      label: 'Trip options set',
      passed: !requiresRouting || students.every((s) => s.tripOption),
    },
    {
      label: 'Points selected',
      passed:
        !requiresRouting ||
        students.every((s) => {
          const opt = (s.tripOption || '').toUpperCase();
          if ((opt === 'MORNING' || opt === 'ROUND_TRIP') && !s.pickupPointId)
            return false;
          if (
            (opt === 'AFTERNOON' || opt === 'ROUND_TRIP') &&
            !s.dropoffPointId
          )
            return false;
          return true;
        }),
    },
    {
      label: 'Active days active',
      passed: students.every(
        (s) =>
          s.monday ||
          s.tuesday ||
          s.wednesday ||
          s.thursday ||
          s.friday ||
          s.saturday ||
          s.sunday
      ),
    },
    {
      label: 'Coordinates verified',
      passed: students.every((s) => {
        if (
          s.pickupPointId &&
          (typeof s.pickupPointLatitude !== 'number' ||
            typeof s.pickupPointLongitude !== 'number')
        )
          return false;
        if (
          s.dropoffPointId &&
          (typeof s.dropoffPointLatitude !== 'number' ||
            typeof s.dropoffPointLongitude !== 'number')
        )
          return false;
        return true;
      }),
    },
  ];

  return (
    <>
      <SchoolBusPageShell
        title={`Transport Request #${request.requestCode || request.id}`}
        description={
          access.isParentOnly
            ? 'View transport request details, student information, and processing status.'
            : 'Inspect the full request payload, review the student list, and take approval actions.'
        }
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Transport Requests', href: '/school-bus/requests' },
              { label: `#${request.requestCode || request.id}`, current: true },
            ]}
          />
        }
        actions={
          <div className='flex flex-wrap items-center gap-2'>
            {/* Parent: Edit (if editable) and Cancel (if cancellable) — no Approve/Reject */}
            {access.isParentOnly ? (
              <>
                {(isSubmitted || request.status === 'REJECTED') && (
                  <Button variant='outline' className='rounded-full' asChild>
                    <Link href={`/school-bus/requests/${request.id}/edit`}>
                      <Pencil className='h-4 w-4' /> Edit
                    </Link>
                  </Button>
                )}
                {(isSubmitted || request.status === 'DRAFT') && (
                  <Button
                    variant='outline'
                    className='rounded-full'
                    onClick={handleCancel}
                  >
                    Cancel
                  </Button>
                )}
              </>
            ) : (
              <>
                {isSubmitted && (
                  <Button variant='outline' className='rounded-full' asChild>
                    <Link href={`/school-bus/requests/${request.id}/edit`}>
                      <Pencil className='h-4 w-4' /> Edit
                    </Link>
                  </Button>
                )}
                {(isSubmitted || request.status === 'DRAFT') && (
                  <Button
                    variant='outline'
                    className='rounded-full'
                    onClick={handleCancel}
                  >
                    Cancel
                  </Button>
                )}
                {isSubmitted && (
                  <Button
                    variant='outline'
                    className='rounded-full'
                    onClick={() => setRejectOpen(true)}
                  >
                    <XCircle className='h-4 w-4' /> Reject
                  </Button>
                )}
                {isSubmitted && (
                  <Button
                    className='rounded-full bg-[#C81E3A] text-white hover:bg-[#B31B34] font-medium px-6'
                    disabled={!allReady}
                    onClick={handleApprove}
                  >
                    <CheckCircle2 className='h-4 w-4' />
                    {approving ? 'Approving…' : 'Approve'}
                  </Button>
                )}
              </>
            )}
          </div>
        }
      >
        <div className='space-y-6'>
          {/* ── Status subtitle row */}
          <div className='flex flex-wrap items-center gap-2.5 bg-slate-50 border border-slate-200/60 rounded-xl p-3.5'>
            <SchoolBusStatusBadge status={request.status} />
            <span className='text-slate-300'>·</span>
            <span className='text-sm font-semibold text-slate-700'>
              {REQUEST_TYPE_LABELS[request.requestType] ?? request.requestType}
            </span>
            <span className='text-slate-300'>·</span>
            <span className='text-sm font-medium text-slate-500'>
              Effective {formatDate(request.effectiveFrom)}
              {request.effectiveTo
                ? ` – ${formatDate(request.effectiveTo)}`
                : ''}
            </span>
          </div>

          {/* ── Status / Eligibility Banner */}
          {access.isParentOnly ? (
            /* ── Parent-friendly status banner */
            <div
              className={cn(
                'flex items-center gap-3 rounded-2xl border p-4.5 shadow-sm',
                request.status === 'SUBMITTED' &&
                  'border-blue-200 bg-blue-50/40 text-blue-800',
                request.status === 'APPROVED' &&
                  'border-emerald-200 bg-emerald-50/40 text-emerald-800',
                request.status === 'REJECTED' &&
                  'border-red-200 bg-red-50/40 text-red-800',
                ['CANCELLED', 'DRAFT'].includes(request.status) &&
                  'border-slate-200 bg-slate-50 text-slate-700'
              )}
            >
              {request.status === 'SUBMITTED' && (
                <Clock className='h-5 w-5 shrink-0 text-blue-600' />
              )}
              {request.status === 'APPROVED' && (
                <CheckCircle2 className='h-5 w-5 shrink-0 text-emerald-600' />
              )}
              {request.status === 'REJECTED' && (
                <XCircle className='h-5 w-5 shrink-0 text-red-600' />
              )}
              {['CANCELLED', 'DRAFT'].includes(request.status) && (
                <Clock className='h-5 w-5 shrink-0 text-slate-500' />
              )}
              <div>
                <span className='text-sm font-bold block'>
                  {request.status === 'SUBMITTED' &&
                    'Request is waiting for review'}
                  {request.status === 'APPROVED' && 'Request approved'}
                  {request.status === 'REJECTED' && 'Request rejected'}
                  {request.status === 'CANCELLED' && 'Request cancelled'}
                  {request.status === 'DRAFT' && 'Draft request'}
                </span>
                <span className='text-xs font-medium opacity-80'>
                  {request.status === 'SUBMITTED' &&
                    'All required information has been submitted.'}
                  {request.status === 'APPROVED' &&
                    'This transport request has been approved.'}
                  {request.status === 'REJECTED' &&
                    'Please review the rejection reason below.'}
                  {request.status === 'CANCELLED' &&
                    'This request has been cancelled.'}
                  {request.status === 'DRAFT' &&
                    'This draft has not been submitted yet.'}
                </span>
              </div>
            </div>
          ) : (
            /* ── Admin/Dispatcher approval banner */
            <>
              {request.status === 'SUBMITTED' ? (
                <div
                  className={cn(
                    'flex items-start gap-3.5 rounded-2xl border p-5 shadow-sm transition-all',
                    allReady
                      ? 'border-emerald-200 bg-emerald-50/60'
                      : 'border-amber-200 bg-amber-50/60'
                  )}
                >
                  {allReady ? (
                    <CheckCircle2 className='mt-0.5 h-6 w-6 shrink-0 text-emerald-600' />
                  ) : (
                    <AlertTriangle className='mt-0.5 h-6 w-6 shrink-0 text-amber-600' />
                  )}
                  <div className='min-w-0'>
                    <p
                      className={cn(
                        'font-bold text-base',
                        allReady ? 'text-emerald-900' : 'text-amber-900'
                      )}
                    >
                      {allReady
                        ? 'Eligible for approval'
                        : `Cannot approve yet — ${blockingIssues.length} approval blocker${blockingIssues.length > 1 ? 's' : ''} must be fixed`}
                    </p>
                    {allReady ? (
                      <p className='text-xs text-emerald-700 mt-1 font-medium'>
                        All required student, pickup/drop-off, and coordinate
                        checks passed.
                      </p>
                    ) : (
                      <ul className='mt-2.5 space-y-1 bg-white/50 rounded-xl p-3 border border-amber-200/50'>
                        {blockingIssues.map((msg, i) => (
                          <li
                            key={i}
                            className='flex items-center gap-2 text-xs font-semibold text-amber-800'
                          >
                            <AlertCircle className='h-4 w-4 shrink-0 text-amber-600' />{' '}
                            {msg}
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                </div>
              ) : (
                <div
                  className={cn(
                    'flex items-center gap-3 rounded-2xl border p-4.5 shadow-sm',
                    request.status === 'APPROVED' &&
                      'border-emerald-200 bg-emerald-50/40 text-emerald-800',
                    request.status === 'REJECTED' &&
                      'border-red-200 bg-red-50/40 text-red-800',
                    ['CANCELLED', 'DRAFT'].includes(request.status) &&
                      'border-slate-200 bg-slate-50 text-slate-700'
                  )}
                >
                  {request.status === 'APPROVED' && (
                    <CheckCircle2 className='h-5 w-5 shrink-0 text-emerald-600' />
                  )}
                  {request.status === 'REJECTED' && (
                    <XCircle className='h-5 w-5 shrink-0 text-red-600' />
                  )}
                  {['CANCELLED', 'DRAFT'].includes(request.status) && (
                    <Clock className='h-5 w-5 shrink-0 text-slate-500' />
                  )}
                  <span className='text-sm font-bold'>
                    Request is currently {request.status.toLowerCase()} — no
                    further actions required.
                  </span>
                </div>
              )}
            </>
          )}

          {/* ── Main layout grid */}
          <div className='grid gap-6 lg:grid-cols-12 items-start'>
            {/* ── Left column (Main content) */}
            <div className='lg:col-span-8 space-y-6'>
              {/* Requested students */}
              <SchoolBusSection
                title='Requested students'
                description={
                  access.isParentOnly
                    ? 'Students included in this transport request.'
                    : 'Linked students, trip options, stops, and active days.'
                }
              >
                <div className='grid gap-4 sm:grid-cols-1'>
                  {students.map((s, i) => {
                    const r = rowEligibility[i];
                    const opt = (s.tripOption || '').toUpperCase();
                    const needsPickup =
                      opt === 'MORNING' || opt === 'ROUND_TRIP';
                    const needsDropoff =
                      opt === 'AFTERNOON' || opt === 'ROUND_TRIP';
                    return (
                      <div
                        key={s.id}
                        className='rounded-2xl border border-slate-200 bg-white p-5 hover:border-slate-300 hover:shadow-md transition-all duration-200 space-y-4'
                      >
                        {/* Student card header */}
                        <div className='flex flex-wrap items-start justify-between gap-3 border-b border-slate-100 pb-3'>
                          <div className='min-w-0'>
                            <p className='font-bold text-slate-900 text-base truncate'>
                              {s.studentName}
                            </p>
                            {s.studentCode && (
                              <p className='text-xs font-bold text-slate-400 mt-0.5'>
                                {s.studentCode}
                              </p>
                            )}
                          </div>
                          <span
                            className={cn(
                              'inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold ring-1 ring-inset',
                              r.state === 'complete'
                                ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
                                : 'bg-amber-50 text-amber-700 ring-amber-600/20'
                            )}
                          >
                            {r.state === 'complete' ? (
                              <CheckCircle2 className='h-3.5 w-3.5' />
                            ) : (
                              <AlertTriangle className='h-3.5 w-3.5' />
                            )}
                            {r.label}
                          </span>
                        </div>

                        {/* Student card details grid */}
                        <div className='grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4 text-xs sm:text-sm'>
                          <div>
                            <span className='text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1'>
                              Trip option
                            </span>
                            {s.tripOption ? (
                              <Pill
                                label={TRIP_OPTION_LABELS[opt] ?? s.tripOption}
                                className='bg-blue-50 text-blue-700 border border-blue-200'
                              />
                            ) : (
                              <span className='text-red-500 font-bold'>
                                Not configured
                              </span>
                            )}
                          </div>

                          {/* Pickup point */}
                          {(needsPickup || !s.tripOption) && (
                            <div className='md:col-span-1'>
                              <span className='text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1'>
                                Pickup point
                              </span>
                              {s.pickupPointName ? (
                                <div className='space-y-0.5 min-w-0'>
                                  <p className='font-semibold text-slate-800 truncate flex items-center gap-1.5'>
                                    <MapPin className='h-3.5 w-3.5 shrink-0 text-[#C81E3A]' />
                                    {s.pickupPointName}
                                  </p>
                                  {s.pickupPointAddress && (
                                    <p className='text-xs text-slate-500 truncate pl-5'>
                                      {s.pickupPointAddress}
                                    </p>
                                  )}
                                </div>
                              ) : (
                                <span
                                  className={cn(
                                    'font-bold',
                                    needsPickup
                                      ? 'text-red-500'
                                      : 'text-slate-400 pl-5'
                                  )}
                                >
                                  {needsPickup
                                    ? 'Required — not configured'
                                    : 'Not required'}
                                </span>
                              )}
                            </div>
                          )}

                          {/* Drop-off point */}
                          {(needsDropoff || !s.tripOption) && (
                            <div className='md:col-span-1'>
                              <span className='text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1'>
                                Drop-off point
                              </span>
                              {s.dropoffPointName ? (
                                <div className='space-y-0.5 min-w-0'>
                                  <p className='font-semibold text-slate-800 truncate flex items-center gap-1.5'>
                                    <MapPin className='h-3.5 w-3.5 shrink-0 text-blue-500' />
                                    {s.dropoffPointName}
                                  </p>
                                  {s.dropoffPointAddress && (
                                    <p className='text-xs text-slate-500 truncate pl-5'>
                                      {s.dropoffPointAddress}
                                    </p>
                                  )}
                                </div>
                              ) : (
                                <span
                                  className={cn(
                                    'font-bold',
                                    needsDropoff
                                      ? 'text-red-500'
                                      : 'text-slate-400 pl-5'
                                  )}
                                >
                                  {needsDropoff
                                    ? 'Required — not configured'
                                    : 'Not required'}
                                </span>
                              )}
                            </div>
                          )}

                          {/* Days active */}
                          <div className='md:col-span-2 border-t border-slate-100 pt-3'>
                            <span className='text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-2'>
                              Active days
                            </span>
                            <DayBadges student={s} />
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </SchoolBusSection>

              {/* Request map */}
              <SchoolBusSection
                title='Request map'
                description={
                  access.isParentOnly
                    ? 'Map view of the school and selected pickup/drop-off points.'
                    : 'Geographical overview of the school hub, configured stops, and students.'
                }
              >
                <div className='mt-2 rounded-2xl overflow-hidden border border-slate-200 shadow-sm'>
                  <SchoolBusMapWorkspace
                    defaultPreset='map-focus'
                    map={
                      <OperationsMap
                        schools={[
                          {
                            id: request.schoolId,
                            name: request.schoolName,
                            latitude: request.schoolLatitude,
                            longitude: request.schoolLongitude,
                            address: undefined,
                          },
                        ]}
                        pickupPoints={[
                          ...uniquePickupPoints.map((p) => ({
                            ...p,
                            schoolId: request.schoolId,
                            schoolName: request.schoolName,
                          })),
                          ...uniqueDropoffPoints.map((p) => ({
                            ...p,
                            schoolId: request.schoolId,
                            schoolName: request.schoolName,
                          })),
                        ]}
                        studentMarkers={studentMarkers}
                        selectedSchoolId={request.schoolId}
                        fitAllKey={fitAllKey}
                        className='h-full w-full'
                      />
                    }
                    onFitAll={() => setFitAllKey((k) => k + 1)}
                    canFitAll
                    legend={<SchoolBusMapLegend />}
                    panel={
                      <div className='space-y-4'>
                        <div>
                          <p className='text-sm font-bold text-slate-900'>
                            Stop context
                          </p>
                          <p className='text-xs text-slate-500 mt-0.5'>
                            {REQUEST_TYPE_LABELS[request.requestType] ??
                              request.requestType}
                          </p>
                        </div>
                        <div className='grid grid-cols-3 gap-2 text-center'>
                          {[
                            { label: 'Students', value: students.length },
                            {
                              label: 'Pickups',
                              value: uniquePickupPoints.length,
                            },
                            {
                              label: 'Dropoffs',
                              value: uniqueDropoffPoints.length,
                            },
                          ].map(({ label, value }) => (
                            <div
                              key={label}
                              className='rounded-xl bg-slate-50 border border-slate-200/60 p-2 shadow-sm'
                            >
                              <p className='text-base font-bold text-slate-900'>
                                {value}
                              </p>
                              <p className='text-[9px] font-semibold text-slate-400 uppercase'>
                                {label}
                              </p>
                            </div>
                          ))}
                        </div>
                        <div className='space-y-2 max-h-[250px] overflow-auto pr-1'>
                          {students.map((s) => (
                            <div
                              key={s.id}
                              className='rounded-xl border border-slate-150 bg-white p-3 hover:shadow-sm transition-all'
                            >
                              <p className='text-xs font-bold text-slate-900 truncate'>
                                {s.studentName}
                              </p>
                              {s.pickupPointId && (
                                <p className='flex items-center gap-1.5 mt-1.5 text-[11px] text-slate-500 truncate'>
                                  <MapPin className='h-3 w-3 shrink-0 text-[#C81E3A]' />
                                  <span className='font-medium truncate'>
                                    {s.pickupPointName}
                                  </span>
                                </p>
                              )}
                              {s.dropoffPointId &&
                                s.dropoffPointId !== s.pickupPointId && (
                                  <p className='flex items-center gap-1.5 mt-0.5 text-[11px] text-slate-500 truncate'>
                                    <MapPin className='h-3 w-3 shrink-0 text-blue-500' />
                                    <span className='font-medium truncate'>
                                      {s.dropoffPointName}
                                    </span>
                                  </p>
                                )}
                            </div>
                          ))}
                        </div>
                      </div>
                    }
                  />
                </div>
              </SchoolBusSection>
            </div>

            {/* ── Right column (Sidebar panel) */}
            <div className='lg:col-span-4 space-y-6'>
              {/* Request summary */}
              <div className='rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-4'>
                <h3 className='font-bold text-slate-900 text-base'>
                  Request summary
                </h3>
                <div className='flex flex-col border-t border-slate-100 mt-1'>
                  {!access.isParentOnly && (
                    <InfoRow
                      label='Parent name'
                      value={request.parentProfileName}
                    />
                  )}
                  <InfoRow label='School name' value={request.schoolName} />
                  <InfoRow
                    label='Request type'
                    value={
                      REQUEST_TYPE_LABELS[request.requestType] ??
                      request.requestType
                    }
                  />
                  <InfoRow
                    label='Status'
                    value={<SchoolBusStatusBadge status={request.status} />}
                  />
                  <InfoRow
                    label='Effective from'
                    value={formatDate(request.effectiveFrom)}
                  />
                  <InfoRow
                    label='Effective to'
                    value={
                      request.effectiveTo
                        ? formatDate(request.effectiveTo)
                        : 'Open-ended'
                    }
                  />
                  <InfoRow
                    label='Submitted at'
                    value={formatDateTime(request.requestedAt)}
                  />
                  <InfoRow
                    label='Approved at'
                    value={
                      request.approvedAt
                        ? formatDateTime(request.approvedAt)
                        : '—'
                    }
                  />
                  {request.rejectionReason && (
                    <InfoRow
                      label='Rejection reason'
                      value={
                        <span className='text-red-600 font-semibold'>
                          {request.rejectionReason}
                        </span>
                      }
                    />
                  )}
                  {request.notes && (
                    <div className='py-3 text-sm'>
                      <p className='text-slate-500 font-medium mb-1'>
                        User notes
                      </p>
                      <p className='text-slate-800 bg-slate-50 border border-slate-100 rounded-xl p-2.5 text-xs font-semibold leading-relaxed'>
                        {request.notes}
                      </p>
                    </div>
                  )}
                </div>
              </div>

              {/* Approval checklist — Admin/Dispatcher only */}
              {isSubmitted && !access.isParentOnly && (
                <div className='rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-4'>
                  <h3 className='font-bold text-slate-900 text-base'>
                    Approval checklist
                  </h3>
                  <div className='space-y-3 mt-1'>
                    {checklist.map((item, idx) => (
                      <div
                        key={idx}
                        className='flex items-start gap-3 text-xs sm:text-sm'
                      >
                        {item.passed ? (
                          <div className='rounded-full bg-emerald-50 p-1 border border-emerald-200 shrink-0'>
                            <Check className='h-3.5 w-3.5 text-emerald-600 font-bold' />
                          </div>
                        ) : (
                          <div className='rounded-full bg-red-50 p-1 border border-red-200 shrink-0'>
                            <AlertOctagon className='h-3.5 w-3.5 text-red-500' />
                          </div>
                        )}
                        <span
                          className={cn(
                            'font-semibold mt-0.5',
                            item.passed ? 'text-slate-700' : 'text-slate-500'
                          )}
                        >
                          {item.label}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Request history */}
              <div className='rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-4'>
                <h3 className='font-bold text-slate-900 text-base'>
                  Request history
                </h3>
                <div className='border-t border-slate-100 pt-3'>
                  <SchoolBusTimeline
                    events={mapRequestHistoryToTimeline(
                      historyData?.data ?? []
                    )}
                    mode='compact'
                    isLoading={historyLoading}
                    isError={historyError}
                    maxHeight='320px'
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </SchoolBusPageShell>

      <RejectTransportRequestDialog
        open={rejectOpen}
        onOpenChange={setRejectOpen}
        onSubmit={handleReject}
        isLoading={rejecting}
      />
    </>
  );
}
