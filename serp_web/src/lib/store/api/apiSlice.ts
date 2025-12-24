/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Base API configuration for all services
 */

import {
  createApi,
  fetchBaseQuery,
  BaseQueryFn,
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/query/react';
import type { RootState } from '../store';

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// Raw base query with authentication
const rawBaseQuery = fetchBaseQuery({
  baseUrl: API_BASE_URL,
  prepareHeaders: (headers, { getState }) => {
    const state = getState() as RootState;
    const token = state.account.auth?.token;

    if (token) {
      headers.set('authorization', `Bearer ${token}`);
    }
    headers.set('content-type', 'application/json');

    return headers;
  },
});

// Dynamic base query to handle different services
const dynamicBaseQuery: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions: any) => {
  const service = extraOptions?.service;
  // Default to 'account' behavior (no prefix before /api/v1)
  // If service is provided and not 'account', prepend /service

  let urlPrefix = '/api/v1';

  if (service && service !== 'account') {
    urlPrefix = `/${service}/api/v1`;
  }

  let adjustedArgs = args;
  if (typeof args === 'string') {
    const path = args.startsWith('/') ? args : `/${args}`;
    adjustedArgs = `${urlPrefix}${path}`;
  } else {
    const path = args.url.startsWith('/') ? args.url : `/${args.url}`;
    adjustedArgs = { ...args, url: `${urlPrefix}${path}` };
  }

  return rawBaseQuery(adjustedArgs, api, extraOptions);
};

// Base query with error handling and token refresh
const baseQueryWithReauth: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  let result = await dynamicBaseQuery(args, api, extraOptions);

  // Handle 401 unauthorized - token refresh logic
  if (result.error && result.error.status === 401) {
    const state = api.getState() as RootState;
    const refreshToken = state.account.auth?.refreshToken;

    if (refreshToken) {
      const refreshResult = await dynamicBaseQuery(
        {
          url: '/auth/refresh-token',
          method: 'POST',
          body: { refreshToken },
        },
        api,
        { ...extraOptions, service: 'account' }
      );

      if (refreshResult.data) {
        // Import setTokens action dynamically to avoid circular imports
        const { setTokens } = await import('@/modules/account/store');
        const tokenData = refreshResult.data as any;

        if (
          tokenData.code === 200 &&
          tokenData.status.toLowerCase() === 'success'
        ) {
          // Store new tokens and retry original request
          api.dispatch(
            setTokens({
              token: tokenData.data.accessToken,
              refreshToken: tokenData.data.refreshToken,
            })
          );

          result = await dynamicBaseQuery(args, api, extraOptions);
        } else {
          // Refresh response indicates failure
          const { clearAuth } = await import('@/modules/account/store');
          api.dispatch(clearAuth());
        }
      } else {
        // Refresh failed - clear auth state
        const { clearAuth } = await import('@/modules/account/store');
        api.dispatch(clearAuth());
      }
    } else {
      // No refresh token - clear auth state
      const { clearAuth } = await import('@/modules/account/store');
      api.dispatch(clearAuth());
    }
  }

  return result;
};

// Create the main API slice
export const api = createApi({
  reducerPath: 'api',
  baseQuery: baseQueryWithReauth,

  // Tag types for cache invalidation
  tagTypes: [
    // Account tags
    'account/user',
    'account/profile',
    'account/auth',
    'account/permissions',
    'account/menus',
    'account/modules',
    'Customer',
    'Lead',
    'Opportunity',
    'Activity',
    'Analytics',
    // Admin tags
    'admin/Organization',
    'admin/Subscription',
    'admin/Plan',
    'admin/PlanModule',
    'admin/Module',
    'admin/User',
    'admin/Role',
    'admin/MenuDisplay',
    // Settings tags
    'settings/Organization',
    'settings/User',
    'settings/Module',
    'settings/ModuleUsers',
    'settings/Department',
    // Subscription tags
    'subscription/Plan',
    'subscription/PlanModule',
    'subscription/Module',
    'subscription/Subscription',
    // PTM v2 tags
    'ptm/Task',
    'ptm/Project',
    'ptm/Schedule',
    'ptm/FocusTime',
    'ptm/Availability',
    'ptm/Activity',
    'ptm/Note',
    'ptm/Dependency',
    // Purchase tags
    'purchase/Supplier',
    'purchase/Product',
    'purchase/Category',
    'purchase/Order',
    'purchase/Facility',
    'purchase/Shipment',
    'purchase/Address',
    // Logistics tags
    'logistics/Address',
    'logistics/Category',
    'logistics/Customer',
    'logistics/Facility',
    'logistics/InventoryItem',
    'logistics/Order',
    'logistics/Product',
    'logistics/Shipment',
    'logistics/Supplier',
    // Notification tags
    'Notification',
  ],

  // Define endpoints in separate files for each module
  endpoints: () => ({}),
});
