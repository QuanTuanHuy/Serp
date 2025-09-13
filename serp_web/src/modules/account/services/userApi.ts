/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication API endpoints
 */

import { api } from '@/lib';
import { createRtkTransformResponse } from '@/lib';
import {
  MenusResponse,
  PermissionsResponse,
  UserProfileResponse,
} from '../types';

export const userApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCurrentUser: builder.query<UserProfileResponse, void>({
      query: () => '/users/profile/me',
      providesTags: ['User'],
      transformErrorResponse: createRtkTransformResponse(),
    }),

    getUserPermissions: builder.query<PermissionsResponse, void>({
      query: () => '/users/permissions/me',
      providesTags: ['User'],
      transformResponse: createRtkTransformResponse(),
    }),

    getUserMenus: builder.query<MenusResponse, void>({
      query: () => '/users/menus/me',
      providesTags: ['User'],
      transformResponse: createRtkTransformResponse(),
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetCurrentUserQuery,
  useGetUserPermissionsQuery,
  useGetUserMenusQuery,
} = userApi;
