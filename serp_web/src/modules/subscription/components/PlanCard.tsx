/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  Badge,
  Button,
} from '@/shared/components/ui';
import { ModuleBadges } from '@/shared/components';
import { cn } from '@/shared/utils';
import { UISubscriptionPlan, BillingCycle, PlanModule } from '../types';
import { Check } from 'lucide-react';

interface PlanCardProps {
  plan: UISubscriptionPlan;
  billingCycle: BillingCycle;
  onSelect: (planId: string) => void;
  isSelected?: boolean;
  modules?: PlanModule[];
}

export const PlanCard: React.FC<PlanCardProps> = ({
  plan,
  billingCycle,
  onSelect,
  isSelected = false,
  modules,
}) => {
  const price =
    billingCycle === 'monthly' ? plan.monthlyPrice : plan.yearlyPrice;
  const displayPrice = billingCycle === 'yearly' ? price / 12 : price;

  const includedModules = modules?.filter((m) => m.isIncluded) || [];
  const moduleBadges = includedModules.map((m) => ({
    code: m.moduleCode || '',
    name: m.moduleName || m.moduleCode || 'Module',
    isIncluded: m.isIncluded,
  }));

  return (
    <Card
      className={cn(
        'relative transition-all duration-300 hover:shadow-lg',
        plan.popular && 'border-primary border-2',
        isSelected && 'ring-2 ring-primary ring-offset-2',
        'h-full flex flex-col'
      )}
    >
      {/* Badges */}
      {(plan.popular || plan.currentPlan) && (
        <div className='absolute -top-3 left-1/2 -translate-x-1/2 flex gap-2'>
          {plan.popular && (
            <Badge variant='default' className='px-3 py-1'>
              Most Popular
            </Badge>
          )}
          {plan.currentPlan && (
            <Badge variant='secondary' className='px-3 py-1'>
              Current Plan
            </Badge>
          )}
        </div>
      )}

      <CardHeader className='text-center pb-4'>
        <h3 className='text-2xl font-bold mb-2'>{plan.name}</h3>
        <p className='text-sm text-muted-foreground mb-4'>{plan.description}</p>

        {/* Price */}
        <div className='mb-4'>
          <div className='flex items-baseline justify-center gap-1'>
            <span className='text-4xl font-bold'>
              ${displayPrice.toFixed(0)}
            </span>
            <span className='text-muted-foreground text-sm'>/month</span>
          </div>
          {billingCycle === 'yearly' && (
            <p className='text-xs text-muted-foreground mt-1'>
              Billed ${price} annually
            </p>
          )}
        </div>

        {/* CTA Button */}
        <Button
          onClick={() => onSelect(plan.id)}
          variant={plan.popular ? 'default' : 'outline'}
          className='w-full'
          disabled={plan.currentPlan}
        >
          {plan.ctaText}
        </Button>
      </CardHeader>

      <CardContent className='flex-1'>
        {/* Modules Section (if available) */}
        {moduleBadges.length > 0 && (
          <div className='mb-4 pb-4 border-b'>
            <p className='text-sm font-semibold mb-3'>Included Modules:</p>
            <ModuleBadges
              modules={moduleBadges}
              maxDisplay={6}
              showIcons={true}
              size='sm'
              variant='outline'
            />
          </div>
        )}

        {/* Plan Limits */}
        <div className='mb-4 pb-4 border-b'>
          <div className='flex items-center justify-between text-sm mb-2'>
            <span className='text-muted-foreground'>Users</span>
            <span className='font-medium'>
              {plan.maxUsers === 'unlimited'
                ? 'Unlimited'
                : `Up to ${plan.maxUsers}`}
            </span>
          </div>
          <div className='flex items-center justify-between text-sm'>
            <span className='text-muted-foreground'>Storage</span>
            <span className='font-medium'>{plan.storage}</span>
          </div>
        </div>

        {/* Features List */}
        <div className='space-y-3'>
          <p className='text-sm font-semibold mb-2'>Key Features:</p>
          {plan.features.map((feature, index) => (
            <div key={index} className='flex items-start gap-2'>
              <Check className='h-4 w-4 text-primary mt-0.5 shrink-0' />
              <span className='text-sm text-muted-foreground'>{feature}</span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};
