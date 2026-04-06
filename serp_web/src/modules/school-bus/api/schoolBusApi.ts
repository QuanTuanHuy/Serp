import { api } from '@/lib/store/api';
import type {
  ApiResponse,
  DashboardSummary,
  OperationalReport,
  SchoolBusAttendance,
  SchoolBusAttendant,
  SchoolBusBus,
  SchoolBusDriver,
  SchoolBusParent,
  SchoolBusPickupPoint,
  SchoolBusRoute,
  SchoolBusSchool,
  SchoolBusStudent,
  SchoolBusTransportRequest,
  SchoolBusTripHistory,
} from '../types';

export const schoolBusApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSchoolBusSummary: builder.query<ApiResponse<DashboardSummary>, void>({
      query: () => ({ url: '/dashboard/summary', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Dashboard', id: 'SUMMARY' }],
    }),
    getSchoolBusReport: builder.query<ApiResponse<OperationalReport>, void>({
      query: () => ({ url: '/reports/operations-summary', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Report', id: 'SUMMARY' }],
    }),
    getSchools: builder.query<ApiResponse<SchoolBusSchool[]>, void>({
      query: () => ({ url: '/schools', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/School', id: 'LIST' }],
    }),
    getParents: builder.query<ApiResponse<SchoolBusParent[]>, void>({
      query: () => ({ url: '/parents', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Parent', id: 'LIST' }],
    }),
    getStudents: builder.query<ApiResponse<SchoolBusStudent[]>, void>({
      query: () => ({ url: '/students', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Student', id: 'LIST' }],
    }),
    getBuses: builder.query<ApiResponse<SchoolBusBus[]>, void>({
      query: () => ({ url: '/buses', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Bus', id: 'LIST' }],
    }),
    getDrivers: builder.query<ApiResponse<SchoolBusDriver[]>, void>({
      query: () => ({ url: '/drivers', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Driver', id: 'LIST' }],
    }),
    getAttendants: builder.query<ApiResponse<SchoolBusAttendant[]>, void>({
      query: () => ({ url: '/attendants', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Attendant', id: 'LIST' }],
    }),
    getPickupPoints: builder.query<ApiResponse<SchoolBusPickupPoint[]>, void>({
      query: () => ({ url: '/pickup-points', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/PickupPoint', id: 'LIST' }],
    }),
    getTransportRequests: builder.query<ApiResponse<SchoolBusTransportRequest[]>, void>({
      query: () => ({ url: '/transport-requests', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/TransportRequest', id: 'LIST' }],
    }),
    approveTransportRequest: builder.mutation<ApiResponse<SchoolBusTransportRequest>, number>({
      query: (id) => ({ url: `/transport-requests/${id}/approve`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: [{ type: 'schoolBus/TransportRequest', id: 'LIST' }],
    }),
    getRoutes: builder.query<ApiResponse<SchoolBusRoute[]>, void>({
      query: () => ({ url: '/routes', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Route', id: 'LIST' }],
    }),
    generateGreedyPlan: builder.mutation<ApiResponse<unknown>, number>({
      query: (id) => ({ url: `/routes/${id}/generate-greedy-plan`, method: 'POST' }),
      extraOptions: { service: 'school-bus' },
      invalidatesTags: [{ type: 'schoolBus/Route', id: 'LIST' }],
    }),
    getAttendance: builder.query<ApiResponse<SchoolBusAttendance[]>, void>({
      query: () => ({ url: '/attendance', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/Attendance', id: 'LIST' }],
    }),
    getTripHistory: builder.query<ApiResponse<SchoolBusTripHistory[]>, void>({
      query: () => ({ url: '/trip-history', method: 'GET' }),
      extraOptions: { service: 'school-bus' },
      providesTags: [{ type: 'schoolBus/TripHistory', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetSchoolBusSummaryQuery,
  useGetSchoolBusReportQuery,
  useGetSchoolsQuery,
  useGetParentsQuery,
  useGetStudentsQuery,
  useGetBusesQuery,
  useGetDriversQuery,
  useGetAttendantsQuery,
  useGetPickupPointsQuery,
  useGetTransportRequestsQuery,
  useApproveTransportRequestMutation,
  useGetRoutesQuery,
  useGenerateGreedyPlanMutation,
  useGetAttendanceQuery,
  useGetTripHistoryQuery,
} = schoolBusApi;
