/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings global search API
 */

import { api } from '@/lib/store/api/apiSlice';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  SettingsGlobalSearchParams,
  SettingsGlobalSearchResponse,
} from '@/modules/settings/types';

export const settingsGlobalSearchApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSettingsGlobalSearch: builder.query<
      SettingsGlobalSearchResponse,
      SettingsGlobalSearchParams
    >({
      query: ({ organizationId, q, limit = 5 }) => {
        const params = new URLSearchParams();
        params.set('q', q);
        params.set('limit', String(limit));

        return {
          url: `/organizations/${organizationId}/settings/search?${params.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createDataTransform<SettingsGlobalSearchResponse>(),
      extraOptions: { service: 'account' },
    }),
  }),
});

export const { useGetSettingsGlobalSearchQuery } = settingsGlobalSearchApi;
