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

    // Don't manually set content-type here
    // fetchBaseQuery will automatically set the correct content-type:
    // - 'application/json' for plain object bodies
    // - 'multipart/form-data' (with boundary) for FormData bodies

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

const isRefreshTokenRequest = (args: string | FetchArgs): boolean => {
  const url = typeof args === 'string' ? args : args.url;
  return url.includes('/auth/refresh-token');
};

let refreshPromise: Promise<boolean> | null = null;

const refreshAccessToken = async (
  api: Parameters<typeof dynamicBaseQuery>[1],
  extraOptions: any,
  refreshToken: string
): Promise<boolean> => {
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
    const tokenData = refreshResult.data as any;

    if (
      tokenData.code === 200 &&
      tokenData.status.toLowerCase() === 'success'
    ) {
      // Import setTokens action dynamically to avoid circular imports
      const { setTokens } = await import('@/modules/account/store');

      api.dispatch(
        setTokens({
          token: tokenData.data.accessToken,
          refreshToken: tokenData.data.refreshToken,
        })
      );

      return true;
    }
  }

  const { clearAuth } = await import('@/modules/account/store');
  api.dispatch(clearAuth());

  return false;
};

// Base query with error handling and token refresh
const baseQueryWithReauth: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  let result = await dynamicBaseQuery(args, api, extraOptions);

  // Handle 401 unauthorized - token refresh logic
  if (
    result.error &&
    result.error.status === 401 &&
    !isRefreshTokenRequest(args)
  ) {
    const state = api.getState() as RootState;
    const refreshToken = state.account.auth?.refreshToken;

    if (refreshToken) {
      if (!refreshPromise) {
        refreshPromise = refreshAccessToken(
          api,
          extraOptions,
          refreshToken
        ).finally(() => {
          refreshPromise = null;
        });
      }

      const refreshSucceeded = await refreshPromise;

      if (refreshSucceeded) {
        result = await dynamicBaseQuery(args, api, extraOptions);
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
    'MeetingRequest',
    'Analytics',
    'Team',
    'Territory',
    // Admin tags
    'admin/Organization',
    'admin/Dashboard',
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
    'settings/Invitation',
    // Subscription tags
    'subscription/Plan',
    'subscription/PlanModule',
    'subscription/Module',
    'subscription/Subscription',
    // PTM v2 tags
    'ptm/Task',
    'ptm/Project',
    'ptm/Schedule',
    'ptm/ScheduleTask',
    'ptm/FocusTime',
    'ptm/Availability',
    'ptm/Activity',
    'ptm/Note',
    'ptm/Dependency',
    // Purchase tags
    'PurchaseSupplier',
    'PurchaseProduct',
    'PurchaseCategory',
    'PurchaseOrder',
    'PurchaseFacility',
    'PurchaseShipment',
    'PurchaseAddress',
    // Logistics tags
    'logistics/Address',
    'logistics/Category',
    'logistics/Customer',
    'logistics/Facility',
    'logistics/InventoryItem',
    'logistics/Order',
    'logistics/Product',
    'logistics/Shipment',
    'logistics/OutboundShipment',
    'logistics/Supplier',
    // Logistics2 tags
    'logistics2/DeliveryPlan',
    'logistics2/DeliverySlip',
    'logistics2/OutboundShipment',
    'logistics2/Route',
    'logistics2/Vehicle',
    'logistics2/VehicleShipper',
    // Sales tags
    'SalesCustomer',
    'Address',
    'Category',
    'Facility',
    'InventoryItem',
    'Product',
    'Order',
    // Notification tags
    'Notification',
    'NotificationPreference',
    // Discuss tags
    'Channel',
    'Message',
    'DiscussActivity',
    'Presence',
    // PM tags
    'pm/Project',
    'pm/ProjectPermission',
    'pm/ProjectPeople',
    'pm/ProjectRole',
    'pm/ProjectComponent',
    'pm/ProjectSummary',
    'pm/ProjectSettingsOverview',
    'pm/IssueType',
    'pm/IssueTypeScheme',
    'pm/IssueTypeSettings',
    'pm/Priority',
    'pm/PriorityScheme',
    'pm/PrioritySettings',
    'pm/Resolution',
    'pm/ResolutionSettings',
    'pm/Workflow',
    'pm/WorkflowEditor',
    'pm/WorkflowScheme',
    'pm/WorkflowSettings',
    'pm/WorkItem',
    'pm/WorkItemComments',
    'pm/WorkItemActivities',
    'pm/WorkItemChildren',
    'pm/WorkItemDependencies',
    'pm/WorkItemLinks',
    'pm/WorkItemWorklogs',
    'pm/Skill',
    'pm/UserSkill',
    'pm/WorkItemSkill',
    'pm/OptimizationRun',
    'pm/ResourceCalendarSettings',
    'pm/ResourceCalendarProfile',
    'pm/ResourceCalendarException',
    // TTCRS tags
    'ttcrs/Request',
    'ttcrs/Location',
    'ttcrs/Resource',
  ],

  // Define endpoints in separate files for each module
  endpoints: () => ({}),
});
