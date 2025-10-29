/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Customer subscription API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type { OrganizationSubscription } from '@/modules/admin/types/subscriptions.types';
import { SubscribeRequest } from '../types';

export const subscriptionApi = api.injectEndpoints({
  endpoints: (builder) => ({
    subscribe: builder.mutation<OrganizationSubscription, SubscribeRequest>({
      query: (req) => ({
        url: '/subscriptions/subscribe',
        method: 'POST',
        body: {
          planId: req.planId,
          billingCycle: req.billingCycle.toUpperCase(),
          isAutoRenew: req.isAutoRenew ?? false,
          notes: req.notes,
        },
      }),
      transformResponse: createDataTransform<OrganizationSubscription>(),
      invalidatesTags: [{ type: 'admin/Subscription', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const { useSubscribeMutation } = subscriptionApi;
