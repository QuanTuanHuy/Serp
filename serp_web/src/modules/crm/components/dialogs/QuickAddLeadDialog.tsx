/*
Author: QuanTuanHuy
Description: Part of Serp Project - Quick Add Lead Dialog for CRM
*/

'use client';

import { useState } from 'react';
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
import type { LeadSource } from '../../types';
import { CRMDatePicker } from '../shared';
import { toLocalDateInputValue } from '../../utils';

export interface QuickLeadFormData {
  name: string;
  email: string;
  phone?: string;
  company?: string;
  jobTitle?: string;
  leadSource: LeadSource;
  estimatedValue?: number;
  followUpDate?: string;
  notes?: string;
}

interface QuickAddLeadDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: QuickLeadFormData) => void | Promise<void>;
  isLoading?: boolean;
}

const initialState: QuickLeadFormData = {
  name: '',
  email: '',
  phone: '',
  company: '',
  jobTitle: '',
  leadSource: 'WEBSITE',
  estimatedValue: undefined,
  followUpDate: '',
  notes: '',
};

export const QuickAddLeadDialog: React.FC<QuickAddLeadDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
}) => {
  const [formData, setFormData] = useState<QuickLeadFormData>(initialState);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit(formData);
    setFormData(initialState);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='w-[95vw] max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Add New Lead</DialogTitle>
          <DialogDescription>
            Enter the core lead information.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='quick-name'>Contact Name</Label>
              <Input
                id='quick-name'
                value={formData.name}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, name: e.target.value }))
                }
                required
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='quick-email'>Email</Label>
              <Input
                id='quick-email'
                type='email'
                value={formData.email}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, email: e.target.value }))
                }
                required
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='quick-phone'>Phone</Label>
              <Input
                id='quick-phone'
                value={formData.phone}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, phone: e.target.value }))
                }
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='quick-company'>Company</Label>
              <Input
                id='quick-company'
                value={formData.company}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, company: e.target.value }))
                }
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='quick-jobTitle'>Job Title</Label>
              <Input
                id='quick-jobTitle'
                value={formData.jobTitle}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, jobTitle: e.target.value }))
                }
              />
            </div>
            <div className='space-y-2'>
              <Label>Lead Source</Label>
              <Select
                value={formData.leadSource}
                onValueChange={(value) =>
                  setFormData((prev) => ({
                    ...prev,
                    leadSource: value as LeadSource,
                  }))
                }
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
              <Label htmlFor='quick-estimatedValue'>Estimated Value</Label>
              <Input
                id='quick-estimatedValue'
                type='number'
                value={formData.estimatedValue ?? ''}
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    estimatedValue: e.target.value
                      ? Number(e.target.value)
                      : undefined,
                  }))
                }
              />
            </div>
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='quick-followUpDate'>Follow-up Date</Label>
              <CRMDatePicker
                id='quick-followUpDate'
                value={formData.followUpDate}
                onChange={(date) =>
                  setFormData((prev) => ({
                    ...prev,
                    followUpDate: date ? toLocalDateInputValue(date) : '',
                  }))
                }
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='quick-notes'>Notes</Label>
              <Textarea
                id='quick-notes'
                rows={3}
                value={formData.notes}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, notes: e.target.value }))
                }
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading}>
              Create Lead
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default QuickAddLeadDialog;
