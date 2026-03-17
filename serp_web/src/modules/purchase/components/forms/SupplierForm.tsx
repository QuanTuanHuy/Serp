// Purchase SupplierForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  Supplier,
  SupplierCreationForm,
  SupplierUpdateForm,
  SupplierStatus,
  AddressType,
} from '../../types';
import { toast } from 'sonner';

const supplierSchema = z.object({
  name: z.string().min(1, 'Tên là bắt buộc').max(255, 'Tên quá dài'),
  email: z.string().email('Email không hợp lệ').optional().or(z.literal('')),
  phone: z.string().optional(),
  statusId: z.enum(['ACTIVE', 'INACTIVE']),
  // Creation only
  addressType: z.enum(['FACILITY', 'SHIPPING', 'BUSINESS']).optional(),
  fullAddress: z.string().optional(),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
});

type SupplierFormData = z.infer<typeof supplierSchema>;

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

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    watch,
  } = useForm<SupplierFormData>({
    resolver: zodResolver(supplierSchema),
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
                    defaultValue='BUSSINESS'
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
                  <Input
                    id='fullAddress'
                    {...register('fullAddress')}
                    disabled={isLoading || isSubmitting}
                    placeholder='Nhập địa chỉ đầy đủ'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='latitude'>Vĩ độ</Label>
                  <Input
                    id='latitude'
                    type='number'
                    step='any'
                    {...register('latitude')}
                    disabled={isLoading || isSubmitting}
                    placeholder='VD: 10.7769'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='longitude'>Kinh độ</Label>
                  <Input
                    id='longitude'
                    type='number'
                    step='any'
                    {...register('longitude')}
                    disabled={isLoading || isSubmitting}
                    placeholder='VD: 106.7009'
                  />
                </div>
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
