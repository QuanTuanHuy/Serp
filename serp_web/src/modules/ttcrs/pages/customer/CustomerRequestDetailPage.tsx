'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  CheckCircle2,
  ExternalLink,
  ImageIcon,
  Loader2,
  MapPin,
  Navigation,
} from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetCustomerRequestDetailQuery,
  useGetTransportPlanDetailQuery,
} from '../../api/ttcrsApi';
import type {
  RequestStatus,
  RequestType,
  TransportPlanStopDetail,
} from '../../types';

// -------------------------------------------------------------------------
// Constants / helpers
// -------------------------------------------------------------------------

const STATUS_CLASS: Record<RequestStatus, string> = {
  PENDING: 'bg-amber-100 text-amber-700 border-amber-200',
  PLANNED: 'bg-blue-100 text-blue-700 border-blue-200',
  IN_PROGRESS: 'bg-indigo-100 text-indigo-700 border-indigo-200',
  COMPLETED: 'bg-green-100 text-green-700 border-green-200',
  CANCELLED: 'bg-red-100 text-red-700 border-red-200',
};

const TYPE_CLASS: Record<RequestType, string> = {
  OF: 'bg-green-500 text-white border-transparent',
  IF: 'bg-blue-500 text-white border-transparent',
  OE: 'bg-amber-500 text-white border-transparent',
  IE: 'bg-purple-500 text-white border-transparent',
};

function formatId(id: number) {
  return `REQ-${String(id).padStart(5, '0')}`;
}

function formatDatetime(val: string | null | undefined) {
  if (!val) return '—';
  try {
    return new Date(val).toLocaleString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  } catch {
    return val;
  }
}

// -------------------------------------------------------------------------
// Evidence viewer
// -------------------------------------------------------------------------

function EvidenceImage({ url, label }: { url: string; label: string }) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="flex items-center gap-2 rounded border border-border bg-muted/40 px-3 py-2 text-sm hover:bg-muted transition-colors"
      >
        <ImageIcon className="h-4 w-4 text-muted-foreground" />
        <span>{label}</span>
      </button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-2xl p-0 gap-0">
          <DialogHeader className="px-6 pt-4 pb-3">
            <DialogTitle className="flex items-center justify-between">
              <span>{label}</span>
              <a
                href={url}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-1 text-xs font-normal text-muted-foreground hover:text-foreground"
              >
                Open original <ExternalLink className="h-3 w-3" />
              </a>
            </DialogTitle>
          </DialogHeader>
          <div className="px-6 pb-6">
            <img
              src={url}
              alt={label}
              className="w-full max-h-[60vh] object-contain rounded-md border border-border"
            />
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}

// -------------------------------------------------------------------------
// Sub-status derivation
// -------------------------------------------------------------------------

type InProgressSubStatus =
  | 'travelling-to-src'
  | 'at-src'
  | 'travelling-to-dest'
  | 'at-dest'
  | 'unknown';

function deriveSubStatus(
  requestId: number,
  stops: TransportPlanStopDetail[],
): InProgressSubStatus {
  const reqStops = stops
    .filter((s) => s.requestId === requestId)
    .sort((a, b) => a.sequence - b.sequence);
  if (reqStops.length === 0) return 'unknown';
  const srcStop = reqStops[0];
  const destStop = reqStops[1] ?? null;
  if (!srcStop.actualArrivalTime) return 'travelling-to-src';
  if (!srcStop.isCompleted) return 'at-src';
  if (!destStop || !destStop.actualArrivalTime) return 'travelling-to-dest';
  if (!destStop.isCompleted) return 'at-dest';
  return 'travelling-to-dest';
}

function subStatusLabel(status: InProgressSubStatus, src: string, dest: string): string {
  switch (status) {
    case 'travelling-to-src':  return `Travelling to ${src}`;
    case 'at-src':             return `At ${src}`;
    case 'travelling-to-dest': return `Travelling to ${dest}`;
    case 'at-dest':            return `At ${dest}`;
    default:                   return 'In progress';
  }
}

const SUB_STATUS_COLOR: Record<InProgressSubStatus, string> = {
  'travelling-to-src':  'text-blue-600',
  'at-src':             'text-orange-600',
  'travelling-to-dest': 'text-blue-600',
  'at-dest':            'text-orange-600',
  unknown:              'text-muted-foreground',
};

// -------------------------------------------------------------------------
// Read-only field
// -------------------------------------------------------------------------

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <p className="text-xs text-muted-foreground uppercase tracking-wider mb-0.5">{label}</p>
      <p className="text-sm">{value ?? '—'}</p>
    </div>
  );
}

// -------------------------------------------------------------------------
// Main page
// -------------------------------------------------------------------------

interface Props {
  requestId: number;
}

export function CustomerRequestDetailPage({ requestId }: Props) {
  const router = useRouter();

  const { data: reqData, isLoading, isError } =
    useGetCustomerRequestDetailQuery(requestId);

  const request = reqData?.data ?? null;

  const planId = request?.transportPlanId ?? null;
  const { data: planData } = useGetTransportPlanDetailQuery(planId!, {
    skip: planId == null || request?.status !== 'IN_PROGRESS',
  });
  const planStops = planData?.data?.stops ?? [];

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center gap-2 text-muted-foreground">
        <Loader2 className="h-5 w-5 animate-spin" />
        <span className="text-sm">Loading request…</span>
      </div>
    );
  }

  if (isError || !request) {
    return (
      <div className="flex h-64 flex-col items-center justify-center gap-3 text-muted-foreground">
        <p className="text-sm">Request not found.</p>
        <Button variant="outline" size="sm" onClick={() => router.back()}>
          <ArrowLeft className="mr-1 h-4 w-4" /> Go back
        </Button>
      </div>
    );
  }

  const isInProgress = request.status === 'IN_PROGRESS';
  const isCompleted = request.status === 'COMPLETED';
  const isCancelled = request.status === 'CANCELLED';

  const subStatus: InProgressSubStatus = isInProgress
    ? deriveSubStatus(request.id, planStops)
    : 'unknown';

  const timeWindows = [
    { label: 'Early at Origin', value: request.earlyAtSrc },
    { label: 'Late at Origin', value: request.lateAtSrc },
    { label: 'Early at Dest', value: request.earlyAtDest },
    { label: 'Late at Dest', value: request.lateAtDest },
  ].filter((tw) => tw.value != null);

  const containerSizeLabel: Record<string, string> = {
    TWENTY_FEET: '20 ft',
    FORTY_FEET: '40 ft',
    FORTY_FIVE_FEET: '45 ft',
  };

  return (
    <div className="space-y-6 px-4 py-6">
      {/* Back + header */}
      <div className="flex items-start gap-4">
        <Button variant="ghost" size="icon" className="mt-0.5 shrink-0" onClick={() => router.back()}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">{formatId(request.id)}</h1>
          <div className="flex items-center gap-2">
            <Badge className={cn('rounded px-2 py-0.5 text-xs font-bold', TYPE_CLASS[request.type])}>
              {request.type}
            </Badge>
            <Badge
              className={cn(
                'rounded-full px-2.5 py-0.5 text-xs font-medium',
                STATUS_CLASS[request.status],
              )}
            >
              {request.status.replace('_', ' ')}
            </Badge>
          </div>
        </div>
      </div>

      {/* Cancellation reason */}
      {isCancelled && request.reason && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          <span className="font-semibold">Cancellation reason: </span>
          {request.reason}
        </div>
      )}

      {/* IN_PROGRESS sub-status */}
      {isInProgress && (
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              {subStatus.startsWith('travelling') ? (
                <Navigation className={cn('h-5 w-5', SUB_STATUS_COLOR[subStatus])} />
              ) : (
                <CheckCircle2 className={cn('h-5 w-5', SUB_STATUS_COLOR[subStatus])} />
              )}
              <div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider">
                  Execution status
                </p>
                <p className={cn('text-sm font-semibold', SUB_STATUS_COLOR[subStatus])}>
                  {subStatusLabel(
                    subStatus,
                    request.srcLocationCode ?? 'Origin',
                    request.destLocationCode
                  )}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Main info */}
      <Card>
        <CardContent className="p-6 space-y-5">
          {/* Locations */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground uppercase tracking-wider">Origin</p>
              <div className="flex items-center gap-1.5 text-sm font-medium">
                <MapPin className="h-3.5 w-3.5 text-muted-foreground" />
                  {request.srcLocationCode ?? '—'}
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground uppercase tracking-wider">Destination</p>
              <div className="flex items-center gap-1.5 text-sm font-medium">
                <MapPin className="h-3.5 w-3.5 text-muted-foreground" />
                {request.destLocationCode}
              </div>
            </div>
          </div>

          {/* Cargo info */}
          <div className="grid grid-cols-3 gap-4">
            <Field
              label="Weight"
              value={request.weight != null ? `${request.weight} kg` : null}
            />
            <Field
              label="Container Size"
              value={
                request.containerSize
                  ? (containerSizeLabel[request.containerSize] ?? request.containerSize)
                  : null
              }
            />
            <Field
              label="Drop Trailer"
              value={request.dropTrailerRequired ? 'Required' : 'Not required'}
            />
          </div>

          {/* Time windows */}
          {timeWindows.length > 0 && (
            <>
              <Separator />
              <div className="grid grid-cols-2 gap-3">
                {timeWindows.map(({ label, value }) => (
                  <Field key={label} label={label} value={formatDatetime(value)} />
                ))}
              </div>
            </>
          )}

          {/* Note */}
          {request.reason && !isCancelled && (
            <>
              <Separator />
              <Field label="Note" value={request.reason} />
            </>
          )}

          <Separator />

          {/* Meta */}
          <div className="grid grid-cols-2 gap-3">
            <Field label="Created at" value={formatDatetime(request.createdAt)} />
            <Field label="Last updated" value={formatDatetime(request.lastUpdatedStamp)} />
            {request.transportPlanId && (
              <Field label="Transport Plan" value={`#${request.transportPlanId}`} />
            )}
          </div>
        </CardContent>
      </Card>

      {/* Evidence */}
      {(isInProgress || isCompleted) &&
        (request.evidenceAtSrc || request.evidenceAtDest) && (
          <Card>
            <CardContent className="p-6 space-y-3">
              <p className="text-sm font-semibold">Evidence</p>
              <div className="flex flex-wrap gap-3">
                {request.evidenceAtSrc && (
                  <EvidenceImage url={request.evidenceAtSrc} label="Evidence at source" />
                )}
                {request.evidenceAtDest && (
                  <EvidenceImage url={request.evidenceAtDest} label="Evidence at destination" />
                )}
              </div>
            </CardContent>
          </Card>
        )}
    </div>
  );
}
