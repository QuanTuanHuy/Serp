/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Tariff rule admin form hook
 */

import React from 'react';
import { getErrorMessage } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import { useUpsertTariffMutation } from '../../../../api';
import type { TariffAdminResponse } from '../../../../types';
import {
  buildUpsertTariffRequest,
  DEFAULT_TARIFF_FORM,
  tariffResponseToForm,
  type TariffFormState,
} from '../billingPageModels';

export const useTariffRuleForm = () => {
  const notification = useNotification();
  const [form, setForm] = React.useState<TariffFormState>(DEFAULT_TARIFF_FORM);
  const [lastSaved, setLastSaved] = React.useState<TariffAdminResponse | null>(
    null
  );
  const [upsertTariff, { isLoading }] = useUpsertTariffMutation();

  const submit = async (event: React.FormEvent): Promise<boolean> => {
    event.preventDefault();

    try {
      const response = await upsertTariff(
        buildUpsertTariffRequest(form)
      ).unwrap();
      setLastSaved(response);
      notification.success('Tariff rule saved successfully.');
      return true;
    } catch (error) {
      notification.error('Unable to save tariff rule.', {
        description: getErrorMessage(error),
      });
      return false;
    }
  };

  const reset = () => {
    setForm(DEFAULT_TARIFF_FORM);
  };

  const loadFromSaved = (saved: TariffAdminResponse) => {
    setForm(tariffResponseToForm(saved));
    setLastSaved(saved);
  };

  return {
    form,
    setForm,
    lastSaved,
    isLoading,
    submit,
    reset,
    loadFromSaved,
  };
};
