/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, Input, Button, Badge } from '@/shared/components/ui';
import { OrderSummary as OrderSummaryType, BillingCycle } from '../types';
import { Loader2 } from 'lucide-react';

interface OrderSummaryProps {
  summary: OrderSummaryType;
  onApplyPromoCode: (code: string) => Promise<boolean>;
}

export const OrderSummary: React.FC<OrderSummaryProps> = ({
  summary,
  onApplyPromoCode,
}) => {
  const [promoCode, setPromoCode] = useState('');
  const [isApplying, setIsApplying] = useState(false);
  const [promoError, setPromoError] = useState('');

  const handleApplyPromo = async () => {
    if (!promoCode.trim()) return;

    setIsApplying(true);
    setPromoError('');

    const success = await onApplyPromoCode(promoCode);

    if (!success) {
      setPromoError('Invalid promo code');
    } else {
      setPromoCode('');
    }

    setIsApplying(false);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <Card className='sticky top-4'>
      <CardHeader>
        <CardTitle>Order Summary</CardTitle>
      </CardHeader>
      <CardContent className='space-y-4'>
        {/* Plan Details */}
        <div className='pb-4 border-b space-y-2'>
          <div className='flex justify-between items-start'>
            <div>
              <p className='font-medium'>{summary.planName}</p>
              <p className='text-sm text-muted-foreground capitalize'>
                {summary.billingCycle} billing
              </p>
            </div>
            <p className='font-semibold'>${summary.basePrice.toFixed(2)}</p>
          </div>
        </div>

        {/* Add-ons */}
        {summary.addOns.length > 0 && (
          <div className='pb-4 border-b space-y-2'>
            <p className='text-sm font-medium'>Add-ons</p>
            {summary.addOns.map((addOn) => (
              <div key={addOn.id} className='flex justify-between items-start text-sm'>
                <div className='flex-1'>
                  <p className='text-muted-foreground'>{addOn.name}</p>
                  {addOn.quantity && addOn.quantity > 1 && (
                    <p className='text-xs text-muted-foreground'>
                      Qty: {addOn.quantity}
                    </p>
                  )}
                </div>
                <p className='font-medium'>
                  ${(addOn.price * (addOn.quantity || 1)).toFixed(2)}
                </p>
              </div>
            ))}
          </div>
        )}

        {/* Promo Code */}
        <div className='space-y-2'>
          <div className='flex gap-2'>
            <Input
              type='text'
              placeholder='Promo code'
              value={promoCode}
              onChange={(e) => {
                setPromoCode(e.target.value.toUpperCase());
                setPromoError('');
              }}
              disabled={isApplying || !!summary.promoCode}
            />
            <Button
              variant='outline'
              onClick={handleApplyPromo}
              disabled={isApplying || !promoCode.trim() || !!summary.promoCode}
            >
              {isApplying ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : (
                'Apply'
              )}
            </Button>
          </div>
          {promoError && (
            <p className='text-sm text-destructive'>{promoError}</p>
          )}
          {summary.promoCode && (
            <div className='flex items-center gap-2'>
              <Badge variant='secondary' className='text-xs'>
                {summary.promoCode.code} applied
              </Badge>
              <span className='text-sm text-primary'>
                -{summary.promoCode.type === 'percentage' ? `${summary.promoCode.discount}%` : `$${summary.promoCode.discount}`}
              </span>
            </div>
          )}
        </div>

        {/* Price Breakdown */}
        <div className='space-y-2 pb-4 border-b'>
          <div className='flex justify-between text-sm'>
            <span className='text-muted-foreground'>Subtotal</span>
            <span>${summary.subtotal.toFixed(2)}</span>
          </div>
          {summary.promoCode && (
            <div className='flex justify-between text-sm text-primary'>
              <span>Discount</span>
              <span>
                -$
                {(
                  summary.promoCode.type === 'percentage'
                    ? (summary.subtotal * summary.promoCode.discount) / 100
                    : summary.promoCode.discount
                ).toFixed(2)}
              </span>
            </div>
          )}
          <div className='flex justify-between text-sm'>
            <span className='text-muted-foreground'>Tax</span>
            <span>${summary.tax.toFixed(2)}</span>
          </div>
        </div>

        {/* Total */}
        <div className='flex justify-between items-center pt-2'>
          <span className='text-lg font-semibold'>Total</span>
          <span className='text-2xl font-bold text-primary'>
            ${summary.total.toFixed(2)}
          </span>
        </div>

        {/* Next Billing Date */}
        <div className='pt-4 border-t'>
          <p className='text-xs text-muted-foreground text-center'>
            Next billing date: {formatDate(summary.nextBillingDate)}
          </p>
        </div>

        {/* Guarantee Badge */}
        <div className='bg-muted/50 p-3 rounded-md text-center'>
          <p className='text-sm font-medium text-primary'>
            30-Day Money-Back Guarantee
          </p>
          <p className='text-xs text-muted-foreground mt-1'>
            Cancel anytime, no questions asked
          </p>
        </div>
      </CardContent>
    </Card>
  );
};
