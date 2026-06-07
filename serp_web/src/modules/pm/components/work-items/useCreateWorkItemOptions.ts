/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM create work item option loading hook
 */

import { useMemo } from 'react';
import {
  useGetPmProjectPeopleQuery,
  useGetPmProjectsQuery,
} from '../../api/projectApi';
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
  projectSearch: string;
  parentSearch: string;
  selectedProjectId: string;
  selectedIssueTypeId: string;
}

export function useCreateWorkItemOptions({
  open,
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

  const { data: projectPeople = [], isLoading: isUserLoading } =
    useGetPmProjectPeopleQuery(Number(selectedProjectId), {
      skip: !open || !selectedProjectId,
    });

  const assigneeOptions = useMemo(
    () =>
      projectPeople
        .map((person) => ({
          id: String(person.userId),
          label: person.name || person.email || `User #${person.userId}`,
        }))
        .sort((left, right) => left.label.localeCompare(right.label)),
    [projectPeople]
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
