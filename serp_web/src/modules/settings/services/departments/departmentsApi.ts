/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings Departments API endpoints
 */

import { api } from '@/lib/store/api';
import {
  createPaginatedTransform,
  createRtkTransformResponse,
} from '@/lib/store/api/utils';
import type {
  Department,
  DepartmentMember,
  DepartmentStatistics,
  CreateDepartmentRequest,
  UpdateDepartmentRequest,
  AssignUserToDepartmentRequest,
  BulkAssignUsersToDepartmentRequest,
  GetDepartmentParams,
} from '../../types/department.types';
import type { PaginatedResponse, ApiResponse } from '@/lib/store/api/types';

export const settingsDepartmentsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDepartments: builder.query<
      PaginatedResponse<Department>,
      GetDepartmentParams
    >({
      query: (params) => {
        const queryParams = new URLSearchParams();
        if (params.page !== undefined)
          queryParams.append('page', String(params.page));
        if (params.pageSize !== undefined)
          queryParams.append('pageSize', String(params.pageSize));
        if (params.sortBy) queryParams.append('sortBy', params.sortBy);
        if (params.sortDir) queryParams.append('sortDir', params.sortDir);
        if (params.search) queryParams.append('search', params.search);
        if (params.parentDepartmentId !== undefined)
          queryParams.append(
            'parentDepartmentId',
            String(params.parentDepartmentId)
          );
        if (params.isActive !== undefined)
          queryParams.append('isActive', String(params.isActive));
        if (params.managerId !== undefined)
          queryParams.append('managerId', String(params.managerId));

        return {
          url: `/organizations/${params.organizationId}/departments?${queryParams.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createPaginatedTransform<Department>(),
      providesTags: (result) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'settings/Department' as const,
                id,
              })),
              { type: 'settings/Department', id: 'LIST' },
            ]
          : [{ type: 'settings/Department', id: 'LIST' }],
    }),

    getDepartmentById: builder.query<
      ApiResponse<Department>,
      { organizationId: number; departmentId: number }
    >({
      query: ({ organizationId, departmentId }) => ({
        url: `/organizations/${organizationId}/departments/${departmentId}`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: (_result, _error, { departmentId }) => [
        { type: 'settings/Department', id: departmentId },
      ],
    }),

    createDepartment: builder.mutation<
      ApiResponse<Department>,
      { organizationId: number; body: CreateDepartmentRequest }
    >({
      query: ({ organizationId, body }) => ({
        url: `/organizations/${organizationId}/departments`,
        method: 'POST',
        body,
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: [{ type: 'settings/Department', id: 'LIST' }],
    }),

    updateDepartment: builder.mutation<
      ApiResponse<Department>,
      {
        organizationId: number;
        departmentId: number;
        body: UpdateDepartmentRequest;
      }
    >({
      query: ({ organizationId, departmentId, body }) => ({
        url: `/organizations/${organizationId}/departments/${departmentId}`,
        method: 'PATCH',
        body,
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: (_result, _error, { departmentId }) => [
        { type: 'settings/Department', id: departmentId },
        { type: 'settings/Department', id: 'LIST' },
      ],
    }),

    deleteDepartment: builder.mutation<
      ApiResponse<void>,
      { organizationId: number; departmentId: number }
    >({
      query: ({ organizationId, departmentId }) => ({
        url: `/organizations/${organizationId}/departments/${departmentId}`,
        method: 'DELETE',
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: (_result, _error, { departmentId }) => [
        { type: 'settings/Department', id: departmentId },
        { type: 'settings/Department', id: 'LIST' },
      ],
    }),

    getDepartmentMembers: builder.query<
      ApiResponse<DepartmentMember[]>,
      { organizationId: number; departmentId: number }
    >({
      query: ({ organizationId, departmentId }) => ({
        url: `/organizations/${organizationId}/departments/${departmentId}/members`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: (_result, _error, { departmentId }) => [
        { type: 'settings/Department', id: `${departmentId}-members` },
      ],
    }),

    assignUserToDepartment: builder.mutation<
      ApiResponse<void>,
      {
        organizationId: number;
        departmentId: number;
        body: AssignUserToDepartmentRequest;
      }
    >({
      query: ({ organizationId, departmentId, body }) => ({
        url: `/organizations/${organizationId}/departments/${departmentId}/users`,
        method: 'POST',
        body,
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: (_result, _error, { departmentId }) => [
        { type: 'settings/Department', id: departmentId },
        { type: 'settings/Department', id: `${departmentId}-members` },
        { type: 'settings/Department', id: 'LIST' },
      ],
    }),

    bulkAssignUsersToDepartment: builder.mutation<
      ApiResponse<void>,
      {
        organizationId: number;
        departmentId: number;
        body: BulkAssignUsersToDepartmentRequest;
      }
    >({
      query: ({ organizationId, departmentId, body }) => ({
        url: `/organizations/${organizationId}/departments/${departmentId}/users/bulk`,
        method: 'POST',
        body,
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: (_result, _error, { departmentId }) => [
        { type: 'settings/Department', id: departmentId },
        { type: 'settings/Department', id: `${departmentId}-members` },
        { type: 'settings/Department', id: 'LIST' },
      ],
    }),

    removeUserFromDepartment: builder.mutation<
      ApiResponse<void>,
      { organizationId: number; departmentId: number; userId: number }
    >({
      query: ({ organizationId, departmentId, userId }) => ({
        url: `/organizations/${organizationId}/departments/${departmentId}/users/${userId}`,
        method: 'DELETE',
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: (_result, _error, { departmentId }) => [
        { type: 'settings/Department', id: departmentId },
        { type: 'settings/Department', id: `${departmentId}-members` },
        { type: 'settings/Department', id: 'LIST' },
      ],
    }),

    // Get department statistics (to be implemented on backend)
    getDepartmentStatistics: builder.query<
      ApiResponse<DepartmentStatistics>,
      { organizationId: number }
    >({
      query: ({ organizationId }) => ({
        url: `/organizations/${organizationId}/departments/stats`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: [{ type: 'settings/Department', id: 'STATISTICS' }],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetDepartmentsQuery,
  useLazyGetDepartmentsQuery,
  useGetDepartmentByIdQuery,
  useLazyGetDepartmentByIdQuery,
  useCreateDepartmentMutation,
  useUpdateDepartmentMutation,
  useDeleteDepartmentMutation,
  useGetDepartmentMembersQuery,
  useLazyGetDepartmentMembersQuery,
  useAssignUserToDepartmentMutation,
  useBulkAssignUsersToDepartmentMutation,
  useRemoveUserFromDepartmentMutation,
  useGetDepartmentStatisticsQuery,
  useLazyGetDepartmentStatisticsQuery,
} = settingsDepartmentsApi;
