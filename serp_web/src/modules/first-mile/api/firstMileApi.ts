/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  AssignHubPostOfficeRequest,
  CreateHubRequest,
  UpdateHubRequest,
  HubImportItem,
  AutoAssignPickupPlanRequest,
  CancelOrderRequest,
  CreateOrderRequest,
  CreateVehicleRequest,
  CreatePostOfficeRequest,
  CreateProductTypeRequest,
  ConfirmOrderPaymentRequest,
  ConfirmPostOfficeInboundRequest,
  FirstMileApiResponse,
  FirstMileOrderDetail,
  FirstMileOrderListFilters,
  FirstMileOrderTimelineItem,
  FirstMilePaginatedData,
  GeocodeAddressRequest,
  GeocodeAddressResponse,
  Hub,
  SecondMileHubStaffAssignment,
  SecondMileHubStaff,
  SecondMileHubStaffRole,
  HubListFilters,
  HubPostOfficeMapping,
  InitiateOrderPaymentRequest,
  ImportHistory,
  ImportType,
  ManualAssignPickupOrdersRequest,
  OptimizePickupPlanRequest,
  OrderConfirmationResponse,
  OrderDropOffPostOfficeSuggestion,
  OrderImportItem,
  OrderPaymentConfirmResponse,
  OrderPaymentInitResponse,
  PostOffice,
  PostOfficeGeocodeBatchResponse,
  PostOfficeStaff,
  PostOfficeStaffAssignment,
  PostOfficeStaffRole,
  UpdatePostOfficeStaffAssignmentRequest,
  PostOfficeImportItem,
  PostOfficeListFilters,
  PickupAssignmentResponse,
  PickupCheckinDetailResponse,
  PickupCheckinResponse,
  PickupTripLifecycleResponse,
  PickupTrackingOverviewResponse,
  PickupOptimizationResponse,
  ProductType,
  Province,
  UpdateOrderRequest,
  UpdatePostOfficeRequest,
  UpdateProductTypeRequest,
  ValidateImportFileResponse,
  SecondMileCreateVehicleRequest,
  SecondMileUpdateVehicleRequest,
  SecondMileVehicle,
  SecondMileVehicleImportItem,
  SecondMileVehicleListFilters,
  AddSecondMileBagOrderRequest,
  AutoSecondMileBaggingPlan,
  AutoSecondMileBaggingPlanRequest,
  CreateSecondMileBagRequest,
  ReopenSecondMileBagRequest,
  SecondMileBag,
  SecondMileBagListFilters,
  SecondMileBagSuggestion,
  SecondMileBaggingKpi,
  SecondMileBaggingValidation,
  UpdateSecondMileBagRequest,
  ValidateSecondMileBaggingRequest,
  SecondMileRoute,
  SecondMileRouteListFilters,
  SecondMileCreateRouteRequest,
  SecondMileUpdateRouteRequest,
  HandoverManifest,
  HandoverManifestListFilters,
  CreateHandoverManifestRequest,
  CreatePostOfficeHandoverManifestRequest,
  SecondMileOrder,
  SecondMileOrderListFilters,
  DispatchPostOfficeHandoverManifestRequest,
  Vehicle,
  VehicleImportItem,
  ScanOutHandoverOrderRequest,
  UpdateVehicleRequest,
  Ward,
} from '../types';
import {
  normalizeSecondMileHubImportHistory,
  normalizeSecondMileHubStaffAssignment,
  normalizeSecondMileHubStaff,
  normalizePostOfficeStaffAssignment,
  normalizeHubPostOfficeMapping,
  normalizeHubPostOfficeMappingPage,
  normalizeHandoverManifest,
  normalizeHandoverManifestPage,
  normalizeSecondMileRoute,
  normalizeSecondMileRoutePage,
  normalizeAutoSecondMileBaggingPlan,
  normalizeSecondMileBag,
  normalizeSecondMileBagPage,
  normalizeSecondMileBagSuggestion,
  normalizeSecondMileBaggingKpi,
  normalizeSecondMileBaggingValidation,
  normalizeSecondMileVehicle,
  normalizeSecondMileVehiclePage,
  normalizeSecondMileOrderPage,
  unwrapFirstMilePageResult,
  unwrapFirstMilePageResultOrRaw,
  unwrapFirstMileResult,
  unwrapFirstMileResultOrRaw,
} from './transforms';

const FIRST_MILE_SERVICE = { service: 'first-mile' as const };
const SECOND_MILE_SERVICE = { service: 'second-mile' as const };
const TMS_ORDER_SERVICE = { service: 'tms-order' as const };

export const firstMileApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getHubs: builder.query<
      FirstMilePaginatedData<Hub>,
      { page?: number; size?: number } & HubListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        code,
        name,
        hubType,
        provinceCode,
        wardCode,
        status,
        hasLocation,
        minDailyCapacity,
        maxDailyCapacity,
        minCurrentLoad,
        maxCurrentLoad,
      }) => ({
        url: '/hubs',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
          ...(code ? { code } : {}),
          ...(name ? { name } : {}),
          ...(hubType ? { hub_type: hubType } : {}),
          ...(provinceCode ? { province_code: provinceCode } : {}),
          ...(wardCode ? { ward_code: wardCode } : {}),
          ...(status ? { status } : {}),
          ...(hasLocation !== undefined ? { has_location: hasLocation } : {}),
          ...(minDailyCapacity !== undefined
            ? { min_daily_capacity: minDailyCapacity }
            : {}),
          ...(maxDailyCapacity !== undefined
            ? { max_daily_capacity: maxDailyCapacity }
            : {}),
          ...(minCurrentLoad !== undefined
            ? { min_current_load: minCurrentLoad }
            : {}),
          ...(maxCurrentLoad !== undefined
            ? { max_current_load: maxCurrentLoad }
            : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResult<Hub>,
    }),

    getHubById: builder.query<Hub, number>({
      query: (id) => ({
        url: `/hubs/${id}`,
        method: 'GET',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Hub>,
    }),

    getHubPostOffices: builder.query<
      FirstMilePaginatedData<HubPostOfficeMapping>,
      { hubId: number; page?: number; size?: number }
    >({
      query: ({ hubId, page = 0, size = 20 }) => ({
        url: `/hubs/${hubId}/post-offices`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: normalizeHubPostOfficeMappingPage,
    }),

    assignPostOfficeToHub: builder.mutation<
      HubPostOfficeMapping,
      { hubId: number; request: AssignHubPostOfficeRequest }
    >({
      query: ({ hubId, request }) => ({
        url: `/hubs/${hubId}/post-offices`,
        method: 'POST',
        body: request,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<HubPostOfficeMapping>
      ) => normalizeHubPostOfficeMapping(unwrapFirstMileResult(response)),
    }),

    removePostOfficeFromHub: builder.mutation<
      any,
      { hubId: number; postOfficeCode: string }
    >({
      query: ({ hubId, postOfficeCode }) => ({
        url: `/hubs/${hubId}/post-offices/${encodeURIComponent(postOfficeCode)}`,
        method: 'DELETE',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw,
    }),

    getSecondMileHubStaffAssignments: builder.query<
      SecondMileHubStaffAssignment[],
      { hubId: number; role?: SecondMileHubStaffRole }
    >({
      query: ({ hubId, role }) => ({
        url: `/hub-staff-assignments/hubs/${hubId}`,
        method: 'GET',
        params: {
          ...(role ? { role } : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<unknown[]>) =>
        unwrapFirstMileResult<unknown[]>(response).map((item) =>
          normalizeSecondMileHubStaffAssignment(item)
        ),
    }),

    getSecondMileAssignableStaffs: builder.query<
      SecondMileHubStaff[],
      { role: SecondMileHubStaffRole; keyword?: string }
    >({
      query: ({ role, keyword }) => ({
        url: '/hub-staff-assignments/staffs',
        method: 'GET',
        params: {
          role,
          ...(keyword ? { keyword } : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<unknown[]>) =>
        unwrapFirstMileResult<unknown[]>(response).map((item) =>
          normalizeSecondMileHubStaff(item)
        ),
    }),

    assignSecondMileStaffToHub: builder.mutation<
      SecondMileHubStaffAssignment,
      { staffId: number; hubId: number }
    >({
      query: ({ staffId, hubId }) => ({
        url: `/hub-staff-assignments/staffs/${staffId}/hubs/${hubId}`,
        method: 'PUT',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<SecondMileHubStaffAssignment>
      ) =>
        normalizeSecondMileHubStaffAssignment(unwrapFirstMileResult(response)),
    }),

    unassignSecondMileHubStaffAssignment: builder.mutation<
      SecondMileHubStaffAssignment,
      number
    >({
      query: (assignmentId) => ({
        url: `/hub-staff-assignments/${assignmentId}/unassign`,
        method: 'PUT',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<SecondMileHubStaffAssignment>
      ) =>
        normalizeSecondMileHubStaffAssignment(unwrapFirstMileResult(response)),
    }),

    exportHubTemplate: builder.query<Blob, void>({
      query: () => ({
        url: '/hubs/template',
        method: 'GET',
        responseHandler: (response) => response.blob(),
      }),
      extraOptions: SECOND_MILE_SERVICE,
    }),

    validateHubImport: builder.mutation<
      ValidateImportFileResponse<HubImportItem>,
      FormData
    >({
      query: (formData) => ({
        url: '/hubs/validate',
        method: 'POST',
        body: formData,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: unknown) =>
        response as ValidateImportFileResponse<HubImportItem>,
    }),

    importHubs: builder.mutation<ImportHistory, FormData>({
      query: (formData) => ({
        url: '/hubs/import',
        method: 'POST',
        body: formData,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: unknown) =>
        normalizeSecondMileHubImportHistory(response),
    }),

    createHub: builder.mutation<Hub, CreateHubRequest>({
      query: (body) => ({
        url: '/hubs',
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Hub>,
    }),

    updateHub: builder.mutation<Hub, { id: number; body: UpdateHubRequest }>({
      query: ({ id, body }) => ({
        url: `/hubs/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Hub>,
    }),

    deleteHub: builder.mutation<string, number>({
      query: (id) => ({
        url: `/hubs/${id}`,
        method: 'DELETE',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: { message?: string }) =>
        response?.message || 'Deleted successfully',
    }),

    uploadHubImage: builder.mutation<Hub, { id: number; file: File }>({
      query: ({ id, file }) => {
        const formData = new FormData();
        formData.append('file', file);
        return {
          url: `/hubs/${id}/image`,
          method: 'POST',
          body: formData,
        };
      },
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Hub>,
    }),

    getSecondMileVehicles: builder.query<
      FirstMilePaginatedData<SecondMileVehicle>,
      { page?: number; size?: number } & SecondMileVehicleListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        licensePlate,
        vehicleType,
        hubId,
        assignedStaffId,
        status,
      }) => ({
        url: '/vehicles',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
          ...(licensePlate ? { license_plate: licensePlate } : {}),
          ...(vehicleType ? { vehicle_type: vehicleType } : {}),
          ...(hubId !== undefined ? { hub_id: hubId } : {}),
          ...(assignedStaffId !== undefined
            ? { assigned_staff_id: assignedStaffId }
            : {}),
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: normalizeSecondMileVehiclePage,
    }),

    getSecondMileVehicleById: builder.query<SecondMileVehicle, number>({
      query: (id) => ({
        url: `/vehicles/${id}`,
        method: 'GET',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileVehicle>) =>
        normalizeSecondMileVehicle(unwrapFirstMileResult(response)),
    }),

    createSecondMileVehicle: builder.mutation<
      SecondMileVehicle,
      SecondMileCreateVehicleRequest
    >({
      query: (body) => ({
        url: '/vehicles',
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileVehicle>) =>
        normalizeSecondMileVehicle(unwrapFirstMileResult(response)),
    }),

    updateSecondMileVehicle: builder.mutation<
      SecondMileVehicle,
      { id: number; body: SecondMileUpdateVehicleRequest }
    >({
      query: ({ id, body }) => ({
        url: `/vehicles/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileVehicle>) =>
        normalizeSecondMileVehicle(unwrapFirstMileResult(response)),
    }),

    deleteSecondMileVehicle: builder.mutation<string, number>({
      query: (id) => ({
        url: `/vehicles/${id}`,
        method: 'DELETE',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: { message?: string }) =>
        response?.message || 'Deleted successfully',
    }),

    uploadSecondMileVehicleImage: builder.mutation<
      SecondMileVehicle,
      { id: number; file: File }
    >({
      query: ({ id, file }) => {
        const formData = new FormData();
        formData.append('file', file);
        return {
          url: `/vehicles/${id}/image`,
          method: 'POST',
          body: formData,
        };
      },
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileVehicle>) =>
        normalizeSecondMileVehicle(unwrapFirstMileResult(response)),
    }),

    exportSecondMileVehicleTemplate: builder.query<Blob, void>({
      query: () => ({
        url: '/vehicles/template',
        method: 'GET',
        responseHandler: (response) => response.blob(),
      }),
      extraOptions: SECOND_MILE_SERVICE,
    }),

    validateSecondMileVehicleImport: builder.mutation<
      ValidateImportFileResponse<SecondMileVehicleImportItem>,
      FormData
    >({
      query: (formData) => ({
        url: '/vehicles/validate',
        method: 'POST',
        body: formData,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: unknown) =>
        response as ValidateImportFileResponse<SecondMileVehicleImportItem>,
    }),

    importSecondMileVehicles: builder.mutation<ImportHistory, FormData>({
      query: (formData) => ({
        url: '/vehicles/import',
        method: 'POST',
        body: formData,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: unknown) =>
        normalizeSecondMileHubImportHistory(response),
    }),

    getSecondMileBags: builder.query<
      FirstMilePaginatedData<SecondMileBag>,
      { page?: number; size?: number } & SecondMileBagListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        bagCode,
        originHubId,
        destinationType,
        destinationHubId,
        destinationPostOfficeCode,
        vehicleId,
        status,
      }) => ({
        url: '/bags',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
          ...(bagCode ? { bag_code: bagCode } : {}),
          ...(originHubId !== undefined ? { origin_hub_id: originHubId } : {}),
          ...(destinationType ? { destination_type: destinationType } : {}),
          ...(destinationHubId !== undefined
            ? { destination_hub_id: destinationHubId }
            : {}),
          ...(destinationPostOfficeCode
            ? { destination_post_office_code: destinationPostOfficeCode }
            : {}),
          ...(vehicleId !== undefined ? { vehicle_id: vehicleId } : {}),
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: normalizeSecondMileBagPage,
      providesTags: (result) => [
        { type: 'SecondMileBag', id: 'LIST' },
        ...(result?.items ?? []).map((bag) => ({
          type: 'SecondMileBag' as const,
          id: String(bag.id),
        })),
      ],
    }),

    getSecondMileBagById: builder.query<SecondMileBag, number>({
      query: (id) => ({
        url: `/bags/${id}`,
        method: 'GET',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileBag>) =>
        normalizeSecondMileBag(unwrapFirstMileResult(response)),
      providesTags: (_result, _error, id) => [
        { type: 'SecondMileBag', id: String(id) },
      ],
    }),

    createSecondMileBag: builder.mutation<
      SecondMileBag,
      CreateSecondMileBagRequest
    >({
      query: (body) => ({
        url: '/bags',
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileBag>) =>
        normalizeSecondMileBag(unwrapFirstMileResult(response)),
      invalidatesTags: [{ type: 'SecondMileBag', id: 'LIST' }],
    }),

    updateSecondMileBag: builder.mutation<
      SecondMileBag,
      { id: number; body: UpdateSecondMileBagRequest }
    >({
      query: ({ id, body }) => ({
        url: `/bags/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileBag>) =>
        normalizeSecondMileBag(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'SecondMileBag', id: 'LIST' },
        { type: 'SecondMileBag', id: String(id) },
      ],
    }),

    deleteSecondMileBag: builder.mutation<string, number>({
      query: (id) => ({
        url: `/bags/${id}`,
        method: 'DELETE',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<void>) =>
        response?.message || 'Deleted successfully',
      invalidatesTags: (_result, _error, id) => [
        { type: 'SecondMileBag', id: 'LIST' },
        { type: 'SecondMileBag', id: String(id) },
      ],
    }),

    addSecondMileBagOrder: builder.mutation<
      SecondMileBag,
      { id: number; body: AddSecondMileBagOrderRequest }
    >({
      query: ({ id, body }) => ({
        url: `/bags/${id}/orders`,
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileBag>) =>
        normalizeSecondMileBag(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'SecondMileBag', id: 'LIST' },
        { type: 'SecondMileBag', id: String(id) },
      ],
    }),

    removeSecondMileBagOrder: builder.mutation<
      SecondMileBag,
      { id: number; orderCode: string }
    >({
      query: ({ id, orderCode }) => ({
        url: `/bags/${id}/orders/${encodeURIComponent(orderCode)}`,
        method: 'DELETE',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileBag>) =>
        normalizeSecondMileBag(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'SecondMileBag', id: 'LIST' },
        { type: 'SecondMileBag', id: String(id) },
      ],
    }),

    sealSecondMileBag: builder.mutation<SecondMileBag, number>({
      query: (id) => ({
        url: `/bags/${id}/seal`,
        method: 'POST',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileBag>) =>
        normalizeSecondMileBag(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, id) => [
        { type: 'SecondMileBag', id: 'LIST' },
        { type: 'SecondMileBag', id: String(id) },
      ],
    }),

    reopenSecondMileBag: builder.mutation<
      SecondMileBag,
      { id: number; body: ReopenSecondMileBagRequest }
    >({
      query: ({ id, body }) => ({
        url: `/bags/${id}/reopen`,
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileBag>) =>
        normalizeSecondMileBag(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'SecondMileBag', id: 'LIST' },
        { type: 'SecondMileBag', id: String(id) },
      ],
    }),

    getSecondMileBagSuggestions: builder.query<
      SecondMileBagSuggestion[],
      { orderCode: string; originHubId?: number }
    >({
      query: ({ orderCode, originHubId }) => ({
        url: '/bags/suggestions',
        method: 'GET',
        params: {
          order_code: orderCode,
          ...(originHubId !== undefined ? { origin_hub_id: originHubId } : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<SecondMileBagSuggestion[]>
      ) =>
        (unwrapFirstMileResult(response) ?? []).map((item) =>
          normalizeSecondMileBagSuggestion(item)
        ),
    }),

    validateSecondMileBagging: builder.mutation<
      SecondMileBaggingValidation,
      ValidateSecondMileBaggingRequest
    >({
      query: (body) => ({
        url: '/bags/validate',
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<SecondMileBaggingValidation>
      ) =>
        normalizeSecondMileBaggingValidation(unwrapFirstMileResult(response)),
    }),

    autoPlanSecondMileBags: builder.mutation<
      AutoSecondMileBaggingPlan,
      AutoSecondMileBaggingPlanRequest
    >({
      query: (body) => ({
        url: '/bags/auto-plan',
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<AutoSecondMileBaggingPlan>
      ) => normalizeAutoSecondMileBaggingPlan(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, request) =>
        request.execute ? [{ type: 'SecondMileBag', id: 'LIST' }] : [],
    }),

    getSecondMileBaggingKpis: builder.query<
      SecondMileBaggingKpi,
      { originHubId: number; from: string; to: string }
    >({
      query: ({ originHubId, from, to }) => ({
        url: '/bags/kpis',
        method: 'GET',
        params: {
          origin_hub_id: originHubId,
          from,
          to,
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<SecondMileBaggingKpi>
      ) => normalizeSecondMileBaggingKpi(unwrapFirstMileResult(response)),
    }),

    getSecondMileRoutes: builder.query<
      FirstMilePaginatedData<SecondMileRoute>,
      { page?: number; size?: number } & SecondMileRouteListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        routeCode,
        originType,
        originHubId,
        originPostOfficeCode,
        destinationType,
        destinationHubId,
        destinationPostOfficeCode,
        vehicleId,
        status,
      }) => ({
        url: '/routes',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
          ...(routeCode ? { route_code: routeCode } : {}),
          ...(originType ? { origin_type: originType } : {}),
          ...(originHubId !== undefined ? { origin_hub_id: originHubId } : {}),
          ...(originPostOfficeCode
            ? { origin_post_office_code: originPostOfficeCode }
            : {}),
          ...(destinationType ? { destination_type: destinationType } : {}),
          ...(destinationHubId !== undefined
            ? { destination_hub_id: destinationHubId }
            : {}),
          ...(destinationPostOfficeCode
            ? { destination_post_office_code: destinationPostOfficeCode }
            : {}),
          ...(vehicleId !== undefined ? { vehicle_id: vehicleId } : {}),
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: normalizeSecondMileRoutePage,
    }),

    getSecondMileRouteById: builder.query<SecondMileRoute, number>({
      query: (id) => ({
        url: `/routes/${id}`,
        method: 'GET',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileRoute>) =>
        normalizeSecondMileRoute(unwrapFirstMileResult(response)),
    }),

    createSecondMileRoute: builder.mutation<
      SecondMileRoute,
      SecondMileCreateRouteRequest
    >({
      query: (body) => ({
        url: '/routes',
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileRoute>) =>
        normalizeSecondMileRoute(unwrapFirstMileResult(response)),
    }),

    updateSecondMileRoute: builder.mutation<
      SecondMileRoute,
      { id: number; body: SecondMileUpdateRouteRequest }
    >({
      query: ({ id, body }) => ({
        url: `/routes/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<SecondMileRoute>) =>
        normalizeSecondMileRoute(unwrapFirstMileResult(response)),
    }),

    deleteSecondMileRoute: builder.mutation<string, number>({
      query: (id) => ({
        url: `/routes/${id}`,
        method: 'DELETE',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: { message?: string }) =>
        response?.message || 'Deleted successfully',
    }),

    getHandoverManifests: builder.query<
      FirstMilePaginatedData<HandoverManifest>,
      { page?: number; size?: number } & HandoverManifestListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        originPostOfficeCode,
        targetHubId,
        vehicleId,
        status,
      }) => ({
        url: '/handover-manifests',
        method: 'GET',
        params: {
          page,
          size,
          ...(originPostOfficeCode
            ? { origin_post_office_code: originPostOfficeCode }
            : {}),
          ...(targetHubId !== undefined ? { target_hub_id: targetHubId } : {}),
          ...(vehicleId !== undefined ? { vehicle_id: vehicleId } : {}),
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: normalizeHandoverManifestPage,
      providesTags: [{ type: 'HandoverManifest', id: 'LIST' }],
    }),

    getHandoverManifestById: builder.query<HandoverManifest, number>({
      query: (id) => ({
        url: `/handover-manifests/${id}`,
        method: 'GET',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      providesTags: (_result, _error, id) => [
        { type: 'HandoverManifest', id: String(id) },
      ],
    }),

    createHandoverManifest: builder.mutation<
      HandoverManifest,
      CreateHandoverManifestRequest
    >({
      query: (body) => ({
        url: '/handover-manifests',
        method: 'POST',
        body,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: [{ type: 'HandoverManifest', id: 'LIST' }],
    }),

    confirmHandoverManifestOutbound: builder.mutation<HandoverManifest, number>(
      {
        query: (manifestId) => ({
          url: `/handover-manifests/${manifestId}/confirm-outbound`,
          method: 'POST',
        }),
        extraOptions: SECOND_MILE_SERVICE,
        transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
          normalizeHandoverManifest(unwrapFirstMileResult(response)),
        invalidatesTags: (_result, _error, id) => [
          { type: 'HandoverManifest', id: 'LIST' },
          { type: 'HandoverManifest', id: String(id) },
        ],
      }
    ),

    confirmHandoverManifestInbound: builder.mutation<
      HandoverManifest,
      { manifestId: number; orderCodes?: string[] }
    >({
      query: ({ manifestId, orderCodes }) => ({
        url: `/handover-manifests/${manifestId}/confirm-inbound`,
        method: 'POST',
        body: orderCodes?.length ? { order_codes: orderCodes } : undefined,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { manifestId }) => [
        { type: 'HandoverManifest', id: 'LIST' },
        { type: 'HandoverManifest', id: String(manifestId) },
      ],
    }),

    driverCheckinHandoverManifestStart: builder.mutation<
      HandoverManifest,
      { manifestId: number; formData: FormData }
    >({
      query: ({ manifestId, formData }) => ({
        url: `/handover-manifests/${manifestId}/driver-checkin-start`,
        method: 'POST',
        body: formData,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { manifestId }) => [
        { type: 'HandoverManifest', id: 'LIST' },
        { type: 'HandoverManifest', id: String(manifestId) },
      ],
    }),

    driverCheckinHandoverManifestEnd: builder.mutation<
      HandoverManifest,
      { manifestId: number; formData: FormData }
    >({
      query: ({ manifestId, formData }) => ({
        url: `/handover-manifests/${manifestId}/driver-checkin-end`,
        method: 'POST',
        body: formData,
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { manifestId }) => [
        { type: 'HandoverManifest', id: 'LIST' },
        { type: 'HandoverManifest', id: String(manifestId) },
      ],
    }),

    getPostOfficeHandoverManifests: builder.query<
      FirstMilePaginatedData<HandoverManifest>,
      { page?: number; size?: number } & HandoverManifestListFilters
    >({
      query: ({ page = 0, size = 20, postOfficeId, targetHubId, status }) => ({
        url: '/post-office-handover-manifests',
        method: 'GET',
        params: {
          page,
          size,
          ...(postOfficeId !== undefined
            ? { post_office_id: postOfficeId }
            : {}),
          ...(targetHubId !== undefined ? { target_hub_id: targetHubId } : {}),
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: normalizeHandoverManifestPage,
      providesTags: [{ type: 'HandoverManifest', id: 'LIST' }],
    }),

    getPostOfficeHandoverManifestById: builder.query<HandoverManifest, number>({
      query: (id) => ({
        url: `/post-office-handover-manifests/${id}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      providesTags: (_result, _error, id) => [
        { type: 'HandoverManifest', id: String(id) },
      ],
    }),

    createPostOfficeHandoverManifest: builder.mutation<
      HandoverManifest,
      CreatePostOfficeHandoverManifestRequest
    >({
      query: (body) => ({
        url: '/post-office-handover-manifests',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: [{ type: 'HandoverManifest', id: 'LIST' }],
    }),

    scanOutPostOfficeHandoverOrder: builder.mutation<
      HandoverManifest,
      { manifestId: number; body: ScanOutHandoverOrderRequest }
    >({
      query: ({ manifestId, body }) => ({
        url: `/post-office-handover-manifests/${manifestId}/scan-out`,
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { manifestId }) => [
        { type: 'HandoverManifest', id: 'LIST' },
        { type: 'HandoverManifest', id: String(manifestId) },
      ],
    }),

    dispatchPostOfficeHandoverManifest: builder.mutation<
      HandoverManifest,
      { manifestId: number; body?: DispatchPostOfficeHandoverManifestRequest }
    >({
      query: ({ manifestId, body }) => ({
        url: `/post-office-handover-manifests/${manifestId}/dispatch`,
        method: 'POST',
        ...(body ? { body } : {}),
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, { manifestId }) => [
        { type: 'HandoverManifest', id: 'LIST' },
        { type: 'HandoverManifest', id: String(manifestId) },
      ],
    }),

    cancelPostOfficeHandoverManifest: builder.mutation<
      HandoverManifest,
      number
    >({
      query: (manifestId) => ({
        url: `/post-office-handover-manifests/${manifestId}/cancel`,
        method: 'POST',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<HandoverManifest>) =>
        normalizeHandoverManifest(unwrapFirstMileResult(response)),
      invalidatesTags: (_result, _error, id) => [
        { type: 'HandoverManifest', id: 'LIST' },
        { type: 'HandoverManifest', id: String(id) },
      ],
    }),

    getSecondMileOrders: builder.query<
      FirstMilePaginatedData<SecondMileOrder>,
      { page?: number; size?: number } & SecondMileOrderListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        orderCode,
        originPostOfficeCode,
        status,
      }) => ({
        url: '/orders',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
          ...(orderCode ? { order_code: orderCode } : {}),
          ...(originPostOfficeCode
            ? { origin_post_office_code: originPostOfficeCode }
            : {}),
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: normalizeSecondMileOrderPage,
    }),

    getPostOffices: builder.query<
      FirstMilePaginatedData<PostOffice>,
      { page?: number; size?: number } & PostOfficeListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        code,
        name,
        provinceCode,
        wardCode,
        status,
        hasLocation,
        minServiceRadiusM,
        maxServiceRadiusM,
        minDailyCapacity,
        maxDailyCapacity,
        minCurrentLoad,
        maxCurrentLoad,
        minPriority,
        maxPriority,
        hubId,
      }) => ({
        url: '/post-offices',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
          ...(code ? { code } : {}),
          ...(name ? { name } : {}),
          ...(provinceCode ? { province_code: provinceCode } : {}),
          ...(wardCode ? { ward_code: wardCode } : {}),
          ...(status ? { status } : {}),
          ...(hasLocation !== undefined ? { has_location: hasLocation } : {}),
          ...(minServiceRadiusM !== undefined
            ? { min_service_radius_m: minServiceRadiusM }
            : {}),
          ...(maxServiceRadiusM !== undefined
            ? { max_service_radius_m: maxServiceRadiusM }
            : {}),
          ...(minDailyCapacity !== undefined
            ? { min_daily_capacity: minDailyCapacity }
            : {}),
          ...(maxDailyCapacity !== undefined
            ? { max_daily_capacity: maxDailyCapacity }
            : {}),
          ...(minCurrentLoad !== undefined
            ? { min_current_load: minCurrentLoad }
            : {}),
          ...(maxCurrentLoad !== undefined
            ? { max_current_load: maxCurrentLoad }
            : {}),
          ...(minPriority !== undefined ? { min_priority: minPriority } : {}),
          ...(maxPriority !== undefined ? { max_priority: maxPriority } : {}),
          ...(hubId !== undefined ? { hub_id: hubId } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResult<PostOffice>,
    }),

    getPostOfficeById: builder.query<PostOffice, number>({
      query: (id) => ({
        url: `/post-offices/${id}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<PostOffice>,
    }),

    createPostOffice: builder.mutation<PostOffice, CreatePostOfficeRequest>({
      query: (body) => ({
        url: '/post-offices',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<PostOffice>,
    }),

    updatePostOffice: builder.mutation<
      PostOffice,
      { id: number; body: UpdatePostOfficeRequest }
    >({
      query: ({ id, body }) => ({
        url: `/post-offices/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<PostOffice>,
    }),

    deletePostOffice: builder.mutation<string, number>({
      query: (id) => ({
        url: `/post-offices/${id}`,
        method: 'DELETE',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: { message?: string }) =>
        response?.message || 'Deleted successfully',
    }),

    geocodePostOfficeById: builder.mutation<PostOffice, number>({
      query: (id) => ({
        url: `/post-offices/${id}/location/geocode`,
        method: 'PUT',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<PostOffice>,
    }),

    geocodeNullPostOffices: builder.mutation<
      PostOfficeGeocodeBatchResponse,
      number
    >({
      query: (batch = 50) => ({
        url: '/post-offices/location/geocode-null',
        method: 'PUT',
        params: { batch },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<PostOfficeGeocodeBatchResponse>,
    }),

    exportPostOfficeTemplate: builder.query<Blob, void>({
      query: () => ({
        url: '/post-offices/template',
        method: 'GET',
        responseHandler: (response) => response.blob(),
      }),
      extraOptions: FIRST_MILE_SERVICE,
    }),

    validatePostOfficeImport: builder.mutation<
      ValidateImportFileResponse<PostOfficeImportItem>,
      FormData
    >({
      query: (formData) => ({
        url: '/post-offices/validate',
        method: 'POST',
        body: formData,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<
        ValidateImportFileResponse<PostOfficeImportItem>
      >,
    }),

    importPostOffices: builder.mutation<ImportHistory, FormData>({
      query: (formData) => ({
        url: '/post-offices/import',
        method: 'POST',
        body: formData,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<ImportHistory>,
    }),

    getFirstMileVehicles: builder.query<
      FirstMilePaginatedData<Vehicle>,
      { page?: number; size?: number; keyword?: string }
    >({
      query: ({ page = 0, size = 20, keyword }) => ({
        url: '/vehicles',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResult<Vehicle>,
    }),

    getVehicleById: builder.query<Vehicle, number>({
      query: (id) => ({
        url: `/vehicles/${id}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Vehicle>,
    }),

    createFirstMileVehicle: builder.mutation<Vehicle, CreateVehicleRequest>({
      query: (body) => ({
        url: '/vehicles',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Vehicle>,
    }),

    updateFirstMileVehicle: builder.mutation<
      Vehicle,
      { id: number; body: UpdateVehicleRequest }
    >({
      query: ({ id, body }) => ({
        url: `/vehicles/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Vehicle>,
    }),

    deleteVehicle: builder.mutation<string, number>({
      query: (id) => ({
        url: `/vehicles/${id}`,
        method: 'DELETE',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: { message?: string }) =>
        response?.message || 'Deleted successfully',
    }),

    exportVehicleTemplate: builder.query<Blob, void>({
      query: () => ({
        url: '/vehicles/template',
        method: 'GET',
        responseHandler: (response) => response.blob(),
      }),
      extraOptions: FIRST_MILE_SERVICE,
    }),

    validateVehicleImport: builder.mutation<
      ValidateImportFileResponse<VehicleImportItem>,
      FormData
    >({
      query: (formData) => ({
        url: '/vehicles/validate',
        method: 'POST',
        body: formData,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<
        ValidateImportFileResponse<VehicleImportItem>
      >,
    }),

    importVehicles: builder.mutation<ImportHistory, FormData>({
      query: (formData) => ({
        url: '/vehicles/import',
        method: 'POST',
        body: formData,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<ImportHistory>,
    }),

    getProductTypes: builder.query<
      FirstMilePaginatedData<ProductType>,
      { page?: number; size?: number; keyword?: string }
    >({
      query: ({ page = 0, size = 20, keyword }) => ({
        url: '/product-types',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
        },
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMilePageResult<ProductType>,
    }),

    getProductTypeById: builder.query<ProductType, number>({
      query: (id) => ({
        url: `/product-types/${id}`,
        method: 'GET',
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResult<ProductType>,
    }),

    createProductType: builder.mutation<ProductType, CreateProductTypeRequest>({
      query: (body) => ({
        url: '/product-types',
        method: 'POST',
        body,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResult<ProductType>,
    }),

    updateProductType: builder.mutation<
      ProductType,
      { id: number; body: UpdateProductTypeRequest }
    >({
      query: ({ id, body }) => ({
        url: `/product-types/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResult<ProductType>,
    }),

    deleteProductType: builder.mutation<string, number>({
      query: (id) => ({
        url: `/product-types/${id}`,
        method: 'DELETE',
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: (response: { message?: string }) =>
        response?.message || 'Deleted successfully',
    }),

    getImportHistories: builder.query<
      FirstMilePaginatedData<ImportHistory>,
      { page?: number; size?: number; type?: ImportType }
    >({
      query: ({ page = 0, size = 20, type }) => ({
        url: '/import-history',
        method: 'GET',
        params: {
          page,
          size,
          ...(type ? { type } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResult<ImportHistory>,
    }),

    getImportHistoryById: builder.query<ImportHistory, number>({
      query: (id) => ({
        url: `/import-history/${id}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<ImportHistory>,
    }),

    getOrderImportHistories: builder.query<
      FirstMilePaginatedData<ImportHistory>,
      { page?: number; size?: number }
    >({
      query: ({ page = 0, size = 20 }) => ({
        url: '/import-history',
        method: 'GET',
        params: {
          page,
          size,
        },
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMilePageResult<ImportHistory>,
    }),

    getOrderImportHistoryById: builder.query<ImportHistory, number>({
      query: (id) => ({
        url: `/import-history/${id}`,
        method: 'GET',
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<ImportHistory>,
    }),

    getProvinces: builder.query<
      FirstMilePaginatedData<Province>,
      { page?: number; size?: number }
    >({
      query: ({ page = 0, size = 20 }) => ({
        url: '/locations/provinces',
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResult<Province>,
    }),

    getWardsByProvinceCode: builder.query<
      FirstMilePaginatedData<Ward>,
      { provinceCode: string; page?: number; size?: number }
    >({
      query: ({ provinceCode, page = 0, size = 20 }) => ({
        url: `/locations/provinces/${provinceCode}/wards`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResult<Ward>,
    }),

    geocodeAddress: builder.mutation<
      GeocodeAddressResponse,
      GeocodeAddressRequest
    >({
      query: (body) => ({
        url: '/locations/geocode',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<GeocodeAddressResponse>,
    }),

    getFirstMileOrders: builder.query<
      FirstMilePaginatedData<FirstMileOrderDetail>,
      { page?: number; size?: number } & FirstMileOrderListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        orderCode,
        customerOrderCode,
        senderPhone,
        receiverPhone,
        originPostOfficeCode,
        destinationPostOfficeCode,
        status,
        isConfirm,
        createdFrom,
        createdTo,
        pickupFrom,
        pickupTo,
      }) => ({
        url: '/orders',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
          ...(orderCode ? { order_code: orderCode } : {}),
          ...(customerOrderCode
            ? { customer_order_code: customerOrderCode }
            : {}),
          ...(senderPhone ? { sender_phone: senderPhone } : {}),
          ...(receiverPhone ? { receiver_phone: receiverPhone } : {}),
          ...(originPostOfficeCode
            ? { origin_post_office_code: originPostOfficeCode }
            : {}),
          ...(destinationPostOfficeCode
            ? { destination_post_office_code: destinationPostOfficeCode }
            : {}),
          ...(status ? { status } : {}),
          ...(isConfirm !== undefined ? { is_confirm: isConfirm } : {}),
          ...(createdFrom ? { created_from: createdFrom } : {}),
          ...(createdTo ? { created_to: createdTo } : {}),
          ...(pickupFrom ? { pickup_from: pickupFrom } : {}),
          ...(pickupTo ? { pickup_to: pickupTo } : {}),
        },
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMilePageResultOrRaw<FirstMileOrderDetail>,
    }),

    getOrderById: builder.query<FirstMileOrderDetail, number>({
      query: (orderId) => ({
        url: `/orders/${orderId}`,
        method: 'GET',
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    getOrderTimeline: builder.query<FirstMileOrderTimelineItem[], number>({
      query: (orderId) => ({
        url: `/orders/${orderId}/timeline`,
        method: 'GET',
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<
        FirstMileOrderTimelineItem[]
      >,
    }),

    createFirstMileOrder: builder.mutation<
      FirstMileOrderDetail,
      CreateOrderRequest
    >({
      query: (body) => ({
        url: '/orders',
        method: 'POST',
        body,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    updateFirstMileOrder: builder.mutation<
      FirstMileOrderDetail,
      { id: number; body: UpdateOrderRequest }
    >({
      query: ({ id, body }) => ({
        url: `/orders/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    cancelFirstMileOrder: builder.mutation<
      FirstMileOrderDetail,
      { id: number; body: CancelOrderRequest }
    >({
      query: ({ id, body }) => ({
        url: `/orders/${id}/cancel`,
        method: 'PUT',
        body,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    confirmOrder: builder.mutation<OrderConfirmationResponse, number>({
      query: (orderId) => ({
        url: `/orders/${orderId}/confirm`,
        method: 'POST',
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<OrderConfirmationResponse>,
    }),

    initiateOrderPayment: builder.mutation<
      OrderPaymentInitResponse,
      { orderId: number; body?: InitiateOrderPaymentRequest }
    >({
      query: ({ orderId, body }) => ({
        url: `/orders/${orderId}/payment/initiate`,
        method: 'POST',
        ...(body ? { body } : {}),
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<OrderPaymentInitResponse>,
    }),

    confirmOrderPayment: builder.mutation<
      OrderPaymentConfirmResponse,
      { orderId: number; body: ConfirmOrderPaymentRequest }
    >({
      query: ({ orderId, body }) => ({
        url: `/orders/${orderId}/payment/confirm`,
        method: 'POST',
        body,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse:
        unwrapFirstMileResultOrRaw<OrderPaymentConfirmResponse>,
    }),

    getDropOffPostOfficeSuggestions: builder.query<
      OrderDropOffPostOfficeSuggestion[],
      { orderId: number; limit?: number }
    >({
      query: ({ orderId, limit = 5 }) => ({
        url: `/orders/${orderId}/drop-off-post-office-suggestions`,
        method: 'GET',
        params: {
          limit,
        },
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<
        OrderDropOffPostOfficeSuggestion[]
      >,
    }),

    confirmDropOffOrderAtPostOffice: builder.mutation<
      OrderConfirmationResponse,
      { orderId: number; postOfficeId: number }
    >({
      query: ({ orderId, postOfficeId }) => ({
        url: `/orders/${orderId}/drop-off-confirm`,
        method: 'POST',
        body: {
          post_office_id: postOfficeId,
        },
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<OrderConfirmationResponse>,
    }),

    getActiveCouriersByPostOffice: builder.query<PostOfficeStaff[], number>({
      query: (postOfficeId) => ({
        url: `/post-office-staffs/post-offices/${postOfficeId}/couriers`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PostOfficeStaff[]>,
    }),

    getPostOfficeStaffById: builder.query<PostOfficeStaff, number>({
      query: (id) => ({
        url: `/post-office-staffs/${id}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PostOfficeStaff>,
    }),

    getAssignablePostOfficeStaffs: builder.query<
      PostOfficeStaff[],
      { role: PostOfficeStaffRole; keyword?: string }
    >({
      query: ({ role, keyword }) => ({
        url: '/post-office-staffs/assignable',
        method: 'GET',
        params: {
          role,
          ...(keyword ? { keyword } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PostOfficeStaff[]>,
    }),

    getPostOfficeStaffAssignmentsByPostOffice: builder.query<
      PostOfficeStaffAssignment[],
      { postOfficeId: number; role?: PostOfficeStaffRole }
    >({
      query: ({ postOfficeId, role }) => ({
        url: `/post-office-staffs/post-offices/${postOfficeId}/assignments`,
        method: 'GET',
        params: {
          ...(role ? { role } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (response: FirstMileApiResponse<unknown[]>) =>
        unwrapFirstMileResult<unknown[]>(response).map((item) =>
          normalizePostOfficeStaffAssignment(item)
        ),
    }),

    assignCourierToPostOffice: builder.mutation<
      PostOfficeStaffAssignment,
      { staffId: number; postOfficeId: number }
    >({
      query: ({ staffId, postOfficeId }) => ({
        url: `/post-office-staffs/${staffId}/assignments/courier/post-offices/${postOfficeId}`,
        method: 'PUT',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<PostOfficeStaffAssignment>
      ) => normalizePostOfficeStaffAssignment(unwrapFirstMileResult(response)),
    }),

    assignManagerToPostOffice: builder.mutation<
      PostOfficeStaffAssignment,
      { staffId: number; postOfficeId: number }
    >({
      query: ({ staffId, postOfficeId }) => ({
        url: `/post-office-staffs/${staffId}/assignments/manager/post-offices/${postOfficeId}`,
        method: 'PUT',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<PostOfficeStaffAssignment>
      ) => normalizePostOfficeStaffAssignment(unwrapFirstMileResult(response)),
    }),

    updateCourierAssignmentDetails: builder.mutation<
      PostOfficeStaffAssignment,
      { assignmentId: number; body: UpdatePostOfficeStaffAssignmentRequest }
    >({
      query: ({ assignmentId, body }) => ({
        url: `/post-office-staffs/assignments/${assignmentId}/courier-details`,
        method: 'PUT',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<PostOfficeStaffAssignment>
      ) => normalizePostOfficeStaffAssignment(unwrapFirstMileResult(response)),
    }),

    unassignPostOfficeStaffAssignment: builder.mutation<
      PostOfficeStaffAssignment,
      number
    >({
      query: (assignmentId) => ({
        url: `/post-office-staffs/assignments/${assignmentId}/unassign`,
        method: 'PUT',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<PostOfficeStaffAssignment>
      ) => normalizePostOfficeStaffAssignment(unwrapFirstMileResult(response)),
    }),

    optimizePickupPlan: builder.mutation<
      PickupOptimizationResponse,
      OptimizePickupPlanRequest
    >({
      query: (body) => ({
        url: '/pickup-optimization/plan',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PickupOptimizationResponse>,
    }),

    autoAssignPickupPlan: builder.mutation<
      PickupAssignmentResponse,
      AutoAssignPickupPlanRequest
    >({
      query: (body) => ({
        url: '/pickup-optimization/auto-assign',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PickupAssignmentResponse>,
    }),

    manualAssignPickupOrders: builder.mutation<
      PickupAssignmentResponse,
      ManualAssignPickupOrdersRequest
    >({
      query: (body) => ({
        url: '/pickup-optimization/manual-assign',
        method: 'POST',
        params: body.force_assign ? { force_assign: true } : undefined,
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PickupAssignmentResponse>,
    }),

    getPickupTrackingOverview: builder.query<
      PickupTrackingOverviewResponse,
      { tripDate?: string; postOfficeId?: number; courierStaffId?: number }
    >({
      query: ({ tripDate, postOfficeId, courierStaffId }) => ({
        url: '/pickup-tracking/overview',
        method: 'GET',
        params: {
          ...(tripDate ? { trip_date: tripDate } : {}),
          ...(postOfficeId ? { post_office_id: postOfficeId } : {}),
          ...(courierStaffId ? { courier_staff_id: courierStaffId } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse:
        unwrapFirstMileResultOrRaw<PickupTrackingOverviewResponse>,
    }),

    pickupCheckinOrder: builder.mutation<
      PickupCheckinResponse,
      { orderId: number; formData: FormData }
    >({
      query: ({ orderId, formData }) => ({
        url: `/pickup-tracking/orders/${orderId}/checkin`,
        method: 'POST',
        body: formData,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PickupCheckinResponse>,
    }),

    getPickupCheckinDetail: builder.query<PickupCheckinDetailResponse, number>({
      query: (orderId) => ({
        url: `/pickup-tracking/orders/${orderId}/checkin`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse:
        unwrapFirstMileResultOrRaw<PickupCheckinDetailResponse>,
    }),

    completePickupTrip: builder.mutation<PickupTripLifecycleResponse, number>({
      query: (tripId) => ({
        url: `/pickup-tracking/trips/${tripId}/complete`,
        method: 'POST',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse:
        unwrapFirstMileResultOrRaw<PickupTripLifecycleResponse>,
    }),

    returnPickupTripToPostOffice: builder.mutation<
      PickupTripLifecycleResponse,
      number
    >({
      query: (tripId) => ({
        url: `/pickup-tracking/trips/${tripId}/return-to-post-office`,
        method: 'POST',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse:
        unwrapFirstMileResultOrRaw<PickupTripLifecycleResponse>,
    }),

    confirmPickupTripPostOfficeInbound: builder.mutation<
      PickupTripLifecycleResponse,
      { tripId: number; body: ConfirmPostOfficeInboundRequest }
    >({
      query: ({ tripId, body }) => ({
        url: `/pickup-tracking/trips/${tripId}/confirm-post-office-inbound`,
        method: 'POST',
        body: {
          order_codes: body.orderCodes,
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse:
        unwrapFirstMileResultOrRaw<PickupTripLifecycleResponse>,
    }),

    exportOrderTemplate: builder.query<Blob, void>({
      query: () => ({
        url: '/orders/template',
        method: 'GET',
        responseHandler: (response) => response.blob(),
      }),
      extraOptions: TMS_ORDER_SERVICE,
    }),

    validateOrderImport: builder.mutation<
      ValidateImportFileResponse<OrderImportItem>,
      FormData
    >({
      query: (formData) => ({
        url: '/orders/validate',
        method: 'POST',
        body: formData,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<
        ValidateImportFileResponse<OrderImportItem>
      >,
    }),

    importOrders: builder.mutation<ImportHistory, FormData>({
      query: (formData) => ({
        url: '/orders/import',
        method: 'POST',
        body: formData,
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<ImportHistory>,
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetHubsQuery,
  useGetHubByIdQuery,
  useGetSecondMileHubStaffAssignmentsQuery,
  useGetSecondMileAssignableStaffsQuery,
  useAssignSecondMileStaffToHubMutation,
  useUnassignSecondMileHubStaffAssignmentMutation,
  useGetHubPostOfficesQuery,
  useAssignPostOfficeToHubMutation,
  useRemovePostOfficeFromHubMutation,
  useLazyExportHubTemplateQuery,
  useValidateHubImportMutation,
  useImportHubsMutation,
  useCreateHubMutation,
  useUpdateHubMutation,
  useDeleteHubMutation,
  useUploadHubImageMutation,
  useGetSecondMileVehiclesQuery,
  useGetSecondMileVehicleByIdQuery,
  useCreateSecondMileVehicleMutation,
  useUpdateSecondMileVehicleMutation,
  useDeleteSecondMileVehicleMutation,
  useUploadSecondMileVehicleImageMutation,
  useLazyExportSecondMileVehicleTemplateQuery,
  useValidateSecondMileVehicleImportMutation,
  useImportSecondMileVehiclesMutation,
  useGetSecondMileBagsQuery,
  useGetSecondMileBagByIdQuery,
  useCreateSecondMileBagMutation,
  useUpdateSecondMileBagMutation,
  useDeleteSecondMileBagMutation,
  useAddSecondMileBagOrderMutation,
  useRemoveSecondMileBagOrderMutation,
  useSealSecondMileBagMutation,
  useReopenSecondMileBagMutation,
  useGetSecondMileBagSuggestionsQuery,
  useValidateSecondMileBaggingMutation,
  useAutoPlanSecondMileBagsMutation,
  useGetSecondMileBaggingKpisQuery,
  useGetSecondMileRoutesQuery,
  useGetSecondMileRouteByIdQuery,
  useCreateSecondMileRouteMutation,
  useUpdateSecondMileRouteMutation,
  useDeleteSecondMileRouteMutation,
  useGetHandoverManifestsQuery,
  useGetHandoverManifestByIdQuery,
  useCreateHandoverManifestMutation,
  useConfirmHandoverManifestOutboundMutation,
  useConfirmHandoverManifestInboundMutation,
  useDriverCheckinHandoverManifestStartMutation,
  useDriverCheckinHandoverManifestEndMutation,
  useGetPostOfficeHandoverManifestsQuery,
  useGetPostOfficeHandoverManifestByIdQuery,
  useCreatePostOfficeHandoverManifestMutation,
  useScanOutPostOfficeHandoverOrderMutation,
  useDispatchPostOfficeHandoverManifestMutation,
  useCancelPostOfficeHandoverManifestMutation,
  useGetSecondMileOrdersQuery,
  useGetPostOfficesQuery,
  useGetPostOfficeByIdQuery,
  useCreatePostOfficeMutation,
  useUpdatePostOfficeMutation,
  useDeletePostOfficeMutation,
  useGeocodePostOfficeByIdMutation,
  useGeocodeNullPostOfficesMutation,
  useLazyExportPostOfficeTemplateQuery,
  useValidatePostOfficeImportMutation,
  useImportPostOfficesMutation,
  useGetFirstMileVehiclesQuery,
  useGetVehicleByIdQuery,
  useCreateFirstMileVehicleMutation,
  useUpdateFirstMileVehicleMutation,
  useDeleteVehicleMutation,
  useLazyExportVehicleTemplateQuery,
  useValidateVehicleImportMutation,
  useImportVehiclesMutation,
  useGetProductTypesQuery,
  useGetProductTypeByIdQuery,
  useCreateProductTypeMutation,
  useUpdateProductTypeMutation,
  useDeleteProductTypeMutation,
  useGetImportHistoriesQuery,
  useGetImportHistoryByIdQuery,
  useGetOrderImportHistoriesQuery,
  useGetOrderImportHistoryByIdQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useLazyGetWardsByProvinceCodeQuery,
  useGeocodeAddressMutation,
  useGetFirstMileOrdersQuery,
  useGetOrderByIdQuery,
  useLazyGetOrderByIdQuery,
  useLazyGetOrderTimelineQuery,
  useCreateFirstMileOrderMutation,
  useUpdateFirstMileOrderMutation,
  useCancelFirstMileOrderMutation,
  useConfirmOrderMutation,
  useInitiateOrderPaymentMutation,
  useConfirmOrderPaymentMutation,
  useGetDropOffPostOfficeSuggestionsQuery,
  useLazyGetDropOffPostOfficeSuggestionsQuery,
  useConfirmDropOffOrderAtPostOfficeMutation,
  useGetActiveCouriersByPostOfficeQuery,
  useGetPostOfficeStaffByIdQuery,
  useGetAssignablePostOfficeStaffsQuery,
  useGetPostOfficeStaffAssignmentsByPostOfficeQuery,
  useAssignCourierToPostOfficeMutation,
  useAssignManagerToPostOfficeMutation,
  useUpdateCourierAssignmentDetailsMutation,
  useUnassignPostOfficeStaffAssignmentMutation,
  useOptimizePickupPlanMutation,
  useAutoAssignPickupPlanMutation,
  useManualAssignPickupOrdersMutation,
  useGetPickupTrackingOverviewQuery,
  useLazyGetPickupCheckinDetailQuery,
  useCompletePickupTripMutation,
  useReturnPickupTripToPostOfficeMutation,
  useConfirmPickupTripPostOfficeInboundMutation,
  usePickupCheckinOrderMutation,
  useLazyExportOrderTemplateQuery,
  useValidateOrderImportMutation,
  useImportOrdersMutation,
} = firstMileApi;

// TMS-local aliases — keep page imports stable without occupying shared WMS endpoint slots.
export const useGetOrdersQuery = useGetFirstMileOrdersQuery;
export const useCreateOrderMutation = useCreateFirstMileOrderMutation;
export const useUpdateOrderMutation = useUpdateFirstMileOrderMutation;
export const useCancelOrderMutation = useCancelFirstMileOrderMutation;
export const useGetVehiclesQuery = useGetFirstMileVehiclesQuery;
export const useCreateVehicleMutation = useCreateFirstMileVehicleMutation;
export const useUpdateVehicleMutation = useUpdateFirstMileVehicleMutation;
