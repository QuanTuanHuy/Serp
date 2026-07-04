/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings modules hook (organization-scoped)
 */

'use client';

import { useMemo, useState, useCallback, useEffect, useRef } from 'react';
import { useGetMyOrganizationQuery } from '../services/organizations/organizationsApi';
import {
  useGetAccessibleModulesForOrganizationQuery,
  useAssignUserToModuleMutation,
  useRevokeUserAccessToModuleMutation,
  useGetModuleRolesQuery,
  useGetModuleUsersQuery,
  useUpdateModuleAccessSettingsMutation,
  useBackfillModuleAutoGrantMutation,
  useBulkAssignUsersToModuleMutation,
  useBulkRevokeUsersFromModuleMutation,
} from '../services/modules/modulesApi';
import type { AccessibleModule } from '@/modules/settings/types/module-access.types';
import { getErrorMessage, getResponseMessage } from '@/lib/store/api/utils';
import { useNotification } from '@/shared/hooks/use-notification';

export function useSettingsModules(options?: { skipQuery?: boolean }) {
  const { success, error: showError } = useNotification();

  const { data: org, isFetching: isFetchingOrg } = useGetMyOrganizationQuery();
  const organizationId = org?.id;

  const [search, setSearch] = useState('');

  const {
    data: modules,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetAccessibleModulesForOrganizationQuery(organizationId as number, {
    skip: !organizationId || options?.skipQuery,
  });

  const lastErrorRef = useRef<any>(null);

  useEffect(() => {
    if (error && error !== lastErrorRef.current) {
      showError(getErrorMessage(error));
      lastErrorRef.current = error;
    } else if (!error) {
      lastErrorRef.current = null;
    }
  }, [error, showError]);

  const filteredModules: AccessibleModule[] = useMemo(() => {
    const items = modules || [];
    if (!search) return items;
    const q = search.toLowerCase();
    return items.filter(
      (m) =>
        m.moduleName?.toLowerCase().includes(q) ||
        m.moduleCode?.toLowerCase().includes(q) ||
        (m.moduleDescription || '').toLowerCase().includes(q)
    );
  }, [modules, search]);

  // Stats
  const activeModules = useMemo(
    () => filteredModules.filter((m) => m.isActive),
    [filteredModules]
  );
  const totalActiveUsers = useMemo(
    () => activeModules.reduce((sum, m) => sum + (m.activeUserCount || 0), 0),
    [activeModules]
  );
  const totalUsersBaseline = useMemo(() => {
    // If all modules report totalUsersCount, just pick max; else fallback to 0
    return (
      filteredModules.reduce(
        (max, m) => Math.max(max, m.totalUsersCount || 0),
        0
      ) || 0
    );
  }, [filteredModules]);

  // Mutations
  const [assignUser, assignStatus] = useAssignUserToModuleMutation();
  const [revokeUser, revokeStatus] = useRevokeUserAccessToModuleMutation();
  const [updateModuleAccessSettings, updateModuleAccessSettingsStatus] =
    useUpdateModuleAccessSettingsMutation();
  const [backfillModuleAutoGrant, backfillModuleAutoGrantStatus] =
    useBackfillModuleAutoGrantMutation();
  const [bulkAssignUser, bulkAssignStatus] =
    useBulkAssignUsersToModuleMutation();
  const [bulkRevokeUser, bulkRevokeStatus] =
    useBulkRevokeUsersFromModuleMutation();

  const assign = useCallback(
    async (moduleId: number, userId: number, roleId?: number) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await assignUser({
          organizationId,
          moduleId,
          userId,
          roleId,
        }).unwrap();
        success(getResponseMessage(result, 'User assigned to module'));
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [assignUser, organizationId, showError, success]
  );

  const revoke = useCallback(
    async (moduleId: number, userId: number) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await revokeUser({
          organizationId,
          moduleId,
          userId,
        }).unwrap();
        success(getResponseMessage(result, 'User access revoked'));
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [revokeUser, organizationId, showError, success]
  );

  const updateAutoGrant = useCallback(
    async (moduleId: number, autoGrantToNewUsers: boolean) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await updateModuleAccessSettings({
          organizationId,
          moduleId,
          body: { autoGrantToNewUsers },
        }).unwrap();
        success(
          autoGrantToNewUsers
            ? 'Auto-grant enabled'
            : 'Auto-grant disabled. Existing access remains unchanged.'
        );
        return result.data;
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [organizationId, showError, success, updateModuleAccessSettings]
  );

  const backfillAutoGrant = useCallback(
    async (moduleId: number) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await backfillModuleAutoGrant({
          organizationId,
          moduleId,
        }).unwrap();
        const data = result.data;
        success(
          `Granted ${data.grantedCount} user${
            data.grantedCount === 1 ? '' : 's'
          }, skipped ${data.skippedCount}.`
        );
        return data;
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [backfillModuleAutoGrant, organizationId, showError, success]
  );

  const bulkAssign = useCallback(
    async (moduleId: number, userIds: number[], roleId?: number) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await bulkAssignUser({
          organizationId,
          moduleId,
          userIds,
          roleId,
        }).unwrap();
        success(getResponseMessage(result, 'Users assigned to module'));
        return result.data;
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [bulkAssignUser, organizationId, showError, success]
  );

  const bulkRevoke = useCallback(
    async (moduleId: number, userIds: number[]) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        const result = await bulkRevokeUser({
          organizationId,
          moduleId,
          userIds,
        }).unwrap();
        success(getResponseMessage(result, 'Users access revoked'));
        return result.data;
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [bulkRevokeUser, organizationId, showError, success]
  );

  // Expose helpers to load roles/users per module (for dialogs)
  const useModuleRoles = (moduleId?: number) =>
    useGetModuleRolesQuery(moduleId as number, { skip: !moduleId });
  const useModuleUsers = (
    moduleId?: number,
    params?: { page?: number; pageSize?: number; search?: string }
  ) =>
    useGetModuleUsersQuery(
      {
        organizationId: organizationId as number,
        moduleId: moduleId as number,
        page: params?.page,
        pageSize: params?.pageSize,
        search: params?.search,
      },
      {
        skip: !organizationId || !moduleId,
      }
    );

  return {
    organizationId,
    isLoading: isLoading || isFetchingOrg,
    isFetching,
    error,
    refetch,
    modules: filteredModules,
    activeModules,
    totalActiveUsers,
    totalUsersBaseline,
    search,
    setSearch,
    assign,
    revoke,
    bulkAssign,
    bulkRevoke,
    assignStatus,
    revokeStatus,
    bulkAssignStatus,
    bulkRevokeStatus,
    updateAutoGrant,
    backfillAutoGrant,
    updateModuleAccessSettingsStatus,
    backfillModuleAutoGrantStatus,
    useModuleRoles,
    useModuleUsers,
  } as const;
}

export type UseSettingsModulesReturn = ReturnType<typeof useSettingsModules>;
