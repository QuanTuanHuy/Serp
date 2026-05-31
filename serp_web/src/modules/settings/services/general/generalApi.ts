/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - General settings API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  OrganizationSettings,
  UpdateOrganizationSettingsRequest,
} from '@/modules/settings/types/general.types';

export const settingsGeneralApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getOrganizationSettings: builder.query<OrganizationSettings, void>({
      query: () => ({ url: '/organizations/me/settings', method: 'GET' }),
      transformResponse: createDataTransform<OrganizationSettings>(),
      providesTags: [{ type: 'settings/Organization', id: 'SETTINGS' }],
      extraOptions: { service: 'account' },
    }),
    updateOrganizationSettings: builder.mutation<
      OrganizationSettings,
      UpdateOrganizationSettingsRequest
    >({
      query: (body) => ({
        url: '/organizations/me/settings',
        method: 'PUT',
        body,
      }),
      transformResponse: createDataTransform<OrganizationSettings>(),
      invalidatesTags: [{ type: 'settings/Organization', id: 'SETTINGS' }],
      extraOptions: { service: 'account' },
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetOrganizationSettingsQuery,
  useUpdateOrganizationSettingsMutation,
} = settingsGeneralApi;
