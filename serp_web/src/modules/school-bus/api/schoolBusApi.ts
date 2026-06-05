import { api } from '@/lib/store/api';
import { createApiResponseTransform } from '@/lib/store/api/utils';
import type {
  ApiResponse,
  DashboardSummary,
  OperationalReport,
  PagedResponse,
  SchoolBusCapacityUtilization,
  SchoolBusDemoEvent,
  SchoolBusDemoSession,
  CreateDemoSessionRequest,
  SchoolBusAttendance,
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
  SchoolBusManualDispatchRequest,
  SchoolBusRoute,
  SchoolBusRouteAssignment,
  SchoolBusAssignmentHistory,
  SchoolBusRouteAssignmentRequest,
  SchoolBusRouteDetail,
  SchoolBusRoutePath,
  SchoolBusRouteStop,
  SchoolBusRouteUpsertRequest,
  SchoolBusSchool,
  SchoolBusSchoolUpsertRequest,
  SchoolBusStudent,
  SchoolBusStudentUpsertRequest,
  SchoolBusSubscription,
  SchoolBusSubscriptionUpsertRequest,
  SchoolBusTransportRequest,
  SchoolBusTransportRequestDetail,
  SchoolBusTransportRequestHistory,
  SchoolBusTransportRequestUpsertRequest,
  SchoolBusSubscriptionHistory,
  SchoolBusSubscriptionPausePeriod,
  SchoolBusTripAttendanceActionRequest,
  SchoolBusTripAttendanceManifest,
  SchoolBusTripAttendanceSummary,
  SchoolBusTripExecution,
  SchoolBusTripHistory,
  SchoolBusTripStopLog,
  SchoolBusTripStudent,
  SchoolBusSchedule,
  SchoolBusScheduleUpsertRequest,
  SchoolBusSchoolPickupPoint,
  SchoolBusSchoolPickupPointUpsertRequest,
  SchoolBusSchoolPickupPointWindow,
  SchoolPickupPointWindowUpsertRequest,
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
    getSchoolBusReport: builder.query<
      ApiResponse<OperationalReport>,
      SchoolBusListParams | void
    >({
      query: (params) => ({
        url: '/dashboard/reports/operations-summary',
        method: 'GET',
        params: params || undefined,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<OperationalReport>(),
      providesTags: [{ type: 'schoolBus/Report', id: 'SUMMARY' }],
    }),
    getSchoolBusReportTrips: builder.query<
      ApiResponse<PagedResponse<SchoolBusTripExecution>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/dashboard/reports/trips', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusTripExecution>>(),
      providesTags: [{ type: 'schoolBus/Report', id: 'TRIPS' }],
    }),
    getSchoolBusReportAttendance: builder.query<
      ApiResponse<PagedResponse<SchoolBusAttendance>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/dashboard/reports/attendance', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusAttendance>>(),
      providesTags: [{ type: 'schoolBus/Report', id: 'ATTENDANCE' }],
    }),
    getSchoolBusReportCapacity: builder.query<
      ApiResponse<PagedResponse<SchoolBusCapacityUtilization>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/dashboard/reports/capacity-utilization', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusCapacityUtilization>>(),
      providesTags: [{ type: 'schoolBus/Report', id: 'CAPACITY' }],
    }),
    getBusTypes: builder.query<ApiResponse<SchoolBusBusType[]>, void>({
      query: () => ({ url: '/master-data/bus-types', method: 'GET' }),
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
    cancelTransportRequest: builder.mutation<
      ApiResponse<SchoolBusTransportRequest>,
      number
    >({
      query: (id) => ({
        url: `/transport-requests/${id}/cancel`,
        method: 'POST',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTransportRequest>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/TransportRequest', id },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    getTransportRequestHistory: builder.query<
      ApiResponse<SchoolBusTransportRequestHistory[]>,
      number
    >({
      query: (id) => ({
        url: `/transport-requests/${id}/history`,
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<SchoolBusTransportRequestHistory[]>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TransportRequest', id: `HISTORY-${id}` },
      ],
    }),

    getSubscriptions: builder.query<
      ApiResponse<PagedResponse<SchoolBusSubscription>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/subscriptions', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusSubscription>>(),
      providesTags: [{ type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' }],
    }),
    getSubscriptionById: builder.query<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      providesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: `SUB-${id}` },
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
      ],
    }),
    createSubscription: builder.mutation<
      ApiResponse<SchoolBusSubscription>,
      SchoolBusSubscriptionUpsertRequest
    >({
      query: (body) => ({ url: '/subscriptions', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      invalidatesTags: [
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    activateSubscription: builder.mutation<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}/activate`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      invalidatesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/TransportRequest', id: `SUB-HISTORY-${id}` },
      ],
    }),
    pauseSubscription: builder.mutation<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}/pause`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      invalidatesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/TransportRequest', id: `SUB-HISTORY-${id}` },
      ],
    }),
    stopSubscription: builder.mutation<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}/stop`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      invalidatesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/TransportRequest', id: `SUB-HISTORY-${id}` },
      ],
    }),
    getSubscriptionHistory: builder.query<
      ApiResponse<SchoolBusSubscriptionHistory[]>,
      number
    >({
      query: (id) => ({
        url: `/subscriptions/${id}/history`,
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<SchoolBusSubscriptionHistory[]>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TransportRequest', id: `SUB-HISTORY-${id}` },
      ],
    }),
    getSubscriptionPausePeriods: builder.query<
      ApiResponse<SchoolBusSubscriptionPausePeriod[]>,
      number
    >({
      query: (id) => ({
        url: `/subscriptions/${id}/pause-periods`,
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<SchoolBusSubscriptionPausePeriod[]>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TransportRequest', id: `SUB-PAUSE-${id}` },
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
    getRoutePath: builder.query<ApiResponse<SchoolBusRoutePath>, number>({
      query: (id) => ({
        url: `/routes/${id}/path`,
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRoutePath>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id: `PATH-${id}` },
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
      ],
    }),
    getAssignmentHistory: builder.query<
      ApiResponse<SchoolBusAssignmentHistory[]>,
      number
    >({
      query: (id) => ({
        url: `/routes/${id}/assignment-history`,
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAssignmentHistory[]>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id: `ASSIGNMENT-HISTORY-${id}` },
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
        { type: 'schoolBus/Route', id: `ASSIGNMENT-HISTORY-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    manualDispatchRoute: builder.mutation<
      ApiResponse<SchoolBusRouteAssignment>,
      { id: number; body: SchoolBusManualDispatchRequest }
    >({
      query: ({ id, body }) => ({
        url: `/routes/${id}/manual-dispatch`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRouteAssignment>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `ASSIGNMENT-HISTORY-${id}` },
        { type: 'schoolBus/Route', id: `PATH-${id}` },
      ],
    }),
    reorderRouteStops: builder.mutation<
      ApiResponse<SchoolBusRouteStop[]>,
      { id: number; orderedStopIds: number[] }
    >({
      query: ({ id, orderedStopIds }) => ({
        url: `/routes/${id}/stops/reorder`,
        method: 'PATCH',
        body: { orderedStopIds },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRouteStop[]>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `PATH-${id}` },
      ],
    }),
    addRouteStop: builder.mutation<
      ApiResponse<SchoolBusRouteStop>,
      { id: number; body: { pickupPointId: number; stopType?: string; estimatedStudentCount?: number } }
    >({
      query: ({ id, body }) => ({
        url: `/routes/${id}/stops`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRouteStop>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `PATH-${id}` },
      ],
    }),
    removeRouteStop: builder.mutation<
      ApiResponse<void>,
      { routeId: number; stopId: number }
    >({
      query: ({ routeId, stopId }) => ({
        url: `/routes/${routeId}/stops/${stopId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, { routeId }) => [
        { type: 'schoolBus/Route', id: routeId },
        { type: 'schoolBus/Route', id: `DETAIL-${routeId}` },
        { type: 'schoolBus/Route', id: `PATH-${routeId}` },
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: 'ACTIVE_SESSION' },
      ],
    }),
    moveRouteStudent: builder.mutation<
      ApiResponse<void>,
      { routeId: number; body: { studentId: number; subscriptionId: number; targetRouteId: number } }
    >({
      query: ({ routeId, body }) => ({
        url: `/routes/${routeId}/students/move`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, { routeId, body }) => [
        { type: 'schoolBus/Route', id: routeId },
        { type: 'schoolBus/Route', id: `DETAIL-${routeId}` },
        { type: 'schoolBus/Route', id: body.targetRouteId },
        { type: 'schoolBus/Route', id: `DETAIL-${body.targetRouteId}` },
      ],
    }),
    removeRouteStudent: builder.mutation<
      ApiResponse<void>,
      { routeId: number; studentId: number; subscriptionId: number }
    >({
      query: ({ routeId, studentId, subscriptionId }) => ({
        url: `/routes/${routeId}/students/${studentId}?subscriptionId=${subscriptionId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, { routeId }) => [
        { type: 'schoolBus/Route', id: routeId },
        { type: 'schoolBus/Route', id: `DETAIL-${routeId}` },
        { type: 'schoolBus/Route', id: `PATH-${routeId}` },
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: 'ACTIVE_SESSION' },
      ],
    }),
    computeRoutePath: builder.mutation<ApiResponse<SchoolBusRoutePath>, number>({
      query: (id) => ({ url: `/routes/${id}/compute-path`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRoutePath>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `MANIFEST-${id}` },
        { type: 'schoolBus/Route', id: `PATH-${id}` },
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
    createTripFromRoute: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      number
    >({
      query: (routePlanId) => ({
        url: `/trips/from-route/${routePlanId}`,
        method: 'POST',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (_result, _error, routePlanId) => [
        { type: 'schoolBus/TripHistory', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id: routePlanId },
        { type: 'schoolBus/Route', id: `DETAIL-${routePlanId}` },
      ],
    }),
    getTrips: builder.query<
      ApiResponse<PagedResponse<SchoolBusTripExecution>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/trips', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusTripExecution>>(),
      providesTags: [{ type: 'schoolBus/TripHistory', id: 'TRIPS' }],
    }),
    getTripById: builder.query<ApiResponse<SchoolBusTripExecution>, number>({
      query: (id) => ({ url: `/trips/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
      ],
    }),
    startTrip: builder.mutation<ApiResponse<SchoolBusTripExecution>, number>({
      query: (id) => ({ url: `/trips/${id}/start`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (_result, _error, id) => [
        { type: 'schoolBus/TripHistory', id: 'TRIPS' },
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
      ],
    }),
    arriveTripStop: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { tripId: number; routeStopId: number }
    >({
      query: ({ tripId, routeStopId }) => ({
        url: `/trips/${tripId}/arrive-stop/${routeStopId}`,
        method: 'POST',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
      ],
    }),
    departTripStop: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { tripId: number; routeStopId: number }
    >({
      query: ({ tripId, routeStopId }) => ({
        url: `/trips/${tripId}/depart-stop/${routeStopId}`,
        method: 'POST',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
      ],
    }),
    completeTrip: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { id: number; body?: { note?: string } }
    >({
      query: ({ id, body }) => ({ url: `/trips/${id}/complete`, method: 'POST', body: body ?? {} }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/TripHistory', id: 'TRIPS' },
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    cancelTrip: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { id: number; body: { reason: string } }
    >({
      query: ({ id, body }) => ({ url: `/trips/${id}/cancel`, method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/TripHistory', id: 'TRIPS' },
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
      ],
    }),
    skipTripStop: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { tripId: number; routeStopId: number; reason: string }
    >({
      query: ({ tripId, routeStopId, reason }) => ({
        url: `/trips/${tripId}/skip-stop/${routeStopId}`,
        method: 'POST',
        body: { reason },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
        { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
      ],
    }),
    getTripStops: builder.query<ApiResponse<SchoolBusTripStopLog[]>, number>({
      query: (id) => ({ url: `/trips/${id}/stops`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripStopLog[]>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
      ],
    }),
    getTripStudents: builder.query<ApiResponse<SchoolBusTripStudent[]>, number>({
      query: (id) => ({ url: `/trips/${id}/students`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripStudent[]>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
      ],
    }),
    getTripAttendance: builder.query<ApiResponse<SchoolBusAttendance[]>, number>({
      query: (id) => ({ url: `/trips/${id}/attendance`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance[]>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Attendance', id: `TRIP-${id}` },
      ],
    }),
    getTripAttendanceManifest: builder.query<ApiResponse<SchoolBusTripAttendanceManifest>, number>({
      query: (id) => ({ url: `/trips/${id}/attendance/manifest`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripAttendanceManifest>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
      ],
    }),
    getTripAttendanceSummary: builder.query<ApiResponse<SchoolBusTripAttendanceSummary>, number>({
      query: (id) => ({ url: `/trips/${id}/attendance/summary`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripAttendanceSummary>(),
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
      ],
    }),
    boardTripStudent: builder.mutation<
      ApiResponse<SchoolBusAttendance>,
      { tripId: number; body: SchoolBusTripAttendanceActionRequest }
    >({
      query: ({ tripId, body }) => ({
        url: `/trips/${tripId}/attendance/board`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance>(),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
        { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
      ],
    }),
    dropoffTripStudent: builder.mutation<
      ApiResponse<SchoolBusAttendance>,
      { tripId: number; body: SchoolBusTripAttendanceActionRequest }
    >({
      query: ({ tripId, body }) => ({
        url: `/trips/${tripId}/attendance/dropoff`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance>(),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
        { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
      ],
    }),
    absentTripStudent: builder.mutation<
      ApiResponse<SchoolBusAttendance>,
      { tripId: number; body: SchoolBusTripAttendanceActionRequest }
    >({
      query: ({ tripId, body }) => ({
        url: `/trips/${tripId}/attendance/absent`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance>(),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
        { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
      ],
    }),
    noShowTripStudent: builder.mutation<
      ApiResponse<SchoolBusAttendance>,
      { tripId: number; body: SchoolBusTripAttendanceActionRequest }
    >({
      query: ({ tripId, body }) => ({
        url: `/trips/${tripId}/attendance/no-show`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance>(),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
        { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
      ],
    }),

    // ===== Demo Sessions (session-centric API) =====
    createDemoSession: builder.mutation<
      ApiResponse<SchoolBusDemoSession>,
      { tripId: number; body?: CreateDemoSessionRequest }
    >({
      query: ({ tripId, body }) => ({
        url: `/demo-sessions/from-trip/${tripId}`,
        method: 'POST',
        body: body ?? {},
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    getDemoSession: builder.query<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      providesTags: (_result, _error, sessionId) => [
        { type: 'schoolBus/TripHistory', id: `DEMO-${sessionId}` },
      ],
    }),
    getDemoSessionByTrip: builder.query<ApiResponse<SchoolBusDemoSession>, number>({
      query: (tripId) => ({ url: `/demo-sessions/by-trip/${tripId}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      providesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    startDemoSession: builder.mutation<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/start`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    pauseDemoSession: builder.mutation<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/pause`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    resumeDemoSession: builder.mutation<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/resume`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    tickDemoSession: builder.mutation<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/tick`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    stopDemoSession: builder.mutation<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/stop`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    jumpDemoSessionToStop: builder.mutation<ApiResponse<SchoolBusDemoSession>, { sessionId: number; stopOrder: number }>({
      query: ({ sessionId, stopOrder }) => ({ url: `/demo-sessions/${sessionId}/jump-to-stop/${stopOrder}`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    jumpDemoSessionToProgress: builder.mutation<ApiResponse<SchoolBusDemoSession>, { sessionId: number; progressPercent: number }>({
      query: ({ sessionId, progressPercent }) => ({ url: `/demo-sessions/${sessionId}/jump-to-progress`, method: 'POST', body: { progressPercent } }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    jumpDemoSessionToStart: builder.mutation<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/jump-to-start`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    jumpDemoSessionToEnd: builder.mutation<ApiResponse<SchoolBusDemoSession>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/jump-to-end`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    updateDemoAutomationSettings: builder.mutation<ApiResponse<SchoolBusDemoSession>, { sessionId: number; autoAdvanceStops?: boolean; autoAttendance?: boolean }>({
      query: ({ sessionId, ...body }) => ({ url: `/demo-sessions/${sessionId}/automation-settings`, method: 'PATCH', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoSession>(),
      invalidatesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),
    getDemoSessionEvents: builder.query<ApiResponse<SchoolBusDemoEvent[]>, number>({
      query: (sessionId) => ({ url: `/demo-sessions/${sessionId}/events`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDemoEvent[]>(),
      providesTags: [{ type: 'schoolBus/TripHistory', id: 'DEMO' }],
    }),

    getTripHistory: builder.query<
      ApiResponse<PagedResponse<SchoolBusTripHistory>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/attendance/trip-history', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusTripHistory>>(),
      providesTags: [{ type: 'schoolBus/TripHistory', id: 'LIST' }],
    }),

    // ===== School Schedule =====
    getSchoolSchedules: builder.query<
      ApiResponse<PagedResponse<SchoolBusSchedule>>,
      { schoolId: number; page?: number; size?: number }
    >({
      query: ({ schoolId, page = 0, size = 20 }) => ({
        url: '/school-schedules/by-school',
        method: 'GET',
        params: { schoolId, page, size },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusSchedule>>(),
      providesTags: [{ type: 'schoolBus/School', id: 'SCHEDULE_LIST' }],
    }),
    getActiveSchoolSchedules: builder.query<
      ApiResponse<SchoolBusSchedule[]>,
      number
    >({
      query: (schoolId) => ({
        url: '/school-schedules/by-school/active',
        method: 'GET',
        params: { schoolId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchedule[]>(),
      providesTags: [{ type: 'schoolBus/School', id: 'SCHEDULE_ACTIVE' }],
    }),
    getSchoolScheduleById: builder.query<
      ApiResponse<SchoolBusSchedule>,
      number
    >({
      query: (id) => ({
        url: '/school-schedules',
        method: 'GET',
        params: { id },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchedule>(),
      providesTags: (_result, _error, id) => [{ type: 'schoolBus/School', id: `SCHEDULE-${id}` }],
    }),
    createSchoolSchedule: builder.mutation<
      ApiResponse<SchoolBusSchedule>,
      { schoolId: number; body: SchoolBusScheduleUpsertRequest }
    >({
      query: ({ schoolId, body }) => ({
        url: '/school-schedules',
        method: 'POST',
        params: { schoolId },
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchedule>(),
      invalidatesTags: [{ type: 'schoolBus/School', id: 'SCHEDULE_LIST' }, { type: 'schoolBus/School', id: 'SCHEDULE_ACTIVE' }],
    }),
    updateSchoolSchedule: builder.mutation<
      ApiResponse<SchoolBusSchedule>,
      { id: number; body: SchoolBusScheduleUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: '/school-schedules',
        method: 'PATCH',
        params: { id },
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchedule>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'schoolBus/School', id: 'SCHEDULE_LIST' },
        { type: 'schoolBus/School', id: 'SCHEDULE_ACTIVE' },
        { type: 'schoolBus/School', id: `SCHEDULE-${id}` },
      ],
    }),
    deleteSchoolSchedule: builder.mutation<ApiResponse<void>, number>({
      query: (id) => ({
        url: '/school-schedules',
        method: 'DELETE',
        params: { id },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: [{ type: 'schoolBus/School', id: 'SCHEDULE_LIST' }, { type: 'schoolBus/School', id: 'SCHEDULE_ACTIVE' }],
    }),

    // ===== School Pickup Points =====
    getSchoolPickupPoints: builder.query<
      ApiResponse<PagedResponse<SchoolBusSchoolPickupPoint>>,
      { schoolId: number; page?: number; size?: number }
    >({
      query: ({ schoolId, page = 0, size = 20 }) => ({
        url: '/school-pickup-points/by-school',
        method: 'GET',
        params: { schoolId, page, size },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<PagedResponse<SchoolBusSchoolPickupPoint>>(),
      providesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_LIST' }],
    }),
    getActiveSchoolPickupPoints: builder.query<
      ApiResponse<SchoolBusSchoolPickupPoint[]>,
      number
    >({
      query: (schoolId) => ({
        url: '/school-pickup-points/by-school/active',
        method: 'GET',
        params: { schoolId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPoint[]>(),
      providesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_ACTIVE' }],
    }),
    getSchoolPickupPointsCompatibility: builder.query<
      ApiResponse<Array<{
        pickupPointId: number;
        pickupPointCode: string | null;
        pickupPointName: string | null;
        usageType: string | null;
        hasCoordinates: boolean | null;
        // Stable enum code for logic — READY | MISSING_PICKUP_WINDOW | MISSING_COORDINATES | UNSUPPORTED_USAGE_TYPE | NOT_CHECKED
        pickupReadinessCode: string;
        // Human-readable label — display directly in UI
        pickupReadinessLabel: string;
        // Legacy — kept for backward compat
        pickupReadinessStatus: string;
        pickupMissingConfigReason: string | null;
        pickupWindowStart: string | null;
        pickupWindowEnd: string | null;
        compatibleForPickup: boolean | null;
        // Stable enum code
        dropoffReadinessCode: string;
        // Human-readable label
        dropoffReadinessLabel: string;
        // Legacy
        dropoffReadinessStatus: string;
        dropoffMissingConfigReason: string | null;
        dropoffWindowStart: string | null;
        dropoffWindowEnd: string | null;
        compatibleForDropoff: boolean | null;
      }>>,
      { schoolId: number; schoolScheduleId: number }
    >({
      query: ({ schoolId, schoolScheduleId }) => ({
        url: '/school-pickup-points/compatibility',
        method: 'GET',
        params: { schoolId, schoolScheduleId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<Array<{
        pickupPointId: number;
        pickupPointCode: string | null;
        pickupPointName: string | null;
        usageType: string | null;
        hasCoordinates: boolean | null;
        pickupReadinessCode: string;
        pickupReadinessLabel: string;
        pickupReadinessStatus: string;
        pickupMissingConfigReason: string | null;
        pickupWindowStart: string | null;
        pickupWindowEnd: string | null;
        compatibleForPickup: boolean | null;
        dropoffReadinessCode: string;
        dropoffReadinessLabel: string;
        dropoffReadinessStatus: string;
        dropoffMissingConfigReason: string | null;
        dropoffWindowStart: string | null;
        dropoffWindowEnd: string | null;
        compatibleForDropoff: boolean | null;
      }>>(),
      providesTags: (_result, _error, { schoolId, schoolScheduleId }) => [
        { type: 'schoolBus/PickupPoint', id: `COMPATIBILITY-${schoolId}-${schoolScheduleId}` }
      ],
    }),

    getAllActiveSchoolPickupLinks: builder.query<
      ApiResponse<SchoolBusSchoolPickupPoint[]>,
      void
    >({
      query: () => ({
        url: '/school-pickup-points/active',
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPoint[]>(),
      providesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_ALL_ACTIVE' }],
    }),
    linkSchoolPickupPoint: builder.mutation<
      ApiResponse<SchoolBusSchoolPickupPoint>,
      { schoolId: number; body: SchoolBusSchoolPickupPointUpsertRequest }
    >({
      query: ({ schoolId, body }) => ({
        url: '/school-pickup-points',
        method: 'POST',
        params: { schoolId },
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPoint>(),
      invalidatesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_LIST' }, { type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_ACTIVE' }],
    }),
    updateSchoolPickupPoint: builder.mutation<
      ApiResponse<SchoolBusSchoolPickupPoint>,
      { id: number; body: SchoolBusSchoolPickupPointUpsertRequest }
    >({
      query: ({ id, body }) => ({
        url: '/school-pickup-points',
        method: 'PATCH',
        params: { id },
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPoint>(),
      invalidatesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_LIST' }, { type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_ACTIVE' }],
    }),
    unlinkSchoolPickupPoint: builder.mutation<
      ApiResponse<void>,
      { schoolId: number; pickupPointId: number }
    >({
      query: ({ schoolId, pickupPointId }) => ({
        url: '/school-pickup-points',
        method: 'DELETE',
        params: { schoolId, pickupPointId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_LIST' }, { type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_ACTIVE' }],
    }),

    // ===== School Pickup Point Windows =====
    getSchoolPickupPointWindows: builder.query<
      ApiResponse<SchoolBusSchoolPickupPointWindow[]>,
      number
    >({
      query: (sppId) => ({
        url: '/school-pickup-point-windows',
        method: 'GET',
        params: { schoolPickupPointId: sppId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPointWindow[]>(),
      providesTags: (_r, _e, sppId) => [{ type: 'schoolBus/PickupPoint', id: `WINDOW_${sppId}` }],
    }),
    getScheduleWindows: builder.query<
      ApiResponse<SchoolBusSchoolPickupPointWindow[]>,
      number
    >({
      query: (scheduleId) => ({
        url: '/school-pickup-point-windows/by-schedule',
        method: 'GET',
        params: { schoolScheduleId: scheduleId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPointWindow[]>(),
      providesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHEDULE_WINDOWS' }],
    }),
    createSchoolPickupPointWindow: builder.mutation<
      ApiResponse<SchoolBusSchoolPickupPointWindow>,
      SchoolPickupPointWindowUpsertRequest
    >({
      query: (body) => ({
        url: '/school-pickup-point-windows',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPointWindow>(),
      invalidatesTags: (_r, _e, body) => [
        { type: 'schoolBus/PickupPoint', id: `WINDOW_${body.schoolPickupPointId}` },
        { type: 'schoolBus/PickupPoint', id: 'SCHEDULE_WINDOWS' },
      ],
    }),
    updateSchoolPickupPointWindow: builder.mutation<
      ApiResponse<SchoolBusSchoolPickupPointWindow>,
      { windowId: number; body: SchoolPickupPointWindowUpsertRequest }
    >({
      query: ({ windowId, body }) => ({
        url: '/school-pickup-point-windows',
        method: 'PUT',
        params: { id: windowId },
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSchoolPickupPointWindow>(),
      invalidatesTags: (_r, _e, { body }) => [
        { type: 'schoolBus/PickupPoint', id: `WINDOW_${body.schoolPickupPointId}` },
        { type: 'schoolBus/PickupPoint', id: 'SCHEDULE_WINDOWS' },
      ],
    }),
    deleteSchoolPickupPointWindow: builder.mutation<
      ApiResponse<void>,
      number
    >({
      query: (windowId) => ({
        url: '/school-pickup-point-windows',
        method: 'DELETE',
        params: { id: windowId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: [{ type: 'schoolBus/PickupPoint', id: 'SCHOOL_LINK_LIST' }, { type: 'schoolBus/PickupPoint', id: 'SCHEDULE_WINDOWS' }],
    }),

    // ── Planning Sessions ──────────────────────────────────────────────────
    previewPlanningDemand: builder.mutation<
      import('../types').ApiResponse<import('../types').SchoolBusPlanningPreview>,
      import('../types').PlanningSessionPreviewRequest
    >({
      query: (body) => ({ url: '/route-planning-sessions/preview', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
    }),
    createPlanningSession: builder.mutation<
      import('../types').ApiResponse<import('../types').SchoolBusPlanningSession>,
      import('../types').PlanningSessionCreateRequest
    >({
      query: (body) => ({ url: '/route-planning-sessions', method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: [{ type: 'schoolBus/Route', id: 'SESSION_LIST' }],
    }),
    getPlanningSessionsQuery: builder.query<
      import('../types').ApiResponse<import('../types').SchoolBusPlanningSession[]>,
      void
    >({
      query: () => ({ url: '/route-planning-sessions', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: (result) => {
        const sessions = result?.data ?? [];
        return [
          { type: 'schoolBus/Route', id: 'SESSION_LIST' },
          ...sessions.map(s => ({ type: 'schoolBus/Route' as const, id: `SESSION-${s.id}` })),
        ];
      },
    }),
    getPlanningSession: builder.query<
      import('../types').ApiResponse<import('../types').SchoolBusPlanningSession>,
      number
    >({
      query: (id) => ({ url: `/route-planning-sessions/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: (_r, _e, id) => [{ type: 'schoolBus/Route', id: `SESSION-${id}` }],
    }),
    generateGreedyForSession: builder.mutation<
      import('../types').ApiResponse<import('../types').SchoolBusGreedyGenerateResult>,
      { id: number; body?: import('../types').GreedyGenerateRequest }
    >({
      query: ({ id, body }) => ({ url: `/route-planning-sessions/${id}/generate-greedy`, method: 'POST', body: body ?? {} }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: (_r, _e, { id }) => [
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: `SESSION-${id}` },
        { type: 'schoolBus/Route', id: 'LIST' },
      ],
    }),
    publishPlanningSession: builder.mutation<
      import('../types').ApiResponse<import('../types').SchoolBusPlanningSession>,
      number
    >({
      query: (id) => ({ url: `/route-planning-sessions/${id}/publish`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: (_r, _e, id) => [
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: `SESSION-${id}` },
        { type: 'schoolBus/Route', id: 'LIST' },
      ],
    }),
    cancelPlanningSession: builder.mutation<
      import('../types').ApiResponse<import('../types').SchoolBusPlanningSession>,
      number
    >({
      query: (id) => ({ url: `/route-planning-sessions/${id}/cancel`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: (_r, _e, id) => [
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: `SESSION-${id}` },
        { type: 'schoolBus/Route', id: 'LIST' },
      ],
    }),
    getSessionRoutes: builder.query<
      import('../types').ApiResponse<import('../types').SchoolBusRoute[]>,
      number
    >({
      query: (id) => ({ url: `/route-planning-sessions/${id}/routes`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: (_r, _e, id) => [{ type: 'schoolBus/Route', id: `SESSION-ROUTES-${id}` }],
    }),
    createRouteInSession: builder.mutation<
      import('../types').ApiResponse<import('../types').SchoolBusRoute>,
      { sessionId: number; body: import('../types').CreateRouteInSessionRequest }
    >({
      query: ({ sessionId, body }) => ({
        url: `/route-planning-sessions/${sessionId}/routes`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: (_r, _e, { sessionId }) => [
        { type: 'schoolBus/Route', id: `SESSION-ROUTES-${sessionId}` },
        { type: 'schoolBus/Route', id: 'LIST' },
      ],
    }),
    getSessionEligibleStudents: builder.query<
      import('../types').ApiResponse<import('../types').SchoolBusEligibleStudent[]>,
      number
    >({
      query: (id) => ({ url: `/route-planning-sessions/${id}/eligible-students`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: (_r, _e, id) => [{ type: 'schoolBus/Route', id: `SESSION-ELIGIBLE-${id}` }],
    }),
    assignStudentToRoute: builder.mutation<
      import('../types').ApiResponse<import('../types').SchoolBusRoutePlanStudent>,
      { routeId: number; sessionId: number; body: import('../types').AddStudentToStopRequest }
    >({
      query: ({ routeId, body }) => ({
        url: `/routes/${routeId}/students/assign`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      // Invalidate route detail + session (with ID) so Active Session counters refresh in real-time
      invalidatesTags: (_result, _error, { routeId, sessionId }) => [
        { type: 'schoolBus/Route', id: routeId },
        { type: 'schoolBus/Route', id: `DETAIL-${routeId}` },
        { type: 'schoolBus/Route', id: `PATH-${routeId}` },
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: `SESSION-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ELIGIBLE-${sessionId}` },
        { type: 'schoolBus/Route', id: 'ACTIVE_SESSION' },
      ],
    }),
  }),
});

export const {
  useLazySearchMapLocationsQuery,
  useLazyReverseMapLocationQuery,
  useGetSchoolBusSummaryQuery,
  useGetSchoolBusReportQuery,
  useGetSchoolBusReportTripsQuery,
  useGetSchoolBusReportAttendanceQuery,
  useGetSchoolBusReportCapacityQuery,
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
  useCancelTransportRequestMutation,
  useGetTransportRequestHistoryQuery,
  useGetSubscriptionsQuery,
  useGetSubscriptionByIdQuery,
  useCreateSubscriptionMutation,
  useActivateSubscriptionMutation,
  usePauseSubscriptionMutation,
  useStopSubscriptionMutation,
  useGetSubscriptionHistoryQuery,
  useGetSubscriptionPausePeriodsQuery,
  useGetRoutesQuery,
  useGetRouteByIdQuery,
  useGetRoutePathQuery,
  useUpdateRouteMutation,
  // useGenerateGreedyPlanMutation removed — replaced by useGenerateGreedyForSessionMutation
  useGetAssignmentHistoryQuery,
  useAssignRouteMutation,
  useManualDispatchRouteMutation,
  useReorderRouteStopsMutation,
  useAddRouteStopMutation,
  useRemoveRouteStopMutation,
  useMoveRouteStudentMutation,
  useRemoveRouteStudentMutation,
  useComputeRoutePathMutation,
  useCreateTripFromRouteMutation,
  useGetTripsQuery,
  useGetTripByIdQuery,
  useStartTripMutation,
  useArriveTripStopMutation,
  useDepartTripStopMutation,
  useSkipTripStopMutation,
  useCompleteTripMutation,
  useCancelTripMutation,
  useGetTripStopsQuery,
  useGetTripStudentsQuery,
  useGetTripAttendanceQuery,
  useGetTripAttendanceManifestQuery,
  useGetTripAttendanceSummaryQuery,
  useBoardTripStudentMutation,
  useDropoffTripStudentMutation,
  useAbsentTripStudentMutation,
  useNoShowTripStudentMutation,
  useCreateDemoSessionMutation,
  useGetDemoSessionQuery,
  useGetDemoSessionByTripQuery,
  useStartDemoSessionMutation,
  usePauseDemoSessionMutation,
  useResumeDemoSessionMutation,
  useTickDemoSessionMutation,
  useStopDemoSessionMutation,
  useJumpDemoSessionToStopMutation,
  useJumpDemoSessionToProgressMutation,
  useJumpDemoSessionToStartMutation,
  useJumpDemoSessionToEndMutation,
  useUpdateDemoAutomationSettingsMutation,
  useGetDemoSessionEventsQuery,
  useGetAttendanceQuery,
  useGetTripHistoryQuery,
  // School Schedule
  useGetSchoolSchedulesQuery,
  useGetActiveSchoolSchedulesQuery,
  useGetSchoolScheduleByIdQuery,
  useCreateSchoolScheduleMutation,
  useUpdateSchoolScheduleMutation,
  useDeleteSchoolScheduleMutation,
  // School Pickup Point
  useGetSchoolPickupPointsQuery,
  useGetActiveSchoolPickupPointsQuery,
  useGetSchoolPickupPointsCompatibilityQuery,
  useGetAllActiveSchoolPickupLinksQuery,
  useLinkSchoolPickupPointMutation,
  useUpdateSchoolPickupPointMutation,
  useUnlinkSchoolPickupPointMutation,
  // School Pickup Point Windows
  useGetSchoolPickupPointWindowsQuery,
  useGetScheduleWindowsQuery,
  useCreateSchoolPickupPointWindowMutation,
  useUpdateSchoolPickupPointWindowMutation,
  useDeleteSchoolPickupPointWindowMutation,
  // Planning Sessions
  usePreviewPlanningDemandMutation,
  useCreatePlanningSessionMutation,
  useGetPlanningSessionsQueryQuery,
  useGetPlanningSessionQuery,
  useGenerateGreedyForSessionMutation,
  usePublishPlanningSessionMutation,
  useCancelPlanningSessionMutation,
  useGetSessionRoutesQuery,
  useCreateRouteInSessionMutation,
  useGetSessionEligibleStudentsQuery,
  useAssignStudentToRouteMutation,
} = schoolBusApi;
