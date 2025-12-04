/**
 * Logistics Module - Customer API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Customer query operations
 */

import { logisticsApi } from './api';
import type { Customer, CustomerFilters } from '../types';

export interface CustomersResponse {
  items: Customer[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const customerApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get customers
    getCustomers: builder.query<CustomersResponse, CustomerFilters | void>({
      query: (params) => ({
        url: '/logistics/api/v1/customer/search',
        method: 'GET',
        params: {
          page: params?.page || 1,
          size: params?.size || 10,
          sortBy: params?.sortBy || 'createdStamp',
          sortDirection: params?.sortDirection || 'desc',
          query: params?.query || '',
          ...(params?.statusId && { statusId: params.statusId }),
        },
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          const pageData = response.data;
          return {
            items: pageData.items || [],
            totalItems: pageData.totalItems || 0,
            totalPages: pageData.totalPages || 0,
            currentPage: pageData.currentPage || 1,
          };
        }
        return { items: [], totalItems: 0, totalPages: 0, currentPage: 1 };
      },
      providesTags: (result) =>
        result
          ? [
              ...result.items.map(({ id }) => ({
                type: 'logistics/Customer' as const,
                id,
              })),
              { type: 'logistics/Customer', id: 'LIST' },
            ]
          : [{ type: 'logistics/Customer', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const { useGetCustomersQuery } = customerApi;
