/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings Organizations API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type { Organization } from '@/modules/admin/types';

export const settingsOrganizationsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMyOrganization: builder.query<Organization, void>({
      query: () => ({ url: `/organizations/me`, method: 'GET' }),
      transformResponse: createDataTransform<Organization>(),
      providesTags: [{ type: 'settings/Organization', id: 'ME' }],
    }),
  }),
  overrideExisting: false,
});

export const { useGetMyOrganizationQuery, useLazyGetMyOrganizationQuery } =
  settingsOrganizationsApi;
