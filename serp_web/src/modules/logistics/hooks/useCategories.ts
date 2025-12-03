/**
 * Logistics Module - Category Custom Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Category business logic and state management
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useGetCategoriesQuery,
  useGetCategoryQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeleteCategoryMutation,
} from '../services';
import {
  selectCategoryFilters,
  selectCategoryDialogOpen,
  selectCategoryDialogMode,
  selectSelectedCategoryId,
  setCategoryFilters,
  setCategoryPage,
  setCategoryPageSize,
  setCategorySearchQuery,
  openCategoryDialog,
  closeCategoryDialog,
  resetCategoryFilters,
} from '../store';
import type { CreateCategoryRequest, UpdateCategoryRequest } from '../types';

export const useCategories = () => {
  const dispatch = useAppDispatch();

  // Selectors
  const filters = useAppSelector(selectCategoryFilters);
  const dialogOpen = useAppSelector(selectCategoryDialogOpen);
  const dialogMode = useAppSelector(selectCategoryDialogMode);
  const selectedCategoryId = useAppSelector(selectSelectedCategoryId);

  // Queries
  const { data: categoriesData, isLoading: isLoadingCategories } =
    useGetCategoriesQuery(filters);

  const { data: selectedCategory, isLoading: isLoadingCategory } =
    useGetCategoryQuery(selectedCategoryId || '', {
      skip: !selectedCategoryId,
    });

  // Mutations
  const [createCategory, { isLoading: isCreating }] =
    useCreateCategoryMutation();
  const [updateCategory, { isLoading: isUpdating }] =
    useUpdateCategoryMutation();
  const [deleteCategory, { isLoading: isDeleting }] =
    useDeleteCategoryMutation();

  // Handlers
  const handleSetFilters = useCallback(
    (newFilters: Partial<typeof filters>) => {
      dispatch(setCategoryFilters(newFilters));
    },
    [dispatch]
  );

  const handleSetPage = useCallback(
    (page: number) => {
      dispatch(setCategoryPage(page));
    },
    [dispatch]
  );

  const handleSetPageSize = useCallback(
    (size: number) => {
      dispatch(setCategoryPageSize(size));
    },
    [dispatch]
  );

  const handleSearch = useCallback(
    (query: string) => {
      dispatch(setCategorySearchQuery(query));
    },
    [dispatch]
  );

  const handleOpenDialog = useCallback(
    (mode: 'create' | 'edit', categoryId?: string) => {
      dispatch(openCategoryDialog({ mode, categoryId }));
    },
    [dispatch]
  );

  const handleCloseDialog = useCallback(() => {
    dispatch(closeCategoryDialog());
  }, [dispatch]);

  const handleCreate = useCallback(
    async (data: CreateCategoryRequest) => {
      try {
        await createCategory(data).unwrap();
        handleCloseDialog();
        return { success: true };
      } catch (error) {
        return { success: false, error };
      }
    },
    [createCategory, handleCloseDialog]
  );

  const handleUpdate = useCallback(
    async (categoryId: string, data: UpdateCategoryRequest) => {
      try {
        await updateCategory({ categoryId, data }).unwrap();
        handleCloseDialog();
        return { success: true };
      } catch (error) {
        return { success: false, error };
      }
    },
    [updateCategory, handleCloseDialog]
  );

  const handleDelete = useCallback(
    async (categoryId: string) => {
      try {
        await deleteCategory(categoryId).unwrap();
        return { success: true };
      } catch (error) {
        return { success: false, error };
      }
    },
    [deleteCategory]
  );

  const handleResetFilters = useCallback(() => {
    dispatch(resetCategoryFilters());
  }, [dispatch]);

  return {
    // Data
    categories: categoriesData?.items || [],
    totalItems: categoriesData?.totalItems || 0,
    totalPages: categoriesData?.totalPages || 0,
    selectedCategory,

    // Loading states
    isLoadingCategories,
    isLoadingCategory,
    isCreating,
    isUpdating,
    isDeleting,

    // UI state
    filters,
    dialogOpen,
    dialogMode,
    selectedCategoryId,

    // Handlers
    setFilters: handleSetFilters,
    setPage: handleSetPage,
    setPageSize: handleSetPageSize,
    search: handleSearch,
    openDialog: handleOpenDialog,
    closeDialog: handleCloseDialog,
    create: handleCreate,
    update: handleUpdate,
    delete: handleDelete,
    resetFilters: handleResetFilters,
  };
};
