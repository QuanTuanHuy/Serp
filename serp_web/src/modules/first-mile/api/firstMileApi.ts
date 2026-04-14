/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  CreateVehicleRequest,
  CreatePostOfficeRequest,
  CreateProductTypeRequest,
  FirstMileOrderDetail,
  FirstMileOrderListFilters,
  FirstMilePaginatedData,
  GeocodeAddressRequest,
  GeocodeAddressResponse,
  ImportHistory,
  ImportType,
  OrderImportItem,
  PostOffice,
  PostOfficeGeocodeBatchResponse,
  PostOfficeImportItem,
  PostOfficeListFilters,
  ProductType,
  Province,
  UpdatePostOfficeRequest,
  UpdateProductTypeRequest,
  ValidateImportFileResponse,
  Vehicle,
  VehicleImportItem,
  UpdateVehicleRequest,
  Ward,
} from '../types';
import {
  unwrapFirstMilePageResult,
  unwrapFirstMilePageResultOrRaw,
  unwrapFirstMileResult,
  unwrapFirstMileResultOrRaw,
} from './transforms';

const FIRST_MILE_SERVICE = { service: 'first-mile' as const };

export const firstMileApi = api.injectEndpoints({
  endpoints: (builder) => ({
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
  useLazyExportOrderTemplateQuery,
  useValidateOrderImportMutation,
  useImportOrdersMutation,
} = firstMileApi;
