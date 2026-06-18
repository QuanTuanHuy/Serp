/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Surcharge rule admin form hook
 */

import React from 'react';
import { getErrorMessage } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import { useUpsertSurchargeRuleMutation } from '../../../api';
import type { SurchargeRuleAdminResponse } from '../../../types';
import {
  buildUpsertSurchargeRuleRequest,
  CALCULATION_TYPE_OPTIONS,
  DEFAULT_SURCHARGE_FORM,
  surchargeResponseToForm,
  type SurchargeRuleFormState,
} from '../billingPageModels';

export const useSurchargeRuleForm = () => {
  const notification = useNotification();
  const [form, setForm] = React.useState<SurchargeRuleFormState>(
    DEFAULT_SURCHARGE_FORM
  );
  const [lastSaved, setLastSaved] =
    React.useState<SurchargeRuleAdminResponse | null>(null);
  const [upsertSurchargeRule, { isLoading }] = useUpsertSurchargeRuleMutation();

  const calculationTypeHelper = React.useMemo(() => {
    return CALCULATION_TYPE_OPTIONS.find(
      (option) => option.value === form.calculationType
    )?.helper;
  }, [form.calculationType]);

  const submit = async (event: React.FormEvent): Promise<boolean> => {
    event.preventDefault();

    try {
      const response = await upsertSurchargeRule(
        buildUpsertSurchargeRuleRequest(form)
      ).unwrap();
      setLastSaved(response);
      notification.success('Surcharge rule saved successfully.');
      return true;
    } catch (error) {
      notification.error('Unable to save surcharge rule.', {
        description: getErrorMessage(error),
      });
      return false;
    }
  };

  const reset = () => {
    setForm(DEFAULT_SURCHARGE_FORM);
  };

  const loadFromSaved = (saved: SurchargeRuleAdminResponse) => {
    setForm(surchargeResponseToForm(saved));
    setLastSaved(saved);
  };

  return {
    form,
    setForm,
    lastSaved,
    calculationTypeHelper,
    isLoading,
    submit,
    reset,
    loadFromSaved,
  };
};
