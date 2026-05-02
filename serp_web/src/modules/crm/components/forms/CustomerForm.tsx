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
import {
  ACCOUNT_TIERS,
  PREFERRED_DAYS,
  PREFERRED_TIME_SLOTS,
} from '../../types/constants';
import type {
  AccountTier,
  CreateCustomerRequest,
  CrmDayOfWeek,
  Customer,
  CustomerStatus,
  CustomerType,
  PreferredTimeSlot,
  UpdateCustomerRequest,
} from '../../types';

const dayOfWeekSchema = z.enum([
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
]);

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
  industry: z.string().optional(),
  companySize: z.string().optional(),
  taxNumber: z.string().optional(),
  website: z.string().url('Invalid website URL').optional().or(z.literal('')),
  paymentTerms: z.string().optional(),
  creditLimit: z.coerce.number().min(0).optional(),
  notes: z.string().optional(),
  tags: z.array(z.string()),
  tier: z
    .string()
    .optional()
    .refine(
      (v) =>
        v === undefined ||
        v === '' ||
        (['STANDARD', 'SILVER', 'GOLD', 'PLATINUM'] as const).includes(
          v as AccountTier
        ),
      { message: 'Invalid tier' }
    ),
  language: z.string().optional(),
  timezone: z.string().optional(),
  preferredTimeSlots: z.array(z.enum(['MORNING', 'AFTERNOON'])),
  preferredDays: z.array(dayOfWeekSchema),
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

const TIER_NONE = '__none__';

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
          city:
            customer.customFields?.city !== undefined
              ? String(customer.customFields.city)
              : '',
          state:
            customer.customFields?.state !== undefined
              ? String(customer.customFields.state)
              : '',
          zipCode:
            customer.customFields?.zipCode !== undefined
              ? String(customer.customFields.zipCode)
              : '',
          country:
            customer.customFields?.country !== undefined
              ? String(customer.customFields.country)
              : '',
          customerType: customer.customerType,
          status: customer.status,
          industry:
            customer.industry ??
            (customer.customFields?.industry as string | undefined) ??
            customer.tags?.[0] ??
            '',
          companySize:
            customer.companySize ??
            (customer.customFields?.companySize as string | undefined) ??
            '',
          taxNumber: customer.taxNumber || '',
          website: customer.website || '',
          paymentTerms:
            (customer.paymentTerms as string | undefined) ??
            (customer.customFields?.paymentTerms as string | undefined) ??
            '',
          creditLimit:
            customer.creditLimit ??
            (customer.customFields?.creditLimit as number | undefined),
          notes: customer.notes || '',
          tags: customer.tags || [],
          tier: customer.tier ?? '',
          language: customer.language || '',
          timezone: customer.timezone || 'Asia/Ho_Chi_Minh',
          preferredTimeSlots: customer.preferredTimeSlots ?? [],
          preferredDays: customer.preferredDays ?? [],
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
          industry: '',
          companySize: '',
          taxNumber: '',
          website: '',
          paymentTerms: '',
          creditLimit: undefined,
          notes: '',
          tags: [],
          tier: '',
          language: '',
          timezone: 'Asia/Ho_Chi_Minh',
          preferredTimeSlots: [],
          preferredDays: [],
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
  const preferredTimeSlots = watch('preferredTimeSlots');
  const preferredDays = watch('preferredDays');

  const togglePreferredSlot = (slot: PreferredTimeSlot) => {
    const current = getValues('preferredTimeSlots');
    if (current.includes(slot)) {
      setValue(
        'preferredTimeSlots',
        current.filter((s) => s !== slot)
      );
    } else {
      setValue('preferredTimeSlots', [...current, slot]);
    }
  };

  const togglePreferredDay = (day: CrmDayOfWeek) => {
    const current = getValues('preferredDays');
    if (current.includes(day)) {
      setValue(
        'preferredDays',
        current.filter((d) => d !== day)
      );
    } else {
      setValue('preferredDays', [...current, day]);
    }
  };

  const onFormSubmit = handleSubmit(async (values) => {
    try {
      const tierRaw = values.tier?.trim();
      const tierParsed: AccountTier | undefined =
        tierRaw &&
        (['STANDARD', 'SILVER', 'GOLD', 'PLATINUM'] as const).includes(
          tierRaw as AccountTier
        )
          ? (tierRaw as AccountTier)
          : undefined;

      const common = {
        name: values.name,
        email: values.email,
        phone: values.phone || undefined,
        address: values.address || undefined,
        city: values.city || undefined,
        state: values.state || undefined,
        zipCode: values.zipCode || undefined,
        country: values.country || undefined,
        customerType: values.customerType,
        status: values.status,
        isActive: values.status === 'ACTIVE',
        industry: values.industry?.trim() || undefined,
        companySize: values.companySize?.trim() || undefined,
        taxNumber: values.taxNumber || undefined,
        website: values.website || undefined,
        paymentTerms: values.paymentTerms || undefined,
        creditLimit: values.creditLimit,
        notes: values.notes || undefined,
        tags: values.tags,
        tier: tierParsed,
        language: values.language?.trim() || undefined,
        timezone: values.timezone?.trim() || undefined,
        preferredTimeSlots:
          values.preferredTimeSlots.length > 0
            ? values.preferredTimeSlots
            : undefined,
        preferredDays:
          values.preferredDays.length > 0 ? values.preferredDays : undefined,
      };

      if (isEditing && customer) {
        const updatePayload: UpdateCustomerRequest = {
          ...common,
          customFields: customer.customFields,
        };
        await onSubmit(updatePayload);
      } else {
        const createPayload = {
          ...common,
          customFields: {},
        } as CreateCustomerRequest;
        await onSubmit(createPayload);
      }
    } catch (error) {
      console.error('Form submission error:', error);
    }
  });

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

  const tierSelectValue = watch('tier') ?? '';
  const tierForSelect =
    tierSelectValue.trim() !== '' ? tierSelectValue : TIER_NONE;

  return (
    <Card className={cn('w-full', className)}>
      <CardHeader className='pb-4'>
        <CardTitle className='text-xl'>
          {isEditing ? 'Edit Account' : 'Create Account'}
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
                  placeholder='Enter account name'
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
                <Label htmlFor='industry'>Industry</Label>
                <Input
                  id='industry'
                  {...register('industry')}
                  disabled={isLoading}
                  placeholder='e.g. Manufacturing'
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

          {/* Tier & contact preferences */}
          <div className='space-y-4'>
            <h3 className='text-base font-medium text-foreground'>
              Tier & contact preferences
            </h3>
            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label>Tier</Label>
                <Select
                  value={tierForSelect}
                  onValueChange={(value) =>
                    setValue(
                      'tier',
                      value === TIER_NONE ? '' : (value as AccountTier)
                    )
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select tier' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={TIER_NONE}>Not set</SelectItem>
                    {ACCOUNT_TIERS.map((t) => (
                      <SelectItem key={t.value} value={t.value}>
                        {t.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label htmlFor='language'>Language</Label>
                <Input
                  id='language'
                  {...register('language')}
                  disabled={isLoading}
                  placeholder='e.g. vi, en'
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='timezone'>Timezone</Label>
                <Input
                  id='timezone'
                  {...register('timezone')}
                  disabled={isLoading}
                  placeholder='Asia/Ho_Chi_Minh'
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label>Preferred contact times</Label>
              <div className='flex flex-wrap gap-2'>
                {PREFERRED_TIME_SLOTS.map(({ value, label }) => (
                  <button
                    key={value}
                    type='button'
                    onClick={() => togglePreferredSlot(value)}
                    disabled={isLoading}
                    className={cn(
                      'rounded-full border px-3 py-1 text-sm transition-colors',
                      preferredTimeSlots.includes(value)
                        ? 'border-primary bg-primary/10 text-primary'
                        : 'border-border bg-background hover:bg-muted'
                    )}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>

            <div className='space-y-2'>
              <Label>Preferred days</Label>
              <div className='flex flex-wrap gap-2'>
                {PREFERRED_DAYS.map(({ value, label }) => (
                  <button
                    key={value}
                    type='button'
                    onClick={() => togglePreferredDay(value)}
                    disabled={isLoading}
                    className={cn(
                      'rounded-md border px-2.5 py-1 text-sm transition-colors',
                      preferredDays.includes(value)
                        ? 'border-primary bg-primary/10 text-primary'
                        : 'border-border bg-background hover:bg-muted'
                    )}
                  >
                    {label}
                  </button>
                ))}
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
                placeholder='Add any additional notes about this account...'
              />
            </div>

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
                  : 'Create Account'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default CustomerForm;
