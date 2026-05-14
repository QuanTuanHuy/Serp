/**
 * CRM Meeting Request API — scheduling queue backed by `/meeting-requests`.
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project
 */

import { api } from '@/lib/store/api';
import {
  mapMeetingRequestListResponse,
  mapSingleMeetingRequestResponse,
} from './mappers';
import type {
  APIResponse,
  CreateMeetingRequestPayload,
  MeetingRequest,
  MeetingRequestStatus,
  PaginatedResponse,
  PaginationParams,
} from '../types';

export const meetingRequestApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMeetingRequests: builder.query<
      APIResponse<PaginatedResponse<MeetingRequest>>,
      {
        pagination: PaginationParams;
        status?: MeetingRequestStatus;
      }
    >({
      query: ({ pagination, status }) => ({
        url: '/meeting-requests',
        method: 'GET',
        params: {
          page: pagination.page,
          size: pagination.limit,
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapMeetingRequestListResponse,
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'MeetingRequest' as const,
                id,
              })),
              { type: 'MeetingRequest', id: 'LIST' },
            ]
          : [{ type: 'MeetingRequest', id: 'LIST' }],
    }),

    getMeetingRequest: builder.query<APIResponse<MeetingRequest>, string>({
      query: (id) => ({
        url: `/meeting-requests/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleMeetingRequestResponse,
      providesTags: (result, error, id) => [{ type: 'MeetingRequest', id }],
    }),

    createMeetingRequest: builder.mutation<
      APIResponse<MeetingRequest>,
      CreateMeetingRequestPayload
    >({
      query: (body) => ({
        url: '/meeting-requests',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleMeetingRequestResponse,
      invalidatesTags: [{ type: 'MeetingRequest', id: 'LIST' }],
    }),

    cancelMeetingRequest: builder.mutation<APIResponse<MeetingRequest>, string>(
      {
        query: (id) => ({
          url: `/meeting-requests/${id}/cancel`,
          method: 'PUT',
        }),
        extraOptions: { service: 'crm' },
        transformResponse: mapSingleMeetingRequestResponse,
        invalidatesTags: (result, error, id) => [
          { type: 'MeetingRequest', id },
          { type: 'MeetingRequest', id: 'LIST' },
          { type: 'Activity', id: 'LIST' },
        ],
      }
    ),
  }),
});

export const {
  useGetMeetingRequestsQuery,
  useGetMeetingRequestQuery,
  useLazyGetMeetingRequestQuery,
  useCreateMeetingRequestMutation,
  useCancelMeetingRequestMutation,
} = meetingRequestApi;
