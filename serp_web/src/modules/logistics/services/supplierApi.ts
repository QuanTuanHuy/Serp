/**
 * Logistics Module - Supplier API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Supplier query operations
 */

import { logisticsApi } from './api';
import type { Supplier, SupplierDetail, SupplierFilters } from '../types';

export interface SuppliersResponse {
  items: Supplier[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const supplierApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get suppliers
    getSuppliers: builder.query<SuppliersResponse, SupplierFilters | void>({
      query: (params) => ({
        url: '/logistics/api/v1/supplier/search',
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
                type: 'logistics/Supplier' as const,
                id,
              })),
              { type: 'logistics/Supplier', id: 'LIST' },
            ]
          : [{ type: 'logistics/Supplier', id: 'LIST' }],
    }),

    // Get supplier detail
    getSupplier: builder.query<SupplierDetail, string>({
      query: (supplierId) => ({
        url: `/logistics/api/v1/supplier/search/${supplierId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return null;
      },
      providesTags: (_result, _error, id) => [
        { type: 'logistics/Supplier', id },
      ],
    }),
  }),
  overrideExisting: false,
});

export const { useGetSuppliersQuery, useGetSupplierQuery } = supplierApi;
