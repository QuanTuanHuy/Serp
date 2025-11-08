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
  ModuleSelector,
  useGetSubscriptionPlansQuery,
  useSubscribeMutation,
  useSubscribeCustomPlanMutation,
  useGetAvailableModulesQuery,
} from '@/modules/subscription';
import {
  BillingCycle,
  SubscriptionPlan,
  UISubscriptionPlan,
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
  Card,
} from '@/shared/components/ui';
import { Separator } from '@/shared/components/ui/separator';
import { toast } from 'sonner';
import { ArrowLeft, Settings } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api/utils';

export default function SubscriptionPage() {
  const router = useRouter();
  const [billingCycle, setBillingCycle] = useState<BillingCycle>('monthly');
  const [selectedPlanId, setSelectedPlanId] = useState<string | null>(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [showCustomPlanDialog, setShowCustomPlanDialog] = useState(false);
  const [selectedModules, setSelectedModules] = useState<number[]>([]);

  const {
    data: bePlans,
    isLoading,
    isError,
  } = useGetSubscriptionPlansQuery({
    isCustom: false,
    pageSize: 100,
  });
  const { data: modules, isLoading: isLoadingModules } =
    useGetAvailableModulesQuery();
  const [subscribe, { isLoading: isSubscribing }] = useSubscribeMutation();
  const [subscribeCustomPlan, { isLoading: isSubscribingCustom }] =
    useSubscribeCustomPlanMutation();

  const availableModules = modules || [];

  const mappedPlans = useMemo((): UISubscriptionPlan[] => {
    if (!bePlans) return SUBSCRIPTION_PLANS;

    const templateIndexByKey = new Map<string, UISubscriptionPlan>();
    for (const tpl of SUBSCRIPTION_PLANS) {
      templateIndexByKey.set(tpl.id.toLowerCase(), tpl);
      templateIndexByKey.set(tpl.name.toLowerCase(), tpl);
    }

    const normalize = (s?: string) => (s ? s.trim().toLowerCase() : '');

    const toUiPlan = (p: SubscriptionPlan): UISubscriptionPlan => {
      const key1 = normalize(p.planCode);
      const key2 = normalize(p.planName);
      const tpl = templateIndexByKey.get(key1) || templateIndexByKey.get(key2);
      const base = tpl || SUBSCRIPTION_PLANS[0];

      return {
        ...base,
        id: String(p.id),
        name: p.planName,
        description: p.description || base.description,
        monthlyPrice: Number(p.monthlyPrice ?? 0),
        yearlyPrice: Number(p.yearlyPrice ?? 0),
        maxUsers: p.maxUsers ?? base.maxUsers,
      };
    };

    return bePlans.data.items
      .filter((p) => p.isActive && !p.isCustom)
      .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
      .map(toUiPlan);
  }, [bePlans]);

  const selectedPlan = useMemo(
    () => mappedPlans.find((plan) => plan.id === selectedPlanId),
    [selectedPlanId, mappedPlans]
  );

  const handlePlanSelect = (planId: string) => {
    const plan = mappedPlans.find((p) => p.id === planId);
    if (!plan) return;
    setSelectedPlanId(planId);
    setShowConfirmDialog(true);
  };

  const handleCustomPlanClick = () => {
    setShowCustomPlanDialog(true);
  };

  const handleModuleToggle = (moduleId: number) => {
    setSelectedModules((prev) =>
      prev.includes(moduleId)
        ? prev.filter((id) => id !== moduleId)
        : [...prev, moduleId]
    );
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
      setTimeout(() => router.push('/'), 1200);
    } catch (e: any) {
      const message =
        getErrorMessage(e) || 'Failed to subscribe. Please try again.';
      toast.error('Subscription Failed', { description: message });
    }
  };

  const handleConfirmCustomSubscription = async () => {
    if (selectedModules.length === 0) {
      toast.error('Please select at least one module');
      return;
    }

    try {
      await subscribeCustomPlan({
        billingCycle: billingCycle,
        isAutoRenew: false,
        moduleIds: selectedModules,
      }).unwrap();
      setShowCustomPlanDialog(false);
      setSelectedModules([]);
      toast.success('Custom subscription is processing!', {
        description: 'Wait Admin to confirm your custom plan!',
      });
      setTimeout(() => router.push('/'), 1200);
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

  const handleCancelCustom = () => {
    setShowCustomPlanDialog(false);
    setSelectedModules([]);
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

        <Card className='mb-8 p-6 border-2 border-dashed border-primary/30 bg-primary/5'>
          <div className='flex items-center justify-between'>
            <div className='flex-1'>
              <h3 className='text-lg font-semibold flex items-center gap-2'>
                <Settings className='h-5 w-5' />
                Need a Custom Plan?
              </h3>
              <p className='text-sm text-muted-foreground mt-1'>
                Select only the modules you need and create your personalized
                plan
              </p>
            </div>
            <Button onClick={handleCustomPlanClick} size='lg'>
              Create Custom Plan
            </Button>
          </div>
        </Card>

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
                $
                {billingCycle === 'monthly'
                  ? selectedPlan?.monthlyPrice
                  : selectedPlan?.yearlyPrice}
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

      {/* Custom Plan Dialog */}
      <Dialog
        open={showCustomPlanDialog}
        onOpenChange={setShowCustomPlanDialog}
      >
        <DialogContent className='!max-w-5xl max-h-[85vh] overflow-hidden flex flex-col'>
          <DialogHeader>
            <DialogTitle>Create Custom Plan</DialogTitle>
            <DialogDescription>
              Select the modules you need for your custom plan
            </DialogDescription>
          </DialogHeader>

          <div className='flex-1 overflow-y-auto py-4 px-2'>
            <div className='space-y-4'>
              <div className='flex items-center justify-between px-1'>
                <span className='text-sm text-muted-foreground'>
                  Billing Cycle:
                </span>
                <span className='font-semibold capitalize'>{billingCycle}</span>
              </div>
              <Separator />

              <ModuleSelector
                modules={availableModules}
                selectedModules={selectedModules}
                onModuleToggle={handleModuleToggle}
                isLoading={isLoadingModules}
              />
            </div>
          </div>

          <DialogFooter className='border-t pt-4 flex-shrink-0'>
            <Button variant='outline' onClick={handleCancelCustom}>
              Cancel
            </Button>
            <Button
              onClick={handleConfirmCustomSubscription}
              disabled={isSubscribingCustom || selectedModules.length === 0}
            >
              {isSubscribingCustom
                ? 'Processing...'
                : `Subscribe (${selectedModules.length} module${
                    selectedModules.length !== 1 ? 's' : ''
                  })`}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
