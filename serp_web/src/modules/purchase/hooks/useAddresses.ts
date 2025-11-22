/*
Author: QuanTuanHuy
Description: Part of Serp Project - useAddresses custom hook
*/

import { useCallback, useMemo } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks/redux';
import { useNotification } from '@/shared/hooks/use-notification';
import {
  useGetAddressesQuery,
  useGetAddressByIdQuery,
  useCreateAddressMutation,
  useUpdateAddressMutation,
} from '../services';
import {
  selectAddressesFilters,
  selectAddressesDialogOpen,
  selectAddressesDialogMode,
  selectSelectedAddressId,
  setAddressesEntityId,
  setAddressesEntityType,
  setAddressesAddressType,
  setAddressesPage,
  setAddressesPageSize,
  setAddressesSorting,
  openCreateAddressDialog,
  openEditAddressDialog,
  setAddressesDialogOpen,
  resetAddressesFilters,
} from '../store';
import type { CreateAddressRequest, UpdateAddressRequest } from '../types';

export function useAddresses() {
  const dispatch = useAppDispatch();
  const { success, error: showError } = useNotification();

  // Redux state
  const filters = useAppSelector(selectAddressesFilters);
  const dialogOpen = useAppSelector(selectAddressesDialogOpen);
  const dialogMode = useAppSelector(selectAddressesDialogMode);
  const selectedAddressId = useAppSelector(selectSelectedAddressId);

  // RTK Query hooks
  const {
    data: response,
    isLoading,
    isFetching,
    error: queryError,
    refetch,
  } = useGetAddressesQuery(filters);

  const { data: selectedAddressData } = useGetAddressByIdQuery(
    selectedAddressId!,
    {
      skip: !selectedAddressId,
    }
  );

  const [createAddress, { isLoading: isCreating }] = useCreateAddressMutation();
  const [updateAddress, { isLoading: isUpdating }] = useUpdateAddressMutation();

  // Extract data from response
  const addresses = useMemo(() => {
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

  const selectedAddress = useMemo(() => {
    return selectedAddressData?.data;
  }, [selectedAddressData]);

  // Filter handlers
  const handleEntityIdChange = useCallback(
    (entityId: string | undefined) => {
      dispatch(setAddressesEntityId(entityId));
    },
    [dispatch]
  );

  const handleEntityTypeChange = useCallback(
    (entityType: string | undefined) => {
      dispatch(setAddressesEntityType(entityType));
    },
    [dispatch]
  );

  const handleAddressTypeChange = useCallback(
    (addressType: string | undefined) => {
      dispatch(setAddressesAddressType(addressType));
    },
    [dispatch]
  );

  const handlePageChange = useCallback(
    (page: number) => {
      dispatch(setAddressesPage(page));
    },
    [dispatch]
  );

  const handlePageSizeChange = useCallback(
    (size: number) => {
      dispatch(setAddressesPageSize(size));
    },
    [dispatch]
  );

  const handleSortingChange = useCallback(
    (sortBy: string, sortDirection: 'asc' | 'desc') => {
      dispatch(setAddressesSorting({ sortBy, sortDirection }));
    },
    [dispatch]
  );

  const handleResetFilters = useCallback(() => {
    dispatch(resetAddressesFilters());
  }, [dispatch]);

  // Dialog handlers
  const handleOpenCreateDialog = useCallback(() => {
    dispatch(openCreateAddressDialog());
  }, [dispatch]);

  const handleOpenEditDialog = useCallback(
    (addressId: string) => {
      dispatch(openEditAddressDialog(addressId));
    },
    [dispatch]
  );

  const handleCloseDialog = useCallback(() => {
    dispatch(setAddressesDialogOpen(false));
  }, [dispatch]);

  // CRUD handlers
  const handleCreateAddress = useCallback(
    async (data: CreateAddressRequest) => {
      try {
        const result = await createAddress(data).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Address created successfully', {
            description: result.message,
          });
          handleCloseDialog();
          refetch();
          return true;
        } else {
          showError('Failed to create address', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error creating address', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [createAddress, success, showError, handleCloseDialog, refetch]
  );

  const handleUpdateAddress = useCallback(
    async (addressId: string, data: UpdateAddressRequest) => {
      try {
        const result = await updateAddress({ addressId, data }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Address updated successfully', {
            description: result.message,
          });
          handleCloseDialog();
          refetch();
          return true;
        } else {
          showError('Failed to update address', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error updating address', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [updateAddress, success, showError, handleCloseDialog, refetch]
  );

  return {
    // Data
    addresses,
    selectedAddress,
    pagination,
    // Loading states
    isLoading,
    isFetching,
    isCreating,
    isUpdating,
    // Error
    error: queryError,
    // Filters
    filters,
    handleEntityIdChange,
    handleEntityTypeChange,
    handleAddressTypeChange,
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
    handleCreateAddress,
    handleUpdateAddress,
    // Utilities
    refetch,
  };
}

export type UseAddressesReturn = ReturnType<typeof useAddresses>;
