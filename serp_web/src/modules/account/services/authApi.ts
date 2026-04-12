/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  ConfirmPasswordResetRequest,
  ConfirmPasswordResetResponse,
  LoginRequest,
  RegisterRequest,
  RefreshTokenRequest,
  RevokeTokenRequest,
  AuthResponse,
  TokenResponse,
  ValidatePasswordResetTokenResponse,
} from '../types';
import { createRtkTransformResponse } from '@/lib/store/api';

export const authApi = api.injectEndpoints({
  endpoints: (builder) => ({
    register: builder.mutation<AuthResponse, RegisterRequest>({
      query: (credentials) => ({
        url: '/auth/register',
        method: 'POST',
        body: credentials,
      }),
      invalidatesTags: [{ type: 'account/user', id: 'CURRENT_USER' }],
    }),

    login: builder.mutation<AuthResponse, LoginRequest>({
      query: (credentials) => ({
        url: '/auth/login',
        method: 'POST',
        body: credentials,
      }),
      invalidatesTags: [{ type: 'account/user', id: 'CURRENT_USER' }],
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
      invalidatesTags: [{ type: 'account/user', id: 'CURRENT_USER' }],
    }),

    validatePasswordResetToken: builder.query<
      ValidatePasswordResetTokenResponse,
      string
    >({
      query: (token) => ({
        url: `/auth/reset-password/validate?token=${encodeURIComponent(token)}`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
    }),

    confirmPasswordReset: builder.mutation<
      ConfirmPasswordResetResponse,
      ConfirmPasswordResetRequest
    >({
      query: (body) => ({
        url: '/auth/reset-password/confirm',
        method: 'POST',
        body,
      }),
      transformResponse: createRtkTransformResponse(),
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
  useValidatePasswordResetTokenQuery,
  useConfirmPasswordResetMutation,
} = authApi;
