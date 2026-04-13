import { api } from '@/lib/store/api';
import type {
  TtcrsApiResponse,
  TtcrsPageResponse,
  TtcrsRequest,
  RequestFilterParams,
  CreateRequestPayload,
  CreateLocationPayload,
  LocationItem,
  ContainerItem,
  TruckItem,
  TrailerItem,
  DriverItem,
  CreateContainerPayload,
  CreateTruckPayload,
  CreateTrailerPayload,
  CreateDriverPayload,
} from '../types';

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
      CreateLocationPayload
    >({
      query: (body) => ({
        url: '/dispatcher/locations',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Location', id: 'LIST' }],
    }),

    // -------------------------------------------------------------------------
    // GET /ttcrs/api/v1/dispatcher/resources/containers
    // -------------------------------------------------------------------------
    getDispatcherContainers: builder.query<TtcrsApiResponse<ContainerItem[]>, void>({
      query: () => ({ url: '/dispatcher/resources/containers', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Resource', id: 'CONTAINERS' }],
    }),

    // POST /ttcrs/api/v1/dispatcher/resources/containers
    createDispatcherContainer: builder.mutation<TtcrsApiResponse<ContainerItem>, CreateContainerPayload>({
      query: (body) => ({ url: '/dispatcher/resources/containers', method: 'POST', body }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'CONTAINERS' }],
    }),

    // -------------------------------------------------------------------------
    // GET /ttcrs/api/v1/dispatcher/resources/trucks
    // -------------------------------------------------------------------------
    getDispatcherTrucks: builder.query<TtcrsApiResponse<TruckItem[]>, void>({
      query: () => ({ url: '/dispatcher/resources/trucks', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Resource', id: 'TRUCKS' }],
    }),

    // POST /ttcrs/api/v1/dispatcher/resources/trucks
    createDispatcherTruck: builder.mutation<TtcrsApiResponse<TruckItem>, CreateTruckPayload>({
      query: (body) => ({ url: '/dispatcher/resources/trucks', method: 'POST', body }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'TRUCKS' }],
    }),

    // -------------------------------------------------------------------------
    // GET /ttcrs/api/v1/dispatcher/resources/trailers
    // -------------------------------------------------------------------------
    getDispatcherTrailers: builder.query<TtcrsApiResponse<TrailerItem[]>, void>({
      query: () => ({ url: '/dispatcher/resources/trailers', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Resource', id: 'TRAILERS' }],
    }),

    // POST /ttcrs/api/v1/dispatcher/resources/trailers
    createDispatcherTrailer: builder.mutation<TtcrsApiResponse<TrailerItem>, CreateTrailerPayload>({
      query: (body) => ({ url: '/dispatcher/resources/trailers', method: 'POST', body }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'TRAILERS' }],
    }),

    // -------------------------------------------------------------------------
    // GET /ttcrs/api/v1/dispatcher/resources/drivers
    // -------------------------------------------------------------------------
    getDispatcherDrivers: builder.query<TtcrsApiResponse<DriverItem[]>, void>({
      query: () => ({ url: '/dispatcher/resources/drivers', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Resource', id: 'DRIVERS' }],
    }),

    // POST /ttcrs/api/v1/dispatcher/resources/drivers
    createDispatcherDriver: builder.mutation<TtcrsApiResponse<DriverItem>, CreateDriverPayload>({
      query: (body) => ({ url: '/dispatcher/resources/drivers', method: 'POST', body }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'DRIVERS' }],
    }),
  }),
});

export const {
  useGetDispatcherRequestsQuery,
  useCreateDispatcherRequestsMutation,
  useGetDispatcherLocationsQuery,
  useCreateDispatcherLocationMutation,
  useGetDispatcherContainersQuery,
  useCreateDispatcherContainerMutation,
  useGetDispatcherTrucksQuery,
  useCreateDispatcherTruckMutation,
  useGetDispatcherTrailersQuery,
  useCreateDispatcherTrailerMutation,
  useGetDispatcherDriversQuery,
  useCreateDispatcherDriverMutation,
} = ttcrsApi;
