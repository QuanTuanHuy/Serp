/*
Author: QuanTuanHuy
Description: Part of Serp Project - Quick Add Customer Dialog for CRM
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
  Globe,
  MapPin,
  FileText,
  Loader2,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import type { CustomerType, CustomerStatus } from '../../types';

export interface QuickCustomerFormData {
  name: string;
  email: string;
  phone?: string;
  customerType: CustomerType;
  status: CustomerStatus;
  companyName?: string;
  website?: string;
  address?: string;
  notes?: string;
}

interface QuickAddCustomerDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: QuickCustomerFormData) => void;
  isLoading?: boolean;
}

export const QuickAddCustomerDialog: React.FC<QuickAddCustomerDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
}) => {
  const [formData, setFormData] = useState<QuickCustomerFormData>({
    name: '',
    email: '',
    phone: '',
    customerType: 'INDIVIDUAL',
    status: 'ACTIVE',
    companyName: '',
    website: '',
    address: '',
    notes: '',
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof QuickCustomerFormData, string>>
  >({});

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof QuickCustomerFormData, string>> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Please enter customer name';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Please enter email';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Invalid email';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
      // Reset form
      setFormData({
        name: '',
        email: '',
        phone: '',
        customerType: 'INDIVIDUAL',
        status: 'ACTIVE',
        companyName: '',
        website: '',
        address: '',
        notes: '',
      });
    }
  };

  const handleChange = (field: keyof QuickCustomerFormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-3xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Add New Customer</DialogTitle>
          <DialogDescription>
            Enter basic information to create a new customer
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4 py-4'>
          {/* Customer Type */}
          <div className='space-y-2'>
            <Label>Customer Type</Label>
            <Select
              value={formData.customerType}
              onValueChange={(value: CustomerType) =>
                handleChange('customerType', value)
              }
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='INDIVIDUAL'>
                  <div className='flex items-center gap-2'>
                    <User className='h-4 w-4' />
                    Individual
                  </div>
                </SelectItem>
                <SelectItem value='COMPANY'>
                  <div className='flex items-center gap-2'>
                    <Building2 className='h-4 w-4' />
                    Company
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Name */}
          <div className='space-y-2'>
            <Label htmlFor='name' className='flex items-center gap-2'>
              <User className='h-4 w-4 text-muted-foreground' />
              Customer Name <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='name'
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder='Enter customer name'
              className={cn(errors.name && 'border-red-500')}
            />
            {errors.name && (
              <p className='text-xs text-red-500'>{errors.name}</p>
            )}
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

          {/* Company Name (conditional) */}
          {formData.customerType === 'COMPANY' && (
            <div className='space-y-2'>
              <Label htmlFor='companyName' className='flex items-center gap-2'>
                <Building2 className='h-4 w-4 text-muted-foreground' />
                Company Name
              </Label>
              <Input
                id='companyName'
                value={formData.companyName}
                onChange={(e) => handleChange('companyName', e.target.value)}
                placeholder='Enter company name'
              />
            </div>
          )}

          {/* Website */}
          <div className='space-y-2'>
            <Label htmlFor='website' className='flex items-center gap-2'>
              <Globe className='h-4 w-4 text-muted-foreground' />
              Website
            </Label>
            <Input
              id='website'
              type='url'
              value={formData.website}
              onChange={(e) => handleChange('website', e.target.value)}
              placeholder='https://example.com'
            />
          </div>

          {/* Address */}
          <div className='space-y-2'>
            <Label htmlFor='address' className='flex items-center gap-2'>
              <MapPin className='h-4 w-4 text-muted-foreground' />
              Address
            </Label>
            <Input
              id='address'
              value={formData.address}
              onChange={(e) => handleChange('address', e.target.value)}
              placeholder='Enter address'
            />
          </div>

          {/* Status */}
          <div className='space-y-2'>
            <Label>Status</Label>
            <Select
              value={formData.status}
              onValueChange={(value: CustomerStatus) =>
                handleChange('status', value)
              }
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ACTIVE'>Active</SelectItem>
                <SelectItem value='POTENTIAL'>Potential</SelectItem>
                <SelectItem value='INACTIVE'>Inactive</SelectItem>
              </SelectContent>
            </Select>
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
              placeholder='Add notes...'
              rows={3}
            />
          </div>

          <DialogFooter className='pt-4'>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
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
                'Create Customer'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default QuickAddCustomerDialog;
