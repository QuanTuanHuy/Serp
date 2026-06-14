/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin global search API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';

import type {
  AdminGlobalSearchParams,
  AdminGlobalSearchResponse,
} from '../../types';

export const globalSearchApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAdminGlobalSearch: builder.query<
      AdminGlobalSearchResponse,
      AdminGlobalSearchParams
    >({
      query: ({ q, limit = 5 }) => {
        const params = new URLSearchParams();
        params.append('q', q);
        params.append('limit', String(limit));

        return {
          url: `/admin/search?${params.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createDataTransform<AdminGlobalSearchResponse>(),
      extraOptions: { service: 'account' },
    }),
  }),
  overrideExisting: false,
});

export const { useGetAdminGlobalSearchQuery } = globalSearchApi;
