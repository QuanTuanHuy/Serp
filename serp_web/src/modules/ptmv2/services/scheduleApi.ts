/**
 * PTM v2 - Schedule API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Schedule & calendar operations
 */

import { ptmApi } from './api';
import { createDataTransform } from '@/lib/store/api/utils';
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
      query: (params) => ({
        url: '/api/v2/schedule/events',
        method: 'GET',
        params,
      }),
      transformResponse: createDataTransform<ScheduleEvent[]>(),
      providesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Update schedule event (drag-drop)
    updateScheduleEvent: builder.mutation<
      ScheduleEvent,
      UpdateScheduleEventRequest
    >({
      query: ({ id, ...patch }) => ({
        url: `/api/v2/schedule/events/${id}`,
        method: 'PUT',
        body: patch,
      }),
      transformResponse: createDataTransform<ScheduleEvent>(),

      // Optimistic update
      async onQueryStarted({ id, ...patch }, { dispatch, queryFulfilled }) {
        // Update in cache immediately
        const patchResult = dispatch(
          scheduleApi.util.updateQueryData(
            'getScheduleEvents',
            undefined as any,
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

    // Trigger optimization
    triggerOptimization: builder.mutation<
      { jobId: string },
      { planId: string; useQuickPlace?: boolean }
    >({
      query: (body) => ({
        url: '/api/v2/schedule/optimize',
        method: 'POST',
        body,
      }),
      transformResponse: createDataTransform<{ jobId: string }>(),
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
  useUpdateScheduleEventMutation,
  useTriggerOptimizationMutation,
  useGetFocusTimeBlocksQuery,
  useCreateFocusTimeBlockMutation,
  useUpdateFocusTimeBlockMutation,
  useDeleteFocusTimeBlockMutation,
} = scheduleApi;
