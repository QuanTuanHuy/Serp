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
                <Label htmlFor='customerType'>Customer Type *</Label>
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
                    <SelectItem value='INDIVIDUAL'>Individual</SelectItem>
                    <SelectItem value='COMPANY'>Company</SelectItem>
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
                    <SelectItem value='POTENTIAL'>Potential</SelectItem>
                    <SelectItem value='ACTIVE'>Active</SelectItem>
                    <SelectItem value='INACTIVE'>Inactive</SelectItem>
                    <SelectItem value='BLOCKED'>Blocked</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='assignedSalesRep'>Assigned Sales Rep</Label>
                <Input
                  id='assignedSalesRep'
                  {...register('assignedSalesRep')}
                  disabled={isLoading}
                  placeholder='Sales representative name'
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='address'>Address</Label>
              <Input
                id='address'
                {...register('address')}
                disabled={isLoading}
                placeholder='Enter full address'
              />
            </div>
          </div>

          {/* Company Information */}
          {customerType === 'COMPANY' && (
            <div className='space-y-4'>
              <h3 className='text-base font-medium text-foreground'>
                Company Information
              </h3>

              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div className='space-y-2'>
                  <Label htmlFor='companyName'>Company Name</Label>
                  <Input
                    id='companyName'
                    {...register('companyName')}
                    disabled={isLoading}
                    placeholder='Enter company name'
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
