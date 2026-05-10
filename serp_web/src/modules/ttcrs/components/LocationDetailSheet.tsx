'use client';

import React, { useState, useEffect } from 'react';
import { Loader2, Trash2, MapPin } from 'lucide-react';
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
  useUpdateDispatcherLocationMutation,
  useDeleteDispatcherLocationMutation,
} from '../api/ttcrsApi';
import { MapPicker } from './MapPicker';
import type { LocationItem, LocationType, UpdateLocationPayload } from '../types';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const LOCATION_TYPES: { value: LocationType; label: string }[] = [
  { value: 'PORT',            label: 'Port' },
  { value: 'WAREHOUSE',       label: 'Warehouse' },
  { value: 'DEPOT_CONTAINER', label: 'Depot – Container' },
  { value: 'DEPOT_TRUCK',     label: 'Depot – Truck' },
  { value: 'DEPOT_TRAILER',   label: 'Depot – Trailer' },
];

const TYPE_CLASS: Record<LocationType, string> = {
  PORT:            'bg-blue-500 text-white border-transparent',
  WAREHOUSE:       'bg-green-500 text-white border-transparent',
  DEPOT_CONTAINER: 'bg-amber-500 text-white border-transparent',
  DEPOT_TRUCK:     'bg-purple-500 text-white border-transparent',
  DEPOT_TRAILER:   'bg-rose-500 text-white border-transparent',
};

const TYPE_LABEL: Record<LocationType, string> = {
  PORT:            'Port',
  WAREHOUSE:       'Warehouse',
  DEPOT_CONTAINER: 'Depot – Container',
  DEPOT_TRUCK:     'Depot – Truck',
  DEPOT_TRAILER:   'Depot – Trailer',
};

// -------------------------------------------------------------------------
// Props
// -------------------------------------------------------------------------

interface LocationDetailSheetProps {
  location: LocationItem | null;
  open: boolean;
  onClose: () => void;
}

// -------------------------------------------------------------------------
// Component
// -------------------------------------------------------------------------

export function LocationDetailSheet({ location, open, onClose }: LocationDetailSheetProps) {
  const [type, setType] = useState<LocationType | ''>('');
  const [lat, setLat] = useState<number | null>(null);
  const [lng, setLng] = useState<number | null>(null);
  const [deleteOpen, setDeleteOpen] = useState(false);

  const [updateLocation, { isLoading: isSaving }] = useUpdateDispatcherLocationMutation();
  const [deleteLocation, { isLoading: isDeleting }] = useDeleteDispatcherLocationMutation();

  useEffect(() => {
    if (!location) return;
    setType(location.type);
    setLat(location.lat);
    setLng(location.lng);
  }, [location]);

  if (!location) return null;

  // ── Handlers ─────────────────────────────────────────────────────────────

  async function handleSave() {
    if (!location) return;
    const payload: UpdateLocationPayload = {};
    if (type)        payload.type = type as LocationType;
    if (lat != null) payload.lat  = lat;
    if (lng != null) payload.lng  = lng;

    try {
      await updateLocation({ id: location.id, body: payload }).unwrap();
      toast.success('Location updated successfully');
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to update location');
    }
  }

  async function handleDelete() {
    if (!location) return;
    try {
      await deleteLocation(location.id).unwrap();
      toast.success(`Location "${location.locationCode}" deleted`);
      setDeleteOpen(false);
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to delete location');
    }
  }

  // ── Render ───────────────────────────────────────────────────────────────

  return (
    <>
      <Sheet open={open} onOpenChange={(o) => { if (!o) onClose(); }}>
        <SheetContent className='w-full sm:max-w-[560px] overflow-y-auto flex flex-col gap-0 p-0'>
          {/* Header */}
          <SheetHeader className='px-6 py-4 border-b'>
            <div className='flex flex-col gap-1 pr-8'>
              <SheetTitle className='flex items-center gap-2 text-lg font-semibold'>
                <MapPin className='h-4 w-4 text-muted-foreground' />
                {location.locationCode}
              </SheetTitle>
              <Badge className={cn('w-fit rounded px-2 py-0.5 text-xs font-bold', TYPE_CLASS[location.type])}>
                {TYPE_LABEL[location.type]}
              </Badge>
            </div>
          </SheetHeader>

          {/* Body */}
          <div className='flex-1 overflow-y-auto px-6 py-5 space-y-5'>
            {/* Location Code (read-only) */}
            <div className='space-y-1.5'>
              <Label htmlFor='loc-code'>Location Code</Label>
              <Input
                id='loc-code'
                value={location.locationCode}
                readOnly
                className='bg-muted/40 text-muted-foreground'
              />
              <p className='text-xs text-muted-foreground'>
                Location code cannot be changed as it is used as a reference key.
              </p>
            </div>

            <Separator />

            {/* Type */}
            <div className='space-y-1.5'>
              <Label>Type</Label>
              <Select value={type} onValueChange={(v) => setType(v as LocationType)}>
                <SelectTrigger className='w-full'>
                  <SelectValue placeholder='Select type' />
                </SelectTrigger>
                <SelectContent>
                  {LOCATION_TYPES.map((t) => (
                    <SelectItem key={t.value} value={t.value}>{t.label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Map Picker */}
            <div className='space-y-1.5'>
              <Label>Coordinates</Label>
              <p className='text-xs text-muted-foreground'>
                Click the map to update the pin location.
              </p>
              <MapPicker
                lat={lat}
                lng={lng}
                onPick={(newLat, newLng) => { setLat(newLat); setLng(newLng); }}
              />
            </div>

            {/* Coordinate display */}
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-1.5'>
                <Label>Latitude</Label>
                <Input
                  value={lat != null ? String(lat) : ''}
                  readOnly
                  className='bg-muted/40 text-muted-foreground'
                />
              </div>
              <div className='space-y-1.5'>
                <Label>Longitude</Label>
                <Input
                  value={lng != null ? String(lng) : ''}
                  readOnly
                  className='bg-muted/40 text-muted-foreground'
                />
              </div>
            </div>
          </div>

          {/* Footer actions */}
          <div className='border-t px-6 py-4 flex items-center justify-between gap-3'>
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
            <AlertDialogTitle>Delete this location?</AlertDialogTitle>
            <AlertDialogDescription>
              <strong>{location.locationCode}</strong> will be permanently removed from the system.
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
