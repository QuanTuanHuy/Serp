// CRM Team API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import type { ApiResponse as BaseApiResponse } from '@/lib/store/api/types';
import type {
  Team,
  TeamMember,
  TeamFilters,
  CreateTeamRequest,
  UpdateTeamRequest,
  CreateTeamMemberRequest,
  UpdateTeamMemberRequest,
  ChangeManagerRequest,
  TeamTerritoryResponse,
  AssignTerritoriesRequest,
  PaginationParams,
  APIResponse,
} from '../types';

export const teamApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Team endpoints
    getTeams: builder.query<
      BaseApiResponse<{
        items: Team[];
        pagination: {
          page: number;
          size: number;
          totalItems: number;
          totalPages: number;
          hasNext: boolean;
          hasPrevious: boolean;
        };
      }>,
      { filters?: TeamFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/teams',
        method: 'GET',
        params: {
          page: pagination.page,
          size: pagination.limit,
          status: filters.status,
        },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: (response: any, _meta, arg) => ({
        code: response.code,
        status: response.status,
        message: response.message,
        data: {
          items: response.data?.items ?? [],
          pagination: {
            page: response.data?.pagination?.page ?? 1,
            size: response.data?.pagination?.size ?? arg.pagination.limit ?? 20,
            totalItems: response.data?.pagination?.totalItems ?? 0,
            totalPages: response.data?.pagination?.totalPages ?? 0,
            hasNext: response.data?.pagination?.hasNext ?? false,
            hasPrevious: response.data?.pagination?.hasPrevious ?? false,
          },
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'Team' as const,
                id,
              })),
              { type: 'Team', id: 'LIST' },
            ]
          : [{ type: 'Team', id: 'LIST' }],
    }),

    getTeam: builder.query<APIResponse<Team>, string>({
      query: (id) => ({ url: `/teams/${id}`, method: 'GET' }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, id) => [{ type: 'Team', id }],
    }),

    createTeam: builder.mutation<APIResponse<Team>, CreateTeamRequest>({
      query: (data) => ({ url: '/teams', method: 'POST', body: data }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Team', id: 'LIST' }],
    }),

    updateTeam: builder.mutation<
      APIResponse<Team>,
      { id: string; data: UpdateTeamRequest }
    >({
      query: ({ id, data }) => ({
        url: `/teams/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { id }) => [
        { type: 'Team', id },
        { type: 'Team', id: 'LIST' },
      ],
    }),

    deleteTeam: builder.mutation<APIResponse<null>, string>({
      query: (id) => ({ url: `/teams/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Team', id: 'LIST' }],
    }),

    getTeamMembers: builder.query<
      BaseApiResponse<{
        items: TeamMember[];
        pagination: {
          page: number;
          size: number;
          totalItems: number;
          totalPages: number;
          hasNext: boolean;
          hasPrevious: boolean;
        };
      }>,
      { teamId: string; page?: number; size?: number }
    >({
      query: ({ teamId, page = 1, size = 20 }) => ({
        url: `/teams/${teamId}/members`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: (response: any) => ({
        code: response.code,
        status: response.status,
        message: response.message,
        data: {
          items: response.data?.items ?? [],
          pagination: {
            page: response.data?.pagination?.page ?? 1,
            size: response.data?.pagination?.size ?? 20,
            totalItems: response.data?.pagination?.totalItems ?? 0,
            totalPages: response.data?.pagination?.totalPages ?? 0,
            hasNext: response.data?.pagination?.hasNext ?? false,
            hasPrevious: response.data?.pagination?.hasPrevious ?? false,
          },
        },
      }),
      providesTags: (result, error, { teamId }) => [
        { type: 'Team', id: `${teamId}-MEMBERS` },
      ],
    }),

    addTeamMember: builder.mutation<
      APIResponse<TeamMember>,
      {
        teamId: string;
        data: CreateTeamMemberRequest;
      }
    >({
      query: ({ teamId, data }) => ({
        url: `/teams/${teamId}/members`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { teamId }) => [
        { type: 'Team', id: `${teamId}-MEMBERS` },
      ],
    }),

    updateTeamMember: builder.mutation<
      APIResponse<TeamMember>,
      {
        teamId: string;
        memberId: string;
        data: UpdateTeamMemberRequest;
      }
    >({
      query: ({ teamId, memberId, data }) => ({
        url: `/teams/${teamId}/members/${memberId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { teamId }) => [
        { type: 'Team', id: `${teamId}-MEMBERS` },
      ],
    }),

    removeTeamMember: builder.mutation<
      APIResponse<null>,
      { teamId: string; memberId: string }
    >({
      query: ({ teamId, memberId }) => ({
        url: `/teams/${teamId}/members/${memberId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { teamId }) => [
        { type: 'Team', id: `${teamId}-MEMBERS` },
      ],
    }),

    getTeamTerritories: builder.query<
      APIResponse<TeamTerritoryResponse>,
      string
    >({
      query: (teamId) => ({
        url: `/teams/${teamId}/territories`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, teamId) => [
        { type: 'Team', id: `${teamId}-TERRITORIES` },
      ],
    }),

    assignTerritories: builder.mutation<
      APIResponse<TeamTerritoryResponse>,
      {
        teamId: string;
        data: AssignTerritoriesRequest;
      }
    >({
      query: ({ teamId, data }) => ({
        url: `/teams/${teamId}/territories`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { teamId }) => [
        { type: 'Team', id: `${teamId}-TERRITORIES` },
        { type: 'Territory', id: 'LIST' },
      ],
    }),

    changeTeamManager: builder.mutation<
      APIResponse<Team>,
      {
        teamId: string;
        data: ChangeManagerRequest;
      }
    >({
      query: ({ teamId, data }) => ({
        url: `/teams/${teamId}/manager`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { teamId }) => [
        { type: 'Team', id: teamId },
        { type: 'Team', id: `${teamId}-MEMBERS` },
      ],
    }),
  }),
});

export const {
  useGetTeamsQuery,
  useGetTeamQuery,
  useCreateTeamMutation,
  useUpdateTeamMutation,
  useDeleteTeamMutation,
  useGetTeamMembersQuery,
  useAddTeamMemberMutation,
  useUpdateTeamMemberMutation,
  useRemoveTeamMemberMutation,
  useGetTeamTerritoriesQuery,
  useAssignTerritoriesMutation,
  useChangeTeamManagerMutation,
} = teamApi;
