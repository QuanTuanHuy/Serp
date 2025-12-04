/**
 * Logistics Module - Category API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Category CRUD operations
 */

import { logisticsApi } from './api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type {
  Category,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CategoryFilters,
} from '../types';

export interface CategoriesResponse {
  items: Category[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export const categoryApi = logisticsApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get categories
    getCategories: builder.query<CategoriesResponse, CategoryFilters | void>({
      query: (params) => ({
        url: '/logistics/api/v1/category/search',
        method: 'GET',
        params: {
          page: params?.page || 1,
          size: params?.size || 10,
          sortBy: params?.sortBy || 'createdStamp',
          sortDirection: params?.sortDirection || 'desc',
          query: params?.query || '',
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
                type: 'logistics/Category' as const,
                id,
              })),
              { type: 'logistics/Category', id: 'LIST' },
            ]
          : [{ type: 'logistics/Category', id: 'LIST' }],
    }),

    // Get single category
    getCategory: builder.query<Category, string>({
      query: (categoryId) => ({
        url: `/logistics/api/v1/category/search/${categoryId}`,
        method: 'GET',
      }),
      transformResponse: (response: any) => {
        if (response?.code === 200 && response?.status === 'SUCCESS') {
          return response.data;
        }
        return null;
      },
      providesTags: (_result, _error, id) => [
        { type: 'logistics/Category', id },
      ],
    }),

    // Create category
    createCategory: builder.mutation<void, CreateCategoryRequest>({
      query: (body) => ({
        url: '/logistics/api/v1/category/create',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'logistics/Category', id: 'LIST' }],
    }),

    // Update category
    updateCategory: builder.mutation<
      void,
      { categoryId: string; data: UpdateCategoryRequest }
    >({
      query: ({ categoryId, data }) => ({
        url: `/logistics/api/v1/category/update/${categoryId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (_result, _error, { categoryId }) => [
        { type: 'logistics/Category', id: categoryId },
        { type: 'logistics/Category', id: 'LIST' },
      ],
    }),

    // Delete category
    deleteCategory: builder.mutation<void, string>({
      query: (categoryId) => ({
        url: `/logistics/api/v1/category/delete/${categoryId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'logistics/Category', id },
        { type: 'logistics/Category', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetCategoriesQuery,
  useGetCategoryQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeleteCategoryMutation,
} = categoryApi;
