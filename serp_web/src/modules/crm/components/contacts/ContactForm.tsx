/*
Author: QuanTuanHuy
Description: Part of Serp Project - Contact Form Component for CRM
*/

'use client';

import { useState } from 'react';
import {
  Button,
  Input,
  Label,
  Textarea,
  Switch,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import {
  User,
  Mail,
  Phone,
  Briefcase,
  Building2,
  MapPin,
  Link,
  FileText,
  Loader2,
} from 'lucide-react';
import { cn } from '@/shared/utils';

export interface ContactFormData {
  name: string;
  email: string;
  phone?: string;
  jobPosition?: string;
  isPrimary?: boolean;
  street?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  contactType?: 'PRIMARY' | 'SECONDARY' | 'BILLING' | 'TECHNICAL';
  activeStatus?: 'ACTIVE' | 'INACTIVE';
  linkedInUrl?: string;
  twitterHandle?: string;
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
    name: initialData.name || '',
    email: initialData.email || '',
    phone: initialData.phone || '',
    jobPosition: initialData.jobPosition || '',
    isPrimary: initialData.isPrimary || false,
    street: initialData.street || '',
    city: initialData.city || '',
    state: initialData.state || '',
    zipCode: initialData.zipCode || '',
    country: initialData.country || '',
    contactType: initialData.contactType || 'SECONDARY',
    activeStatus: initialData.activeStatus || 'ACTIVE',
    linkedInUrl: initialData.linkedInUrl || '',
    twitterHandle: initialData.twitterHandle || '',
    notes: initialData.notes || '',
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof ContactFormData, string>>
  >({});

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof ContactFormData, string>> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Please enter contact name';
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
    <form
      onSubmit={handleSubmit}
      className={cn('space-y-4 overflow-y-auto max-h-[70vh] pr-1', className)}
    >
      {/* Name */}
      <div className='space-y-2'>
        <Label htmlFor='name' className='flex items-center gap-2'>
          <User className='h-4 w-4 text-muted-foreground' />
          Contact Name <span className='text-red-500'>*</span>
        </Label>
        <Input
          id='name'
          value={formData.name}
          onChange={(e) => handleChange('name', e.target.value)}
          placeholder='Enter full contact name'
          className={cn(errors.name && 'border-red-500')}
        />
        {errors.name && <p className='text-xs text-red-500'>{errors.name}</p>}
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
          Phone Number
        </Label>
        <Input
          id='phone'
          type='tel'
          value={formData.phone}
          onChange={(e) => handleChange('phone', e.target.value)}
          placeholder='+84 xxx xxx xxx'
        />
      </div>

      {/* Job Position & Type */}
      <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
        <div className='space-y-2'>
          <Label htmlFor='jobPosition' className='flex items-center gap-2'>
            <Briefcase className='h-4 w-4 text-muted-foreground' />
            Job Position
          </Label>
          <Input
            id='jobPosition'
            value={formData.jobPosition}
            onChange={(e) => handleChange('jobPosition', e.target.value)}
            placeholder='e.g. Director'
          />
        </div>

        <div className='space-y-2'>
          <Label htmlFor='contactType' className='flex items-center gap-2'>
            <Building2 className='h-4 w-4 text-muted-foreground' />
            Contact Type
          </Label>
          <Select
            value={formData.contactType}
            onValueChange={(value) => handleChange('contactType', value)}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='PRIMARY'>Primary</SelectItem>
              <SelectItem value='SECONDARY'>Secondary</SelectItem>
              <SelectItem value='BILLING'>Billing</SelectItem>
              <SelectItem value='TECHNICAL'>Technical</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Address */}
      <div className='space-y-4'>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <div className='space-y-2 md:col-span-2'>
            <Label htmlFor='street' className='flex items-center gap-2'>
              <MapPin className='h-4 w-4 text-muted-foreground' />
              Street
            </Label>
            <Input
              id='street'
              value={formData.street}
              onChange={(e) => handleChange('street', e.target.value)}
              placeholder='Street address'
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='city'>City</Label>
            <Input
              id='city'
              value={formData.city}
              onChange={(e) => handleChange('city', e.target.value)}
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='state'>State</Label>
            <Input
              id='state'
              value={formData.state}
              onChange={(e) => handleChange('state', e.target.value)}
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='zipCode'>Zip Code</Label>
            <Input
              id='zipCode'
              value={formData.zipCode}
              onChange={(e) => handleChange('zipCode', e.target.value)}
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='country'>Country</Label>
            <Input
              id='country'
              value={formData.country}
              onChange={(e) => handleChange('country', e.target.value)}
            />
          </div>
        </div>
      </div>

      {/* Status */}
      <div className='space-y-2'>
        <Label>Active Status</Label>
        <Select
          value={formData.activeStatus}
          onValueChange={(value) => handleChange('activeStatus', value)}
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='ACTIVE'>Active</SelectItem>
            <SelectItem value='INACTIVE'>Inactive</SelectItem>
          </SelectContent>
        </Select>
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

      <div className='space-y-2'>
        <Label htmlFor='twitterHandle' className='flex items-center gap-2'>
          <Link className='h-4 w-4 text-muted-foreground' />
          Twitter Handle
        </Label>
        <Input
          id='twitterHandle'
          value={formData.twitterHandle}
          onChange={(e) => handleChange('twitterHandle', e.target.value)}
          placeholder='@username'
        />
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
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between p-3 bg-muted rounded-lg'>
        <div>
          <Label htmlFor='isPrimary' className='cursor-pointer'>
            Primary Contact
          </Label>
          <p className='text-xs text-muted-foreground'>
            Set as primary contact for this account
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
