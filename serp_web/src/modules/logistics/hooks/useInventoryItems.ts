/**
 * Logistics Module - Inventory Item Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom hook for inventory item operations
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useGetInventoryItemsQuery,
  useGetInventoryItemQuery,
  useCreateInventoryItemMutation,
  useUpdateInventoryItemMutation,
  useDeleteInventoryItemMutation,
} from '../services';
import {
  setInventoryItemFilters,
  setInventoryItemPage,
  setInventoryItemPageSize,
  setInventoryItemSearchQuery,
  setInventoryItemProductFilter,
  setInventoryItemFacilityFilter,
  openInventoryItemDialog,
  closeInventoryItemDialog,
  resetInventoryItemFilters,
  selectInventoryItemFilters,
  selectInventoryItemDialogOpen,
  selectInventoryItemDialogMode,
  selectSelectedInventoryItemId,
} from '../store';
import type {
  CreateInventoryItemRequest,
  UpdateInventoryItemRequest,
} from '../types';

export const useInventoryItems = () => {
  const dispatch = useAppDispatch();

  // Redux state
  const filters = useAppSelector(selectInventoryItemFilters);
  const dialogOpen = useAppSelector(selectInventoryItemDialogOpen);
  const dialogMode = useAppSelector(selectInventoryItemDialogMode);
  const selectedInventoryItemId = useAppSelector(selectSelectedInventoryItemId);

  // RTK Query hooks
  const { data: inventoryItemsData, isLoading: isLoadingInventoryItems } =
    useGetInventoryItemsQuery(filters);

  const { data: selectedInventoryItem, isLoading: isLoadingInventoryItem } =
    useGetInventoryItemQuery(selectedInventoryItemId || '', {
      skip: !selectedInventoryItemId,
    });

  const [createInventoryItem, { isLoading: isCreating }] =
    useCreateInventoryItemMutation();
  const [updateInventoryItem, { isLoading: isUpdating }] =
    useUpdateInventoryItemMutation();
  const [deleteInventoryItem, { isLoading: isDeleting }] =
    useDeleteInventoryItemMutation();

  // Handlers
  const setFilters = useCallback(
    (newFilters: Partial<typeof filters>) => {
      dispatch(setInventoryItemFilters(newFilters));
    },
    [dispatch]
  );

  const setPage = useCallback(
    (page: number) => {
      dispatch(setInventoryItemPage(page));
    },
    [dispatch]
  );

  const setPageSize = useCallback(
    (size: number) => {
      dispatch(setInventoryItemPageSize(size));
    },
    [dispatch]
  );

  const search = useCallback(
    (query: string) => {
      dispatch(setInventoryItemSearchQuery(query));
    },
    [dispatch]
  );

  const setProductFilter = useCallback(
    (productId: string | undefined) => {
      dispatch(setInventoryItemProductFilter(productId));
    },
    [dispatch]
  );

  const setFacilityFilter = useCallback(
    (facilityId: string | undefined) => {
      dispatch(setInventoryItemFacilityFilter(facilityId));
    },
    [dispatch]
  );

  const openDialog = useCallback(
    (mode: 'create' | 'edit', inventoryItemId?: string) => {
      dispatch(openInventoryItemDialog({ mode, inventoryItemId }));
    },
    [dispatch]
  );

  const closeDialog = useCallback(() => {
    dispatch(closeInventoryItemDialog());
  }, [dispatch]);

  const resetFilters = useCallback(() => {
    dispatch(resetInventoryItemFilters());
  }, [dispatch]);

  const handleCreate = useCallback(
    async (data: CreateInventoryItemRequest) => {
      try {
        await createInventoryItem(data).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to create inventory item:', error);
        return { success: false, error };
      }
    },
    [createInventoryItem, closeDialog]
  );

  const handleUpdate = useCallback(
    async (inventoryItemId: string, data: UpdateInventoryItemRequest) => {
      try {
        await updateInventoryItem({ inventoryItemId, data }).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to update inventory item:', error);
        return { success: false, error };
      }
    },
    [updateInventoryItem, closeDialog]
  );

  const handleDelete = useCallback(
    async (inventoryItemId: string) => {
      try {
        await deleteInventoryItem(inventoryItemId).unwrap();
        return { success: true };
      } catch (error) {
        console.error('Failed to delete inventory item:', error);
        return { success: false, error };
      }
    },
    [deleteInventoryItem]
  );

  return {
    // Data
    inventoryItems: inventoryItemsData?.items || [],
    totalItems: inventoryItemsData?.totalItems || 0,
    totalPages: inventoryItemsData?.totalPages || 0,
    currentPage: inventoryItemsData?.currentPage || 1,
    selectedInventoryItem,

    // Loading states
    isLoadingInventoryItems,
    isLoadingInventoryItem,
    isCreating,
    isUpdating,
    isDeleting,

    // UI state
    filters,
    dialogOpen,
    dialogMode,
    selectedInventoryItemId,

    // Handlers
    setFilters,
    setPage,
    setPageSize,
    search,
    setProductFilter,
    setFacilityFilter,
    openDialog,
    closeDialog,
    resetFilters,
    create: handleCreate,
    update: handleUpdate,
    delete: handleDelete,
  };
};
