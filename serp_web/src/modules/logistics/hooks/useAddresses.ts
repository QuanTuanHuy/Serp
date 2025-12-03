/**
 * Logistics Module - Address Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom hook for address operations
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useCreateAddressMutation,
  useUpdateAddressMutation,
  useGetAddressesByEntityQuery,
} from '../services';
import {
  openAddressDialog,
  closeAddressDialog,
  selectAddressDialogOpen,
  selectAddressDialogMode,
  selectSelectedAddressId,
  selectAddressEntityId,
  selectAddressEntityTypeId,
} from '../store';
import type { CreateAddressRequest, UpdateAddressRequest } from '../types';

export const useAddresses = (entityId?: string, entityTypeId?: string) => {
  const dispatch = useAppDispatch();

  // Redux state
  const dialogOpen = useAppSelector(selectAddressDialogOpen);
  const dialogMode = useAppSelector(selectAddressDialogMode);
  const selectedAddressId = useAppSelector(selectSelectedAddressId);
  const stateEntityId = useAppSelector(selectAddressEntityId);
  const stateEntityTypeId = useAppSelector(selectAddressEntityTypeId);

  // Use props or state for entity filters
  const finalEntityId = entityId || stateEntityId;
  const finalEntityTypeId = entityTypeId || stateEntityTypeId;

  // RTK Query hooks
  const { data: addresses, isLoading: isLoadingAddresses } =
    useGetAddressesByEntityQuery(
      `${finalEntityId || ''}?entityTypeId=${finalEntityTypeId || ''}`,
      {
        skip: !finalEntityId || !finalEntityTypeId,
      }
    );

  const [createAddress, { isLoading: isCreating }] =
    useCreateAddressMutation();
  const [updateAddress, { isLoading: isUpdating }] =
    useUpdateAddressMutation();

  // Handlers
  const openDialog = useCallback(
    (
      mode: 'create' | 'edit',
      addressId?: string,
      entityId?: string,
      entityTypeId?: string
    ) => {
      if (!entityId || !entityTypeId) return;
      dispatch(openAddressDialog({ mode, addressId, entityId, entityTypeId }));
    },
    [dispatch]
  );

  const closeDialog = useCallback(() => {
    dispatch(closeAddressDialog());
  }, [dispatch]);

  const handleCreate = useCallback(
    async (data: CreateAddressRequest) => {
      try {
        await createAddress(data).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to create address:', error);
        return { success: false, error };
      }
    },
    [createAddress, closeDialog]
  );

  const handleUpdate = useCallback(
    async (addressId: string, data: UpdateAddressRequest) => {
      try {
        await updateAddress({ addressId, data }).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to update address:', error);
        return { success: false, error };
      }
    },
    [updateAddress, closeDialog]
  );

  return {
    // Data
    addresses: addresses || [],

    // Loading states
    isLoadingAddresses,
    isCreating,
    isUpdating,

    // UI state
    dialogOpen,
    dialogMode,
    selectedAddressId,
    entityId: finalEntityId,
    entityTypeId: finalEntityTypeId,

    // Handlers
    openDialog,
    closeDialog,
    create: handleCreate,
    update: handleUpdate,
  };
};
