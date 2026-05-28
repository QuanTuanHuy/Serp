/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  CalculateShippingFeeRequest,
  CalculateShippingFeeResponse,
  SurchargeRuleAdminResponse,
  TariffAdminResponse,
  UpsertSurchargeRuleRequest,
  UpsertTariffRequest,
  UpsertVasRuleRequest,
  VasRuleAdminResponse,
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
    upsertTariff: builder.mutation<TariffAdminResponse, UpsertTariffRequest>({
      query: (body) => ({
        url: '/admin/pricing/tariffs',
        method: 'PUT',
        body,
      }),
      extraOptions: BILLING_SERVICE,
      transformResponse: unwrapFirstMileResult<TariffAdminResponse>,
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
    }),
    upsertVasRule: builder.mutation<VasRuleAdminResponse, UpsertVasRuleRequest>({
      query: (body) => ({
        url: '/admin/pricing/vas-rules',
        method: 'PUT',
        body,
      }),
      extraOptions: BILLING_SERVICE,
      transformResponse: unwrapFirstMileResult<VasRuleAdminResponse>,
    }),
  }),
  overrideExisting: false,
});

export const {
  useCalculateShippingFeeMutation,
  useUpsertTariffMutation,
  useUpsertSurchargeRuleMutation,
  useUpsertVasRuleMutation,
} = billingApi;
