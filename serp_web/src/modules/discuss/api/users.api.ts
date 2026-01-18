/*
Author: QuanTuanHuy
Description: Part of Serp Project - User API endpoints for Discuss module
*/

import { api } from '@/lib/store/api';
import type { APIResponse, UserInfo } from '../types';

export const userApi = api.injectEndpoints({
  endpoints: (builder) => ({
    /**
     * Get users in the current tenant (searchable)
     */
    getUsers: builder.query<APIResponse<UserInfo[]>, { query?: string }>({
      query: ({ query }) => ({
        url: '/users',
        params: { query },
      }),
      extraOptions: { service: 'discuss' },
    }),
  }),
});

export const { useGetUsersQuery, useLazyGetUsersQuery } = userApi;
