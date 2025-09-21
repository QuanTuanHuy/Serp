// CustomerForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  Customer,
  CreateCustomerRequest,
  UpdateCustomerRequest,
  CustomerType,
  CustomerStatus,
} from '../../types';

// Validation schema
const customerSchema = z.object({
  name: z.string().min(1, 'Name is required').max(255, 'Name is too long'),
  email: z.string().email('Invalid email address'),
  phone: z.string().optional(),
  address: z.string().optional(),
  customerType: z.enum(['INDIVIDUAL', 'COMPANY']),
  status: z.enum(['ACTIVE', 'INACTIVE', 'POTENTIAL', 'BLOCKED']),
  companyName: z.string().optional(),
  taxNumber: z.string().optional(),
  website: z.string().url('Invalid website URL').optional().or(z.literal('')),
  notes: z.string().optional(),
  assignedSalesRep: z.string().optional(),
  tags: z.array(z.string()),
});

type CustomerFormData = z.infer<typeof customerSchema>;

interface CustomerFormProps {
  customer?: Customer;
  onSubmit: (
    data: CreateCustomerRequest | UpdateCustomerRequest
  ) => Promise<void>;
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
    watch,
    formState: { errors, isSubmitting },
    setValue,
    getValues,
  } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
    defaultValues: customer
      ? {
          name: customer.name,
          email: customer.email,
          phone: customer.phone || '',
          address: customer.address || '',
          customerType: customer.customerType,
          status: customer.status,
          companyName: customer.companyName || '',
          taxNumber: customer.taxNumber || '',
          website: customer.website || '',
          notes: customer.notes || '',
          assignedSalesRep: customer.assignedSalesRep || '',
          tags: customer.tags || [],
        }
      : {
          name: '',
          email: '',
          phone: '',
          address: '',
          customerType: 'INDIVIDUAL' as CustomerType,
          status: 'POTENTIAL' as CustomerStatus,
          companyName: '',
          taxNumber: '',
          website: '',
          notes: '',
          assignedSalesRep: '',
          tags: [],
        },
  });

  const customerType = watch('customerType');

  // Handle form submission
  const onFormSubmit = handleSubmit(async (data: CustomerFormData) => {
    try {
      await onSubmit(data);
    } catch (error) {
      console.error('Form submission error:', error);
    }
  });

  // Handle tag management
  const handleTagAdd = (tag: string) => {
    if (tag.trim()) {
      const currentTags = getValues('tags');
      if (!currentTags.includes(tag.trim())) {
        setValue('tags', [...currentTags, tag.trim()]);
      }
    }
  };

  const handleTagRemove = (tagToRemove: string) => {
    const currentTags = getValues('tags');
    setValue(
      'tags',
      currentTags.filter((tag) => tag !== tagToRemove)
    );
  };

  return (
    <Card className={cn('w-full max-w-2xl mx-auto', className)}>
      <CardHeader>
        <h2 className='text-xl font-semibold'>
          {isEditing ? 'Edit Customer' : 'Create Customer'}
        </h2>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          {/* Basic Information */}
          <div className='space-y-4'>
            <h3 className='text-lg font-medium'>Basic Information</h3>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div>
                <Label htmlFor='name'>Name *</Label>
                <Input
                  id='name'
                  {...register('name')}
                  className={errors.name ? 'border-red-500' : ''}
                  disabled={isLoading}
                />
                {errors.name && (
                  <p className='text-sm text-red-600 mt-1'>
                    {errors.name.message}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor='email'>Email *</Label>
                <Input
                  id='email'
                  type='email'
                  {...register('email')}
                  className={errors.email ? 'border-red-500' : ''}
                  disabled={isLoading}
                />
                {errors.email && (
                  <p className='text-sm text-red-600 mt-1'>
                    {errors.email.message}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor='phone'>Phone</Label>
                <Input id='phone' {...register('phone')} disabled={isLoading} />
              </div>

              <div>
                <Label htmlFor='customerType'>Customer Type *</Label>
                <select
                  {...register('customerType')}
                  className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                  disabled={isLoading}
                >
                  <option value='INDIVIDUAL'>Individual</option>
                  <option value='COMPANY'>Company</option>
                </select>
              </div>

              <div>
                <Label htmlFor='status'>Status *</Label>
                <select
                  {...register('status')}
                  className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                  disabled={isLoading}
                >
                  <option value='POTENTIAL'>Potential</option>
                  <option value='ACTIVE'>Active</option>
                  <option value='INACTIVE'>Inactive</option>
                  <option value='BLOCKED'>Blocked</option>
                </select>
              </div>

              <div>
                <Label htmlFor='assignedSalesRep'>Assigned Sales Rep</Label>
                <Input
                  id='assignedSalesRep'
                  {...register('assignedSalesRep')}
                  disabled={isLoading}
                />
              </div>
            </div>

            <div>
              <Label htmlFor='address'>Address</Label>
              <Input
                id='address'
                {...register('address')}
                disabled={isLoading}
              />
            </div>
          </div>

          {/* Company Information */}
          {customerType === 'COMPANY' && (
            <div className='space-y-4'>
              <h3 className='text-lg font-medium'>Company Information</h3>

              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div>
                  <Label htmlFor='companyName'>Company Name</Label>
                  <Input
                    id='companyName'
                    {...register('companyName')}
                    disabled={isLoading}
                  />
                </div>

                <div>
                  <Label htmlFor='taxNumber'>Tax Number</Label>
                  <Input
                    id='taxNumber'
                    {...register('taxNumber')}
                    disabled={isLoading}
                  />
                </div>

                <div className='md:col-span-2'>
                  <Label htmlFor='website'>Website</Label>
                  <Input
                    id='website'
                    type='url'
                    {...register('website')}
                    className={errors.website ? 'border-red-500' : ''}
                    disabled={isLoading}
                  />
                  {errors.website && (
                    <p className='text-sm text-red-600 mt-1'>
                      {errors.website.message}
                    </p>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Additional Information */}
          <div className='space-y-4'>
            <h3 className='text-lg font-medium'>Additional Information</h3>

            <div>
              <Label htmlFor='notes'>Notes</Label>
              <textarea
                id='notes'
                {...register('notes')}
                rows={3}
                className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                disabled={isLoading}
              />
            </div>

            {/* Tags */}
            <div>
              <Label>Tags</Label>
              <div className='flex flex-wrap gap-2 mt-2'>
                {watch('tags').map((tag, index) => (
                  <span
                    key={index}
                    className='inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800'
                  >
                    {tag}
                    <button
                      type='button'
                      onClick={() => handleTagRemove(tag)}
                      className='ml-1 text-blue-600 hover:text-blue-800'
                      disabled={isLoading}
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
              <div className='flex mt-2'>
                <Input
                  placeholder='Add tag and press Enter'
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleTagAdd(e.currentTarget.value);
                      e.currentTarget.value = '';
                    }
                  }}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className='flex justify-end space-x-3 pt-6 border-t'>
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
