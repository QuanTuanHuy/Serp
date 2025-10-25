/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - useRoles hook for Roles page
 */

'use client';

import { useMemo, useCallback, useEffect } from 'react';
import {
  useGetAllRolesQuery,
  useCreateRoleMutation,
  useUpdateRoleMutation,
  useDeleteRoleMutation,
} from '@/modules/admin/services/roles/rolesApi';
import type {
  Role,
  RoleFilters,
  CreateRoleRequest,
  UpdateRoleRequest,
} from '@/modules/admin/types';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  selectRolesFilters,
  setRolesFilters,
  setRolesSearch,
  setRolesScope,
  setRolesRoleType,
  setRolesOrganizationId,
  setRolesModuleId,
  setRolesIsDefault,
  setRolesPage,
  setRolesPageSize,
  setRolesSort,
} from '@/modules/admin/store';
import { useNotification } from '@/shared/hooks/use-notification';

export function useRoles() {
  const dispatch = useAppDispatch();
  const { success, error: showError } = useNotification();

  const filters = useAppSelector(selectRolesFilters);

  const {
    data: response,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetAllRolesQuery();

  // Mutations
  const [createRole, { isLoading: isCreating }] = useCreateRoleMutation();
  const [updateRole, { isLoading: isUpdating }] = useUpdateRoleMutation();
  const [deleteRole, { isLoading: isDeleting }] = useDeleteRoleMutation();

  useEffect(() => {
    if (error) {
      let errorMessage = 'Failed to load roles. Please try again.';

      if ('message' in error && error.message) {
        errorMessage = error.message;
      } else if ('status' in error && error.status) {
        errorMessage = `Failed to load roles: ${error.status}`;
      }

      showError(errorMessage);
    }
  }, [error, showError]);

  const { roles, pagination } = useMemo(() => {
    const allRoles = response || [];

    let filtered = allRoles;

    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(
        (role) =>
          role.name.toLowerCase().includes(searchLower) ||
          role.description?.toLowerCase().includes(searchLower)
      );
    }

    if (filters.scope) {
      filtered = filtered.filter((role) => role.scope === filters.scope);
    }

    if (filters.roleType) {
      filtered = filtered.filter((role) => role.roleType === filters.roleType);
    }

    if (filters.organizationId !== undefined) {
      filtered = filtered.filter(
        (role) => role.organizationId === filters.organizationId
      );
    }

    if (filters.moduleId !== undefined) {
      filtered = filtered.filter((role) => role.moduleId === filters.moduleId);
    }

    if (filters.isDefault !== undefined) {
      filtered = filtered.filter(
        (role) => role.isDefault === filters.isDefault
      );
    }

    const sortBy = filters.sortBy || 'id';
    const sortDir = filters.sortDir || 'DESC';
    filtered = [...filtered].sort((a, b) => {
      const aVal = a[sortBy as keyof Role];
      const bVal = b[sortBy as keyof Role];

      if (aVal === undefined || bVal === undefined) return 0;

      let comparison = 0;
      if (aVal < bVal) comparison = -1;
      if (aVal > bVal) comparison = 1;

      return sortDir === 'DESC' ? -comparison : comparison;
    });

    // Pagination
    const pageSize = filters.pageSize || 10;
    const currentPage = filters.page || 0;
    const totalItems = filtered.length;
    const totalPages = Math.ceil(totalItems / pageSize);

    const startIndex = currentPage * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedRoles = filtered.slice(startIndex, endIndex);

    return {
      roles: paginatedRoles,
      pagination: {
        totalPages,
        currentPage,
        totalItems,
      },
    };
  }, [response, filters]);

  // Handlers
  const handleSearch = useCallback(
    (search: string) => dispatch(setRolesSearch(search || undefined)),
    [dispatch]
  );

  const handleFilterChange = useCallback(
    (key: keyof RoleFilters, value: any) => {
      switch (key) {
        case 'scope':
          dispatch(setRolesScope(value || undefined));
          break;
        case 'roleType':
          dispatch(setRolesRoleType(value || undefined));
          break;
        case 'organizationId':
          dispatch(setRolesOrganizationId(value || undefined));
          break;
        case 'moduleId':
          dispatch(setRolesModuleId(value || undefined));
          break;
        case 'isDefault':
          dispatch(setRolesIsDefault(value));
          break;
        case 'page':
          dispatch(setRolesPage(value as number));
          break;
        case 'pageSize':
          dispatch(setRolesPageSize(value as number));
          break;
        case 'sortBy':
        case 'sortDir':
          dispatch(
            setRolesSort({
              sortBy: (key === 'sortBy' ? value : filters.sortBy) as string,
              sortDir: (key === 'sortDir' ? value : filters.sortDir) as
                | 'ASC'
                | 'DESC',
            })
          );
          break;
        default:
          dispatch(setRolesFilters({ [key]: value } as any));
      }
    },
    [dispatch, filters.sortBy, filters.sortDir]
  );

  const handlePageChange = useCallback(
    (newPage: number) => dispatch(setRolesPage(newPage)),
    [dispatch]
  );

  const handleRefetch = useCallback(async () => {
    try {
      await refetch();
      success('Roles refreshed successfully');
    } catch (err) {
      showError('Failed to refresh roles');
    }
  }, [refetch, success, showError]);

  const handleCreateRole = useCallback(
    async (data: CreateRoleRequest) => {
      try {
        await createRole(data).unwrap();
        success('Role created successfully');
        await refetch();
      } catch (err: any) {
        const errorMessage =
          err?.data?.message || 'Failed to create role. Please try again.';
        showError(errorMessage);
        throw err;
      }
    },
    [createRole, refetch, success, showError]
  );

  const handleUpdateRole = useCallback(
    async (roleId: number, data: UpdateRoleRequest) => {
      try {
        await updateRole({ roleId, data }).unwrap();
        success('Role updated successfully');
        await refetch();
      } catch (err: any) {
        const errorMessage =
          err?.data?.message || 'Failed to update role. Please try again.';
        showError(errorMessage);
        throw err;
      }
    },
    [updateRole, refetch, success, showError]
  );

  const handleDeleteRole = useCallback(
    async (roleId: number) => {
      try {
        await deleteRole(roleId).unwrap();
        success('Role deleted successfully');
        await refetch();
      } catch (err: any) {
        const errorMessage =
          err?.data?.message || 'Failed to delete role. Please try again.';
        showError(errorMessage);
        throw err;
      }
    },
    [deleteRole, refetch, success, showError]
  );

  return {
    filters,
    roles,
    pagination,
    isLoading,
    isFetching,
    error,
    refetch: handleRefetch,
    handleSearch,
    handleFilterChange,
    handlePageChange,
    // Mutations
    handleCreateRole,
    handleUpdateRole,
    handleDeleteRole,
    isCreating,
    isUpdating,
    isDeleting,
  };
}

export type UseRolesReturn = ReturnType<typeof useRoles>;
