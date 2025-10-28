/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription Details Dialog
 */

'use client';

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Separator } from '@/shared/components/ui/separator';
import {
  Building2,
  CreditCard,
  Calendar,
  Package,
  Users,
  Clock,
  DollarSign,
  CheckCircle,
  XCircle,
} from 'lucide-react';
import type { OrganizationSubscription, SubscriptionPlan } from '../../types';
import { AdminStatusBadge } from '../shared/AdminStatusBadge';
import { cn } from '@/shared/utils';

interface SubscriptionDetailsDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  subscription: OrganizationSubscription | null;
  plan?: SubscriptionPlan;
}

export const SubscriptionDetailsDialog: React.FC<
  SubscriptionDetailsDialogProps
> = ({ open, onOpenChange, subscription, plan }) => {
  if (!subscription) return null;

  const formatDate = (ms?: number) => {
    if (!ms) return 'N/A';
    return new Date(ms).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatPrice = (price?: number) => {
    if (!price) return '$0.00';
    return `$${price.toFixed(2)}`;
  };

  const getDaysRemaining = () => {
    if (!subscription.endDate) return null;
    const now = Date.now();
    const daysLeft = Math.ceil(
      (subscription.endDate - now) / (1000 * 60 * 60 * 24)
    );
    return daysLeft;
  };

  const daysRemaining = getDaysRemaining();
  const isExpiringSoon =
    daysRemaining !== null && daysRemaining <= 30 && daysRemaining > 0;
  const isExpired = daysRemaining !== null && daysRemaining < 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-4xl w-full max-h-[90vh]'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <Building2 className='h-5 w-5' />
            Subscription Details
          </DialogTitle>
        </DialogHeader>

        <ScrollArea className='h-[calc(90vh-120px)]'>
          <div className='space-y-6 pr-6'>
            {/* Status Overview */}
            <Card>
              <CardHeader className='pb-3'>
                <div className='flex items-center justify-between'>
                  <CardTitle className='text-base'>Status Overview</CardTitle>
                  <AdminStatusBadge status={subscription.status} />
                </div>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-1'>
                    <p className='text-xs text-muted-foreground'>
                      Organization
                    </p>
                    <p className='font-medium'>
                      {subscription.organizationName || 'N/A'}
                    </p>
                  </div>
                  <div className='space-y-1'>
                    <p className='text-xs text-muted-foreground'>
                      Subscription ID
                    </p>
                    <p className='font-mono text-sm'>#{subscription.id}</p>
                  </div>
                </div>

                {daysRemaining !== null && (
                  <div className='flex items-center gap-2 p-3 rounded-lg bg-muted/50'>
                    <Clock className='h-4 w-4' />
                    <span className='text-sm'>
                      {isExpired ? (
                        <span className='text-destructive font-medium'>
                          Expired {Math.abs(daysRemaining)} days ago
                        </span>
                      ) : isExpiringSoon ? (
                        <span className='text-amber-600 font-medium'>
                          Expires in {daysRemaining} days
                        </span>
                      ) : (
                        <span>{daysRemaining} days remaining</span>
                      )}
                    </span>
                  </div>
                )}

                {subscription.isAutoRenew && (
                  <div className='flex items-center gap-2 text-sm text-green-600'>
                    <CheckCircle className='h-4 w-4' />
                    <span>Auto-renewal enabled</span>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Plan Information */}
            <Card>
              <CardHeader className='pb-3'>
                <CardTitle className='text-base flex items-center gap-2'>
                  <Package className='h-4 w-4' />
                  Plan Information
                </CardTitle>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='space-y-1'>
                  <p className='text-xs text-muted-foreground'>Plan Name</p>
                  <p className='text-lg font-semibold'>
                    {subscription.planName || plan?.planName || 'N/A'}
                  </p>
                  {plan?.description && (
                    <p className='text-sm text-muted-foreground'>
                      {plan.description}
                    </p>
                  )}
                </div>

                <Separator />

                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-1'>
                    <p className='text-xs text-muted-foreground flex items-center gap-1'>
                      <DollarSign className='h-3 w-3' />
                      Pricing
                    </p>
                    {plan && (
                      <div className='space-y-1'>
                        <p className='text-sm'>
                          Monthly:{' '}
                          <span className='font-semibold'>
                            {formatPrice(plan.monthlyPrice)}
                          </span>
                        </p>
                        <p className='text-sm'>
                          Yearly:{' '}
                          <span className='font-semibold'>
                            {formatPrice(plan.yearlyPrice)}
                          </span>
                        </p>
                      </div>
                    )}
                  </div>

                  <div className='space-y-1'>
                    <p className='text-xs text-muted-foreground flex items-center gap-1'>
                      <Users className='h-3 w-3' />
                      User Limits
                    </p>
                    <p className='text-sm'>
                      Max Users:{' '}
                      <span className='font-semibold'>
                        {plan?.maxUsers || 'Unlimited'}
                      </span>
                    </p>
                    {plan?.trialDays && plan.trialDays > 0 && (
                      <p className='text-sm'>
                        Trial:{' '}
                        <span className='font-semibold'>
                          {plan.trialDays} days
                        </span>
                      </p>
                    )}
                  </div>
                </div>

                {plan?.modules && plan.modules.length > 0 && (
                  <>
                    <Separator />
                    <div className='space-y-2'>
                      <p className='text-xs text-muted-foreground'>
                        Included Modules ({plan.modules.length})
                      </p>
                      <div className='flex flex-wrap gap-2'>
                        {plan.modules.map((module) => (
                          <Badge
                            key={module.id}
                            variant='outline'
                            className='text-xs'
                          >
                            {module.moduleName || `Module #${module.moduleId}`}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  </>
                )}
              </CardContent>
            </Card>

            {/* Billing Information */}
            <Card>
              <CardHeader className='pb-3'>
                <CardTitle className='text-base flex items-center gap-2'>
                  <CreditCard className='h-4 w-4' />
                  Billing Information
                </CardTitle>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-1'>
                    <p className='text-xs text-muted-foreground'>
                      Billing Cycle
                    </p>
                    <Badge variant='secondary'>
                      {subscription.billingCycle || 'N/A'}
                    </Badge>
                  </div>
                  <div className='space-y-1'>
                    <p className='text-xs text-muted-foreground'>
                      Auto Renewal
                    </p>
                    <div className='flex items-center gap-2'>
                      {subscription.isAutoRenew ? (
                        <>
                          <CheckCircle className='h-4 w-4 text-green-600' />
                          <span className='text-sm text-green-600'>
                            Enabled
                          </span>
                        </>
                      ) : (
                        <>
                          <XCircle className='h-4 w-4 text-muted-foreground' />
                          <span className='text-sm text-muted-foreground'>
                            Disabled
                          </span>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Timeline */}
            <Card>
              <CardHeader className='pb-3'>
                <CardTitle className='text-base flex items-center gap-2'>
                  <Calendar className='h-4 w-4' />
                  Timeline
                </CardTitle>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='space-y-3'>
                  <div className='flex items-start gap-3'>
                    <div className='flex h-8 w-8 items-center justify-center rounded-full bg-green-100'>
                      <CheckCircle className='h-4 w-4 text-green-600' />
                    </div>
                    <div className='flex-1'>
                      <p className='text-sm font-medium'>Start Date</p>
                      <p className='text-xs text-muted-foreground'>
                        {formatDate(subscription.startDate)}
                      </p>
                    </div>
                  </div>

                  {subscription.endDate && (
                    <div className='flex items-start gap-3'>
                      <div
                        className={cn(
                          'flex h-8 w-8 items-center justify-center rounded-full',
                          isExpired
                            ? 'bg-red-100'
                            : isExpiringSoon
                              ? 'bg-amber-100'
                              : 'bg-blue-100'
                        )}
                      >
                        <Calendar
                          className={cn(
                            'h-4 w-4',
                            isExpired
                              ? 'text-red-600'
                              : isExpiringSoon
                                ? 'text-amber-600'
                                : 'text-blue-600'
                          )}
                        />
                      </div>
                      <div className='flex-1'>
                        <p className='text-sm font-medium'>End Date</p>
                        <p className='text-xs text-muted-foreground'>
                          {formatDate(subscription.endDate)}
                        </p>
                      </div>
                    </div>
                  )}

                  {subscription.trialEndsAt && (
                    <div className='flex items-start gap-3'>
                      <div className='flex h-8 w-8 items-center justify-center rounded-full bg-purple-100'>
                        <Clock className='h-4 w-4 text-purple-600' />
                      </div>
                      <div className='flex-1'>
                        <p className='text-sm font-medium'>Trial End Date</p>
                        <p className='text-xs text-muted-foreground'>
                          {formatDate(subscription.trialEndsAt)}
                        </p>
                      </div>
                    </div>
                  )}
                </div>

                <Separator />

                <div className='grid grid-cols-2 gap-4 text-xs'>
                  <div>
                    <p className='text-muted-foreground'>Created</p>
                    <p>{formatDate(subscription.createdAt)}</p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Last Updated</p>
                    <p>{formatDate(subscription.updatedAt)}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
};
