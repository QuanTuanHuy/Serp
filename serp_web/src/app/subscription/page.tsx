/*
Author: QuanTuanHuy
Description: Part of Serp Project - Subscription Plans Page
*/

'use client';

import React, { useState, useMemo } from 'react';
import { Header } from '@/shared/components';
import {
  PlanCard,
  BillingToggle,
  ComparisonTable,
  FAQSection,
  TrustIndicators,
} from '@/modules/subscription';
import {
  BillingCycle,
  OrderSummary as OrderSummaryType,
} from '@/modules/subscription/types';
import {
  SUBSCRIPTION_PLANS,
  FAQS,
} from '@/modules/subscription/types/constants';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Button,
} from '@/shared/components/ui';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useGetSubscriptionPlansQuery } from '@/modules/admin/services/plans/plansApi';
import { useSubscribeMutation } from '@/modules/subscription/services/subscriptionApi';
import type { SubscriptionPlan as BEPlan } from '@/modules/admin/types/plans.types';
import { getErrorMessage } from '@/lib';

export default function SubscriptionPage() {
  const router = useRouter();
  const [billingCycle, setBillingCycle] = useState<BillingCycle>('monthly');
  const [selectedPlanId, setSelectedPlanId] = useState<string | null>(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);

  const { data: bePlans, isLoading, isError } = useGetSubscriptionPlansQuery();
  const [subscribe, { isLoading: isSubscribing }] = useSubscribeMutation();

  const mappedPlans = useMemo(() => {
    if (!bePlans) return SUBSCRIPTION_PLANS;

    const templateIndexByKey = new Map<
      string,
      (typeof SUBSCRIPTION_PLANS)[number]
    >();
    for (const tpl of SUBSCRIPTION_PLANS) {
      templateIndexByKey.set(tpl.id.toLowerCase(), tpl);
      templateIndexByKey.set(tpl.name.toLowerCase(), tpl);
    }

    const normalize = (s?: string) => (s ? s.trim().toLowerCase() : '');

    const toUiPlan = (p: BEPlan) => {
      const key1 = normalize(p.planCode);
      const key2 = normalize(p.planName);
      const tpl = templateIndexByKey.get(key1) || templateIndexByKey.get(key2);

      const base = tpl || SUBSCRIPTION_PLANS[0];

      return {
        ...base,
        id: String(p.id),
        name: p.planName || base.name,
        description: p.description || base.description,
        monthlyPrice: Number(p.monthlyPrice ?? base.monthlyPrice ?? 0),
        yearlyPrice: Number(p.yearlyPrice ?? base.yearlyPrice ?? 0),
        maxUsers: p.maxUsers ?? base.maxUsers,
      };
    };

    const sorted = [...bePlans]
      .filter((p) => p.isActive)
      .sort((a, b) => {
        const ao = a.displayOrder ?? 0;
        const bo = b.displayOrder ?? 0;
        return ao - bo;
      });

    return sorted.map(toUiPlan);
  }, [bePlans]);

  const selectedPlan = useMemo(
    () => mappedPlans.find((plan) => plan.id === selectedPlanId),
    [selectedPlanId, mappedPlans]
  );

  const orderSummary: OrderSummaryType | null = useMemo(() => {
    if (!selectedPlan) return null;
    const basePrice =
      billingCycle === 'monthly'
        ? selectedPlan.monthlyPrice
        : selectedPlan.yearlyPrice;
    const nextBillingDate = new Date();
    nextBillingDate.setMonth(
      nextBillingDate.getMonth() + (billingCycle === 'monthly' ? 1 : 12)
    );
    return {
      planId: selectedPlan.id,
      planName: selectedPlan.name,
      billingCycle,
      basePrice,
      addOns: [],
      subtotal: basePrice,
      tax: 0,
      total: basePrice,
      nextBillingDate: nextBillingDate.toISOString(),
    };
  }, [selectedPlan, billingCycle]);

  const handlePlanSelect = (planId: string) => {
    const plan = mappedPlans.find((p) => p.id === planId);
    if (!plan) return;
    setSelectedPlanId(planId);
    setShowConfirmDialog(true);
  };

  const handleConfirmSubscription = async () => {
    if (!selectedPlanId) return;
    try {
      const planIdNum = Number(selectedPlanId);
      await subscribe({
        planId: planIdNum,
        billingCycle: billingCycle,
        isAutoRenew: false,
      }).unwrap();
      setShowConfirmDialog(false);
      toast.success('Subscription is processing!', {
        description: `Wait Admin to confirm ${selectedPlan?.name}!`,
      });
      setTimeout(() => {
        router.push('/');
      }, 1200);
    } catch (e: any) {
      const message =
        getErrorMessage(e) || 'Failed to subscribe. Please try again.';
      toast.error('Subscription Failed', { description: message });
    }
  };

  const handleCancel = () => {
    setSelectedPlanId(null);
    setShowConfirmDialog(false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='container mx-auto px-4 py-12'>
        <button
          onClick={() => router.push('/')}
          className='flex items-center gap-2 text-muted-foreground hover:text-foreground mb-8 transition-colors'
        >
          <ArrowLeft className='h-4 w-4' />
          Back to Home
        </button>

        <div className='text-center mb-12'>
          <h1 className='text-4xl md:text-5xl font-bold mb-4'>
            Choose Your Perfect Plan
          </h1>
          <p className='text-xl text-muted-foreground max-w-2xl mx-auto'>
            Start with a free trial. Upgrade or downgrade anytime. Cancel with
            no penalties.
          </p>
        </div>

        {isError && (
          <div className='mb-6 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive'>
            Could not load plans from server. Showing default plans.
          </div>
        )}

        <BillingToggle billingCycle={billingCycle} onToggle={setBillingCycle} />

        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12'>
          {(isLoading ? SUBSCRIPTION_PLANS : mappedPlans).map((plan) => (
            <PlanCard
              key={plan.id}
              plan={plan}
              billingCycle={billingCycle}
              onSelect={handlePlanSelect}
              isSelected={selectedPlanId === plan.id}
            />
          ))}
        </div>

        <div className='mb-12'>
          <ComparisonTable
            plans={isLoading ? SUBSCRIPTION_PLANS : mappedPlans}
          />
        </div>

        <div className='mb-12'>
          <TrustIndicators />
        </div>
        <div className='mb-12'>
          <FAQSection faqs={FAQS} />
        </div>
      </main>

      <Dialog open={showConfirmDialog} onOpenChange={setShowConfirmDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Your Subscription</DialogTitle>
            <DialogDescription>
              You're about to subscribe to {selectedPlan?.name} plan.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-3 py-4'>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Plan:</span>
              <span className='font-semibold'>{selectedPlan?.name}</span>
            </div>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Billing:</span>
              <span className='font-semibold capitalize'>{billingCycle}</span>
            </div>
            <div className='flex justify-between text-lg'>
              <span className='font-semibold'>Total:</span>
              <span className='font-bold text-primary'>
                ${orderSummary?.total.toFixed(2)}
              </span>
            </div>
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={handleCancel}>
              Cancel
            </Button>
            <Button
              onClick={handleConfirmSubscription}
              disabled={isSubscribing}
            >
              {isSubscribing ? 'Subscribing...' : 'Confirm Subscription'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
