/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing fee calculator page
 */

'use client';

import React from 'react';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { Calculator, Settings2 } from 'lucide-react';
import {
  BillingCalculatorTab,
  BillingPageHeader,
  BillingPricingAdminTab,
} from './components';

export const BillingPage: React.FC = () => {
  const [activeTab, setActiveTab] = React.useState('calculator');

  return (
    <div className='space-y-6'>
      <BillingPageHeader />

      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList>
          <TabsTrigger value='calculator' className='gap-2'>
            <Calculator className='h-4 w-4' />
            Tính phí
          </TabsTrigger>
          <TabsTrigger value='pricing' className='gap-2'>
            <Settings2 className='h-4 w-4' />
            Quản trị bảng giá
          </TabsTrigger>
        </TabsList>

        <TabsContent value='calculator'>
          <BillingCalculatorTab />
        </TabsContent>

        <TabsContent value='pricing'>
          <BillingPricingAdminTab />
        </TabsContent>
      </Tabs>
    </div>
  );
};
