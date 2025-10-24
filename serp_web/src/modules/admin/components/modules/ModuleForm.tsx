/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module Form Component
 */

'use client';

import { useForm } from 'react-hook-form';
import {
  Button,
  Input,
  Label,
  Card,
  CardContent,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { Loader2 } from 'lucide-react';
import type {
  Module,
  ModuleStatus,
  ModuleType,
  PricingModel,
} from '../../types';

interface ModuleFormProps {
  module?: Module;
  onSubmit: (
    data: Omit<Module, 'id' | 'createdAt' | 'updatedAt'>
  ) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

interface ModuleFormData {
  name: string;
  code: string;
  description?: string;
  keycloakClientId?: string;
  category?: string;
  icon?: string;
  displayOrder?: number | string;
  moduleType: ModuleType;
  isGlobal: boolean;
  organizationId?: number | string;
  isFree: boolean;
  pricingModel: PricingModel;
  status: ModuleStatus;
  version?: string;
}

export const ModuleForm: React.FC<ModuleFormProps> = ({
  module,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!module;

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<ModuleFormData>({
    defaultValues: module
      ? {
          name: module.name,
          code: module.code,
          description: module.description || '',
          keycloakClientId: module.keycloakClientId || '',
          category: module.category || '',
          icon: module.icon || '',
          displayOrder: module.displayOrder || 0,
          moduleType: module.moduleType,
          isGlobal: module.isGlobal,
          organizationId: module.organizationId || undefined,
          isFree: module.isFree,
          pricingModel: module.pricingModel,
          status: module.status,
          version: module.version || '',
        }
      : {
          name: '',
          code: '',
          description: '',
          keycloakClientId: '',
          category: '',
          icon: '',
          displayOrder: 0,
          moduleType: 'SYSTEM',
          isGlobal: true,
          organizationId: undefined,
          isFree: true,
          pricingModel: 'FREE',
          status: 'ACTIVE',
          version: '',
        },
  });

  const onSubmitInternal = handleSubmit(async (data) => {
    try {
      const transformed = {
        ...data,
        displayOrder: Number(data.displayOrder) || 0,
        organizationId: data.organizationId
          ? Number(data.organizationId)
          : undefined,
      } as any;
      await onSubmit(transformed);
    } catch (e) {
      // handled by caller
    }
  });

  return (
    <form onSubmit={onSubmitInternal} className={cn('space-y-6', className)}>
      {/* Basic Information */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Basic Information</h3>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='name'>
                Module Name <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='name'
                placeholder='e.g. CRM'
                {...register('name')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.name && 'border-destructive')}
              />
              {errors.name && (
                <p className='text-sm text-destructive'>
                  {errors.name.message}
                </p>
              )}
            </div>
            <div className='space-y-2'>
              <Label htmlFor='code'>
                Code <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='code'
                placeholder='e.g. CRM'
                {...register('code')}
                disabled={isLoading || isSubmitting || isEditing}
                className={cn('uppercase', errors.code && 'border-destructive')}
              />
              <p className='text-xs text-muted-foreground'>
                Uppercase alphanumeric with underscores only
              </p>
              {errors.code && (
                <p className='text-sm text-destructive'>
                  {errors.code.message}
                </p>
              )}
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='description'>Description</Label>
            <textarea
              id='description'
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

      {/* Settings */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Settings</h3>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label>Module Type</Label>
              <Select
                onValueChange={(v) => setValue('moduleType', v as ModuleType)}
                defaultValue={module?.moduleType || 'SYSTEM'}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select type' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='SYSTEM'>SYSTEM</SelectItem>
                  <SelectItem value='CUSTOM'>CUSTOM</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label>Status</Label>
              <Select
                onValueChange={(v) => setValue('status', v as ModuleStatus)}
                defaultValue={module?.status || 'ACTIVE'}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select status' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ACTIVE'>ACTIVE</SelectItem>
                  <SelectItem value='BETA'>BETA</SelectItem>
                  <SelectItem value='DEPRECATED'>DEPRECATED</SelectItem>
                  <SelectItem value='MAINTENANCE'>MAINTENANCE</SelectItem>
                  <SelectItem value='DISABLED'>DISABLED</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label>Pricing Model</Label>
              <Select
                onValueChange={(v) =>
                  setValue('pricingModel', v as PricingModel)
                }
                defaultValue={module?.pricingModel || 'FREE'}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select pricing' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='FREE'>FREE</SelectItem>
                  <SelectItem value='FIXED'>FIXED</SelectItem>
                  <SelectItem value='PER_USER'>PER_USER</SelectItem>
                  <SelectItem value='TIERED'>TIERED</SelectItem>
                </SelectContent>
              </Select>
            </div>
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
              {errors.displayOrder && (
                <p className='text-sm text-destructive'>
                  {errors.displayOrder.message}
                </p>
              )}
            </div>
          </div>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='keycloakClientId'>Keycloak Client ID</Label>
              <Input
                id='keycloakClientId'
                {...register('keycloakClientId')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.keycloakClientId && 'border-destructive')}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='category'>Category</Label>
              <Input
                id='category'
                {...register('category')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.category && 'border-destructive')}
              />
            </div>
          </div>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='icon'>Icon</Label>
              <Input
                id='icon'
                {...register('icon')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.icon && 'border-destructive')}
              />
            </div>
            <div className='space-y-2'>
              <Label className='text-base'>Global Module</Label>
              <div className='flex items-center justify-between rounded-lg border p-3'>
                <span className='text-sm text-muted-foreground'>
                  Available to all organizations
                </span>
                <input
                  type='checkbox'
                  {...register('isGlobal')}
                  disabled={isLoading || isSubmitting || isEditing}
                  className='h-4 w-4 rounded border-gray-300 text-primary focus:ring-2 focus:ring-primary'
                />
              </div>
            </div>
          </div>

          {/* Organization ID conditional */}
          <div className='space-y-2'>
            <Label htmlFor='organizationId'>Organization ID</Label>
            <Input
              id='organizationId'
              type='number'
              min='1'
              placeholder='Enter organization ID'
              {...register('organizationId')}
              disabled={isLoading || isSubmitting || isEditing}
              className={cn(errors.organizationId && 'border-destructive')}
            />
            {errors.organizationId && (
              <p className='text-sm text-destructive'>
                {errors.organizationId.message}
              </p>
            )}
          </div>

          <div className='space-y-2'>
            <Label className='text-base'>Free Module</Label>
            <div className='flex items-center justify-between rounded-lg border p-3'>
              <span className='text-sm text-muted-foreground'>
                No payment required
              </span>
              <input
                type='checkbox'
                {...register('isFree')}
                disabled={isLoading || isSubmitting}
                className='h-4 w-4 rounded border-gray-300 text-primary focus:ring-2 focus:ring-primary'
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='version'>Version</Label>
            <Input
              id='version'
              {...register('version')}
              disabled={isLoading || isSubmitting}
              className={cn(errors.version && 'border-destructive')}
            />
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
          {isEditing ? 'Update Module' : 'Create Module'}
        </Button>
      </div>
    </form>
  );
};
