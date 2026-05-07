'use client';

import React, { useState, useEffect, useId } from 'react';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api/utils';
import {
  Button,
  Input,
  Label,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useCreateDispatcherLocationMutation } from '../api/ttcrsApi';
import type { LocationType, CreateLocationPayload } from '../types';
import { MapPicker } from './MapPicker';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface CreateLocationDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

interface FormState {
  locationCode: string;
  type: LocationType | '';
  lat: number | null;
  lng: number | null;
}

const INITIAL_FORM: FormState = {
  locationCode: '',
  type: '',
  lat: null,
  lng: null,
};

const LOCATION_TYPES: { value: LocationType; label: string }[] = [
  { value: 'PORT',            label: 'Port' },
  { value: 'WAREHOUSE',       label: 'Warehouse' },
  { value: 'DEPOT_CONTAINER', label: 'Depot – Container' },
  { value: 'DEPOT_TRUCK',     label: 'Depot – Truck' },
  { value: 'DEPOT_TRAILER',   label: 'Depot – Trailer' },
];

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export function CreateLocationDialog({
  open,
  onClose,
  onSuccess,
}: CreateLocationDialogProps) {
  const formId = useId();

  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const [errors, setErrors] = useState<Partial<Record<keyof FormState, string>>>({});

  const [createLocation, { isLoading: isCreating }] =
    useCreateDispatcherLocationMutation();

  // Reset when dialog opens
  useEffect(() => {
    if (open) {
      setForm(INITIAL_FORM);
      setErrors({});
    }
  }, [open]);

  // ── Helpers ──────────────────────────────────────────────────────────────

  const set = <K extends keyof FormState>(field: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const handleMapPick = (lat: number, lng: number) => {
    setForm((prev) => ({ ...prev, lat, lng }));
    setErrors((prev) => ({ ...prev, lat: undefined, lng: undefined }));
  };

  // ── Validation ───────────────────────────────────────────────────────────

  function validate(): boolean {
    const next: Partial<Record<keyof FormState, string>> = {};

    if (!form.locationCode.trim()) {
      next.locationCode = 'Location code is required';
    } else if (form.locationCode.length > 50) {
      next.locationCode = 'Location code must not exceed 50 characters';
    }

    if (!form.type) {
      next.type = 'Please select a location type';
    }

    if (form.lat === null || form.lng === null) {
      next.lat = 'Please click on the map to select a coordinate';
    }

    setErrors(next);
    return Object.keys(next).length === 0;
  }

  // ── Submit ───────────────────────────────────────────────────────────────

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    const payload: CreateLocationPayload = {
      locationCode: form.locationCode.trim().toUpperCase(),
      type: form.type as LocationType,
      lat: form.lat!,
      lng: form.lng!,
    };

    try {
      await createLocation(payload).unwrap();
      toast.success('Location created successfully');
      onSuccess?.();
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to create location');
      console.error('Failed to create location:', err);
    }
  }

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o) onClose(); }}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-[560px]'>
        <DialogHeader>
          <DialogTitle className='text-xl font-semibold'>
            Create New Location
          </DialogTitle>
        </DialogHeader>

        <form id={formId} onSubmit={handleSubmit} className='space-y-5 pt-2'>

          {/* Row: Location Code + Type */}
          <div className='grid grid-cols-2 gap-4'>
            {/* Location Code */}
            <div className='space-y-1.5'>
              <Label htmlFor={`${formId}-code`}>
                Location Code <span className='text-destructive'>*</span>
              </Label>
              <Input
                id={`${formId}-code`}
                type='text'
                placeholder='e.g. PORT-HCM'
                value={form.locationCode}
                onChange={(e) => set('locationCode', e.target.value)}
                className={cn(errors.locationCode && 'border-destructive')}
              />
              {errors.locationCode && (
                <p className='text-xs text-destructive'>{errors.locationCode}</p>
              )}
            </div>

            {/* Type */}
            <div className='space-y-1.5'>
              <Label htmlFor={`${formId}-type`}>
                Type <span className='text-destructive'>*</span>
              </Label>
              <Select
                value={form.type}
                onValueChange={(v) => set('type', v as LocationType)}
              >
                <SelectTrigger
                  id={`${formId}-type`}
                  className={cn('w-full', errors.type && 'border-destructive')}
                >
                  <SelectValue placeholder='Select type' />
                </SelectTrigger>
                <SelectContent>
                  {LOCATION_TYPES.map((t) => (
                    <SelectItem key={t.value} value={t.value}>
                      {t.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.type && (
                <p className='text-xs text-destructive'>{errors.type}</p>
              )}
            </div>
          </div>

          {/* Map picker */}
          <div className='space-y-1.5'>
            <Label>
              Coordinates <span className='text-destructive'>*</span>
            </Label>
            <p className='text-xs text-muted-foreground'>
              Click anywhere on the map to place a pin and extract the coordinates.
            </p>
            <MapPicker lat={form.lat} lng={form.lng} onPick={handleMapPick} />
            {errors.lat && (
              <p className='text-xs text-destructive'>{errors.lat}</p>
            )}
          </div>

          {/* Coordinate readout */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-1.5'>
              <Label htmlFor={`${formId}-lat`}>Latitude</Label>
              <Input
                id={`${formId}-lat`}
                type='text'
                readOnly
                value={form.lat !== null ? String(form.lat) : ''}
                placeholder='Click map to fill'
                className='bg-muted/40 text-muted-foreground'
              />
            </div>
            <div className='space-y-1.5'>
              <Label htmlFor={`${formId}-lng`}>Longitude</Label>
              <Input
                id={`${formId}-lng`}
                type='text'
                readOnly
                value={form.lng !== null ? String(form.lng) : ''}
                placeholder='Click map to fill'
                className='bg-muted/40 text-muted-foreground'
              />
            </div>
          </div>
        </form>

        <DialogFooter className='gap-2 pt-2'>
          <Button
            id='create-location-cancel-btn'
            type='button'
            variant='outline'
            onClick={onClose}
            disabled={isCreating}
          >
            Cancel
          </Button>
          <Button
            id='create-location-submit-btn'
            type='submit'
            form={formId}
            disabled={isCreating}
          >
            {isCreating && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
            Create Location
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
