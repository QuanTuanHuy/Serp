/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings users hook (organization-scoped)
 */

'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  useGetOrganizationUsersQuery,
  useSettingsCreateUserForOrganizationMutation,
  useUpdateOrganizationUserMutation,
} from '../services/users/usersApi';
import { useGetMyOrganizationQuery } from '../services/organizations/organizationsApi';
import type {
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
  UserProfile,
  UserStatus,
} from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';

type StatusFilter = UserStatus | 'PENDING' | 'all' | undefined;

export function useSettingsUsers() {
  const { success, error: showError } = useNotification();

  const [search, setSearch] = useState<string | undefined>(undefined);
  const [status, setStatus] = useState<StatusFilter>('all');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [sortBy, setSortBy] = useState<string | undefined>(undefined);
  const [sortDir, setSortDir] = useState<'ASC' | 'DESC' | undefined>(undefined);

  const { data: org, isFetching: isFetchingOrg } = useGetMyOrganizationQuery();
  const organizationId = org?.id;

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
      status: status === 'all' ? undefined : (status as UserStatus),
      page,
      pageSize,
      sortBy,
      sortDir,
    } as any,
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
  const [updateUser, updateStatus] = useUpdateOrganizationUserMutation();

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

  const users: UserProfile[] = response?.data.items || [];
  const pagination = useMemo(
    () => ({
      currentPage: response?.data.currentPage || 0,
      totalPages: response?.data.totalPages || 0,
      totalItems: response?.data.totalItems || 0,
    }),
    [response]
  );

  const handlePageChange = (newPage: number) => setPage(newPage);

  return {
    organizationId,
    filters: { search, status, page, pageSize, sortBy, sortDir },
    setSearch,
    setStatus,
    setPage,
    setPageSize,
    setSortBy,
    setSortDir,
    users,
    pagination,
    isLoading: isLoading || isFetchingOrg,
    isFetching,
    error,
    refetch,
    handlePageChange,
    create,
    update,
    createStatus,
    updateStatus,
  };
}

export type UseSettingsUsersReturn = ReturnType<typeof useSettingsUsers>;
