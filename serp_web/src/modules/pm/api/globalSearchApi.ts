/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';

import type {
  PMGlobalSearchParams,
  PMGlobalSearchResponse,
} from '../types/api';

export const pmGlobalSearchApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPmGlobalSearch: builder.query<
      PMGlobalSearchResponse,
      PMGlobalSearchParams
    >({
      query: ({ q, limit = 5, currentProjectId }) => ({
        url: '/search',
        method: 'GET',
        params: {
          q,
          limit,
          ...(typeof currentProjectId === 'number' ? { currentProjectId } : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMGlobalSearchResponse>(),
    }),
  }),
  overrideExisting: false,
});

export const { useGetPmGlobalSearchQuery, useLazyGetPmGlobalSearchQuery } =
  pmGlobalSearchApi;
