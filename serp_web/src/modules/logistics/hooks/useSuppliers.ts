/**
 * Logistics Module - Supplier Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom hook for supplier operations
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useGetSuppliersQuery, useGetSupplierQuery } from '../services';
import { selectSupplier, selectSelectedSupplierId } from '../store';

export const useSuppliers = () => {
  const dispatch = useAppDispatch();

  // Redux state
  const selectedSupplierId = useAppSelector(selectSelectedSupplierId);

  // RTK Query hooks
  const { data: suppliersData, isLoading: isLoadingSuppliers } =
    useGetSuppliersQuery();

  const { data: selectedSupplier, isLoading: isLoadingSupplier } =
    useGetSupplierQuery(selectedSupplierId || '', {
      skip: !selectedSupplierId,
    });

  // Handlers
  const handleSelectSupplier = useCallback(
    (supplierId: string | null) => {
      dispatch(selectSupplier(supplierId));
    },
    [dispatch]
  );

  return {
    // Data
    suppliers: suppliersData?.items || [],
    totalItems: suppliersData?.totalItems || 0,
    totalPages: suppliersData?.totalPages || 0,
    currentPage: suppliersData?.currentPage || 1,
    selectedSupplier,

    // Loading states
    isLoadingSuppliers,
    isLoadingSupplier,

    // UI state
    selectedSupplierId,

    // Handlers
    selectSupplier: handleSelectSupplier,
  };
};
