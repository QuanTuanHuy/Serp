/**
 * Logistics Module - Order API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Order query operations
 */

import { logisticsApi } from './api';
import type { Order, OrderDetail, OrderFilters } from '../types';

export interface OrdersResponse {
  items: Order[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const orderApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get orders
    getOrders: builder.query<OrdersResponse, OrderFilters | void>({
      query: (params) => ({
        url: '/logistics/api/v1/order/search',
        method: 'GET',
        params: {
          page: params?.page || 1,
          size: params?.size || 10,
          sortBy: params?.sortBy || 'createdStamp',
          sortDirection: params?.sortDirection || 'desc',
          query: params?.query || '',
          ...(params?.statusId && { statusId: params.statusId }),
          ...(params?.orderTypeId && { orderTypeId: params.orderTypeId }),
          ...(params?.toCustomerId && { toCustomerId: params.toCustomerId }),
          ...(params?.fromSupplierId && { fromSupplierId: params.fromSupplierId }),
          ...(params?.saleChannelId && { saleChannelId: params.saleChannelId }),
          ...(params?.orderDateAfter && { orderDateAfter: params.orderDateAfter }),
          ...(params?.orderDateBefore && { orderDateBefore: params.orderDateBefore }),
          ...(params?.deliveryBefore && { deliveryBefore: params.deliveryBefore }),
          ...(params?.deliveryAfter && { deliveryAfter: params.deliveryAfter }),
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
                type: 'logistics/Order' as const,
                id,
              })),
              { type: 'logistics/Order', id: 'LIST' },
            ]
          : [{ type: 'logistics/Order', id: 'LIST' }],
    }),

    // Get order detail
    getOrder: builder.query<OrderDetail, string>({
      query: (orderId) => ({
        url: `/logistics/api/v1/order/search/${orderId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return null;
      },
      providesTags: (_result, _error, id) => [{ type: 'logistics/Order', id }],
    }),
  }),
  overrideExisting: false,
});

export const { useGetOrdersQuery, useGetOrderQuery } = orderApi;
