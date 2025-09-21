// LeadForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
              <div>
                <Label htmlFor='firstName'>First Name *</Label>
                <Input
                  id='firstName'
                  {...register('firstName')}
                  className={errors.firstName ? 'border-red-500' : ''}
                  disabled={isLoading}
                />
                {errors.firstName && (
                  <p className='text-sm text-red-600 mt-1'>
                    {errors.firstName.message}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor='lastName'>Last Name *</Label>
                <Input
                  id='lastName'
                  {...register('lastName')}
                  className={errors.lastName ? 'border-red-500' : ''}
                  disabled={isLoading}
                />
                {errors.lastName && (
                  <p className='text-sm text-red-600 mt-1'>
                    {errors.lastName.message}
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
            <h3 className='text-lg font-medium'>Lead Information</h3>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div>
                <Label htmlFor='source'>Source *</Label>
                <select
                  {...register('source')}
                  className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                  disabled={isLoading}
                >
                  <option value='WEBSITE'>Website</option>
                  <option value='REFERRAL'>Referral</option>
                  <option value='EMAIL'>Email</option>
                  <option value='PHONE'>Phone</option>
                  <option value='SOCIAL_MEDIA'>Social Media</option>
                  <option value='TRADE_SHOW'>Trade Show</option>
                  <option value='OTHER'>Other</option>
                </select>
              </div>

              <div>
                <Label htmlFor='status'>Status *</Label>
                <select
                  {...register('status')}
                  className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                  disabled={isLoading}
                >
                  <option value='NEW'>New</option>
                  <option value='CONTACTED'>Contacted</option>
                  <option value='QUALIFIED'>Qualified</option>
                  <option value='CONVERTED'>Converted</option>
                  <option value='LOST'>Lost</option>
                </select>
              </div>

              <div>
                <Label htmlFor='priority'>Priority *</Label>
                <select
                  {...register('priority')}
                  className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                  disabled={isLoading}
                >
                  <option value='LOW'>Low</option>
                  <option value='MEDIUM'>Medium</option>
                  <option value='HIGH'>High</option>
                  <option value='URGENT'>Urgent</option>
                </select>
              </div>

              <div>
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
