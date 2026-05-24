/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM create work item option loading hook
 */

import { useMemo } from 'react';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetPmProjectsQuery } from '../../api/projectApi';
import {
  useGetPmWorkItemCreateMetaQuery,
  useSearchPmWorkItemsQuery,
} from '../../api/workItemApi';
import {
  mapProjectToComboboxItem,
  mapWorkItemToComboboxItem,
} from './createWorkItemForm';

interface UseCreateWorkItemOptionsArgs {
  open: boolean;
  organizationId?: number | null;
  projectSearch: string;
  parentSearch: string;
  selectedProjectId: string;
  selectedIssueTypeId: string;
}

export function useCreateWorkItemOptions({
  open,
  organizationId,
  projectSearch,
  parentSearch,
  selectedProjectId,
  selectedIssueTypeId,
}: UseCreateWorkItemOptionsArgs) {
  const { data: projectResponse, isLoading: isProjectLoading } =
    useGetPmProjectsQuery({
      page: 0,
      pageSize: 50,
      search: projectSearch.trim() || undefined,
      sortBy: 'updatedAt',
      sortDirection: 'desc',
    });

  const projectItems = useMemo(
    () => (projectResponse?.data.items || []).map(mapProjectToComboboxItem),
    [projectResponse]
  );

  const { data: meta, isFetching: isMetaFetching } =
    useGetPmWorkItemCreateMetaQuery(
      {
        projectId: Number(selectedProjectId),
        issueTypeId: selectedIssueTypeId
          ? Number(selectedIssueTypeId)
          : undefined,
      },
      {
        skip: !selectedProjectId,
      }
    );

  const { data: usersResponse, isLoading: isUserLoading } =
    useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
      },
      { skip: !organizationId || !open }
    );

  const assigneeOptions = useMemo(
    () =>
      [...(usersResponse?.data.items || [])]
        .sort((left, right) => {
          const leftName =
            `${left.firstName || ''} ${left.lastName || ''}`.trim();
          const rightName =
            `${right.firstName || ''} ${right.lastName || ''}`.trim();
          return leftName.localeCompare(rightName);
        })
        .map((user) => ({
          id: String(user.id),
          label:
            `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
            user.email ||
            `User #${user.id}`,
        })),
    [usersResponse]
  );

  const { data: parentResponse, isFetching: isParentFetching } =
    useSearchPmWorkItemsQuery(
      {
        projectId: Number(selectedProjectId),
        params: {
          keyword: parentSearch.trim() || undefined,
          page: 0,
          pageSize: 20,
          sortField: 'updatedAt',
          sortDirection: 'DESC',
        },
      },
      {
        skip: !selectedProjectId,
      }
    );

  const parentItems = useMemo(
    () =>
      (parentResponse?.data.items || [])
        .filter((item) => String(item.issueTypeId) !== selectedIssueTypeId)
        .map(mapWorkItemToComboboxItem),
    [parentResponse, selectedIssueTypeId]
  );

  return {
    assigneeOptions,
    isMetaFetching,
    isParentFetching,
    isProjectLoading,
    isUserLoading,
    meta,
    parentItems,
    projectItems,
  };
}
