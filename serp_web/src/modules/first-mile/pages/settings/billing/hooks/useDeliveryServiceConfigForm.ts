/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Delivery service config admin form hook
 */

import React from 'react';
import { getErrorMessage } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import { useUpsertDeliveryServiceConfigMutation } from '../../../../api';
import type { DeliveryServiceConfigAdminResponse } from '../../../../types';
import {
  buildUpsertDeliveryServiceConfigRequest,
  DEFAULT_DELIVERY_SERVICE_CONFIG_FORM,
  deliveryServiceConfigResponseToForm,
  type DeliveryServiceConfigFormState,
} from '../billingPageModels';

export const useDeliveryServiceConfigForm = () => {
  const notification = useNotification();
  const [form, setForm] = React.useState<DeliveryServiceConfigFormState>(
    DEFAULT_DELIVERY_SERVICE_CONFIG_FORM
  );
  const [lastSaved, setLastSaved] =
    React.useState<DeliveryServiceConfigAdminResponse | null>(null);
  const [upsertDeliveryServiceConfig, { isLoading }] =
    useUpsertDeliveryServiceConfigMutation();

  const submit = async (event: React.FormEvent): Promise<boolean> => {
    event.preventDefault();

    try {
      const response = await upsertDeliveryServiceConfig(
        buildUpsertDeliveryServiceConfigRequest(form)
      ).unwrap();
      setLastSaved(response);
      notification.success('Đã lưu hình thức vận chuyển.');
      return true;
    } catch (error) {
      notification.error('Không thể lưu hình thức vận chuyển.', {
        description: getErrorMessage(error),
      });
      return false;
    }
  };

  const reset = () => {
    setForm(DEFAULT_DELIVERY_SERVICE_CONFIG_FORM);
  };

  const loadFromSaved = (saved: DeliveryServiceConfigAdminResponse) => {
    setForm(deliveryServiceConfigResponseToForm(saved));
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
