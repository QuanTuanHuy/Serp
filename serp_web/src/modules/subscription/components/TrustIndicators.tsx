/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import React from 'react';
import { Card, CardContent } from '@/shared/components/ui';
import { Shield, Lock, RefreshCw, CheckCircle } from 'lucide-react';

export const TrustIndicators: React.FC = () => {
  const indicators = [
    {
      icon: Shield,
      title: 'PCI DSS Compliant',
      description: 'Bank-level security standards',
    },
    {
      icon: Lock,
      title: '256-bit SSL Encryption',
      description: 'Your data is always encrypted',
    },
    {
      icon: RefreshCw,
      title: '30-Day Money Back',
      description: 'Full refund, no questions asked',
    },
    {
      icon: CheckCircle,
      title: 'Cancel Anytime',
      description: 'No long-term contracts required',
    },
  ];

  return (
    <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
      {indicators.map((indicator, index) => (
        <Card key={index} className='border-muted'>
          <CardContent className='flex flex-col items-center text-center pt-6'>
            <div className='w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mb-3'>
              <indicator.icon className='h-6 w-6 text-primary' />
            </div>
            <h3 className='font-semibold text-sm mb-1'>{indicator.title}</h3>
            <p className='text-xs text-muted-foreground'>
              {indicator.description}
            </p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};
