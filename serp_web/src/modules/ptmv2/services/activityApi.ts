/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity Tracking API Endpoints
 */

import { ptmApi } from './api';
import type {
  ActivityEvent,
  ActivityFeedResponse,
  ActivityFeedFilters,
} from '../types';

export const activityApi = ptmApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get paginated activity feed
    getActivityFeed: builder.query<ActivityFeedResponse, ActivityFeedFilters>({
      query: (filters) => ({
        url: '/ptm_task/activities',
        params: {
          types: filters.types?.join(','),
          entity: filters.entity,
          from: filters.fromDateMs,
          to: filters.toDateMs,
          page: filters.page ?? 0,
          size: filters.size ?? 20,
        },
      }),
      providesTags: (result) =>
        result
          ? [
              ...result.activities.map(({ id }) => ({
                type: 'ptm/Activity' as const,
                id,
              })),
              { type: 'ptm/Activity', id: 'LIST' },
            ]
          : [{ type: 'ptm/Activity', id: 'LIST' }],

      // Infinite scroll configuration
      serializeQueryArgs: ({ queryArgs }) => {
        const { page, ...rest } = queryArgs;
        return rest;
      },
      merge: (currentCache, newItems, { arg }) => {
        if (arg.page === 0) {
          return newItems;
        }
        return {
          ...newItems,
          activities: [...currentCache.activities, ...newItems.activities],
        };
      },
      forceRefetch: ({ currentArg, previousArg }) => {
        return currentArg?.page !== previousArg?.page;
      },
    }),

    // Get activities for specific entity (task, project, etc.)
    getEntityActivities: builder.query<
      ActivityEvent[],
      { entityType: string; entityId: number }
    >({
      query: ({ entityType, entityId }) =>
        `/ptm_task/activities/entity/${entityType}/${entityId}`,
      providesTags: (result, error, { entityType, entityId }) => [
        { type: 'ptm/Activity', id: `${entityType}:${entityId}` },
      ],
    }),

    // Get activity statistics (future enhancement)
    getActivityStats: builder.query<
      {
        todayCount: number;
        weekCount: number;
        averagePerDay: number;
        mostActiveHour: string;
      },
      void
    >({
      query: () => '/ptm_task/activities/stats',
      providesTags: [{ type: 'ptm/Activity', id: 'STATS' }],
    }),
  }),
});

export const {
  useGetActivityFeedQuery,
  useGetEntityActivitiesQuery,
  useGetActivityStatsQuery,
} = activityApi;
