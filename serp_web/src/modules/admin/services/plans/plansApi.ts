/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription Plans API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import { SubscriptionPlan } from '../../types';

export const plansApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSubscriptionPlans: builder.query<SubscriptionPlan[], void>({
      query: () => ({
        url: '/subscription-plans',
        method: 'GET',
      }),
      transformResponse: createDataTransform<SubscriptionPlan[]>(),
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({ type: 'admin/Plan' as const, id })),
              { type: 'admin/Plan', id: 'LIST' },
            ]
          : [{ type: 'admin/Plan', id: 'LIST' }],
    }),

    getSubscriptionPlanById: builder.query<SubscriptionPlan, string>({
      query: (planId) => ({
        url: `/subscription-plans/${planId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<SubscriptionPlan>(),
      providesTags: (_result, _error, id) => [{ type: 'admin/Plan', id }],
    }),

    createSubscriptionPlan: builder.mutation<
      SubscriptionPlan,
      Omit<
        SubscriptionPlan,
        'id' | 'createdAt' | 'updatedAt' | 'createdBy' | 'updatedBy'
      >
    >({
      query: (planData) => ({
        url: '/subscription-plans',
        method: 'POST',
        body: planData,
      }),
      transformResponse: createDataTransform<SubscriptionPlan>(),
      invalidatesTags: [{ type: 'admin/Plan', id: 'LIST' }],
    }),

    updateSubscriptionPlan: builder.mutation<
      SubscriptionPlan,
      { id: string; data: Partial<SubscriptionPlan> }
    >({
      query: ({ id, data }) => ({
        url: `/subscription-plans/${id}`,
        method: 'PUT',
        body: data,
      }),
      transformResponse: createDataTransform<SubscriptionPlan>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'admin/Plan', id },
        { type: 'admin/Plan', id: 'LIST' },
      ],
    }),

    deleteSubscriptionPlan: builder.mutation<void, string>({
      query: (planId) => ({
        url: `/subscription-plans/${planId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'admin/Plan', id },
        { type: 'admin/Plan', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetSubscriptionPlansQuery,
  useGetSubscriptionPlanByIdQuery,
  useCreateSubscriptionPlanMutation,
  useUpdateSubscriptionPlanMutation,
  useDeleteSubscriptionPlanMutation,
} = plansApi;
