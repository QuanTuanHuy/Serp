/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin users hook
 */

'use client';

import { useMemo, useCallback, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  selectUsersUiState,
  openCreateUserDialog,
  openEditUserDialog,
  closeUserDialog,
  selectSelectedOrganizationId,
  selectSelectedUserId,
  // Filters
  selectUsersFilters,
  setUsersFilters,
  setUsersSearch,
  setUsersStatus,
  setUsersOrganizationId,
  setUsersPage,
  setUsersPageSize,
  setUsersSort,
} from '../store';
import {
  useGetUsersQuery,
  useCreateUserForOrganizationMutation,
  useUpdateUserInfoMutation,
} from '../services/users/usersApi';
import type {
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
  UserFilters,
  UserProfile,
} from '../types';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';

export function useUsers() {
  const dispatch = useAppDispatch();
  const { success, error: showError } = useNotification();

  const ui = useAppSelector(selectUsersUiState);
  const selectedOrgId = useAppSelector(selectSelectedOrganizationId);
  const selectedUserId = useAppSelector(selectSelectedUserId);
  const filters = useAppSelector(selectUsersFilters);

  // Query
  const {
    data: response,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetUsersQuery(filters);

  // Mutations
  const [createUser, createUserStatus] = useCreateUserForOrganizationMutation();
  const [updateUser, updateUserStatus] = useUpdateUserInfoMutation();

  useEffect(() => {
    if (error) {
      let errorMessage = 'Failed to load users. Please try again.';
      if (typeof error === 'object' && error && 'status' in error) {
        errorMessage = `Failed to load users: ${(error as any).status}`;
      }
      showError(errorMessage);
    }
  }, [error, showError]);

  const users = response?.data.items || [];
  const pagination = useMemo(
    () => ({
      totalPages: response?.data.totalPages || 0,
      currentPage: response?.data.currentPage || 0,
      totalItems: response?.data.totalItems || 0,
    }),
    [response]
  );

  const handleSearch = useCallback(
    (search: string) => dispatch(setUsersSearch(search || undefined)),
    [dispatch]
  );

  const handleFilterChange = useCallback(
    (key: keyof UserFilters, value: any) => {
      switch (key) {
        case 'status':
          dispatch(setUsersStatus(value || undefined));
          break;
        case 'organizationId':
          dispatch(setUsersOrganizationId(value || undefined));
          break;
        case 'page':
          dispatch(setUsersPage(value as number));
          break;
        case 'pageSize':
          dispatch(setUsersPageSize(value as number));
          break;
        case 'sortBy':
        case 'sortDir':
          dispatch(
            setUsersSort({
              sortBy: (key === 'sortBy' ? value : filters.sortBy) as string,
              sortDir: (key === 'sortDir' ? value : filters.sortDir) as
                | 'ASC'
                | 'DESC',
            })
          );
          break;
        default:
          dispatch(setUsersFilters({ [key]: value } as any));
      }
    },
    [dispatch, filters.sortBy, filters.sortDir]
  );

  const handlePageChange = useCallback(
    (newPage: number) => dispatch(setUsersPage(newPage)),
    [dispatch]
  );

  const handleRefetch = useCallback(async () => {
    try {
      await refetch();
      success('Users refreshed successfully');
    } catch (err) {
      showError('Failed to refresh users');
    }
  }, [refetch, success, showError]);

  // Dialog controls
  const openCreate = (organizationId?: number) =>
    dispatch(openCreateUserDialog({ organizationId }));
  const openEdit = (userId: number) => dispatch(openEditUserDialog({ userId }));
  const closeDialog = () => dispatch(closeUserDialog());

  // Mutations wrappers
  const create = async (
    organizationId: number,
    body: CreateUserForOrganizationRequest
  ) => {
    const orgId = organizationId ?? selectedOrgId;
    if (!orgId) {
      const msg = 'Organization ID is required to create a user';
      showError(msg);
      throw new Error(msg);
    }
    try {
      const res = await createUser({ organizationId: orgId, body }).unwrap();
      success('User created successfully');
      return res;
    } catch (err: any) {
      showError(getErrorMessage(err));
      throw err;
    }
  };

  const update = async (userId: number, body: UpdateUserInfoRequest) => {
    const id = userId ?? selectedUserId;
    if (!id) {
      const msg = 'User ID is required to update user';
      showError(msg);
      throw new Error(msg);
    }
    try {
      const res = await updateUser({ userId: id, body }).unwrap();
      success('User updated successfully');
      return res;
    } catch (err: any) {
      showError(getErrorMessage(err));
      throw err;
    }
  };

  return {
    // State
    filters,
    users,
    pagination,
    isLoading,
    isFetching,
    error,
    // Controls
    refetch: handleRefetch,
    handleSearch,
    handleFilterChange,
    handlePageChange,
    // Dialog
    ui,
    openCreate,
    openEdit,
    closeDialog,
    // Mutations
    create,
    update,
    createUserStatus,
    updateUserStatus,
  };
}

export type UseUsersReturn = ReturnType<typeof useUsers>;
