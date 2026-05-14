// CRM Territory API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import type {
  Territory,
  TerritoryFilters,
  CreateTerritoryRequest,
  UpdateTerritoryRequest,
  PaginationParams,
  APIResponse,
} from '../types';

export const territoryApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Territory endpoints
    getTerritories: builder.query<
      APIResponse<Territory[]>,
      {
        filters?: TerritoryFilters;
        pagination: PaginationParams;
      }
    >({
      query: ({ filters = {} }) => ({
        url: '/territories',
        method: 'GET',
        params: filters,
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.map(({ territoryCode }) => ({
                type: 'Territory' as const,
                id: territoryCode,
              })),
              { type: 'Territory', id: 'LIST' },
            ]
          : [{ type: 'Territory', id: 'LIST' }],
    }),

    getTerritory: builder.query<APIResponse<Territory>, string>({
      query: (territoryCode) => ({
        url: `/territories/${territoryCode}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, territoryCode) => [
        { type: 'Territory', id: territoryCode },
      ],
    }),

    createTerritory: builder.mutation<
      APIResponse<Territory>,
      CreateTerritoryRequest
    >({
      query: (data) => ({ url: '/territories', method: 'POST', body: data }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Territory', id: 'LIST' }],
    }),

    updateTerritory: builder.mutation<
      APIResponse<Territory>,
      {
        territoryCode: string;
        data: UpdateTerritoryRequest;
      }
    >({
      query: ({ territoryCode, data }) => ({
        url: `/territories/${territoryCode}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { territoryCode }) => [
        { type: 'Territory', id: territoryCode },
        { type: 'Territory', id: 'LIST' },
      ],
    }),

    activateTerritory: builder.mutation<APIResponse<Territory>, string>({
      query: (territoryCode) => ({
        url: `/territories/${territoryCode}/activate`,
        method: 'POST',
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, territoryCode) => [
        { type: 'Territory', id: territoryCode },
      ],
    }),

    deactivateTerritory: builder.mutation<APIResponse<Territory>, string>({
      query: (territoryCode) => ({
        url: `/territories/${territoryCode}/deactivate`,
        method: 'POST',
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, territoryCode) => [
        { type: 'Territory', id: territoryCode },
      ],
    }),

    getTerritoryOwner: builder.query<
      APIResponse<{
        territoryCode: string;
        teamId: number;
        teamName: string;
        active: boolean;
        assignedAt?: string;
        assignedBy?: number;
      }>,
      string
    >({
      query: (territoryCode) => ({
        url: `/territories/${territoryCode}/owner`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, territoryCode) => [
        { type: 'Territory', id: `${territoryCode}-OWNER` },
      ],
    }),
  }),
});

export const {
  useGetTerritoriesQuery,
  useGetTerritoryQuery,
  useCreateTerritoryMutation,
  useUpdateTerritoryMutation,
  useActivateTerritoryMutation,
  useDeactivateTerritoryMutation,
  useGetTerritoryOwnerQuery,
} = territoryApi;
