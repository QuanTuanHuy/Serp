/*
Author: QuanTuanHuy
Description: Part of Serp Project - Quick Add Opportunity Dialog for CRM
*/

'use client';

import { useEffect, useMemo, useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Button,
  Input,
  Label,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import {
  Target,
  Building2,
  DollarSign,
  Calendar,
  FileText,
  Loader2,
  Percent,
  User,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useGetAccountsQuery } from '../../api/crmApi';
import { formatCurrency } from '../../utils';
import type { CreateOpportunityRequest, OpportunityStage } from '../../types';

export interface QuickOpportunityFormData {
  name: string;
  accountId: string;
  leadId?: string;
  stage: OpportunityStage;
  estimatedValue: number;
  expectedCloseDate: string;
  assignedTo?: string;
  description?: string;
  notes?: string;
}

interface QuickAddOpportunityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateOpportunityRequest) => void | Promise<void>;
  isLoading?: boolean;
  preselectedAccountId?: string;
}

const defaultFormData = (
  preselectedAccountId?: string
): QuickOpportunityFormData => ({
  name: '',
  accountId: preselectedAccountId || '',
  leadId: '',
  stage: 'PROSPECTING',
  estimatedValue: 0,
  expectedCloseDate: '',
  assignedTo: '',
  description: '',
  notes: '',
});

export const QuickAddOpportunityDialog: React.FC<
  QuickAddOpportunityDialogProps
> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
  preselectedAccountId,
}) => {
  const [formData, setFormData] = useState<QuickOpportunityFormData>(
    defaultFormData(preselectedAccountId)
  );
  const [errors, setErrors] = useState<
    Partial<Record<keyof QuickOpportunityFormData, string>>
  >({});

  const { data: accountsResponse, isLoading: isAccountsLoading } =
    useGetAccountsQuery({
      filters: {},
      pagination: { page: 1, limit: 100, sortBy: 'name', sortOrder: 'asc' },
    });

  const accounts = useMemo(
    () => accountsResponse?.data?.data || [],
    [accountsResponse]
  );

  const STAGE_PROBABILITY: Record<OpportunityStage, number> = {
    PROSPECTING: 10,
    QUALIFICATION: 25,
    PROPOSAL: 50,
    NEGOTIATION: 75,
    CLOSED_WON: 100,
    CLOSED_LOST: 0,
  };

  useEffect(() => {
    if (!open) {
      setFormData(defaultFormData(preselectedAccountId));
      setErrors({});
      return;
    }

    setFormData((prev) => ({
      ...prev,
      accountId: prev.accountId || preselectedAccountId || '',
    }));
  }, [open, preselectedAccountId]);

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof QuickOpportunityFormData, string>> =
      {};

    if (!formData.name.trim()) {
      newErrors.name = 'Please enter an opportunity name.';
    }

    if (!formData.accountId) {
      newErrors.accountId = 'Please select an account.';
    }

    if (formData.estimatedValue < 0) {
      newErrors.estimatedValue =
        'Estimated value must be greater than or equal to 0.';
    }

    if (!formData.expectedCloseDate) {
      newErrors.expectedCloseDate = 'Please select an expected close date.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    await onSubmit({
      name: formData.name.trim(),
      accountId: formData.accountId,
      leadId: formData.leadId?.trim() || undefined,
      stage: formData.stage,
      estimatedValue: formData.estimatedValue,
      expectedCloseDate: formData.expectedCloseDate,
      assignedTo: formData.assignedTo?.trim() || undefined,
      description: formData.description?.trim() || undefined,
      notes: formData.notes?.trim() || undefined,
    });

    if (!isLoading) {
      onOpenChange(false);
      setFormData(defaultFormData(preselectedAccountId));
      setErrors({});
    }
  };

  const handleChange = (
    field: keyof QuickOpportunityFormData,
    value: string | number
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  const weightedValue =
    (formData.estimatedValue * STAGE_PROBABILITY[formData.stage]) / 100;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-3xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Create Opportunity</DialogTitle>
          <DialogDescription>
            Enter the core opportunity information and save it to your pipeline.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4 py-4'>
          <div className='space-y-2'>
            <Label htmlFor='name' className='flex items-center gap-2'>
              <Target className='h-4 w-4 text-muted-foreground' />
              Opportunity Name <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='name'
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder='E.g. Q1 2026 Service Contract'
              className={cn(errors.name && 'border-red-500')}
              disabled={isLoading}
            />
            {errors.name && (
              <p className='text-xs text-red-500'>{errors.name}</p>
            )}
          </div>

          <div className='space-y-2'>
            <Label className='flex items-center gap-2'>
              <Building2 className='h-4 w-4 text-muted-foreground' />
              Account <span className='text-red-500'>*</span>
            </Label>
            <Select
              value={formData.accountId}
              onValueChange={(value) => handleChange('accountId', value)}
              disabled={isLoading || isAccountsLoading}
            >
              <SelectTrigger
                className={cn(errors.accountId && 'border-red-500')}
              >
                <SelectValue
                  placeholder={
                    isAccountsLoading ? 'Loading accounts...' : 'Select account'
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
              <p className='text-xs text-red-500'>{errors.accountId}</p>
            )}
          </div>

          <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <Target className='h-4 w-4 text-muted-foreground' />
                Stage
              </Label>
              <Select
                value={formData.stage}
                onValueChange={(value: OpportunityStage) =>
                  handleChange('stage', value)
                }
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue />
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
              <Label htmlFor='assignedTo' className='flex items-center gap-2'>
                <User className='h-4 w-4 text-muted-foreground' />
                Assigned To
              </Label>
              <Input
                id='assignedTo'
                value={formData.assignedTo || ''}
                onChange={(e) => handleChange('assignedTo', e.target.value)}
                placeholder='Optional assignee user ID'
                disabled={isLoading}
              />
            </div>
          </div>

          <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label
                htmlFor='estimatedValue'
                className='flex items-center gap-2'
              >
                <DollarSign className='h-4 w-4 text-muted-foreground' />
                Estimated Value <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='estimatedValue'
                type='number'
                value={formData.estimatedValue || 0}
                onChange={(e) =>
                  handleChange('estimatedValue', Number(e.target.value || 0))
                }
                placeholder='0'
                className={cn(errors.estimatedValue && 'border-red-500')}
                disabled={isLoading}
              />
              {errors.estimatedValue && (
                <p className='text-xs text-red-500'>{errors.estimatedValue}</p>
              )}
            </div>

            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <Percent className='h-4 w-4 text-muted-foreground' />
                Probability
              </Label>
              <div className='rounded-md border bg-muted/30 px-3 py-2 text-sm'>
                {STAGE_PROBABILITY[formData.stage]}%
              </div>
              <p className='text-xs text-muted-foreground'>
                Weighted value: {formatCurrency(weightedValue)}
              </p>
            </div>
          </div>

          <div className='space-y-2'>
            <Label
              htmlFor='expectedCloseDate'
              className='flex items-center gap-2'
            >
              <Calendar className='h-4 w-4 text-muted-foreground' />
              Expected Close Date <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='expectedCloseDate'
              type='date'
              value={formData.expectedCloseDate}
              onChange={(e) =>
                handleChange('expectedCloseDate', e.target.value)
              }
              className={cn(errors.expectedCloseDate && 'border-red-500')}
              disabled={isLoading}
            />
            {errors.expectedCloseDate && (
              <p className='text-xs text-red-500'>{errors.expectedCloseDate}</p>
            )}
          </div>

          <div className='space-y-2'>
            <Label htmlFor='description' className='flex items-center gap-2'>
              <FileText className='h-4 w-4 text-muted-foreground' />
              Description
            </Label>
            <Textarea
              id='description'
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder='Describe the opportunity...'
              rows={3}
              disabled={isLoading}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='notes' className='flex items-center gap-2'>
              <FileText className='h-4 w-4 text-muted-foreground' />
              Notes
            </Label>
            <Textarea
              id='notes'
              value={formData.notes}
              onChange={(e) => handleChange('notes', e.target.value)}
              placeholder='Add any relevant notes...'
              rows={3}
              disabled={isLoading}
            />
          </div>

          <DialogFooter className='pt-4'>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Creating...
                </>
              ) : (
                'Create Opportunity'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default QuickAddOpportunityDialog;
