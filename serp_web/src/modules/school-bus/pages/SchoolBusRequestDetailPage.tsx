'use client';

import Link from 'next/link';
import * as React from 'react';
import { AlertCircle, AlertTriangle, CheckCircle2, MapPin, Pencil, XCircle } from 'lucide-react';
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
import { SchoolBusTimeline, mapRequestHistoryToTimeline } from '../components/history';
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

// ─── helpers ────────────────────────────────────────────────────────────────

const TRIP_OPTION_LABELS: Record<string, string> = {
  MORNING: 'To school',
  AFTERNOON: 'From school',
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

/** Row-level readiness derived purely from snapshot data on the detail page. */
function deriveReadiness(s: SchoolBusRequestStudent, requiresRouting: boolean): {
  state: 'ready' | 'needs-config' | 'blocking';
  label: string;
  issues: string[];
} {
  const issues: string[] = [];
  if (!s.studentId) issues.push('Missing student');
  if (requiresRouting) {
    if (!s.schoolScheduleId) issues.push('Missing schedule');
    if (!s.tripOption) issues.push('Missing trip option');
    const opt = (s.tripOption || '').toUpperCase();
    const needsPickup = opt === 'MORNING' || opt === 'ROUND_TRIP';
    const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';
    if (needsPickup && !s.pickupPointId) issues.push('Missing pickup point');
    if (needsDropoff && !s.dropoffPointId) issues.push('Missing drop-off point');
  }
  if (issues.length > 0) return { state: 'blocking', label: 'Missing required fields', issues };

  // With snapshot we can't check window/coord — show "ready" or neutral
  return { state: 'ready', label: 'Ready', issues: [] };
}

// ─── sub-components ─────────────────────────────────────────────────────────

function Pill({ label, className }: { label: string; className: string }) {
  return (
    <span className={cn('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', className)}>
      {label}
    </span>
  );
}

function DayBadges({ student }: { student: SchoolBusRequestStudent }) {
  return (
    <div className='flex flex-wrap gap-1'>
      {DAY_KEYS.map(({ key, label }) => (
        <span
          key={key}
          className={cn(
            'rounded px-1.5 py-0.5 text-[10px] font-semibold',
            student[key]
              ? 'bg-[#FDECEF] text-[#C81E3A]'
              : 'bg-slate-100 text-slate-400 line-through'
          )}
        >
          {label}
        </span>
      ))}
    </div>
  );
}

function InfoRow({ label, value, wide }: { label: string; value: React.ReactNode; wide?: boolean }) {
  return (
    <div className={cn('flex flex-col gap-0.5', wide && 'md:col-span-2')}>
      <span className='text-[11px] font-semibold uppercase tracking-wide text-slate-400'>{label}</span>
      <span className='text-sm font-medium text-slate-900 break-words'>{value}</span>
    </div>
  );
}

// ─── main page ───────────────────────────────────────────────────────────────

interface SchoolBusRequestDetailPageProps { requestId: number }

export function SchoolBusRequestDetailPage({ requestId }: SchoolBusRequestDetailPageProps) {
  const { data, isLoading } = useGetTransportRequestByIdQuery(requestId);
  const [approveTransportRequest, { isLoading: approving }] = useApproveTransportRequestMutation();
  const [rejectTransportRequest, { isLoading: rejecting }] = useRejectTransportRequestMutation();
  const [cancelTransportRequest] = useCancelTransportRequestMutation();
  const { data: historyData, isLoading: historyLoading, isError: historyError } =
    useGetTransportRequestHistoryQuery(requestId);
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
      const res = await rejectTransportRequest({ id: requestId, body: values }).unwrap();
      toast.success(res.message || 'Transport request rejected');
      setRejectOpen(false);
    } catch (e: any) {
      toast.error(e?.data?.message || 'Failed to reject');
    }
  };

  if (isLoading || !detail) {
    return (
      <SchoolBusPageShell
        title='Transport request detail'
        description='Loading…'
        breadcrumb={
          <SchoolBusBreadcrumb items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Requests', href: '/school-bus/requests' },
            { label: 'Detail', current: true },
          ]} />
        }
      >
        <SchoolBusEmptyState title='Loading request detail' description='Fetching data…' />
      </SchoolBusPageShell>
    );
  }

  const request = detail.request;
  const students = detail.students;
  const requiresRouting = ['NEW_SERVICE', 'CHANGE_SERVICE', 'RENEW_SERVICE'].includes(request.requestType);
  const isSubmitted = request.status === 'SUBMITTED';

  // ── Readiness analysis
  const rowReadiness = students.map((s) => deriveReadiness(s, requiresRouting));
  const blockingIssues = rowReadiness.flatMap((r, i) =>
    r.issues.map((issue) => `${students[i].studentName}: ${issue}`)
  );
  const allReady = blockingIssues.length === 0;

  // ── Map data
  const studentMarkers: StudentMapMarker[] = students.reduce<StudentMapMarker[]>((acc, s) => {
    if (s.pickupPointId && typeof s.pickupPointLatitude === 'number' && typeof s.pickupPointLongitude === 'number') {
      acc.push({ key: `pickup-${s.id}`, studentName: s.studentName, pointName: s.pickupPointName || 'Pickup', latitude: s.pickupPointLatitude, longitude: s.pickupPointLongitude, role: 'pickup' });
    }
    if (s.dropoffPointId && typeof s.dropoffPointLatitude === 'number' && typeof s.dropoffPointLongitude === 'number'
      && (s.dropoffPointId !== s.pickupPointId || s.dropoffPointLatitude !== s.pickupPointLatitude)) {
      acc.push({ key: `dropoff-${s.id}`, studentName: s.studentName, pointName: s.dropoffPointName || 'Drop-off', latitude: s.dropoffPointLatitude, longitude: s.dropoffPointLongitude, role: 'dropoff' });
    }
    return acc;
  }, []);

  const uniquePickupPoints = students
    .filter((s) => s.pickupPointId && typeof s.pickupPointLatitude === 'number' && typeof s.pickupPointLongitude === 'number')
    .reduce<{ id: number; name: string; address: string; latitude: number; longitude: number }[]>((acc, s) => {
      if (!acc.find((p) => p.id === s.pickupPointId)) {
        acc.push({ id: s.pickupPointId!, name: s.pickupPointName || 'Pickup', address: s.pickupPointAddress || '', latitude: s.pickupPointLatitude!, longitude: s.pickupPointLongitude! });
      }
      return acc;
    }, []);

  const uniqueDropoffPoints = students
    .filter((s) => s.dropoffPointId && typeof s.dropoffPointLatitude === 'number' && typeof s.dropoffPointLongitude === 'number')
    .reduce<{ id: number; name: string; address: string; latitude: number; longitude: number }[]>((acc, s) => {
      if (!acc.find((p) => p.id === s.dropoffPointId)) {
        acc.push({ id: s.dropoffPointId!, name: s.dropoffPointName || 'Drop-off', address: s.dropoffPointAddress || '', latitude: s.dropoffPointLatitude!, longitude: s.dropoffPointLongitude! });
      }
      return acc;
    }, []);

  return (
    <>
      <SchoolBusPageShell
        title={`Transport request #${request.id}`}
        description='Inspect the full request payload, review the student list, and take approval actions.'
        breadcrumb={
          <SchoolBusBreadcrumb items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Requests', href: '/school-bus/requests' },
            { label: `#${request.id} — ${request.parentProfileName ?? 'Request'}`, current: true },
          ]} />
        }
        actions={
          <>
            {isSubmitted && (
              <Button variant='outline' className='rounded-full' asChild>
                <Link href={`/school-bus/requests/${request.id}/edit`}>
                  <Pencil className='h-4 w-4' /> Edit
                </Link>
              </Button>
            )}
            {(isSubmitted || request.status === 'DRAFT') && (
              <Button variant='outline' className='rounded-full' onClick={handleCancel}>Cancel</Button>
            )}
            {isSubmitted && (
              <Button variant='outline' className='rounded-full' onClick={() => setRejectOpen(true)}>
                <XCircle className='h-4 w-4' /> Reject
              </Button>
            )}
            {isSubmitted && (
              <Button
                className='rounded-full bg-[#C81E3A] text-white hover:bg-[#B31B34]'
                disabled={!allReady}
                onClick={handleApprove}
              >
                <CheckCircle2 className='h-4 w-4' />
                {approving ? 'Approving…' : 'Approve'}
              </Button>
            )}
          </>
        }
      >
        {/* ── Status subtitle row */}
        <div className='flex flex-wrap items-center gap-2'>
          <SchoolBusStatusBadge status={request.status} />
          <span className='text-slate-300'>·</span>
          <span className='text-sm text-slate-600'>{REQUEST_TYPE_LABELS[request.requestType] ?? request.requestType}</span>
          <span className='text-slate-300'>·</span>
          <span className='text-sm text-slate-500'>
            Effective {formatDate(request.effectiveFrom)}{request.effectiveTo ? ` – ${formatDate(request.effectiveTo)}` : ''}
          </span>
        </div>

        {/* ── Approval Readiness Banner */}
        {isSubmitted && (
          <div className={cn(
            'flex items-start gap-3 rounded-2xl border p-4',
            allReady
              ? 'border-emerald-200 bg-emerald-50'
              : 'border-amber-200 bg-amber-50'
          )}>
            {allReady ? (
              <CheckCircle2 className='mt-0.5 h-5 w-5 shrink-0 text-emerald-600' />
            ) : (
              <AlertTriangle className='mt-0.5 h-5 w-5 shrink-0 text-amber-600' />
            )}
            <div className='min-w-0'>
              <p className={cn('font-semibold text-sm', allReady ? 'text-emerald-800' : 'text-amber-800')}>
                {allReady ? 'Ready to approve' : `Cannot approve yet — ${blockingIssues.length} issue${blockingIssues.length > 1 ? 's' : ''} must be fixed`}
              </p>
              {allReady ? (
                <p className='text-xs text-emerald-700 mt-0.5'>All required student, schedule and pickup/drop-off fields are present.</p>
              ) : (
                <ul className='mt-1.5 space-y-0.5'>
                  {blockingIssues.map((msg, i) => (
                    <li key={i} className='flex items-center gap-1.5 text-xs text-amber-700'>
                      <AlertCircle className='h-3.5 w-3.5 shrink-0' /> {msg}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        )}

        {/* ── Summary + Students side-by-side on xl */}
        <div className='grid gap-6 xl:grid-cols-[0.85fr_1.15fr]'>
          {/* Request Summary */}
          <SchoolBusSection title='Request summary' description='Operational metadata and approval state.'>
            <div className='grid grid-cols-2 gap-x-4 gap-y-5'>
              <InfoRow label='Parent' value={request.parentProfileName} />
              <InfoRow label='School' value={request.schoolName} />
              <InfoRow label='Request type' value={
                <Pill label={REQUEST_TYPE_LABELS[request.requestType] ?? request.requestType}
                  className='bg-slate-100 text-slate-700' />
              } />
              <InfoRow label='Status' value={<SchoolBusStatusBadge status={request.status} />} />
              <InfoRow label='Effective from' value={formatDate(request.effectiveFrom)} />
              <InfoRow label='Effective to' value={request.effectiveTo ? formatDate(request.effectiveTo) : 'Open-ended'} />
              <InfoRow label='Submitted at' value={formatDateTime(request.requestedAt)} />
              <InfoRow label='Approved at' value={request.approvedAt ? formatDateTime(request.approvedAt) : '—'} />
              {request.rejectionReason && (
                <InfoRow wide label='Rejection reason' value={
                  <span className='text-red-600'>{request.rejectionReason}</span>
                } />
              )}
              {request.notes && (
                <InfoRow wide label='Notes' value={request.notes} />
              )}
            </div>
          </SchoolBusSection>

          {/* Requested Students */}
          <SchoolBusSection title='Requested students' description='Student details, schedule, pickup/drop-off and readiness.'>
            <div className='space-y-3'>
              {students.map((s, i) => {
                const r = rowReadiness[i];
                const opt = (s.tripOption || '').toUpperCase();
                const needsPickup = opt === 'MORNING' || opt === 'ROUND_TRIP';
                const needsDropoff = opt === 'AFTERNOON' || opt === 'ROUND_TRIP';
                return (
                  <div key={s.id} className='rounded-2xl border border-slate-200 bg-slate-50/50 p-4 space-y-3'>
                    {/* Row header */}
                    <div className='flex items-start justify-between gap-2'>
                      <div className='min-w-0'>
                        <p className='font-semibold text-slate-900 truncate'>{s.studentName}</p>
                        {s.studentCode && <p className='text-xs text-slate-400'>{s.studentCode}</p>}
                      </div>
                      {/* Readiness badge */}
                      <span className={cn(
                        'shrink-0 inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium',
                        r.state === 'ready'
                          ? 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200'
                          : 'bg-amber-50 text-amber-700 ring-1 ring-amber-200'
                      )}>
                        {r.state === 'ready' ? <CheckCircle2 className='h-3 w-3' /> : <AlertTriangle className='h-3 w-3' />}
                        {r.label}
                      </span>
                    </div>

                    {/* Detail grid */}
                    <div className='grid grid-cols-2 gap-x-4 gap-y-3 text-xs'>
                      {/* Trip option */}
                      <div className='col-span-1'>
                        <p className='text-[10px] font-bold uppercase tracking-wide text-slate-400 mb-0.5'>Trip option</p>
                        {s.tripOption
                          ? <Pill label={TRIP_OPTION_LABELS[opt] ?? s.tripOption} className='bg-blue-50 text-blue-700' />
                          : <span className='text-amber-600 font-medium'>Not set</span>}
                      </div>

                      {/* Schedule */}
                      <div className='col-span-1'>
                        <p className='text-[10px] font-bold uppercase tracking-wide text-slate-400 mb-0.5'>Schedule</p>
                        {s.schoolScheduleName ? (
                          <div>
                            <p className='font-medium text-slate-800 truncate'>{s.schoolScheduleName}</p>
                            {(s.arrivalDeadline || s.departureTime) && (
                              <p className='text-slate-500'>
                                {s.departureTime ?? '—'} → {s.arrivalDeadline ?? '—'}
                              </p>
                            )}
                          </div>
                        ) : (
                          <span className='text-amber-600 font-medium'>Not set</span>
                        )}
                      </div>

                      {/* Pickup */}
                      {(needsPickup || !s.tripOption) && (
                        <div className='col-span-1'>
                          <p className='text-[10px] font-bold uppercase tracking-wide text-slate-400 mb-0.5'>Pickup</p>
                          {s.pickupPointName
                            ? <p className='font-medium text-slate-800 truncate'>{s.pickupPointName}</p>
                            : <span className={cn('font-medium', needsPickup ? 'text-red-500' : 'text-slate-400')}>
                                {needsPickup ? 'Required — not set' : 'Not required'}
                              </span>
                          }
                        </div>
                      )}

                      {/* Drop-off */}
                      {(needsDropoff || !s.tripOption) && (
                        <div className='col-span-1'>
                          <p className='text-[10px] font-bold uppercase tracking-wide text-slate-400 mb-0.5'>Drop-off</p>
                          {s.dropoffPointName
                            ? <p className='font-medium text-slate-800 truncate'>{s.dropoffPointName}</p>
                            : <span className={cn('font-medium', needsDropoff ? 'text-red-500' : 'text-slate-400')}>
                                {needsDropoff ? 'Required — not set' : 'Not required'}
                              </span>
                          }
                        </div>
                      )}

                      {/* Days */}
                      <div className='col-span-2'>
                        <p className='text-[10px] font-bold uppercase tracking-wide text-slate-400 mb-1'>Days</p>
                        <DayBadges student={s} />
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </SchoolBusSection>
        </div>

        {/* ── Request Map */}
        <SchoolBusSection title='Request map' description='School hub and all pickup/drop-off points included in this request.'>
          <SchoolBusMapWorkspace
            defaultPreset='map-focus'
            map={
              <OperationsMap
                schools={[{
                  id: request.schoolId,
                  name: request.schoolName,
                  latitude: request.schoolLatitude,
                  longitude: request.schoolLongitude,
                  address: undefined,
                }]}
                pickupPoints={[
                  ...uniquePickupPoints.map((p) => ({ ...p, schoolId: request.schoolId, schoolName: request.schoolName })),
                  ...uniqueDropoffPoints.map((p) => ({ ...p, schoolId: request.schoolId, schoolName: request.schoolName })),
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
                  <p className='text-sm font-semibold text-slate-900'>Route context</p>
                  <p className='text-xs text-slate-500 mt-0.5'>{REQUEST_TYPE_LABELS[request.requestType] ?? request.requestType}</p>
                </div>
                <div className='grid grid-cols-3 gap-2 text-center'>
                  {[
                    { label: 'Students', value: students.length },
                    { label: 'Pickup pts', value: uniquePickupPoints.length },
                    { label: 'Drop-off pts', value: uniqueDropoffPoints.length },
                  ].map(({ label, value }) => (
                    <div key={label} className='rounded-xl bg-slate-50 border border-slate-100 p-2'>
                      <p className='text-lg font-bold text-slate-900'>{value}</p>
                      <p className='text-[10px] text-slate-400'>{label}</p>
                    </div>
                  ))}
                </div>
                <div className='space-y-2 max-h-[280px] overflow-auto pr-1'>
                  {students.map((s) => (
                    <div key={s.id} className='rounded-xl border border-slate-200 bg-white p-3'>
                      <p className='text-xs font-semibold text-slate-900 truncate'>{s.studentName}</p>
                      {s.pickupPointId && (
                        <p className='flex items-center gap-1 mt-1 text-[11px] text-slate-500 truncate'>
                          <MapPin className='h-3 w-3 shrink-0 text-[#C81E3A]' />
                          {s.pickupPointName}
                        </p>
                      )}
                      {s.dropoffPointId && s.dropoffPointId !== s.pickupPointId && (
                        <p className='flex items-center gap-1 mt-0.5 text-[11px] text-slate-500 truncate'>
                          <MapPin className='h-3 w-3 shrink-0 text-blue-500' />
                          {s.dropoffPointName}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            }
          />
        </SchoolBusSection>

        {/* ── History */}
        <SchoolBusSection title='Request history' description='All status transitions and notes for this request.'>
          <SchoolBusTimeline
            events={mapRequestHistoryToTimeline(historyData?.data ?? [])}
            mode='compact'
            isLoading={historyLoading}
            isError={historyError}
            maxHeight='400px'
          />
        </SchoolBusSection>
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
