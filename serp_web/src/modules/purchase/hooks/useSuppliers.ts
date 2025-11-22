/*
Author: QuanTuanHuy
Description: Part of Serp Project - useSuppliers custom hook
*/

import { useCallback, useMemo } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks/redux';
import { useNotification } from '@/shared/hooks/use-notification';
import {
  useGetSuppliersQuery,
  useGetSupplierByIdQuery,
  useCreateSupplierMutation,
  useUpdateSupplierMutation,
  useDeleteSupplierMutation,
} from '../services';
import {
  selectSuppliersFilters,
  selectSuppliersDialogOpen,
  selectSuppliersDialogMode,
  selectSelectedSupplierId,
  setSuppliersQuery,
  setSuppliersStatusId,
  setSuppliersPage,
  setSuppliersPageSize,
  setSuppliersSorting,
  openCreateSupplierDialog,
  openEditSupplierDialog,
  setSuppliersDialogOpen,
  resetSuppliersFilters,
} from '../store';
import type { CreateSupplierRequest, UpdateSupplierRequest } from '../types';

export function useSuppliers() {
  const dispatch = useAppDispatch();
  const { success, error: showError } = useNotification();

  // Redux state
  const filters = useAppSelector(selectSuppliersFilters);
  const dialogOpen = useAppSelector(selectSuppliersDialogOpen);
  const dialogMode = useAppSelector(selectSuppliersDialogMode);
  const selectedSupplierId = useAppSelector(selectSelectedSupplierId);

  // RTK Query hooks
  const {
    data: response,
    isLoading,
    isFetching,
    error: queryError,
    refetch,
  } = useGetSuppliersQuery(filters);

  const { data: selectedSupplierData } = useGetSupplierByIdQuery(
    selectedSupplierId!,
    {
      skip: !selectedSupplierId,
    }
  );

  const [createSupplier, { isLoading: isCreating }] =
    useCreateSupplierMutation();
  const [updateSupplier, { isLoading: isUpdating }] =
    useUpdateSupplierMutation();
  const [deleteSupplier, { isLoading: isDeleting }] =
    useDeleteSupplierMutation();

  // Extract data from response
  const suppliers = useMemo(() => {
    return response?.data?.items || [];
  }, [response]);

  const pagination = useMemo(() => {
    if (!response?.data) return null;
    return {
      currentPage: response.data.currentPage,
      totalPages: response.data.totalPages,
      totalItems: response.data.totalItems,
      pageSize: filters.size || 10,
    };
  }, [response, filters.size]);

  const selectedSupplier = useMemo(() => {
    return selectedSupplierData?.data;
  }, [selectedSupplierData]);

  // Filter handlers
  const handleQueryChange = useCallback(
    (query: string | undefined) => {
      dispatch(setSuppliersQuery(query));
    },
    [dispatch]
  );

  const handleStatusChange = useCallback(
    (statusId: string | undefined) => {
      dispatch(setSuppliersStatusId(statusId));
    },
    [dispatch]
  );

  const handlePageChange = useCallback(
    (page: number) => {
      dispatch(setSuppliersPage(page));
    },
    [dispatch]
  );

  const handlePageSizeChange = useCallback(
    (size: number) => {
      dispatch(setSuppliersPageSize(size));
    },
    [dispatch]
  );

  const handleSortingChange = useCallback(
    (sortBy: string, sortDirection: 'asc' | 'desc') => {
      dispatch(setSuppliersSorting({ sortBy, sortDirection }));
    },
    [dispatch]
  );

  const handleResetFilters = useCallback(() => {
    dispatch(resetSuppliersFilters());
  }, [dispatch]);

  // Dialog handlers
  const handleOpenCreateDialog = useCallback(() => {
    dispatch(openCreateSupplierDialog());
  }, [dispatch]);

  const handleOpenEditDialog = useCallback(
    (supplierId: string) => {
      dispatch(openEditSupplierDialog(supplierId));
    },
    [dispatch]
  );

  const handleCloseDialog = useCallback(() => {
    dispatch(setSuppliersDialogOpen(false));
  }, [dispatch]);

  // CRUD handlers
  const handleCreateSupplier = useCallback(
    async (data: CreateSupplierRequest) => {
      try {
        const result = await createSupplier(data).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Supplier created successfully', {
            description: result.message,
          });
          handleCloseDialog();
          refetch();
          return true;
        } else {
          showError('Failed to create supplier', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error creating supplier', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [createSupplier, success, showError, handleCloseDialog, refetch]
  );

  const handleUpdateSupplier = useCallback(
    async (supplierId: string, data: UpdateSupplierRequest) => {
      try {
        const result = await updateSupplier({ supplierId, data }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Supplier updated successfully', {
            description: result.message,
          });
          handleCloseDialog();
          refetch();
          return true;
        } else {
          showError('Failed to update supplier', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error updating supplier', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [updateSupplier, success, showError, handleCloseDialog, refetch]
  );

  const handleDeleteSupplier = useCallback(
    async (supplierId: string) => {
      try {
        await deleteSupplier(supplierId).unwrap();
        success('Supplier deleted successfully');
        refetch();
        return true;
      } catch (err: any) {
        showError('Error deleting supplier', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [deleteSupplier, success, showError, refetch]
  );

  return {
    // Data
    suppliers,
    selectedSupplier,
    pagination,
    // Loading states
    isLoading,
    isFetching,
    isCreating,
    isUpdating,
    isDeleting,
    // Error
    error: queryError,
    // Filters
    filters,
    handleQueryChange,
    handleStatusChange,
    handlePageChange,
    handlePageSizeChange,
    handleSortingChange,
    handleResetFilters,
    // Dialog state
    dialogOpen,
    dialogMode,
    handleOpenCreateDialog,
    handleOpenEditDialog,
    handleCloseDialog,
    // CRUD operations
    handleCreateSupplier,
    handleUpdateSupplier,
    handleDeleteSupplier,
    // Utilities
    refetch,
  };
}

export type UseSuppliersReturn = ReturnType<typeof useSuppliers>;
