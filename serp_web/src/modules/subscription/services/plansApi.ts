/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Customer-facing subscription plans API
 */

import { api } from '@/lib/store/api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type { PaginationParams } from '@/lib/store/api/types';
import type {
  SubscriptionPlan,
  PlanModule,
  SubscriptionPlansResponse,
} from '../types';

export interface GetSubscriptionPlansParams extends PaginationParams {
  isCustom?: boolean;
  organizationId?: number;
}

export const plansApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSubscriptionPlans: builder.query<
      SubscriptionPlansResponse,
      GetSubscriptionPlansParams | void
    >({
      query: (params) => {
        const queryParams = params || {};
        return {
          url: '/subscription-plans',
          method: 'GET',
          params: {
            page: queryParams.page,
            pageSize: queryParams.pageSize,
            sortBy: queryParams.sortBy,
            sortDir: queryParams.sortDir,
            isCustom: queryParams.isCustom,
            organizationId: queryParams.organizationId,
          },
        };
      },
      transformResponse: createPaginatedTransform<SubscriptionPlan>(),
      providesTags: (result) =>
        result
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'subscription/Plan' as const,
                id,
              })),
              { type: 'subscription/Plan', id: 'LIST' },
            ]
          : [{ type: 'subscription/Plan', id: 'LIST' }],
    }),

    getSubscriptionPlanById: builder.query<SubscriptionPlan, string>({
      query: (planId) => ({
        url: `/subscription-plans/${planId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<SubscriptionPlan>(),
      providesTags: (_result, _error, id) => [
        { type: 'subscription/Plan', id },
      ],
    }),

    getPlanModules: builder.query<PlanModule[], string>({
      query: (planId) => ({
        url: `/subscription-plans/${planId}/modules`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<PlanModule[]>(),
      providesTags: (_result, _error, planId) => [
        { type: 'subscription/PlanModule', id: planId },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetSubscriptionPlansQuery,
  useGetSubscriptionPlanByIdQuery,
  useGetPlanModulesQuery,
} = plansApi;
