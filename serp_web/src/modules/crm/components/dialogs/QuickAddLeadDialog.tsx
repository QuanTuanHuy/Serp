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
import {
  User,
  Building2,
  Mail,
  Phone,
  Briefcase,
  DollarSign,
  Calendar,
  FileText,
  Loader2,
  Globe,
  Target,
  Users,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import type { LeadSource, LeadStatus, Priority } from '../../types';

export interface QuickLeadFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  company?: string;
  jobTitle?: string;
  source: LeadSource;
  status: LeadStatus;
  priority: Priority;
  estimatedValue?: number;
  expectedCloseDate?: string;
  notes?: string;
}

interface QuickAddLeadDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: QuickLeadFormData) => void;
  isLoading?: boolean;
}

export const QuickAddLeadDialog: React.FC<QuickAddLeadDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
}) => {
  const [formData, setFormData] = useState<QuickLeadFormData>({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    company: '',
    jobTitle: '',
    source: 'WEBSITE',
    status: 'NEW',
    priority: 'MEDIUM',
    estimatedValue: undefined,
    expectedCloseDate: '',
    notes: '',
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof QuickLeadFormData, string>>
  >({});

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof QuickLeadFormData, string>> = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Vui lòng nhập tên';
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Vui lòng nhập họ';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Vui lòng nhập email';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Email không hợp lệ';
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
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      company: '',
      jobTitle: '',
      source: 'WEBSITE',
      status: 'NEW',
      priority: 'MEDIUM',
      estimatedValue: undefined,
      expectedCloseDate: '',
      notes: '',
    });
    setErrors({});
  };

  const handleChange = (
    field: keyof QuickLeadFormData,
    value: string | number | undefined
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-3xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Thêm Lead mới</DialogTitle>
          <DialogDescription>
            Nhập thông tin lead tiềm năng mới
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4 py-4'>
          {/* Name Fields */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='firstName'>
                Tên <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='firstName'
                value={formData.firstName}
                onChange={(e) => handleChange('firstName', e.target.value)}
                placeholder='Nhập tên'
                className={cn(errors.firstName && 'border-red-500')}
              />
              {errors.firstName && (
                <p className='text-xs text-red-500'>{errors.firstName}</p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='lastName'>
                Họ <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='lastName'
                value={formData.lastName}
                onChange={(e) => handleChange('lastName', e.target.value)}
                placeholder='Nhập họ'
                className={cn(errors.lastName && 'border-red-500')}
              />
              {errors.lastName && (
                <p className='text-xs text-red-500'>{errors.lastName}</p>
              )}
            </div>
          </div>

          {/* Email */}
          <div className='space-y-2'>
            <Label htmlFor='email' className='flex items-center gap-2'>
              <Mail className='h-4 w-4 text-muted-foreground' />
              Email <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='email'
              type='email'
              value={formData.email}
              onChange={(e) => handleChange('email', e.target.value)}
              placeholder='email@example.com'
              className={cn(errors.email && 'border-red-500')}
            />
            {errors.email && (
              <p className='text-xs text-red-500'>{errors.email}</p>
            )}
          </div>

          {/* Phone */}
          <div className='space-y-2'>
            <Label htmlFor='phone' className='flex items-center gap-2'>
              <Phone className='h-4 w-4 text-muted-foreground' />
              Số điện thoại
            </Label>
            <Input
              id='phone'
              type='tel'
              value={formData.phone}
              onChange={(e) => handleChange('phone', e.target.value)}
              placeholder='+84 xxx xxx xxx'
            />
          </div>

          {/* Company & Job Title */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='company' className='flex items-center gap-2'>
                <Building2 className='h-4 w-4 text-muted-foreground' />
                Công ty
              </Label>
              <Input
                id='company'
                value={formData.company}
                onChange={(e) => handleChange('company', e.target.value)}
                placeholder='Tên công ty'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='jobTitle' className='flex items-center gap-2'>
                <Briefcase className='h-4 w-4 text-muted-foreground' />
                Chức vụ
              </Label>
              <Input
                id='jobTitle'
                value={formData.jobTitle}
                onChange={(e) => handleChange('jobTitle', e.target.value)}
                placeholder='VD: Giám đốc'
              />
            </div>
          </div>

          {/* Source & Priority */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <Globe className='h-4 w-4 text-muted-foreground' />
                Nguồn
              </Label>
              <Select
                value={formData.source}
                onValueChange={(value: LeadSource) =>
                  handleChange('source', value)
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='WEBSITE'>Website</SelectItem>
                  <SelectItem value='REFERRAL'>Giới thiệu</SelectItem>
                  <SelectItem value='EMAIL'>Email</SelectItem>
                  <SelectItem value='PHONE'>Điện thoại</SelectItem>
                  <SelectItem value='SOCIAL_MEDIA'>Mạng xã hội</SelectItem>
                  <SelectItem value='TRADE_SHOW'>Triển lãm</SelectItem>
                  <SelectItem value='OTHER'>Khác</SelectItem>
                </SelectContent>
              </Select>
            </div>

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
          </div>

          {/* Estimated Value & Expected Close Date */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label
                htmlFor='estimatedValue'
                className='flex items-center gap-2'
              >
                <DollarSign className='h-4 w-4 text-muted-foreground' />
                Giá trị ước tính
              </Label>
              <Input
                id='estimatedValue'
                type='number'
                value={formData.estimatedValue || ''}
                onChange={(e) =>
                  handleChange(
                    'estimatedValue',
                    e.target.value ? Number(e.target.value) : undefined
                  )
                }
                placeholder='VNĐ'
              />
            </div>

            <div className='space-y-2'>
              <Label
                htmlFor='expectedCloseDate'
                className='flex items-center gap-2'
              >
                <Calendar className='h-4 w-4 text-muted-foreground' />
                Ngày đóng dự kiến
              </Label>
              <Input
                id='expectedCloseDate'
                type='date'
                value={formData.expectedCloseDate}
                onChange={(e) =>
                  handleChange('expectedCloseDate', e.target.value)
                }
              />
            </div>
          </div>

          {/* Notes */}
          <div className='space-y-2'>
            <Label htmlFor='notes' className='flex items-center gap-2'>
              <FileText className='h-4 w-4 text-muted-foreground' />
              Ghi chú
            </Label>
            <Textarea
              id='notes'
              value={formData.notes}
              onChange={(e) => handleChange('notes', e.target.value)}
              placeholder='Thêm ghi chú về lead...'
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
              Hủy
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Đang tạo...
                </>
              ) : (
                'Tạo Lead'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default QuickAddLeadDialog;
