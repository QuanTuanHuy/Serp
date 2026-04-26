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
  Textarea,
  Badge,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { X } from 'lucide-react';
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
  city: z.string().optional(),
  state: z.string().optional(),
  zipCode: z.string().optional(),
  country: z.string().optional(),
  customerType: z.enum(['PROSPECT', 'CUSTOMER']),
  status: z.enum(['ACTIVE', 'INACTIVE']),
  companySize: z.string().optional(),
  taxNumber: z.string().optional(),
  website: z.string().url('Invalid website URL').optional().or(z.literal('')),
  paymentTerms: z.string().optional(),
  creditLimit: z.coerce.number().min(0).optional(),
  notes: z.string().optional(),
  tags: z.array(z.string()),
});

interface CustomerFormData {
  name: string;
  email: string;
  phone?: string;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  customerType: CustomerType;
  status: CustomerStatus;
  companySize?: string;
  taxNumber?: string;
  website?: string;
  paymentTerms?: string;
  creditLimit?: number;
  notes?: string;
  tags: string[];
}

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

  const customerForm = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema) as any,
    defaultValues: customer
      ? {
          name: customer.name,
          email: customer.email,
          phone: customer.phone || '',
          address: customer.address || '',
          city: customer.customFields.city || '',
          state: customer.customFields.state || '',
          zipCode: customer.customFields.zipCode || '',
          country: customer.customFields.country || '',
          customerType: customer.customerType,
          status: customer.status,
          companySize: customer.customFields.companySize || '',
          taxNumber: customer.taxNumber || '',
          website: customer.website || '',
          paymentTerms: customer.customFields.paymentTerms || '',
          creditLimit: customer.customFields.creditLimit || undefined,
          notes: customer.notes || '',
          tags: customer.tags || [],
        }
      : {
          name: '',
          email: '',
          phone: '',
          address: '',
          city: '',
          state: '',
          zipCode: '',
          country: '',
          customerType: 'PROSPECT' as CustomerType,
          status: 'ACTIVE' as CustomerStatus,
          companySize: '',
          taxNumber: '',
          website: '',
          paymentTerms: '',
          creditLimit: undefined,
          notes: '',
          tags: [],
        },
  });

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
    setValue,
    getValues,
  } = customerForm;

  const customerType = watch('customerType');

  // Handle form submission
  const onFormSubmit = handleSubmit(async (data) => {
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
                  disabled={isLoading}
                  placeholder='Enter customer name'
                />
                {errors.name && (
                  <p className='text-sm text-destructive'>
                    {errors.name.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='email'>Email *</Label>
                <Input
                  id='email'
                  type='email'
                  {...register('email')}
                  className={cn(errors.email && 'border-destructive')}
                  disabled={isLoading}
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
                  disabled={isLoading}
                  placeholder='+84 xxx xxx xxx'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='customerType'>Account Type *</Label>
                <Select
                  value={watch('customerType')}
                  onValueChange={(value) =>
                    setValue('customerType', value as CustomerType)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select type' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='PROSPECT'>Prospect</SelectItem>
                    <SelectItem value='CUSTOMER'>Customer</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='status'>Status *</Label>
                <Select
                  value={watch('status')}
                  onValueChange={(value) =>
                    setValue('status', value as CustomerStatus)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select status' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='ACTIVE'>Active</SelectItem>
                    <SelectItem value='INACTIVE'>Inactive</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='address'>Street</Label>
                <Input
                  id='address'
                  {...register('address')}
                  disabled={isLoading}
                  placeholder='Street address'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='city'>City</Label>
                <Input id='city' {...register('city')} disabled={isLoading} />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='state'>State</Label>
                <Input id='state' {...register('state')} disabled={isLoading} />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='zipCode'>Zip Code</Label>
                <Input
                  id='zipCode'
                  {...register('zipCode')}
                  disabled={isLoading}
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='country'>Country</Label>
                <Input
                  id='country'
                  {...register('country')}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>

          {/* Account Information */}
          {customerType === 'CUSTOMER' && (
            <div className='space-y-4'>
              <h3 className='text-base font-medium text-foreground'>
                Account Information
              </h3>

              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div className='space-y-2'>
                  <Label htmlFor='companySize'>Company Size</Label>
                  <Input
                    id='companySize'
                    {...register('companySize')}
                    disabled={isLoading}
                    placeholder='e.g. 100-500 employees'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='taxNumber'>Tax Number</Label>
                  <Input
                    id='taxNumber'
                    {...register('taxNumber')}
                    disabled={isLoading}
                    placeholder='Tax identification number'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='paymentTerms'>Payment Terms</Label>
                  <Input
                    id='paymentTerms'
                    {...register('paymentTerms')}
                    disabled={isLoading}
                    placeholder='Net 30, COD, etc.'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='creditLimit'>Credit Limit</Label>
                  <Input
                    id='creditLimit'
                    type='number'
                    {...register('creditLimit', { valueAsNumber: true })}
                    className={cn(errors.creditLimit && 'border-destructive')}
                    disabled={isLoading}
                    placeholder='0'
                  />
                </div>

                <div className='md:col-span-2 space-y-2'>
                  <Label htmlFor='website'>Website</Label>
                  <Input
                    id='website'
                    type='url'
                    {...register('website')}
                    className={cn(errors.website && 'border-destructive')}
                    disabled={isLoading}
                    placeholder='https://example.com'
                  />
                  {errors.website && (
                    <p className='text-sm text-destructive'>
                      {errors.website.message}
                    </p>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Additional Information */}
          <div className='space-y-4'>
            <h3 className='text-base font-medium text-foreground'>
              Additional Information
            </h3>

            <div className='space-y-2'>
              <Label htmlFor='notes'>Notes</Label>
              <Textarea
                id='notes'
                {...register('notes')}
                rows={3}
                disabled={isLoading}
                placeholder='Add any additional notes about this customer...'
              />
            </div>

            {/* Tags */}
            <div className='space-y-2'>
              <Label>Tags</Label>
              <div className='flex flex-wrap gap-2'>
                {watch('tags').map((tag, index) => (
                  <Badge key={index} variant='secondary' className='gap-1 pr-1'>
                    {tag}
                    <button
                      type='button'
                      onClick={() => handleTagRemove(tag)}
                      className='ml-1 rounded-full hover:bg-muted-foreground/20 p-0.5'
                      disabled={isLoading}
                    >
                      <X className='h-3 w-3' />
                    </button>
                  </Badge>
                ))}
              </div>
              <Input
                placeholder='Type a tag and press Enter'
                onKeyDown={(e) => {
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

          {/* Actions */}
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
