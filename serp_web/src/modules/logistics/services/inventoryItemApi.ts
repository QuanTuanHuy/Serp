/**
 * Logistics Module - Inventory Item API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Inventory Item CRUD operations
 */

import { logisticsApi } from './api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type {
  InventoryItem,
  CreateInventoryItemRequest,
  UpdateInventoryItemRequest,
  InventoryItemFilters,
} from '../types';

export interface InventoryItemsResponse {
  items: InventoryItem[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const inventoryItemApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get inventory items
    getInventoryItems: builder.query<
      InventoryItemsResponse,
      InventoryItemFilters | void
    >({
      query: (params) => ({
        url: '/logistics/api/v1/inventory-item/search',
        method: 'GET',
        params: {
          page: params?.page || 1,
          size: params?.size || 10,
          sortBy: params?.sortBy || 'createdStamp',
          sortDirection: params?.sortDirection || 'desc',
          query: params?.query || '',
          ...(params?.productId && { productId: params.productId }),
          ...(params?.facilityId && { facilityId: params.facilityId }),
          ...(params?.expirationDateFrom && { expirationDateFrom: params.expirationDateFrom }),
          ...(params?.expirationDateTo && { expirationDateTo: params.expirationDateTo }),
          ...(params?.manufacturingDateFrom && { manufacturingDateFrom: params.manufacturingDateFrom }),
          ...(params?.manufacturingDateTo && { manufacturingDateTo: params.manufacturingDateTo }),
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
                type: 'logistics/InventoryItem' as const,
                id,
              })),
              { type: 'logistics/InventoryItem', id: 'LIST' },
            ]
          : [{ type: 'logistics/InventoryItem', id: 'LIST' }],
    }),

    // Get single inventory item
    getInventoryItem: builder.query<InventoryItem, string>({
      query: (inventoryItemId) => ({
        url: `/logistics/api/v1/inventory-item/search/${inventoryItemId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return null;
      },
      providesTags: (_result, _error, id) => [
        { type: 'logistics/InventoryItem', id },
      ],
    }),

    // Create inventory item
    createInventoryItem: builder.mutation<void, CreateInventoryItemRequest>({
      query: (body) => ({
        url: '/logistics/api/v1/inventory-item/create',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'logistics/InventoryItem', id: 'LIST' }],
    }),

    // Update inventory item
    updateInventoryItem: builder.mutation<
      void,
      { inventoryItemId: string; data: UpdateInventoryItemRequest }
    >({
      query: ({ inventoryItemId, data }) => ({
        url: `/logistics/api/v1/inventory-item/update/${inventoryItemId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (_result, _error, { inventoryItemId }) => [
        { type: 'logistics/InventoryItem', id: inventoryItemId },
        { type: 'logistics/InventoryItem', id: 'LIST' },
      ],
    }),

    // Delete inventory item
    deleteInventoryItem: builder.mutation<void, string>({
      query: (inventoryItemId) => ({
        url: `/logistics/api/v1/inventory-item/delete/${inventoryItemId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'logistics/InventoryItem', id },
        { type: 'logistics/InventoryItem', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetInventoryItemsQuery,
  useGetInventoryItemQuery,
  useCreateInventoryItemMutation,
  useUpdateInventoryItemMutation,
  useDeleteInventoryItemMutation,
} = inventoryItemApi;
