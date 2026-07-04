import { api } from '@/lib/store/api';
import { createApiResponseTransform } from '@/lib/store/api/utils';
import type {
  ApiResponse,
  ChartItemDto,
  DashboardOperationsResponse,
  DashboardSummary,
  SchoolBusDropdownOption,
} from '../types';

export interface SchoolBusDashboardQueryParams {
  serviceDate?: string;
  schoolId?: number;
  direction?: 'OUTBOUND' | 'RETURN';
  userKey: string;
}

function dashboardQuery(url: string, args: SchoolBusDashboardQueryParams) {
  const { userKey: _userKey, ...params } = args;
  return {
    url,
    method: 'GET',
    params,
  };
}

const transformApiResponse = createApiResponseTransform;

export const schoolBusDashboardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDashboardSummary: builder.query<
      ApiResponse<DashboardSummary>,
      SchoolBusDashboardQueryParams
    >({
      query: (args) => dashboardQuery('/dashboard/summary', args),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<DashboardSummary>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'SUMMARY' }],
    }),
    getDashboardOperations: builder.query<
      ApiResponse<DashboardOperationsResponse>,
      SchoolBusDashboardQueryParams
    >({
      query: (args) => dashboardQuery('/dashboard/operations', args),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<DashboardOperationsResponse>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'OPERATIONS' }],
    }),
    getDashboardTripStatus: builder.query<
      ApiResponse<ChartItemDto[]>,
      SchoolBusDashboardQueryParams
    >({
      query: (args) => dashboardQuery('/dashboard/charts/trip-status', args),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<ChartItemDto[]>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'TRIP_STATUS' }],
    }),
    getDashboardAttendanceStatus: builder.query<
      ApiResponse<ChartItemDto[]>,
      SchoolBusDashboardQueryParams
    >({
      query: (args) =>
        dashboardQuery('/dashboard/charts/attendance-status', args),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<ChartItemDto[]>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'ATTENDANCE_STATUS' }],
    }),
    getDashboardRouteReadiness: builder.query<
      ApiResponse<ChartItemDto[]>,
      SchoolBusDashboardQueryParams
    >({
      query: (args) =>
        dashboardQuery('/dashboard/charts/route-readiness', args),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<ChartItemDto[]>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'ROUTE_READINESS' }],
    }),
    getDashboardRequestStatus: builder.query<
      ApiResponse<ChartItemDto[]>,
      SchoolBusDashboardQueryParams
    >({
      query: (args) => dashboardQuery('/dashboard/charts/request-status', args),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<ChartItemDto[]>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'REQUEST_STATUS' }],
    }),
    getDashboardTripsByDate: builder.query<
      ApiResponse<ChartItemDto[]>,
      SchoolBusDashboardQueryParams
    >({
      query: (args) => dashboardQuery('/dashboard/charts/trips-by-date', args),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<ChartItemDto[]>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'TRIPS_BY_DATE' }],
    }),
    getDashboardSchools: builder.query<
      ApiResponse<SchoolBusDropdownOption[]>,
      { userKey: string }
    >({
      query: () => ({
        url: '/dashboard/schools',
        method: 'GET',
      }),
      extraOptions: { service: 'school-bus' },
      transformResponse: transformApiResponse<SchoolBusDropdownOption[]>(),
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'SCHOOLS' }],
    }),
  }),
});

export const {
  useGetDashboardSummaryQuery,
  useGetDashboardOperationsQuery,
  useGetDashboardTripStatusQuery,
  useGetDashboardAttendanceStatusQuery,
  useGetDashboardRouteReadinessQuery,
  useGetDashboardRequestStatusQuery,
  useGetDashboardTripsByDateQuery,
  useGetDashboardSchoolsQuery,
} = schoolBusDashboardApi;
