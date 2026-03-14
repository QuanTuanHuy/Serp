/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings users hook (organization-scoped)
 */

'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  useGetOrganizationUsersQuery,
  useGetUserStatsQuery,
  useGetInvitationsQuery,
  useSettingsCreateUserForOrganizationMutation,
  useUpdateOrganizationUserMutation,
  useUpdateUserStatusMutation,
  useUpdateUserRolesMutation,
  useUpdateUserTypeMutation,
  useResetUserPasswordMutation,
  useInviteUserMutation,
  useCancelInvitationMutation,
  useResendInvitationMutation,
} from '../services/users/usersApi';
import { useGetMyOrganizationQuery } from '../services/organizations/organizationsApi';
import type {
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
  UserProfile,
  UserStatus,
} from '@/modules/admin/types';
import type {
  InviteUserRequest,
  UpdateUserRolesRequest,
  UpdateUserTypeRequest,
} from '../types/user.types';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';

type StatusFilter = UserStatus | 'PENDING' | 'all' | undefined;

export function useSettingsUsers() {
  const { success, error: showError } = useNotification();

  // Filters
  const [search, setSearch] = useState<string | undefined>(undefined);
  const [status, setStatus] = useState<StatusFilter>('all');
  const [userType, setUserType] = useState<string | undefined>(undefined);
  const [roleId, setRoleId] = useState<number | undefined>(undefined);
  const [departmentId, setDepartmentId] = useState<number | undefined>(
    undefined
  );
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [sortBy, setSortBy] = useState<string | undefined>(undefined);
  const [sortDir, setSortDir] = useState<'ASC' | 'DESC' | undefined>(undefined);

  // Invitation filters
  const [invitationPage, setInvitationPage] = useState(0);
  const [invitationStatus, setInvitationStatus] = useState<string | undefined>(
    undefined
  );

  const { data: org, isFetching: isFetchingOrg } = useGetMyOrganizationQuery();
  const organizationId = org?.id;

  // Users query
  const {
    data: response,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      search,
      status: status === 'all' ? undefined : (status as string),
      userType,
      roleId,
      departmentId,
      page,
      pageSize,
      sortBy,
      sortDir,
    },
    { skip: !organizationId }
  );

  // Stats query
  const { data: statsResponse, isLoading: isLoadingStats } =
    useGetUserStatsQuery(
      { organizationId: organizationId as number },
      { skip: !organizationId }
    );

  // Invitations query
  const {
    data: invitationsResponse,
    isLoading: isLoadingInvitations,
    isFetching: isFetchingInvitations,
  } = useGetInvitationsQuery(
    {
      organizationId: organizationId as number,
      status: invitationStatus,
      page: invitationPage,
      pageSize: 10,
    },
    { skip: !organizationId }
  );

  useEffect(() => {
    if (error) {
      showError('Failed to load users');
    }
  }, [error, showError]);

  // Mutations
  const [createUser, createStatus] =
    useSettingsCreateUserForOrganizationMutation();
  const [updateUser, updateUserStatus] = useUpdateOrganizationUserMutation();
  const [changeStatus] = useUpdateUserStatusMutation();
  const [changeRoles] = useUpdateUserRolesMutation();
  const [changeType] = useUpdateUserTypeMutation();
  const [resetPwd] = useResetUserPasswordMutation();
  const [invite] = useInviteUserMutation();
  const [cancelInvite] = useCancelInvitationMutation();
  const [resendInvite] = useResendInvitationMutation();

  const create = useCallback(
    async (body: CreateUserForOrganizationRequest) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const res = await createUser({ organizationId, body }).unwrap();
        success('User created successfully');
        return res;
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, createUser, showError, success]
  );

  const update = useCallback(
    async (userId: number, body: UpdateUserInfoRequest) => {
      try {
        const res = await updateUser({ userId, body }).unwrap();
        success('User updated successfully');
        return res;
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [updateUser, showError, success]
  );

  const updateStatus = useCallback(
    async (userId: number, newStatus: string) => {
      if (!organizationId) return;
      try {
        await changeStatus({
          organizationId,
          userId,
          status: newStatus,
        }).unwrap();
        success(`User status updated to ${newStatus}`);
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, changeStatus, showError, success]
  );

  const updateRoles = useCallback(
    async (userId: number, body: UpdateUserRolesRequest) => {
      if (!organizationId) return;
      try {
        await changeRoles({ organizationId, userId, body }).unwrap();
        success('User roles updated');
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, changeRoles, showError, success]
  );

  const updateUserType = useCallback(
    async (userId: number, body: UpdateUserTypeRequest) => {
      if (!organizationId) return;
      try {
        await changeType({ organizationId, userId, body }).unwrap();
        success('User type updated');
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, changeType, showError, success]
  );

  const resetPassword = useCallback(
    async (userId: number) => {
      if (!organizationId) return;
      try {
        await resetPwd({ organizationId, userId }).unwrap();
        success('Password reset email sent');
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, resetPwd, showError, success]
  );

  const inviteUser = useCallback(
    async (body: InviteUserRequest) => {
      if (!organizationId) return;
      try {
        const res = await invite({ organizationId, body }).unwrap();
        success('Invitation sent successfully');
        return res;
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, invite, showError, success]
  );

  const cancelInvitation = useCallback(
    async (invitationId: number) => {
      if (!organizationId) return;
      try {
        await cancelInvite({ organizationId, invitationId }).unwrap();
        success('Invitation cancelled');
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, cancelInvite, showError, success]
  );

  const resendInvitation = useCallback(
    async (invitationId: number) => {
      if (!organizationId) return;
      try {
        await resendInvite({ organizationId, invitationId }).unwrap();
        success('Invitation resent');
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, resendInvite, showError, success]
  );

  const users: UserProfile[] = response?.data.items || [];
  const stats = statsResponse?.data;
  const invitations = invitationsResponse?.data.items || [];

  const pagination = useMemo(
    () => ({
      currentPage: response?.data.currentPage || 0,
      totalPages: response?.data.totalPages || 0,
      totalItems: response?.data.totalItems || 0,
    }),
    [response]
  );

  const invitationPagination = useMemo(
    () => ({
      currentPage: invitationsResponse?.data.currentPage || 0,
      totalPages: invitationsResponse?.data.totalPages || 0,
      totalItems: invitationsResponse?.data.totalItems || 0,
    }),
    [invitationsResponse]
  );

  const handlePageChange = (newPage: number) => setPage(newPage);

  return {
    organizationId,
    // User filters
    filters: {
      search,
      status,
      userType,
      roleId,
      departmentId,
      page,
      pageSize,
      sortBy,
      sortDir,
    },
    setSearch,
    setStatus,
    setUserType,
    setRoleId,
    setDepartmentId,
    setPage,
    setPageSize,
    setSortBy,
    setSortDir,
    // User data
    users,
    stats,
    pagination,
    isLoading: isLoading || isFetchingOrg,
    isLoadingStats,
    isFetching,
    error,
    refetch,
    handlePageChange,
    // User actions
    create,
    update,
    updateStatus,
    updateRoles,
    updateUserType,
    resetPassword,
    createStatus,
    // Invitation data & actions
    invitations,
    invitationPagination,
    isLoadingInvitations,
    isFetchingInvitations,
    invitationPage,
    setInvitationPage,
    invitationStatus,
    setInvitationStatus,
    inviteUser,
    cancelInvitation,
    resendInvitation,
  };
}

export type UseSettingsUsersReturn = ReturnType<typeof useSettingsUsers>;
