/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  LoginRequest,
  RegisterRequest,
  RefreshTokenRequest,
  RevokeTokenRequest,
  AuthResponse,
  TokenResponse,
  UserProfileResponse,
} from '../types';

export const authApi = api.injectEndpoints({
  endpoints: (builder) => ({
    register: builder.mutation<AuthResponse, RegisterRequest>({
      query: (credentials) => ({
        url: '/auth/register',
        method: 'POST',
        body: credentials,
      }),
      invalidatesTags: ['User'],
    }),

    login: builder.mutation<AuthResponse, LoginRequest>({
      query: (credentials) => ({
        url: '/auth/login',
        method: 'POST',
        body: credentials,
      }),
      invalidatesTags: ['User'],
    }),

    getToken: builder.mutation<TokenResponse, LoginRequest>({
      query: (credentials) => ({
        url: '/auth/get-token',
        method: 'POST',
        body: credentials,
      }),
    }),

    refreshToken: builder.mutation<TokenResponse, RefreshTokenRequest>({
      query: (tokenData) => ({
        url: '/auth/refresh-token',
        method: 'POST',
        body: tokenData,
      }),
    }),

    revokeToken: builder.mutation<
      { code: number; status: string; message: string },
      RevokeTokenRequest
    >({
      query: (tokenData) => ({
        url: '/auth/revoke-token',
        method: 'POST',
        body: tokenData,
      }),
      invalidatesTags: ['User'],
    }),

    getCurrentUser: builder.query<UserProfileResponse, void>({
      query: () => '/users/profile/me',
      providesTags: ['User'],
    }),
  }),
  overrideExisting: false,
});

export const {
  useRegisterMutation,
  useLoginMutation,
  useGetTokenMutation,
  useRefreshTokenMutation,
  useRevokeTokenMutation,
  useGetCurrentUserQuery,
  useLazyGetCurrentUserQuery,
} = authApi;
