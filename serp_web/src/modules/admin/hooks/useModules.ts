/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - useModules hook for Modules management
 */

'use client';

import { useMemo, useCallback } from 'react';
import {
  useGetModulesQuery,
  useCreateModuleMutation,
  useUpdateModuleMutation,
} from '@/modules/admin/services/modules/modulesApi';
import type { Module } from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  selectModulesDialogOpen,
  selectSelectedModuleId,
  selectModulesFilters,
  setModulesDialogOpen,
  setSelectedModuleId,
  clearSelectedModule,
  setModulesSearch,
  setModulesStatus,
  setModulesType,
} from '@/modules/admin/store';
import { getErrorMessage } from '@/lib/store/api';

type CreateUpdateModulePayload = Omit<Module, 'id' | 'createdAt' | 'updatedAt'>;

export function useModules() {
  const dispatch = useAppDispatch();
  const notification = useNotification();

  const isDialogOpen = useAppSelector(selectModulesDialogOpen);
  const selectedModuleId = useAppSelector(selectSelectedModuleId);
  const filters = useAppSelector(selectModulesFilters);

  const {
    data: modules = [],
    isLoading,
    error,
    refetch,
  } = useGetModulesQuery();
  const [createModuleMutation, { isLoading: isCreating }] =
    useCreateModuleMutation();
  const [updateModuleMutation] = useUpdateModuleMutation();

  const selectedModule = useMemo(
    () => modules.find((m) => m.id === selectedModuleId),
    [modules, selectedModuleId]
  );

  const stats = useMemo(
    () => ({
      total: modules.length,
      enabled: modules.filter((m) => m.status === 'ACTIVE').length,
      disabled: modules.filter((m) => m.status === 'DISABLED').length,
    }),
    [modules]
  );

  const filteredModules = useMemo(() => {
    const term = (filters.search || '').toLowerCase().trim();
    return modules.filter((m) => {
      const matchesSearch = !term
        ? true
        : m.name?.toLowerCase().includes(term) ||
          m.code?.toLowerCase().includes(term) ||
          (m.description || '').toLowerCase().includes(term) ||
          (m.category || '').toLowerCase().includes(term) ||
          m.status.toLowerCase().includes(term) ||
          m.moduleType.toLowerCase().includes(term);
      const matchesStatus = filters.status ? m.status === filters.status : true;
      const matchesType = filters.type ? m.moduleType === filters.type : true;
      return matchesSearch && matchesStatus && matchesType;
    });
  }, [modules, filters]);

  const openCreateDialog = useCallback(() => {
    dispatch(setSelectedModuleId(null));
    dispatch(setModulesDialogOpen(true));
  }, [dispatch]);

  const openEditDialog = useCallback(
    (module: Module) => {
      dispatch(setSelectedModuleId(module.id));
      dispatch(setModulesDialogOpen(true));
    },
    [dispatch]
  );

  const closeDialog = useCallback(() => {
    dispatch(setModulesDialogOpen(false));
    dispatch(clearSelectedModule());
  }, [dispatch]);

  const handleSearch = useCallback(
    (value: string) => {
      dispatch(setModulesSearch(value));
    },
    [dispatch]
  );

  const handleFilterChange = useCallback(
    (key: 'status' | 'type', value?: string) => {
      if (key === 'status') {
        dispatch(setModulesStatus((value as any) || undefined));
      } else if (key === 'type') {
        dispatch(setModulesType((value as any) || undefined));
      }
    },
    [dispatch]
  );

  const createModule = useCallback(
    async (data: CreateUpdateModulePayload) => {
      try {
        await createModuleMutation(data).unwrap();
        notification.success('Module created successfully');
        closeDialog();
      } catch (err: any) {
        notification.error(getErrorMessage(err));
        throw err;
      }
    },
    [createModuleMutation, notification, closeDialog]
  );

  const updateModule = useCallback(
    async (id: number | string, data: Partial<Module>) => {
      try {
        await updateModuleMutation({ id: String(id), data }).unwrap();
        notification.success('Module updated successfully');
        closeDialog();
      } catch (err: any) {
        notification.error(getErrorMessage(err));
        throw err;
      }
    },
    [updateModuleMutation, notification, closeDialog]
  );

  const toggleStatus = useCallback(
    async (id: number | string, currentStatus: string) => {
      try {
        const newStatus = currentStatus === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
        await updateModuleMutation({
          id: String(id),
          data: { status: newStatus },
        }).unwrap();
        notification.success(
          `Module ${newStatus === 'ACTIVE' ? 'enabled' : 'disabled'} successfully`
        );
      } catch (err: any) {
        notification.error(getErrorMessage(err));
      }
    },
    [updateModuleMutation, notification]
  );

  const submitModule = useCallback(
    async (data: any) => {
      if (selectedModule) {
        return updateModule(selectedModule.id, data);
      }
      return createModule(data);
    },
    [selectedModule, updateModule, createModule]
  );

  return {
    modules: filteredModules,
    rawModules: modules,
    stats,
    selectedModule,
    isDialogOpen,
    isCreating,
    isLoading,
    error,
    refetch,
    filters,
    openCreateDialog,
    openEditDialog,
    closeDialog,
    handleSearch,
    handleFilterChange,
    submitModule,
    createModule,
    updateModule,
    toggleStatus,
  };
}

export type UseModulesReturn = ReturnType<typeof useModules>;
