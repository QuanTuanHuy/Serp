/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings modules hook (organization-scoped)
 */

'use client';

import { useMemo, useState, useCallback, useEffect } from 'react';
import { useGetMyOrganizationQuery } from '../services/organizations/organizationsApi';
import {
  useGetAccessibleModulesForOrganizationQuery,
  useAssignUserToModuleMutation,
  useRevokeUserAccessToModuleMutation,
  useGetModuleRolesQuery,
  useGetModuleUsersQuery,
} from '../services/modules/modulesApi';
import type { AccessibleModule } from '@/modules/settings/types/module-access.types';
import { getErrorMessage } from '@/lib/store/api/utils';
import { useNotification } from '@/shared/hooks/use-notification';

export function useSettingsModules() {
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
    skip: !organizationId,
  });

  useEffect(() => {
    if (error) {
      showError('Failed to load modules');
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

  const assign = useCallback(
    async (moduleId: number, userId: number, roleId?: number) => {
      if (!organizationId) {
        const msg = 'Organization is not ready';
        showError(msg);
        throw new Error(msg);
      }
      try {
        await assignUser({ organizationId, moduleId, userId, roleId }).unwrap();
        success('User assigned to module');
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
        await revokeUser({ organizationId, moduleId, userId }).unwrap();
        success('User access revoked');
      } catch (e: any) {
        showError(getErrorMessage(e));
        throw e;
      }
    },
    [revokeUser, organizationId, showError, success]
  );

  // Expose helpers to load roles/users per module (for dialogs)
  const useModuleRoles = (moduleId?: number) =>
    useGetModuleRolesQuery(moduleId as number, { skip: !moduleId });
  const useModuleUsers = (moduleId?: number) =>
    useGetModuleUsersQuery(
      {
        organizationId: organizationId as number,
        moduleId: moduleId as number,
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
    assignStatus,
    revokeStatus,
    useModuleRoles,
    useModuleUsers,
  } as const;
}

export type UseSettingsModulesReturn = ReturnType<typeof useSettingsModules>;
