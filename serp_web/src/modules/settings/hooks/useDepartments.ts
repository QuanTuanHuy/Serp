/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings departments hook (organization-scoped)
 */

'use client';

import { useMemo, useState, useCallback, useEffect } from 'react';
import { useGetMyOrganizationQuery } from '../services/organizations/organizationsApi';
import {
  useGetDepartmentsQuery,
  useCreateDepartmentMutation,
  useUpdateDepartmentMutation,
  useDeleteDepartmentMutation,
  useGetDepartmentByIdQuery,
  useGetDepartmentMembersQuery,
  useAssignUserToDepartmentMutation,
  useBulkAssignUsersToDepartmentMutation,
  useRemoveUserFromDepartmentMutation,
  useGetDepartmentStatisticsQuery,
} from '../services/departments/departmentsApi';
import { useGetAccessibleModulesForOrganizationQuery } from '../services/modules/modulesApi';
import { useGetOrganizationUsersQuery } from '../services/users/usersApi';
import type {
  Department,
  CreateDepartmentRequest,
  UpdateDepartmentRequest,
  AssignUserToDepartmentRequest,
  BulkAssignUsersToDepartmentRequest,
} from '../types/department.types';
import { getErrorMessage, isSuccessResponse } from '@/lib/store/api/utils';
import { useNotification } from '@/shared/hooks/use-notification';
import { useDebounce } from '@/shared/hooks/use-debounce';
import { useAuth } from '@/modules/account';

interface DepartmentFilters {
  isActive?: boolean;
  parentDepartmentId?: number;
  managerId?: number;
}

export function useSettingsDepartments() {
  const { success, error: showError } = useNotification();

  const { user } = useAuth();
  const organizationId = user?.organizationId;

  const [search, setSearch] = useState('');
  const [filters, setFilters] = useState<DepartmentFilters>({});
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(50);

  const debouncedSearch = useDebounce(search, 500);

  const {
    data: departmentsData,
    isLoading: isLoadingDepartments,
    isFetching: isFetchingDepartments,
    error: departmentsError,
    refetch: refetchDepartments,
  } = useGetDepartmentsQuery(
    {
      organizationId: organizationId!,
      page,
      pageSize,
      search: debouncedSearch || undefined,
      sortBy: 'name',
      sortDir: 'asc',
      ...filters,
    },
    { skip: !organizationId }
  );

  const { data: statsData, refetch: refetchStats } =
    useGetDepartmentStatisticsQuery(
      { organizationId: organizationId! },
      { skip: !organizationId }
    );

  const { data: modulesData } = useGetAccessibleModulesForOrganizationQuery(
    organizationId as number,
    { skip: !organizationId }
  );

  const { data: usersData } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId!,
      page: 0,
      pageSize: 100,
      status: 'ACTIVE',
    },
    { skip: !organizationId }
  );

  useEffect(() => {
    if (departmentsError) {
      showError('Failed to load departments');
    }
  }, [departmentsError, showError]);

  const departments = useMemo(
    () =>
      departmentsData && isSuccessResponse(departmentsData)
        ? departmentsData.data.items
        : [],
    [departmentsData]
  );

  const totalPages = useMemo(
    () =>
      departmentsData && isSuccessResponse(departmentsData)
        ? departmentsData.data.totalPages
        : 0,
    [departmentsData]
  );

  const totalItems = useMemo(
    () =>
      departmentsData && isSuccessResponse(departmentsData)
        ? departmentsData.data.totalItems
        : 0,
    [departmentsData]
  );

  const statistics = useMemo(
    () => (statsData && isSuccessResponse(statsData) ? statsData.data : null),
    [statsData]
  );

  const activeModules = useMemo(
    () => (modulesData || []).filter((m) => m.isActive),
    [modulesData]
  );

  const managers = useMemo(
    () =>
      usersData && isSuccessResponse(usersData) && usersData.data.items
        ? usersData.data.items.map((u) => ({
            id: u.id,
            name: `${u.firstName || ''} ${u.lastName || ''}`.trim() || u.email,
            email: u.email,
          }))
        : [],
    [usersData]
  );

  const activeDepartments = useMemo(
    () => departments.filter((d) => d.isActive),
    [departments]
  );

  // Mutations
  const [createDepartment, createStatus] = useCreateDepartmentMutation();
  const [updateDepartment, updateStatus] = useUpdateDepartmentMutation();
  const [deleteDepartment, deleteStatus] = useDeleteDepartmentMutation();
  const [assignUser, assignStatus] = useAssignUserToDepartmentMutation();
  const [bulkAssignUsers, bulkAssignStatus] =
    useBulkAssignUsersToDepartmentMutation();
  const [removeUser, removeStatus] = useRemoveUserFromDepartmentMutation();

  const create = useCallback(
    async (request: CreateDepartmentRequest) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await createDepartment({
          organizationId,
          body: request,
        }).unwrap();

        if (isSuccessResponse(result)) {
          success('Department created successfully');
          refetchDepartments();
          refetchStats();
          return result.data;
        } else {
          throw new Error(result.message);
        }
      } catch (e: any) {
        const msg = getErrorMessage(e);
        showError(msg);
        throw e;
      }
    },
    [
      createDepartment,
      organizationId,
      showError,
      success,
      refetchDepartments,
      refetchStats,
    ]
  );

  const update = useCallback(
    async (departmentId: number, request: UpdateDepartmentRequest) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await updateDepartment({
          organizationId,
          departmentId,
          body: request,
        }).unwrap();

        if (isSuccessResponse(result)) {
          success('Department updated successfully');
          refetchDepartments();
          refetchStats();
          return result.data;
        } else {
          throw new Error(result.message);
        }
      } catch (e: any) {
        const msg = getErrorMessage(e);
        showError(msg);
        throw e;
      }
    },
    [
      updateDepartment,
      organizationId,
      showError,
      success,
      refetchDepartments,
      refetchStats,
    ]
  );

  const remove = useCallback(
    async (departmentId: number) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await deleteDepartment({
          organizationId,
          departmentId,
        }).unwrap();

        if (isSuccessResponse(result)) {
          success('Department deleted successfully');
          refetchDepartments();
          refetchStats();
        } else {
          throw new Error(result.message);
        }
      } catch (e: any) {
        const msg = getErrorMessage(e);
        showError(msg);
        throw e;
      }
    },
    [
      deleteDepartment,
      organizationId,
      showError,
      success,
      refetchDepartments,
      refetchStats,
    ]
  );

  const assignUserToDept = useCallback(
    async (departmentId: number, request: AssignUserToDepartmentRequest) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        await assignUser({
          organizationId,
          departmentId,
          body: request,
        }).unwrap();
        success('User assigned to department');
        refetchDepartments();
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [assignUser, organizationId, showError, success, refetchDepartments]
  );

  const bulkAssignUsersToDept = useCallback(
    async (
      departmentId: number,
      request: BulkAssignUsersToDepartmentRequest
    ) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        await bulkAssignUsers({
          organizationId,
          departmentId,
          body: request,
        }).unwrap();
        success('Users assigned to department');
        refetchDepartments();
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [bulkAssignUsers, organizationId, showError, success, refetchDepartments]
  );

  const removeUserFromDept = useCallback(
    async (departmentId: number, userId: number) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        await removeUser({
          organizationId,
          departmentId,
          userId,
        }).unwrap();
        success('User removed from department');
        refetchDepartments();
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [removeUser, organizationId, showError, success, refetchDepartments]
  );

  const setActiveFilter = useCallback((isActive?: boolean) => {
    setFilters((prev) => ({ ...prev, isActive }));
    setPage(0);
  }, []);

  const setParentFilter = useCallback((parentDepartmentId?: number) => {
    setFilters((prev) => ({ ...prev, parentDepartmentId }));
    setPage(0);
  }, []);

  const setManagerFilter = useCallback((managerId?: number) => {
    setFilters((prev) => ({ ...prev, managerId }));
    setPage(0);
  }, []);

  const clearFilters = useCallback(() => {
    setFilters({});
    setSearch('');
    setPage(0);
  }, []);

  const useDepartmentById = (departmentId?: number) =>
    useGetDepartmentByIdQuery(
      {
        organizationId: organizationId!,
        departmentId: departmentId!,
      },
      {
        skip: !organizationId || !departmentId,
      }
    );

  const useDepartmentMembers = (departmentId?: number) =>
    useGetDepartmentMembersQuery(
      {
        organizationId: organizationId!,
        departmentId: departmentId!,
      },
      {
        skip: !organizationId || !departmentId,
      }
    );

  return {
    organizationId,
    isLoading: isLoadingDepartments,
    isFetching: isFetchingDepartments,
    error: departmentsError,
    departments,
    activeDepartments,
    totalPages,
    totalItems,
    currentPage: page,
    pageSize,
    statistics,
    activeModules,
    managers,
    search,
    filters,
    setSearch,
    setPage,
    setPageSize,
    setActiveFilter,
    setParentFilter,
    setManagerFilter,
    clearFilters,
    refetch: refetchDepartments,
    create,
    update,
    remove,
    assignUserToDept,
    bulkAssignUsersToDept,
    removeUserFromDept,
    createStatus,
    updateStatus,
    deleteStatus,
    assignStatus,
    bulkAssignStatus,
    removeStatus,
    useDepartmentById,
    useDepartmentMembers,
  } as const;
}

export type UseSettingsDepartmentsReturn = ReturnType<
  typeof useSettingsDepartments
>;
