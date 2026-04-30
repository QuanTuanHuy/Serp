// OpportunityForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useMemo } from 'react';
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetAccountsQuery, useGetLeadsQuery } from '../../api/crmApi';
import type {
  CreateOpportunityRequest,
  Opportunity,
  OpportunityStage,
  UpdateOpportunityRequest,
} from '../../types';

const opportunitySchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'Opportunity name is required.')
    .max(255, 'Name is too long.'),
  accountId: z.string().trim().min(1, 'Account is required.'),
  leadId: z.string().trim().optional(),
  stage: z.enum([
    'PROSPECTING',
    'QUALIFICATION',
    'PROPOSAL',
    'NEGOTIATION',
    'CLOSED_WON',
    'CLOSED_LOST',
  ]),
  estimatedValue: z
    .number()
    .min(0, 'Estimated value must be greater than or equal to 0.'),
  expectedCloseDate: z.string().min(1, 'Expected close date is required.'),
  assignedTo: z.string().trim().optional(),
  description: z
    .string()
    .max(1000, 'Description must not exceed 1000 characters.')
    .optional(),
  notes: z
    .string()
    .max(1000, 'Notes must not exceed 1000 characters.')
    .optional(),
});

type OpportunityFormData = z.infer<typeof opportunitySchema>;

interface OpportunityFormProps {
  opportunity?: Opportunity;
  onSubmit: (
    data: CreateOpportunityRequest | UpdateOpportunityRequest
  ) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

const defaultValues: OpportunityFormData = {
  name: '',
  accountId: '',
  leadId: '',
  stage: 'PROSPECTING',
  estimatedValue: 0,
  expectedCloseDate: '',
  assignedTo: '',
  description: '',
  notes: '',
};

export const OpportunityForm: React.FC<OpportunityFormProps> = ({
  opportunity,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!opportunity;
  const { data: accountsResponse, isLoading: isAccountsLoading } =
    useGetAccountsQuery({
      filters: {},
      pagination: { page: 1, limit: 100, sortBy: 'name', sortOrder: 'asc' },
    });
  const { data: leadsResponse, isLoading: isLeadsLoading } = useGetLeadsQuery({
    filters: {},
    pagination: { page: 1, limit: 100, sortBy: 'name', sortOrder: 'asc' },
  });

  const accounts = useMemo(
    () => accountsResponse?.data?.data || [],
    [accountsResponse]
  );
  const leads = useMemo(() => leadsResponse?.data?.data || [], [leadsResponse]);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
    setValue,
  } = useForm<OpportunityFormData>({
    resolver: zodResolver(opportunitySchema),
    defaultValues: opportunity
      ? {
          name: opportunity.name,
          accountId: opportunity.accountId || opportunity.customerId || '',
          leadId: opportunity.leadId || '',
          stage: opportunity.stage,
          estimatedValue: opportunity.estimatedValue ?? opportunity.value ?? 0,
          expectedCloseDate: opportunity.expectedCloseDate.split('T')[0],
          assignedTo: opportunity.assignedTo || '',
          description: opportunity.description || '',
          notes: opportunity.notes || '',
        }
      : defaultValues,
  });

  const watchedStage = watch('stage');
  const estimatedValue = watch('estimatedValue') || 0;

  const stageProbability: Record<OpportunityStage, number> = {
    PROSPECTING: 10,
    QUALIFICATION: 25,
    PROPOSAL: 50,
    NEGOTIATION: 75,
    CLOSED_WON: 100,
    CLOSED_LOST: 0,
  };

  const computedProbability = stageProbability[watchedStage];
  const weightedValue = (estimatedValue * computedProbability) / 100;

  const onFormSubmit = handleSubmit(async (data) => {
    await onSubmit({
      name: data.name,
      accountId: data.accountId,
      leadId: data.leadId?.trim() ? data.leadId.trim() : undefined,
      stage: data.stage,
      estimatedValue: data.estimatedValue,
      expectedCloseDate: data.expectedCloseDate,
      assignedTo: data.assignedTo?.trim() ? data.assignedTo.trim() : undefined,
      description: data.description?.trim() || undefined,
      notes: data.notes?.trim() || undefined,
    });
  });

  return (
    <Card className={cn('w-full max-w-4xl', className)}>
      <CardHeader>
        <CardTitle className='text-xl'>
          {isEditing ? 'Edit Opportunity' : 'Create Opportunity'}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
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
                <Label>Account *</Label>
                <Select
                  value={watch('accountId')}
                  onValueChange={(value) => setValue('accountId', value)}
                  disabled={isLoading || isAccountsLoading}
                >
                  <SelectTrigger
                    className={errors.accountId ? 'border-destructive' : ''}
                  >
                    <SelectValue
                      placeholder={
                        isAccountsLoading
                          ? 'Loading accounts...'
                          : 'Select account'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {accounts.map((account) => (
                      <SelectItem key={account.id} value={account.id}>
                        {account.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.accountId && (
                  <p className='text-sm text-destructive'>
                    {errors.accountId.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label>Lead</Label>
                <Select
                  value={watch('leadId') || ''}
                  onValueChange={(value) => setValue('leadId', value)}
                  disabled={isLoading || isLeadsLoading}
                >
                  <SelectTrigger>
                    <SelectValue
                      placeholder={
                        isLeadsLoading
                          ? 'Loading leads...'
                          : 'Select lead (optional)'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {leads.map((lead) => (
                      <SelectItem key={lead.id} value={lead.id}>
                        {lead.name || lead.email || `Lead #${lead.id}`}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <div className='space-y-4'>
            <CardTitle className='text-lg'>Pipeline Information</CardTitle>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label>Stage *</Label>
                <Select
                  value={watch('stage')}
                  onValueChange={(value) =>
                    setValue('stage', value as OpportunityStage)
                  }
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
                <Label htmlFor='assignedTo'>Assigned To</Label>
                <Input
                  id='assignedTo'
                  {...register('assignedTo')}
                  placeholder='Optional assignee user ID'
                  disabled={isLoading}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='estimatedValue'>Estimated Value *</Label>
                <Input
                  id='estimatedValue'
                  type='number'
                  min={0}
                  {...register('estimatedValue', { valueAsNumber: true })}
                  placeholder='0'
                  className={errors.estimatedValue ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.estimatedValue && (
                  <p className='text-sm text-destructive'>
                    {errors.estimatedValue.message}
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

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4 rounded-lg border bg-muted/30 p-4'>
              <div>
                <p className='text-sm text-muted-foreground'>Probability</p>
                <p className='text-xl font-semibold'>{computedProbability}%</p>
              </div>
              <div>
                <p className='text-sm text-muted-foreground'>Weighted Value</p>
                <p className='text-xl font-semibold'>
                  ${weightedValue.toLocaleString()}
                </p>
              </div>
            </div>
          </div>

          <div className='space-y-4'>
            <CardTitle className='text-lg'>Additional Information</CardTitle>

            <div className='space-y-2'>
              <Label htmlFor='description'>Description</Label>
              <Textarea
                id='description'
                {...register('description')}
                rows={4}
                placeholder='Enter opportunity description...'
                disabled={isLoading}
              />
              {errors.description && (
                <p className='text-sm text-destructive'>
                  {errors.description.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='notes'>Notes</Label>
              <Textarea
                id='notes'
                {...register('notes')}
                rows={3}
                placeholder='Enter any additional notes...'
                disabled={isLoading}
              />
              {errors.notes && (
                <p className='text-sm text-destructive'>
                  {errors.notes.message}
                </p>
              )}
            </div>
          </div>

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
