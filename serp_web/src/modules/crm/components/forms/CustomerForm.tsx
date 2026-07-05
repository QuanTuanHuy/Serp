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
    <div className={cn("grid grid-cols-1 lg:grid-cols-3 gap-6", className)}>
      <Card className="lg:col-span-2 border border-muted/50 shadow-sm rounded-xl">
        <CardHeader>
          <CardTitle className="text-xl font-extrabold tracking-tight">
            {isEditing ? 'Update Business Account' : 'Register Business Account'}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onFormSubmit} className="space-y-6">
            <div className="space-y-4">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Basic Profile</h3>

              <div className="space-y-2">
                <Label htmlFor="name">Account Name *</Label>
                <Input id="name" {...register('name')} placeholder="e.g. Acme Corporation" className={errors.name ? 'border-destructive' : ''} disabled={isLoading} />
                {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="taxNumber">Tax Identification Number</Label>
                  <Input id="taxNumber" {...register('taxNumber')} placeholder="e.g. 0102030405" disabled={isLoading} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Customer Type *</Label>
                  <Select value={watch('customerType')} onValueChange={(val) => setValue('customerType', val as any)} disabled={isLoading}>
                    <SelectTrigger><SelectValue placeholder="Select type" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="PROSPECT">Prospect</SelectItem>
                      <SelectItem value="CUSTOMER">Customer</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>Account Tier</Label>
                  <Select value={tierForSelect} onValueChange={(val) => setValue('tier', val === TIER_NONE ? '' : (val as AccountTier))} disabled={isLoading}>
                    <SelectTrigger><SelectValue placeholder="Select tier" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value={TIER_NONE}>Not set</SelectItem>
                      {ACCOUNT_TIERS.map((tier) => (
                        <SelectItem key={tier.value} value={tier.value}>{tier.label}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="industry">Industry / Sector</Label>
                  <Input id="industry" {...register('industry')} placeholder="e.g. Logistics" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="companySize">Company Size (Employees)</Label>
                  <Input id="companySize" {...register('companySize')} placeholder="e.g. 100-500 employees" disabled={isLoading} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label>Status *</Label>
                  <Select value={watch('status')} onValueChange={(val) => setValue('status', val as any)} disabled={isLoading}>
                    <SelectTrigger><SelectValue placeholder="Select status" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="ACTIVE">Active</SelectItem>
                      <SelectItem value="INACTIVE">Inactive</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="creditLimit">Credit Limit (VND)</Label>
                  <Input id="creditLimit" type="number" {...register('creditLimit', { valueAsNumber: true })} placeholder="100000000" className={errors.creditLimit ? 'border-destructive' : ''} disabled={isLoading} />
                  {errors.creditLimit && <p className="text-xs text-destructive">{errors.creditLimit.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="paymentTerms">Payment Terms</Label>
                  <Input id="paymentTerms" {...register('paymentTerms')} placeholder="Net 30, COD, etc." disabled={isLoading} />
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Contacts & Locale</h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Primary Email *</Label>
                  <Input id="email" type="email" {...register('email')} placeholder="billing@acme.com" className={errors.email ? 'border-destructive' : ''} disabled={isLoading} />
                  {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Phone Number</Label>
                  <Input id="phone" {...register('phone')} placeholder="+84 901234567" disabled={isLoading} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="website">Website Address</Label>
                  <Input id="website" type="url" {...register('website')} placeholder="https://acme.com" className={errors.website ? 'border-destructive' : ''} disabled={isLoading} />
                  {errors.website && <p className="text-xs text-destructive">{errors.website.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="timezone">Office Timezone</Label>
                  <Input id="timezone" {...register('timezone')} placeholder="Asia/Ho_Chi_Minh" disabled={isLoading} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="language">Preferred Language</Label>
                  <Input id="language" {...register('language')} placeholder="vi" disabled={isLoading} />
                </div>
              </div>

              <div className="space-y-2 pt-2">
                <Label htmlFor="address">Street Address</Label>
                <Input id="address" {...register('address')} placeholder="123 Tech Park Road" disabled={isLoading} />
              </div>

              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="city">City</Label>
                  <Input id="city" {...register('city')} placeholder="Hanoi" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="state">State / Province</Label>
                  <Input id="state" {...register('state')} placeholder="Hanoi" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="zipCode">Zip Code</Label>
                  <Input id="zipCode" {...register('zipCode')} placeholder="10000" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="country">Country</Label>
                  <Input id="country" {...register('country')} placeholder="Vietnam" disabled={isLoading} />
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Internal Remarks</h3>
              <div className="space-y-2">
                <Label htmlFor="notes">Static Account Notes</Label>
                <Textarea id="notes" {...register('notes')} rows={3} placeholder="Add specific terms or relationship summaries..." disabled={isLoading} />
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-6 border-t border-muted/30">
              {onCancel && (
                <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading || isSubmitting}>
                  Cancel
                </Button>
              )}
              <Button type="submit" disabled={isLoading || isSubmitting}>
                {isSubmitting ? 'Saving...' : isEditing ? 'Update Account' : 'Register Account'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Widget Column */}
      <div className="lg:col-span-1 space-y-6">
        {/* Tag Cloud Selector */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader>
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Account Tags</CardTitle>
          </CardHeader>
          <CardContent className="p-4 space-y-4">
            <div className="flex flex-wrap gap-1.5">
              {watch('tags').map((tag, index) => (
                <Badge key={index} variant="secondary" className="gap-1 pr-1 text-[10px] py-0.5">
                  {tag}
                  <button
                    type="button"
                    onClick={() => handleTagRemove(tag)}
                    className="ml-1 rounded-full hover:bg-muted-foreground/20 p-0.5"
                    disabled={isLoading}
                  >
                    <X className="h-2.5 w-2.5" />
                  </button>
                </Badge>
              ))}
            </div>

            <div className="pt-3 border-t border-muted/20 space-y-3">
              <div className="text-[10px] text-muted-foreground uppercase font-bold">Preset Options</div>
              <div className="flex flex-wrap gap-1.5">
                {['VIP', 'PARTNER', 'TARGET', 'KEY_ACCOUNT', 'PROSPECT', 'INACTIVE'].map((tg) => {
                  const isSelected = watch('tags')?.includes(tg);
                  return (
                    <Badge
                      key={tg}
                      variant={isSelected ? 'default' : 'outline'}
                      className="cursor-pointer text-[10px] px-2 py-0.5 select-none"
                      onClick={() => {
                        const cur = watch('tags') || [];
                        if (cur.includes(tg)) {
                          handleTagRemove(tg);
                        } else {
                          handleTagAdd(tg);
                        }
                      }}
                    >
                      {tg.replace('_', ' ')}
                    </Badge>
                  );
                })}
              </div>
            </div>

            <div className="pt-3 border-t border-muted/20 space-y-1">
              <Label className="text-[10px] text-muted-foreground uppercase font-bold">Add Custom Tag</Label>
              <Input
                placeholder="Press Enter to add tag"
                className="h-8 text-xs"
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
          </CardContent>
        </Card>

        {/* Preferred Communication Badges */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader>
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Best Contact Days</CardTitle>
          </CardHeader>
          <CardContent className="p-4 space-y-4">
            <div className="flex flex-wrap gap-1.5">
              {PREFERRED_DAYS.map((day) => {
                const isSelected = preferredDays.includes(day.value);
                return (
                  <Badge
                    key={day.value}
                    variant={isSelected ? 'default' : 'secondary'}
                    className="cursor-pointer text-[10px] px-2 py-1 select-none"
                    onClick={() => togglePreferredDay(day.value)}
                  >
                    {day.label}
                  </Badge>
                );
              })}
            </div>

            <div className="pt-3 border-t border-muted/20 space-y-2">
              <div className="text-[10px] text-muted-foreground uppercase font-bold">Best Hours</div>
              <div className="flex flex-wrap gap-1.5">
                {PREFERRED_TIME_SLOTS.map((slot) => {
                  const isSelected = preferredTimeSlots.includes(slot.value);
                  return (
                    <Badge
                      key={slot.value}
                      variant={isSelected ? 'default' : 'secondary'}
                      className="cursor-pointer text-[10px] px-2 py-1 select-none"
                      onClick={() => togglePreferredSlot(slot.value)}
                    >
                      {slot.label}
                    </Badge>
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default CustomerForm;
