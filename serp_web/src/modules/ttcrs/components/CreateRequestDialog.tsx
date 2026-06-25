'use client';

import React, { useState, useEffect, useId } from 'react';
import { Loader2, X } from 'lucide-react';
import { toast } from 'sonner';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings';
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
  Checkbox,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetDispatcherLocationsQuery,
  useCreateDispatcherRequestsMutation,
} from '../api/ttcrsApi';
import type { RequestType, CreateRequestPayload } from '../types';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface CreateRequestDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

interface FormState {
  customerId: string;
  type: RequestType | '';
  srcLocationCode: string;
  destLocationCode: string;
  quantity: string;
  weight: string;
  dropTrailerRequired: boolean;
  earlyAtSrc: string;
  lateAtSrc: string;
  earlyAtDest: string;
  lateAtDest: string;
}

const INITIAL_FORM: FormState = {
  customerId: '',
  type: '',
  srcLocationCode: '',
  destLocationCode: '',
  quantity: '1',
  weight: '',
  dropTrailerRequired: false,
  earlyAtSrc: '',
  lateAtSrc: '',
  earlyAtDest: '',
  lateAtDest: '',
};

// Time window fields required for each request type
const TIME_WINDOW_FIELDS: Record<
  RequestType,
  {
    earlyAtSrc: boolean;
    lateAtSrc: boolean;
    earlyAtDest: boolean;
    lateAtDest: boolean;
  }
> = {
  OF: {
    earlyAtSrc: true,
    lateAtSrc: false,
    earlyAtDest: false,
    lateAtDest: true,
  },
  IF: {
    earlyAtSrc: true,
    lateAtSrc: true,
    earlyAtDest: true,
    lateAtDest: true,
  },
  OE: {
    earlyAtSrc: true,
    lateAtSrc: true,
    earlyAtDest: true,
    lateAtDest: true,
  },
  IE: {
    earlyAtSrc: false,
    lateAtSrc: false,
    earlyAtDest: false,
    lateAtDest: true,
  },
};

const REQUEST_TYPES: { value: RequestType; label: string }[] = [
  { value: 'OF', label: 'Outbound Full (OF)' },
  { value: 'IF', label: 'Inbound Full (IF)' },
  { value: 'OE', label: 'Outbound Empty (OE)' },
  { value: 'IE', label: 'Inbound Empty (IE)' },
];

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export function CreateRequestDialog({
  open,
  onClose,
  onSuccess,
}: CreateRequestDialogProps) {
  const formId = useId();
  const user = useAppSelector(selectUserProfile);
  const organizationId = user?.organizationId;

  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const [errors, setErrors] = useState<
    Partial<Record<keyof FormState, string>>
  >({});

  // Fetch locations for dropdowns
  const { data: locationsData, isLoading: locationsLoading } =
    useGetDispatcherLocationsQuery(undefined, { skip: !open });

  // Fetch users to find customers (role TTCRS_CUSTOMER)
  const { data: usersData, isLoading: usersLoading } =
    useGetOrganizationUsersQuery(
      { organizationId: organizationId!, pageSize: 100, page: 0 },
      { skip: !open || !organizationId }
    );

  const customers =
    usersData?.data?.items?.filter((u) =>
      u.roles?.includes('TTCRS_CUSTOMER')
    ) ?? [];
  const locations = locationsData?.data ?? [];
  // Mutation
  const [createRequests, { isLoading: isCreating }] =
    useCreateDispatcherRequestsMutation();

  // Reset form when dialog opens/closes
  useEffect(() => {
    if (open) {
      setForm(INITIAL_FORM);
      setErrors({});
    }
  }, [open]);

  // Helpers
  const set = (field: keyof FormState, value: string | boolean) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const handleTypeChange = (value: string) => {
    set('type', value);
    if (value === 'OE') {
      setForm((prev) => ({ ...prev, srcLocationCode: '' }));
      setErrors((prev) => ({ ...prev, srcLocationCode: undefined }));
    }
  };

  // Validate
  function validate(): boolean {
    const next: Partial<Record<keyof FormState, string>> = {};
    const requiresOrigin = Boolean(form.type && form.type !== 'OE');

    if (!form.customerId) next.customerId = 'Please select a customer';
    if (!form.type) next.type = 'Please select a request type';
    if (requiresOrigin && !form.srcLocationCode) {
      next.srcLocationCode = 'Please select a source location';
    }
    if (!form.destLocationCode) next.destLocationCode = 'Please select a destination location';
    if (
      requiresOrigin &&
      form.srcLocationCode &&
      form.destLocationCode &&
      form.srcLocationCode === form.destLocationCode
    ) {
      next.destLocationCode = 'Destination location must be different from source location';
    }

    const qty = parseInt(form.quantity, 10);
    if (!form.quantity || isNaN(qty) || qty < 1) {
      next.quantity = 'Quantity must be >= 1';
    } else if (qty > 100) {
      next.quantity = 'Quantity must not exceed 100';
    }

    if (form.weight && isNaN(parseFloat(form.weight))) {
      next.weight = 'Weight must be a number';
    }

    setErrors(next);
    return Object.keys(next).length === 0;
  }

  // Submit
  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    const timeFields =
      form.type ? TIME_WINDOW_FIELDS[form.type as RequestType] : undefined;
    const requiresOrigin = form.type !== 'OE';

    const payload: CreateRequestPayload = {
      customerId: parseInt(form.customerId, 10),
      type: form.type as RequestType,
      destLocationCode: form.destLocationCode,
      quantity: parseInt(form.quantity, 10),
      weight: form.weight ? parseFloat(form.weight) : null,
      dropTrailerRequired: form.dropTrailerRequired,
      earlyAtSrc:
        timeFields?.earlyAtSrc && form.earlyAtSrc ? form.earlyAtSrc : null,
      lateAtSrc:
        timeFields?.lateAtSrc && form.lateAtSrc ? form.lateAtSrc : null,
      earlyAtDest:
        timeFields?.earlyAtDest && form.earlyAtDest ? form.earlyAtDest : null,
      lateAtDest:
        timeFields?.lateAtDest && form.lateAtDest ? form.lateAtDest : null,
    };

    if (requiresOrigin) {
      payload.srcLocationCode = form.srcLocationCode;
    } else {
      payload.srcLocationCode = null;
    }

    try {
      await createRequests(payload).unwrap();
      toast.success('Create request successfully');
      onSuccess?.();
      onClose();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to create request');
      console.error('Failed to create requests:', err);
    }
  }

  const selectedTypeFields = form.type
    ? TIME_WINDOW_FIELDS[form.type as RequestType]
    : null;

  const isLoading = locationsLoading || usersLoading;

  return (
    <Dialog
      open={open}
      onOpenChange={(o) => {
        if (!o) onClose();
      }}
    >
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-[600px]'>
        <DialogHeader>
          <DialogTitle className='text-xl font-semibold'>
            Create New Request
          </DialogTitle>
        </DialogHeader>

        <form id={formId} onSubmit={handleSubmit} className='space-y-5 pt-2'>
          {/* Row 1: Customer + Quantity */}
          <div className='grid grid-cols-2 gap-4'>
            {/* Customer */}
            <div className='min-w-0 space-y-1.5'>
              <Label htmlFor={`${formId}-customer`}>
                Customer <span className='text-destructive'>*</span>
              </Label>
              <Select
                value={form.customerId}
                onValueChange={(v) => set('customerId', v)}
                disabled={isLoading}
              >
                <SelectTrigger
                  id={`${formId}-customer`}
                  className={cn(
                    'w-full',
                    errors.customerId && 'border-destructive'
                  )}
                >
                  <SelectValue
                    className='truncate'
                    placeholder={usersLoading ? 'Loading…' : 'Select customer'}
                  />
                </SelectTrigger>
                <SelectContent>
                  {customers.length === 0 && !usersLoading && (
                    <SelectItem value='__none__' disabled>
                      No customers found
                    </SelectItem>
                  )}
                  {customers.map((c) => (
                    <SelectItem key={c.id} value={String(c.id)}>
                      {c.firstName} {c.lastName} — {c.email}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.customerId && (
                <p className='text-xs text-destructive'>{errors.customerId}</p>
              )}
            </div>

            {/* Quantity */}
            <div className='min-w-0 space-y-1.5'>
              <Label htmlFor={`${formId}-quantity`}>
                Quantity <span className='text-destructive'>*</span>
              </Label>
              <Input
                id={`${formId}-quantity`}
                type='number'
                min={1}
                max={100}
                value={form.quantity}
                onChange={(e) => set('quantity', e.target.value)}
                className={cn(errors.quantity && 'border-destructive')}
                placeholder='1'
              />
              {errors.quantity && (
                <p className='text-xs text-destructive'>{errors.quantity}</p>
              )}
            </div>
          </div>

          {/* Row 2: Type */}
          <div className='space-y-1.5'>
            <Label htmlFor={`${formId}-type`}>
              Request Type <span className='text-destructive'>*</span>
            </Label>
            <Select
              value={form.type}
              onValueChange={handleTypeChange}
            >
              <SelectTrigger
                id={`${formId}-type`}
                className={cn('w-full', errors.type && 'border-destructive')}
              >
                <SelectValue placeholder='Select request type' />
              </SelectTrigger>
              <SelectContent>
                {REQUEST_TYPES.map((t) => (
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

          {/* Row 3: Origin + Destination */}
          <div className='grid grid-cols-2 gap-4'>
            {/* Origin */}
            {form.type === 'OE' ? (
              <div className="min-w-0 space-y-1.5">
                <Label>Origin</Label>
                <div className="rounded-md border border-dashed border-border bg-muted/30 px-3 py-2 text-xs text-muted-foreground">
                  Origin for OE requests is determined by the selected container when building a route.
                </div>
              </div>
            ) : (
              <div className="min-w-0 space-y-1.5">
                <Label htmlFor={`${formId}-src`}>
                  Origin <span className="text-destructive">*</span>
                </Label>
                <Select
                  value={form.srcLocationCode}
                  onValueChange={(v) => set('srcLocationCode', v)}
                  disabled={locationsLoading}
                >
                  <SelectTrigger
                    id={`${formId}-src`}
                    className={cn(
                      'w-full',
                      errors.srcLocationCode && 'border-destructive'
                    )}
                  >
                    <SelectValue placeholder={locationsLoading ? 'Loading…' : 'Select origin'} />
                  </SelectTrigger>
                  <SelectContent>
                    {locations.map((loc) => (
                      <SelectItem key={loc.id} value={loc.locationCode}>
                        {loc.locationCode}
                        <span className="ml-1.5 text-xs text-muted-foreground">
                          ({loc.type})
                        </span>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.srcLocationCode && (
                  <p className="text-xs text-destructive">{errors.srcLocationCode}</p>
                )}
              </div>
            )}

            {/* Destination */}
            <div className='min-w-0 space-y-1.5'>
              <Label htmlFor={`${formId}-dest`}>
                Destination <span className='text-destructive'>*</span>
              </Label>
              <Select
                value={form.destLocationCode}
                onValueChange={(v) => set('destLocationCode', v)}
                disabled={locationsLoading}
              >
                <SelectTrigger
                  id={`${formId}-dest`}
                  className={cn(
                    'w-full',
                    errors.destLocationCode && 'border-destructive'
                  )}
                >
                  <SelectValue
                    placeholder={
                      locationsLoading ? 'Loading…' : 'Select destination'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {locations.map((loc) => (
                    <SelectItem key={loc.id} value={loc.locationCode}>
                      {loc.locationCode}
                      <span className='ml-1.5 text-xs text-muted-foreground'>
                        ({loc.type})
                      </span>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.destLocationCode && (
                <p className='text-xs text-destructive'>
                  {errors.destLocationCode}
                </p>
              )}
            </div>
          </div>

          {/* Time window — conditional on type */}
          {selectedTypeFields && (
            <div className='rounded-lg border border-border bg-muted/30 p-4 space-y-3'>
              <p className='text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                Time Window
              </p>
              <div className='grid grid-cols-2 gap-4'>
                {selectedTypeFields.earlyAtSrc && (
                  <div className='space-y-1.5'>
                    <Label htmlFor={`${formId}-earlyAtSrc`}>
                      Earliest at Origin
                    </Label>
                    <Input
                      id={`${formId}-earlyAtSrc`}
                      type='datetime-local'
                      value={form.earlyAtSrc}
                      onChange={(e) => set('earlyAtSrc', e.target.value)}
                    />
                  </div>
                )}
                {selectedTypeFields.lateAtSrc && (
                  <div className='space-y-1.5'>
                    <Label htmlFor={`${formId}-lateAtSrc`}>
                      Latest at Origin
                    </Label>
                    <Input
                      id={`${formId}-lateAtSrc`}
                      type='datetime-local'
                      value={form.lateAtSrc}
                      onChange={(e) => set('lateAtSrc', e.target.value)}
                    />
                  </div>
                )}
                {selectedTypeFields.earlyAtDest && (
                  <div className='space-y-1.5'>
                    <Label htmlFor={`${formId}-earlyAtDest`}>
                      Earliest at Destination
                    </Label>
                    <Input
                      id={`${formId}-earlyAtDest`}
                      type='datetime-local'
                      value={form.earlyAtDest}
                      onChange={(e) => set('earlyAtDest', e.target.value)}
                    />
                  </div>
                )}
                {selectedTypeFields.lateAtDest && (
                  <div className='space-y-1.5'>
                    <Label htmlFor={`${formId}-lateAtDest`}>
                      Latest at Destination
                    </Label>
                    <Input
                      id={`${formId}-lateAtDest`}
                      type='datetime-local'
                      value={form.lateAtDest}
                      onChange={(e) => set('lateAtDest', e.target.value)}
                    />
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Row: Weight + Drop trailer */}
          <div className='grid grid-cols-2 gap-4 items-end'>
            <div className='space-y-1.5'>
              <Label htmlFor={`${formId}-weight`}>Weight (tons)</Label>
              <Input
                id={`${formId}-weight`}
                type='number'
                min={0}
                step='0.01'
                placeholder='Optional'
                value={form.weight}
                onChange={(e) => set('weight', e.target.value)}
                className={cn(errors.weight && 'border-destructive')}
              />
              {errors.weight && (
                <p className='text-xs text-destructive'>{errors.weight}</p>
              )}
            </div>

            <div className='flex items-center gap-2 pb-2'>
              <Checkbox
                id={`${formId}-drop-trailer`}
                checked={form.dropTrailerRequired}
                onCheckedChange={(checked) =>
                  set('dropTrailerRequired', Boolean(checked))
                }
              />
              <Label
                htmlFor={`${formId}-drop-trailer`}
                className='cursor-pointer select-none text-sm font-normal'
              >
                Need to Detach Trailer
              </Label>
            </div>
          </div>
        </form>

        <DialogFooter className='gap-2 pt-2'>
          <Button
            id='create-request-cancel-btn'
            type='button'
            variant='outline'
            onClick={onClose}
            disabled={isCreating}
          >
            Cancel
          </Button>
          <Button
            id='create-request-submit-btn'
            type='submit'
            form={formId}
            disabled={isCreating}
          >
            {isCreating && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
            Create Request
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
