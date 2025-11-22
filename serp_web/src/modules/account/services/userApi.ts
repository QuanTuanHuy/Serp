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
  UpdateProfileRequest,
  ChangePasswordRequest,
} from '../types';

export const userApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCurrentUser: builder.query<UserProfileResponse, void>({
      query: () => '/users/profile/me',
      providesTags: [
        { type: 'account/user', id: 'CURRENT_USER' },
        'account/profile',
      ],
      transformResponse: createRtkTransformResponse(),
    }),

    getUserPermissions: builder.query<PermissionsResponse, void>({
      query: () => '/users/permissions/me',
      providesTags: [
        { type: 'account/user', id: 'CURRENT_USER' },
        'account/permissions',
      ],
      transformResponse: createRtkTransformResponse(),
    }),

    getUserMenus: builder.query<MenusResponse, void>({
      query: () => '/users/menus/me',
      providesTags: [
        { type: 'account/user', id: 'CURRENT_USER' },
        'account/menus',
      ],
      transformResponse: createRtkTransformResponse(),
    }),

    updateUserProfile: builder.mutation<
      UserProfileResponse,
      { userId: number; data: UpdateProfileRequest }
    >({
      query: ({ userId, data }) => ({
        url: `/users/${userId}/info`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: [
        { type: 'account/user', id: 'CURRENT_USER' },
        'account/profile',
      ],
      transformResponse: createRtkTransformResponse(),
    }),

    changePassword: builder.mutation<
      void,
      { userId: number; data: ChangePasswordRequest }
    >({
      query: ({ data }) => ({
        url: `/auth/change-password`,
        method: 'POST',
        body: data,
      }),
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetCurrentUserQuery,
  useGetUserPermissionsQuery,
  useGetUserMenusQuery,
  useUpdateUserProfileMutation,
  useChangePasswordMutation,
} = userApi;
