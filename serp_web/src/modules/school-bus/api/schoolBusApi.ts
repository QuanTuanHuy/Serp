import { api } from '@/lib/store/api';
import { createApiResponseTransform } from '@/lib/store/api/utils';
import type { FetchBaseQueryMeta } from '@reduxjs/toolkit/query';
import type {
  ApiResponse,
  DashboardSummary,
  DashboardOperationsResponse,
  ChartItemDto,
  OperationalReport,

  PagedResponse,
  SchoolBusCapacityUtilization,
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
  SchoolBusTripAttendanceActionRequest,
  SchoolBusTripAttendanceManifest,
  SchoolBusTripAttendanceSummary,
  SchoolBusTripExecution,
  SchoolBusTripHistory,
  SchoolBusTripStopLog,
  SchoolBusTripStudent,
  SchoolBusSchoolPickupPoint,
  SchoolBusSchoolPickupPointUpsertRequest,
  SchoolBusDropdownOption,
} from '../types';

const transformApiResponse = createApiResponseTransform;

function transformPollingError(response: unknown, meta?: FetchBaseQueryMeta) {
  if (
    typeof response !== 'object' ||
    response === null ||
    !('status' in response) ||
    response.status !== 429
  ) {
    return response;
  }

  const retryAfterHeader = meta?.response?.headers.get('Retry-After');
  const retryAfterSeconds = retryAfterHeader ? Number.parseInt(retryAfterHeader, 10) : undefined;
  const errorData =
    'data' in response && typeof response.data === 'object' && response.data !== null
      ? response.data
      : {};

  return {
    ...response,
    data: {
      ...errorData,
      retryAfterSeconds:
        retryAfterSeconds && retryAfterSeconds > 0 ? retryAfterSeconds : undefined,
    },
  };
}

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
    getOperationsDashboard: builder.query<
      ApiResponse<DashboardOperationsResponse>,
      {
        serviceDate?: string;
        fromDate?: string;
        toDate?: string;
        schoolId?: number;
        direction?: string;
      } | void
    >({
      query: (params) => ({
        url: '/dashboard/operations',
        method: 'GET',
        params: params || undefined,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<DashboardOperationsResponse>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'OPERATIONS' }],
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

    getSchoolDropdownOptions: builder.query<ApiResponse<SchoolBusDropdownOption[]>, void>({
      query: () => ({ url: '/dropdowns/schools', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDropdownOption[]>(),
    }),
    getSchoolPickupPointDropdownOptions: builder.query<
      ApiResponse<SchoolBusDropdownOption[]>,
      number
    >({
      query: (schoolId) => ({
        url: '/dropdowns/school-pickup-points',
        method: 'GET',
        params: { schoolId },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDropdownOption[]>(),
    }),
    getParentDropdownOptions: builder.query<ApiResponse<SchoolBusDropdownOption[]>, void>({
      query: () => ({ url: '/dropdowns/parents', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDropdownOption[]>(),
    }),
    getDriverDropdownOptions: builder.query<ApiResponse<SchoolBusDropdownOption[]>, void>({
      query: () => ({ url: '/dropdowns/drivers', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDropdownOption[]>(),
    }),
    getAttendantDropdownOptions: builder.query<ApiResponse<SchoolBusDropdownOption[]>, void>({
      query: () => ({ url: '/dropdowns/attendants', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDropdownOption[]>(),
    }),
    getBusDropdownOptions: builder.query<
      ApiResponse<SchoolBusDropdownOption[]>,
      number | void
    >({
      query: (depotId) => ({
        url: '/dropdowns/buses',
        method: 'GET',
        params: depotId ? { depotId } : undefined,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDropdownOption[]>(),
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
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
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
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
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
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
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

    getSchoolBusSubscriptions: builder.query<
      ApiResponse<PagedResponse<SchoolBusSubscription>>,
      SchoolBusListParams | void
    >({
      query: (params) => listQuery('/subscriptions', params),
      extraOptions: { service: 'school-bus' },
      transformResponse:
        transformApiResponse<PagedResponse<SchoolBusSubscription>>(),
      providesTags: [{ type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' }],
    }),
    getSchoolBusSubscriptionById: builder.query<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      providesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: `SUB-${id}` },
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
      ],
    }),
    createSchoolBusSubscription: builder.mutation<
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
    activateSchoolBusSubscription: builder.mutation<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}/activate`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      invalidatesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/TransportRequest', id: `SUB-HISTORY-${id}` },
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    pauseSchoolBusSubscription: builder.mutation<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}/pause`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      invalidatesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/TransportRequest', id: `SUB-HISTORY-${id}` },
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    stopSchoolBusSubscription: builder.mutation<ApiResponse<SchoolBusSubscription>, number>({
      query: (id) => ({ url: `/subscriptions/${id}/stop`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusSubscription>(),
      invalidatesTags: (result, error, id) => [
        { type: 'schoolBus/TransportRequest', id: 'SUBSCRIPTIONS' },
        { type: 'schoolBus/TransportRequest', id: `SUB-HISTORY-${id}` },
        { type: 'schoolBus/TransportRequest', id: 'LIST' },
        { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
        { type: 'schoolBus/Report', id: 'SUMMARY' },
      ],
    }),
    getSchoolBusSubscriptionHistory: builder.query<
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
      { id: number; orderedStopIds: number[]; sessionId: number }
    >({
      query: ({ id, orderedStopIds }) => ({
        url: `/routes/${id}/stops/reorder`,
        method: 'PATCH',
        body: { orderedStopIds },
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusRouteStop[]>(),
      invalidatesTags: (_result, _error, { id, sessionId }) => [
        { type: 'schoolBus/Route', id },
        { type: 'schoolBus/Route', id: `DETAIL-${id}` },
        { type: 'schoolBus/Route', id: `PATH-${id}` },
        { type: 'schoolBus/Route', id: `SESSION-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ELIGIBLE-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ROUTES-${sessionId}` },
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
      { routeId: number; stopId: number; sessionId: number }
    >({
      query: ({ routeId, stopId }) => ({
        url: `/routes/${routeId}/stops/${stopId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, { routeId, sessionId }) => [
        { type: 'schoolBus/Route', id: routeId },
        { type: 'schoolBus/Route', id: `DETAIL-${routeId}` },
        { type: 'schoolBus/Route', id: `PATH-${routeId}` },
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: `SESSION-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ELIGIBLE-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ROUTES-${sessionId}` },
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
        { type: 'schoolBus/Route', id: `PATH-${routeId}` },
        { type: 'schoolBus/Route', id: body.targetRouteId },
        { type: 'schoolBus/Route', id: `DETAIL-${body.targetRouteId}` },
        { type: 'schoolBus/Route', id: `PATH-${body.targetRouteId}` },
      ],
    }),
    removeRouteStudent: builder.mutation<
      ApiResponse<void>,
      { routeId: number; studentId: number; subscriptionId: number; sessionId: number }
    >({
      query: ({ routeId, studentId, subscriptionId }) => ({
        url: `/routes/${routeId}/students/${studentId}?subscriptionId=${subscriptionId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<void>(),
      invalidatesTags: (_result, _error, { routeId, sessionId }) => [
        { type: 'schoolBus/Route', id: routeId },
        { type: 'schoolBus/Route', id: `DETAIL-${routeId}` },
        { type: 'schoolBus/Route', id: `PATH-${routeId}` },
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: `SESSION-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ELIGIBLE-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ROUTES-${sessionId}` },
        { type: 'schoolBus/Route', id: 'ACTIVE_SESSION' },
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
      invalidatesTags: (result, _error, id) =>
        result
          ? [
              { type: 'schoolBus/TripHistory', id: 'TRIPS' },
              { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
            ]
          : [],
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
      invalidatesTags: (result, _error, { tripId }) =>
        result ? [{ type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` }] : [],
    }),
    startBoardingTripStop: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { tripId: number; routeStopId: number }
    >({
      query: ({ tripId, routeStopId }) => ({
        url: `/trips/${tripId}/start-boarding/${routeStopId}`,
        method: 'POST',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (result, _error, { tripId }) =>
        result ? [{ type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` }] : [],
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
      invalidatesTags: (result, _error, { tripId }) =>
        result ? [{ type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` }] : [],
    }),
    completeTrip: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { id: number; body?: { note?: string } }
    >({
      query: ({ id, body }) => ({ url: `/trips/${id}/complete`, method: 'POST', body: body ?? {} }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (result, _error, { id }) =>
        result
          ? [
              { type: 'schoolBus/TripHistory', id: 'TRIPS' },
              { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
              { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
            ]
          : [],
    }),
    cancelTrip: builder.mutation<
      ApiResponse<SchoolBusTripExecution>,
      { id: number; body: { reason: string } }
    >({
      query: ({ id, body }) => ({ url: `/trips/${id}/cancel`, method: 'POST', body }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripExecution>(),
      invalidatesTags: (result, _error, { id }) =>
        result
          ? [
              { type: 'schoolBus/TripHistory', id: 'TRIPS' },
              { type: 'schoolBus/TripHistory', id: `TRIP-${id}` },
              { type: 'schoolBus/Dashboard', id: 'SUMMARY' },
            ]
          : [],
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
      invalidatesTags: (result, _error, { tripId }) =>
        result
          ? [
              { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
              { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
            ]
          : [],
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
      transformErrorResponse: transformPollingError,
      providesTags: (_result, _error, id) => [
        { type: 'schoolBus/Attendance', id: `TRIP-${id}` },
      ],
    }),
    getTripAttendanceManifest: builder.query<ApiResponse<SchoolBusTripAttendanceManifest>, number>({
      query: (id) => ({ url: `/trips/${id}/attendance/manifest`, method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusTripAttendanceManifest>(),
      transformErrorResponse: transformPollingError,
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
      invalidatesTags: (result, _error, { tripId }) =>
        result
          ? [
              { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
              { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
            ]
          : [],
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
      invalidatesTags: (result, _error, { tripId }) =>
        result
          ? [
              { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
              { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
            ]
          : [],
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
      invalidatesTags: (result, _error, { tripId }) =>
        result
          ? [
              { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
              { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
            ]
          : [],
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
      invalidatesTags: (result, _error, { tripId }) =>
        result
          ? [
              { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
              { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
            ]
          : [],
    }),
    notServedTripStudent: builder.mutation<
      ApiResponse<SchoolBusAttendance>,
      { tripId: number; body: SchoolBusTripAttendanceActionRequest }
    >({
      query: ({ tripId, body }) => ({
        url: `/trips/${tripId}/attendance/not-served`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusAttendance>(),
      invalidatesTags: (result, _error, { tripId }) =>
        result
          ? [
              { type: 'schoolBus/Attendance', id: `TRIP-${tripId}` },
              { type: 'schoolBus/TripHistory', id: `TRIP-${tripId}` },
            ]
          : [],
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
    deleteRouteInSession: builder.mutation<
      import('../types').ApiResponse<void>,
      { sessionId: number; routeId: number }
    >({
      query: ({ sessionId, routeId }) => ({
        url: `/route-planning-sessions/${sessionId}/routes/${routeId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: (_r, _e, { sessionId }) => [
        { type: 'schoolBus/Route', id: `SESSION-ROUTES-${sessionId}` },
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: `SESSION-${sessionId}` },
        { type: 'schoolBus/Route', id: 'ACTIVE_SESSION' },
      ],
    }),
    greedyFillRoute: builder.mutation<
      import('../types').ApiResponse<import('../types').GreedyFillRouteResponse>,
      {
        sessionId: number;
        routeId: number;
        body?: import('../types').GreedyFillRouteRequest;
      }
    >({
      query: ({ sessionId, routeId, body }) => ({
        url: `/route-planning-sessions/${sessionId}/routes/${routeId}/greedy-fill`,
        method: 'POST',
        body: body ?? { preserveExistingAssignments: true },
      }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: (_result, _error, { sessionId, routeId }) => [
        { type: 'schoolBus/Route', id: routeId },
        { type: 'schoolBus/Route', id: `DETAIL-${routeId}` },
        { type: 'schoolBus/Route', id: `PATH-${routeId}` },
        { type: 'schoolBus/Route', id: `SESSION-ROUTES-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-${sessionId}` },
        { type: 'schoolBus/Route', id: `SESSION-ELIGIBLE-${sessionId}` },
        { type: 'schoolBus/Route', id: 'SESSION_LIST' },
        { type: 'schoolBus/Route', id: 'LIST' },
        { type: 'schoolBus/Route', id: 'ACTIVE_SESSION' },
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
        { type: 'schoolBus/Route', id: `SESSION-ROUTES-${sessionId}` },
        { type: 'schoolBus/Route', id: 'ACTIVE_SESSION' },
      ],
    }),
  }),
});

const {
  useLazySearchMapLocationsQuery,
  useLazyReverseMapLocationQuery,
  useGetSchoolBusSummaryQuery: useGetSchoolBusSummaryQueryOrig,
  useGetOperationsDashboardQuery: useGetOperationsDashboardQueryOrig,
  useLazyGetOperationsDashboardQuery,
  useGetSchoolBusReportQuery: useGetSchoolBusReportQueryOrig,
  useGetSchoolBusReportTripsQuery: useGetSchoolBusReportTripsQueryOrig,

  useGetSchoolBusReportAttendanceQuery: useGetSchoolBusReportAttendanceQueryOrig,
  useGetSchoolBusReportCapacityQuery: useGetSchoolBusReportCapacityQueryOrig,
  useGetBusTypesQuery: useGetBusTypesQueryOrig,
  useGetSchoolsQuery: useGetSchoolsQueryOrig,
  useGetSchoolByIdQuery: useGetSchoolByIdQueryOrig,
  useCreateSchoolMutation,
  useUpdateSchoolMutation,
  useDeleteSchoolMutation,
  useGetParentsQuery: useGetParentsQueryOrig,
  useGetParentByIdQuery: useGetParentByIdQueryOrig,
  useCreateParentMutation,
  useUpdateParentMutation,
  useDeleteParentMutation,
  useGetStudentsQuery: useGetStudentsQueryOrig,
  useGetStudentByIdQuery: useGetStudentByIdQueryOrig,
  useCreateStudentMutation,
  useUpdateStudentMutation,
  useDeleteStudentMutation,
  useGetBusesQuery: useGetBusesQueryOrig,
  useGetBusByIdQuery: useGetBusByIdQueryOrig,
  useCreateBusMutation,
  useUpdateBusMutation,
  useDeleteBusMutation,
  useGetDriversQuery: useGetDriversQueryOrig,
  useGetDriverByIdQuery: useGetDriverByIdQueryOrig,
  useCreateDriverMutation,
  useUpdateDriverMutation,
  useDeleteDriverMutation,
  useGetAttendantsQuery: useGetAttendantsQueryOrig,
  useGetAttendantByIdQuery: useGetAttendantByIdQueryOrig,
  useCreateAttendantMutation,
  useUpdateAttendantMutation,
  useDeleteAttendantMutation,
  useGetPickupPointsQuery: useGetPickupPointsQueryOrig,
  useGetPickupPointByIdQuery: useGetPickupPointByIdQueryOrig,
  useCreatePickupPointMutation,
  useUpdatePickupPointMutation,
  useDeletePickupPointMutation,
  useGetDepotsQuery: useGetDepotsQueryOrig,
  useGetDepotByIdQuery: useGetDepotByIdQueryOrig,
  useCreateDepotMutation,
  useUpdateDepotMutation,
  useDeleteDepotMutation,
  useGetTransportRequestsQuery: useGetTransportRequestsQueryOrig,
  useGetTransportRequestByIdQuery: useGetTransportRequestByIdQueryOrig,
  useCreateTransportRequestMutation,
  useUpdateTransportRequestMutation,
  useApproveTransportRequestMutation,
  useRejectTransportRequestMutation,
  useCancelTransportRequestMutation,
  useGetTransportRequestHistoryQuery: useGetTransportRequestHistoryQueryOrig,
  useGetSchoolBusSubscriptionsQuery: useGetSchoolBusSubscriptionsQueryOrig,
  useGetSchoolBusSubscriptionByIdQuery: useGetSchoolBusSubscriptionByIdQueryOrig,
  useCreateSchoolBusSubscriptionMutation,
  useActivateSchoolBusSubscriptionMutation,
  usePauseSchoolBusSubscriptionMutation,
  useStopSchoolBusSubscriptionMutation,
  useGetSchoolBusSubscriptionHistoryQuery: useGetSchoolBusSubscriptionHistoryQueryOrig,
  useGetRoutesQuery: useGetRoutesQueryOrig,
  useGetRouteByIdQuery: useGetRouteByIdQueryOrig,
  useGetRoutePathQuery: useGetRoutePathQueryOrig,
  useUpdateRouteMutation,
  useGetAssignmentHistoryQuery: useGetAssignmentHistoryQueryOrig,
  useAssignRouteMutation,
  useManualDispatchRouteMutation,
  useReorderRouteStopsMutation,
  useAddRouteStopMutation,
  useRemoveRouteStopMutation,
  useMoveRouteStudentMutation,
  useRemoveRouteStudentMutation,
  useCreateTripFromRouteMutation,
  useGetTripsQuery: useGetTripsQueryOrig,
  useGetTripByIdQuery: useGetTripByIdQueryOrig,
  useStartTripMutation,
  useArriveTripStopMutation,
  useDepartTripStopMutation,
  useSkipTripStopMutation,
  useCompleteTripMutation,
  useCancelTripMutation,
  useGetTripStopsQuery: useGetTripStopsQueryOrig,
  useGetTripStudentsQuery: useGetTripStudentsQueryOrig,
  useGetTripAttendanceQuery: useGetTripAttendanceQueryOrig,
  useGetTripAttendanceManifestQuery: useGetTripAttendanceManifestQueryOrig,
  useGetTripAttendanceSummaryQuery: useGetTripAttendanceSummaryQueryOrig,
  useBoardTripStudentMutation,
  useDropoffTripStudentMutation,
  useAbsentTripStudentMutation,
  useNoShowTripStudentMutation,
  useNotServedTripStudentMutation,
  useStartBoardingTripStopMutation,
  useGetAttendanceQuery: useGetAttendanceQueryOrig,
  useGetTripHistoryQuery: useGetTripHistoryQueryOrig,
  useGetSchoolPickupPointsQuery: useGetSchoolPickupPointsQueryOrig,
  useGetActiveSchoolPickupPointsQuery: useGetActiveSchoolPickupPointsQueryOrig,
  useGetSchoolPickupPointsCompatibilityQuery: useGetSchoolPickupPointsCompatibilityQueryOrig,
  useGetAllActiveSchoolPickupLinksQuery: useGetAllActiveSchoolPickupLinksQueryOrig,
  useLinkSchoolPickupPointMutation,
  useUpdateSchoolPickupPointMutation,
  useUnlinkSchoolPickupPointMutation,
  usePreviewPlanningDemandMutation,
  useCreatePlanningSessionMutation,
  useGetPlanningSessionsQueryQuery: useGetPlanningSessionsQueryQueryOrig,
  useGetPlanningSessionQuery: useGetPlanningSessionQueryOrig,
  usePublishPlanningSessionMutation,
  useCancelPlanningSessionMutation,
  useGetSessionRoutesQuery: useGetSessionRoutesQueryOrig,
  useCreateRouteInSessionMutation,
  useDeleteRouteInSessionMutation,
  useGreedyFillRouteMutation,
  useGetSessionEligibleStudentsQuery: useGetSessionEligibleStudentsQueryOrig,
  useAssignStudentToRouteMutation,
  useGetSchoolDropdownOptionsQuery: useGetSchoolDropdownOptionsQueryOrig,
  useGetSchoolPickupPointDropdownOptionsQuery: useGetSchoolPickupPointDropdownOptionsQueryOrig,
  useGetParentDropdownOptionsQuery: useGetParentDropdownOptionsQueryOrig,
  useGetDriverDropdownOptionsQuery: useGetDriverDropdownOptionsQueryOrig,
  useGetAttendantDropdownOptionsQuery: useGetAttendantDropdownOptionsQueryOrig,
  useGetBusDropdownOptionsQuery: useGetBusDropdownOptionsQueryOrig,
} = schoolBusApi;

function wrapQueryHook<T extends (arg: any, options?: any) => any>(hook: T): T {
  return ((arg: any, options?: any) => {
    return hook(arg, { refetchOnMountOrArgChange: true, ...options });
  }) as unknown as T;
}

export const useGetSchoolBusSummaryQuery = wrapQueryHook(useGetSchoolBusSummaryQueryOrig);
export const useGetSchoolDropdownOptionsQuery = wrapQueryHook(useGetSchoolDropdownOptionsQueryOrig);
export const useGetSchoolPickupPointDropdownOptionsQuery = wrapQueryHook(useGetSchoolPickupPointDropdownOptionsQueryOrig);
export const useGetParentDropdownOptionsQuery = wrapQueryHook(useGetParentDropdownOptionsQueryOrig);
export const useGetDriverDropdownOptionsQuery = wrapQueryHook(useGetDriverDropdownOptionsQueryOrig);
export const useGetAttendantDropdownOptionsQuery = wrapQueryHook(useGetAttendantDropdownOptionsQueryOrig);
export const useGetBusDropdownOptionsQuery = wrapQueryHook(useGetBusDropdownOptionsQueryOrig);
export const useGetOperationsDashboardQuery = wrapQueryHook(useGetOperationsDashboardQueryOrig);
export const useGetSchoolBusReportQuery = wrapQueryHook(useGetSchoolBusReportQueryOrig);

export const useGetSchoolBusReportTripsQuery = wrapQueryHook(useGetSchoolBusReportTripsQueryOrig);
export const useGetSchoolBusReportAttendanceQuery = wrapQueryHook(useGetSchoolBusReportAttendanceQueryOrig);
export const useGetSchoolBusReportCapacityQuery = wrapQueryHook(useGetSchoolBusReportCapacityQueryOrig);
export const useGetBusTypesQuery = wrapQueryHook(useGetBusTypesQueryOrig);
export const useGetSchoolsQuery = wrapQueryHook(useGetSchoolsQueryOrig);
export const useGetSchoolByIdQuery = wrapQueryHook(useGetSchoolByIdQueryOrig);
export const useGetParentsQuery = wrapQueryHook(useGetParentsQueryOrig);
export const useGetParentByIdQuery = wrapQueryHook(useGetParentByIdQueryOrig);
export const useGetStudentsQuery = wrapQueryHook(useGetStudentsQueryOrig);
export const useGetStudentByIdQuery = wrapQueryHook(useGetStudentByIdQueryOrig);
export const useGetBusesQuery = wrapQueryHook(useGetBusesQueryOrig);
export const useGetBusByIdQuery = wrapQueryHook(useGetBusByIdQueryOrig);
export const useGetDriversQuery = wrapQueryHook(useGetDriversQueryOrig);
export const useGetDriverByIdQuery = wrapQueryHook(useGetDriverByIdQueryOrig);
export const useGetAttendantsQuery = wrapQueryHook(useGetAttendantsQueryOrig);
export const useGetAttendantByIdQuery = wrapQueryHook(useGetAttendantByIdQueryOrig);
export const useGetPickupPointsQuery = wrapQueryHook(useGetPickupPointsQueryOrig);
export const useGetPickupPointByIdQuery = wrapQueryHook(useGetPickupPointByIdQueryOrig);
export const useGetDepotsQuery = wrapQueryHook(useGetDepotsQueryOrig);
export const useGetDepotByIdQuery = wrapQueryHook(useGetDepotByIdQueryOrig);
export const useGetTransportRequestsQuery = wrapQueryHook(useGetTransportRequestsQueryOrig);
export const useGetTransportRequestByIdQuery = wrapQueryHook(useGetTransportRequestByIdQueryOrig);
export const useGetTransportRequestHistoryQuery = wrapQueryHook(useGetTransportRequestHistoryQueryOrig);
export const useGetSchoolBusSubscriptionsQuery = wrapQueryHook(useGetSchoolBusSubscriptionsQueryOrig);
export const useGetSchoolBusSubscriptionByIdQuery = wrapQueryHook(useGetSchoolBusSubscriptionByIdQueryOrig);
export const useGetSchoolBusSubscriptionHistoryQuery = wrapQueryHook(useGetSchoolBusSubscriptionHistoryQueryOrig);
export const useGetRoutesQuery = wrapQueryHook(useGetRoutesQueryOrig);
export const useGetRouteByIdQuery = wrapQueryHook(useGetRouteByIdQueryOrig);
export const useGetRoutePathQuery = wrapQueryHook(useGetRoutePathQueryOrig);
export const useGetAssignmentHistoryQuery = wrapQueryHook(useGetAssignmentHistoryQueryOrig);
export const useGetTripsQuery = wrapQueryHook(useGetTripsQueryOrig);
export const useGetTripByIdQuery = wrapQueryHook(useGetTripByIdQueryOrig);
export const useGetTripStopsQuery = wrapQueryHook(useGetTripStopsQueryOrig);
export const useGetTripStudentsQuery = wrapQueryHook(useGetTripStudentsQueryOrig);
export const useGetTripAttendanceQuery = wrapQueryHook(useGetTripAttendanceQueryOrig);
export const useGetTripAttendanceManifestQuery = wrapQueryHook(useGetTripAttendanceManifestQueryOrig);
export const useGetTripAttendanceSummaryQuery = wrapQueryHook(useGetTripAttendanceSummaryQueryOrig);
export const useGetAttendanceQuery = wrapQueryHook(useGetAttendanceQueryOrig);
export const useGetTripHistoryQuery = wrapQueryHook(useGetTripHistoryQueryOrig);
export const useGetSchoolPickupPointsQuery = wrapQueryHook(useGetSchoolPickupPointsQueryOrig);
export const useGetActiveSchoolPickupPointsQuery = wrapQueryHook(useGetActiveSchoolPickupPointsQueryOrig);
export const useGetSchoolPickupPointsCompatibilityQuery = wrapQueryHook(useGetSchoolPickupPointsCompatibilityQueryOrig);
export const useGetAllActiveSchoolPickupLinksQuery = wrapQueryHook(useGetAllActiveSchoolPickupLinksQueryOrig);
export const useGetPlanningSessionsQueryQuery = wrapQueryHook(useGetPlanningSessionsQueryQueryOrig);
export const useGetPlanningSessionQuery = wrapQueryHook(useGetPlanningSessionQueryOrig);
export const useGetSessionRoutesQuery = wrapQueryHook(useGetSessionRoutesQueryOrig);
export const useGetSessionEligibleStudentsQuery = wrapQueryHook(useGetSessionEligibleStudentsQueryOrig);

export {
  useLazySearchMapLocationsQuery,
  useLazyReverseMapLocationQuery,
  useLazyGetOperationsDashboardQuery,
  useCreateSchoolMutation,

  useUpdateSchoolMutation,
  useDeleteSchoolMutation,
  useCreateParentMutation,
  useUpdateParentMutation,
  useDeleteParentMutation,
  useCreateStudentMutation,
  useUpdateStudentMutation,
  useDeleteStudentMutation,
  useCreateBusMutation,
  useUpdateBusMutation,
  useDeleteBusMutation,
  useCreateDriverMutation,
  useUpdateDriverMutation,
  useDeleteDriverMutation,
  useCreateAttendantMutation,
  useUpdateAttendantMutation,
  useDeleteAttendantMutation,
  useCreatePickupPointMutation,
  useUpdatePickupPointMutation,
  useDeletePickupPointMutation,
  useCreateDepotMutation,
  useUpdateDepotMutation,
  useDeleteDepotMutation,
  useCreateTransportRequestMutation,
  useUpdateTransportRequestMutation,
  useApproveTransportRequestMutation,
  useRejectTransportRequestMutation,
  useCancelTransportRequestMutation,
  useCreateSchoolBusSubscriptionMutation,
  useActivateSchoolBusSubscriptionMutation,
  usePauseSchoolBusSubscriptionMutation,
  useStopSchoolBusSubscriptionMutation,
  useUpdateRouteMutation,
  useAssignRouteMutation,
  useManualDispatchRouteMutation,
  useReorderRouteStopsMutation,
  useAddRouteStopMutation,
  useRemoveRouteStopMutation,
  useMoveRouteStudentMutation,
  useRemoveRouteStudentMutation,
  useCreateTripFromRouteMutation,
  useStartTripMutation,
  useArriveTripStopMutation,
  useDepartTripStopMutation,
  useSkipTripStopMutation,
  useCompleteTripMutation,
  useCancelTripMutation,
  useBoardTripStudentMutation,
  useDropoffTripStudentMutation,
  useAbsentTripStudentMutation,
  useNoShowTripStudentMutation,
  useNotServedTripStudentMutation,
  useStartBoardingTripStopMutation,
  useLinkSchoolPickupPointMutation,
  useUpdateSchoolPickupPointMutation,
  useUnlinkSchoolPickupPointMutation,
  usePreviewPlanningDemandMutation,
  useCreatePlanningSessionMutation,
  usePublishPlanningSessionMutation,
  useCancelPlanningSessionMutation,
  useCreateRouteInSessionMutation,
  useDeleteRouteInSessionMutation,
  useGreedyFillRouteMutation,
  useAssignStudentToRouteMutation,
};
