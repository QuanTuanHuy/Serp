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
import {
  useCreateDispatcherContainerMutation,
  useCreateDispatcherTruckMutation,
  useCreateDispatcherTrailerMutation,
  useGetDispatcherLocationsQuery,
} from '../api/ttcrsApi';
import type {
  ResourceKind,
  ResourceContainerSize,
  CreateContainerPayload,
  CreateTruckPayload,
  CreateTrailerPayload,
} from '../types';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const CONTAINER_SIZES: { value: ResourceContainerSize; label: string }[] = [
  { value: 'TWENTY', label: '20 ft' },
  { value: 'FORTY', label: '40 ft' },
];

const KIND_TABS: { value: ResourceKind; label: string }[] = [
  { value: 'CONTAINER', label: 'Container' },
  { value: 'TRUCK', label: 'Truck' },
  { value: 'TRAILER', label: 'Trailer' },
];

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface CreateResourceDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
  defaultKind?: ResourceKind;
}

type FormState = {
  kind: ResourceKind;
  code: string;
  size: ResourceContainerSize | '';
  currentLocationCode: string;
};

const INITIAL_FORM: FormState = {
  kind: 'CONTAINER',
  code: '',
  size: '',
  currentLocationCode: '',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export function CreateResourceDialog({
  open,
  onClose,
  onSuccess,
  defaultKind = 'CONTAINER',
}: CreateResourceDialogProps) {
  const formId = useId();

  const [form, setForm] = useState<FormState>({ ...INITIAL_FORM, kind: defaultKind });
  const [errors, setErrors] = useState<Partial<Record<keyof FormState, string>>>({});

  const [createContainer, { isLoading: creatingContainer }] = useCreateDispatcherContainerMutation();
  const [createTruck,     { isLoading: creatingTruck     }] = useCreateDispatcherTruckMutation();
  const [createTrailer,   { isLoading: creatingTrailer   }] = useCreateDispatcherTrailerMutation();

  const { data: locationsData } = useGetDispatcherLocationsQuery(undefined, { skip: !open });
  const locationCodes = locationsData?.data?.map((l) => l.locationCode) ?? [];

  const isCreating = creatingContainer || creatingTruck || creatingTrailer;

  useEffect(() => {
    if (open) {
      setForm({ ...INITIAL_FORM, kind: defaultKind });
      setErrors({});
    }
  }, [open, defaultKind]);

  const set = <K extends keyof FormState>(field: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const handleKindChange = (kind: ResourceKind) => {
    setForm({ ...INITIAL_FORM, kind });
    setErrors({});
  };

  // ── Validation ───────────────────────────────────────────────────────────

  function validate(): boolean {
    const next: Partial<Record<keyof FormState, string>> = {};

    if (!form.code.trim()) next.code = 'Code is required';
    else if (form.code.length > 50) next.code = 'Code must not exceed 50 characters';

    if (!form.currentLocationCode) next.currentLocationCode = 'Current location is required';

    setErrors(next);
    return Object.keys(next).length === 0;
  }

  // ── Submit ───────────────────────────────────────────────────────────────

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    const code = form.code.trim().toUpperCase();

    try {
      if (form.kind === 'CONTAINER') {
        const payload: CreateContainerPayload = {
          code,
          size: form.size || null,
          currentLocationCode: form.currentLocationCode,
        };
        await createContainer(payload).unwrap();
        toast.success('Container created successfully');
      } else if (form.kind === 'TRUCK') {
        const payload: CreateTruckPayload = {
          code,
          currentLocationCode: form.currentLocationCode,
        };
        await createTruck(payload).unwrap();
        toast.success('Truck created successfully');
      } else if (form.kind === 'TRAILER') {
        const payload: CreateTrailerPayload = {
          code,
          currentLocationCode: form.currentLocationCode,
        };
        await createTrailer(payload).unwrap();
        toast.success('Trailer created successfully');
      }
      onSuccess?.();
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to create resource');
    }
  }

  const currentKindLabel = KIND_TABS.find((t) => t.value === form.kind)?.label ?? '';

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o) onClose(); }}>
      <DialogContent className='sm:max-w-[480px]'>
        <DialogHeader>
          <DialogTitle className='text-xl font-semibold'>Create New Resource</DialogTitle>
        </DialogHeader>

        {/* Kind selector */}
        <div className='flex gap-1 rounded-lg border p-1'>
          {KIND_TABS.map((tab) => (
            <button
              key={tab.value}
              type='button'
              onClick={() => handleKindChange(tab.value)}
              className={cn(
                'flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
                form.kind === tab.value
                  ? 'bg-primary text-primary-foreground shadow-sm'
                  : 'text-muted-foreground hover:text-foreground'
              )}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <form id={formId} onSubmit={handleSubmit} className='space-y-4 pt-1'>
          {/* Code */}
          <div className='space-y-1.5'>
            <Label htmlFor={`${formId}-code`}>
              Code <span className='text-destructive'>*</span>
            </Label>
            <Input
              id={`${formId}-code`}
              type='text'
              placeholder={
                form.kind === 'CONTAINER'
                  ? 'e.g. CONT-001'
                  : form.kind === 'TRUCK'
                  ? 'e.g. TRK-001'
                  : 'e.g. TRL-001'
              }
              value={form.code}
              onChange={(e) => set('code', e.target.value)}
              className={cn(errors.code && 'border-destructive')}
            />
            {errors.code && <p className='text-xs text-destructive'>{errors.code}</p>}
          </div>

          {/* Size — containers only */}
          {form.kind === 'CONTAINER' && (
            <div className='space-y-1.5'>
              <Label htmlFor={`${formId}-size`}>Size</Label>
              <Select
                value={form.size}
                onValueChange={(v) => set('size', v as ResourceContainerSize)}
              >
                <SelectTrigger id={`${formId}-size`} className='w-full'>
                  <SelectValue placeholder='Select size (optional)' />
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
          )}

          {/* Current Location */}
          <div className='space-y-1.5'>
            <Label htmlFor={`${formId}-loc`}>
              Current Location <span className='text-destructive'>*</span>
            </Label>
            <Select
              value={form.currentLocationCode}
              onValueChange={(v) => set('currentLocationCode', v)}
            >
              <SelectTrigger
                id={`${formId}-loc`}
                className={cn('w-full', errors.currentLocationCode && 'border-destructive')}
              >
                <SelectValue placeholder='Select a location' />
              </SelectTrigger>
              <SelectContent>
                {locationCodes.length === 0 ? (
                  <SelectItem value='__none__' disabled>
                    No locations available
                  </SelectItem>
                ) : (
                  locationCodes.map((code) => (
                    <SelectItem key={code} value={code}>
                      {code}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
            {errors.currentLocationCode && (
              <p className='text-xs text-destructive'>{errors.currentLocationCode}</p>
            )}
          </div>
        </form>

        <DialogFooter className='gap-2 pt-2'>
          <Button
            type='button'
            variant='outline'
            onClick={onClose}
            disabled={isCreating}
          >
            Cancel
          </Button>
          <Button
            id='create-resource-submit-btn'
            type='submit'
            form={formId}
            disabled={isCreating}
          >
            {isCreating && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
            Create {currentKindLabel}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
