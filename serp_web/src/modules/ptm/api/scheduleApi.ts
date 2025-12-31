/**
 * PTM v2 - Schedule API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Schedule & calendar operations
 */

import { createDataTransform } from '@/lib/store/api/utils';
import { USE_MOCK_DATA, mockApiHandlers } from '../mocks/mockHandlers';
import type {
  SchedulePlan,
  ScheduleEvent,
  ScheduleTask,
  GetScheduleTasksResponse,
  FocusTimeBlock,
  CreateSchedulePlanRequest,
  UpdateScheduleEventRequest,
  UpdateScheduleTaskRequest,
  GetScheduleTasksParams,
  TriggerRescheduleRequest,
  PlanDetailResponse,
  PlanHistoryResponse,
  AvailabilityCalendar,
  CreateAvailabilityRequest,
  UpdateAvailabilityRequest,
} from '../types';
import { api } from '@/lib';

export const scheduleApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Get schedule plans
    getSchedulePlans: builder.query<SchedulePlan[], void>({
      query: () => ({
        url: '/schedule/plans',
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<SchedulePlan[]>(),
      providesTags: [{ type: 'ptm/Schedule', id: 'LIST' }],
    }),

    // Get active schedule plan
    getActiveSchedulePlan: builder.query<SchedulePlan, void>({
      query: () => ({
        url: '/schedule-plans/active',
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<SchedulePlan>(),
      providesTags: [{ type: 'ptm/Schedule', id: 'ACTIVE' }],
    }),

    // Get or create active plan
    getOrCreateActivePlan: builder.mutation<SchedulePlan, void>({
      query: () => ({
        url: '/schedule-plans',
        method: 'POST',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<SchedulePlan>(),
      invalidatesTags: [
        { type: 'ptm/Schedule', id: 'ACTIVE' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Get active plan detail (with events)
    getActivePlanDetail: builder.query<
      PlanDetailResponse,
      { fromDateMs: number; toDateMs: number }
    >({
      query: (params) => ({
        url: '/schedule-plans/active/detail',
        method: 'GET',
        params,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<PlanDetailResponse>(),
      providesTags: [{ type: 'ptm/Schedule', id: 'ACTIVE_DETAIL' }],
    }),

    // Get plan history
    getPlanHistory: builder.query<
      PlanHistoryResponse,
      { page?: number; pageSize?: number }
    >({
      query: (params) => ({
        url: '/schedule-plans/history',
        method: 'GET',
        params: { page: 1, pageSize: 10, ...params },
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<PlanHistoryResponse>(),
      providesTags: [{ type: 'ptm/Schedule', id: 'HISTORY' }],
    }),

    // Get plan by ID
    getPlanById: builder.query<SchedulePlan, number>({
      query: (id) => ({
        url: `/schedule-plans/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<SchedulePlan>(),
      providesTags: (_result, _error, id) => [
        { type: 'ptm/Schedule', id: `PLAN_${id}` },
      ],
    }),

    // Get plan with events
    getPlanWithEvents: builder.query<
      PlanDetailResponse,
      { id: number; fromDateMs: number; toDateMs: number }
    >({
      query: ({ id, ...params }) => ({
        url: `/schedule-plans/${id}/events`,
        method: 'GET',
        params,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<PlanDetailResponse>(),
      providesTags: (_result, _error, { id }) => [
        { type: 'ptm/Schedule', id: `PLAN_${id}_EVENTS` },
      ],
    }),

    // Trigger reschedule
    triggerReschedule: builder.mutation<SchedulePlan, TriggerRescheduleRequest>(
      {
        query: (body) => ({
          url: '/schedule-plans/reschedule',
          method: 'POST',
          body,
        }),
        extraOptions: { service: 'ptm' },
        transformResponse: createDataTransform<SchedulePlan>(),
        invalidatesTags: [
          { type: 'ptm/Schedule', id: 'HISTORY' },
          { type: 'ptm/Schedule', id: 'ACTIVE' },
        ],
      }
    ),

    // Apply proposed plan
    applyProposedPlan: builder.mutation<SchedulePlan, number>({
      query: (id) => ({
        url: `/schedule-plans/${id}/apply`,
        method: 'POST',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<SchedulePlan>(),
      invalidatesTags: [
        { type: 'ptm/Schedule', id: 'ACTIVE' },
        { type: 'ptm/Schedule', id: 'HISTORY' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),

    // Revert to plan
    revertToPlan: builder.mutation<SchedulePlan, number>({
      query: (id) => ({
        url: `/schedule-plans/${id}/revert`,
        method: 'POST',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<SchedulePlan>(),
      invalidatesTags: [
        { type: 'ptm/Schedule', id: 'ACTIVE' },
        { type: 'ptm/Schedule', id: 'HISTORY' },
      ],
    }),

    // Discard proposed plan
    discardProposedPlan: builder.mutation<void, number>({
      query: (id) => ({
        url: `/schedule-plans/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ptm' },
      invalidatesTags: [{ type: 'ptm/Schedule', id: 'HISTORY' }],
    }),

    // Create schedule plan
    createSchedulePlan: builder.mutation<
      SchedulePlan,
      CreateSchedulePlanRequest
    >({
      query: (body) => ({
        url: '/schedule/plans',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ptm' },
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

    // Update schedule event (drag-drop, pin, etc.)
    updateScheduleEvent: builder.mutation<
      ScheduleEvent,
      UpdateScheduleEventRequest & {
        dateRange?: { startDateMs: number; endDateMs: number };
      }
    >({
      query: ({ id, dateRange, ...patch }) => ({
        url: `/schedule-events/${id}/move`,
        method: 'POST',
        body: patch,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<ScheduleEvent>(),

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

    // Mark event complete
    completeScheduleEvent: builder.mutation<
      ScheduleEvent,
      { id: number; actualStartMin: number; actualEndMin: number }
    >({
      query: ({ id, ...body }) => ({
        url: `/schedule-events/${id}/complete`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<ScheduleEvent>(),
      invalidatesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Split event
    splitScheduleEvent: builder.mutation<
      { firstPart: ScheduleEvent; secondPart: ScheduleEvent },
      { id: number; splitPointMin: number }
    >({
      query: ({ id, ...body }) => ({
        url: `/schedule-events/${id}/split`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<{
        firstPart: ScheduleEvent;
        secondPart: ScheduleEvent;
      }>(),
      invalidatesTags: [{ type: 'ptm/Schedule', id: 'EVENTS' }],
    }),

    // Delete schedule event
    deleteScheduleEvent: builder.mutation<void, number>({
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
        url: '/schedule/focus-blocks',
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<FocusTimeBlock[]>(),
      providesTags: [{ type: 'ptm/FocusTime', id: 'LIST' }],
    }),

    // Create focus time block
    createFocusTimeBlock: builder.mutation<
      FocusTimeBlock,
      Partial<FocusTimeBlock>
    >({
      query: (body) => ({
        url: '/schedule/focus-blocks',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ptm' },
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
        url: `/schedule/focus-blocks/${id}`,
        method: 'PUT',
        body: patch,
      }),
      extraOptions: { service: 'ptm' },
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
        url: `/schedule/focus-blocks/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ptm' },
      invalidatesTags: (_result, _error, id) => [
        { type: 'ptm/FocusTime', id },
        { type: 'ptm/FocusTime', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),

    // Get availability calendar
    getAvailabilityCalendar: builder.query<AvailabilityCalendar[], void>({
      query: () => ({
        url: '/schedule/availability',
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<AvailabilityCalendar[]>(),
      providesTags: [{ type: 'ptm/Availability', id: 'LIST' }],
    }),

    // Set availability calendar (add/update specific days)
    setAvailabilityCalendar: builder.mutation<
      AvailabilityCalendar[],
      CreateAvailabilityRequest
    >({
      query: (body) => ({
        url: '/schedule/availability/set',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<AvailabilityCalendar[]>(),
      invalidatesTags: [
        { type: 'ptm/Availability', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),

    // Replace availability calendar (replace all for specific days)
    replaceAvailabilityCalendar: builder.mutation<
      AvailabilityCalendar[],
      UpdateAvailabilityRequest
    >({
      query: (body) => ({
        url: '/schedule/availability/replace',
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<AvailabilityCalendar[]>(),
      invalidatesTags: [
        { type: 'ptm/Availability', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),

    // Get schedule tasks (belonging to a specific plan)
    // Note: Each plan has its own snapshot of tasks. When PTM Task changes,
    // it syncs to active plan only. Proposed/archived plans maintain their
    // snapshots for comparison. This allows:
    // - Comparing different scheduling algorithms
    // - Reverting to previous plans without data loss
    // - Independent status per plan (PENDING/SCHEDULED/EXCLUDED)
    getScheduleTasks: builder.query<ScheduleTask[], GetScheduleTasksParams>({
      query: (params) => ({
        url: '/schedule-tasks',
        method: 'GET',
        params, // planId defaults to active plan in backend
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<ScheduleTask[]>(),
      providesTags: (result, error, params) =>
        result
          ? [
              ...result.map(({ id }) => ({
                type: 'ptm/ScheduleTask' as const,
                id,
              })),
              {
                type: 'ptm/ScheduleTask',
                id: `PLAN_${params.planId || 'ACTIVE'}`,
              },
            ]
          : [
              {
                type: 'ptm/ScheduleTask',
                id: `PLAN_${params.planId || 'ACTIVE'}`,
              },
            ],
    }),

    // Update schedule task constraints (within a plan)
    // Updates trigger reschedule for that plan only
    updateScheduleTask: builder.mutation<
      ScheduleTask,
      { id: number; updates: UpdateScheduleTaskRequest }
    >({
      query: ({ id, updates }) => ({
        url: `/schedule-tasks/${id}`,
        method: 'PUT',
        body: updates,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<ScheduleTask>(),
      invalidatesTags: (result, error, { id }) => [
        { type: 'ptm/ScheduleTask', id },
        { type: 'ptm/ScheduleTask', id: 'PLAN_ACTIVE' }, // Invalidate active plan tasks
        { type: 'ptm/Schedule', id: 'ACTIVE_DETAIL' },
        { type: 'ptm/Schedule', id: 'EVENTS' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetSchedulePlansQuery,
  useGetActiveSchedulePlanQuery,
  useGetOrCreateActivePlanMutation,
  useGetActivePlanDetailQuery,
  useGetPlanHistoryQuery,
  useGetPlanByIdQuery,
  useGetPlanWithEventsQuery,
  useTriggerRescheduleMutation,
  useApplyProposedPlanMutation,
  useRevertToPlanMutation,
  useDiscardProposedPlanMutation,
  useCreateSchedulePlanMutation,
  useGetScheduleEventsQuery,
  useCreateScheduleEventMutation,
  useUpdateScheduleEventMutation,
  useCompleteScheduleEventMutation,
  useSplitScheduleEventMutation,
  useDeleteScheduleEventMutation,
  useTriggerOptimizationMutation,
  useGetFocusTimeBlocksQuery,
  useCreateFocusTimeBlockMutation,
  useUpdateFocusTimeBlockMutation,
  useDeleteFocusTimeBlockMutation,
  useGetAvailabilityCalendarQuery,
  useSetAvailabilityCalendarMutation,
  useReplaceAvailabilityCalendarMutation,
  useGetScheduleTasksQuery,
  useUpdateScheduleTaskMutation,
} = scheduleApi;
