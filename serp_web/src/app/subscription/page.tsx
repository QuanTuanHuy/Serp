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
  PaymentForm,
  OrderSummaryComponent,
  AddOnsSection,
  FAQSection,
  TrustIndicators,
} from '@/modules/subscription';
import {
  BillingCycle,
  PaymentInfo,
  AddOn as AddOnType,
  OrderSummary as OrderSummaryType,
} from '@/modules/subscription/types';
import {
  SUBSCRIPTION_PLANS,
  ADD_ONS,
  FAQS,
  TAX_RATE,
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

export default function SubscriptionPage() {
  const router = useRouter();
  const [billingCycle, setBillingCycle] = useState<BillingCycle>('monthly');
  const [selectedPlanId, setSelectedPlanId] = useState<string | null>(null);
  const [addOns, setAddOns] = useState<AddOnType[]>(
    ADD_ONS.map((addon) => ({ ...addon, selected: false, quantity: 1 }))
  );
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [promoCode, setPromoCode] = useState<{ code: string; discount: number; type: 'percentage' | 'fixed' } | undefined>(undefined);

  const selectedPlan = useMemo(
    () => SUBSCRIPTION_PLANS.find((plan) => plan.id === selectedPlanId),
    [selectedPlanId]
  );

  const orderSummary: OrderSummaryType | null = useMemo(() => {
    if (!selectedPlan) return null;

    const basePrice =
      billingCycle === 'monthly'
        ? selectedPlan.monthlyPrice
        : selectedPlan.yearlyPrice;

    const addOnsTotal = addOns
      .filter((addon) => addon.selected)
      .reduce((sum, addon) => sum + addon.price * (addon.quantity || 1), 0);

    const subtotal = basePrice + addOnsTotal;

    let discountAmount = 0;
    if (promoCode) {
      discountAmount =
        promoCode.type === 'percentage'
          ? (subtotal * promoCode.discount) / 100
          : promoCode.discount;
    }

    const afterDiscount = subtotal - discountAmount;
    const tax = afterDiscount * TAX_RATE;
    const total = afterDiscount + tax;

    const nextBillingDate = new Date();
    nextBillingDate.setMonth(
      nextBillingDate.getMonth() + (billingCycle === 'monthly' ? 1 : 12)
    );

    return {
      planId: selectedPlan.id,
      planName: selectedPlan.name,
      billingCycle,
      basePrice,
      addOns: addOns.filter((addon) => addon.selected),
      promoCode,
      subtotal,
      tax,
      total,
      nextBillingDate: nextBillingDate.toISOString(),
    };
  }, [selectedPlan, billingCycle, addOns, promoCode]);

  const handlePlanSelect = (planId: string) => {
    const plan = SUBSCRIPTION_PLANS.find((p) => p.id === planId);
    if (!plan) return;

    setSelectedPlanId(planId);

    // Free plan doesn't need payment
    if (plan.tier === 'free') {
      handleFreeSignup();
      return;
    }

    // Enterprise needs to contact sales
    if (plan.tier === 'enterprise') {
      toast.info('Contact Sales', {
        description: 'Please contact our sales team for enterprise pricing.',
      });
      return;
    }

    // Show payment form for paid plans
    setShowPaymentForm(true);
    setTimeout(() => {
      document.getElementById('payment-section')?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
    }, 100);
  };

  const handleFreeSignup = () => {
    toast.success('Welcome!', {
      description: 'You can now start using SERP with the Free plan.',
    });
    setTimeout(() => {
      router.push('/');
    }, 2000);
  };

  const handleAddOnToggle = (addOnId: string) => {
    setAddOns((prev) =>
      prev.map((addon) =>
        addon.id === addOnId ? { ...addon, selected: !addon.selected } : addon
      )
    );
  };

  const handleAddOnQuantityChange = (addOnId: string, quantity: number) => {
    setAddOns((prev) =>
      prev.map((addon) =>
        addon.id === addOnId ? { ...addon, quantity } : addon
      )
    );
  };

  const handleApplyPromoCode = async (code: string): Promise<boolean> => {
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Mock promo codes
    const validPromoCodes: Record<string, { discount: number; type: 'percentage' | 'fixed' }> = {
      'SAVE20': { discount: 20, type: 'percentage' },
      'WELCOME10': { discount: 10, type: 'fixed' },
    };

    if (validPromoCodes[code]) {
      setPromoCode({ code, ...validPromoCodes[code] });
      toast.success('Promo code applied!');
      return true;
    }

    return false;
  };

  const handlePaymentSubmit = (paymentInfo: PaymentInfo) => {
    // Payment info would be sent to payment gateway here
    // Do NOT log sensitive payment information
    setShowConfirmDialog(true);
  };

  const handleConfirmSubscription = () => {
    setShowConfirmDialog(false);
    toast.success('Subscription Activated!', {
      description: `Welcome to SERP ${selectedPlan?.name}! Your subscription is now active.`,
    });
    
    setTimeout(() => {
      router.push('/');
    }, 2000);
  };

  const handleCancelPayment = () => {
    setShowPaymentForm(false);
    setSelectedPlanId(null);
    setAddOns((prev) =>
      prev.map((addon) => ({ ...addon, selected: false, quantity: 1 }))
    );
    setPromoCode(undefined);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='container mx-auto px-4 py-12'>
        {/* Back Button */}
        <button
          onClick={() => router.push('/')}
          className='flex items-center gap-2 text-muted-foreground hover:text-foreground mb-8 transition-colors'
        >
          <ArrowLeft className='h-4 w-4' />
          Back to Home
        </button>

        {/* Hero Section */}
        <div className='text-center mb-12'>
          <h1 className='text-4xl md:text-5xl font-bold mb-4'>
            Choose Your Perfect Plan
          </h1>
          <p className='text-xl text-muted-foreground max-w-2xl mx-auto'>
            Start with a free trial. Upgrade or downgrade anytime. Cancel with no
            penalties.
          </p>
        </div>

        {/* Billing Toggle */}
        <BillingToggle
          billingCycle={billingCycle}
          onToggle={setBillingCycle}
        />

        {/* Plan Cards */}
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12'>
          {SUBSCRIPTION_PLANS.map((plan) => (
            <PlanCard
              key={plan.id}
              plan={plan}
              billingCycle={billingCycle}
              onSelect={handlePlanSelect}
              isSelected={selectedPlanId === plan.id}
            />
          ))}
        </div>

        {/* Comparison Table */}
        <div className='mb-12'>
          <ComparisonTable plans={SUBSCRIPTION_PLANS} />
        </div>

        {/* Trust Indicators */}
        <div className='mb-12'>
          <TrustIndicators />
        </div>

        {/* Payment Section (shown when a paid plan is selected) */}
        {showPaymentForm && selectedPlan && orderSummary && (
          <div
            id='payment-section'
            className='scroll-mt-20 mb-12 border-t pt-12'
          >
            <h2 className='text-3xl font-bold mb-8 text-center'>
              Complete Your Subscription
            </h2>

            <div className='grid grid-cols-1 lg:grid-cols-3 gap-8'>
              {/* Left Column - Payment & Add-ons */}
              <div className='lg:col-span-2 space-y-8'>
                {/* Add-ons */}
                <AddOnsSection
                  addOns={addOns}
                  onToggle={handleAddOnToggle}
                  onQuantityChange={handleAddOnQuantityChange}
                />

                {/* Payment Form */}
                <PaymentForm
                  onSubmit={handlePaymentSubmit}
                  onCancel={handleCancelPayment}
                />
              </div>

              {/* Right Column - Order Summary (Sticky) */}
              <div className='lg:col-span-1'>
                <OrderSummaryComponent
                  summary={orderSummary}
                  onApplyPromoCode={handleApplyPromoCode}
                />
              </div>
            </div>
          </div>
        )}

        {/* FAQ Section */}
        <div className='mb-12'>
          <FAQSection faqs={FAQS} />
        </div>
      </main>

      {/* Confirmation Dialog */}
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
            <Button
              variant='outline'
              onClick={() => setShowConfirmDialog(false)}
            >
              Cancel
            </Button>
            <Button onClick={handleConfirmSubscription}>
              Confirm Subscription
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
