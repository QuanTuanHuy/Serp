/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - useModules hook for Modules management
 */

'use client';

import { useMemo, useCallback } from 'react';
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import {
  useGetModulesQuery,
  useGetModulesV2Query,
  useCreateModuleMutation,
  useUpdateModuleMutation,
} from '@/modules/admin/services/modules/modulesApi';
import type { Module } from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  selectModulesDialogOpen,
  selectSelectedModuleId,
  setModulesDialogOpen,
  setSelectedModuleId,
  clearSelectedModule,
} from '@/modules/admin/store';
import { getErrorMessage } from '@/lib/store/api';

type CreateUpdateModulePayload = Omit<Module, 'id' | 'createdAt' | 'updatedAt'>;

export function useModules() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const page = Number(searchParams.get('page')) || 1;
  const pageSize = Number(searchParams.get('pageSize')) || 12;
  const search = searchParams.get('search') || '';
  const status = searchParams.get('status') || '';
  const type = searchParams.get('type') || '';
  const view = (searchParams.get('view') as 'grid' | 'list') || 'grid';

  const dispatch = useAppDispatch();
  const notification = useNotification();

  const isDialogOpen = useAppSelector(selectModulesDialogOpen);
  const selectedModuleId = useAppSelector(selectSelectedModuleId);

  // Fetch all modules only for calculating stats (leveraging RTK Query cache)
  const { data: allModules = [] } = useGetModulesQuery();

  // Fetch paginated modules for current page/search/filters
  const {
    data,
    isLoading,
    error,
    refetch,
  } = useGetModulesV2Query({
    page: page - 1,
    pageSize,
    search: search || undefined,
    status: status || undefined,
    moduleType: type || undefined,
    sortBy: 'displayOrder',
    sortDirection: 'asc',
  });

  const [createModuleMutation, { isLoading: isCreating }] =
    useCreateModuleMutation();
  const [updateModuleMutation] = useUpdateModuleMutation();

  const modules = data?.items || [];
  const pageInfo = {
    size: pageSize,
    number: data?.currentPage ?? 0,
    totalElements: data?.totalItems ?? 0,
    totalPages: data?.totalPages ?? 0,
  };

  const selectedModule = useMemo(
    () => allModules.find((m) => m.id === selectedModuleId),
    [allModules, selectedModuleId]
  );

  const stats = useMemo(
    () => ({
      total: allModules.length,
      enabled: allModules.filter((m) => m.status === 'ACTIVE').length,
      disabled: allModules.filter((m) => m.status === 'DISABLED').length,
    }),
    [allModules]
  );

  const updateUrl = useCallback(
    (newParams: Record<string, string | null>) => {
      const params = new URLSearchParams(searchParams.toString());
      Object.entries(newParams).forEach(([key, value]) => {
        if (value === null || value === undefined || value === '') {
          params.delete(key);
        } else {
          params.set(key, value);
        }
      });
      router.push(`${pathname}?${params.toString()}`);
    },
    [searchParams, pathname, router]
  );

  const setPage = useCallback((newPage: number) => {
    updateUrl({ page: String(newPage) });
  }, [updateUrl]);

  const setViewMode = useCallback((mode: 'grid' | 'list') => {
    updateUrl({ view: mode });
  }, [updateUrl]);

  const handleSearch = useCallback((value: string) => {
    updateUrl({ search: value, page: '1' });
  }, [updateUrl]);

  const handleFilterChange = useCallback((key: 'status' | 'type', value?: string) => {
    updateUrl({ [key === 'type' ? 'type' : 'status']: value || null, page: '1' });
  }, [updateUrl]);

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
    modules,
    rawModules: allModules,
    stats,
    pageInfo,
    selectedModule,
    isDialogOpen,
    isCreating,
    isLoading,
    error,
    refetch,
    filters: { search, status, type },
    view,
    setPage,
    setViewMode,
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
