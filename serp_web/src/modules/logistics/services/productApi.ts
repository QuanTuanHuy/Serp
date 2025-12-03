/**
 * Logistics Module - Product API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Product CRUD operations
 */

import { logisticsApi } from './api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type {
  Product,
  CreateProductRequest,
  UpdateProductRequest,
  ProductFilters,
} from '../types';

export interface ProductsResponse {
  items: Product[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const productApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get products
    getProducts: builder.query<ProductsResponse, ProductFilters | void>({
      query: (params) => ({
        url: '/logistics/api/v1/product/search',
        method: 'GET',
        params: {
          page: params?.page || 1,
          size: params?.size || 10,
          sortBy: params?.sortBy || 'createdStamp',
          sortDirection: params?.sortDirection || 'desc',
          query: params?.query || '',
          ...(params?.categoryId && { categoryId: params.categoryId }),
          ...(params?.statusId && { statusId: params.statusId }),
        },
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          const pageData = response.data;
          return {
            items: pageData.items || [],
            totalItems: pageData.totalItems || 0,
            totalPages: pageData.totalPages || 0,
            currentPage: pageData.currentPage || 1,
          };
        }
        return { items: [], totalItems: 0, totalPages: 0, currentPage: 1 };
      },
      providesTags: (result) =>
        result
          ? [
              ...result.items.map(({ id }) => ({
                type: 'logistics/Product' as const,
                id,
              })),
              { type: 'logistics/Product', id: 'LIST' },
            ]
          : [{ type: 'logistics/Product', id: 'LIST' }],
    }),

    // Get single product
    getProduct: builder.query<Product, string>({
      query: (productId) => ({
        url: `/logistics/api/v1/product/search/${productId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return null;
      },
      providesTags: (_result, _error, id) => [
        { type: 'logistics/Product', id },
      ],
    }),

    // Create product
    createProduct: builder.mutation<void, CreateProductRequest>({
      query: (body) => ({
        url: '/logistics/api/v1/product/create',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'logistics/Product', id: 'LIST' }],
    }),

    // Update product
    updateProduct: builder.mutation<
      void,
      { productId: string; data: UpdateProductRequest }
    >({
      query: ({ productId, data }) => ({
        url: `/logistics/api/v1/product/update/${productId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (_result, _error, { productId }) => [
        { type: 'logistics/Product', id: productId },
        { type: 'logistics/Product', id: 'LIST' },
      ],
    }),

    // Delete product
    deleteProduct: builder.mutation<void, string>({
      query: (productId) => ({
        url: `/logistics/api/v1/product/delete/${productId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'logistics/Product', id },
        { type: 'logistics/Product', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetProductsQuery,
  useGetProductQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
} = productApi;
