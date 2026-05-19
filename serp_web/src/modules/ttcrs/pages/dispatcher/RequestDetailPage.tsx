'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  Ban,
  CheckCircle2,
  ExternalLink,
  ImageIcon,
  Loader2,
  MapPin,
  Navigation,
  RotateCcw,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api/utils';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Badge,
  Button,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Separator,
  Textarea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetDispatcherRequestDetailQuery,
  useGetDispatcherLocationsQuery,
  useUpdateDispatcherRequestMutation,
  useUpdateDispatcherRequestsStatusMutation,
  useGetTransportPlanDetailQuery,
} from '../../api/ttcrsApi';
import type {
  ContainerSize,
  RequestStatus,
  RequestType,
  TransportPlanStopDetail,
  UpdateRequestPayload,
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

const CONTAINER_SIZES: { value: ContainerSize; label: string }[] = [
  { value: 'TWENTY_FEET', label: '20 ft' },
  { value: 'FORTY_FEET', label: '40 ft' },
];

function formatId(id: number) {
  return `REQ-${String(id).padStart(5, '0')}`;
}

function formatDatetime(val: string | null | undefined) {
  if (!val) return '—';
  try {
    return new Date(val).toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return val;
  }
}

function toLocalInput(s: string | null) {
  return s ? s.slice(0, 16) : '';
}

// -------------------------------------------------------------------------
// Sub-components
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

function subStatusLabel(
  status: InProgressSubStatus,
  src: string,
  dest: string,
): string {
  switch (status) {
    case 'travelling-to-src': return `Travelling to ${src}`;
    case 'at-src':            return `At ${src}`;
    case 'travelling-to-dest': return `Travelling to ${dest}`;
    case 'at-dest':            return `At ${dest}`;
    default:                   return 'In progress';
  }
}

const SUB_STATUS_COLOR: Record<InProgressSubStatus, string> = {
  'travelling-to-src': 'text-blue-600',
  'at-src': 'text-orange-600',
  'travelling-to-dest': 'text-blue-600',
  'at-dest': 'text-orange-600',
  unknown: 'text-muted-foreground',
};

// -------------------------------------------------------------------------
// Main page
// -------------------------------------------------------------------------

interface Props {
  requestId: number;
}

export function RequestDetailPage({ requestId }: Props) {
  const router = useRouter();

  const {
    data: reqData,
    isLoading,
    isError,
    refetch,
  } = useGetDispatcherRequestDetailQuery(requestId);

  const request = reqData?.data ?? null;

  const planId = request?.transportPlanId ?? null;
  const { data: planData } = useGetTransportPlanDetailQuery(planId!, {
    skip: planId == null || request?.status !== 'IN_PROGRESS',
  });
  const planStops = planData?.data?.stops ?? [];

  const { data: locationsData } = useGetDispatcherLocationsQuery();
  const locationOptions = locationsData?.data ?? [];

  // Editable form state (PENDING only)
  const [srcLocationCode, setSrcLocationCode] = useState('');
  const [destLocationCode, setDestLocationCode] = useState('');
  const [weight, setWeight] = useState('');
  const [containerSize, setContainerSize] = useState<ContainerSize | ''>('');
  const [dropTrailerRequired, setDropTrailerRequired] = useState(false);
  const [reason, setReason] = useState('');
  const [earlyAtSrc, setEarlyAtSrc] = useState('');
  const [lateAtSrc, setLateAtSrc] = useState('');
  const [earlyAtDest, setEarlyAtDest] = useState('');
  const [lateAtDest, setLateAtDest] = useState('');

  const [cancelOpen, setCancelOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [restoreOpen, setRestoreOpen] = useState(false);

  const [updateRequest, { isLoading: isSaving }] = useUpdateDispatcherRequestMutation();
  const [updateStatus, { isLoading: isStatusUpdating }] =
    useUpdateDispatcherRequestsStatusMutation();

  useEffect(() => {
    if (!request) return;
    setSrcLocationCode(request.srcLocationCode ?? '');
    setDestLocationCode(request.destLocationCode ?? '');
    setWeight(request.weight != null ? String(request.weight) : '');
    setContainerSize((request.containerSize as ContainerSize | null) ?? '');
    setDropTrailerRequired(request.dropTrailerRequired ?? false);
    setReason(request.reason ?? '');
    setEarlyAtSrc(toLocalInput(request.earlyAtSrc));
    setLateAtSrc(toLocalInput(request.lateAtSrc));
    setEarlyAtDest(toLocalInput(request.earlyAtDest));
    setLateAtDest(toLocalInput(request.lateAtDest));
  }, [request]);

  async function handleSave() {
    if (!request) return;
    const payload: UpdateRequestPayload = {};
    if (srcLocationCode.trim()) payload.srcLocationCode = srcLocationCode.trim();
    if (destLocationCode.trim()) payload.destLocationCode = destLocationCode.trim();
    if (weight !== '') payload.weight = Number(weight);
    if (containerSize) payload.containerSize = containerSize;
    payload.dropTrailerRequired = dropTrailerRequired;
    if (reason.trim()) payload.reason = reason.trim();
    if (earlyAtSrc) payload.earlyAtSrc = earlyAtSrc + ':00';
    if (lateAtSrc) payload.lateAtSrc = lateAtSrc + ':00';
    if (earlyAtDest) payload.earlyAtDest = earlyAtDest + ':00';
    if (lateAtDest) payload.lateAtDest = lateAtDest + ':00';
    try {
      await updateRequest({ id: request.id, body: payload }).unwrap();
      toast.success('Request updated');
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to update request');
    }
  }

  async function handleCancel() {
    if (!request) return;
    try {
      await updateRequest({ id: request.id, body: { reason: cancelReason.trim() } }).unwrap();
      await updateStatus({ requestIds: [request.id], status: 'CANCELLED' }).unwrap();
      toast.success(`${formatId(request.id)} cancelled`);
      setCancelOpen(false);
      setCancelReason('');
      refetch();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to cancel request');
    }
  }

  async function handleRestore() {
    if (!request) return;
    try {
      await updateStatus({ requestIds: [request.id], status: 'PENDING' }).unwrap();
      toast.success(`${formatId(request.id)} restored to PENDING`);
      setRestoreOpen(false);
      refetch();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to restore request');
    }
  }

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

  const isPending = request.status === 'PENDING';
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

  return (
    <>
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

        {/* Cancellation reason banner */}
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
                    {subStatusLabel(subStatus, request.srcLocationCode, request.destLocationCode)}
                  </p>
                </div>
                {planId && (
                  <Button
                    variant="outline"
                    size="sm"
                    className="ml-auto text-xs"
                    onClick={() => router.push(`/ttcrs/dispatcher/routes/${planId}`)}
                  >
                    View transport plan #{planId}
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Main info card */}
        <Card>
          <CardContent className="p-6 space-y-5">
            {/* Customer */}
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground uppercase tracking-wider">Customer</p>
              <p className="text-sm">
                {request.customerId ? `Customer #${request.customerId}` : '—'}
              </p>
            </div>

            <Separator />

            {/* Locations */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Origin</Label>
                {isPending ? (
                  <Select value={srcLocationCode} onValueChange={setSrcLocationCode}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select origin" />
                    </SelectTrigger>
                    <SelectContent>
                      {locationOptions.map((loc) => (
                        <SelectItem key={loc.locationCode} value={loc.locationCode}>
                          {loc.locationCode}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                ) : (
                  <div className="flex items-center gap-1.5 rounded-md bg-muted/40 px-3 py-2 text-sm">
                    <MapPin className="h-3.5 w-3.5 text-muted-foreground" />
                    {request.srcLocationCode}
                  </div>
                )}
              </div>
              <div className="space-y-1.5">
                <Label>Destination</Label>
                {isPending ? (
                  <Select value={destLocationCode} onValueChange={setDestLocationCode}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select destination" />
                    </SelectTrigger>
                    <SelectContent>
                      {locationOptions.map((loc) => (
                        <SelectItem key={loc.locationCode} value={loc.locationCode}>
                          {loc.locationCode}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                ) : (
                  <div className="flex items-center gap-1.5 rounded-md bg-muted/40 px-3 py-2 text-sm">
                    <MapPin className="h-3.5 w-3.5 text-muted-foreground" />
                    {request.destLocationCode}
                  </div>
                )}
              </div>
            </div>

            {/* Weight + Container Size */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="rd-weight">Weight (kg)</Label>
                <Input
                  id="rd-weight"
                  type="number"
                  value={weight}
                  onChange={(e) => setWeight(e.target.value)}
                  disabled={!isPending}
                  placeholder="e.g. 1500"
                  className={!isPending ? 'bg-muted/40' : ''}
                />
              </div>
              <div className="space-y-1.5">
                <Label>Container Size</Label>
                <Select
                  value={containerSize}
                  onValueChange={(v) => setContainerSize(v as ContainerSize)}
                  disabled={!isPending}
                >
                  <SelectTrigger className={!isPending ? 'bg-muted/40' : ''}>
                    <SelectValue placeholder="Select size" />
                  </SelectTrigger>
                  <SelectContent>
                    {CONTAINER_SIZES.map((s) => (
                      <SelectItem key={s.value} value={s.value}>
                        {s.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Time windows */}
            {isPending ? (
              <div className="grid grid-cols-2 gap-4">
                {[
                  { id: 'rd-early-src', label: 'Early at Origin', val: earlyAtSrc, set: setEarlyAtSrc },
                  { id: 'rd-late-src', label: 'Late at Origin', val: lateAtSrc, set: setLateAtSrc },
                  { id: 'rd-early-dest', label: 'Early at Dest', val: earlyAtDest, set: setEarlyAtDest },
                  { id: 'rd-late-dest', label: 'Late at Dest', val: lateAtDest, set: setLateAtDest },
                ].map(({ id, label, val, set }) => (
                  <div key={id} className="space-y-1.5">
                    <Label htmlFor={id}>{label}</Label>
                    <Input
                      id={id}
                      type="datetime-local"
                      value={val}
                      onChange={(e) => set(e.target.value)}
                    />
                  </div>
                ))}
              </div>
            ) : timeWindows.length > 0 ? (
              <div className="grid grid-cols-2 gap-3">
                {timeWindows.map(({ label, value }) => (
                  <div key={label}>
                    <p className="text-xs text-muted-foreground">{label}</p>
                    <p className="text-sm font-medium">{formatDatetime(value)}</p>
                  </div>
                ))}
              </div>
            ) : null}

            {/* Drop trailer */}
            <div className="flex items-center gap-3">
              <input
                id="rd-drop-trailer"
                type="checkbox"
                checked={dropTrailerRequired}
                onChange={(e) => setDropTrailerRequired(e.target.checked)}
                disabled={!isPending}
                className="h-4 w-4 rounded border-border"
              />
              <Label htmlFor="rd-drop-trailer" className={cn(!isPending && 'cursor-default')}>
                Drop trailer required
              </Label>
            </div>

            {/* Reason / note
            {(isPending || reason) && (
              <div className="space-y-1.5">
                <Label htmlFor="rd-reason">Reason / Note</Label>
                <Textarea
                  id="rd-reason"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  disabled={!isPending}
                  rows={3}
                  placeholder="Optional reason or note"
                  className={!isPending ? 'bg-muted/40' : ''}
                />
              </div>
            )} */}

            <Separator />

            {/* Meta */}
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider mb-0.5">
                  Created at
                </p>
                <p>{formatDatetime(request.createdAt)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider mb-0.5">
                  Last updated
                </p>
                <p>{formatDatetime(request.lastUpdatedStamp)}</p>
              </div>
              {request.transportPlanId && (
                <div>
                  <p className="text-xs text-muted-foreground uppercase tracking-wider mb-0.5">
                    Transport Plan
                  </p>
                  <p>#{request.transportPlanId}</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Evidence card — IN_PROGRESS or COMPLETED */}
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

        {/* Action bar */}
        {(isPending || isCancelled) && (
          <div className="flex items-center justify-between gap-3">
            {isPending && (
              <Button
                variant="outline"
                size="sm"
                className="border-red-300 text-red-600 hover:bg-red-50 hover:text-red-700"
                onClick={() => setCancelOpen(true)}
                disabled={isStatusUpdating || isSaving}
              >
                <Ban className="h-4 w-4" />
                Cancel Request
              </Button>
            )}

            {isCancelled && (
              <Button
                variant="outline"
                size="sm"
                className="border-green-300 text-green-700 hover:bg-green-50"
                onClick={() => setRestoreOpen(true)}
                disabled={isStatusUpdating}
              >
                <RotateCcw className="h-4 w-4" />
                Restore to Pending
              </Button>
            )}

            {isPending && (
              <Button size="sm" onClick={handleSave} disabled={isSaving}>
                {isSaving && <Loader2 className="h-4 w-4 animate-spin" />}
                Save Changes
              </Button>
            )}
          </div>
        )}
      </div>

      {/* Cancel dialog */}
      <AlertDialog
        open={cancelOpen}
        onOpenChange={(o) => {
          if (!o) { setCancelOpen(false); setCancelReason(''); }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel this request?</AlertDialogTitle>
            <AlertDialogDescription>
              {formatId(request.id)} will be marked as <strong>Cancelled</strong>. Please provide
              a reason.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="px-1 pb-2">
            <Textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Enter cancellation reason…"
              rows={3}
              disabled={isStatusUpdating || isSaving}
              autoFocus
            />
          </div>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isStatusUpdating || isSaving}>Go back</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCancel}
              disabled={isStatusUpdating || isSaving || !cancelReason.trim()}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {(isStatusUpdating || isSaving) && <Loader2 className="h-4 w-4 animate-spin" />}
              Yes, cancel it
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Restore dialog */}
      <AlertDialog open={restoreOpen} onOpenChange={setRestoreOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Restore to Pending?</AlertDialogTitle>
            <AlertDialogDescription>
              {formatId(request.id)} will be moved back to <strong>Pending</strong> status.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isStatusUpdating}>Go back</AlertDialogCancel>
            <AlertDialogAction onClick={handleRestore} disabled={isStatusUpdating}>
              {isStatusUpdating && <Loader2 className="h-4 w-4 animate-spin" />}
              Yes, restore
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
