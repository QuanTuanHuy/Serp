/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Fee calculator tab
 */

'use client';

import React from 'react';
import { useBillingCalculator, useBillingLocationOptions } from '../hooks';
import { BillingCalculatorForm } from './BillingCalculatorForm';
import { BillingCalculationResultCard } from './BillingCalculationResultCard';

export const BillingCalculatorTab: React.FC = () => {
  const { formValues, setFormValues, result, isLoading, submit, reset } =
    useBillingCalculator();

  const senderProvinceCode = formValues.senderProvinceCode.trim();
  const receiverProvinceCode = formValues.receiverProvinceCode.trim();

  const {
    provinceOptions,
    senderWardOptions,
    receiverWardOptions,
    isFetchingProvinces,
    isFetchingSenderWards,
    isFetchingReceiverWards,
  } = useBillingLocationOptions({
    senderProvinceCode,
    receiverProvinceCode,
  });

  return (
    <div className='space-y-6'>
      <BillingCalculatorForm
        formValues={formValues}
        onFormChange={setFormValues}
        provinceOptions={provinceOptions}
        senderWardOptions={senderWardOptions}
        receiverWardOptions={receiverWardOptions}
        isFetchingProvinces={isFetchingProvinces}
        isFetchingSenderWards={isFetchingSenderWards}
        isFetchingReceiverWards={isFetchingReceiverWards}
        isLoading={isLoading}
        onSubmit={submit}
        onReset={reset}
      />

      {result ? <BillingCalculationResultCard result={result} /> : null}
    </div>
  );
};
