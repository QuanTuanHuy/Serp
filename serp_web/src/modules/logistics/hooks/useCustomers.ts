/**
 * Logistics Module - Customer Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom hook for customer operations
 */

import { useCallback, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useGetCustomersQuery } from '../services';
import { selectCustomer, selectSelectedCustomerId } from '../store';
import type { CustomerFilters } from '../types';

export const useCustomers = () => {
  const dispatch = useAppDispatch();

  // Local state for filters (customers don't have dedicated slice filters)
  const [filters, setFiltersState] = useState<CustomerFilters>({
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
    query: '',
  });

  // Redux state
  const selectedCustomerId = useAppSelector(selectSelectedCustomerId);

  // RTK Query hooks
  const { data: customersData, isLoading: isLoadingCustomers } =
    useGetCustomersQuery(filters);

  // Handlers
  const setFilters = useCallback((newFilters: Partial<CustomerFilters>) => {
    setFiltersState((prev) => ({ ...prev, ...newFilters }));
  }, []);

  const setPage = useCallback((page: number) => {
    setFiltersState((prev) => ({ ...prev, page }));
  }, []);

  const setPageSize = useCallback((size: number) => {
    setFiltersState((prev) => ({ ...prev, size, page: 1 }));
  }, []);

  const search = useCallback((query: string) => {
    setFiltersState((prev) => ({ ...prev, query, page: 1 }));
  }, []);

  const handleSelectCustomer = useCallback(
    (customerId: string | null) => {
      dispatch(selectCustomer(customerId));
    },
    [dispatch]
  );

  const resetFilters = useCallback(() => {
    setFiltersState({
      page: 1,
      size: 10,
      sortBy: 'createdStamp',
      sortDirection: 'desc',
      query: '',
    });
  }, []);

  return {
    // Data
    customers: customersData?.items || [],
    totalItems: customersData?.totalItems || 0,
    totalPages: customersData?.totalPages || 0,
    currentPage: customersData?.currentPage || 1,

    // Loading states
    isLoadingCustomers,

    // UI state
    filters,
    selectedCustomerId,

    // Handlers
    setFilters,
    setPage,
    setPageSize,
    search,
    selectCustomer: handleSelectCustomer,
    resetFilters,
  };
};
