import { api } from '@/lib/store/api';
import { createApiResponseTransform } from '@/lib/store/api/utils';
import type {
  ApiResponse,
  DashboardSummary,
  OperationalReport,
  PagedResponse,
  SchoolBusAttendance,
  SchoolBusAttendanceActionRequest,
  SchoolBusAttendanceManifest,
  SchoolBusListParams,
  SchoolBusMapLocation,
  SchoolBusAttendant,
  SchoolBusAttendantUpsertRequest,
  SchoolBusBus,
  SchoolBusBusType,
  SchoolBusBusUpsertRequest,
  SchoolBusDepot,
  SchoolBusDepotUpsertRequest,
  SchoolBusDriver,
  SchoolBusDriverUpsertRequest,
  SchoolBusParent,
  SchoolBusParentUpsertRequest,
  SchoolBusPickupPoint,
  SchoolBusPickupPointUpsertRequest,
  SchoolBusRejectRequest,
  SchoolBusRoute,
  SchoolBusRouteAssignment,
  SchoolBusRouteAssignmentRequest,
  SchoolBusRouteDetail,
  SchoolBusRouteStop,
  SchoolBusRouteUpsertRequest,
  SchoolBusSchool,
  SchoolBusSchoolUpsertRequest,
  SchoolBusStudent,
  SchoolBusStudentUpsertRequest,
  SchoolBusTransportRequest,
  SchoolBusTransportRequestDetail,
  SchoolBusTransportRequestUpsertRequest,
  SchoolBusTripHistory,
} from '../types';

const transformApiResponse = createApiResponseTransform;

function listQuery(url: string, params: SchoolBusListParams | void) {
  return {
    url,
    method: 'GET',
    params: params || undefined,
  };
}

export const schoolBusApi = api.injectEndpoints({
  endpoints: (builder) => ({
    searchMapLocations: builder.query<ApiResponse<SchoolBusMapLocation[]>, string>({
      query: (query) => ({
        url: '/maps/geocode',
        method: 'GET',
        params: { q: query },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusMapLocation[]>(),
    }),
    reverseMapLocation: builder.query<
      ApiResponse<SchoolBusMapLocation>,
      { lat: number; lng: number }
    >({
      query: ({ lat, lng }) => ({
        url: '/maps/reverse-geocode',
        method: 'GET',
        params: { lat, lng },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusMapLocation>(),
    }),
    getSchoolBusSummary: builder.query<ApiResponse<DashboardSummary>, void>({
      query: () => ({ url: '/dashboard/summary', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<DashboardSummary>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'SUMMARY' }],
    }),
    getSchoolBusReport: builder.query<ApiResponse<OperationalReport>, void>({
      query: () => ({ url: '/reports/operations-summary', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<OperationalReport>(),
      providesTags: [{ type: 'schoolBus/Report', id: 'SUMMARY' }],
    }),
    getBusTypes: builder.query<ApiResponse<SchoolBusBusType[]>, void>({
      query: () => ({ url: '/bus-types', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusBusType[]>(),
    }),

    getSchools: builder.query<
      ApiResponse<PagedResponse<SchoolBusSchool>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/schools', params),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusSchool>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/School' as const,
                id,
              })),
              { type: 'schoolBus/School', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/School', id: 'LIST' }],
    }),
    getSchoolById: builder.query<ApiResponse<SchoolBusSchool>, number>({
      query: (id) => ({ url: `/schools/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchool>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/School', id },
      ],
    }),
    createSchool: builder.mutation<
      ApiResponse<SchoolBusSchool>,
      SchoolBusSchoolUpsertRequest
    >({
      query: (body) => ({ url: '/schools', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchool>(),
      invalidatesTags: [
        { type: 'schoolBus/School', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updateSchool: builder.mutation<
      ApiResponse<SchoolBusSchool>,
      { id: number; body: SchoolBusSchoolUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/schools/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchool>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/School', id: 'LIST' },
        { type: 'schoolBus/School', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deleteSchool: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/schools/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/School', id: 'LIST' },
        { type: 'schoolBus/School', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getParents: builder.query<
      ApiResponse<PagedResponse<SchoolBusParent>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/parents', params),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusParent>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/Parent' as const,
                id,
              })),
              { type: 'schoolBus/Parent', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/Parent', id: 'LIST' }],
    }),
    getParentById: builder.query<ApiResponse<SchoolBusParent>, number>({
      query: (id) => ({ url: `/parents/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusParent>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Parent', id },
      ],
    }),
    createParent: builder.mutation<
      ApiResponse<SchoolBusParent>,
      SchoolBusParentUpsertRequest
    >({
      query: (body) => ({ url: '/parents', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusParent>(),
      invalidatesTags: [
        { type: 'schoolBus/Parent', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updateParent: builder.mutation<
      ApiResponse<SchoolBusParent>,
      { id: number; body: SchoolBusParentUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/parents/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusParent>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Parent', id: 'LIST' },
        { type: 'schoolBus/Parent', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deleteParent: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/parents/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Parent', id: 'LIST' },
        { type: 'schoolBus/Parent', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getStudents: builder.query<
      ApiResponse<PagedResponse<SchoolBusStudent>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/students', params),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusStudent>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/Student' as const,
                id,
              })),
              { type: 'schoolBus/Student', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/Student', id: 'LIST' }],
    }),
    getStudentById: builder.query<ApiResponse<SchoolBusStudent>, number>({
      query: (id) => ({ url: `/students/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusStudent>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Student', id },
      ],
    }),
    createStudent: builder.mutation<
      ApiResponse<SchoolBusStudent>,
      SchoolBusStudentUpsertRequest
    >({
      query: (body) => ({ url: '/students', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusStudent>(),
      invalidatesTags: [
        { type: 'schoolBus/Student', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updateStudent: builder.mutation<
      ApiResponse<SchoolBusStudent>,
      { id: number; body: SchoolBusStudentUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/students/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusStudent>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Student', id: 'LIST' },
        { type: 'schoolBus/Student', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deleteStudent: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/students/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Student', id: 'LIST' },
        { type: 'schoolBus/Student', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getBuses: builder.query<
      ApiResponse<PagedResponse<SchoolBusBus>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/buses', params),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusBus>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/Bus' as const,
                id,
              })),
              { type: 'schoolBus/Bus', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/Bus', id: 'LIST' }],
    }),
    getBusById: builder.query<ApiResponse<SchoolBusBus>, number>({
      query: (id) => ({ url: `/buses/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusBus>(),
      providesTags: (_result, _error, id) => [{ type: 'schoolBus/Bus', id }],
    }),
    createBus: builder.mutation<
      ApiResponse<SchoolBusBus>,
      SchoolBusBusUpsertRequest
    >({
      query: (body) => ({ url: '/buses', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusBus>(),
      invalidatesTags: [
        { type: 'schoolBus/Bus', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updateBus: builder.mutation<
      ApiResponse<SchoolBusBus>,
      { id: number; body: SchoolBusBusUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/buses/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusBus>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Bus', id: 'LIST' },
        { type: 'schoolBus/Bus', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deleteBus: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/buses/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Bus', id: 'LIST' },
        { type: 'schoolBus/Bus', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getDrivers: builder.query<
      ApiResponse<PagedResponse<SchoolBusDriver>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/drivers', params),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusDriver>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/Driver' as const,
                id,
              })),
              { type: 'schoolBus/Driver', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/Driver', id: 'LIST' }],
    }),
    getDriverById: builder.query<ApiResponse<SchoolBusDriver>, number>({
      query: (id) => ({ url: `/drivers/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDriver>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Driver', id },
      ],
    }),
    createDriver: builder.mutation<
      ApiResponse<SchoolBusDriver>,
      SchoolBusDriverUpsertRequest
    >({
      query: (body) => ({ url: '/drivers', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDriver>(),
      invalidatesTags: [
        { type: 'schoolBus/Driver', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updateDriver: builder.mutation<
      ApiResponse<SchoolBusDriver>,
      { id: number; body: SchoolBusDriverUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/drivers/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDriver>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Driver', id: 'LIST' },
        { type: 'schoolBus/Driver', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deleteDriver: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/drivers/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Driver', id: 'LIST' },
        { type: 'schoolBus/Driver', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getAttendants: builder.query<
      ApiResponse<PagedResponse<SchoolBusAttendant>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/attendants', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusAttendant>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/Attendant' as const,
                id,
              })),
              { type: 'schoolBus/Attendant', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/Attendant', id: 'LIST' }],
    }),
    getAttendantById: builder.query<ApiResponse<SchoolBusAttendant>, number>({
      query: (id) => ({ url: `/attendants/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendant>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Attendant', id },
      ],
    }),
    createAttendant: builder.mutation<
      ApiResponse<SchoolBusAttendant>,
      SchoolBusAttendantUpsertRequest
    >({
      query: (body) => ({ url: '/attendants', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendant>(),
      invalidatesTags: [
        { type: 'schoolBus/Attendant', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updateAttendant: builder.mutation<
      ApiResponse<SchoolBusAttendant>,
      { id: number; body: SchoolBusAttendantUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/attendants/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendant>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Attendant', id: 'LIST' },
        { type: 'schoolBus/Attendant', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deleteAttendant: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/attendants/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Attendant', id: 'LIST' },
        { type: 'schoolBus/Attendant', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getPickupPoints: builder.query<
      ApiResponse<PagedResponse<SchoolBusPickupPoint>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/pickup-points', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusPickupPoint>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/PickupPoint' as const,
                id,
              })),
              { type: 'schoolBus/PickupPoint', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/PickupPoint', id: 'LIST' }],
    }),
    getPickupPointById: builder.query<ApiResponse<SchoolBusPickupPoint>, number>({
      query: (id) => ({ url: `/pickup-points/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusPickupPoint>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/PickupPoint', id },
      ],
    }),
    createPickupPoint: builder.mutation<
      ApiResponse<SchoolBusPickupPoint>,
      SchoolBusPickupPointUpsertRequest
    >({
      query: (body) => ({ url: '/pickup-points', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusPickupPoint>(),
      invalidatesTags: [
        { type: 'schoolBus/PickupPoint', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updatePickupPoint: builder.mutation<
      ApiResponse<SchoolBusPickupPoint>,
      { id: number; body: SchoolBusPickupPointUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/pickup-points/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusPickupPoint>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/PickupPoint', id: 'LIST' },
        { type: 'schoolBus/PickupPoint', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deletePickupPoint: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/pickup-points/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/PickupPoint', id: 'LIST' },
        { type: 'schoolBus/PickupPoint', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getDepots: builder.query<
      ApiResponse<PagedResponse<SchoolBusDepot>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/depots', params),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusDepot>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/Depot' as const,
                id,
              })),
              { type: 'schoolBus/Depot', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/Depot', id: 'LIST' }],
    }),
    getDepotById: builder.query<ApiResponse<SchoolBusDepot>, number>({
      query: (id) => ({ url: `/depots/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDepot>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Depot', id },
      ],
    }),
    createDepot: builder.mutation<
      ApiResponse<SchoolBusDepot>,
      SchoolBusDepotUpsertRequest
    >({
      query: (body) => ({ url: '/depots', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDepot>(),
      invalidatesTags: [
        { type: 'schoolBus/Depot', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    updateDepot: builder.mutation<
      ApiResponse<SchoolBusDepot>,
      { id: number; body: SchoolBusDepotUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/depots/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDepot>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Depot', id: 'LIST' },
        { type: 'schoolBus/Depot', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    deleteDepot: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({ url: `/depots/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Depot', id: 'LIST' },
        { type: 'schoolBus/Depot', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),

    getTransportRequests: builder.query<
      ApiResponse<PagedResponse<SchoolBusTransportRequest>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/transport-requests', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusTransportRequest>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/TransportRequest' as const,
                id,
              })),
              { type: 'schoolBus/TransportRequest', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/TransportRequest', id: 'LIST' }],
    }),
    getTransportRequestById: builder.query<
      ApiResponse<SchoolBusTransportRequestDetail>,
      number
    >({
      query: (id) => ({ url: `/transport-requests/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<SchoolBusTransportRequestDetail>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TransportRequest', id },
      ],
    }),
    createTransportRequest: builder.mutation<
      ApiResponse<SchoolBusTransportRequest>,
      SchoolBusTransportRequestUpsertRequest
    >({
      query: (body) => ({ url: '/transport-requests', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTransportRequest>(),
      invalidatesTags: [
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    updateTransportRequest: builder.mutation<
      ApiResponse<SchoolBusTransportRequest>,
      { id: number; body: SchoolBusTransportRequestUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/transport-requests/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTransportRequest>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/TransportRequest', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    approveTransportRequest: builder.mutation<
      ApiResponse<SchoolBusTransportRequest>,
      number
    >({
      query: (id) => ({
        url: `/transport-requests/${id}/approve`,
        method: 'POST',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTransportRequest>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/TransportRequest', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    rejectTransportRequest: builder.mutation<
      ApiResponse<SchoolBusTransportRequest>,
      { id: number; body: SchoolBusRejectRequest }
    >({
      query: ({ id, body }) => ({
        url: `/transport-requests/${id}/reject`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTransportRequest>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/TransportRequest', id },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),

    getRoutes: builder.query<
      ApiResponse<PagedResponse<SchoolBusRoute>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/routes', params),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusRoute>>(),
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'schoolBus/Route' as const,
                id,
              })),
              { type: 'schoolBus/Route', id: 'LIST' },
            ]
          : [{ type: 'schoolBus/Route', id: 'LIST' }],
    }),
    getRouteById: builder.query<ApiResponse<SchoolBusRouteDetail>, number>({
      query: (id) => ({ url: `/routes/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRouteDetail>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
      ],
    }),
    getRouteAttendanceManifest: builder.query<
      ApiResponse<SchoolBusAttendanceManifest>,
      number
    >({
      query: (id) => ({
        url: `/routes/${id}/attendance-manifest`,
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendanceManifest>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id: `MANIFEST-${id}` },
      ],
    }),
    createRoute: builder.mutation<
      ApiResponse<SchoolBusRoute>,
      SchoolBusRouteUpsertRequest
    >({
      query: (body) => ({ url: '/routes', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRoute>(),
      invalidatesTags: [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    updateRoute: builder.mutation<
      ApiResponse<SchoolBusRoute>,
      { id: number; body: SchoolBusRouteUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: `/routes/${id}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRoute>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `MANIFEST-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    generateGreedyPlan: builder.mutation<
      ApiResponse<SchoolBusRouteStop[]>,
      number
    >({
      query: (id) => ({
        url: `/routes/${id}/generate-greedy-plan`,
        method: 'POST',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRouteStop[]>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `MANIFEST-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    assignRoute: builder.mutation<
      ApiResponse<SchoolBusRouteAssignment>,
      { id: number; body: SchoolBusRouteAssignmentRequest }
    >({
      query: ({ id, body }) => ({
        url: `/routes/${id}/assign`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRouteAssignment>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `MANIFEST-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    startRoute: builder.mutation<ApiResponse<SchoolBusRoute>, number>({
      query: (id) => ({ url: `/routes/${id}/start`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRoute>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `MANIFEST-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
        { type: 'schoolBus/TripHistory', id: 'LIST' },
      ],
    }),
    completeRoute: builder.mutation<ApiResponse<SchoolBusRoute>, number>({
      query: (id) => ({ url: `/routes/${id}/complete`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRoute>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `MANIFEST-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
        { type: 'schoolBus/TripHistory', id: 'LIST' },
      ],
    }),

    getAttendance: builder.query<
      ApiResponse<PagedResponse<SchoolBusAttendance>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/attendance', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusAttendance>>(),
      providesTags: [{ type: 'schoolBus/Attendance', id: 'LIST' }],
    }),
    checkInStudent: builder.mutation<
      ApiResponse<SchoolBusAttendance>,
      SchoolBusAttendanceActionRequest
    >({
      query: (body) => ({
        url: '/attendance/check-in',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance>(),
      invalidatesTags: (_result, _error, { routeId }) => [
        { type: 'schoolBus/Attendance', id: 'LIST' },
        { type: 'schoolBus/Route', id: `MANIFEST-${routeId}` },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    checkOutStudent: builder.mutation<
      ApiResponse<SchoolBusAttendance>,
      SchoolBusAttendanceActionRequest
    >({
      query: (body) => ({
        url: '/attendance/check-out',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance>(),
      invalidatesTags: (_result, _error, { routeId }) => [
        { type: 'schoolBus/Attendance', id: 'LIST' },
        { type: 'schoolBus/Route', id: `MANIFEST-${routeId}` },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    getTripHistory: builder.query<
      ApiResponse<PagedResponse<SchoolBusTripHistory>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/trip-history', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusTripHistory>>(),
      providesTags: [{ type: 'schoolBus/TripHistory', id: 'LIST' }],
    }),
  }),
});

export const {
  useLazySearchMapLocationsQuery,
  useLazyReverseMapLocationQuery,
  useGetSchoolBusSummaryQuery,
  useGetSchoolBusReportQuery,
  useGetBusTypesQuery,
  useGetSchoolsQuery,
  useGetSchoolByIdQuery,
  useCreateSchoolMutation,
  useUpdateSchoolMutation,
  useDeleteSchoolMutation,
  useGetParentsQuery,
  useGetParentByIdQuery,
  useCreateParentMutation,
  useUpdateParentMutation,
  useDeleteParentMutation,
  useGetStudentsQuery,
  useGetStudentByIdQuery,
  useCreateStudentMutation,
  useUpdateStudentMutation,
  useDeleteStudentMutation,
  useGetBusesQuery,
  useGetBusByIdQuery,
  useCreateBusMutation,
  useUpdateBusMutation,
  useDeleteBusMutation,
  useGetDriversQuery,
  useGetDriverByIdQuery,
  useCreateDriverMutation,
  useUpdateDriverMutation,
  useDeleteDriverMutation,
  useGetAttendantsQuery,
  useGetAttendantByIdQuery,
  useCreateAttendantMutation,
  useUpdateAttendantMutation,
  useDeleteAttendantMutation,
  useGetPickupPointsQuery,
  useGetPickupPointByIdQuery,
  useCreatePickupPointMutation,
  useUpdatePickupPointMutation,
  useDeletePickupPointMutation,
  useGetDepotsQuery,
  useGetDepotByIdQuery,
  useCreateDepotMutation,
  useUpdateDepotMutation,
  useDeleteDepotMutation,
  useGetTransportRequestsQuery,
  useGetTransportRequestByIdQuery,
  useCreateTransportRequestMutation,
  useUpdateTransportRequestMutation,
  useApproveTransportRequestMutation,
  useRejectTransportRequestMutation,
  useGetRoutesQuery,
  useGetRouteByIdQuery,
  useGetRouteAttendanceManifestQuery,
  useCreateRouteMutation,
  useUpdateRouteMutation,
  useGenerateGreedyPlanMutation,
  useAssignRouteMutation,
  useStartRouteMutation,
  useCompleteRouteMutation,
  useGetAttendanceQuery,
  useCheckInStudentMutation,
  useCheckOutStudentMutation,
  useGetTripHistoryQuery,
} = schoolBusApi;
