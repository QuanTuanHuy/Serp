/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings Modules API (organization module access)
 */

import { api } from '@/lib/store/api/apiSlice';
import { createDataTransform, createPaginatedItemsTransform, createPaginatedTransform, createApiResponseTransform } from '@/lib/store/api/utils';
import type {
  AccessibleModule,
  ModuleRole,
} from '@/modules/settings/types/module-access.types';
import type { UserProfile } from '@/modules/admin/types';
import type { PaginatedResponse, ApiResponse } from '@/lib/store/api/types';

export const settingsModulesApi = api.injectEndpoints({
  endpoints: (build) => ({
    getAccessibleModulesForOrganization: build.query<
      AccessibleModule[],
      number
    >({
      query: (organizationId) => ({
        url: `/organizations/${organizationId}/modules`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<AccessibleModule[]>(),
      providesTags: (result) => [
        { type: 'settings/Module', id: 'LIST' },
        ...(result
          ? result.map((m) => ({
              type: 'settings/Module' as const,
              id: m.moduleId ?? m.moduleCode,
            }))
          : []),
      ],
    }),

    getModuleRoles: build.query<ModuleRole[], number>({
      query: (moduleId) => ({
        url: `/modules/${moduleId}/roles`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<ModuleRole[]>(),
      providesTags: (_result, _err, arg) => [
        { type: 'settings/Module', id: arg },
      ],
    }),

    assignUserToModule: build.mutation<
      ApiResponse<any>,
      {
        organizationId: number;
        moduleId: number;
        userId: number;
        roleId?: number;
      }
    >({
      query: ({ organizationId, moduleId, userId, roleId }) => ({
        url: `/organizations/${organizationId}/modules/${moduleId}/users`,
        method: 'POST',
        body: { userId, moduleId, roleId },
      }),
      transformResponse: createApiResponseTransform<any>(),
      invalidatesTags: (_result, _error, { moduleId }) => [
        { type: 'settings/Module', id: moduleId },
        { type: 'settings/ModuleUsers', id: moduleId },
        { type: 'settings/Module', id: 'LIST' },
      ],
    }),

    revokeUserAccessToModule: build.mutation<
      ApiResponse<any>,
      { organizationId: number; moduleId: number; userId: number }
    >({
      query: ({ organizationId, moduleId, userId }) => ({
        url: `/organizations/${organizationId}/modules/${moduleId}/users/${userId}`,
        method: 'DELETE',
      }),
      transformResponse: createApiResponseTransform<any>(),
      invalidatesTags: (_result, _error, { moduleId }) => [
        { type: 'settings/Module', id: moduleId },
        { type: 'settings/ModuleUsers', id: moduleId },
      ],
    }),

    getModuleUsers: build.query<
      PaginatedResponse<UserProfile>,
      {
        organizationId: number;
        moduleId: number;
        page?: number;
        pageSize?: number;
        search?: string;
      }
    >({
      query: ({ organizationId, moduleId, page, pageSize, search }) => {
        const params = new URLSearchParams();
        if (page !== undefined) params.append('page', String(page));
        if (pageSize !== undefined) params.append('pageSize', String(pageSize));
        if (search) params.append('search', search);
        return {
          url: `/organizations/${organizationId}/modules/${moduleId}/users?${params.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createPaginatedTransform<UserProfile>(),
      providesTags: (_result, _err, { moduleId }) => [
        { type: 'settings/ModuleUsers', id: moduleId },
      ],
    }),

    requestMoreModules: build.mutation<
      { message: string },
      { additionalModuleIds: number[] }
    >({
      query: (body) => ({
        url: '/subscriptions/request-more-modules',
        method: 'POST',
        body,
      }),
      transformResponse: createDataTransform<{ message: string }>(),
      invalidatesTags: [
        { type: 'settings/Module', id: 'LIST' },
        { type: 'subscription/Subscription', id: 'ACTIVE' },
      ],
    }),
  }),
});

export const {
  useGetAccessibleModulesForOrganizationQuery,
  useLazyGetAccessibleModulesForOrganizationQuery,
  useGetModuleRolesQuery,
  useAssignUserToModuleMutation,
  useRevokeUserAccessToModuleMutation,
  useGetModuleUsersQuery,
  useLazyGetModuleUsersQuery,
  useRequestMoreModulesMutation,
} = settingsModulesApi;
