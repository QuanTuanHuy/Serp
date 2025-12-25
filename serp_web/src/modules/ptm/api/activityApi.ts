/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity Tracking API Endpoints
 */

import { api } from '@/lib';
import { USE_MOCK_DATA, mockApiHandlers } from '../mocks/mockHandlers';
import type {
  ActivityEvent,
  ActivityFeedResponse,
  ActivityFeedFilters,
} from '../types';

export const activityApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Get paginated activity feed
    getActivityFeed: builder.query<ActivityFeedResponse, ActivityFeedFilters>({
      queryFn: async (filters) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.activities.getFeed(filters);
          return { data };
        }

        // Real API implementation (when backend ready)
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
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
      queryFn: async ({ entityType, entityId }) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.activities.getByEntity(
            entityType,
            entityId
          );
          return { data };
        }

        // Real API implementation
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
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
      queryFn: async () => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.activities.getStats();
          return { data };
        }

        // Real API implementation
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: [{ type: 'ptm/Activity', id: 'STATS' }],
    }),
  }),
});

export const {
  useGetActivityFeedQuery,
  useGetEntityActivitiesQuery,
  useGetActivityStatsQuery,
} = activityApi;
