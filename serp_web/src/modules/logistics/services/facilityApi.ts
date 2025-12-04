/**
 * Logistics Module - Facility API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Facility CRUD operations
 */

import { logisticsApi } from './api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type {
  Facility,
  FacilityDetail,
  CreateFacilityRequest,
  UpdateFacilityRequest,
  FacilityFilters,
} from '../types';

export interface FacilitiesResponse {
  items: Facility[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const facilityApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get facilities
    getFacilities: builder.query<FacilitiesResponse, FacilityFilters | void>({
      query: (params) => ({
        url: '/logistics/api/v1/facility/search',
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
                type: 'logistics/Facility' as const,
                id,
              })),
              { type: 'logistics/Facility', id: 'LIST' },
            ]
          : [{ type: 'logistics/Facility', id: 'LIST' }],
    }),

    // Get facility detail
    getFacility: builder.query<FacilityDetail, string>({
      query: (facilityId) => ({
        url: `/logistics/api/v1/facility/search/${facilityId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return null;
      },
      providesTags: (_result, _error, id) => [
        { type: 'logistics/Facility', id },
      ],
    }),

    // Create facility
    createFacility: builder.mutation<void, CreateFacilityRequest>({
      query: (body) => ({
        url: '/logistics/api/v1/facility/create',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'logistics/Facility', id: 'LIST' }],
    }),

    // Update facility
    updateFacility: builder.mutation<
      void,
      { facilityId: string; data: UpdateFacilityRequest }
    >({
      query: ({ facilityId, data }) => ({
        url: `/logistics/api/v1/facility/update/${facilityId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (_result, _error, { facilityId }) => [
        { type: 'logistics/Facility', id: facilityId },
        { type: 'logistics/Facility', id: 'LIST' },
      ],
    }),

    // Delete facility
    deleteFacility: builder.mutation<void, string>({
      query: (facilityId) => ({
        url: `/logistics/api/v1/facility/delete/${facilityId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'logistics/Facility', id },
        { type: 'logistics/Facility', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetFacilitiesQuery,
  useGetFacilityQuery,
  useCreateFacilityMutation,
  useUpdateFacilityMutation,
  useDeleteFacilityMutation,
} = facilityApi;
