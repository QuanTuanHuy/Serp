/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Chargeable weight config admin form hook
 */

import React from 'react';
import { getErrorMessage } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import { useUpsertChargeableWeightConfigMutation } from '../../../../api';
import type { ChargeableWeightConfigAdminResponse } from '../../../../types';
import {
  buildUpsertChargeableWeightConfigRequest,
  chargeableWeightConfigResponseToForm,
  DEFAULT_CHARGEABLE_WEIGHT_CONFIG_FORM,
  type ChargeableWeightConfigFormState,
} from '../billingPageModels';

export const useChargeableWeightConfigForm = () => {
  const notification = useNotification();
  const [form, setForm] = React.useState<ChargeableWeightConfigFormState>(
    DEFAULT_CHARGEABLE_WEIGHT_CONFIG_FORM
  );
  const [lastSaved, setLastSaved] =
    React.useState<ChargeableWeightConfigAdminResponse | null>(null);
  const [upsertChargeableWeightConfig, { isLoading }] =
    useUpsertChargeableWeightConfigMutation();

  const submit = async (event: React.FormEvent): Promise<boolean> => {
    event.preventDefault();

    try {
      const response = await upsertChargeableWeightConfig(
        buildUpsertChargeableWeightConfigRequest(form)
      ).unwrap();
      setLastSaved(response);
      notification.success('Đã lưu cấu hình khối lượng tính cước.');
      return true;
    } catch (error) {
      notification.error('Không thể lưu cấu hình khối lượng tính cước.', {
        description: getErrorMessage(error),
      });
      return false;
    }
  };

  const reset = () => {
    setForm(DEFAULT_CHARGEABLE_WEIGHT_CONFIG_FORM);
  };

  const loadFromSaved = (saved: ChargeableWeightConfigAdminResponse) => {
    setForm(chargeableWeightConfigResponseToForm(saved));
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
