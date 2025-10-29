/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

export type BillingCycle = 'monthly' | 'yearly';

export type PlanTier = 'free' | 'starter' | 'professional' | 'enterprise';

export interface SubscribeRequest {
  planId: number;
  billingCycle: BillingCycle;
  isAutoRenew?: boolean;
  notes?: string;
}

export interface PlanFeature {
  name: string;
  included: boolean;
  tooltip?: string;
}

export interface PlanFeatureCategory {
  category: string;
  features: PlanFeature[];
}

export interface SubscriptionPlan {
  id: string;
  tier: PlanTier;
  name: string;
  description: string;
  monthlyPrice: number;
  yearlyPrice: number;
  features: string[];
  featuresDetailed: PlanFeatureCategory[];
  maxUsers: number | 'unlimited';
  storage: string;
  popular?: boolean;
  currentPlan?: boolean;
  ctaText: string;
}

export interface AddOn {
  id: string;
  name: string;
  description: string;
  price: number;
  unit: string;
  selected?: boolean;
  quantity?: number;
}

export interface PaymentMethod {
  type: 'visa' | 'mastercard' | 'amex' | 'discover';
  last4: string;
}

export interface BillingAddress {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface PaymentInfo {
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  billingAddress: BillingAddress;
}

export interface PromoCode {
  code: string;
  discount: number;
  type: 'percentage' | 'fixed';
}

export interface OrderSummary {
  planId: string;
  planName: string;
  billingCycle: BillingCycle;
  basePrice: number;
  addOns: AddOn[];
  promoCode?: PromoCode;
  subtotal: number;
  tax: number;
  total: number;
  nextBillingDate: string;
}

export interface FAQ {
  question: string;
  answer: string;
}
