/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipments management hook
*/

'use client';

import { useCallback, useMemo } from 'react';
import { useNotification } from '@/shared/hooks';
import { useAuth } from '@/modules/account';
import {
  useGetShipmentsByOrderIdQuery,
  useGetShipmentByIdQuery,
  useCreateShipmentMutation,
  useUpdateShipmentMutation,
  useDeleteShipmentMutation,
  useImportShipmentMutation,
  useAddItemToShipmentMutation,
  useUpdateItemInShipmentMutation,
  useDeleteItemFromShipmentMutation,
  useUpdateShipmentFacilityMutation,
} from '../services/purchaseApi';
import type {
  CreateShipmentRequest,
  UpdateShipmentRequest,
  ShipmentItemAddRequest,
  InventoryItemDetailUpdateRequest,
  ShipmentFacilityUpdateRequest,
} from '../types';
import { getErrorMessage, isSuccessResponse } from '@/lib/store/api/utils';

export function useShipments(orderId?: string) {
  const notification = useNotification();
  const { user } = useAuth();

  // Queries
  const {
    data: shipmentsResponse,
    isLoading: isLoadingShipments,
    refetch: refetchShipments,
  } = useGetShipmentsByOrderIdQuery(orderId || '', {
    skip: !orderId,
  });

  // Mutations
  const [createShipment, { isLoading: isCreating }] =
    useCreateShipmentMutation();
  const [updateShipment, { isLoading: isUpdating }] =
    useUpdateShipmentMutation();
  const [deleteShipment, { isLoading: isDeleting }] =
    useDeleteShipmentMutation();
  const [importShipment, { isLoading: isImporting }] =
    useImportShipmentMutation();
  const [addItem, { isLoading: isAddingItem }] = useAddItemToShipmentMutation();
  const [updateItem, { isLoading: isUpdatingItem }] =
    useUpdateItemInShipmentMutation();
  const [deleteItem, { isLoading: isDeletingItem }] =
    useDeleteItemFromShipmentMutation();
  const [updateFacility, { isLoading: isUpdatingFacility }] =
    useUpdateShipmentFacilityMutation();

  const shipments = useMemo(
    () => shipmentsResponse?.data || [],
    [shipmentsResponse]
  );

  // Check permissions - only PURCHASE_STAFF and PURCHASE_ADMIN can edit
  const canEdit = useMemo(() => {
    if (!user?.roles) return false;
    return user.roles.some(
      (role) => role === 'PURCHASE_STAFF' || role === 'PURCHASE_ADMIN'
    );
  }, [user]);

  // Create shipment handler
  const handleCreateShipment = useCallback(
    async (data: CreateShipmentRequest) => {
      try {
        const result = await createShipment(data).unwrap();
        if (isSuccessResponse(result)) {
          notification.success('Tạo phiếu nhập thành công');
          return true;
        } else {
          notification.error(getErrorMessage(result));
          return false;
        }
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [createShipment, notification]
  );

  // Update shipment handler
  const handleUpdateShipment = useCallback(
    async (shipmentId: string, data: UpdateShipmentRequest) => {
      try {
        const result = await updateShipment({ shipmentId, data }).unwrap();
        if (isSuccessResponse(result)) {
          notification.success('Cập nhật phiếu nhập thành công');
          return true;
        } else {
          notification.error(getErrorMessage(result));
          return false;
        }
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [updateShipment, notification]
  );

  // Delete shipment handler
  const handleDeleteShipment = useCallback(
    async (shipmentId: string) => {
      try {
        await deleteShipment(shipmentId).unwrap();
        notification.success('Xóa phiếu nhập thành công');
        return true;
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [deleteShipment, notification]
  );

  // Import shipment handler
  const handleImportShipment = useCallback(
    async (shipmentId: string) => {
      try {
        const result = await importShipment(shipmentId).unwrap();
        if (isSuccessResponse(result)) {
          notification.success('Nhập hàng thành công');
          return true;
        } else {
          notification.error(getErrorMessage(result));
          return false;
        }
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [importShipment, notification]
  );

  // Add item to shipment handler
  const handleAddItem = useCallback(
    async (shipmentId: string, data: ShipmentItemAddRequest) => {
      try {
        const result = await addItem({ shipmentId, data }).unwrap();
        if (isSuccessResponse(result)) {
          notification.success('Thêm sản phẩm thành công');
          return true;
        } else {
          notification.error(getErrorMessage(result));
          return false;
        }
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [addItem, notification]
  );

  // Update item in shipment handler
  const handleUpdateItem = useCallback(
    async (
      shipmentId: string,
      itemId: string,
      data: InventoryItemDetailUpdateRequest
    ) => {
      try {
        const result = await updateItem({ shipmentId, itemId, data }).unwrap();
        if (isSuccessResponse(result)) {
          notification.success('Cập nhật sản phẩm thành công');
          return true;
        } else {
          notification.error(getErrorMessage(result));
          return false;
        }
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [updateItem, notification]
  );

  // Delete item from shipment handler
  const handleDeleteItem = useCallback(
    async (shipmentId: string, itemId: string) => {
      try {
        const result = await deleteItem({ shipmentId, itemId }).unwrap();
        if (isSuccessResponse(result)) {
          notification.success('Xóa sản phẩm thành công');
          return true;
        } else {
          notification.error(getErrorMessage(result));
          return false;
        }
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [deleteItem, notification]
  );

  // Update facility handler
  const handleUpdateFacility = useCallback(
    async (shipmentId: string, data: ShipmentFacilityUpdateRequest) => {
      try {
        const result = await updateFacility({ shipmentId, data }).unwrap();
        if (isSuccessResponse(result)) {
          notification.success('Đổi kho thành công');
          return true;
        } else {
          notification.error(getErrorMessage(result));
          return false;
        }
      } catch (error) {
        notification.error(getErrorMessage(error));
        return false;
      }
    },
    [updateFacility, notification]
  );

  return {
    // Data
    shipments,
    isLoadingShipments,
    canEdit,

    // Handlers
    handleCreateShipment,
    handleUpdateShipment,
    handleDeleteShipment,
    handleImportShipment,
    handleAddItem,
    handleUpdateItem,
    handleDeleteItem,
    handleUpdateFacility,
    refetchShipments,

    // Loading states
    isCreating,
    isUpdating,
    isDeleting,
    isImporting,
    isAddingItem,
    isUpdatingItem,
    isDeletingItem,
    isUpdatingFacility,
  };
}

export type UseShipmentsReturn = ReturnType<typeof useShipments>;
