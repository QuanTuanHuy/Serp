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
  CardHeader,
  CardTitle,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Switch,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  Territory,
  CreateTerritoryRequest,
  UpdateTerritoryRequest,
  TerritoryLevel,
} from '../../types';

const territorySchema = z.object({
  territoryCode: z
    .string()
    .min(1, 'Territory code is required')
    .max(50, 'Code is too long'),
  territoryName: z
    .string()
    .min(1, 'Territory name is required')
    .max(255, 'Name is too long'),
  territoryLevel: z.enum(['PROVINCE_CITY']),
  countryCode: z
    .string()
    .min(1, 'Country code is required')
    .max(10, 'Country code is too long'),
  parentTerritoryCode: z.string().optional(),
  active: z.boolean(),
});

interface TerritoryFormData {
  territoryCode: string;
  territoryName: string;
  territoryLevel: TerritoryLevel;
  countryCode: string;
  parentTerritoryCode?: string;
  active: boolean;
}

interface TerritoryFormProps {
  territory?: Territory;
  onSubmit: (
    data: CreateTerritoryRequest | UpdateTerritoryRequest
  ) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const TerritoryForm: React.FC<TerritoryFormProps> = ({
  territory,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!territory;

  const territoryForm = useForm<TerritoryFormData>({
    resolver: zodResolver(territorySchema) as any,
    defaultValues: territory
      ? {
          territoryCode: territory.territoryCode,
          territoryName: territory.territoryName,
          territoryLevel: territory.territoryLevel,
          countryCode: territory.countryCode,
          parentTerritoryCode: territory.parentTerritoryCode || '',
          active: territory.active,
        }
      : {
          territoryCode: '',
          territoryName: '',
          territoryLevel: 'PROVINCE_CITY' as TerritoryLevel,
          countryCode: 'VN',
          parentTerritoryCode: '',
          active: true,
        },
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    watch,
  } = territoryForm;

  const onFormSubmit = handleSubmit(async (data) => {
    try {
      await onSubmit({
        ...data,
        parentTerritoryCode: data.parentTerritoryCode || undefined,
      });
    } catch (error) {
      console.error('Form submission error:', error);
    }
  });

  return (
    <Card className={cn('w-full', className)}>
      <CardHeader className='pb-4'>
        <CardTitle className='text-xl'>
          {isEditing ? 'Edit Territory' : 'Create Territory'}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          <div className='space-y-4'>
            <h3 className='text-base font-medium text-foreground'>
              Basic Information
            </h3>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='territoryCode'>Territory Code *</Label>
                <Input
                  id='territoryCode'
                  {...register('territoryCode')}
                  className={cn(errors.territoryCode && 'border-destructive')}
                  disabled={isLoading || isEditing}
                  placeholder='e.g. VN-HN'
                />
                {errors.territoryCode && (
                  <p className='text-sm text-destructive'>
                    {errors.territoryCode.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='territoryName'>Territory Name *</Label>
                <Input
                  id='territoryName'
                  {...register('territoryName')}
                  className={cn(errors.territoryName && 'border-destructive')}
                  disabled={isLoading}
                  placeholder='e.g. Hanoi'
                />
                {errors.territoryName && (
                  <p className='text-sm text-destructive'>
                    {errors.territoryName.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='territoryLevel'>Level *</Label>
                <Select
                  value={watch('territoryLevel')}
                  onValueChange={(value) =>
                    setValue('territoryLevel', value as TerritoryLevel)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select level' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='PROVINCE_CITY'>Province/City</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='countryCode'>Country Code *</Label>
                <Input
                  id='countryCode'
                  {...register('countryCode')}
                  className={cn(errors.countryCode && 'border-destructive')}
                  disabled={isLoading}
                  placeholder='e.g. VN'
                />
                {errors.countryCode && (
                  <p className='text-sm text-destructive'>
                    {errors.countryCode.message}
                  </p>
                )}
              </div>

              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='parentTerritoryCode'>
                  Parent Territory Code
                </Label>
                <Input
                  id='parentTerritoryCode'
                  {...register('parentTerritoryCode')}
                  disabled={isLoading}
                  placeholder='Optional parent territory code'
                />
              </div>

              <div className='space-y-2 md:col-span-2'>
                <div className='flex items-center justify-between'>
                  <Label htmlFor='active'>Active Status</Label>
                  <Switch
                    id='active'
                    checked={watch('active')}
                    onCheckedChange={(checked) => setValue('active', checked)}
                    disabled={isLoading}
                  />
                </div>
              </div>
            </div>
          </div>

          <div className='flex justify-end gap-3 pt-6 border-t'>
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
              {isSubmitting
                ? 'Saving...'
                : isEditing
                  ? 'Update Territory'
                  : 'Create Territory'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default TerritoryForm;
