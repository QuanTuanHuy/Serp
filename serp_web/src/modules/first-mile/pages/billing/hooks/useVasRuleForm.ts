/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - VAS rule admin form hook
 */

import React from 'react';
import { getErrorMessage } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import { useUpsertVasRuleMutation } from '../../../api';
import type { VasRuleAdminResponse } from '../../../types';
import {
  buildUpsertVasRuleRequest,
  CALCULATION_TYPE_OPTIONS,
  DEFAULT_VAS_FORM,
  type VasRuleFormState,
} from '../billingPageModels';

export const useVasRuleForm = () => {
  const notification = useNotification();
  const [form, setForm] = React.useState<VasRuleFormState>(DEFAULT_VAS_FORM);
  const [lastSaved, setLastSaved] = React.useState<VasRuleAdminResponse | null>(
    null
  );
  const [upsertVasRule, { isLoading }] = useUpsertVasRuleMutation();

  const calculationTypeHelper = React.useMemo(() => {
    return CALCULATION_TYPE_OPTIONS.find(
      (option) => option.value === form.calculationType
    )?.helper;
  }, [form.calculationType]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();

    try {
      const response = await upsertVasRule(buildUpsertVasRuleRequest(form)).unwrap();
      setLastSaved(response);
      notification.success('VAS rule saved successfully.');
    } catch (error) {
      notification.error('Unable to save VAS rule.', {
        description: getErrorMessage(error),
      });
    }
  };

  const reset = () => {
    setForm(DEFAULT_VAS_FORM);
  };

  return {
    form,
    setForm,
    lastSaved,
    calculationTypeHelper,
    isLoading,
    submit,
    reset,
  };
};
