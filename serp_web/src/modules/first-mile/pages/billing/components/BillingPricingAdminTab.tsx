/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Pricing administration tab
 */

'use client';

import React from 'react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { CircleHelp } from 'lucide-react';
import {
  useSurchargeRuleForm,
  useTariffRuleForm,
  useVasRuleForm,
} from '../hooks';
import { SurchargeRuleFormCard } from './SurchargeRuleFormCard';
import { TariffRuleFormCard } from './TariffRuleFormCard';
import { VasRuleFormCard } from './VasRuleFormCard';

export const BillingPricingAdminTab: React.FC = () => {
  const [activeTab, setActiveTab] = React.useState('tariff');
  const tariff = useTariffRuleForm();
  const surcharge = useSurchargeRuleForm();
  const vas = useVasRuleForm();

  return (
    <div className='space-y-6'>
      <Alert>
        <CircleHelp className='h-4 w-4' />
        <AlertTitle>How to use pricing administration</AlertTitle>
        <AlertDescription>
          These forms call backend upsert APIs directly. Fill at least the
          required fields in each section and click save. If a rule already
          exists for the same key, the backend will update it.
        </AlertDescription>
      </Alert>

      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList>
          <TabsTrigger value='tariff'>Tariff</TabsTrigger>
          <TabsTrigger value='surcharge'>Surcharge</TabsTrigger>
          <TabsTrigger value='vas'>VAS</TabsTrigger>
        </TabsList>

        <TabsContent value='tariff'>
          <TariffRuleFormCard
            form={tariff.form}
            onFormChange={tariff.setForm}
            lastSaved={tariff.lastSaved}
            isLoading={tariff.isLoading}
            onSubmit={tariff.submit}
            onReset={tariff.reset}
          />
        </TabsContent>

        <TabsContent value='surcharge'>
          <SurchargeRuleFormCard
            form={surcharge.form}
            onFormChange={surcharge.setForm}
            calculationTypeHelper={surcharge.calculationTypeHelper}
            lastSaved={surcharge.lastSaved}
            isLoading={surcharge.isLoading}
            onSubmit={surcharge.submit}
            onReset={surcharge.reset}
          />
        </TabsContent>

        <TabsContent value='vas'>
          <VasRuleFormCard
            form={vas.form}
            onFormChange={vas.setForm}
            calculationTypeHelper={vas.calculationTypeHelper}
            lastSaved={vas.lastSaved}
            isLoading={vas.isLoading}
            onSubmit={vas.submit}
            onReset={vas.reset}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
};
