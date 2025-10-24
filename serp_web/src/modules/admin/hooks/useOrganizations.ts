/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - useOrganizations hook for Organizations page
 */

'use client';

import { useMemo, useCallback } from 'react';
import { useGetOrganizationsQuery } from '@/modules/admin/services/organizations/organizationsApi';
import type { Organization, OrganizationFilters } from '@/modules/admin/types';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  selectOrganizationsFilters,
  setOrganizationsFilters,
  setOrganizationsSearch,
  setOrganizationsStatus,
  setOrganizationsType,
  setOrganizationsPage,
  setOrganizationsPageSize,
  setOrganizationsSort,
} from '@/modules/admin/store';
import { useNotification } from '@/shared/hooks/use-notification';

export function useOrganizations() {
  const dispatch = useAppDispatch();
  const notification = useNotification();

  const filters = useAppSelector(selectOrganizationsFilters);

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
    (key: keyof OrganizationFilters, value: any) => {
      switch (key) {
        case 'status':
          dispatch(setOrganizationsStatus(value || undefined));
          break;
        case 'type':
          dispatch(setOrganizationsType(value || undefined));
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
          dispatch(setOrganizationsFilters({ [key]: value } as any));
      }
    },
    [dispatch, filters.sortBy, filters.sortDir]
  );

  const handlePageChange = useCallback(
    (newPage: number) => dispatch(setOrganizationsPage(newPage)),
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
    handleSearch,
    handleFilterChange,
    handlePageChange,
  };
}

export type UseOrganizationsReturn = ReturnType<typeof useOrganizations>;
