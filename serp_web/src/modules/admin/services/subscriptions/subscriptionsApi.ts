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
          if (filters.planId) params.append('planId', String(filters.planId));
          if (filters.page !== undefined)
            params.append('page', String(filters.page));
          if (filters.pageSize !== undefined)
            params.append('pageSize', String(filters.pageSize));
          if (filters.sortBy) params.append('sortBy', filters.sortBy);
          if (filters.sortDir) params.append('sortDir', filters.sortDir);

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
        url: `/account/api/v1/subscriptions/${subscriptionId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<OrganizationSubscription>(),
      providesTags: (_result, _error, id) => [
        { type: 'admin/Subscription', id },
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
} = subscriptionsApi;
