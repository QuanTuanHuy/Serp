/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Roles API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  Role,
  RoleResponse,
  CreateRoleRequest,
  UpdateRoleRequest,
  AddPermissionToRoleRequest,
} from '../../types';
import { createDataTransform } from '@/lib/store/api/utils';

export const rolesApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAllRoles: builder.query<Role[], void>({
      query: () => ({
        url: '/roles',
        method: 'GET',
      }),
      transformResponse: createDataTransform<Role[]>(),
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({
                type: 'admin/Role' as const,
                id,
              })),
              { type: 'admin/Role', id: 'LIST' },
            ]
          : [{ type: 'admin/Role', id: 'LIST' }],
    }),

    getRoleById: builder.query<RoleResponse, number>({
      query: (roleId) => ({
        url: `/roles/${roleId}`,
        method: 'GET',
      }),
      providesTags: (result, error, roleId) => [
        { type: 'admin/Role', id: roleId },
      ],
    }),

    createRole: builder.mutation<RoleResponse, CreateRoleRequest>({
      query: (data) => ({
        url: '/roles',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'admin/Role', id: 'LIST' }],
    }),

    updateRole: builder.mutation<
      RoleResponse,
      { roleId: number; data: UpdateRoleRequest }
    >({
      query: ({ roleId, data }) => ({
        url: `/roles/${roleId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { roleId }) => [
        { type: 'admin/Role', id: roleId },
        { type: 'admin/Role', id: 'LIST' },
      ],
    }),

    addPermissionsToRole: builder.mutation<
      RoleResponse,
      { roleId: number; data: AddPermissionToRoleRequest }
    >({
      query: ({ roleId, data }) => ({
        url: `/roles/${roleId}/permissions`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: (result, error, { roleId }) => [
        { type: 'admin/Role', id: roleId },
        { type: 'admin/Role', id: 'LIST' },
      ],
    }),

    deleteRole: builder.mutation<RoleResponse, number>({
      query: (roleId) => ({
        url: `/roles/${roleId}`,
        method: 'DELETE',
      }),
      invalidatesTags: [{ type: 'admin/Role', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetAllRolesQuery,
  useLazyGetAllRolesQuery,
  useGetRoleByIdQuery,
  useLazyGetRoleByIdQuery,
  useCreateRoleMutation,
  useUpdateRoleMutation,
  useAddPermissionsToRoleMutation,
  useDeleteRoleMutation,
} = rolesApi;
