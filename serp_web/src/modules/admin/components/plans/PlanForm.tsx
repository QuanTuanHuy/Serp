/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription Plan Form Component
 */

'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Button,
  Input,
  Label,
  Card,
  CardContent,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { Loader2 } from 'lucide-react';
import type { SubscriptionPlan } from '../../types';

// Validation schema
const planFormSchema = z
  .object({
    planName: z
      .string()
      .min(1, 'Plan name is required')
      .max(100, 'Plan name must not exceed 100 characters'),
    planCode: z
      .string()
      .min(1, 'Plan code is required')
      .max(50, 'Plan code must not exceed 50 characters')
      .regex(
        /^[A-Z0-9_]+$/,
        'Plan code must be uppercase alphanumeric with underscores'
      ),
    description: z
      .string()
      .max(1000, 'Description must not exceed 1000 characters')
      .optional(),
    monthlyPrice: z.preprocess(
      (val) => (val === '' ? 0 : Number(val)),
      z.number().min(0, 'Monthly price must be >= 0').max(99999999.99)
    ),
    yearlyPrice: z.preprocess(
      (val) => (val === '' ? 0 : Number(val)),
      z.number().min(0, 'Yearly price must be >= 0').max(99999999.99)
    ),
    maxUsers: z.preprocess(
      (val) => (val === '' || val === null ? undefined : Number(val)),
      z.number().int().min(1).optional()
    ),
    trialDays: z.preprocess(
      (val) => (val === '' ? 0 : Number(val)),
      z.number().int().min(0).max(365)
    ),
    isActive: z.boolean(),
    isCustom: z.boolean(),
    organizationId: z.preprocess(
      (val) => (val === '' || val === null ? undefined : Number(val)),
      z.number().int().positive().optional()
    ),
    displayOrder: z.preprocess(
      (val) => (val === '' ? 0 : Number(val)),
      z.number().int().min(0)
    ),
  })
  .refine(
    (data) => {
      if (data.isCustom && !data.organizationId) {
        return false;
      }
      return true;
    },
    {
      message: 'Organization ID is required for custom plans',
      path: ['organizationId'],
    }
  )
  .refine(
    (data) => {
      if (!data.isCustom && data.organizationId) {
        return false;
      }
      return true;
    },
    {
      message: 'Organization ID must be empty for standard plans',
      path: ['organizationId'],
    }
  );

type PlanFormData = z.infer<typeof planFormSchema>;

interface PlanFormProps {
  plan?: SubscriptionPlan;
  onSubmit: (
    data: Omit<
      SubscriptionPlan,
      'id' | 'createdAt' | 'updatedAt' | 'createdBy' | 'updatedBy'
    >
  ) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const PlanForm: React.FC<PlanFormProps> = ({
  plan,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!plan;

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<PlanFormData>({
    defaultValues: plan
      ? {
          planName: plan.planName,
          planCode: plan.planCode,
          description: plan.description || '',
          monthlyPrice: plan.monthlyPrice,
          yearlyPrice: plan.yearlyPrice,
          maxUsers: plan.maxUsers || undefined,
          trialDays: plan.trialDays || 0,
          isActive: plan.isActive,
          isCustom: plan.isCustom,
          organizationId: plan.organizationId || undefined,
          displayOrder: plan.displayOrder || 0,
        }
      : {
          planName: '',
          planCode: '',
          description: '',
          monthlyPrice: 0,
          yearlyPrice: 0,
          maxUsers: undefined,
          trialDays: 0,
          isActive: true,
          isCustom: false,
          organizationId: undefined,
          displayOrder: 0,
        },
  });

  const isCustom = watch('isCustom');

  const handleFormSubmit = handleSubmit(async (data) => {
    try {
      // Transform string values to numbers
      const transformedData = {
        ...data,
        monthlyPrice: Number(data.monthlyPrice) || 0,
        yearlyPrice: Number(data.yearlyPrice) || 0,
        maxUsers: data.maxUsers ? Number(data.maxUsers) : undefined,
        trialDays: Number(data.trialDays) || 0,
        displayOrder: Number(data.displayOrder) || 0,
        organizationId: data.organizationId
          ? Number(data.organizationId)
          : undefined,
      };

      await onSubmit(transformedData as any);
    } catch (error) {
      console.error('Form submission error:', error);
    }
  });

  return (
    <form onSubmit={handleFormSubmit} className={cn('space-y-6', className)}>
      {/* Basic Information */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Basic Information</h3>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='planName'>
                Plan Name <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='planName'
                placeholder='e.g. Professional Plan'
                {...register('planName')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.planName && 'border-destructive')}
              />
              {errors.planName && (
                <p className='text-sm text-destructive'>
                  {errors.planName.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='planCode'>
                Plan Code <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='planCode'
                placeholder='e.g. PROFESSIONAL_PLAN'
                {...register('planCode')}
                disabled={isLoading || isSubmitting || isEditing}
                className={cn(
                  'uppercase',
                  errors.planCode && 'border-destructive'
                )}
              />
              <p className='text-xs text-muted-foreground'>
                Uppercase alphanumeric with underscores only
              </p>
              {errors.planCode && (
                <p className='text-sm text-destructive'>
                  {errors.planCode.message}
                </p>
              )}
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='description'>Description</Label>
            <textarea
              id='description'
              placeholder='Describe the plan features and benefits...'
              {...register('description')}
              disabled={isLoading || isSubmitting}
              className={cn(
                'flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
                errors.description && 'border-destructive'
              )}
              rows={3}
            />
            {errors.description && (
              <p className='text-sm text-destructive'>
                {errors.description.message}
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Pricing */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Pricing</h3>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='monthlyPrice'>
                Monthly Price ($) <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='monthlyPrice'
                type='number'
                step='0.01'
                min='0'
                placeholder='0.00'
                {...register('monthlyPrice')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.monthlyPrice && 'border-destructive')}
              />
              {errors.monthlyPrice && (
                <p className='text-sm text-destructive'>
                  {errors.monthlyPrice.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='yearlyPrice'>
                Yearly Price ($) <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='yearlyPrice'
                type='number'
                step='0.01'
                min='0'
                placeholder='0.00'
                {...register('yearlyPrice')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.yearlyPrice && 'border-destructive')}
              />
              {errors.yearlyPrice && (
                <p className='text-sm text-destructive'>
                  {errors.yearlyPrice.message}
                </p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Limits & Trial */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Limits & Trial</h3>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='maxUsers'>Max Users</Label>
              <Input
                id='maxUsers'
                type='number'
                min='1'
                placeholder='Leave empty for unlimited'
                {...register('maxUsers')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.maxUsers && 'border-destructive')}
              />
              <p className='text-xs text-muted-foreground'>
                Leave empty for unlimited users
              </p>
              {errors.maxUsers && (
                <p className='text-sm text-destructive'>
                  {errors.maxUsers.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='trialDays'>
                Trial Days <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='trialDays'
                type='number'
                min='0'
                max='365'
                placeholder='0'
                {...register('trialDays')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.trialDays && 'border-destructive')}
              />
              <p className='text-xs text-muted-foreground'>0-365 days</p>
              {errors.trialDays && (
                <p className='text-sm text-destructive'>
                  {errors.trialDays.message}
                </p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Settings */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Settings</h3>

          <div className='space-y-4'>
            <div className='flex items-center justify-between rounded-lg border p-4'>
              <div className='space-y-0.5'>
                <Label htmlFor='isActive' className='text-base'>
                  Active
                </Label>
                <p className='text-sm text-muted-foreground'>
                  Make this plan available for subscription
                </p>
              </div>
              <input
                type='checkbox'
                id='isActive'
                {...register('isActive')}
                disabled={isLoading || isSubmitting}
                className='h-4 w-4 rounded border-gray-300 text-primary focus:ring-2 focus:ring-primary'
              />
            </div>

            <div className='flex items-center justify-between rounded-lg border p-4'>
              <div className='space-y-0.5'>
                <Label htmlFor='isCustom' className='text-base'>
                  Custom Plan
                </Label>
                <p className='text-sm text-muted-foreground'>
                  This plan is for a specific organization
                </p>
              </div>
              <input
                type='checkbox'
                id='isCustom'
                {...register('isCustom')}
                disabled={isLoading || isSubmitting || isEditing}
                className='h-4 w-4 rounded border-gray-300 text-primary focus:ring-2 focus:ring-primary'
              />
            </div>

            {isCustom && (
              <div className='space-y-2'>
                <Label htmlFor='organizationId'>
                  Organization ID <span className='text-destructive'>*</span>
                </Label>
                <Input
                  id='organizationId'
                  type='number'
                  min='1'
                  placeholder='Enter organization ID'
                  {...register('organizationId')}
                  disabled={isLoading || isSubmitting || isEditing}
                  className={cn(errors.organizationId && 'border-destructive')}
                />
                <p className='text-xs text-muted-foreground'>
                  Required for custom plans
                </p>
                {errors.organizationId && (
                  <p className='text-sm text-destructive'>
                    {errors.organizationId.message}
                  </p>
                )}
              </div>
            )}

            <div className='space-y-2'>
              <Label htmlFor='displayOrder'>Display Order</Label>
              <Input
                id='displayOrder'
                type='number'
                min='0'
                placeholder='0'
                {...register('displayOrder')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.displayOrder && 'border-destructive')}
              />
              <p className='text-xs text-muted-foreground'>
                Order in which the plan appears (lower = first)
              </p>
              {errors.displayOrder && (
                <p className='text-sm text-destructive'>
                  {errors.displayOrder.message}
                </p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Submit Buttons */}
      <div className='flex justify-end gap-2'>
        {onCancel && (
          <Button
            type='button'
            variant='outline'
            onClick={onCancel}
            disabled={isLoading || isSubmitting}
          >
            Cancel
          </Button>
        )}
        <Button type='submit' disabled={isLoading || isSubmitting}>
          {(isLoading || isSubmitting) && (
            <Loader2 className='mr-2 h-4 w-4 animate-spin' />
          )}
          {isEditing ? 'Update Plan' : 'Create Plan'}
        </Button>
      </div>
    </form>
  );
};
