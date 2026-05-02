// CRM Analytics API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import type {
  CRMMetrics,
  PipelineMetrics,
  SalesMetrics,
  LeadSourceMetrics,
  APIResponse,
} from '../types';

export const analyticsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Analytics endpoints
    getCRMMetrics: builder.query<APIResponse<CRMMetrics>, void>({
      query: () => ({
        url: '/analytics/metrics',
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: [{ type: 'Analytics', id: 'METRICS' }],
    }),

    getPipelineMetrics: builder.query<APIResponse<PipelineMetrics[]>, void>({
      query: () => ({
        url: '/analytics/pipeline',
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: [{ type: 'Analytics', id: 'PIPELINE' }],
    }),

    getSalesMetrics: builder.query<
      APIResponse<SalesMetrics[]>,
      { period?: string }
    >({
      query: ({ period = 'monthly' }) => ({
        url: '/analytics/sales',
        params: { period },
      }),
      extraOptions: { service: 'crm' },
      providesTags: [{ type: 'Analytics', id: 'SALES' }],
    }),

    getLeadSourceMetrics: builder.query<APIResponse<LeadSourceMetrics[]>, void>(
      {
        query: () => ({
          url: '/analytics/lead-sources',
          method: 'GET',
        }),
        extraOptions: { service: 'crm' },
        providesTags: [{ type: 'Analytics', id: 'LEAD_SOURCES' }],
      }
    ),
  }),
});

export const {
  useGetCRMMetricsQuery,
  useGetPipelineMetricsQuery,
  useGetSalesMetricsQuery,
  useGetLeadSourceMetricsQuery,
} = analyticsApi;
