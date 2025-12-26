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
      console.error('Form submission error:', error);
    }
  });

  return (
    <Card className={cn('w-full', className)}>
      <CardHeader className='pb-4'>
        <CardTitle className='text-xl'>
          {isEditing ? 'Edit Customer' : 'Create Customer'}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          {/* Basic Information */}
          <div className='space-y-4'>
            <h3 className='text-base font-medium text-foreground'>
              Basic Information
            </h3>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='name'>Name *</Label>
                <Input
                  id='name'
                  {...register('name')}
                  className={cn(errors.name && 'border-destructive')}
                  disabled={isLoading || isSubmitting}
                  placeholder='Enter customer name'
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
                <Label htmlFor='phone'>Phone</Label>
                <Input
                  id='phone'
                  {...register('phone')}
                  disabled={isLoading || isSubmitting}
                  placeholder='+84 xxx xxx xxx'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='statusId'>Status *</Label>
                <Select
                  value={statusId}
                  onValueChange={(value) =>
                    setValue('statusId', value as CustomerStatus)
                  }
                  disabled={isLoading || isSubmitting}
                >
                  <SelectTrigger id='statusId'>
                    <SelectValue placeholder='Select status' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='ACTIVE'>Active</SelectItem>
                    <SelectItem value='INACTIVE'>Inactive</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {!isEditing && (
                <div className='space-y-2'>
                  <Label htmlFor='addressType'>Address Type</Label>
                  <Select
                    defaultValue='SHIPPING'
                    onValueChange={(value) =>
                      setValue('addressType', value as AddressType)
                    }
                    disabled={isLoading || isSubmitting}
                  >
                    <SelectTrigger id='addressType'>
                      <SelectValue placeholder='Select address type' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='FACILIY'>Facility</SelectItem>
                      <SelectItem value='SHIPPING'>Shipping</SelectItem>
                      <SelectItem value='BUSSINESS'>Business</SelectItem>
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
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading || isSubmitting}>
              {isLoading || isSubmitting
                ? 'Saving...'
                : isEditing
                  ? 'Update Customer'
                  : 'Create Customer'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default CustomerForm;
