/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  CreatePostOfficeRequest,
  CreateProductTypeRequest,
  FirstMilePaginatedData,
  GeocodeAddressRequest,
  GeocodeAddressResponse,
  ImportHistory,
  ImportType,
  OrderImportItem,
  PostOffice,
  PostOfficeGeocodeBatchResponse,
  PostOfficeImportItem,
  ProductType,
  Province,
  UpdatePostOfficeRequest,
  UpdateProductTypeRequest,
  ValidateImportFileResponse,
  Ward,
} from '../types';
import { unwrapFirstMilePageResult, unwrapFirstMileResult } from './transforms';

const FIRST_MILE_SERVICE = { service: 'first-mile' as const };

export const firstMileApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPostOffices: builder.query<
      FirstMilePaginatedData<PostOffice>,
      { page?: number; size?: number; keyword?: string }
    >({
      query: ({ page = 0, size = 20, keyword }) => ({
        url: '/post-offices',
        method: 'GET',
        params: {
          page,
          size,
          ...(keyword ? { keyword } : {}),
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
      transformResponse: unwrapFirstMileResult<
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
      transformResponse: unwrapFirstMileResult<ImportHistory>,
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
      transformResponse: unwrapFirstMileResult<
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
      transformResponse: unwrapFirstMileResult<ImportHistory>,
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
  useGetProductTypesQuery,
  useGetProductTypeByIdQuery,
  useCreateProductTypeMutation,
  useUpdateProductTypeMutation,
  useDeleteProductTypeMutation,
  useGetImportHistoriesQuery,
  useGetImportHistoryByIdQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useGeocodeAddressMutation,
  useLazyExportOrderTemplateQuery,
  useValidateOrderImportMutation,
  useImportOrdersMutation,
} = firstMileApi;
