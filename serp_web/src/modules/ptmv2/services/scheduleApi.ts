/**
 * PTM v2 - Schedule API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Schedule & calendar operations
 */

import { ptmApi } from './api';
import { createDataTransform } from '@/lib/store/api/utils';
import { USE_MOCK_DATA, mockApiHandlers } from '../mocks/mockHandlers';
import type {
  SchedulePlan,
  ScheduleEvent,
  FocusTimeBlock,
  CreateSchedulePlanRequest,
  UpdateScheduleEventRequest,
} from '../types';

export const scheduleApi = ptmApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get schedule plans
    getSchedulePlans: builder.query<SchedulePlan[], void>({
      query: () => ({
        url: '/api/v2/schedule/plans',
        method: 'GET',
      }),
      transformResponse: createDataTransform<SchedulePlan[]>(),
      providesTags: [{ type: 'ptm/Schedule', id: 'LIST' }],
    }),

    // Get active schedule plan
    getActiveSchedulePlan: builder.query<SchedulePlan, void>({
      query: () => ({
        url: '/api/v2/schedule/plans/active',
        method: 'GET',
      }),
      transformResponse: createDataTransform<SchedulePlan>(),
      providesTags: [{ type: 'ptm/Schedule', id: 'ACTIVE' }],
    }),

    // Create schedule plan
    createSchedulePlan: builder.mutation<
      SchedulePlan,
      CreateSchedulePlanRequest
    >({
      query: (body) => ({
        url: '/api/v2/schedule/plans',
        method: 'POST',
        body,
      }),
      transformResponse: createDataTransform<SchedulePlan>(),
      invalidatesTags: [
        { type: 'ptm/Schedule', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'ACTIVE' },
      ],
    }),

    // Get schedule events
    getScheduleEvents: builder.query<
      ScheduleEvent[],
      { startDateMs: number; endDateMs: number }
    >({
      queryFn: async (params) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.schedule.getEvents(params);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Create schedule event
    createScheduleEvent: builder.mutation<
      ScheduleEvent,
      Partial<ScheduleEvent>
    >({
      queryFn: async (event) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.schedule.createEvent(event);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Update schedule event (drag-drop)
    updateScheduleEvent: builder.mutation<
      ScheduleEvent,
      UpdateScheduleEventRequest & {
        dateRange?: { startDateMs: number; endDateMs: number };
      }
    >({
      queryFn: async ({ id, dateRange, ...patch }) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.schedule.updateEvent(id, patch);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },

      // Optimistic update
      async onQueryStarted(
        { id, dateRange, ...patch },
        { dispatch, queryFulfilled }
      ) {
        if (!dateRange) {
          // Skip optimistic update if no dateRange provided
          return;
        }

        // Update in cache immediately
        const patchResult = dispatch(
          scheduleApi.util.updateQueryData(
            'getScheduleEvents',
            dateRange,
            (draft) => {
              const event = draft.find((e) => e.id === id);
              if (event) {
                Object.assign(event, patch);
              }
            }
          )
        );

        try {
          await queryFulfilled;
        } catch {
          patchResult.undo();
        }
      },

      invalidatesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Delete schedule event
    deleteScheduleEvent: builder.mutation<void, string>({
      queryFn: async (id) => {
        if (USE_MOCK_DATA) {
          await mockApiHandlers.schedule.deleteEvent(id);
          return { data: undefined };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Trigger optimization
    triggerOptimization: builder.mutation<
      { jobId: string },
      { planId: string; useQuickPlace?: boolean }
    >({
      queryFn: async (params) => {
        if (USE_MOCK_DATA) {
          const data =
            await mockApiHandlers.schedule.triggerOptimization(params);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Get focus time blocks
    getFocusTimeBlocks: builder.query<FocusTimeBlock[], void>({
      query: () => ({
        url: '/api/v2/schedule/focus-blocks',
        method: 'GET',
      }),
      transformResponse: createDataTransform<FocusTimeBlock[]>(),
      providesTags: [{ type: 'ptm/FocusTime', id: 'LIST' }],
    }),

    // Create focus time block
    createFocusTimeBlock: builder.mutation<
      FocusTimeBlock,
      Partial<FocusTimeBlock>
    >({
      query: (body) => ({
        url: '/api/v2/schedule/focus-blocks',
        method: 'POST',
        body,
      }),
      transformResponse: createDataTransform<FocusTimeBlock>(),
      invalidatesTags: [
        { type: 'ptm/FocusTime', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),

    // Update focus time block
    updateFocusTimeBlock: builder.mutation<
      FocusTimeBlock,
      { id: string } & Partial<FocusTimeBlock>
    >({
      query: ({ id, ...patch }) => ({
        url: `/api/v2/schedule/focus-blocks/${id}`,
        method: 'PUT',
        body: patch,
      }),
      transformResponse: createDataTransform<FocusTimeBlock>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'ptm/FocusTime', id },
        { type: 'ptm/FocusTime', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),

    // Delete focus time block
    deleteFocusTimeBlock: builder.mutation<void, string>({
      query: (id) => ({
        url: `/api/v2/schedule/focus-blocks/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'ptm/FocusTime', id },
        { type: 'ptm/FocusTime', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetSchedulePlansQuery,
  useGetActiveSchedulePlanQuery,
  useCreateSchedulePlanMutation,
  useGetScheduleEventsQuery,
  useCreateScheduleEventMutation,
  useUpdateScheduleEventMutation,
  useDeleteScheduleEventMutation,
  useTriggerOptimizationMutation,
  useGetFocusTimeBlocksQuery,
  useCreateFocusTimeBlockMutation,
  useUpdateFocusTimeBlockMutation,
  useDeleteFocusTimeBlockMutation,
} = scheduleApi;
