/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Shipping fee calculator hook
 */

import React from 'react';
import { getErrorMessage } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import { useCalculateShippingFeeMutation } from '../../../../api';
import type { CalculateShippingFeeResponse } from '../../../../types';
import {
  buildCalculateRequest,
  DEFAULT_BILLING_FORM,
  type BillingFormState,
} from '../billingPageModels';

export const useBillingCalculator = () => {
  const notification = useNotification();
  const [formValues, setFormValues] =
    React.useState<BillingFormState>(DEFAULT_BILLING_FORM);
  const [calculateShippingFee, { data, isLoading }] =
    useCalculateShippingFeeMutation();

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();

    try {
      const payload = buildCalculateRequest(formValues);
      await calculateShippingFee(payload).unwrap();
      notification.success('Shipping fee calculated successfully.');
    } catch (error) {
      notification.error('Unable to calculate shipping fee.', {
        description: getErrorMessage(error),
      });
    }
  };

  const reset = () => {
    setFormValues(DEFAULT_BILLING_FORM);
  };

  return {
    formValues,
    setFormValues,
    result: data as CalculateShippingFeeResponse | undefined,
    isLoading,
    submit,
    reset,
  };
};
