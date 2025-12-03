/**
 * Logistics Module - Order Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom hook for order operations
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useGetOrdersQuery, useGetOrderQuery } from '../services';
import {
  setOrderFilters,
  setOrderPage,
  setOrderPageSize,
  setOrderSearchQuery,
  setOrderCustomerFilter,
  setOrderSupplierFilter,
  selectOrder,
  resetOrderFilters,
  selectOrderFilters,
  selectSelectedOrderId,
} from '../store';

export const useOrders = () => {
  const dispatch = useAppDispatch();

  // Redux state
  const filters = useAppSelector(selectOrderFilters);
  const selectedOrderId = useAppSelector(selectSelectedOrderId);

  // RTK Query hooks
  const { data: ordersData, isLoading: isLoadingOrders } =
    useGetOrdersQuery(filters);

  const { data: selectedOrder, isLoading: isLoadingOrder } = useGetOrderQuery(
    selectedOrderId || '',
    {
      skip: !selectedOrderId,
    }
  );

  // Handlers
  const setFilters = useCallback(
    (newFilters: Partial<typeof filters>) => {
      dispatch(setOrderFilters(newFilters));
    },
    [dispatch]
  );

  const setPage = useCallback(
    (page: number) => {
      dispatch(setOrderPage(page));
    },
    [dispatch]
  );

  const setPageSize = useCallback(
    (size: number) => {
      dispatch(setOrderPageSize(size));
    },
    [dispatch]
  );

  const search = useCallback(
    (query: string) => {
      dispatch(setOrderSearchQuery(query));
    },
    [dispatch]
  );

  const setCustomerFilter = useCallback(
    (customerId: string | undefined) => {
      dispatch(setOrderCustomerFilter(customerId));
    },
    [dispatch]
  );

  const setSupplierFilter = useCallback(
    (supplierId: string | undefined) => {
      dispatch(setOrderSupplierFilter(supplierId));
    },
    [dispatch]
  );

  const handleSelectOrder = useCallback(
    (orderId: string | null) => {
      dispatch(selectOrder(orderId));
    },
    [dispatch]
  );

  const resetFilters = useCallback(() => {
    dispatch(resetOrderFilters());
  }, [dispatch]);

  return {
    // Data
    orders: ordersData?.items || [],
    totalItems: ordersData?.totalItems || 0,
    totalPages: ordersData?.totalPages || 0,
    currentPage: ordersData?.currentPage || 1,
    selectedOrder,

    // Loading states
    isLoadingOrders,
    isLoadingOrder,

    // UI state
    filters,
    selectedOrderId,

    // Handlers
    setFilters,
    setPage,
    setPageSize,
    search,
    setCustomerFilter,
    setSupplierFilter,
    selectOrder: handleSelectOrder,
    resetFilters,
  };
};
