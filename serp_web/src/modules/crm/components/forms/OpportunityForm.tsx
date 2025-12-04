// OpportunityForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
  Opportunity,
  OpportunityStage,
  OpportunityType,
} from '../../types';

// Validation schema
const opportunitySchema = z.object({
  name: z.string().min(1, 'Name is required').max(255, 'Name is too long'),
  customerId: z.string().min(1, 'Customer is required'),
  customerName: z.string().min(1, 'Customer name is required'),
  stage: z.enum([
    'PROSPECTING',
    'QUALIFICATION',
    'PROPOSAL',
    'NEGOTIATION',
    'CLOSED_WON',
    'CLOSED_LOST',
  ]),
  type: z.enum(['NEW_BUSINESS', 'EXISTING_BUSINESS', 'RENEWAL']),
  value: z.number().min(0, 'Value must be positive'),
  probability: z.number().min(0).max(100, 'Probability must be 0-100'),
  expectedCloseDate: z.string().min(1, 'Expected close date is required'),
  assignedTo: z.string().optional(),
  assignedToName: z.string().optional(),
  description: z.string().optional(),
  notes: z.string().optional(),
  nextAction: z.string().optional(),
  nextActionDate: z.string().optional(),
  tags: z.array(z.string()),
});

type OpportunityFormData = z.infer<typeof opportunitySchema>;

interface OpportunityFormProps {
  opportunity?: Opportunity;
  onSubmit: (data: OpportunityFormData) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

// Mock customers for select
const MOCK_CUSTOMERS = [
  { id: 'cust-001', name: 'ABC Corporation' },
  { id: 'cust-002', name: 'XYZ Enterprises' },
  { id: 'cust-003', name: 'Tech Solutions Inc' },
  { id: 'cust-004', name: 'Global Trading Co' },
];

export const OpportunityForm: React.FC<OpportunityFormProps> = ({
  opportunity,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!opportunity;

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
    setValue,
    getValues,
  } = useForm<OpportunityFormData>({
    resolver: zodResolver(opportunitySchema),
    defaultValues: opportunity
      ? {
          name: opportunity.name,
          customerId: opportunity.customerId,
          customerName: opportunity.customerName,
          stage: opportunity.stage,
          type: opportunity.type,
          value: opportunity.value,
          probability: opportunity.probability,
          expectedCloseDate: opportunity.expectedCloseDate.split('T')[0],
          assignedTo: opportunity.assignedTo || '',
          assignedToName: opportunity.assignedToName || '',
          description: opportunity.description || '',
          notes: opportunity.notes || '',
          nextAction: opportunity.nextAction || '',
          nextActionDate: opportunity.nextActionDate?.split('T')[0] || '',
          tags: opportunity.tags || [],
        }
      : {
          name: '',
          customerId: '',
          customerName: '',
          stage: 'PROSPECTING' as OpportunityStage,
          type: 'NEW_BUSINESS' as OpportunityType,
          value: 0,
          probability: 10,
          expectedCloseDate: '',
          assignedTo: '',
          assignedToName: '',
          description: '',
          notes: '',
          nextAction: '',
          nextActionDate: '',
          tags: [],
        },
  });

  // Handle form submission
  const onFormSubmit = handleSubmit(async (data: OpportunityFormData) => {
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

  // Handle customer selection
  const handleCustomerChange = (customerId: string) => {
    const customer = MOCK_CUSTOMERS.find((c) => c.id === customerId);
    if (customer) {
      setValue('customerId', customer.id);
      setValue('customerName', customer.name);
    }
  };

  // Update probability based on stage
  const handleStageChange = (stage: OpportunityStage) => {
    setValue('stage', stage);
    // Auto-update probability based on stage
    const probabilities: Record<OpportunityStage, number> = {
      PROSPECTING: 10,
      QUALIFICATION: 25,
      PROPOSAL: 50,
      NEGOTIATION: 75,
      CLOSED_WON: 100,
      CLOSED_LOST: 0,
    };
    setValue('probability', probabilities[stage]);
  };

  return (
    <Card className={cn('w-full max-w-2xl mx-auto', className)}>
      <CardHeader>
        <CardTitle className='text-xl'>
          {isEditing ? 'Edit Opportunity' : 'Create Opportunity'}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          {/* Basic Information */}
          <div className='space-y-4'>
            <CardTitle className='text-lg'>Basic Information</CardTitle>

            <div className='space-y-2'>
              <Label htmlFor='name'>Opportunity Name *</Label>
              <Input
                id='name'
                {...register('name')}
                placeholder='Enter opportunity name'
                className={errors.name ? 'border-destructive' : ''}
                disabled={isLoading}
              />
              {errors.name && (
                <p className='text-sm text-destructive'>
                  {errors.name.message}
                </p>
              )}
            </div>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label>Customer *</Label>
                <Select
                  value={watch('customerId')}
                  onValueChange={handleCustomerChange}
                  disabled={isLoading}
                >
                  <SelectTrigger
                    className={errors.customerId ? 'border-destructive' : ''}
                  >
                    <SelectValue placeholder='Select customer' />
                  </SelectTrigger>
                  <SelectContent>
                    {MOCK_CUSTOMERS.map((customer) => (
                      <SelectItem key={customer.id} value={customer.id}>
                        {customer.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.customerId && (
                  <p className='text-sm text-destructive'>
                    {errors.customerId.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label>Type *</Label>
                <Select
                  value={watch('type')}
                  onValueChange={(value) =>
                    setValue('type', value as OpportunityType)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select type' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='NEW_BUSINESS'>New Business</SelectItem>
                    <SelectItem value='EXISTING_BUSINESS'>
                      Existing Business
                    </SelectItem>
                    <SelectItem value='RENEWAL'>Renewal</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {/* Pipeline Information */}
          <div className='space-y-4'>
            <CardTitle className='text-lg'>Pipeline Information</CardTitle>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label>Stage *</Label>
                <Select
                  value={watch('stage')}
                  onValueChange={handleStageChange}
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select stage' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='PROSPECTING'>Prospecting</SelectItem>
                    <SelectItem value='QUALIFICATION'>Qualification</SelectItem>
                    <SelectItem value='PROPOSAL'>Proposal</SelectItem>
                    <SelectItem value='NEGOTIATION'>Negotiation</SelectItem>
                    <SelectItem value='CLOSED_WON'>Closed Won</SelectItem>
                    <SelectItem value='CLOSED_LOST'>Closed Lost</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='probability'>Probability (%) *</Label>
                <Input
                  id='probability'
                  type='number'
                  min={0}
                  max={100}
                  {...register('probability', { valueAsNumber: true })}
                  className={errors.probability ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.probability && (
                  <p className='text-sm text-destructive'>
                    {errors.probability.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='value'>Value ($) *</Label>
                <Input
                  id='value'
                  type='number'
                  min={0}
                  {...register('value', { valueAsNumber: true })}
                  placeholder='0'
                  className={errors.value ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.value && (
                  <p className='text-sm text-destructive'>
                    {errors.value.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='expectedCloseDate'>Expected Close Date *</Label>
                <Input
                  id='expectedCloseDate'
                  type='date'
                  {...register('expectedCloseDate')}
                  className={
                    errors.expectedCloseDate ? 'border-destructive' : ''
                  }
                  disabled={isLoading}
                />
                {errors.expectedCloseDate && (
                  <p className='text-sm text-destructive'>
                    {errors.expectedCloseDate.message}
                  </p>
                )}
              </div>
            </div>
          </div>

          {/* Next Action */}
          <div className='space-y-4'>
            <CardTitle className='text-lg'>Next Action</CardTitle>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='nextAction'>Next Action</Label>
                <Input
                  id='nextAction'
                  {...register('nextAction')}
                  placeholder='e.g., Follow up call'
                  disabled={isLoading}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='nextActionDate'>Next Action Date</Label>
                <Input
                  id='nextActionDate'
                  type='date'
                  {...register('nextActionDate')}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>

          {/* Additional Information */}
          <div className='space-y-4'>
            <CardTitle className='text-lg'>Additional Information</CardTitle>

            <div className='space-y-2'>
              <Label htmlFor='description'>Description</Label>
              <Textarea
                id='description'
                {...register('description')}
                rows={3}
                placeholder='Enter opportunity description...'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='notes'>Notes</Label>
              <Textarea
                id='notes'
                {...register('notes')}
                rows={2}
                placeholder='Enter any additional notes...'
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
                  ? 'Update Opportunity'
                  : 'Create Opportunity'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default OpportunityForm;
