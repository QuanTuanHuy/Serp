'use client';

import { useState } from 'react';
import { useForm, type Resolver } from 'react-hook-form';
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
} from '@/shared/components/ui';
import { MapLocationPicker } from '@/shared/components';
import { cn } from '@/shared/utils';
import type {
  Supplier,
  SupplierCreationForm,
  SupplierUpdateForm,
  SupplierStatus,
  AddressType,
} from '../../types';
import { toast } from 'sonner';

const baseSupplierSchema = z.object({
  name: z.string().min(1, 'Tên là bắt buộc').max(255, 'Tên quá dài'),
  email: z.string().email('Email không hợp lệ').optional().or(z.literal('')),
  phone: z.string().optional(),
  statusId: z.enum(['ACTIVE', 'INACTIVE']),
});

const updateSupplierSchema = baseSupplierSchema;

const createSupplierSchema = baseSupplierSchema.extend({
  addressType: z.enum(['FACILITY', 'SHIPPING', 'BUSINESS']),
  fullAddress: z.string().min(1, 'Địa chỉ đầy đủ là bắt buộc'),
  latitude: z.string().min(1, 'Vui lòng ghim vị trí trên bản đồ'),
  longitude: z.string().min(1, 'Vui lòng ghim vị trí trên bản đồ'),
});

type SupplierFormData = z.infer<typeof baseSupplierSchema> & {
  addressType?: AddressType;
  fullAddress?: string;
  latitude?: string;
  longitude?: string;
};

const DEFAULT_MAP_CENTER = {
  lat: 21.028511,
  lng: 105.804817,
};

interface SupplierFormProps {
  supplier?: Supplier;
  onSubmit: (data: SupplierCreationForm | SupplierUpdateForm) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const SupplierForm: React.FC<SupplierFormProps> = ({
  supplier,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!supplier;
  const [showMap, setShowMap] = useState(false);
  const [mapCenter, setMapCenter] = useState(DEFAULT_MAP_CENTER);
  const [isGeocoding, setIsGeocoding] = useState(false);
  const activeSchema = isEditing ? updateSupplierSchema : createSupplierSchema;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    watch,
  } = useForm<SupplierFormData>({
    resolver: zodResolver(activeSchema) as Resolver<SupplierFormData>,
    defaultValues: supplier
      ? {
          name: supplier.name,
          email: supplier.email || '',
          phone: supplier.phone || '',
          statusId: supplier.statusId,
        }
      : {
          name: '',
          email: '',
          phone: '',
          statusId: 'ACTIVE' as SupplierStatus,
          addressType: 'BUSINESS' as AddressType,
          fullAddress: '',
          latitude: '',
          longitude: '',
        },
  });

  const statusId = watch('statusId');
  const fullAddress = watch('fullAddress');
  const hasLocationError = Boolean(errors.latitude || errors.longitude);

  const handleGeocodeAndShowMap = async () => {
    if (!fullAddress?.trim()) {
      toast.error('Vui lòng nhập địa chỉ đầy đủ trước');
      return;
    }

    setIsGeocoding(true);

    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(fullAddress)}`
      );
      const data: Array<{ lat: string; lon: string }> = await response.json();

      if (data && data.length > 0) {
        const lat = Number.parseFloat(data[0].lat);
        const lng = Number.parseFloat(data[0].lon);

        setMapCenter({ lat, lng });
        setValue('latitude', lat.toString(), {
          shouldDirty: true,
          shouldValidate: true,
        });
        setValue('longitude', lng.toString(), {
          shouldDirty: true,
          shouldValidate: true,
        });
      } else {
        toast.error(
          'Không tìm thấy tọa độ cho địa chỉ này, vui lòng ghim thủ công.'
        );
        setMapCenter(DEFAULT_MAP_CENTER);
      }

      setShowMap(true);
    } catch (error) {
      console.error('Lỗi khi tìm tọa độ:', error);
      toast.error('Lỗi khi tìm tọa độ');
      setShowMap(true);
    } finally {
      setIsGeocoding(false);
    }
  };

  const onFormSubmit = handleSubmit(async (data: SupplierFormData) => {
    try {
      if (isEditing) {
        const updateData: SupplierUpdateForm = {
          name: data.name,
          email: data.email || undefined,
          phone: data.phone || undefined,
          statusId: data.statusId,
        };
        await onSubmit(updateData);
      } else {
        const createData: SupplierCreationForm = {
          name: data.name,
          email: data.email || undefined,
          phone: data.phone || undefined,
          statusId: data.statusId,
          addressType: data.addressType || 'BUSINESS',
          fullAddress: data.fullAddress || '',
          latitude: data.latitude ? parseFloat(data.latitude) : undefined,
          longitude: data.longitude ? parseFloat(data.longitude) : undefined,
        };
        await onSubmit(createData);
      }
    } catch (error) {
      console.error('Lỗi xảy ra khi gửi biểu mẫu:', error);
      toast.error('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  });

  return (
    <Card className={cn('w-full', className)}>
      <CardHeader className='pb-4'>
        <CardTitle className='text-xl'>
          {isEditing ? 'Chỉnh sửa nhà cung cấp' : 'Tạo nhà cung cấp mới'}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          {/* Basic Information */}
          <div className='space-y-4'>
            <h3 className='text-base font-medium text-foreground'>
              Thông tin cơ bản
            </h3>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='name'>Tên *</Label>
                <Input
                  id='name'
                  {...register('name')}
                  className={cn(errors.name && 'border-destructive')}
                  disabled={isLoading || isSubmitting}
                  placeholder='Nhập tên nhà cung cấp'
                />
                {errors.name && (
                  <p className='text-sm text-destructive'>
                    {errors.name.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='email'>Email</Label>
                <Input
                  id='email'
                  type='email'
                  {...register('email')}
                  className={cn(errors.email && 'border-destructive')}
                  disabled={isLoading || isSubmitting}
                  placeholder='email@example.com'
                />
                {errors.email && (
                  <p className='text-sm text-destructive'>
                    {errors.email.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='phone'>Số điện thoại</Label>
                <Input
                  id='phone'
                  {...register('phone')}
                  disabled={isLoading || isSubmitting}
                  placeholder='Nhập số điện thoại'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='statusId'>Trạng thái *</Label>
                <Select
                  value={statusId}
                  onValueChange={(value) =>
                    setValue('statusId', value as SupplierStatus)
                  }
                  disabled={isLoading || isSubmitting}
                >
                  <SelectTrigger id='statusId'>
                    <SelectValue placeholder='Chọn trạng thái' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='ACTIVE'>Hoạt động</SelectItem>
                    <SelectItem value='INACTIVE'>Không hoạt động</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {/* Address (creation only) */}
          {!isEditing && (
            <div className='space-y-4'>
              <h3 className='text-base font-medium text-foreground'>Địa chỉ</h3>
              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div className='space-y-2'>
                  <Label htmlFor='addressType'>Loại địa chỉ</Label>
                  <Select
                    defaultValue='BUSINESS'
                    onValueChange={(value) =>
                      setValue('addressType', value as AddressType)
                    }
                    disabled={isLoading || isSubmitting}
                  >
                    <SelectTrigger id='addressType'>
                      <SelectValue placeholder='Chọn loại địa chỉ' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='FACILITY'>Kho</SelectItem>
                      <SelectItem value='SHIPPING'>Giao hàng</SelectItem>
                      <SelectItem value='BUSINESS'>Cơ sở</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2 md:col-span-2'>
                  <Label htmlFor='fullAddress'>Địa chỉ đầy đủ</Label>
                  <div className='flex flex-col gap-2 md:flex-row'>
                    <Input
                      id='fullAddress'
                      {...register('fullAddress')}
                      className={cn(errors.fullAddress && 'border-destructive')}
                      disabled={isLoading || isSubmitting}
                      placeholder='Nhập địa chỉ đầy đủ (VD: Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội)'
                    />
                    <Button
                      type='button'
                      onClick={handleGeocodeAndShowMap}
                      disabled={isGeocoding || !fullAddress?.trim()}
                    >
                      {isGeocoding ? 'Đang tìm...' : 'Tìm trên bản đồ'}
                    </Button>
                  </div>
                  {errors.fullAddress && (
                    <p className='text-sm text-destructive'>
                      {errors.fullAddress.message}
                    </p>
                  )}
                </div>

                {showMap && (
                  <div className='space-y-2 md:col-span-2'>
                    <Label>
                      Kéo thả hoặc click trên bản đồ để chọn vị trí chính xác
                    </Label>
                    <MapLocationPicker
                      initialLat={mapCenter.lat}
                      initialLng={mapCenter.lng}
                      className={cn(hasLocationError && 'border-destructive')}
                      onLocationSelect={(lat, lng) => {
                        setValue('latitude', lat.toString(), {
                          shouldDirty: true,
                          shouldValidate: true,
                        });
                        setValue('longitude', lng.toString(), {
                          shouldDirty: true,
                          shouldValidate: true,
                        });
                      }}
                    />
                    {hasLocationError && (
                      <p className='text-sm text-destructive'>
                        {errors.latitude?.message ||
                          errors.longitude?.message ||
                          'Vui lòng ghim vị trí trên bản đồ'}
                      </p>
                    )}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Form Actions */}
          <div className='flex items-center justify-end gap-3 pt-4 border-t'>
            <Button
              type='button'
              variant='outline'
              onClick={onCancel}
              disabled={isLoading || isSubmitting}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isLoading || isSubmitting}>
              {isLoading || isSubmitting
                ? 'Đang lưu...'
                : isEditing
                  ? 'Cập nhật nhà cung cấp'
                  : 'Tạo nhà cung cấp'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default SupplierForm;
