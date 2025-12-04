/**
 * Logistics Module - Facility Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom hook for facility operations
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useGetFacilitiesQuery,
  useGetFacilityQuery,
  useCreateFacilityMutation,
  useUpdateFacilityMutation,
  useDeleteFacilityMutation,
} from '../services';
import {
  setFacilityFilters,
  setFacilityPage,
  setFacilityPageSize,
  setFacilitySearchQuery,
  openFacilityDialog,
  closeFacilityDialog,
  resetFacilityFilters,
  selectFacilityFilters,
  selectFacilityDialogOpen,
  selectFacilityDialogMode,
  selectSelectedFacilityId,
} from '../store';
import type { CreateFacilityRequest, UpdateFacilityRequest } from '../types';

export const useFacilities = () => {
  const dispatch = useAppDispatch();

  // Redux state
  const filters = useAppSelector(selectFacilityFilters);
  const dialogOpen = useAppSelector(selectFacilityDialogOpen);
  const dialogMode = useAppSelector(selectFacilityDialogMode);
  const selectedFacilityId = useAppSelector(selectSelectedFacilityId);

  // RTK Query hooks
  const { data: facilitiesData, isLoading: isLoadingFacilities } =
    useGetFacilitiesQuery(filters);

  const { data: selectedFacility, isLoading: isLoadingFacility } =
    useGetFacilityQuery(selectedFacilityId || '', {
      skip: !selectedFacilityId,
    });

  const [createFacility, { isLoading: isCreating }] =
    useCreateFacilityMutation();
  const [updateFacility, { isLoading: isUpdating }] =
    useUpdateFacilityMutation();
  const [deleteFacility, { isLoading: isDeleting }] =
    useDeleteFacilityMutation();

  // Handlers
  const setFilters = useCallback(
    (newFilters: Partial<typeof filters>) => {
      dispatch(setFacilityFilters(newFilters));
    },
    [dispatch]
  );

  const setPage = useCallback(
    (page: number) => {
      dispatch(setFacilityPage(page));
    },
    [dispatch]
  );

  const setPageSize = useCallback(
    (size: number) => {
      dispatch(setFacilityPageSize(size));
    },
    [dispatch]
  );

  const search = useCallback(
    (query: string) => {
      dispatch(setFacilitySearchQuery(query));
    },
    [dispatch]
  );

  const openDialog = useCallback(
    (mode: 'create' | 'edit', facilityId?: string) => {
      dispatch(openFacilityDialog({ mode, facilityId }));
    },
    [dispatch]
  );

  const closeDialog = useCallback(() => {
    dispatch(closeFacilityDialog());
  }, [dispatch]);

  const resetFilters = useCallback(() => {
    dispatch(resetFacilityFilters());
  }, [dispatch]);

  const handleCreate = useCallback(
    async (data: CreateFacilityRequest) => {
      try {
        await createFacility(data).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to create facility:', error);
        return { success: false, error };
      }
    },
    [createFacility, closeDialog]
  );

  const handleUpdate = useCallback(
    async (facilityId: string, data: UpdateFacilityRequest) => {
      try {
        await updateFacility({ facilityId, data }).unwrap();
        closeDialog();
        return { success: true };
      } catch (error) {
        console.error('Failed to update facility:', error);
        return { success: false, error };
      }
    },
    [updateFacility, closeDialog]
  );

  const handleDelete = useCallback(
    async (facilityId: string) => {
      try {
        await deleteFacility(facilityId).unwrap();
        return { success: true };
      } catch (error) {
        console.error('Failed to delete facility:', error);
        return { success: false, error };
      }
    },
    [deleteFacility]
  );

  return {
    // Data
    facilities: facilitiesData?.items || [],
    totalItems: facilitiesData?.totalItems || 0,
    totalPages: facilitiesData?.totalPages || 0,
    currentPage: facilitiesData?.currentPage || 1,
    selectedFacility,

    // Loading states
    isLoadingFacilities,
    isLoadingFacility,
    isCreating,
    isUpdating,
    isDeleting,

    // UI state
    filters,
    dialogOpen,
    dialogMode,
    selectedFacilityId,

    // Handlers
    setFilters,
    setPage,
    setPageSize,
    search,
    openDialog,
    closeDialog,
    resetFilters,
    create: handleCreate,
    update: handleUpdate,
    delete: handleDelete,
  };
};
