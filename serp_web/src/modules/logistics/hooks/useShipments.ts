/**
 * Logistics Module - Shipment Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom hook for shipment operations
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useGetShipmentsQuery,
  useGetShipmentQuery,
  useCreateShipmentMutation,
  useUpdateShipmentMutation,
  useImportShipmentMutation,
  useDeleteShipmentMutation,
  useAddItemToShipmentMutation,
  useUpdateItemInShipmentMutation,
  useDeleteItemFromShipmentMutation,
} from '../services';
import {
  setShipmentFilters,
  setShipmentPage,
  setShipmentPageSize,
  setShipmentSearchQuery,
  setShipmentCustomerFilter,
  setShipmentSupplierFilter,
  setShipmentOrderFilter,
  openShipmentDialog,
  closeShipmentDialog,
  openShipmentItemDialog,
  closeShipmentItemDialog,
  resetShipmentFilters,
  selectShipmentFilters,
  selectShipmentDialogOpen,
  selectShipmentDialogMode,
  selectSelectedShipmentId,
  selectShipmentItemDialogOpen,
  selectSelectedItemId,
} from '../store';
import type {
  CreateShipmentRequest,
  UpdateShipmentRequest,
  InventoryItemDetailForm,
  UpdateInventoryItemDetailRequest,
} from '../types';

export const useShipments = () => {
  const dispatch = useAppDispatch();

  // Redux state
  const filters = useAppSelector(selectShipmentFilters);
  const dialogOpen = useAppSelector(selectShipmentDialogOpen);
  const dialogMode = useAppSelector(selectShipmentDialogMode);
  const selectedShipmentId = useAppSelector(selectSelectedShipmentId);
  const itemDialogOpen = useAppSelector(selectShipmentItemDialogOpen);
  const selectedItemId = useAppSelector(selectSelectedItemId);

  // RTK Query hooks
  const { data: shipmentsData, isLoading: isLoadingShipments } =
    useGetShipmentsQuery(filters);

  const { data: selectedShipment, isLoading: isLoadingShipment } =
    useGetShipmentQuery(selectedShipmentId || '', {
      skip: !selectedShipmentId,
    });

  const [createShipment, { isLoading: isCreating }] =
    useCreateShipmentMutation();
  const [updateShipment, { isLoading: isUpdating }] =
    useUpdateShipmentMutation();
  const [importShipment, { isLoading: isImporting }] =
    useImportShipmentMutation();
  const [deleteShipment, { isLoading: isDeleting }] =
    useDeleteShipmentMutation();
  const [addItem, { isLoading: isAddingItem }] = useAddItemToShipmentMutation();
  const [updateItem, { isLoading: isUpdatingItem }] =
    useUpdateItemInShipmentMutation();
  const [deleteItem, { isLoading: isDeletingItem }] =
    useDeleteItemFromShipmentMutation();

  // Handlers
  const setFilters = useCallback(
    (newFilters: Partial<typeof filters>) => {
      dispatch(setShipmentFilters(newFilters));
    },
    [dispatch]
  );

  const setPage = useCallback(
    (page: number) => {
      dispatch(setShipmentPage(page));
    },
    [dispatch]
  );

  const setPageSize = useCallback(
    (size: number) => {
      dispatch(setShipmentPageSize(size));
    },
    [dispatch]
  );

  const search = useCallback(
    (query: string) => {
      dispatch(setShipmentSearchQuery(query));
    },
    [dispatch]
  );

  const setCustomerFilter = useCallback(
    (customerId: string | undefined) => {
      dispatch(setShipmentCustomerFilter(customerId));
    },
    [dispatch]
  );

  const setSupplierFilter = useCallback(
    (supplierId: string | undefined) => {
      dispatch(setShipmentSupplierFilter(supplierId));
    },
    [dispatch]
  );

  const setOrderFilter = useCallback(
    (orderId: string | undefined) => {
      dispatch(setShipmentOrderFilter(orderId));
    },
    [dispatch]
  );

  const openDialog = useCallback(
    (mode: 'create' | 'edit', shipmentId?: string) => {
      dispatch(openShipmentDialog({ mode, shipmentId }));
    },
    [dispatch]
  );

  const closeDialog = useCallback(() => {
    dispatch(closeShipmentDialog());
  }, [dispatch]);

  const openItemDialog = useCallback(
    (itemId: string | null) => {
      dispatch(openShipmentItemDialog(itemId));
    },
    [dispatch]
  );

  const closeItemDialog = useCallback(() => {
    dispatch(closeShipmentItemDialog());
  }, [dispatch]);

  const resetFilters = useCallback(() => {
    dispatch(resetShipmentFilters());
  }, [dispatch]);

  const handleCreate = useCallback(
    async (data: CreateShipmentRequest) => {
      try {
        await createShipment(data).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to create shipment:', error);
        return { success: false, error };
      }
    },
    [createShipment, closeDialog]
  );

  const handleUpdate = useCallback(
    async (shipmentId: string, data: UpdateShipmentRequest) => {
      try {
        await updateShipment({ shipmentId, data }).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to update shipment:', error);
        return { success: false, error };
      }
    },
    [updateShipment, closeDialog]
  );

  const handleImport = useCallback(
    async (shipmentId: string) => {
      try {
        await importShipment(shipmentId).unwrap();
        return { success: true };
      } catch (error) {
        console.error('Failed to import shipment:', error);
        return { success: false, error };
      }
    },
    [importShipment]
  );

  const handleDelete = useCallback(
    async (shipmentId: string) => {
      try {
        await deleteShipment(shipmentId).unwrap();
        return { success: true };
      } catch (error) {
        console.error('Failed to delete shipment:', error);
        return { success: false, error };
      }
    },
    [deleteShipment]
  );

  const handleAddItem = useCallback(
    async (shipmentId: string, data: InventoryItemDetailForm) => {
      try {
        await addItem({ shipmentId, data }).unwrap();
        closeItemDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to add item to shipment:', error);
        return { success: false, error };
      }
    },
    [addItem, closeItemDialog]
  );

  const handleUpdateItem = useCallback(
    async (
      shipmentId: string,
      itemId: string,
      data: UpdateInventoryItemDetailRequest
    ) => {
      try {
        await updateItem({ shipmentId, itemId, data }).unwrap();
        closeItemDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to update item in shipment:', error);
        return { success: false, error };
      }
    },
    [updateItem, closeItemDialog]
  );

  const handleDeleteItem = useCallback(
    async (shipmentId: string, itemId: string) => {
      try {
        await deleteItem({ shipmentId, itemId }).unwrap();
        return { success: true };
      } catch (error) {
        console.error('Failed to delete item from shipment:', error);
        return { success: false, error };
      }
    },
    [deleteItem]
  );

  return {
    // Data
    shipments: shipmentsData?.items || [],
    totalItems: shipmentsData?.totalItems || 0,
    totalPages: shipmentsData?.totalPages || 0,
    currentPage: shipmentsData?.currentPage || 1,
    selectedShipment,

    // Loading states
    isLoadingShipments,
    isLoadingShipment,
    isCreating,
    isUpdating,
    isImporting,
    isDeleting,
    isAddingItem,
    isUpdatingItem,
    isDeletingItem,

    // UI state
    filters,
    dialogOpen,
    dialogMode,
    selectedShipmentId,
    itemDialogOpen,
    selectedItemId,

    // Handlers
    setFilters,
    setPage,
    setPageSize,
    search,
    setCustomerFilter,
    setSupplierFilter,
    setOrderFilter,
    openDialog,
    closeDialog,
    openItemDialog,
    closeItemDialog,
    resetFilters,
    create: handleCreate,
    update: handleUpdate,
    import: handleImport,
    delete: handleDelete,
    addItem: handleAddItem,
    updateItem: handleUpdateItem,
    deleteItem: handleDeleteItem,
  };
};
