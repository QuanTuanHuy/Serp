/*
Author: QuanTuanHuy
Description: Part of Serp Project - Quick Add Activity Dialog for CRM
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
} from '../../types';
import {
  useGetCustomersQuery,
  useGetLeadsQuery,
  useGetOpportunitiesQuery,
} from '../../api/crmApi';

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
}) => {
  const [formData, setFormData] = useState<QuickActivityFormData>({
    type: 'CALL',
    subject: '',
    description: '',
    scheduledDate: '',
    scheduledTime: '',
    duration: 30,
    priority: 'MEDIUM',
    status: 'PLANNED',
    relatedTo: preselectedRelation || { type: 'CUSTOMER', id: '' },
    location: '',
    assignedTo: '',
  });

  const { data: customersData } = useGetCustomersQuery({
    filters: {},
    pagination: { page: 1, limit: 50 },
  });
  const { data: leadsData } = useGetLeadsQuery({
    filters: {},
    pagination: { page: 1, limit: 50 },
  });
  const { data: opportunitiesData } = useGetOpportunitiesQuery({
    filters: {},
    pagination: { page: 1, limit: 50 },
  });

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
    });
    resetForm();
  };

  const resetForm = () => {
    setFormData({
      type: 'CALL',
      subject: '',
      description: '',
      scheduledDate: '',
      scheduledTime: '',
      duration: 30,
      priority: 'MEDIUM',
      status: 'PLANNED',
      relatedTo: preselectedRelation || { type: 'CUSTOMER', id: '' },
      location: '',
      assignedTo: '',
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

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-lg max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Create New Activity</DialogTitle>
          <DialogDescription>
            Schedule a call, meeting, task, or other activity
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4 py-4'>
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
                    'flex flex-col items-center gap-1 p-3 rounded-lg border transition-colors',
                    formData.type === type
                      ? 'border-primary bg-primary/10 text-primary'
                      : 'border-muted hover:border-primary/50'
                  )}
                >
                  <Icon className='h-5 w-5' />
                  <span className='text-xs'>{label}</span>
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
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label>Related To</Label>
              <Select
                value={formData.relatedTo.type}
                onValueChange={(value: 'CUSTOMER' | 'LEAD' | 'OPPORTUNITY') => {
                  handleChange('relatedTo.type', value);
                  handleChange('relatedTo.id', '');
                }}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='CUSTOMER'>Customer</SelectItem>
                  <SelectItem value='LEAD'>Lead</SelectItem>
                  <SelectItem value='OPPORTUNITY'>Opportunity</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label>
                Select Object <span className='text-red-500'>*</span>
              </Label>
              <Select
                value={formData.relatedTo.id}
                onValueChange={(value) => handleChange('relatedTo.id', value)}
              >
                <SelectTrigger
                  className={cn(errors.relatedToId && 'border-red-500')}
                >
                  <SelectValue placeholder='Select...' />
                </SelectTrigger>
                <SelectContent>
                  {relatedOptions.map((option) => (
                    <SelectItem key={option.id} value={option.id}>
                      <div>
                        <span>{option.name}</span>
                        {option.subtitle && (
                          <span className='text-muted-foreground ml-1'>
                            ({option.subtitle})
                          </span>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.relatedToId && (
                <p className='text-xs text-red-500'>{errors.relatedToId}</p>
              )}
            </div>
          </div>

          {/* Date & Time */}
          <div className='grid grid-cols-3 gap-4'>
            <div className='space-y-2'>
              <Label
                htmlFor='scheduledDate'
                className='flex items-center gap-2'
              >
                <Calendar className='h-4 w-4 text-muted-foreground' />
                Date
              </Label>
              <Input
                id='scheduledDate'
                type='date'
                value={formData.scheduledDate}
                onChange={(e) => handleChange('scheduledDate', e.target.value)}
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
                onChange={(e) => handleChange('scheduledTime', e.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='duration'>Duration (minutes)</Label>
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
                  <SelectItem value='15'>15 minutes</SelectItem>
                  <SelectItem value='30'>30 minutes</SelectItem>
                  <SelectItem value='45'>45 minutes</SelectItem>
                  <SelectItem value='60'>1 hour</SelectItem>
                  <SelectItem value='90'>1.5 hours</SelectItem>
                  <SelectItem value='120'>2 hours</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Priority & Status */}
          <div className='grid grid-cols-2 gap-4'>
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
                  <SelectItem value='LOW'>Low</SelectItem>
                  <SelectItem value='MEDIUM'>Medium</SelectItem>
                  <SelectItem value='HIGH'>High</SelectItem>
                  <SelectItem value='URGENT'>Urgent</SelectItem>
                </SelectContent>
              </Select>
            </div>

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
                  <SelectItem value='PLANNED'>Planned</SelectItem>
                  <SelectItem value='COMPLETED'>Completed</SelectItem>
                  <SelectItem value='CANCELLED'>Cancelled</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Assigned To */}
          <div className='space-y-2'>
            <Label className='flex items-center gap-2'>
              <Users className='h-4 w-4 text-muted-foreground' />
              Assigned To (Optional)
            </Label>
            <Input
              type='number'
              value={formData.assignedTo}
              onChange={(e) => handleChange('assignedTo', e.target.value)}
              placeholder='Enter user ID'
            />
            <p className='text-xs text-muted-foreground'>
              Leave empty to assign to yourself
            </p>
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
            <Label htmlFor='description' className='flex items-center gap-2'>
              <FileText className='h-4 w-4 text-muted-foreground' />
              Description
            </Label>
            <Textarea
              id='description'
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder='Add detailed description...'
              rows={3}
            />
          </div>

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
