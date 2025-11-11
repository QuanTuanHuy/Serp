/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User Menu API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';

import type { MenuDisplayDetail } from '@/modules/admin/types';

export const menuApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMenuDisplaysByModuleAndUser: builder.query<MenuDisplayDetail[], number>({
      query: (moduleId) => ({
        url: `/menu-displays/get-by-module-and-user`,
        method: 'GET',
        params: { moduleId },
      }),
      transformResponse: createDataTransform<MenuDisplayDetail[]>(),
      providesTags: ['account/menus'],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetMenuDisplaysByModuleAndUserQuery,
  useLazyGetMenuDisplaysByModuleAndUserQuery,
} = menuApi;
