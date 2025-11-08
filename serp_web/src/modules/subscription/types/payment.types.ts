/*
Author: QuanTuanHuy
Description: Part of Serp Project - Payment & Billing Types
*/

import { BillingCycle } from './plan.types';

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
