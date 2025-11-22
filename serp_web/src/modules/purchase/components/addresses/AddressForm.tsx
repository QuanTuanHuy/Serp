/*
Author: QuanTuanHuy
Description: Part of Serp Project - Address form component
*/

'use client';

import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Textarea } from '@/shared/components/ui/textarea';
import { Checkbox } from '@/shared/components/ui/checkbox';
import type { Address } from '../../types';

// Form validation schema
const addressFormSchema = z.object({
  entityId: z.string().min(1, 'Mã thực thể là bắt buộc'),
  entityType: z.string().min(1, 'Loại thực thể là bắt buộc'),
  addressType: z.string().min(1, 'Loại địa chỉ là bắt buộc'),
  fullAddress: z.string().min(1, 'Địa chỉ chi tiết là bắt buộc'),
  latitude: z.number(),
  longitude: z.number(),
  isDefault: z.boolean(),
});

type AddressFormValues = z.infer<typeof addressFormSchema>;

interface AddressFormProps {
  address?: Address;
  onSubmit: (data: AddressFormValues) => void;
  onCancel: () => void;
  isSubmitting?: boolean;
  mode: 'create' | 'edit' | 'view';
}

export function AddressForm({
  address,
  onSubmit,
  onCancel,
  isSubmitting = false,
  mode,
}: AddressFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
  } = useForm<AddressFormValues>({
    resolver: zodResolver(addressFormSchema),
    defaultValues: {
      entityId: address?.entityId || '',
      entityType: address?.entityType || '',
      addressType: address?.addressType || '',
      fullAddress: address?.fullAddress || '',
      latitude: address?.latitude || 0,
      longitude: address?.longitude || 0,
      isDefault: address?.isDefault || false,
    },
  });

  const entityType = watch('entityType');
  const addressType = watch('addressType');
  const isDefault = watch('isDefault');

  // Update form when address changes
  useEffect(() => {
    if (address) {
      setValue('entityId', address.entityId);
      setValue('entityType', address.entityType);
      setValue('addressType', address.addressType);
      setValue('fullAddress', address.fullAddress);
      setValue('latitude', address.latitude);
      setValue('longitude', address.longitude);
      setValue('isDefault', address.isDefault);
    }
  }, [address, setValue]);

  const isViewMode = mode === 'view';

  return (
    <form onSubmit={handleSubmit(onSubmit)} className='space-y-6'>
      <div className='grid grid-cols-2 gap-4'>
        {/* Entity ID */}
        <div className='space-y-2'>
          <Label htmlFor='entityId'>
            Mã thực thể <span className='text-red-500'>*</span>
          </Label>
          <Input
            id='entityId'
            {...register('entityId')}
            disabled={isViewMode}
            placeholder='Nhập mã thực thể'
          />
          {errors.entityId && (
            <p className='text-sm text-red-500'>{errors.entityId.message}</p>
          )}
        </div>

        {/* Entity Type */}
        <div className='space-y-2'>
          <Label htmlFor='entityType'>
            Loại thực thể <span className='text-red-500'>*</span>
          </Label>
          <Select
            value={entityType}
            onValueChange={(value) => setValue('entityType', value)}
            disabled={isViewMode}
          >
            <SelectTrigger>
              <SelectValue placeholder='Chọn loại thực thể' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='supplier'>Nhà cung cấp</SelectItem>
              <SelectItem value='customer'>Khách hàng</SelectItem>
              <SelectItem value='facility'>Cơ sở</SelectItem>
            </SelectContent>
          </Select>
          {errors.entityType && (
            <p className='text-sm text-red-500'>{errors.entityType.message}</p>
          )}
        </div>
      </div>

      {/* Address Type */}
      <div className='space-y-2'>
        <Label htmlFor='addressType'>
          Loại địa chỉ <span className='text-red-500'>*</span>
        </Label>
        <Select
          value={addressType}
          onValueChange={(value) => setValue('addressType', value)}
          disabled={isViewMode}
        >
          <SelectTrigger>
            <SelectValue placeholder='Chọn loại địa chỉ' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='shipping'>Địa chỉ giao hàng</SelectItem>
            <SelectItem value='billing'>Địa chỉ thanh toán</SelectItem>
            <SelectItem value='office'>Địa chỉ văn phòng</SelectItem>
            <SelectItem value='warehouse'>Địa chỉ kho</SelectItem>
          </SelectContent>
        </Select>
        {errors.addressType && (
          <p className='text-sm text-red-500'>{errors.addressType.message}</p>
        )}
      </div>

      {/* Full Address */}
      <div className='space-y-2'>
        <Label htmlFor='fullAddress'>
          Địa chỉ chi tiết <span className='text-red-500'>*</span>
        </Label>
        <Textarea
          id='fullAddress'
          {...register('fullAddress')}
          disabled={isViewMode}
          placeholder='Nhập địa chỉ đầy đủ (số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố)'
          rows={3}
        />
        {errors.fullAddress && (
          <p className='text-sm text-red-500'>{errors.fullAddress.message}</p>
        )}
      </div>

      <div className='grid grid-cols-2 gap-4'>
        {/* Latitude */}
        <div className='space-y-2'>
          <Label htmlFor='latitude'>
            Vĩ độ <span className='text-red-500'>*</span>
          </Label>
          <Input
            id='latitude'
            type='number'
            step='any'
            {...register('latitude', { valueAsNumber: true })}
            disabled={isViewMode}
            placeholder='10.8231'
          />
          {errors.latitude && (
            <p className='text-sm text-red-500'>{errors.latitude.message}</p>
          )}
        </div>

        {/* Longitude */}
        <div className='space-y-2'>
          <Label htmlFor='longitude'>
            Kinh độ <span className='text-red-500'>*</span>
          </Label>
          <Input
            id='longitude'
            type='number'
            step='any'
            {...register('longitude', { valueAsNumber: true })}
            disabled={isViewMode}
            placeholder='106.6297'
          />
          {errors.longitude && (
            <p className='text-sm text-red-500'>{errors.longitude.message}</p>
          )}
        </div>
      </div>

      {/* Is Default */}
      <div className='flex items-center space-x-2'>
        <Checkbox
          id='isDefault'
          checked={isDefault}
          onCheckedChange={(checked) =>
            setValue('isDefault', checked as boolean)
          }
          disabled={isViewMode}
        />
        <Label htmlFor='isDefault' className='cursor-pointer'>
          Đặt làm địa chỉ mặc định
        </Label>
      </div>

      {/* Actions */}
      {!isViewMode && (
        <div className='flex justify-end gap-3 pt-4'>
          <Button
            type='button'
            variant='outline'
            onClick={onCancel}
            disabled={isSubmitting}
          >
            Hủy
          </Button>
          <Button type='submit' disabled={isSubmitting}>
            {isSubmitting
              ? 'Đang xử lý...'
              : mode === 'create'
                ? 'Tạo địa chỉ'
                : 'Cập nhật'}
          </Button>
        </div>
      )}
    </form>
  );
}
