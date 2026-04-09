import { api } from '@/lib/store/api';
import type {
  TtcrsApiResponse,
  TtcrsPageResponse,
  TtcrsRequest,
  RequestFilterParams,
  CreateRequestPayload,
  LocationItem,
} from '../types';
import { create } from 'domain';

export const ttcrsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // -------------------------------------------------------------------------
    // GET /ttcrs/api/v1/dispatcher/requests
    // -------------------------------------------------------------------------
    getDispatcherRequests: builder.query<
      TtcrsApiResponse<TtcrsPageResponse<TtcrsRequest>>,
      RequestFilterParams
    >({
      query: (params) => {
        // Build URLSearchParams manually to support multi-value `statuses`
        const searchParams = new URLSearchParams();

        if (params.statuses && params.statuses.length > 0) {
          params.statuses.forEach((s) => searchParams.append('statuses', s));
        }
        if (params.type) searchParams.set('type', params.type);
        if (params.srcLocationCode)
          searchParams.set('srcLocationCode', params.srcLocationCode);
        if (params.destLocationCode)
          searchParams.set('destLocationCode', params.destLocationCode);
        if (params.createdFrom)
          searchParams.set('createdFrom', params.createdFrom);
        if (params.createdTo) searchParams.set('createdTo', params.createdTo);
        if (params.page !== undefined)
          searchParams.set('page', String(params.page));
        if (params.size !== undefined)
          searchParams.set('size', String(params.size));
        if (params.sortBy) searchParams.set('sortBy', params.sortBy);
        if (params.sortDirection)
          searchParams.set('sortDirection', params.sortDirection);

        return {
          url: `/dispatcher/requests?${searchParams.toString()}`,
          method: 'GET',
        };
      },
      extraOptions: { service: 'ttcrs' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'ttcrs/Request' as const,
                id,
              })),
              { type: 'ttcrs/Request', id: 'LIST' },
            ]
          : [{ type: 'ttcrs/Request', id: 'LIST' }],
    }),

    // -------------------------------------------------------------------------
    // POST /ttcrs/api/v1/dispatcher/requests
    // -------------------------------------------------------------------------
    createDispatcherRequests: builder.mutation<
      TtcrsApiResponse<TtcrsRequest[]>,
      CreateRequestPayload
    >({
      query: (body) => ({
        url: '/dispatcher/requests',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Request', id: 'LIST' }],
    }),

    // -------------------------------------------------------------------------
    // GET /ttcrs/api/v1/dispatcher/locations
    // -------------------------------------------------------------------------
    getDispatcherLocations: builder.query<
      TtcrsApiResponse<LocationItem[]>,
      void
    >({
      query: () => ({
        url: '/dispatcher/locations',
        method: 'GET',
      }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Location', id: 'LIST' }],
    }),

    // -------------------------------------------------------------------------  
    // POST /ttcrs/api/v1/dispatcher/locations
    // -------------------------------------------------------------------------
    createDispatcherLocation: builder.mutation<
      TtcrsApiResponse<LocationItem>,
      { name: string; code: string; type: string }
    >({
      query: (body) => ({
        url: '/dispatcher/locations',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Location', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetDispatcherRequestsQuery,
  useCreateDispatcherRequestsMutation,
  useGetDispatcherLocationsQuery,
  useCreateDispatcherLocationMutation,
} = ttcrsApi;
