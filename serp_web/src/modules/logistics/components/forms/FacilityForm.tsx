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
  Facility,
  FacilityCreationForm,
  FacilityUpdateForm,
  FacilityStatus,
  AddressType,
} from '../../types';
import { toast } from 'sonner';

const baseFacilitySchema = z.object({
  name: z.string().min(1, 'Tên là bắt buộc').max(255, 'Tên quá dài'),
  phone: z.string().optional(),
  statusId: z.enum(['ACTIVE', 'INACTIVE']),
  postalCode: z.string().optional(),
  length: z.string().optional(),
  width: z.string().optional(),
  height: z.string().optional(),
});

const updateFacilitySchema = baseFacilitySchema;

const createFacilitySchema = baseFacilitySchema.extend({
  addressType: z.enum(['FACILITY', 'SHIPPING', 'BUSINESS']),
  fullAddress: z.string().min(1, 'Địa chỉ đầy đủ là bắt buộc'),
  latitude: z.string().min(1, 'Vui lòng ghim vị trí trên bản đồ'),
  longitude: z.string().min(1, 'Vui lòng ghim vị trí trên bản đồ'),
});

type FacilityFormData = z.infer<typeof baseFacilitySchema> & {
  addressType?: AddressType;
  fullAddress?: string;
  latitude?: string;
  longitude?: string;
};

const DEFAULT_MAP_CENTER = {
  lat: 16.047079,
  lng: 108.20623,
};

interface FacilityFormProps {
  facility?: Facility;
  onSubmit: (data: FacilityCreationForm | FacilityUpdateForm) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const FacilityForm: React.FC<FacilityFormProps> = ({
  facility,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!facility;
  const [showMap, setShowMap] = useState(false);
  const [mapCenter, setMapCenter] = useState(DEFAULT_MAP_CENTER);
  const [isGeocoding, setIsGeocoding] = useState(false);
  const activeSchema = isEditing ? updateFacilitySchema : createFacilitySchema;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    watch,
  } = useForm<FacilityFormData>({
    resolver: zodResolver(activeSchema) as Resolver<FacilityFormData>,
    defaultValues: facility
      ? {
          name: facility.name,
          phone: facility.phone || '',
          statusId: facility.statusId,
          postalCode: facility.postalCode || '',
          length: facility.length?.toString() || '',
          width: facility.width?.toString() || '',
          height: facility.height?.toString() || '',
        }
      : {
          name: '',
          phone: '',
          statusId: 'ACTIVE' as FacilityStatus,
          postalCode: '',
          length: '',
          width: '',
          height: '',
          addressType: 'FACILITY' as AddressType,
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

  const onFormSubmit = handleSubmit(async (data: FacilityFormData) => {
    try {
      if (isEditing) {
        const updateData: FacilityUpdateForm = {
          name: data.name,
          phone: data.phone || undefined,
          statusId: data.statusId as FacilityStatus,
          postalCode: data.postalCode || undefined,
          length: data.length ? parseFloat(data.length) : undefined,
          width: data.width ? parseFloat(data.width) : undefined,
          height: data.height ? parseFloat(data.height) : undefined,
        };
        await onSubmit(updateData);
      } else {
        const createData: FacilityCreationForm = {
          name: data.name,
          phone: data.phone || undefined,
          statusId: data.statusId,
          postalCode: data.postalCode || '',
          length: data.length ? parseFloat(data.length) : undefined,
          width: data.width ? parseFloat(data.width) : undefined,
          height: data.height ? parseFloat(data.height) : undefined,
          addressType: data.addressType || 'FACILITY',
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
          {isEditing ? 'Chỉnh sửa kho hàng' : 'Tạo kho hàng mới'}
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
                  placeholder='Nhập tên kho hàng'
                />
                {errors.name && (
                  <p className='text-sm text-destructive'>
                    {errors.name.message}
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
                    setValue('statusId', value as FacilityStatus)
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

              <div className='space-y-2'>
                <Label htmlFor='postalCode'>Mã bưu chính</Label>
                <Input
                  id='postalCode'
                  {...register('postalCode')}
                  disabled={isLoading || isSubmitting}
                  placeholder='Nhập mã bưu chính'
                />
              </div>
            </div>
          </div>

          {/* Dimensions */}
          <div className='space-y-4'>
            <h3 className='text-base font-medium text-foreground'>
              Kích thước
            </h3>
            <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='length'>Chiều dài (m)</Label>
                <Input
                  id='length'
                  type='number'
                  step='any'
                  {...register('length')}
                  disabled={isLoading || isSubmitting}
                  placeholder='VD: 20'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='width'>Chiều rộng (m)</Label>
                <Input
                  id='width'
                  type='number'
                  step='any'
                  {...register('width')}
                  disabled={isLoading || isSubmitting}
                  placeholder='VD: 10'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='height'>Chiều cao (m)</Label>
                <Input
                  id='height'
                  type='number'
                  step='any'
                  {...register('height')}
                  disabled={isLoading || isSubmitting}
                  placeholder='VD: 5'
                />
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
                    defaultValue='FACILIY'
                    onValueChange={(value) =>
                      setValue('addressType', value as AddressType)
                    }
                    disabled={isLoading || isSubmitting}
                  >
                    <SelectTrigger id='addressType'>
                      <SelectValue placeholder='Chọn loại địa chỉ' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='FACILIY'>Kho</SelectItem>
                      <SelectItem value='SHIPPING'>Giao hàng</SelectItem>
                      <SelectItem value='BUSSINESS'>Cơ sở</SelectItem>
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
                  ? 'Cập nhật kho hàng'
                  : 'Tạo kho hàng'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default FacilityForm;
