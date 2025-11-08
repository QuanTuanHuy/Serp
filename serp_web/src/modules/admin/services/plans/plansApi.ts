/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription Plans API endpoints
 */

import { api } from '@/lib/store/api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import {
  SubscriptionPlan,
  PlanModule,
  AddModuleToPlanRequest,
  PlansResponse,
} from '../../types';

export interface GetPlansParams {
  page?: number;
  pageSize?: number;
  organizationId?: number;
  isCustom?: boolean;
  isActive?: boolean;
  search?: string;
}

export const plansApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSubscriptionPlans: builder.query<
      SubscriptionPlan[],
      GetPlansParams | void
    >({
      query: (params) => {
        const queryParams = new URLSearchParams();

        if (params) {
          if (params.page !== undefined)
            queryParams.append('page', String(params.page));
          if (params.pageSize !== undefined)
            queryParams.append('pageSize', String(params.pageSize));
          if (params.organizationId !== undefined)
            queryParams.append('organizationId', String(params.organizationId));
          if (params.isCustom !== undefined)
            queryParams.append('isCustom', String(params.isCustom));
          if (params.isActive !== undefined)
            queryParams.append('isActive', String(params.isActive));
          if (params.search) queryParams.append('search', params.search);
        }

        return {
          url: `/subscription-plans${queryParams.toString() ? `?${queryParams.toString()}` : ''}`,
          method: 'GET',
        };
      },
      transformResponse: (response: any) => {
        const transformed =
          createPaginatedTransform<SubscriptionPlan>()(response);
        return transformed.data?.items || [];
      },
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({ type: 'admin/Plan' as const, id })),
              { type: 'admin/Plan', id: 'LIST' },
            ]
          : [{ type: 'admin/Plan', id: 'LIST' }],
    }),

    getSubscriptionPlansWithPagination: builder.query<
      PlansResponse,
      GetPlansParams
    >({
      query: (params) => {
        const queryParams = new URLSearchParams();

        if (params.page !== undefined)
          queryParams.append('page', String(params.page));
        if (params.pageSize !== undefined)
          queryParams.append('pageSize', String(params.pageSize));
        if (params.organizationId !== undefined)
          queryParams.append('organizationId', String(params.organizationId));
        if (params.isCustom !== undefined)
          queryParams.append('isCustom', String(params.isCustom));
        if (params.isActive !== undefined)
          queryParams.append('isActive', String(params.isActive));
        if (params.search) queryParams.append('search', params.search);

        return {
          url: `/subscription-plans?${queryParams.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createPaginatedTransform<SubscriptionPlan>(),
      providesTags: (result) =>
        result
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'admin/Plan' as const,
                id,
              })),
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
      Partial<SubscriptionPlan>
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

    // Plan Modules endpoints
    getPlanModules: builder.query<PlanModule[], string>({
      query: (planId) => ({
        url: `/subscription-plans/${planId}/modules`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<PlanModule[]>(),
      providesTags: (_result, _error, planId) => [
        { type: 'admin/PlanModule', id: planId },
      ],
    }),

    addModuleToPlan: builder.mutation<
      void,
      { planId: string; data: AddModuleToPlanRequest }
    >({
      query: ({ planId, data }) => ({
        url: `/subscription-plans/${planId}/modules`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: (_result, _error, { planId }) => [
        { type: 'admin/PlanModule', id: planId },
        { type: 'admin/Plan', id: planId },
      ],
    }),

    removeModuleFromPlan: builder.mutation<
      void,
      { planId: string; moduleId: number }
    >({
      query: ({ planId, moduleId }) => ({
        url: `/subscription-plans/${planId}/modules/${moduleId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, { planId }) => [
        { type: 'admin/PlanModule', id: planId },
        { type: 'admin/Plan', id: planId },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetSubscriptionPlansQuery,
  useGetSubscriptionPlansWithPaginationQuery,
  useGetSubscriptionPlanByIdQuery,
  useCreateSubscriptionPlanMutation,
  useUpdateSubscriptionPlanMutation,
  useDeleteSubscriptionPlanMutation,
  useGetPlanModulesQuery,
  useAddModuleToPlanMutation,
  useRemoveModuleFromPlanMutation,
} = plansApi;
