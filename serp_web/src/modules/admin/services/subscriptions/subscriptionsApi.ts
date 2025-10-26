/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscriptions API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  OrganizationSubscription,
  SubscriptionFilters,
  SubscriptionsResponse,
} from '../../types';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';

export const subscriptionsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSubscriptions: builder.query<SubscriptionsResponse, SubscriptionFilters>(
      {
        query: (filters) => {
          const params = new URLSearchParams();

          if (filters.organizationId)
            params.append('organizationId', String(filters.organizationId));
          if (filters.status) params.append('status', filters.status);
          if (filters.planId) params.append('planId', String(filters.planId)); // BE may ignore
          if (filters.page !== undefined)
            params.append('page', String(filters.page));
          if (filters.pageSize !== undefined)
            params.append('pageSize', String(filters.pageSize));
          if (filters.sortBy) params.append('sortBy', filters.sortBy);
          if (filters.sortDir) params.append('sortDir', filters.sortDir);
          if (filters.billingCycle) {
            const allowed = ['MONTHLY', 'YEARLY', 'TRIAL'];
            const bc = String(filters.billingCycle).toUpperCase();
            if (allowed.includes(bc)) params.append('billingCycle', bc);
          }

          return {
            url: `/admin/subscriptions?${params.toString()}`,
            method: 'GET',
          };
        },
        transformResponse: createPaginatedTransform<OrganizationSubscription>(),
        providesTags: (result) =>
          result?.data.items
            ? [
                ...result.data.items.map(({ id }) => ({
                  type: 'admin/Subscription' as const,
                  id,
                })),
                { type: 'admin/Subscription', id: 'LIST' },
              ]
            : [{ type: 'admin/Subscription', id: 'LIST' }],
      }
    ),

    getSubscriptionById: builder.query<OrganizationSubscription, string>({
      query: (subscriptionId) => ({
        url: `/subscriptions/${subscriptionId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<OrganizationSubscription>(),
      providesTags: (_result, _error, id) => [
        { type: 'admin/Subscription', id },
      ],
    }),

    activateSubscription: builder.mutation<
      { success: boolean },
      { subscriptionId: number }
    >({
      query: ({ subscriptionId }) => ({
        url: `/admin/subscriptions/${subscriptionId}/activate`,
        method: 'PUT',
      }),
      invalidatesTags: (_result, _error, { subscriptionId }) => [
        { type: 'admin/Subscription', id: subscriptionId },
        { type: 'admin/Subscription', id: 'LIST' },
      ],
    }),

    rejectSubscription: builder.mutation<
      { success: boolean },
      { subscriptionId: number; reason: string }
    >({
      query: ({ subscriptionId, reason }) => ({
        url: `/admin/subscriptions/${subscriptionId}/reject`,
        method: 'PUT',
        body: { reason },
      }),
      invalidatesTags: (_result, _error, { subscriptionId }) => [
        { type: 'admin/Subscription', id: subscriptionId },
        { type: 'admin/Subscription', id: 'LIST' },
      ],
    }),

    expireSubscription: builder.mutation<
      { success: boolean },
      { subscriptionId: number }
    >({
      query: ({ subscriptionId }) => ({
        url: `/admin/subscriptions/${subscriptionId}/expire`,
        method: 'PUT',
      }),
      invalidatesTags: (_result, _error, { subscriptionId }) => [
        { type: 'admin/Subscription', id: subscriptionId },
        { type: 'admin/Subscription', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetSubscriptionsQuery,
  useGetSubscriptionByIdQuery,
  useLazyGetSubscriptionsQuery,
  useLazyGetSubscriptionByIdQuery,
  useActivateSubscriptionMutation,
  useRejectSubscriptionMutation,
  useExpireSubscriptionMutation,
} = subscriptionsApi;
