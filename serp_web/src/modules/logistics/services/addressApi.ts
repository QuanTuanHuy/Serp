/**
 * Logistics Module - Address API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Address CRUD operations
 */

import { logisticsApi } from './api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  Address,
  CreateAddressRequest,
  UpdateAddressRequest,
} from '../types';

export const addressApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Create address
    createAddress: builder.mutation<void, CreateAddressRequest>({
      query: (body) => ({
        url: '/logistics/api/v1/address/create',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'logistics/Address', id: 'LIST' }],
    }),

    // Update address
    updateAddress: builder.mutation<
      void,
      { addressId: string; data: UpdateAddressRequest }
    >({
      query: ({ addressId, data }) => ({
        url: `/logistics/api/v1/address/update/${addressId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (_result, _error, { addressId }) => [
        { type: 'logistics/Address', id: addressId },
        { type: 'logistics/Address', id: 'LIST' },
      ],
    }),

    // Get addresses by entity
    getAddressesByEntity: builder.query<Address[], string>({
      query: (entityId) => ({
        url: `/logistics/api/v1/address/search/by-entity/${entityId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return [];
      },
      providesTags: (result, _error, entityId) =>
        result
          ? [
              ...result.map(({ id }) => ({
                type: 'logistics/Address' as const,
                id,
              })),
              { type: 'logistics/Address', id: entityId },
            ]
          : [{ type: 'logistics/Address', id: entityId }],
    }),
  }),
  overrideExisting: false,
});

export const {
  useCreateAddressMutation,
  useUpdateAddressMutation,
  useGetAddressesByEntityQuery,
} = addressApi;
