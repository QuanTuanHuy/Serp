/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard API endpoints
 */

import { api } from '@/lib/store/api';

import type {
  TmsDashboardAlerts,
  TmsDashboardFilter,
  TmsDashboardLegs,
  TmsDashboardOverview,
} from '../types';
import { unwrapFirstMileResult } from './transforms';

const TMS_ORDER_SERVICE = { service: 'tms-order' as const };

const toDashboardParams = (filter: TmsDashboardFilter) => ({
  fromDate: filter.fromDate,
  toDate: filter.toDate,
  timezone: filter.timezone,
  granularity: filter.granularity,
  ...(filter.hubId !== undefined ? { hubId: filter.hubId } : {}),
  ...(filter.postOfficeId !== undefined
    ? { postOfficeId: filter.postOfficeId }
    : {}),
  ...(filter.postOfficeCode ? { postOfficeCode: filter.postOfficeCode } : {}),
  ...(filter.postOfficeCodes?.length
    ? { postOfficeCodes: filter.postOfficeCodes }
    : {}),
  ...(filter.serviceType ? { serviceType: filter.serviceType } : {}),
});

export const dashboardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getTmsDashboardOverview: builder.query<
      TmsDashboardOverview,
      TmsDashboardFilter
    >({
      query: (filter) => ({
        url: '/tms/dashboard/overview',
        method: 'GET',
        params: toDashboardParams(filter),
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResult<TmsDashboardOverview>,
    }),
    getTmsDashboardLegs: builder.query<TmsDashboardLegs, TmsDashboardFilter>({
      query: (filter) => ({
        url: '/tms/dashboard/legs',
        method: 'GET',
        params: toDashboardParams(filter),
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResult<TmsDashboardLegs>,
    }),
    getTmsDashboardAlerts: builder.query<
      TmsDashboardAlerts,
      TmsDashboardFilter & { size?: number }
    >({
      query: ({ size = 10, ...filter }) => ({
        url: '/tms/dashboard/alerts',
        method: 'GET',
        params: {
          ...toDashboardParams(filter),
          size,
        },
      }),
      extraOptions: TMS_ORDER_SERVICE,
      transformResponse: unwrapFirstMileResult<TmsDashboardAlerts>,
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetTmsDashboardOverviewQuery,
  useGetTmsDashboardLegsQuery,
  useGetTmsDashboardAlertsQuery,
} = dashboardApi;
