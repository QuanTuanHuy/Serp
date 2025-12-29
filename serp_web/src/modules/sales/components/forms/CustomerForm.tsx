// CustomerForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
  Customer,
  CustomerCreationForm,
  CustomerUpdateForm,
  CustomerStatus,
  AddressType,
} from '../../types';
import { toast } from 'sonner';

// Validation schema
const customerSchema = z.object({
  name: z.string().min(1, 'Name is required').max(255, 'Name is too long'),
  email: z.string().email('Invalid email address').optional().or(z.literal('')),
  phone: z.string().optional(),
  statusId: z.enum(['ACTIVE', 'INACTIVE']),
  addressType: z.enum(['FACILIY', 'SHIPPING', 'BUSSINESS']).optional(),
});

type CustomerFormData = z.infer<typeof customerSchema>;

interface CustomerFormProps {
  customer?: Customer;
  onSubmit: (data: CustomerCreationForm | CustomerUpdateForm) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const CustomerForm: React.FC<CustomerFormProps> = ({
  customer,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!customer;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    watch,
  } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
    defaultValues: customer
      ? {
          name: customer.name,
          email: customer.email || '',
          phone: customer.phone || '',
          statusId: customer.statusId,
          addressType: 'SHIPPING' as AddressType,
        }
      : {
          name: '',
          email: '',
          phone: '',
          statusId: 'ACTIVE' as CustomerStatus,
          addressType: 'SHIPPING' as AddressType,
        },
  });

  const statusId = watch('statusId');

  // Handle form submission
  const onFormSubmit = handleSubmit(async (data: CustomerFormData) => {
    try {
      if (isEditing) {
        // For update, only send changed fields
        const updateData: CustomerUpdateForm = {
          name: data.name,
          email: data.email || undefined,
          phone: data.phone || undefined,
          statusId: data.statusId,
        };
        await onSubmit(updateData);
      } else {
        // For creation, send all required fields
        const createData: CustomerCreationForm = {
          name: data.name,
          email: data.email || undefined,
          phone: data.phone || undefined,
          statusId: data.statusId,
          addressType: data.addressType || 'SHIPPING',
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
          {isEditing ? 'Chỉnh sửa khách hàng' : 'Tạo khách hàng mới'}
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
                  placeholder='Nhập tên khách hàng'
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
                    setValue('statusId', value as CustomerStatus)
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

              {!isEditing && (
                <div className='space-y-2'>
                  <Label htmlFor='addressType'>Loại địa chỉ</Label>
                  <Select
                    defaultValue='SHIPPING'
                    onValueChange={(value) =>
                      setValue('addressType', value as AddressType)
                    }
                    disabled={isLoading || isSubmitting}
                  >
                    <SelectTrigger id='addressType'>
                      <SelectValue placeholder='Chọn loại địa chỉ' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='FACILIY'>Cơ sở</SelectItem>
                      <SelectItem value='SHIPPING'>Giao hàng</SelectItem>
                      <SelectItem value='BUSSINESS'>Kinh doanh</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}
            </div>
          </div>

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
                  ? 'Cập nhật khách hàng'
                  : 'Tạo khách hàng'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default CustomerForm;
