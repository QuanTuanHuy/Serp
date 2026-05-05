'use client';

import React, { useState, useEffect } from 'react';
import { Loader2, Trash2 } from 'lucide-react';
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
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetDispatcherLocationsQuery,
  useUpdateDispatcherContainerMutation,
  useDeleteDispatcherContainerMutation,
  useUpdateDispatcherTruckMutation,
  useDeleteDispatcherTruckMutation,
  useUpdateDispatcherTrailerMutation,
  useDeleteDispatcherTrailerMutation,
  useUpdateDispatcherDriverMutation,
} from '../api/ttcrsApi';
import type {
  ResourceRow,
  ResourceKind,
  VehicleStatus,
  DriverStatus,
} from '../types';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const KIND_CLASS: Record<ResourceKind, string> = {
  CONTAINER: 'bg-blue-500 text-white border-transparent',
  TRUCK:     'bg-green-500 text-white border-transparent',
  TRAILER:   'bg-amber-500 text-white border-transparent',
  DRIVER:    'bg-purple-500 text-white border-transparent',
};

const VEHICLE_STATUSES: { value: VehicleStatus; label: string }[] = [
  { value: 'AVAILABLE',   label: 'Available' },
  { value: 'IN_USE',      label: 'In Use' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
];

const DRIVER_STATUSES: { value: DriverStatus; label: string }[] = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'IN_USE',    label: 'In Use' },
  { value: 'OFF',       label: 'Off' },
];

const STATUS_CLASS: Record<string, string> = {
  AVAILABLE:   'bg-green-100 text-green-700 border-green-200',
  IN_USE:      'bg-blue-100 text-blue-700 border-blue-200',
  MAINTENANCE: 'bg-amber-100 text-amber-700 border-amber-200',
  OFF:         'bg-red-100 text-red-700 border-red-200',
};

// -------------------------------------------------------------------------
// Props
// -------------------------------------------------------------------------

interface ResourceDetailSheetProps {
  resource: ResourceRow | null;
  open: boolean;
  onClose: () => void;
}

// -------------------------------------------------------------------------
// Component
// -------------------------------------------------------------------------

export function ResourceDetailSheet({ resource, open, onClose }: ResourceDetailSheetProps) {
  const [status, setStatus] = useState('');
  const [currentLocationCode, setCurrentLocationCode] = useState('');
  const [name, setName] = useState('');
  const [deleteOpen, setDeleteOpen] = useState(false);

  // Mutations
  const [updateContainer, { isLoading: savingContainer }] = useUpdateDispatcherContainerMutation();
  const [deleteContainer, { isLoading: deletingContainer }] = useDeleteDispatcherContainerMutation();
  const [updateTruck,     { isLoading: savingTruck }]     = useUpdateDispatcherTruckMutation();
  const [deleteTruck,     { isLoading: deletingTruck }]   = useDeleteDispatcherTruckMutation();
  const [updateTrailer,   { isLoading: savingTrailer }]   = useUpdateDispatcherTrailerMutation();
  const [deleteTrailer,   { isLoading: deletingTrailer }] = useDeleteDispatcherTrailerMutation();
  const [updateDriver,    { isLoading: savingDriver }]    = useUpdateDispatcherDriverMutation();

  const { data: locationsData } = useGetDispatcherLocationsQuery();
  const locationOptions = locationsData?.data ?? [];

  const isSaving   = savingContainer || savingTruck || savingTrailer || savingDriver;
  const isDeleting = deletingContainer || deletingTruck || deletingTrailer;

  useEffect(() => {
    if (!resource) return;
    setStatus(resource.status);
    setCurrentLocationCode(resource.currentLocationCode ?? '');
    setName(resource.identifier);
  }, [resource]);

  if (!resource) return null;

  const kind = resource.kind;
  const isDriver = kind === 'DRIVER';

  // ── Handlers ─────────────────────────────────────────────────────────────

  async function handleSave() {
    if (!resource) return;
    try {
      switch (kind) {
        case 'CONTAINER':
          await updateContainer({
            id: resource.id,
            body: { status: status as VehicleStatus, currentLocationCode: currentLocationCode || undefined },
          }).unwrap();
          break;
        case 'TRUCK':
          await updateTruck({
            id: resource.id,
            body: { status: status as VehicleStatus, currentLocationCode: currentLocationCode || undefined },
          }).unwrap();
          break;
        case 'TRAILER':
          await updateTrailer({
            id: resource.id,
            body: { status: status as VehicleStatus, currentLocationCode: currentLocationCode || undefined },
          }).unwrap();
          break;
        case 'DRIVER':
          await updateDriver({
            id: resource.id,
            body: { status: status as DriverStatus },
          }).unwrap();
          break;
      }
      toast.success(`${kind.charAt(0) + kind.slice(1).toLowerCase()} updated successfully`);
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to update resource');
    }
  }

  async function handleDelete() {
    if (!resource || kind === 'DRIVER') return;
    try {
      switch (kind) {
        case 'CONTAINER': await deleteContainer(resource.id).unwrap(); break;
        case 'TRUCK':     await deleteTruck(resource.id).unwrap();     break;
        case 'TRAILER':   await deleteTrailer(resource.id).unwrap();   break;
      }
      toast.success(`${resource.identifier} deleted`);
      setDeleteOpen(false);
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to delete resource');
    }
  }

  // ── Render ───────────────────────────────────────────────────────────────

  const kindLabel = kind.charAt(0) + kind.slice(1).toLowerCase();

  return (
    <>
      <Sheet open={open} onOpenChange={(o) => { if (!o) onClose(); }}>
        <SheetContent className='w-full sm:max-w-[480px] overflow-y-auto flex flex-col gap-0 p-0'>
          {/* Header */}
          <SheetHeader className='px-6 py-4 border-b'>
            <div className='flex flex-col gap-1 pr-8'>
              <SheetTitle className='text-lg font-semibold'>
                {resource.identifier}
              </SheetTitle>
              <div className='flex items-center gap-2'>
                <Badge className={cn('rounded px-2 py-0.5 text-xs font-bold', KIND_CLASS[kind])}>
                  {kindLabel}
                </Badge>
                <Badge className={cn('rounded-full px-2.5 py-0.5 text-xs font-medium', STATUS_CLASS[resource.status] ?? '')}>
                  {resource.status.replace('_', ' ')}
                </Badge>
              </div>
            </div>
          </SheetHeader>

          {/* Body */}
          <div className='flex-1 overflow-y-auto px-6 py-5 space-y-5'>
            {/* Code / Name */}
            {isDriver ? (
              <div className='space-y-1.5'>
                <Label htmlFor='res-name'>Name</Label>
                <Input
                  id='res-name'
                  value={resource.identifier}
                  readOnly
                  className='bg-muted/40 text-muted-foreground'
                />
                <p className='text-xs text-muted-foreground'>
                  Driver name is managed in Account settings.
                </p>
              </div>
            ) : (
              <div className='space-y-1.5'>
                <Label htmlFor='res-code'>Code</Label>
                <Input
                  id='res-code'
                  value={resource.identifier}
                  readOnly
                  className='bg-muted/40 text-muted-foreground'
                />
                <p className='text-xs text-muted-foreground'>
                  Resource code cannot be changed.
                </p>
              </div>
            )}

            <Separator />

            {/* Status */}
            <div className='space-y-1.5'>
              <Label>Status</Label>
              <Select value={status} onValueChange={setStatus}>
                <SelectTrigger className='w-full'>
                  <SelectValue placeholder='Select status' />
                </SelectTrigger>
                <SelectContent>
                  {(isDriver ? DRIVER_STATUSES : VEHICLE_STATUSES).map((s) => (
                    <SelectItem key={s.value} value={s.value}>{s.label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Current Location (not for drivers) */}
            {!isDriver && (
              <div className='space-y-1.5'>
                <Label>Current Location</Label>
                <Select value={currentLocationCode} onValueChange={setCurrentLocationCode}>
                  <SelectTrigger className='w-full'>
                    <SelectValue placeholder='Select location' />
                  </SelectTrigger>
                  <SelectContent>
                    {locationOptions.map((loc) => (
                      <SelectItem key={loc.locationCode} value={loc.locationCode}>
                        {loc.locationCode}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            {/* Container size (read-only for containers) */}
            {kind === 'CONTAINER' && resource.size && (
              <div className='space-y-1.5'>
                <Label>Size</Label>
                <Input
                  value={resource.size === 'TWENTY' ? '20 ft' : resource.size === 'FORTY' ? '40 ft' : resource.size}
                  readOnly
                  className='bg-muted/40 text-muted-foreground'
                />
              </div>
            )}
          </div>

          {/* Footer */}
          <div className='border-t px-6 py-4 flex items-center justify-between gap-3'>
            {!isDriver && (
              <Button
                variant='outline'
                size='sm'
                className='border-red-300 text-red-600 hover:bg-red-50 hover:text-red-700'
                onClick={() => setDeleteOpen(true)}
                disabled={isSaving || isDeleting}
              >
                <Trash2 className='h-4 w-4' />
                Delete
              </Button>
            )}
            {isDriver && <div />}

            <div className='flex items-center gap-2'>
              <Button variant='outline' size='sm' onClick={onClose} disabled={isSaving}>
                Close
              </Button>
              <Button size='sm' onClick={handleSave} disabled={isSaving}>
                {isSaving && <Loader2 className='h-4 w-4 animate-spin' />}
                Save Changes
              </Button>
            </div>
          </div>
        </SheetContent>
      </Sheet>

      {/* Delete Confirmation */}
      <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete this {kindLabel.toLowerCase()}?</AlertDialogTitle>
            <AlertDialogDescription>
              <strong>{resource.identifier}</strong> will be permanently removed.
              This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              disabled={isDeleting}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isDeleting && <Loader2 className='h-4 w-4 animate-spin' />}
              Yes, delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
