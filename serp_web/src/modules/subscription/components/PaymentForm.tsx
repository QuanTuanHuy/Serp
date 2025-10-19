/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, Input, Label, Button } from '@/shared/components/ui';
import { PaymentInfo } from '../types';
import { CreditCard, Lock } from 'lucide-react';

interface PaymentFormProps {
  onSubmit: (paymentInfo: PaymentInfo) => void;
  onCancel?: () => void;
}

export const PaymentForm: React.FC<PaymentFormProps> = ({ onSubmit, onCancel }) => {
  const [formData, setFormData] = useState<PaymentInfo>({
    cardNumber: '',
    expiryDate: '',
    cvv: '',
    billingAddress: {
      street: '',
      city: '',
      state: '',
      zipCode: '',
      country: '',
    },
  });

  const [errors, setErrors] = useState<Partial<Record<keyof PaymentInfo, string>>>({});

  const validateCardNumber = (value: string) => {
    const cleaned = value.replace(/\s/g, '');
    // Accept 15-16 digits (AMEX has 15, most others have 16)
    // In production, use a proper card validation library
    return /^\d{15,16}$/.test(cleaned);
  };

  const validateExpiryDate = (value: string) => {
    return /^(0[1-9]|1[0-2])\/\d{2}$/.test(value);
  };

  const validateCVV = (value: string) => {
    return /^\d{3,4}$/.test(value);
  };

  const formatCardNumber = (value: string) => {
    const cleaned = value.replace(/\s/g, '');
    // Basic formatting - in production, detect card type and format accordingly
    // AMEX: 4-6-5, Others: 4-4-4-4
    if (cleaned.length <= 4) return cleaned;
    if (cleaned.length <= 10) {
      return `${cleaned.slice(0, 4)} ${cleaned.slice(4)}`;
    }
    if (cleaned.length <= 14) {
      return `${cleaned.slice(0, 4)} ${cleaned.slice(4, 10)} ${cleaned.slice(10)}`;
    }
    return `${cleaned.slice(0, 4)} ${cleaned.slice(4, 8)} ${cleaned.slice(8, 12)} ${cleaned.slice(12, 16)}`;
  };

  const formatExpiryDate = (value: string) => {
    const cleaned = value.replace(/\D/g, '');
    if (cleaned.length >= 2) {
      return `${cleaned.slice(0, 2)}/${cleaned.slice(2, 4)}`;
    }
    return cleaned;
  };

  const handleInputChange = (field: keyof PaymentInfo, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const handleAddressChange = (field: keyof PaymentInfo['billingAddress'], value: string) => {
    setFormData((prev) => ({
      ...prev,
      billingAddress: {
        ...prev.billingAddress,
        [field]: value,
      },
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const newErrors: Partial<Record<keyof PaymentInfo, string>> = {};

    if (!validateCardNumber(formData.cardNumber)) {
      newErrors.cardNumber = 'Please enter a valid card number (15-16 digits)';
    }
    if (!validateExpiryDate(formData.expiryDate)) {
      newErrors.expiryDate = 'Please enter a valid expiry date (MM/YY)';
    }
    if (!validateCVV(formData.cvv)) {
      newErrors.cvv = 'Please enter a valid CVV';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    onSubmit(formData);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          <CreditCard className='h-5 w-5' />
          Payment Information
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className='space-y-6'>
          {/* Payment Method Icons */}
          <div className='flex items-center gap-3 pb-4 border-b'>
            <span className='text-sm text-muted-foreground'>We accept:</span>
            <div className='flex gap-2'>
              <div className='px-3 py-1 border rounded text-xs font-medium'>VISA</div>
              <div className='px-3 py-1 border rounded text-xs font-medium'>Mastercard</div>
              <div className='px-3 py-1 border rounded text-xs font-medium'>AMEX</div>
            </div>
          </div>

          {/* Card Number */}
          <div className='space-y-2'>
            <Label htmlFor='cardNumber'>Card Number</Label>
            <Input
              id='cardNumber'
              type='text'
              placeholder='1234 5678 9012 3456'
              value={formatCardNumber(formData.cardNumber)}
              onChange={(e) => {
                const cleaned = e.target.value.replace(/\s/g, '');
                if (cleaned.length <= 16) {
                  handleInputChange('cardNumber', cleaned);
                }
              }}
              maxLength={19}
              aria-invalid={!!errors.cardNumber}
            />
            {errors.cardNumber && (
              <p className='text-sm text-destructive'>{errors.cardNumber}</p>
            )}
          </div>

          {/* Expiry Date and CVV */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='expiryDate'>Expiry Date</Label>
              <Input
                id='expiryDate'
                type='text'
                placeholder='MM/YY'
                value={formData.expiryDate}
                onChange={(e) => {
                  const formatted = formatExpiryDate(e.target.value);
                  if (formatted.length <= 5) {
                    handleInputChange('expiryDate', formatted);
                  }
                }}
                maxLength={5}
                aria-invalid={!!errors.expiryDate}
              />
              {errors.expiryDate && (
                <p className='text-sm text-destructive'>{errors.expiryDate}</p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='cvv'>CVV</Label>
              <Input
                id='cvv'
                type='text'
                placeholder='123'
                value={formData.cvv}
                onChange={(e) => {
                  const cleaned = e.target.value.replace(/\D/g, '');
                  if (cleaned.length <= 4) {
                    handleInputChange('cvv', cleaned);
                  }
                }}
                maxLength={4}
                aria-invalid={!!errors.cvv}
              />
              {errors.cvv && (
                <p className='text-sm text-destructive'>{errors.cvv}</p>
              )}
            </div>
          </div>

          {/* Billing Address */}
          <div className='space-y-4 pt-4 border-t'>
            <h3 className='font-medium'>Billing Address</h3>

            <div className='space-y-2'>
              <Label htmlFor='street'>Street Address</Label>
              <Input
                id='street'
                type='text'
                placeholder='123 Main St'
                value={formData.billingAddress.street}
                onChange={(e) => handleAddressChange('street', e.target.value)}
                required
              />
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='city'>City</Label>
                <Input
                  id='city'
                  type='text'
                  placeholder='New York'
                  value={formData.billingAddress.city}
                  onChange={(e) => handleAddressChange('city', e.target.value)}
                  required
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='state'>State/Province</Label>
                <Input
                  id='state'
                  type='text'
                  placeholder='NY'
                  value={formData.billingAddress.state}
                  onChange={(e) => handleAddressChange('state', e.target.value)}
                  required
                />
              </div>
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='zipCode'>ZIP/Postal Code</Label>
                <Input
                  id='zipCode'
                  type='text'
                  placeholder='10001'
                  value={formData.billingAddress.zipCode}
                  onChange={(e) => handleAddressChange('zipCode', e.target.value)}
                  required
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='country'>Country</Label>
                <Input
                  id='country'
                  type='text'
                  placeholder='United States'
                  value={formData.billingAddress.country}
                  onChange={(e) => handleAddressChange('country', e.target.value)}
                  required
                />
              </div>
            </div>
          </div>

          {/* Security Message */}
          <div className='flex items-center gap-2 p-3 bg-muted/50 rounded-md'>
            <Lock className='h-4 w-4 text-primary' />
            <p className='text-xs text-muted-foreground'>
              Your payment information is secure and encrypted with 256-bit SSL
            </p>
          </div>

          {/* Submit Buttons */}
          <div className='flex gap-3 pt-4'>
            {onCancel && (
              <Button type='button' variant='outline' onClick={onCancel} className='flex-1'>
                Cancel
              </Button>
            )}
            <Button type='submit' className='flex-1'>
              Confirm Payment
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};
