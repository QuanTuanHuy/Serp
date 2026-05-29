/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM skill API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  PMCreateSkillRequest,
  PMReplaceUserSkillsRequest,
  PMReplaceWorkItemSkillsRequest,
  PMSkillApi,
  PMUpdateSkillRequest,
  PMUserSkillApi,
  PMUserSkillsByUserApi,
  PMWorkItemSkillApi,
} from '../types/api';

export const pmSkillApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPmSkills: builder.query<PMSkillApi[], void>({
      query: () => ({
        url: '/skills',
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMSkillApi[]>(),
      providesTags: [{ type: 'pm/Skill' as const, id: 'LIST' }],
    }),

    createPmSkill: builder.mutation<PMSkillApi, PMCreateSkillRequest>({
      query: (body) => ({
        url: '/skills',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMSkillApi>(),
      invalidatesTags: [{ type: 'pm/Skill' as const, id: 'LIST' }],
    }),

    updatePmSkill: builder.mutation<
      PMSkillApi,
      { skillId: number; body: PMUpdateSkillRequest }
    >({
      query: ({ skillId, body }) => ({
        url: `/skills/${skillId}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMSkillApi>(),
      invalidatesTags: (_result, _error, { skillId }) => [
        { type: 'pm/Skill' as const, id: 'LIST' },
        { type: 'pm/Skill' as const, id: skillId },
      ],
    }),

    archivePmSkill: builder.mutation<PMSkillApi, number>({
      query: (skillId) => ({
        url: `/skills/${skillId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMSkillApi>(),
      invalidatesTags: (_result, _error, skillId) => [
        { type: 'pm/Skill' as const, id: 'LIST' },
        { type: 'pm/Skill' as const, id: skillId },
      ],
    }),

    getPmUserSkills: builder.query<PMUserSkillApi[], number>({
      query: (userId) => ({
        url: `/users/${userId}/skills`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMUserSkillApi[]>(),
      providesTags: (_result, _error, userId) => [
        { type: 'pm/UserSkill' as const, id: userId },
      ],
    }),

    getPmUsersSkills: builder.query<PMUserSkillsByUserApi, number[]>({
      query: (userIds) => ({
        url: '/users/skills',
        method: 'GET',
        params: { userIds: userIds.join(',') },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMUserSkillsByUserApi>(),
      providesTags: (_result, _error, userIds) => [
        ...userIds.map((userId) => ({
          type: 'pm/UserSkill' as const,
          id: userId,
        })),
        { type: 'pm/UserSkill' as const, id: 'BATCH' },
      ],
    }),

    replacePmUserSkills: builder.mutation<
      PMUserSkillApi[],
      { userId: number; body: PMReplaceUserSkillsRequest }
    >({
      query: ({ userId, body }) => ({
        url: `/users/${userId}/skills`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMUserSkillApi[]>(),
      invalidatesTags: (_result, _error, { userId }) => [
        { type: 'pm/UserSkill' as const, id: userId },
      ],
    }),

    getPmWorkItemSkills: builder.query<
      PMWorkItemSkillApi[],
      { projectId: number; workItemId: number }
    >({
      query: ({ projectId, workItemId }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/skills`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemSkillApi[]>(),
      providesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItemSkill' as const, id: workItemId },
      ],
    }),

    replacePmWorkItemSkills: builder.mutation<
      PMWorkItemSkillApi[],
      {
        projectId: number;
        workItemId: number;
        body: PMReplaceWorkItemSkillsRequest;
      }
    >({
      query: ({ projectId, workItemId, body }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/skills`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemSkillApi[]>(),
      invalidatesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItemSkill' as const, id: workItemId },
        { type: 'pm/WorkItem' as const, id: workItemId },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useArchivePmSkillMutation,
  useCreatePmSkillMutation,
  useGetPmSkillsQuery,
  useGetPmUserSkillsQuery,
  useGetPmUsersSkillsQuery,
  useGetPmWorkItemSkillsQuery,
  useReplacePmUserSkillsMutation,
  useReplacePmWorkItemSkillsMutation,
  useUpdatePmSkillMutation,
} = pmSkillApi;
