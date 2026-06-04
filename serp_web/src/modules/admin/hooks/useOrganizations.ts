/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - useOrganizations hook for Organizations page
 */

'use client';

import { useMemo, useCallback } from 'react';
import { useGetOrganizationsQuery } from '@/modules/admin/services/organizations/organizationsApi';
import type {
  Organization,
  OrganizationFilters,
  OrganizationStatus,
} from '@/modules/admin/types';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  closeOrganizationDetailDrawer,
  closeOrganizationStatusDialog,
  openOrganizationDetailDrawer,
  openOrganizationStatusDialog,
  selectOrganizationsFilters,
  selectOrganizationsUiState,
  setOrganizationsFilters,
  setOrganizationsSearch,
  setOrganizationsStatus,
  setOrganizationsType,
  setOrganizationsPage,
  setOrganizationsPageSize,
  setOrganizationsSort,
} from '@/modules/admin/store';

export function useOrganizations() {
  const dispatch = useAppDispatch();

  const filters = useAppSelector(selectOrganizationsFilters);
  const ui = useAppSelector(selectOrganizationsUiState);

  const {
    data: response,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetOrganizationsQuery(filters);

  const organizations: Organization[] = useMemo(
    () => response?.data.items || [],
    [response]
  );

  const pagination = useMemo(
    () => ({
      totalPages: response?.data.totalPages || 0,
      currentPage: response?.data.currentPage || 0,
      totalItems: response?.data.totalItems || 0,
    }),
    [response]
  );

  // Handlers
  const handleSearch = useCallback(
    (search: string) => dispatch(setOrganizationsSearch(search || undefined)),
    [dispatch]
  );

  const handleFilterChange = useCallback(
    <K extends keyof OrganizationFilters>(
      key: K,
      value: OrganizationFilters[K]
    ) => {
      switch (key) {
        case 'status':
          dispatch(
            setOrganizationsStatus(
              (value || undefined) as OrganizationFilters['status']
            )
          );
          break;
        case 'type':
          dispatch(
            setOrganizationsType(
              (value || undefined) as OrganizationFilters['type']
            )
          );
          break;
        case 'page':
          dispatch(setOrganizationsPage(value as number));
          break;
        case 'pageSize':
          dispatch(setOrganizationsPageSize(value as number));
          break;
        case 'sortBy':
        case 'sortDir':
          dispatch(
            setOrganizationsSort({
              sortBy: (key === 'sortBy' ? value : filters.sortBy) as string,
              sortDir: (key === 'sortDir' ? value : filters.sortDir) as
                | 'ASC'
                | 'DESC',
            })
          );
          break;
        default:
          dispatch(
            setOrganizationsFilters({
              [key]: value,
            } as Partial<OrganizationFilters>)
          );
      }
    },
    [dispatch, filters.sortBy, filters.sortDir]
  );

  const handlePageChange = useCallback(
    (newPage: number) => dispatch(setOrganizationsPage(newPage)),
    [dispatch]
  );

  const openDetails = useCallback(
    (organizationId: number) =>
      dispatch(openOrganizationDetailDrawer({ organizationId })),
    [dispatch]
  );

  const closeDetails = useCallback(
    () => dispatch(closeOrganizationDetailDrawer()),
    [dispatch]
  );

  const openStatus = useCallback(
    (organizationId: number, status: OrganizationStatus) =>
      dispatch(openOrganizationStatusDialog({ organizationId, status })),
    [dispatch]
  );

  const closeStatus = useCallback(
    () => dispatch(closeOrganizationStatusDialog()),
    [dispatch]
  );

  return {
    filters,
    organizations,
    pagination,
    isLoading,
    isFetching,
    error,
    refetch,
    ui,
    handleSearch,
    handleFilterChange,
    handlePageChange,
    openDetails,
    closeDetails,
    openStatus,
    closeStatus,
  };
}

export type UseOrganizationsReturn = ReturnType<typeof useOrganizations>;
