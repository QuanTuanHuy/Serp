/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings subscription management page
 */

'use client';

import React, { useState } from 'react';
import {
  CreditCard,
  Calendar,
  DollarSign,
  Download,
  FileText,
  CheckCircle2,
  AlertCircle,
  TrendingUp,
  Users,
  Zap,
  Crown,
  ArrowUpCircle,
  Clock,
  Ban,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { SettingsStatsCard, SettingsStatusBadge } from '@/modules/settings';
import { Separator } from '@/shared/components/ui/separator';
import { Progress } from '@/shared/components/ui/progress';
import { Switch } from '@/shared/components/ui/switch';
import { Label } from '@/shared/components/ui/label';

export default function SettingsSubscriptionPage() {
  const [autoRenew, setAutoRenew] = useState(true);

  // Mock subscription data
  const subscription = {
    id: '1',
    planName: 'Professional',
    planDescription: 'Perfect for growing teams',
    status: 'ACTIVE',
    billingCycle: 'MONTHLY',
    price: 99,
    currency: 'USD',
    startDate: '2024-01-15T00:00:00Z',
    endDate: '2025-01-15T00:00:00Z',
    nextBillingDate: '2024-11-15T00:00:00Z',
    autoRenew: true,
    usersLimit: 50,
    currentUsersCount: 45,
    modulesIncluded: ['CRM', 'PTM', 'SALES', 'ACCOUNTING', 'INVENTORY'],
  };

  // Mock invoices
  const invoices = [
    {
      id: '1',
      invoiceNumber: 'INV-2024-10',
      amount: 99,
      currency: 'USD',
      status: 'PAID',
      dueDate: '2024-10-15T00:00:00Z',
      paidDate: '2024-10-14T10:30:00Z',
      description: 'Professional Plan - October 2024',
      invoiceUrl: '#',
    },
    {
      id: '2',
      invoiceNumber: 'INV-2024-09',
      amount: 99,
      currency: 'USD',
      status: 'PAID',
      dueDate: '2024-09-15T00:00:00Z',
      paidDate: '2024-09-14T09:15:00Z',
      description: 'Professional Plan - September 2024',
      invoiceUrl: '#',
    },
    {
      id: '3',
      invoiceNumber: 'INV-2024-08',
      amount: 99,
      currency: 'USD',
      status: 'PAID',
      dueDate: '2024-08-15T00:00:00Z',
      paidDate: '2024-08-14T11:45:00Z',
      description: 'Professional Plan - August 2024',
      invoiceUrl: '#',
    },
  ];

  // Available plans
  const availablePlans = [
    {
      name: 'Starter',
      price: 29,
      users: 10,
      modules: 3,
      features: [
        'Up to 10 users',
        '3 modules included',
        'Email support',
        '10GB storage',
      ],
      isCurrent: false,
    },
    {
      name: 'Professional',
      price: 99,
      users: 50,
      modules: 8,
      features: [
        'Up to 50 users',
        '8 modules included',
        'Priority support',
        '100GB storage',
        'Advanced analytics',
        'API access',
      ],
      isCurrent: true,
      popular: true,
    },
    {
      name: 'Enterprise',
      price: 299,
      users: 'Unlimited',
      modules: 'All',
      features: [
        'Unlimited users',
        'All modules',
        'Dedicated support',
        'Unlimited storage',
        'Advanced security',
        'Custom integrations',
        'SLA guarantee',
      ],
      isCurrent: false,
    },
  ];

  const usagePercentage = Math.round(
    (subscription.currentUsersCount / subscription.usersLimit) * 100
  );

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PAID':
        return <CheckCircle2 className='h-4 w-4 text-green-600' />;
      case 'PENDING':
        return <Clock className='h-4 w-4 text-yellow-600' />;
      case 'OVERDUE':
        return <AlertCircle className='h-4 w-4 text-red-600' />;
      default:
        return null;
    }
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Subscription</h1>
          <p className='text-muted-foreground mt-2'>
            Manage your subscription plan and billing information
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Current Plan'
          value={subscription.planName}
          description='Active subscription'
          icon={<Crown className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Monthly Cost'
          value={`$${subscription.price}`}
          description={`Billed ${subscription.billingCycle.toLowerCase()}`}
          icon={<DollarSign className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Next Billing'
          value={formatDate(subscription.nextBillingDate).split(',')[0]}
          description={formatDate(subscription.nextBillingDate)}
          icon={<Calendar className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='User Usage'
          value={`${subscription.currentUsersCount}/${subscription.usersLimit}`}
          description={`${usagePercentage}% capacity`}
          icon={<Users className='h-4 w-4' />}
        />
      </div>

      {/* Current Subscription */}
      <Card className='border-purple-200 dark:border-purple-800'>
        <CardHeader>
          <div className='flex items-start justify-between'>
            <div className='flex items-center gap-3'>
              <div className='h-12 w-12 rounded-lg bg-purple-600 flex items-center justify-center'>
                <Crown className='h-6 w-6 text-white' />
              </div>
              <div>
                <CardTitle className='flex items-center gap-2'>
                  {subscription.planName} Plan
                  <SettingsStatusBadge status={subscription.status} />
                </CardTitle>
                <CardDescription>
                  {subscription.planDescription}
                </CardDescription>
              </div>
            </div>
            <Button className='bg-purple-600 hover:bg-purple-700'>
              <ArrowUpCircle className='h-4 w-4 mr-2' />
              Upgrade Plan
            </Button>
          </div>
        </CardHeader>
        <CardContent className='space-y-6'>
          {/* User Usage */}
          <div className='space-y-2'>
            <div className='flex items-center justify-between text-sm'>
              <Label className='flex items-center gap-2'>
                <Users className='h-4 w-4 text-muted-foreground' />
                User Capacity
              </Label>
              <span className='font-medium'>
                {subscription.currentUsersCount} / {subscription.usersLimit}{' '}
                users
              </span>
            </div>
            <Progress value={usagePercentage} className='h-2' />
            {usagePercentage >= 90 && (
              <div className='flex items-center gap-2 text-xs text-amber-600 dark:text-amber-400'>
                <AlertCircle className='h-3 w-3' />
                <span>
                  You're approaching your user limit. Consider upgrading your
                  plan.
                </span>
              </div>
            )}
          </div>

          <Separator />

          {/* Included Modules */}
          <div className='space-y-2'>
            <Label className='flex items-center gap-2'>
              <Zap className='h-4 w-4 text-muted-foreground' />
              Included Modules ({subscription.modulesIncluded.length})
            </Label>
            <div className='flex flex-wrap gap-2'>
              {subscription.modulesIncluded.map((module, index) => (
                <Badge key={index} variant='secondary' className='px-3 py-1'>
                  {module}
                </Badge>
              ))}
            </div>
          </div>

          <Separator />

          {/* Billing Information */}
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-1'>
              <Label className='text-sm text-muted-foreground'>
                Billing Cycle
              </Label>
              <p className='text-sm font-medium'>
                {subscription.billingCycle === 'MONTHLY' ? 'Monthly' : 'Yearly'}
              </p>
            </div>
            <div className='space-y-1'>
              <Label className='text-sm text-muted-foreground'>
                Subscription Start
              </Label>
              <p className='text-sm font-medium'>
                {formatDate(subscription.startDate)}
              </p>
            </div>
            <div className='space-y-1'>
              <Label className='text-sm text-muted-foreground'>
                Next Billing
              </Label>
              <p className='text-sm font-medium'>
                {formatDate(subscription.nextBillingDate)}
              </p>
            </div>
            <div className='space-y-1'>
              <Label className='text-sm text-muted-foreground'>
                Amount Due
              </Label>
              <p className='text-sm font-medium'>
                ${subscription.price} {subscription.currency}
              </p>
            </div>
          </div>

          <Separator />

          {/* Auto Renewal */}
          <div className='flex items-center justify-between'>
            <div className='space-y-0.5'>
              <Label className='text-sm font-medium'>Auto-renewal</Label>
              <p className='text-xs text-muted-foreground'>
                Automatically renew subscription at end of billing period
              </p>
            </div>
            <Switch
              checked={autoRenew}
              onCheckedChange={setAutoRenew}
              className='data-[state=checked]:bg-purple-600'
            />
          </div>

          <Separator />

          {/* Actions */}
          <div className='flex gap-2'>
            <Button variant='outline' className='flex-1'>
              <CreditCard className='h-4 w-4 mr-2' />
              Update Payment Method
            </Button>
            <Button variant='outline' className='flex-1 text-destructive'>
              <Ban className='h-4 w-4 mr-2' />
              Cancel Subscription
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Available Plans */}
      <Card>
        <CardHeader>
          <CardTitle>Available Plans</CardTitle>
          <CardDescription>
            Choose the plan that best fits your organization's needs
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className='grid gap-4 md:grid-cols-3'>
            {availablePlans.map((plan, index) => (
              <Card
                key={index}
                className={`relative ${
                  plan.isCurrent
                    ? 'border-purple-600 shadow-lg'
                    : 'hover:shadow-md transition-shadow'
                }`}
              >
                {plan.popular && (
                  <div className='absolute -top-3 left-1/2 -translate-x-1/2'>
                    <Badge className='bg-purple-600 hover:bg-purple-700'>
                      Most Popular
                    </Badge>
                  </div>
                )}
                <CardHeader>
                  <CardTitle className='text-xl'>{plan.name}</CardTitle>
                  <div className='mt-2'>
                    <span className='text-3xl font-bold'>${plan.price}</span>
                    <span className='text-muted-foreground'>/month</span>
                  </div>
                  <CardDescription className='pt-2'>
                    Up to {plan.users} users • {plan.modules} modules
                  </CardDescription>
                </CardHeader>
                <CardContent className='space-y-4'>
                  <ul className='space-y-2'>
                    {plan.features.map((feature, idx) => (
                      <li key={idx} className='flex items-start gap-2 text-sm'>
                        <CheckCircle2 className='h-4 w-4 text-green-600 flex-shrink-0 mt-0.5' />
                        <span>{feature}</span>
                      </li>
                    ))}
                  </ul>
                  <Button
                    className={`w-full ${
                      plan.isCurrent
                        ? 'bg-purple-600 hover:bg-purple-700'
                        : 'bg-primary hover:bg-primary/90'
                    }`}
                    disabled={plan.isCurrent}
                  >
                    {plan.isCurrent
                      ? 'Current Plan'
                      : 'Upgrade to ' + plan.name}
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Billing History */}
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <div>
              <CardTitle>Billing History</CardTitle>
              <CardDescription>View and download past invoices</CardDescription>
            </div>
            <Button variant='outline' size='sm'>
              <Download className='h-4 w-4 mr-2' />
              Download All
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className='space-y-3'>
            {invoices.map((invoice) => (
              <div
                key={invoice.id}
                className='flex items-center justify-between p-4 rounded-lg border hover:bg-accent transition-colors'
              >
                <div className='flex items-center gap-4'>
                  <div className='h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center'>
                    <FileText className='h-5 w-5 text-purple-600 dark:text-purple-400' />
                  </div>
                  <div>
                    <p className='font-medium text-sm'>
                      {invoice.invoiceNumber}
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      {invoice.description}
                    </p>
                  </div>
                </div>

                <div className='flex items-center gap-6'>
                  <div className='text-right'>
                    <p className='font-medium text-sm'>
                      ${invoice.amount} {invoice.currency}
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      {formatDate(invoice.paidDate || invoice.dueDate)}
                    </p>
                  </div>

                  <Badge
                    variant={
                      invoice.status === 'PAID' ? 'default' : 'destructive'
                    }
                    className='flex items-center gap-1 w-20 justify-center'
                  >
                    {getStatusIcon(invoice.status)}
                    {invoice.status}
                  </Badge>

                  <Button variant='ghost' size='sm'>
                    <Download className='h-4 w-4' />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
