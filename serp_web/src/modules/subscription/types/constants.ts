/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import { SubscriptionPlan, AddOn, FAQ } from './index';

export const SUBSCRIPTION_PLANS: SubscriptionPlan[] = [
  {
    id: 'free',
    tier: 'free',
    name: 'Free',
    description: 'Perfect for individuals and small teams getting started',
    monthlyPrice: 0,
    yearlyPrice: 0,
    features: [
      'Up to 5 users',
      '10 GB storage',
      'Basic CRM features',
      'Task management',
      'Email support',
    ],
    featuresDetailed: [
      {
        category: 'Core Features',
        features: [
          { name: 'Basic CRM', included: true },
          { name: 'Task Management', included: true },
          { name: 'Contact Management', included: true },
          { name: 'Dashboard Analytics', included: false },
          { name: 'Custom Reports', included: false },
        ],
      },
      {
        category: 'Advanced Features',
        features: [
          { name: 'Workflow Automation', included: false },
          { name: 'API Access', included: false },
          { name: 'Custom Integrations', included: false },
          { name: 'Advanced Analytics', included: false },
        ],
      },
      {
        category: 'Support Level',
        features: [
          { name: 'Email Support', included: true },
          { name: 'Priority Support', included: false },
          { name: 'Dedicated Account Manager', included: false },
        ],
      },
    ],
    maxUsers: 5,
    storage: '10 GB',
    ctaText: 'Get Started Free',
  },
  {
    id: 'starter',
    tier: 'starter',
    name: 'Starter',
    description: 'Ideal for growing teams with advanced needs',
    monthlyPrice: 29,
    yearlyPrice: 290,
    features: [
      'Up to 20 users',
      '100 GB storage',
      'All Free features',
      'Advanced CRM & Analytics',
      'Workflow automation',
      'Priority email support',
      'API access',
    ],
    featuresDetailed: [
      {
        category: 'Core Features',
        features: [
          { name: 'Basic CRM', included: true },
          { name: 'Task Management', included: true },
          { name: 'Contact Management', included: true },
          { name: 'Dashboard Analytics', included: true },
          { name: 'Custom Reports', included: true },
        ],
      },
      {
        category: 'Advanced Features',
        features: [
          { name: 'Workflow Automation', included: true },
          { name: 'API Access', included: true },
          { name: 'Custom Integrations', included: false },
          { name: 'Advanced Analytics', included: true },
        ],
      },
      {
        category: 'Support Level',
        features: [
          { name: 'Email Support', included: true },
          { name: 'Priority Support', included: true },
          { name: 'Dedicated Account Manager', included: false },
        ],
      },
    ],
    maxUsers: 20,
    storage: '100 GB',
    popular: true,
    ctaText: 'Start Trial',
  },
  {
    id: 'professional',
    tier: 'professional',
    name: 'Professional',
    description: 'For established businesses requiring full features',
    monthlyPrice: 79,
    yearlyPrice: 790,
    features: [
      'Up to 100 users',
      '500 GB storage',
      'All Starter features',
      'Custom integrations',
      'Advanced analytics',
      'Phone & email support',
      'Custom branding',
      'SSO authentication',
    ],
    featuresDetailed: [
      {
        category: 'Core Features',
        features: [
          { name: 'Basic CRM', included: true },
          { name: 'Task Management', included: true },
          { name: 'Contact Management', included: true },
          { name: 'Dashboard Analytics', included: true },
          { name: 'Custom Reports', included: true },
        ],
      },
      {
        category: 'Advanced Features',
        features: [
          { name: 'Workflow Automation', included: true },
          { name: 'API Access', included: true },
          { name: 'Custom Integrations', included: true },
          { name: 'Advanced Analytics', included: true },
        ],
      },
      {
        category: 'Support Level',
        features: [
          { name: 'Email Support', included: true },
          { name: 'Priority Support', included: true },
          { name: 'Dedicated Account Manager', included: false },
        ],
      },
    ],
    maxUsers: 100,
    storage: '500 GB',
    ctaText: 'Subscribe Now',
  },
  {
    id: 'enterprise',
    tier: 'enterprise',
    name: 'Enterprise',
    description: 'Custom solutions for large organizations',
    monthlyPrice: 199,
    yearlyPrice: 1990,
    features: [
      'Unlimited users',
      'Unlimited storage',
      'All Professional features',
      'Dedicated account manager',
      'Custom SLA',
      '24/7 phone support',
      'On-premise deployment option',
      'Advanced security features',
    ],
    featuresDetailed: [
      {
        category: 'Core Features',
        features: [
          { name: 'Basic CRM', included: true },
          { name: 'Task Management', included: true },
          { name: 'Contact Management', included: true },
          { name: 'Dashboard Analytics', included: true },
          { name: 'Custom Reports', included: true },
        ],
      },
      {
        category: 'Advanced Features',
        features: [
          { name: 'Workflow Automation', included: true },
          { name: 'API Access', included: true },
          { name: 'Custom Integrations', included: true },
          { name: 'Advanced Analytics', included: true },
        ],
      },
      {
        category: 'Support Level',
        features: [
          { name: 'Email Support', included: true },
          { name: 'Priority Support', included: true },
          { name: 'Dedicated Account Manager', included: true },
        ],
      },
    ],
    maxUsers: 'unlimited',
    storage: 'Unlimited',
    ctaText: 'Contact Sales',
  },
];

export const ADD_ONS: AddOn[] = [
  {
    id: 'extra-users',
    name: 'Additional Users',
    description: 'Add more team members beyond your plan limit',
    price: 5,
    unit: 'per user/month',
  },
  {
    id: 'extra-storage',
    name: 'Additional Storage',
    description: 'Expand your storage capacity',
    price: 10,
    unit: 'per 100GB/month',
  },
  {
    id: 'premium-support',
    name: 'Premium Support',
    description: '24/7 priority support with dedicated specialists',
    price: 99,
    unit: 'per month',
  },
];

export const FAQS: FAQ[] = [
  {
    question: 'Can I change plans later?',
    answer:
      "Yes! You can upgrade or downgrade your plan at any time. Changes will be reflected in your next billing cycle, and we'll prorate any price differences.",
  },
  {
    question: 'What happens if I cancel?',
    answer:
      "You can cancel your subscription anytime. Your account will remain active until the end of your current billing period, and you won't be charged again.",
  },
  {
    question: 'Do you offer refunds?',
    answer:
      "We offer a 30-day money-back guarantee for all paid plans. If you're not satisfied within the first 30 days, contact us for a full refund.",
  },
  {
    question: 'How does the free trial work?',
    answer:
      'Start with a 14-day free trial of any paid plan. No credit card required. After the trial ends, you can choose to subscribe or continue with the free plan.',
  },
  {
    question: 'Is my data secure?',
    answer:
      "Absolutely! We use bank-level encryption (256-bit SSL) to protect your data. We're PCI DSS compliant and regularly undergo security audits.",
  },
  {
    question: 'Can I get a custom plan for my organization?',
    answer:
      'Yes! For Enterprise customers, we offer custom plans tailored to your specific needs. Contact our sales team to discuss your requirements.',
  },
];

export const BILLING_DISCOUNT_PERCENTAGE = 20; // 20% discount for yearly billing

export const TAX_RATE = 0.1; // 10% tax rate
