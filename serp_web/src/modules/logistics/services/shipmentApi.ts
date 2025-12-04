/**
 * Logistics Module - Shipment API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shipment CRUD operations
 */

import { logisticsApi } from './api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type {
  Shipment,
  ShipmentDetail,
  CreateShipmentRequest,
  UpdateShipmentRequest,
  InventoryItemDetailForm,
  UpdateInventoryItemDetailRequest,
  ShipmentFilters,
} from '../types';

export interface ShipmentsResponse {
  items: Shipment[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const shipmentApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get shipments
    getShipments: builder.query<ShipmentsResponse, ShipmentFilters | void>({
      query: (params) => ({
        url: '/logistics/api/v1/shipment/search',
        method: 'GET',
        params: {
          page: params?.page || 1,
          size: params?.size || 10,
          sortBy: params?.sortBy || 'createdStamp',
          sortDirection: params?.sortDirection || 'desc',
          query: params?.query || '',
          ...(params?.statusId && { statusId: params.statusId }),
          ...(params?.shipmentTypeId && {
            shipmentTypeId: params.shipmentTypeId,
          }),
          ...(params?.toCustomerId && { toCustomerId: params.toCustomerId }),
          ...(params?.fromSupplierId && {
            fromSupplierId: params.fromSupplierId,
          }),
          ...(params?.orderId && { orderId: params.orderId }),
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
                type: 'logistics/Shipment' as const,
                id,
              })),
              { type: 'logistics/Shipment', id: 'LIST' },
            ]
          : [{ type: 'logistics/Shipment', id: 'LIST' }],
    }),

    // Get shipment detail
    getShipment: builder.query<ShipmentDetail, string>({
      query: (shipmentId) => ({
        url: `/logistics/api/v1/shipment/search/${shipmentId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return null;
      },
      providesTags: (_result, _error, id) => [
        { type: 'logistics/Shipment', id },
      ],
    }),

    // Create shipment
    createShipment: builder.mutation<void, CreateShipmentRequest>({
      query: (body) => ({
        url: '/logistics/api/v1/shipment/create',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'logistics/Shipment', id: 'LIST' }],
    }),

    // Update shipment
    updateShipment: builder.mutation<
      void,
      { shipmentId: string; data: UpdateShipmentRequest }
    >({
      query: ({ shipmentId, data }) => ({
        url: `/logistics/api/v1/shipment/update/${shipmentId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (_result, _error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
        { type: 'logistics/Shipment', id: 'LIST' },
      ],
    }),

    // Import shipment
    importShipment: builder.mutation<void, string>({
      query: (shipmentId) => ({
        url: `/logistics/api/v1/shipment/manage/${shipmentId}/import`,
        method: 'PATCH',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'logistics/Shipment', id },
        { type: 'logistics/InventoryItem', id: 'LIST' },
      ],
    }),

    // Delete shipment
    deleteShipment: builder.mutation<void, string>({
      query: (shipmentId) => ({
        url: `/logistics/api/v1/shipment/delete/${shipmentId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'logistics/Shipment', id },
        { type: 'logistics/Shipment', id: 'LIST' },
      ],
    }),

    // Add item to shipment
    addItemToShipment: builder.mutation<
      void,
      { shipmentId: string; data: InventoryItemDetailForm }
    >({
      query: ({ shipmentId, data }) => ({
        url: `/logistics/api/v1/shipment/create/${shipmentId}/add`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: (_result, _error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
      ],
    }),

    // Update item in shipment
    updateItemInShipment: builder.mutation<
      void,
      {
        shipmentId: string;
        itemId: string;
        data: UpdateInventoryItemDetailRequest;
      }
    >({
      query: ({ shipmentId, itemId, data }) => ({
        url: `/logistics/api/v1/shipment/update/${shipmentId}/update/${itemId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (_result, _error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
      ],
    }),

    // Delete item from shipment
    deleteItemFromShipment: builder.mutation<
      void,
      { shipmentId: string; itemId: string }
    >({
      query: ({ shipmentId, itemId }) => ({
        url: `/logistics/api/v1/shipment/update/${shipmentId}/delete/${itemId}`,
        method: 'PATCH',
      }),
      invalidatesTags: (_result, _error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetShipmentsQuery,
  useGetShipmentQuery,
  useCreateShipmentMutation,
  useUpdateShipmentMutation,
  useImportShipmentMutation,
  useDeleteShipmentMutation,
  useAddItemToShipmentMutation,
  useUpdateItemInShipmentMutation,
  useDeleteItemFromShipmentMutation,
} = shipmentApi;
