'use client';

import React, { useState, useEffect } from 'react';
import { Loader2, Ban } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api/utils';
import {
  Button,
  Input,
  Label,
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Badge,
  Separator,
  Textarea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetDispatcherLocationsQuery,
  useUpdateDispatcherRequestMutation,
  useUpdateDispatcherRequestsStatusMutation,
} from '../api/ttcrsApi';
import type {
  TtcrsRequest,
  RequestStatus,
  RequestType,
  ContainerSize,
  UpdateRequestPayload,
} from '../types';

// -------------------------------------------------------------------------
// Constants
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

// -------------------------------------------------------------------------
// Props
// -------------------------------------------------------------------------

interface RequestDetailSheetProps {
  request: TtcrsRequest | null;
  open: boolean;
  onClose: () => void;
  customerName?: string;
}

// -------------------------------------------------------------------------
// Component
// -------------------------------------------------------------------------

export function RequestDetailSheet({
  request,
  open,
  onClose,
  customerName,
}: RequestDetailSheetProps) {
  const isPending = request?.status === 'PENDING';

  // Form state
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

  const { data: locationsData } = useGetDispatcherLocationsQuery();
  const locationOptions = locationsData?.data ?? [];

  const [updateRequest, { isLoading: isSaving }] =
    useUpdateDispatcherRequestMutation();
  const [updateStatus, { isLoading: isCancelling }] =
    useUpdateDispatcherRequestsStatusMutation();

  // Sync form when request changes
  useEffect(() => {
    if (!request) return;
    setSrcLocationCode(request.srcLocationCode ?? '');
    setDestLocationCode(request.destLocationCode ?? '');
    setWeight(request.weight != null ? String(request.weight) : '');
    setContainerSize((request.containerSize as ContainerSize | null) ?? '');
    setDropTrailerRequired(request.dropTrailerRequired ?? false);
    setReason(request.reason ?? '');
    // Convert ISO string to datetime-local format (YYYY-MM-DDTHH:mm)
    const toLocal = (s: string | null) => (s ? s.slice(0, 16) : '');
    setEarlyAtSrc(toLocal(request.earlyAtSrc));
    setLateAtSrc(toLocal(request.lateAtSrc));
    setEarlyAtDest(toLocal(request.earlyAtDest));
    setLateAtDest(toLocal(request.lateAtDest));
  }, [request]);

  if (!request) return null;

  // ── Handlers ─────────────────────────────────────────────────────────────

  async function handleSave() {
    if (!request) return;
    const payload: UpdateRequestPayload = {};
    if (srcLocationCode.trim())
      payload.srcLocationCode = srcLocationCode.trim();
    if (destLocationCode.trim())
      payload.destLocationCode = destLocationCode.trim();
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
      toast.success('Request updated successfully');
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to update request');
    }
  }

  async function handleCancel() {
    if (!request) return;
    try {
      await updateStatus({
        requestIds: [request.id],
        status: 'CANCELLED',
      }).unwrap();
      toast.success(`${formatId(request.id)} has been cancelled`);
      setCancelOpen(false);
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to cancel request');
    }
  }

  // ── Render ───────────────────────────────────────────────────────────────

  return (
    <>
      <Sheet
        open={open}
        onOpenChange={(o) => {
          if (!o) onClose();
        }}
      >
        <SheetContent className='w-full sm:max-w-[540px] overflow-y-auto flex flex-col gap-0 p-0'>
          {/* Header */}
          <SheetHeader className='px-6 py-4 border-b'>
            <div className='flex flex-col gap-1 pr-8'>
              <SheetTitle className='text-lg font-semibold'>
                {formatId(request.id)}
              </SheetTitle>
              <div className='flex items-center gap-2'>
                <Badge
                  className={cn(
                    'rounded px-2 py-0.5 text-xs font-bold',
                    TYPE_CLASS[request.type]
                  )}
                >
                  {request.type}
                </Badge>
                <Badge
                  className={cn(
                    'rounded-full px-2.5 py-0.5 text-xs font-medium',
                    STATUS_CLASS[request.status]
                  )}
                >
                  {request.status.replace('_', ' ')}
                </Badge>
              </div>
            </div>
          </SheetHeader>

          {/* Body */}
          <div className='flex-1 overflow-y-auto px-6 py-5 space-y-5'>
            {/* Customer */}
            <div className='space-y-1'>
              <Label className='text-xs text-muted-foreground uppercase tracking-wider'>
                Customer
              </Label>
              <p className='text-sm text-foreground'>
                {customerName ||
                  (request.customerId
                    ? `Customer #${request.customerId}`
                    : '—')}
              </p>
            </div>

            <Separator />

            {/* Locations */}
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-1.5'>
                <Label>Origin</Label>
                {isPending ? (
                  <Select
                    value={srcLocationCode}
                    onValueChange={setSrcLocationCode}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select origin' />
                    </SelectTrigger>
                    <SelectContent>
                      {locationOptions.map((loc) => (
                        <SelectItem
                          key={loc.locationCode}
                          value={loc.locationCode}
                        >
                          {loc.locationCode}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                ) : (
                  <Input
                    value={srcLocationCode}
                    readOnly
                    className='bg-muted/40 text-muted-foreground'
                  />
                )}
              </div>
              <div className='space-y-1.5'>
                <Label>Destination</Label>
                {isPending ? (
                  <Select
                    value={destLocationCode}
                    onValueChange={setDestLocationCode}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select destination' />
                    </SelectTrigger>
                    <SelectContent>
                      {locationOptions.map((loc) => (
                        <SelectItem
                          key={loc.locationCode}
                          value={loc.locationCode}
                        >
                          {loc.locationCode}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                ) : (
                  <Input
                    value={destLocationCode}
                    readOnly
                    className='bg-muted/40 text-muted-foreground'
                  />
                )}
              </div>
            </div>

            {/* Weight + Container Size */}
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-1.5'>
                <Label htmlFor='req-weight'>Weight (kg)</Label>
                <Input
                  id='req-weight'
                  type='number'
                  value={weight}
                  onChange={(e) => setWeight(e.target.value)}
                  disabled={!isPending}
                  placeholder='e.g. 1500'
                />
              </div>
              <div className='space-y-1.5'>
                <Label>Container Size</Label>
                <Select
                  value={containerSize}
                  onValueChange={(v) => setContainerSize(v as ContainerSize)}
                  disabled={!isPending}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select size' />
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
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-1.5'>
                <Label htmlFor='req-early-src'>Early at Origin</Label>
                <Input
                  id='req-early-src'
                  type='datetime-local'
                  value={earlyAtSrc}
                  onChange={(e) => setEarlyAtSrc(e.target.value)}
                  disabled={!isPending}
                />
              </div>
              <div className='space-y-1.5'>
                <Label htmlFor='req-late-src'>Late at Origin</Label>
                <Input
                  id='req-late-src'
                  type='datetime-local'
                  value={lateAtSrc}
                  onChange={(e) => setLateAtSrc(e.target.value)}
                  disabled={!isPending}
                />
              </div>
              <div className='space-y-1.5'>
                <Label htmlFor='req-early-dest'>Early at Dest</Label>
                <Input
                  id='req-early-dest'
                  type='datetime-local'
                  value={earlyAtDest}
                  onChange={(e) => setEarlyAtDest(e.target.value)}
                  disabled={!isPending}
                />
              </div>
              <div className='space-y-1.5'>
                <Label htmlFor='req-late-dest'>Late at Dest</Label>
                <Input
                  id='req-late-dest'
                  type='datetime-local'
                  value={lateAtDest}
                  onChange={(e) => setLateAtDest(e.target.value)}
                  disabled={!isPending}
                />
              </div>
            </div>

            {/* Drop trailer */}
            <div className='flex items-center gap-3'>
              <input
                id='req-drop-trailer'
                type='checkbox'
                checked={dropTrailerRequired}
                onChange={(e) => setDropTrailerRequired(e.target.checked)}
                disabled={!isPending}
                className='h-4 w-4 rounded border-border'
              />
              <Label htmlFor='req-drop-trailer' className='cursor-pointer'>
                Drop trailer required
              </Label>
            </div>

            {/* Reason / note */}
            <div className='space-y-1.5'>
              <Label htmlFor='req-reason'>Reason / Note</Label>
              <Textarea
                id='req-reason'
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                disabled={!isPending}
                rows={3}
                placeholder='Optional reason or note'
              />
            </div>

            {!isPending && (
              <div className='rounded-md bg-muted/50 px-4 py-3 text-xs text-muted-foreground'>
                This request is{' '}
                <strong>{request.status.replace('_', ' ')}</strong> and cannot
                be edited.
              </div>
            )}

            {/* Read-only meta */}
            <Separator />
            <div className='grid grid-cols-2 gap-3 text-sm'>
              <div>
                <p className='text-xs text-muted-foreground uppercase tracking-wider mb-0.5'>
                  Created at
                </p>
                <p>{formatDatetime(request.createdAt)}</p>
              </div>
              <div>
                <p className='text-xs text-muted-foreground uppercase tracking-wider mb-0.5'>
                  Last updated
                </p>
                <p>{formatDatetime(request.lastUpdatedStamp)}</p>
              </div>
              {request.transportPlanId && (
                <div>
                  <p className='text-xs text-muted-foreground uppercase tracking-wider mb-0.5'>
                    Transport Plan
                  </p>
                  <p>#{request.transportPlanId}</p>
                </div>
              )}
            </div>
          </div>

          {/* Footer actions */}
          {isPending && (
            <div className='border-t px-6 py-4 flex items-center justify-between gap-3'>
              <Button
                variant='outline'
                size='sm'
                className='border-red-300 text-red-600 hover:bg-red-50 hover:text-red-700'
                onClick={() => setCancelOpen(true)}
                disabled={isCancelling || isSaving}
              >
                <Ban className='h-4 w-4' />
                Cancel Request
              </Button>

              <div className='flex items-center gap-2'>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={onClose}
                  disabled={isSaving}
                >
                  Close
                </Button>
                <Button size='sm' onClick={handleSave} disabled={isSaving}>
                  {isSaving && <Loader2 className='h-4 w-4 animate-spin' />}
                  Save Changes
                </Button>
              </div>
            </div>
          )}
        </SheetContent>
      </Sheet>

      {/* Cancel Confirmation */}
      <AlertDialog open={cancelOpen} onOpenChange={setCancelOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel this request?</AlertDialogTitle>
            <AlertDialogDescription>
              {formatId(request.id)} will be marked as{' '}
              <strong>Cancelled</strong>. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isCancelling}>
              Go back
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCancel}
              disabled={isCancelling}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isCancelling && <Loader2 className='h-4 w-4 animate-spin' />}
              Yes, cancel it
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
