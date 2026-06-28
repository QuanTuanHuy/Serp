// LeadForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState } from 'react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { CRMDatePicker, CRMUserSelect } from '../shared';
import { toLocalDateInputValue } from '../../utils';
import type {
  CreateLeadRequest,
  Lead,
  LeadSource,
  LeadStatus,
  UpdateLeadRequest,
} from '../../types';

type FormLeadSource =
  | 'WEBSITE'
  | 'SOCIAL_MEDIA'
  | 'REFERRAL'
  | 'COLD_CALL'
  | 'EMAIL_CAMPAIGN';
type FormLeadStatus =
  | 'NEW'
  | 'CONTACTED'
  | 'NURTURING'
  | 'QUALIFIED'
  | 'DISQUALIFIED'
  | 'CONVERTED';

type LeadFormData = {
  name: string;
  email: string;
  phone: string;
  company: string;
  industry: string;
  companySize: string;
  website: string;
  jobTitle: string;
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  leadSource: FormLeadSource;
  leadStatus: FormLeadStatus;
  assignedTo: string;
  estimatedValue: string;
  leadScore: string;
  followUpDate: string;
  notes: string;
};

interface LeadFormProps {
  lead?: Lead;
  onSubmit: (data: CreateLeadRequest | UpdateLeadRequest) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

const normalizeLeadSource = (value?: LeadSource): FormLeadSource => {
  switch (value) {
    case 'SOCIAL_MEDIA':
    case 'REFERRAL':
    case 'COLD_CALL':
    case 'EMAIL_CAMPAIGN':
      return value;
    default:
      return 'WEBSITE';
  }
};

const normalizeLeadStatus = (value?: LeadStatus): FormLeadStatus => {
  switch (value) {
    case 'CONTACTED':
    case 'NURTURING':
    case 'QUALIFIED':
    case 'DISQUALIFIED':
    case 'CONVERTED':
      return value;
    default:
      return 'NEW';
  }
};

const getDefaultValues = (lead?: Lead): LeadFormData => ({
  name: lead?.name || '',
  email: lead?.email || '',
  phone: lead?.phone || '',
  company: lead?.company || '',
  industry: lead?.industry || '',
  companySize: lead?.companySize || '',
  website: lead?.website || '',
  jobTitle: lead?.jobTitle || '',
  street: lead?.address?.street || '',
  city: lead?.address?.city || '',
  state: lead?.address?.state || '',
  postalCode: lead?.address?.postalCode || '',
  country: lead?.address?.country || '',
  leadSource: normalizeLeadSource(lead?.leadSource),
  leadStatus: normalizeLeadStatus(lead?.leadStatus),
  assignedTo: lead?.assignedTo || '',
  estimatedValue:
    lead?.estimatedValue === undefined ? '' : String(lead.estimatedValue),
  leadScore: lead?.leadScore === undefined ? '' : String(lead.leadScore),
  followUpDate: lead?.followUpDate || '',
  notes: lead?.notes || '',
});

export const LeadForm: React.FC<LeadFormProps> = ({
  lead,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!lead;
  const [formData, setFormData] = useState<LeadFormData>(
    getDefaultValues(lead)
  );
  const [error, setError] = useState<string>('');

  const updateField = <K extends keyof LeadFormData>(
    field: K,
    value: LeadFormData[K]
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError('Contact name is required.');
      return;
    }

    if (!formData.email.trim()) {
      setError('Email is required.');
      return;
    }

    setError('');

    const nameParts = formData.name.trim().split(/\s+/);
    const firstName = nameParts[0] || formData.name.trim();
    const lastName = nameParts.slice(1).join(' ');

    await onSubmit({
      firstName,
      lastName,
      source: formData.leadSource,
      status: formData.leadStatus,
      priority: lead?.priority || 'MEDIUM',
      tags: lead?.tags || [],
      customFields: lead?.customFields || {},
      isActive: lead?.isActive ?? true,
      name: formData.name.trim(),
      email: formData.email.trim(),
      phone: formData.phone || undefined,
      company: formData.company || undefined,
      industry: formData.industry || undefined,
      companySize: formData.companySize || undefined,
      website: formData.website || undefined,
      jobTitle: formData.jobTitle || undefined,
      street: formData.street || undefined,
      city: formData.city || undefined,
      state: formData.state || undefined,
      postalCode: formData.postalCode || undefined,
      country: formData.country || undefined,
      leadSource: formData.leadSource,
      assignedTo: formData.assignedTo || undefined,
      estimatedValue: formData.estimatedValue
        ? Number(formData.estimatedValue)
        : undefined,
      leadScore: formData.leadScore ? Number(formData.leadScore) : undefined,
      followUpDate: formData.followUpDate || undefined,
      notes: formData.notes || undefined,
    });
  };

  return (
    <Card className={cn('w-full', className)}>
      <CardHeader>
        <h2 className='text-xl font-semibold'>
          {isEditing ? 'Edit Lead' : 'Create Lead'}
        </h2>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className='space-y-6'>
          {error && <p className='text-sm text-destructive'>{error}</p>}

          <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='name'>Contact Name *</Label>
              <Input
                id='name'
                value={formData.name}
                onChange={(e) => updateField('name', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='email'>Email *</Label>
              <Input
                id='email'
                type='email'
                value={formData.email}
                onChange={(e) => updateField('email', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='phone'>Phone</Label>
              <Input
                id='phone'
                value={formData.phone}
                onChange={(e) => updateField('phone', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='company'>Company</Label>
              <Input
                id='company'
                value={formData.company}
                onChange={(e) => updateField('company', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='jobTitle'>Job Title</Label>
              <Input
                id='jobTitle'
                value={formData.jobTitle}
                onChange={(e) => updateField('jobTitle', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='industry'>Industry</Label>
              <Input
                id='industry'
                value={formData.industry}
                onChange={(e) => updateField('industry', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='companySize'>Company Size</Label>
              <Input
                id='companySize'
                value={formData.companySize}
                onChange={(e) => updateField('companySize', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='website'>Website</Label>
              <Input
                id='website'
                value={formData.website}
                onChange={(e) => updateField('website', e.target.value)}
                disabled={isLoading}
              />
            </div>
          </div>

          <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label>Lead Source</Label>
              <Select
                value={formData.leadSource}
                onValueChange={(value) =>
                  updateField('leadSource', value as FormLeadSource)
                }
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='WEBSITE'>Website</SelectItem>
                  <SelectItem value='SOCIAL_MEDIA'>Social Media</SelectItem>
                  <SelectItem value='REFERRAL'>Referral</SelectItem>
                  <SelectItem value='COLD_CALL'>Cold Call</SelectItem>
                  <SelectItem value='EMAIL_CAMPAIGN'>Email Campaign</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label>Lead Status</Label>
              <Select
                value={formData.leadStatus}
                onValueChange={(value) =>
                  updateField('leadStatus', value as FormLeadStatus)
                }
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='NEW'>New</SelectItem>
                  <SelectItem value='CONTACTED'>Contacted</SelectItem>
                  <SelectItem value='NURTURING'>Nurturing</SelectItem>
                  <SelectItem value='QUALIFIED'>Qualified</SelectItem>
                  <SelectItem value='DISQUALIFIED'>Disqualified</SelectItem>
                  <SelectItem value='CONVERTED'>Converted</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='assignedTo'>Assigned To</Label>
              <CRMUserSelect
                id='assignedTo'
                value={formData.assignedTo}
                onChange={(v) => updateField('assignedTo', v)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='followUpDate'>Follow-up Date</Label>
              <CRMDatePicker
                id='followUpDate'
                value={formData.followUpDate}
                onChange={(date) =>
                  updateField('followUpDate', date ? toLocalDateInputValue(date) : '')
                }
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='estimatedValue'>Estimated Value</Label>
              <Input
                id='estimatedValue'
                type='number'
                value={formData.estimatedValue}
                onChange={(e) => updateField('estimatedValue', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='leadScore'>Lead Score</Label>
              <Input
                id='leadScore'
                type='number'
                value={formData.leadScore}
                onChange={(e) => updateField('leadScore', e.target.value)}
                disabled={isLoading}
              />
            </div>
          </div>

          <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='street'>Street</Label>
              <Input
                id='street'
                value={formData.street}
                onChange={(e) => updateField('street', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='city'>City</Label>
              <Input
                id='city'
                value={formData.city}
                onChange={(e) => updateField('city', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='state'>State</Label>
              <Input
                id='state'
                value={formData.state}
                onChange={(e) => updateField('state', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='postalCode'>Postal Code</Label>
              <Input
                id='postalCode'
                value={formData.postalCode}
                onChange={(e) => updateField('postalCode', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='country'>Country</Label>
              <Input
                id='country'
                value={formData.country}
                onChange={(e) => updateField('country', e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='notes'>Notes</Label>
              <Textarea
                id='notes'
                rows={4}
                value={formData.notes}
                onChange={(e) => updateField('notes', e.target.value)}
                disabled={isLoading}
              />
            </div>
          </div>

          <div className='flex justify-end gap-3 border-t pt-6'>
            {onCancel && (
              <Button
                type='button'
                variant='outline'
                onClick={onCancel}
                disabled={isLoading}
              >
                Cancel
              </Button>
            )}
            <Button type='submit' disabled={isLoading}>
              {isEditing ? 'Update Lead' : 'Create Lead'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default LeadForm;
