/**
 * Author: Nguyễn Thế Anh
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
  FirstMileOrderDetail,
  FirstMileOrderListFilters,
  FirstMilePaginatedData,
  GeocodeAddressRequest,
  GeocodeAddressResponse,
  Hub,
  HubListFilters,
  HubPostOfficeMapping,
  ImportHistory,
  ImportType,
  ManualAssignPickupOrdersRequest,
  OptimizePickupPlanRequest,
  OrderConfirmationResponse,
  OrderDropOffPostOfficeSuggestion,
  OrderImportItem,
  PostOffice,
  PostOfficeGeocodeBatchResponse,
  PostOfficeStaff,
  PostOfficeImportItem,
  PostOfficeListFilters,
  PickupAssignmentResponse,
  PickupCheckinResponse,
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
  SecondMileRoute,
  SecondMileRouteListFilters,
  SecondMileCreateRouteRequest,
  SecondMileUpdateRouteRequest,
  Vehicle,
  VehicleImportItem,
  UpdateVehicleRequest,
  Ward,
} from '../types';
import {
  normalizeSecondMileHubImportHistory,
  unwrapFirstMilePageResult,
  unwrapFirstMilePageResultOrRaw,
  unwrapFirstMileResult,
  unwrapFirstMileResultOrRaw,
} from './transforms';

const FIRST_MILE_SERVICE = { service: 'first-mile' as const };
const SECOND_MILE_SERVICE = { service: 'second-mile' as const };

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
      transformResponse: unwrapFirstMilePageResult<HubPostOfficeMapping>,
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
      transformResponse: unwrapFirstMileResult<HubPostOfficeMapping>,
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

    updateHub: builder.mutation<
      Hub,
      { id: number; body: UpdateHubRequest }
    >({
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
      transformResponse: unwrapFirstMilePageResult<SecondMileVehicle>,
    }),

    getSecondMileVehicleById: builder.query<SecondMileVehicle, number>({
      query: (id) => ({
        url: `/vehicles/${id}`,
        method: 'GET',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<SecondMileVehicle>,
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
      transformResponse: unwrapFirstMileResult<SecondMileVehicle>,
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
      transformResponse: unwrapFirstMileResult<SecondMileVehicle>,
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
      transformResponse: unwrapFirstMileResult<SecondMileVehicle>,
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

    getSecondMileRoutes: builder.query<
      FirstMilePaginatedData<SecondMileRoute>,
      { page?: number; size?: number } & SecondMileRouteListFilters
    >({
      query: ({
        page = 0,
        size = 20,
        keyword,
        routeCode,
        originHubId,
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
      transformResponse: unwrapFirstMilePageResult<SecondMileRoute>,
    }),

    getSecondMileRouteById: builder.query<SecondMileRoute, number>({
      query: (id) => ({
        url: `/routes/${id}`,
        method: 'GET',
      }),
      extraOptions: SECOND_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<SecondMileRoute>,
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
      transformResponse: unwrapFirstMileResult<SecondMileRoute>,
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
      transformResponse: unwrapFirstMileResult<SecondMileRoute>,
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

    getVehicles: builder.query<
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

    createVehicle: builder.mutation<Vehicle, CreateVehicleRequest>({
      query: (body) => ({
        url: '/vehicles',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<Vehicle>,
    }),

    updateVehicle: builder.mutation<
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
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResult<ProductType>,
    }),

    getProductTypeById: builder.query<ProductType, number>({
      query: (id) => ({
        url: `/product-types/${id}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<ProductType>,
    }),

    createProductType: builder.mutation<ProductType, CreateProductTypeRequest>({
      query: (body) => ({
        url: '/product-types',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
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
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResult<ProductType>,
    }),

    deleteProductType: builder.mutation<string, number>({
      query: (id) => ({
        url: `/product-types/${id}`,
        method: 'DELETE',
      }),
      extraOptions: FIRST_MILE_SERVICE,
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
      transformResponse: unwrapFirstMileResult<ImportHistory>,
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

    getOrders: builder.query<
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
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMilePageResultOrRaw<FirstMileOrderDetail>,
    }),

    getOrderById: builder.query<FirstMileOrderDetail, number>({
      query: (orderId) => ({
        url: `/orders/${orderId}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    createOrder: builder.mutation<FirstMileOrderDetail, CreateOrderRequest>({
      query: (body) => ({
        url: '/orders',
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    updateOrder: builder.mutation<
      FirstMileOrderDetail,
      { id: number; body: UpdateOrderRequest }
    >({
      query: ({ id, body }) => ({
        url: `/orders/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    cancelOrder: builder.mutation<
      FirstMileOrderDetail,
      { id: number; body: CancelOrderRequest }
    >({
      query: ({ id, body }) => ({
        url: `/orders/${id}/cancel`,
        method: 'PUT',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<FirstMileOrderDetail>,
    }),

    confirmOrder: builder.mutation<OrderConfirmationResponse, number>({
      query: (orderId) => ({
        url: `/orders/${orderId}/confirm`,
        method: 'POST',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<OrderConfirmationResponse>,
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
      extraOptions: FIRST_MILE_SERVICE,
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
      extraOptions: FIRST_MILE_SERVICE,
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
        url: `/orders/${orderId}/pickup-checkin`,
        method: 'POST',
        body: formData,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<PickupCheckinResponse>,
    }),

    exportOrderTemplate: builder.query<Blob, void>({
      query: () => ({
        url: '/orders/template',
        method: 'GET',
        responseHandler: (response) => response.blob(),
      }),
      extraOptions: FIRST_MILE_SERVICE,
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
      extraOptions: FIRST_MILE_SERVICE,
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
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: unwrapFirstMileResultOrRaw<ImportHistory>,
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetHubsQuery,
  useGetHubByIdQuery,
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
  useGetSecondMileRoutesQuery,
  useGetSecondMileRouteByIdQuery,
  useCreateSecondMileRouteMutation,
  useUpdateSecondMileRouteMutation,
  useDeleteSecondMileRouteMutation,
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
  useGetVehiclesQuery,
  useGetVehicleByIdQuery,
  useCreateVehicleMutation,
  useUpdateVehicleMutation,
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
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useLazyGetWardsByProvinceCodeQuery,
  useGeocodeAddressMutation,
  useGetOrdersQuery,
  useGetOrderByIdQuery,
  useLazyGetOrderByIdQuery,
  useCreateOrderMutation,
  useUpdateOrderMutation,
  useCancelOrderMutation,
  useConfirmOrderMutation,
  useGetDropOffPostOfficeSuggestionsQuery,
  useLazyGetDropOffPostOfficeSuggestionsQuery,
  useConfirmDropOffOrderAtPostOfficeMutation,
  useGetActiveCouriersByPostOfficeQuery,
  useGetPostOfficeStaffByIdQuery,
  useOptimizePickupPlanMutation,
  useAutoAssignPickupPlanMutation,
  useManualAssignPickupOrdersMutation,
  useGetPickupTrackingOverviewQuery,
  usePickupCheckinOrderMutation,
  useLazyExportOrderTemplateQuery,
  useValidateOrderImportMutation,
  useImportOrdersMutation,
} = firstMileApi;
