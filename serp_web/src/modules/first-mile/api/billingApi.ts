/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  BillingDeliveryService,
  CalculateShippingFeeRequest,
  CalculateShippingFeeResponse,
  SurchargeRuleAdminResponse,
  TariffAdminResponse,
  UpsertSurchargeRuleRequest,
  UpsertTariffRequest,
} from '../types';
import { unwrapFirstMileResult } from './transforms';

const BILLING_SERVICE = { service: 'tms-billing-service' as const };

export const billingApi = api.injectEndpoints({
  endpoints: (builder) => ({
    calculateShippingFee: builder.mutation<
      CalculateShippingFeeResponse,
      CalculateShippingFeeRequest
    >({
      query: (body) => ({
        url: '/shipping-fees/calculate',
        method: 'POST',
        body,
      }),
      extraOptions: BILLING_SERVICE,
      transformResponse: unwrapFirstMileResult<CalculateShippingFeeResponse>,
    }),
    listTariffs: builder.query<
      TariffAdminResponse[],
      { serviceCode?: BillingDeliveryService } | void
    >({
      query: (params) => ({
        url: '/admin/pricing/tariffs',
        params: params?.serviceCode
          ? { serviceCode: params.serviceCode }
          : undefined,
      }),
      extraOptions: BILLING_SERVICE,
      transformResponse: unwrapFirstMileResult<TariffAdminResponse[]>,
      providesTags: (result) =>
        result
          ? [
              ...result.map((item) => ({
                type: 'billing/Tariff' as const,
                id: item.id,
              })),
              { type: 'billing/Tariff', id: 'LIST' },
            ]
          : [{ type: 'billing/Tariff', id: 'LIST' }],
    }),
    upsertTariff: builder.mutation<TariffAdminResponse, UpsertTariffRequest>({
      query: (body) => ({
        url: '/admin/pricing/tariffs',
        method: 'PUT',
        body,
      }),
      extraOptions: BILLING_SERVICE,
      transformResponse: unwrapFirstMileResult<TariffAdminResponse>,
      invalidatesTags: [{ type: 'billing/Tariff', id: 'LIST' }],
    }),
    listSurchargeRules: builder.query<SurchargeRuleAdminResponse[], void>({
      query: () => ({
        url: '/admin/pricing/surcharge-rules',
      }),
      extraOptions: BILLING_SERVICE,
      transformResponse: unwrapFirstMileResult<SurchargeRuleAdminResponse[]>,
      providesTags: (result) =>
        result
          ? [
              ...result.map((item) => ({
                type: 'billing/SurchargeRule' as const,
                id: item.id,
              })),
              { type: 'billing/SurchargeRule', id: 'LIST' },
            ]
          : [{ type: 'billing/SurchargeRule', id: 'LIST' }],
    }),
    upsertSurchargeRule: builder.mutation<
      SurchargeRuleAdminResponse,
      UpsertSurchargeRuleRequest
    >({
      query: (body) => ({
        url: '/admin/pricing/surcharge-rules',
        method: 'PUT',
        body,
      }),
      extraOptions: BILLING_SERVICE,
      transformResponse: unwrapFirstMileResult<SurchargeRuleAdminResponse>,
      invalidatesTags: [{ type: 'billing/SurchargeRule', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const {
  useCalculateShippingFeeMutation,
  useListTariffsQuery,
  useUpsertTariffMutation,
  useListSurchargeRulesQuery,
  useUpsertSurchargeRuleMutation,
} = billingApi;
