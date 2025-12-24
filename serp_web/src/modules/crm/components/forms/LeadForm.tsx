// LeadForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
  Lead,
  CreateLeadRequest,
  UpdateLeadRequest,
  LeadSource,
  LeadStatus,
  Priority,
} from '../../types';

// Validation schema
const leadSchema = z.object({
  firstName: z
    .string()
    .min(1, 'First name is required')
    .max(255, 'First name is too long'),
  lastName: z
    .string()
    .min(1, 'Last name is required')
    .max(255, 'Last name is too long'),
  email: z.string().email('Invalid email address'),
  phone: z.string().optional(),
  company: z.string().optional(),
  jobTitle: z.string().optional(),
  source: z.enum([
    'WEBSITE',
    'REFERRAL',
    'EMAIL',
    'PHONE',
    'SOCIAL_MEDIA',
    'TRADE_SHOW',
    'OTHER',
  ]),
  status: z.enum(['NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED', 'LOST']),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'URGENT']),
  assignedTo: z.string().optional(),
  notes: z.string().optional(),
  tags: z.array(z.string()),
});

type LeadFormData = z.infer<typeof leadSchema>;

interface LeadFormProps {
  lead?: Lead;
  onSubmit: (data: CreateLeadRequest | UpdateLeadRequest) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const LeadForm: React.FC<LeadFormProps> = ({
  lead,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!lead;

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
    setValue,
    getValues,
  } = useForm<LeadFormData>({
    resolver: zodResolver(leadSchema),
    defaultValues: lead
      ? {
          firstName: lead.firstName,
          lastName: lead.lastName,
          email: lead.email,
          phone: lead.phone || '',
          company: lead.company || '',
          jobTitle: lead.jobTitle || '',
          source: lead.source,
          status: lead.status,
          priority: lead.priority,
          assignedTo: lead.assignedTo || '',
          notes: lead.notes || '',
          tags: lead.tags || [],
        }
      : {
          firstName: '',
          lastName: '',
          email: '',
          phone: '',
          company: '',
          jobTitle: '',
          source: 'WEBSITE' as LeadSource,
          status: 'NEW' as LeadStatus,
          priority: 'MEDIUM' as Priority,
          assignedTo: '',
          notes: '',
          tags: [],
        },
  });

  // Handle form submission
  const onFormSubmit = handleSubmit(async (data: LeadFormData) => {
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
          {isEditing ? 'Edit Lead' : 'Create Lead'}
        </h2>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          {/* Personal Information */}
          <div className='space-y-4'>
            <h3 className='text-lg font-medium'>Personal Information</h3>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='firstName'>First Name *</Label>
                <Input
                  id='firstName'
                  {...register('firstName')}
                  className={errors.firstName ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.firstName && (
                  <p className='text-sm text-destructive'>
                    {errors.firstName.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='lastName'>Last Name *</Label>
                <Input
                  id='lastName'
                  {...register('lastName')}
                  className={errors.lastName ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.lastName && (
                  <p className='text-sm text-destructive'>
                    {errors.lastName.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='email'>Email *</Label>
                <Input
                  id='email'
                  type='email'
                  {...register('email')}
                  className={errors.email ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.email && (
                  <p className='text-sm text-destructive'>
                    {errors.email.message}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor='phone'>Phone</Label>
                <Input id='phone' {...register('phone')} disabled={isLoading} />
              </div>

              <div>
                <Label htmlFor='company'>Company</Label>
                <Input
                  id='company'
                  {...register('company')}
                  disabled={isLoading}
                />
              </div>

              <div>
                <Label htmlFor='jobTitle'>Job Title</Label>
                <Input
                  id='jobTitle'
                  {...register('jobTitle')}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>

          {/* Lead Information */}
          <div className='space-y-4'>
            <CardTitle className='text-lg'>Lead Information</CardTitle>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='source'>Source *</Label>
                <Select
                  value={watch('source')}
                  onValueChange={(value) =>
                    setValue('source', value as LeadSource)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select source' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='WEBSITE'>Website</SelectItem>
                    <SelectItem value='REFERRAL'>Referral</SelectItem>
                    <SelectItem value='EMAIL'>Email</SelectItem>
                    <SelectItem value='PHONE'>Phone</SelectItem>
                    <SelectItem value='SOCIAL_MEDIA'>Social Media</SelectItem>
                    <SelectItem value='TRADE_SHOW'>Trade Show</SelectItem>
                    <SelectItem value='OTHER'>Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='status'>Status *</Label>
                <Select
                  value={watch('status')}
                  onValueChange={(value) =>
                    setValue('status', value as LeadStatus)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select status' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='NEW'>New</SelectItem>
                    <SelectItem value='CONTACTED'>Contacted</SelectItem>
                    <SelectItem value='QUALIFIED'>Qualified</SelectItem>
                    <SelectItem value='CONVERTED'>Converted</SelectItem>
                    <SelectItem value='LOST'>Lost</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='priority'>Priority *</Label>
                <Select
                  value={watch('priority')}
                  onValueChange={(value) =>
                    setValue('priority', value as Priority)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select priority' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='LOW'>Low</SelectItem>
                    <SelectItem value='MEDIUM'>Medium</SelectItem>
                    <SelectItem value='HIGH'>High</SelectItem>
                    <SelectItem value='URGENT'>Urgent</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='assignedTo'>Assigned To</Label>
                <Input
                  id='assignedTo'
                  {...register('assignedTo')}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>

          {/* Additional Information */}
          <div className='space-y-4'>
            <CardTitle className='text-lg'>Additional Information</CardTitle>

            <div className='space-y-2'>
              <Label htmlFor='notes'>Notes</Label>
              <Textarea
                id='notes'
                {...register('notes')}
                rows={3}
                placeholder='Enter any additional notes about this lead...'
                disabled={isLoading}
              />
            </div>

            {/* Tags */}
            <div className='space-y-2'>
              <Label>Tags</Label>
              <div className='flex flex-wrap gap-2'>
                {watch('tags').map((tag, index) => (
                  <Badge
                    key={index}
                    variant='secondary'
                    className='flex items-center gap-1'
                  >
                    {tag}
                    <button
                      type='button'
                      onClick={() => handleTagRemove(tag)}
                      className='hover:text-destructive'
                      disabled={isLoading}
                    >
                      <X className='h-3 w-3' />
                    </button>
                  </Badge>
                ))}
              </div>
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
                  ? 'Update Lead'
                  : 'Create Lead'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default LeadForm;
