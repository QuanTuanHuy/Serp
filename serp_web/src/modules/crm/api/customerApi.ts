// CRM Customer API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import type { ApiResponse as BaseApiResponse } from '@/lib/store/api/types';
import {
  mapAccountFormToBackendPayload,
  mapAccountListResponse,
  mapActivityListResponse,
  mapContactListResponse,
  mapCustomerFiltersToAccountSearch,
  mapSingleAccountResponse,
} from './mappers';
import type {
  Customer,
  Contact,
  Activity,
  CreateCustomerRequest,
  UpdateCustomerRequest,
  CustomerFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
  BulkOperationResult,
} from '../types';

export const customerApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Customer endpoints
    getCustomers: builder.query<
      APIResponse<PaginatedResponse<Customer>>,
      { filters?: CustomerFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/accounts/search',
        method: 'POST',
        body: mapCustomerFiltersToAccountSearch(filters, pagination),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapAccountListResponse,
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Customer' as const,
                id,
              })),
              { type: 'Customer', id: 'LIST' },
            ]
          : [{ type: 'Customer', id: 'LIST' }],
    }),

    getCustomer: builder.query<APIResponse<Customer>, string>({
      query: (id) => ({
        url: `/accounts/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleAccountResponse,
      providesTags: (result, error, id) => [{ type: 'Customer', id }],
    }),

    createCustomer: builder.mutation<
      APIResponse<Customer>,
      CreateCustomerRequest
    >({
      query: (data) => ({
        url: '/accounts',
        method: 'POST',
        body: mapAccountFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleAccountResponse,
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),

    updateCustomer: builder.mutation<
      APIResponse<Customer>,
      { id: string; data: UpdateCustomerRequest }
    >({
      query: ({ id, data }) => ({
        url: `/accounts/${id}`,
        method: 'PUT',
        body: mapAccountFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleAccountResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Customer', id },
        { type: 'Customer', id: 'LIST' },
      ],
    }),

    deleteCustomer: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (id) => ({
          url: `/accounts/${id}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'crm' },
        invalidatesTags: (result, error, id) => [
          { type: 'Customer', id },
          { type: 'Customer', id: 'LIST' },
        ],
      }
    ),

    getAccountContacts: builder.query<APIResponse<Contact[]>, string>({
      query: (accountId) => ({
        url: `/accounts/${accountId}/contacts`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapContactListResponse,
      providesTags: (result, error, accountId) => [
        { type: 'Customer', id: `${accountId}-CONTACTS` },
      ],
    }),

    createAccountContact: builder.mutation<
      APIResponse<Contact>,
      { accountId: string; data: Record<string, unknown> }
    >({
      query: ({ accountId, data }) => ({
        url: `/accounts/${accountId}/contacts`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { accountId }) => [
        { type: 'Customer', id: `${accountId}-CONTACTS` },
      ],
    }),

    updateContact: builder.mutation<
      APIResponse<Contact>,
      { id: string; data: Record<string, unknown> }
    >({
      query: ({ id, data }) => ({
        url: `/contacts/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
    }),

    deleteContact: builder.mutation<APIResponse<{ deleted: boolean }>, string>({
      query: (id) => ({
        url: `/contacts/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'crm' },
    }),

    setPrimaryContact: builder.mutation<APIResponse<Contact>, string>({
      query: (id) => ({
        url: `/contacts/${id}/set-primary`,
        method: 'PUT',
      }),
      extraOptions: { service: 'crm' },
    }),

    deactivateContact: builder.mutation<APIResponse<Contact>, string>({
      query: (id) => ({
        url: `/contacts/${id}/deactivate`,
        method: 'PUT',
      }),
      extraOptions: { service: 'crm' },
    }),

    getAccountActivities: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { accountId: string; page?: number; size?: number }
    >({
      query: ({ accountId, page = 1, size = 20 }) => ({
        url: `/accounts/${accountId}/activities`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
      providesTags: (result, error, { accountId }) => [
        { type: 'Activity', id: `${accountId}-ACCOUNT` },
      ],
    }),

    bulkDeleteCustomers: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/customers/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetCustomersQuery,
  useGetCustomerQuery,
  useCreateCustomerMutation,
  useUpdateCustomerMutation,
  useDeleteCustomerMutation,
  useGetAccountContactsQuery,
  useCreateAccountContactMutation,
  useUpdateContactMutation,
  useDeleteContactMutation,
  useSetPrimaryContactMutation,
  useDeactivateContactMutation,
  useGetAccountActivitiesQuery,
  useBulkDeleteCustomersMutation,
} = customerApi;

export const useGetAccountsQuery = useGetCustomersQuery;
export const useGetAccountQuery = useGetCustomerQuery;
export const useCreateAccountMutation = useCreateCustomerMutation;
export const useUpdateAccountMutation = useUpdateCustomerMutation;
export const useDeleteAccountMutation = useDeleteCustomerMutation;
