/*
Author: QuanTuanHuy
Description: Part of Serp Project - Contact Form Component for CRM
*/

'use client';

import { useState } from 'react';
import { Button, Input, Label, Textarea, Switch } from '@/shared/components/ui';
import {
  User,
  Mail,
  Phone,
  Briefcase,
  Building2,
  Link,
  FileText,
  Loader2,
} from 'lucide-react';
import { cn } from '@/shared/utils';

export interface ContactFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  jobTitle?: string;
  department?: string;
  isPrimary?: boolean;
  linkedInUrl?: string;
  notes?: string;
}

interface ContactFormProps {
  initialData?: Partial<ContactFormData>;
  onSubmit: (data: ContactFormData) => void;
  onCancel: () => void;
  isLoading?: boolean;
  isEditing?: boolean;
  className?: string;
}

export const ContactForm: React.FC<ContactFormProps> = ({
  initialData = {},
  onSubmit,
  onCancel,
  isLoading = false,
  isEditing = false,
  className,
}) => {
  const [formData, setFormData] = useState<ContactFormData>({
    firstName: initialData.firstName || '',
    lastName: initialData.lastName || '',
    email: initialData.email || '',
    phone: initialData.phone || '',
    jobTitle: initialData.jobTitle || '',
    department: initialData.department || '',
    isPrimary: initialData.isPrimary || false,
    linkedInUrl: initialData.linkedInUrl || '',
    notes: initialData.notes || '',
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof ContactFormData, string>>
  >({});

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof ContactFormData, string>> = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Please enter first name';
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Please enter last name';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Please enter email';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Invalid email';
    }

    if (
      formData.linkedInUrl &&
      !formData.linkedInUrl.includes('linkedin.com')
    ) {
      newErrors.linkedInUrl = 'Invalid LinkedIn URL';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
    }
  };

  const handleChange = (
    field: keyof ContactFormData,
    value: string | boolean
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className={cn('space-y-4', className)}>
      {/* Name Fields */}
      <div className='grid grid-cols-2 gap-4'>
        <div className='space-y-2'>
          <Label htmlFor='firstName' className='flex items-center gap-2'>
            <User className='h-4 w-4 text-muted-foreground' />
            First Name <span className='text-red-500'>*</span>
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
          <Label htmlFor='lastName' className='flex items-center gap-2'>
            <User className='h-4 w-4 text-muted-foreground' />
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
        {errors.email && <p className='text-xs text-red-500'>{errors.email}</p>}
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

      {/* Job Title & Department */}
      <div className='grid grid-cols-2 gap-4'>
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

        <div className='space-y-2'>
          <Label htmlFor='department' className='flex items-center gap-2'>
            <Building2 className='h-4 w-4 text-muted-foreground' />
            Phòng ban
          </Label>
          <Input
            id='department'
            value={formData.department}
            onChange={(e) => handleChange('department', e.target.value)}
            placeholder='VD: Kinh doanh'
          />
        </div>
      </div>

      {/* LinkedIn URL */}
      <div className='space-y-2'>
        <Label htmlFor='linkedInUrl' className='flex items-center gap-2'>
          <Link className='h-4 w-4 text-muted-foreground' />
          LinkedIn URL
        </Label>
        <Input
          id='linkedInUrl'
          type='url'
          value={formData.linkedInUrl}
          onChange={(e) => handleChange('linkedInUrl', e.target.value)}
          placeholder='https://linkedin.com/in/username'
          className={cn(errors.linkedInUrl && 'border-red-500')}
        />
        {errors.linkedInUrl && (
          <p className='text-xs text-red-500'>{errors.linkedInUrl}</p>
        )}
      </div>

      {/* Notes */}
      <div className='space-y-2'>
        <Label htmlFor='notes' className='flex items-center gap-2'>
          <FileText className='h-4 w-4 text-muted-foreground' />
          Notes
        </Label>
        <Textarea
          id='notes'
          value={formData.notes}
          onChange={(e) => handleChange('notes', e.target.value)}
          placeholder='Add notes about this contact...'
          rows={3}
        />
      </div>

      {/* Primary Contact Toggle */}
      <div className='flex items-center justify-between p-3 bg-muted rounded-lg'>
        <div>
          <Label htmlFor='isPrimary' className='cursor-pointer'>
            Primary Contact
          </Label>
          <p className='text-xs text-muted-foreground'>
            Set as primary contact for this customer
          </p>
        </div>
        <Switch
          id='isPrimary'
          checked={formData.isPrimary}
          onCheckedChange={(checked) => handleChange('isPrimary', checked)}
        />
      </div>

      {/* Actions */}
      <div className='flex justify-end gap-3 pt-4 border-t'>
        <Button type='button' variant='outline' onClick={onCancel}>
          Cancel
        </Button>
        <Button type='submit' disabled={isLoading}>
          {isLoading ? (
            <>
              <Loader2 className='h-4 w-4 mr-2 animate-spin' />
              Saving...
            </>
          ) : isEditing ? (
            'Update'
          ) : (
            'Add Contact'
          )}
        </Button>
      </div>
    </form>
  );
};

export default ContactForm;
