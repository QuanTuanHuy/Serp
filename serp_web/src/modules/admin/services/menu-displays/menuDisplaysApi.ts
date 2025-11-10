/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Displays API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  MenuDisplayDetail,
  CreateMenuDisplayRequest,
  UpdateMenuDisplayRequest,
  AssignMenuDisplayToRoleRequest,
  GetMenuDisplayParams,
} from '../../types';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import { MenuDisplaysResponse } from '../../types/menu-display.types';

export const menuDisplaysApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAllMenuDisplays: builder.query<
      MenuDisplaysResponse,
      GetMenuDisplayParams | void
    >({
      query: (params) => ({
        url: '/menu-displays',
        method: 'GET',
        params: params || {},
      }),
      transformResponse: createPaginatedTransform<MenuDisplayDetail>(),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'admin/MenuDisplay' as const,
                id,
              })),
              { type: 'admin/MenuDisplay', id: 'LIST' },
            ]
          : [{ type: 'admin/MenuDisplay', id: 'LIST' }],
    }),

    getMenuDisplaysByModule: builder.query<MenuDisplayDetail[], number>({
      query: (moduleId) => ({
        url: `/menu-displays/get-by-module/${moduleId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<MenuDisplayDetail[]>(),
      providesTags: (result, _error, moduleId) =>
        result
          ? [
              ...result.map(({ id }) => ({
                type: 'admin/MenuDisplay' as const,
                id,
              })),
              { type: 'admin/MenuDisplay', id: `MODULE_${moduleId}` },
            ]
          : [{ type: 'admin/MenuDisplay', id: `MODULE_${moduleId}` }],
    }),

    getMenuDisplaysByRoleIds: builder.query<MenuDisplayDetail[], number[]>({
      query: (roleIds) => ({
        url: '/menu-displays/get-by-role-ids',
        method: 'GET',
        params: { roleIds: roleIds.join(',') },
      }),
      transformResponse: createDataTransform<MenuDisplayDetail[]>(),
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({
                type: 'admin/MenuDisplay' as const,
                id,
              })),
              { type: 'admin/MenuDisplay', id: 'ROLE_LIST' },
            ]
          : [{ type: 'admin/MenuDisplay', id: 'ROLE_LIST' }],
    }),

    createMenuDisplay: builder.mutation<
      MenuDisplayDetail,
      CreateMenuDisplayRequest
    >({
      query: (data) => ({
        url: '/menu-displays',
        method: 'POST',
        body: data,
      }),
      transformResponse: createDataTransform<MenuDisplayDetail>(),
      invalidatesTags: [
        { type: 'admin/MenuDisplay', id: 'LIST' },
        { type: 'admin/MenuDisplay', id: 'ROLE_LIST' },
      ],
    }),

    updateMenuDisplay: builder.mutation<
      MenuDisplayDetail,
      { id: number; data: UpdateMenuDisplayRequest }
    >({
      query: ({ id, data }) => ({
        url: `/menu-displays/${id}`,
        method: 'PUT',
        body: data,
      }),
      transformResponse: createDataTransform<MenuDisplayDetail>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'admin/MenuDisplay', id },
        { type: 'admin/MenuDisplay', id: 'LIST' },
        { type: 'admin/MenuDisplay', id: 'ROLE_LIST' },
      ],
    }),

    deleteMenuDisplay: builder.mutation<void, number>({
      query: (id) => ({
        url: `/menu-displays/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'admin/MenuDisplay', id },
        { type: 'admin/MenuDisplay', id: 'LIST' },
        { type: 'admin/MenuDisplay', id: 'ROLE_LIST' },
      ],
    }),

    assignMenuDisplaysToRole: builder.mutation<
      void,
      AssignMenuDisplayToRoleRequest
    >({
      query: (data) => ({
        url: '/menu-displays/assign-to-role',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [
        { type: 'admin/MenuDisplay', id: 'LIST' },
        { type: 'admin/MenuDisplay', id: 'ROLE_LIST' },
        { type: 'admin/Role', id: 'LIST' },
      ],
    }),

    unassignMenuDisplaysFromRole: builder.mutation<
      void,
      AssignMenuDisplayToRoleRequest
    >({
      query: (data) => ({
        url: '/menu-displays/unassign-from-role',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [
        { type: 'admin/MenuDisplay', id: 'LIST' },
        { type: 'admin/MenuDisplay', id: 'ROLE_LIST' },
        { type: 'admin/Role', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetAllMenuDisplaysQuery,
  useGetMenuDisplaysByModuleQuery,
  useGetMenuDisplaysByRoleIdsQuery,
  useLazyGetAllMenuDisplaysQuery,
  useLazyGetMenuDisplaysByModuleQuery,
  useCreateMenuDisplayMutation,
  useUpdateMenuDisplayMutation,
  useDeleteMenuDisplayMutation,
  useAssignMenuDisplaysToRoleMutation,
  useUnassignMenuDisplaysFromRoleMutation,
} = menuDisplaysApi;
