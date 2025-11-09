/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings Modules API (organization module access)
 */

import { api } from '@/lib/store/api/apiSlice';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  AccessibleModule,
  ModuleRole,
} from '@/modules/settings/types/module-access.types';
import type { UserProfile } from '@/modules/admin/types';

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
      unknown,
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
      invalidatesTags: (_result, _error, { moduleId }) => [
        { type: 'settings/Module', id: moduleId },
        { type: 'settings/ModuleUsers', id: moduleId },
        { type: 'settings/Module', id: 'LIST' },
      ],
    }),

    revokeUserAccessToModule: build.mutation<
      unknown,
      { organizationId: number; moduleId: number; userId: number }
    >({
      query: ({ organizationId, moduleId, userId }) => ({
        url: `/organizations/${organizationId}/modules/${moduleId}/users/${userId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, { moduleId }) => [
        { type: 'settings/Module', id: moduleId },
        { type: 'settings/ModuleUsers', id: moduleId },
      ],
    }),

    getModuleUsers: build.query<
      UserProfile[],
      { organizationId: number; moduleId: number }
    >({
      query: ({ organizationId, moduleId }) => ({
        url: `/organizations/${organizationId}/modules/${moduleId}/users`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<UserProfile[]>(),
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
