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
  MessageSquare,
  Presentation,
  CheckSquare,
  MapPin,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import type { ActivityType, ActivityStatus, Priority } from '../../types';
import { MOCK_CUSTOMERS, MOCK_LEADS, MOCK_OPPORTUNITIES } from '../../mocks';

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
}

interface QuickAddActivityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: QuickActivityFormData) => void;
  isLoading?: boolean;
  preselectedRelation?: {
    type: 'CUSTOMER' | 'LEAD' | 'OPPORTUNITY';
    id: string;
  };
}

const ACTIVITY_TYPE_CONFIG = [
  { type: 'CALL' as ActivityType, label: 'Cuộc gọi', icon: Phone },
  { type: 'EMAIL' as ActivityType, label: 'Email', icon: Mail },
  { type: 'MEETING' as ActivityType, label: 'Cuộc họp', icon: Video },
  { type: 'TASK' as ActivityType, label: 'Công việc', icon: CheckSquare },
  { type: 'NOTE' as ActivityType, label: 'Ghi chú', icon: MessageSquare },
  { type: 'DEMO' as ActivityType, label: 'Demo', icon: Presentation },
  { type: 'PROPOSAL' as ActivityType, label: 'Proposal', icon: FileText },
  { type: 'FOLLOW_UP' as ActivityType, label: 'Follow up', icon: Target },
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
    if (validateForm()) {
      onSubmit(formData);
      resetForm();
    }
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
        return MOCK_CUSTOMERS.map((c) => ({
          id: c.id,
          name: c.name,
          subtitle: c.companyName,
        }));
      case 'LEAD':
        return MOCK_LEADS.map((l) => ({
          id: l.id,
          name: `${l.firstName} ${l.lastName}`,
          subtitle: l.company,
        }));
      case 'OPPORTUNITY':
        return MOCK_OPPORTUNITIES.map((o) => ({
          id: o.id,
          name: o.name,
          subtitle: o.customerName,
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
          <DialogTitle>Tạo hoạt động mới</DialogTitle>
          <DialogDescription>
            Lên lịch cuộc gọi, họp, task hoặc hoạt động khác
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4 py-4'>
          {/* Activity Type */}
          <div className='space-y-2'>
            <Label>Loại hoạt động</Label>
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
              Tiêu đề <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='subject'
              value={formData.subject}
              onChange={(e) => handleChange('subject', e.target.value)}
              placeholder='VD: Gọi điện giới thiệu sản phẩm'
              className={cn(errors.subject && 'border-red-500')}
            />
            {errors.subject && (
              <p className='text-xs text-red-500'>{errors.subject}</p>
            )}
          </div>

          {/* Related To */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label>Liên quan đến</Label>
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
                Ngày
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
                Giờ
              </Label>
              <Input
                id='scheduledTime'
                type='time'
                value={formData.scheduledTime}
                onChange={(e) => handleChange('scheduledTime', e.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='duration'>Thời lượng (phút)</Label>
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
                  <SelectItem value='15'>15 phút</SelectItem>
                  <SelectItem value='30'>30 phút</SelectItem>
                  <SelectItem value='45'>45 phút</SelectItem>
                  <SelectItem value='60'>1 giờ</SelectItem>
                  <SelectItem value='90'>1.5 giờ</SelectItem>
                  <SelectItem value='120'>2 giờ</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Priority & Status */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <Target className='h-4 w-4 text-muted-foreground' />
                Mức độ ưu tiên
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
                  <SelectItem value='LOW'>Thấp</SelectItem>
                  <SelectItem value='MEDIUM'>Trung bình</SelectItem>
                  <SelectItem value='HIGH'>Cao</SelectItem>
                  <SelectItem value='URGENT'>Khẩn cấp</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label>Trạng thái</Label>
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
                  <SelectItem value='PLANNED'>Đã lên kế hoạch</SelectItem>
                  <SelectItem value='IN_PROGRESS'>Đang thực hiện</SelectItem>
                  <SelectItem value='COMPLETED'>Hoàn thành</SelectItem>
                  <SelectItem value='CANCELLED'>Đã hủy</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Location (for meetings) */}
          {(formData.type === 'MEETING' || formData.type === 'DEMO') && (
            <div className='space-y-2'>
              <Label htmlFor='location' className='flex items-center gap-2'>
                <MapPin className='h-4 w-4 text-muted-foreground' />
                Địa điểm
              </Label>
              <Input
                id='location'
                value={formData.location}
                onChange={(e) => handleChange('location', e.target.value)}
                placeholder='VD: Phòng họp A hoặc link Google Meet'
              />
            </div>
          )}

          {/* Description */}
          <div className='space-y-2'>
            <Label htmlFor='description' className='flex items-center gap-2'>
              <FileText className='h-4 w-4 text-muted-foreground' />
              Mô tả
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
                  Đang tạo...
                </>
              ) : (
                'Tạo hoạt động'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default QuickAddActivityDialog;
