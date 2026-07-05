/*
Author: QuanTuanHuy
Description: Part of Serp Project - Quick Add Activity Dialog for CRM
*/

'use client';

import { useState, useEffect, useMemo } from 'react';
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
  Calendar,
  Clock,
  FileText,
  Loader2,
  Phone,
  Mail,
  Video,
  Users,
  Target,
  CheckSquare,
  MapPin,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import type {
  ActivityStatus,
  ActivityType,
  BackendActivityStatus,
  BackendActivityType,
  CreateActivityRequest,
  Priority,
  ActivityOutcome,
} from '../../types';
import {
  useGetCustomersQuery,
  useGetLeadsQuery,
  useGetOpportunitiesQuery,
} from '../../api/crmApi';
import { CRMDatePicker, CRMUserSelect } from '../shared';
import { toLocalDateInputValue } from '../../utils';
import { useDebounce } from '@/shared/hooks';
import { Combobox } from '@/shared/components/ui/combobox';

export interface QuickActivityFormData {
  type: ActivityType;
  subject: string;
  description?: string;
  scheduledDate?: string;
  scheduledTime?: string;
  duration?: number;
  priority: Priority;
  status: ActivityStatus;
  relatedTo: {
    type: 'CUSTOMER' | 'LEAD' | 'OPPORTUNITY';
    id: string;
  };
  location?: string;
  assignedTo?: string;
  outcome?: string;
  notes?: string;
  progressPercent?: number;
}

interface QuickAddActivityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateActivityRequest) => void;
  isLoading?: boolean;
  preselectedRelation?: {
    type: 'CUSTOMER' | 'LEAD' | 'OPPORTUNITY';
    id: string;
  };
  preselectedDate?: string;
}

const ACTIVITY_TYPE_CONFIG = [
  { type: 'CALL' as ActivityType, label: 'Call', icon: Phone },
  { type: 'EMAIL' as ActivityType, label: 'Email', icon: Mail },
  { type: 'MEETING' as ActivityType, label: 'Meeting', icon: Video },
  { type: 'TASK' as ActivityType, label: 'Task', icon: CheckSquare },
];

export const QuickAddActivityDialog: React.FC<QuickAddActivityDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
  preselectedRelation,
  preselectedDate,
}) => {
  const [formData, setFormData] = useState<QuickActivityFormData>({
    type: 'CALL',
    subject: '',
    description: '',
    scheduledDate: preselectedDate || '',
    scheduledTime: preselectedDate ? '09:00' : '',
    duration: 30,
    priority: 'MEDIUM',
    status: 'PLANNED',
    relatedTo: preselectedRelation || { type: 'CUSTOMER', id: '' },
    location: '',
    assignedTo: '',
    outcome: '',
    notes: '',
    progressPercent: 0,
  });

  useEffect(() => {
    if (open) {
      setFormData((prev) => ({
        ...prev,
        scheduledDate: preselectedDate || '',
        scheduledTime: preselectedDate ? '09:00' : '',
        relatedTo: preselectedRelation || { type: 'CUSTOMER', id: '' },
        outcome: '',
        notes: '',
        progressPercent: 0,
      }));
    }
  }, [open, preselectedDate, preselectedRelation]);

  const [relationSearch, setRelationSearch] = useState('');
  const debouncedRelationSearch = useDebounce(relationSearch, 300);

  const { data: customersData, isFetching: isFetchingCustomers } =
    useGetCustomersQuery({
      filters: { search: debouncedRelationSearch },
      pagination: { page: 1, limit: 50 },
    });
  const { data: leadsData, isFetching: isFetchingLeads } = useGetLeadsQuery({
    filters: { search: debouncedRelationSearch },
    pagination: { page: 1, limit: 50 },
  });
  const { data: opportunitiesData, isFetching: isFetchingOpportunities } =
    useGetOpportunitiesQuery({
      filters: { search: debouncedRelationSearch },
      pagination: { page: 1, limit: 50 },
    });

  const isFetchingRelated =
    isFetchingCustomers || isFetchingLeads || isFetchingOpportunities;

  useEffect(() => {
    setRelationSearch('');
  }, [formData.relatedTo.type]);

  const [errors, setErrors] = useState<Partial<Record<string, string>>>({});

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<string, string>> = {};

    if (!formData.subject.trim()) {
      newErrors.subject = 'Please enter subject';
    }

    if (!formData.relatedTo.id) {
      newErrors.relatedToId = 'Please select related object';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    const scheduledAt = formData.scheduledDate
      ? new Date(
          `${formData.scheduledDate}T${formData.scheduledTime || '00:00'}`
        ).getTime()
      : undefined;
    const relatedId = Number(formData.relatedTo.id);

    onSubmit({
      subject: formData.subject,
      description: formData.description || undefined,
      activityType: formData.type as BackendActivityType,
      status: formData.status as BackendActivityStatus,
      location: formData.location || undefined,
      activityDate: scheduledAt,
      dueDate: formData.type === 'TASK' ? scheduledAt : undefined,
      durationMinutes: formData.duration,
      priority: formData.priority,
      accountId: formData.relatedTo.type === 'CUSTOMER' ? relatedId : undefined,
      leadId: formData.relatedTo.type === 'LEAD' ? relatedId : undefined,
      opportunityId:
        formData.relatedTo.type === 'OPPORTUNITY' ? relatedId : undefined,
      assignedTo: formData.assignedTo ? Number(formData.assignedTo) : undefined,
      outcome:
        formData.status === 'COMPLETED' && formData.outcome
          ? (formData.outcome as ActivityOutcome)
          : undefined,
      notes:
        formData.status === 'COMPLETED' && formData.notes
          ? formData.notes
          : undefined,
      progressPercent:
        formData.type === 'TASK' ? formData.progressPercent : undefined,
    });
    resetForm();
  };

  const resetForm = () => {
    setFormData({
      type: 'CALL',
      subject: '',
      description: '',
      scheduledDate: preselectedDate || '',
      scheduledTime: preselectedDate ? '09:00' : '',
      duration: 30,
      priority: 'MEDIUM',
      status: 'PLANNED',
      relatedTo: preselectedRelation || { type: 'CUSTOMER', id: '' },
      location: '',
      assignedTo: '',
      outcome: '',
      notes: '',
      progressPercent: 0,
    });
    setErrors({});
  };

  const handleChange = (field: string, value: any) => {
    if (field.startsWith('relatedTo.')) {
      const subField = field.split('.')[1];
      setFormData((prev) => ({
        ...prev,
        relatedTo: { ...prev.relatedTo, [subField]: value },
      }));
    } else {
      setFormData((prev) => ({ ...prev, [field]: value }));
    }

    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  // Get related entities based on type
  const getRelatedOptions = () => {
    switch (formData.relatedTo.type) {
      case 'CUSTOMER':
        return (customersData?.data?.data || []).map((customer) => ({
          id: customer.id,
          name: customer.name,
          subtitle: customer.companyName,
        }));
      case 'LEAD':
        return (leadsData?.data?.data || []).map((lead) => ({
          id: lead.id,
          name: lead.name || `${lead.firstName} ${lead.lastName}`.trim(),
          subtitle: lead.company,
        }));
      case 'OPPORTUNITY':
        return (opportunitiesData?.data?.data || []).map((opportunity) => ({
          id: opportunity.id,
          name: opportunity.name,
          subtitle: opportunity.customerName,
        }));
      default:
        return [];
    }
  };

  const relatedOptions = getRelatedOptions();

  const [selectedItemName, setSelectedItemName] = useState('');

  // Keep track of selected item name
  useEffect(() => {
    if (formData.relatedTo.id) {
      const found = relatedOptions.find(
        (opt) => String(opt.id) === String(formData.relatedTo.id)
      );
      if (found) {
        setSelectedItemName(found.name);
      }
    } else {
      setSelectedItemName('');
    }
  }, [formData.relatedTo.id, relatedOptions]);

  const comboboxItems = useMemo(() => {
    const list = relatedOptions.map((opt) => ({
      value: String(opt.id),
      label: opt.subtitle ? `${opt.name} (${opt.subtitle})` : opt.name,
    }));

    // Fallback/Ensure selected item is always in the items list
    if (
      formData.relatedTo.id &&
      !list.some((item) => String(item.value) === String(formData.relatedTo.id))
    ) {
      list.unshift({
        value: String(formData.relatedTo.id),
        label: selectedItemName || `Selected (ID: ${formData.relatedTo.id})`,
      });
    }
    return list;
  }, [relatedOptions, formData.relatedTo.id, selectedItemName]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-4xl w-full max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Create New Activity</DialogTitle>
          <DialogDescription>
            Schedule a call, meeting, task, or other activity
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-6 py-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
            {/* Cột trái: General Information */}
            <div className='space-y-4'>
              {/* Activity Type */}
              <div className='space-y-2'>
                <Label>Activity Type</Label>
                <div className='grid grid-cols-4 gap-2'>
                  {ACTIVITY_TYPE_CONFIG.map(({ type, label, icon: Icon }) => (
                    <button
                      key={type}
                      type='button'
                      onClick={() => handleChange('type', type)}
                      className={cn(
                        'flex flex-col items-center gap-1.5 p-3 rounded-lg border transition-colors cursor-pointer text-center',
                        formData.type === type
                          ? 'border-primary bg-primary/10 text-primary'
                          : 'border-muted hover:border-primary/50'
                      )}
                    >
                      <Icon className='h-5 w-5' />
                      <span className='text-xs font-medium'>{label}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Subject */}
              <div className='space-y-2'>
                <Label htmlFor='subject'>
                  Subject <span className='text-red-500'>*</span>
                </Label>
                <Input
                  id='subject'
                  value={formData.subject}
                  onChange={(e) => handleChange('subject', e.target.value)}
                  placeholder='Ex: Call to introduce product'
                  className={cn(errors.subject && 'border-red-500')}
                />
                {errors.subject && (
                  <p className='text-xs text-red-500'>{errors.subject}</p>
                )}
              </div>

              {/* Related To */}
              <div className='space-y-2'>
                <Label>Related To</Label>
                <div className='flex gap-1 p-1 bg-muted rounded-lg w-full mb-2'>
                  {(['CUSTOMER', 'LEAD', 'OPPORTUNITY'] as const).map(
                    (type) => (
                      <button
                        key={type}
                        type='button'
                        onClick={() => {
                          handleChange('relatedTo.type', type);
                          handleChange('relatedTo.id', '');
                        }}
                        className={cn(
                          'flex-1 py-1.5 px-3 rounded-md text-xs font-medium transition-all cursor-pointer text-center',
                          formData.relatedTo.type === type
                            ? 'bg-background text-foreground shadow-sm'
                            : 'text-muted-foreground hover:text-foreground'
                        )}
                      >
                        {type === 'CUSTOMER'
                          ? 'Customer'
                          : type === 'LEAD'
                            ? 'Lead'
                            : 'Opportunity'}
                      </button>
                    )
                  )}
                </div>
                <Combobox
                  value={formData.relatedTo.id}
                  onChange={(value) => handleChange('relatedTo.id', value)}
                  items={comboboxItems}
                  placeholder={`Search ${formData.relatedTo.type.toLowerCase()}...`}
                  onSearch={setRelationSearch}
                  loading={isFetchingRelated}
                  modal={true}
                  className={cn(errors.relatedToId && 'border-red-500')}
                />
                {errors.relatedToId && (
                  <p className='text-xs text-red-500'>{errors.relatedToId}</p>
                )}
              </div>

              {/* Location (for meetings) */}
              {formData.type === 'MEETING' && (
                <div className='space-y-2'>
                  <Label htmlFor='location' className='flex items-center gap-2'>
                    <MapPin className='h-4 w-4 text-muted-foreground' />
                    Location
                  </Label>
                  <Input
                    id='location'
                    value={formData.location}
                    onChange={(e) => handleChange('location', e.target.value)}
                    placeholder='Ex: Meeting Room A or Google Meet link'
                  />
                </div>
              )}

              {/* Description */}
              <div className='space-y-2'>
                <Label
                  htmlFor='description'
                  className='flex items-center gap-2'
                >
                  <FileText className='h-4 w-4 text-muted-foreground' />
                  Description
                </Label>
                <Textarea
                  id='description'
                  value={formData.description}
                  onChange={(e) => handleChange('description', e.target.value)}
                  placeholder='Add detailed description...'
                  rows={4}
                />
              </div>
            </div>

            {/* Cột phải: Scheduling & Assignee */}
            <div className='space-y-4'>
              {/* Status */}
              <div className='space-y-2'>
                <Label>Status</Label>
                <Select
                  value={formData.status}
                  onValueChange={(value: ActivityStatus) =>
                    handleChange('status', value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='PLANNED'>
                      <span className='inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300'>
                        Planned
                      </span>
                    </SelectItem>
                    <SelectItem value='COMPLETED'>
                      <span className='inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300'>
                        Completed
                      </span>
                    </SelectItem>
                    <SelectItem value='CANCELLED'>
                      <span className='inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300'>
                        Cancelled
                      </span>
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Date & Time Grid */}
              <div className='grid grid-cols-3 gap-4'>
                <div className='space-y-2'>
                  <Label
                    htmlFor='scheduledDate'
                    className='flex items-center gap-2'
                  >
                    <Calendar className='h-4 w-4 text-muted-foreground' />
                    Date
                  </Label>
                  <CRMDatePicker
                    id='scheduledDate'
                    value={formData.scheduledDate}
                    onChange={(date) =>
                      handleChange(
                        'scheduledDate',
                        date ? toLocalDateInputValue(date) : ''
                      )
                    }
                    disabled={isLoading}
                  />
                </div>

                <div className='space-y-2'>
                  <Label
                    htmlFor='scheduledTime'
                    className='flex items-center gap-2'
                  >
                    <Clock className='h-4 w-4 text-muted-foreground' />
                    Time
                  </Label>
                  <Input
                    id='scheduledTime'
                    type='time'
                    value={formData.scheduledTime}
                    onChange={(e) =>
                      handleChange('scheduledTime', e.target.value)
                    }
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='duration'>Duration</Label>
                  <Select
                    value={String(formData.duration)}
                    onValueChange={(value) =>
                      handleChange('duration', Number(value))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='15'>15 mins</SelectItem>
                      <SelectItem value='30'>30 mins</SelectItem>
                      <SelectItem value='45'>45 mins</SelectItem>
                      <SelectItem value='60'>1 hour</SelectItem>
                      <SelectItem value='90'>1.5 hours</SelectItem>
                      <SelectItem value='120'>2 hours</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Priority */}
              <div className='space-y-2'>
                <Label className='flex items-center gap-2'>
                  <Target className='h-4 w-4 text-muted-foreground' />
                  Priority
                </Label>
                <Select
                  value={formData.priority}
                  onValueChange={(value: Priority) =>
                    handleChange('priority', value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='LOW'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-green-500' />
                        Low
                      </div>
                    </SelectItem>
                    <SelectItem value='MEDIUM'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-blue-500' />
                        Medium
                      </div>
                    </SelectItem>
                    <SelectItem value='HIGH'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-orange-500' />
                        High
                      </div>
                    </SelectItem>
                    <SelectItem value='URGENT'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-red-500' />
                        Urgent
                      </div>
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Progress Slider (for Tasks) */}
              {formData.type === 'TASK' && (
                <div className='space-y-2'>
                  <div className='flex justify-between items-center'>
                    <Label htmlFor='progressPercent'>Progress</Label>
                    <span className='text-xs font-semibold text-muted-foreground'>
                      {formData.progressPercent}%
                    </span>
                  </div>
                  <div className='flex items-center gap-4'>
                    <input
                      id='progressPercent'
                      type='range'
                      min='0'
                      max='100'
                      value={formData.progressPercent}
                      onChange={(e) =>
                        handleChange('progressPercent', Number(e.target.value))
                      }
                      className='flex-1 h-1.5 bg-muted rounded-lg appearance-none cursor-pointer accent-primary'
                    />
                  </div>
                </div>
              )}

              {/* Assigned To */}
              <div className='space-y-2'>
                <Label className='flex items-center gap-2'>
                  <Users className='h-4 w-4 text-muted-foreground' />
                  Assigned To
                </Label>
                <CRMUserSelect
                  value={formData.assignedTo}
                  onChange={(v) => handleChange('assignedTo', v)}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>

          {/* Completion Details Card (Expands when status === COMPLETED) */}
          {formData.status === 'COMPLETED' && (
            <div className='space-y-4 p-4 border rounded-lg bg-muted/20 animate-fade-in'>
              <Label className='text-sm font-semibold block'>
                Completion Details
              </Label>

              {formData.type === 'CALL' && (
                <div className='space-y-2'>
                  <Label htmlFor='outcome'>Call Outcome</Label>
                  <Select
                    value={formData.outcome}
                    onValueChange={(value) => handleChange('outcome', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select outcome' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='REACHED'>Reached</SelectItem>
                      <SelectItem value='VOICEMAIL'>Voicemail</SelectItem>
                      <SelectItem value='NO_ANSWER'>No Answer</SelectItem>
                      <SelectItem value='BUSY'>Busy</SelectItem>
                      <SelectItem value='WRONG_NUMBER'>Wrong Number</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              {formData.type === 'MEETING' && (
                <div className='space-y-2'>
                  <Label htmlFor='outcome'>Meeting Outcome</Label>
                  <Select
                    value={formData.outcome}
                    onValueChange={(value) => handleChange('outcome', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select outcome' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='OCCURRED'>Occurred</SelectItem>
                      <SelectItem value='NO_SHOW'>No Show</SelectItem>
                      <SelectItem value='RESCHEDULED'>Rescheduled</SelectItem>
                      <SelectItem value='CANCELLED_BY_CUSTOMER'>
                        Cancelled by Customer
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              <div className='space-y-2'>
                <Label htmlFor='notes'>Notes / Summary</Label>
                <Textarea
                  id='notes'
                  value={formData.notes}
                  onChange={(e) => handleChange('notes', e.target.value)}
                  placeholder='Add summary or notes...'
                  rows={4}
                />
              </div>
            </div>
          )}

          <DialogFooter className='pt-4'>
            <Button
              type='button'
              variant='outline'
              onClick={() => {
                onOpenChange(false);
                resetForm();
              }}
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
                'Create Activity'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default QuickAddActivityDialog;
