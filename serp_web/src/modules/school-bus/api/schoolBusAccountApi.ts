import { api } from '@/lib/store/api';
import { createApiResponseTransform } from '@/lib/store/api/utils';
import type { ApiResponse, SchoolBusAccountUser } from '../types';

const transformApiResponse = createApiResponseTransform;

export const schoolBusAccountApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSchoolBusModuleUsers: builder.query<
      ApiResponse<SchoolBusAccountUser[]>,
      { organizationId: number; moduleId: number }
    >({
      query: ({ organizationId, moduleId }) => ({
        url: `/organizations/${organizationId}/modules/${moduleId}/users`,
        method: 'GET',
      }),
      extraOptions: { service: 'account' },
      transformResponse: transformApiResponse<SchoolBusAccountUser[]>(),
      providesTags: (_result, _error, { moduleId }) => [
        { type: 'settings/ModuleUsers', id: moduleId },
      ],
    }),
  }),
  overrideExisting: false,
});

export const { useGetSchoolBusModuleUsersQuery } = schoolBusAccountApi;
