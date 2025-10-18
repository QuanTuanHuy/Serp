/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React from 'react';
import { Switch, Label, Badge } from '@/shared/components/ui';
import { BillingCycle } from '../types';
import { BILLING_DISCOUNT_PERCENTAGE } from '../types/constants';

interface BillingToggleProps {
  billingCycle: BillingCycle;
  onToggle: (cycle: BillingCycle) => void;
}

export const BillingToggle: React.FC<BillingToggleProps> = ({
  billingCycle,
  onToggle,
}) => {
  const handleToggle = (checked: boolean) => {
    onToggle(checked ? 'yearly' : 'monthly');
  };

  return (
    <div className='flex items-center justify-center gap-4 mb-8'>
      <Label
        htmlFor='billing-toggle'
        className={`cursor-pointer text-base transition-colors ${
          billingCycle === 'monthly'
            ? 'text-foreground font-semibold'
            : 'text-muted-foreground'
        }`}
      >
        Monthly
      </Label>
      <Switch
        id='billing-toggle'
        checked={billingCycle === 'yearly'}
        onCheckedChange={handleToggle}
        className='data-[state=checked]:bg-primary'
      />
      <div className='flex items-center gap-2'>
        <Label
          htmlFor='billing-toggle'
          className={`cursor-pointer text-base transition-colors ${
            billingCycle === 'yearly'
              ? 'text-foreground font-semibold'
              : 'text-muted-foreground'
          }`}
        >
          Yearly
        </Label>
        <Badge variant='secondary' className='text-xs'>
          Save {BILLING_DISCOUNT_PERCENTAGE}%
        </Badge>
      </div>
    </div>
  );
};
