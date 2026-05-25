'use client';

import * as React from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { schoolBusUi } from '../../theme';
import type {
  SchoolBusPickupPoint,
  SchoolBusSchoolPickupPointUpsertRequest,
} from '../../types';
import { SchoolBusFormDialog } from '../SchoolBusFormDialog';

const linkSchema = z.object({
  pickupPointId: z.coerce.number().min(1, 'Pickup point is required'),
  isDefault: z.boolean().default(false),
  isActive: z.boolean().default(true),
});

type LinkFormValues = z.infer<typeof linkSchema>;

interface LinkPickupPointDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pickupPoints: SchoolBusPickupPoint[];
  onSubmit: (values: SchoolBusSchoolPickupPointUpsertRequest) => Promise<void>;
  isLoading?: boolean;
}

export function LinkPickupPointDialog({
  open,
  onOpenChange,
  pickupPoints,
  onSubmit,
  isLoading = false,
}: LinkPickupPointDialogProps) {
  const form = useForm<LinkFormValues>({
    resolver: zodResolver(linkSchema) as any,
    defaultValues: {
      pickupPointId: pickupPoints[0]?.id ?? 0,
      isDefault: false,
      isActive: true,
    },
  });

  React.useEffect(() => {
    if (open) {
      form.reset({
        pickupPointId: pickupPoints[0]?.id ?? 0,
        isDefault: false,
        isActive: true,
      });
    }
  }, [form, open, pickupPoints]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title='Link pickup point to school'
      description='Associate an existing pickup point with this school. Time windows are configured separately per schedule.'
    >
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(async (values) =>
            onSubmit({
              pickupPointId: values.pickupPointId,
              isDefault: values.isDefault,
              isActive: values.isActive,
            })
          )}
          className='space-y-4'
        >
          <div className='grid gap-4 md:grid-cols-2'>
            <FormField
              control={form.control}
              name='pickupPointId'
              render={({ field }) => (
                <FormItem className='md:col-span-2'>
                  <FormLabel>Pickup point *</FormLabel>
                  <Select
                    onValueChange={(v) => field.onChange(Number(v))}
                    value={String(field.value || '')}
                  >
                    <FormControl>
                      <SelectTrigger className='h-11 w-full rounded-xl border-slate-200 bg-white shadow-sm [&_[data-slot=select-value]]:block [&_[data-slot=select-value]]:max-w-[calc(100%-1.75rem)] [&_[data-slot=select-value]]:truncate'>
                        <SelectValue placeholder='Select pickup point' />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent className='z-[120] max-h-72 rounded-xl border-slate-200 bg-white'>
                      {pickupPoints.map((pp) => (
                        <SelectItem key={pp.id} value={String(pp.id)}>
                          {pp.name} — {pp.address} ({pp.usageType || 'N/A'})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>
          <div className='flex justify-end gap-2 border-t pt-4'>
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              className={schoolBusUi.primaryButton}
              disabled={isLoading}
            >
              {isLoading ? 'Linking...' : 'Link pickup point'}
            </Button>
          </div>
        </form>
      </Form>
    </SchoolBusFormDialog>
  );
}
