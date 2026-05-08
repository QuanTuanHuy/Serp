import { api } from '@/lib/store/api';
import type {
  TtcrsApiResponse,
  TtcrsPageResponse,
  TtcrsRequest,
  RequestFilterParams,
  CreateRequestPayload,
  UpdateRequestPayload,
  UpdateRequestsStatusPayload,
  CreateTransportPlanInputPayload,
  TransportPlanResult,
  SaveTransportPlanPayload,
  TransportPlanSavedItem,
  TransportPlanListItem,
  TransportPlanDetail,
  CreateLocationPayload,
  UpdateLocationPayload,
  LocationItem,
  ContainerItem,
  TruckItem,
  TrailerItem,
  DriverItem,
  CreateContainerPayload,
  CreateTruckPayload,
  CreateTrailerPayload,
  UpdateContainerPayload,
  UpdateTruckPayload,
  UpdateTrailerPayload,
  UpdateDriverPayload,
  CancelRoutePayload,
  CompleteStopPayload,
  UploadEvidenceResponse,
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
    // PATCH /ttcrs/api/v1/dispatcher/requests/status
    // -------------------------------------------------------------------------
    updateDispatcherRequestsStatus: builder.mutation<
      TtcrsApiResponse<TtcrsRequest[]>,
      UpdateRequestsStatusPayload
    >({
      query: (body) => ({
        url: '/dispatcher/requests/status',
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Request', id: 'LIST' }],
    }),

    // -------------------------------------------------------------------------
    // POST /ttcrs/api/v1/dispatcher/requests/transport-plan
    // -------------------------------------------------------------------------
    createDispatcherTransportPlan: builder.mutation<
      TtcrsApiResponse<TransportPlanResult>,
      CreateTransportPlanInputPayload
    >({
      query: (body) => ({
        url: '/dispatcher/requests/transport-plan',
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
    getDispatcherContainers: builder.query<
      TtcrsApiResponse<ContainerItem[]>,
      void
    >({
      query: () => ({ url: '/dispatcher/resources/containers', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Resource', id: 'CONTAINERS' }],
    }),

    // POST /ttcrs/api/v1/dispatcher/resources/containers
    createDispatcherContainer: builder.mutation<
      TtcrsApiResponse<ContainerItem>,
      CreateContainerPayload
    >({
      query: (body) => ({
        url: '/dispatcher/resources/containers',
        method: 'POST',
        body,
      }),
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
    createDispatcherTruck: builder.mutation<
      TtcrsApiResponse<TruckItem>,
      CreateTruckPayload
    >({
      query: (body) => ({
        url: '/dispatcher/resources/trucks',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'TRUCKS' }],
    }),

    // -------------------------------------------------------------------------
    // GET /ttcrs/api/v1/dispatcher/resources/trailers
    // -------------------------------------------------------------------------
    getDispatcherTrailers: builder.query<TtcrsApiResponse<TrailerItem[]>, void>(
      {
        query: () => ({ url: '/dispatcher/resources/trailers', method: 'GET' }),
        extraOptions: { service: 'ttcrs' },
        providesTags: [{ type: 'ttcrs/Resource', id: 'TRAILERS' }],
      }
    ),

    // POST /ttcrs/api/v1/dispatcher/resources/trailers
    createDispatcherTrailer: builder.mutation<
      TtcrsApiResponse<TrailerItem>,
      CreateTrailerPayload
    >({
      query: (body) => ({
        url: '/dispatcher/resources/trailers',
        method: 'POST',
        body,
      }),
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

    // -------------------------------------------------------------------------
    // PUT /ttcrs/api/v1/dispatcher/requests/{id}
    // -------------------------------------------------------------------------
    updateDispatcherRequest: builder.mutation<
      TtcrsApiResponse<TtcrsRequest>,
      { id: number; body: UpdateRequestPayload }
    >({
      query: ({ id, body }) => ({
        url: `/dispatcher/requests/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_r, _e, arg) => [
        { type: 'ttcrs/Request', id: arg.id },
        { type: 'ttcrs/Request', id: 'LIST' },
      ],
    }),

    // -------------------------------------------------------------------------
    // PUT /ttcrs/api/v1/dispatcher/locations/{id}
    // -------------------------------------------------------------------------
    updateDispatcherLocation: builder.mutation<
      TtcrsApiResponse<LocationItem>,
      { id: number; body: UpdateLocationPayload }
    >({
      query: ({ id, body }) => ({
        url: `/dispatcher/locations/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Location', id: 'LIST' }],
    }),

    // DELETE /ttcrs/api/v1/dispatcher/locations/{id}
    deleteDispatcherLocation: builder.mutation<void, number>({
      query: (id) => ({ url: `/dispatcher/locations/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Location', id: 'LIST' }],
    }),

    // -------------------------------------------------------------------------
    // Containers update / delete
    // -------------------------------------------------------------------------
    updateDispatcherContainer: builder.mutation<
      TtcrsApiResponse<ContainerItem>,
      { id: number; body: UpdateContainerPayload }
    >({
      query: ({ id, body }) => ({
        url: `/dispatcher/resources/containers/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'CONTAINERS' }],
    }),

    deleteDispatcherContainer: builder.mutation<void, number>({
      query: (id) => ({
        url: `/dispatcher/resources/containers/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'CONTAINERS' }],
    }),

    // -------------------------------------------------------------------------
    // Trucks update / delete
    // -------------------------------------------------------------------------
    updateDispatcherTruck: builder.mutation<
      TtcrsApiResponse<TruckItem>,
      { id: number; body: UpdateTruckPayload }
    >({
      query: ({ id, body }) => ({
        url: `/dispatcher/resources/trucks/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'TRUCKS' }],
    }),

    deleteDispatcherTruck: builder.mutation<void, number>({
      query: (id) => ({
        url: `/dispatcher/resources/trucks/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'TRUCKS' }],
    }),

    // -------------------------------------------------------------------------
    // Trailers update / delete
    // -------------------------------------------------------------------------
    updateDispatcherTrailer: builder.mutation<
      TtcrsApiResponse<TrailerItem>,
      { id: number; body: UpdateTrailerPayload }
    >({
      query: ({ id, body }) => ({
        url: `/dispatcher/resources/trailers/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'TRAILERS' }],
    }),

    deleteDispatcherTrailer: builder.mutation<void, number>({
      query: (id) => ({
        url: `/dispatcher/resources/trailers/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'TRAILERS' }],
    }),

    // -------------------------------------------------------------------------
    // Drivers update / delete
    // -------------------------------------------------------------------------
    // PUT /ttcrs/api/v1/dispatcher/resources/drivers/{userId} — status only
    updateDispatcherDriver: builder.mutation<
      TtcrsApiResponse<DriverItem>,
      { id: number; body: UpdateDriverPayload }
    >({
      query: ({ id, body }) => ({
        url: `/dispatcher/resources/drivers/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Resource', id: 'DRIVERS' }],
    }),

    // GET /ttcrs/api/v1/driver/transport-plans
    getMyTransportPlans: builder.query<
      TtcrsApiResponse<TransportPlanListItem[]>,
      void
    >({
      query: () => ({ url: '/driver/transport-plans', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Request', id: 'MY_TRANSPORT_PLANS' }],
    }),

    // GET /ttcrs/api/v1/driver/transport-plans/{id}
    getMyTransportPlanDetail: builder.query<
      TtcrsApiResponse<TransportPlanDetail>,
      number
    >({
      query: (id) => ({ url: `/driver/transport-plans/${id}`, method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: (_, __, id) => [
        { type: 'ttcrs/Request', id: `MY_TRANSPORT_PLAN_${id}` },
      ],
    }),

    // GET /ttcrs/api/v1/dispatcher/transport-plans
    getTransportPlans: builder.query<
      TtcrsApiResponse<TransportPlanListItem[]>,
      void
    >({
      query: () => ({ url: '/dispatcher/transport-plans', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: [{ type: 'ttcrs/Request', id: 'TRANSPORT_PLANS' }],
    }),

    // GET /ttcrs/api/v1/dispatcher/transport-plans/{id}
    getTransportPlanDetail: builder.query<
      TtcrsApiResponse<TransportPlanDetail>,
      number
    >({
      query: (id) => ({
        url: `/dispatcher/transport-plans/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'ttcrs' },
      providesTags: (_, __, id) => [
        { type: 'ttcrs/Request', id: `TRANSPORT_PLAN_${id}` },
      ],
    }),

    // POST /ttcrs/api/v1/dispatcher/transport-plans
    saveTransportPlans: builder.mutation<
      TtcrsApiResponse<TransportPlanSavedItem[]>,
      SaveTransportPlanPayload
    >({
      query: (body) => ({
        url: '/dispatcher/transport-plans',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'ttcrs/Request', id: 'LIST' }],
    }),

    // -------------------------------------------------------------------------
    // Driver execution actions
    // -------------------------------------------------------------------------

    // PATCH /ttcrs/api/v1/driver/transport-plans/{id}/start
    startRoute: builder.mutation<TtcrsApiResponse<TransportPlanDetail>, number>({
      query: (id) => ({ url: `/driver/transport-plans/${id}/start`, method: 'PATCH' }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_r, _e, id) => [
        { type: 'ttcrs/Request', id: `MY_TRANSPORT_PLAN_${id}` },
        { type: 'ttcrs/Request', id: 'MY_TRANSPORT_PLANS' },
      ],
    }),

    // PATCH /ttcrs/api/v1/dispatcher/transport-plans/{id}/cancel
    cancelRoute: builder.mutation<
      TtcrsApiResponse<TransportPlanDetail>,
      { id: number; body: CancelRoutePayload }
    >({
      query: ({ id, body }) => ({
        url: `/dispatcher/transport-plans/${id}/cancel`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_r, _e, { id }) => [
        { type: 'ttcrs/Request', id: `TRANSPORT_PLAN_${id}` },
        { type: 'ttcrs/Request', id: 'TRANSPORT_PLANS' },
      ],
    }),

    // PATCH /ttcrs/api/v1/driver/transport-plans/{id}/stops/{seq}/arrive
    arriveAtStop: builder.mutation<
      TtcrsApiResponse<TransportPlanDetail>,
      { id: number; seq: number }
    >({
      query: ({ id, seq }) => ({
        url: `/driver/transport-plans/${id}/stops/${seq}/arrive`,
        method: 'PATCH',
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_r, _e, { id }) => [
        { type: 'ttcrs/Request', id: `MY_TRANSPORT_PLAN_${id}` },
      ],
    }),

    // PATCH /ttcrs/api/v1/driver/transport-plans/{id}/stops/{seq}/complete
    completeStop: builder.mutation<
      TtcrsApiResponse<TransportPlanDetail>,
      { id: number; seq: number; body: CompleteStopPayload }
    >({
      query: ({ id, seq, body }) => ({
        url: `/driver/transport-plans/${id}/stops/${seq}/complete`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_r, _e, { id }) => [
        { type: 'ttcrs/Request', id: `MY_TRANSPORT_PLAN_${id}` },
        { type: 'ttcrs/Request', id: 'MY_TRANSPORT_PLANS' },
      ],
    }),

    // PATCH /ttcrs/api/v1/dispatcher/transport-plans/{id}/restore
    restoreRoute: builder.mutation<TtcrsApiResponse<TransportPlanDetail>, number>({
      query: (id) => ({ url: `/dispatcher/transport-plans/${id}/restore`, method: 'PATCH' }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_r, _e, id) => [
        { type: 'ttcrs/Request', id: `TRANSPORT_PLAN_${id}` },
        { type: 'ttcrs/Request', id: 'TRANSPORT_PLANS' },
      ],
    }),

    // POST /ttcrs/api/v1/driver/transport-plans/evidence/upload
    uploadEvidence: builder.mutation<TtcrsApiResponse<UploadEvidenceResponse>, FormData>({
      query: (formData) => ({
        url: '/driver/transport-plans/evidence/upload',
        method: 'POST',
        body: formData,
      }),
      extraOptions: { service: 'ttcrs' },
    }),
  }),
});

export const {
  useGetDispatcherRequestsQuery,
  useCreateDispatcherRequestsMutation,
  useUpdateDispatcherRequestsStatusMutation,
  useUpdateDispatcherRequestMutation,
  useCreateDispatcherTransportPlanMutation,
  useGetDispatcherLocationsQuery,
  useCreateDispatcherLocationMutation,
  useUpdateDispatcherLocationMutation,
  useDeleteDispatcherLocationMutation,
  useGetDispatcherContainersQuery,
  useCreateDispatcherContainerMutation,
  useUpdateDispatcherContainerMutation,
  useDeleteDispatcherContainerMutation,
  useGetDispatcherTrucksQuery,
  useCreateDispatcherTruckMutation,
  useUpdateDispatcherTruckMutation,
  useDeleteDispatcherTruckMutation,
  useGetDispatcherTrailersQuery,
  useCreateDispatcherTrailerMutation,
  useUpdateDispatcherTrailerMutation,
  useDeleteDispatcherTrailerMutation,
  useGetDispatcherDriversQuery,
  useUpdateDispatcherDriverMutation,
  useSaveTransportPlansMutation,
  useGetTransportPlansQuery,
  useGetTransportPlanDetailQuery,
  useGetMyTransportPlansQuery,
  useGetMyTransportPlanDetailQuery,
  useStartRouteMutation,
  useCancelRouteMutation,
  useArriveAtStopMutation,
  useCompleteStopMutation,
  useRestoreRouteMutation,
  useUploadEvidenceMutation,
} = ttcrsApi;
