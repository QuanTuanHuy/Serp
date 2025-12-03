/**
 * Logistics Module - Product Custom Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Product business logic and state management
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useGetProductsQuery,
  useGetProductQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
} from '../services';
import {
  selectProductFilters,
  selectProductDialogOpen,
  selectProductDialogMode,
  selectSelectedProductId,
  selectProductViewMode,
  setProductFilters,
  setProductPage,
  setProductPageSize,
  setProductSearchQuery,
  setProductCategoryFilter,
  setProductViewMode,
  openProductDialog,
  closeProductDialog,
  resetProductFilters,
} from '../store';
import type { CreateProductRequest, UpdateProductRequest } from '../types';

export const useProducts = () => {
  const dispatch = useAppDispatch();

  // Selectors
  const filters = useAppSelector(selectProductFilters);
  const dialogOpen = useAppSelector(selectProductDialogOpen);
  const dialogMode = useAppSelector(selectProductDialogMode);
  const selectedProductId = useAppSelector(selectSelectedProductId);
  const viewMode = useAppSelector(selectProductViewMode);

  // Queries
  const { data: productsData, isLoading: isLoadingProducts } =
    useGetProductsQuery(filters);

  const { data: selectedProduct, isLoading: isLoadingProduct } =
    useGetProductQuery(selectedProductId || '', {
      skip: !selectedProductId,
    });

  // Mutations
  const [createProduct, { isLoading: isCreating }] =
    useCreateProductMutation();
  const [updateProduct, { isLoading: isUpdating }] =
    useUpdateProductMutation();
  const [deleteProduct, { isLoading: isDeleting }] =
    useDeleteProductMutation();

  // Handlers
  const handleSetFilters = useCallback(
    (newFilters: Partial<typeof filters>) => {
      dispatch(setProductFilters(newFilters));
    },
    [dispatch]
  );

  const handleSetPage = useCallback(
    (page: number) => {
      dispatch(setProductPage(page));
    },
    [dispatch]
  );

  const handleSetPageSize = useCallback(
    (size: number) => {
      dispatch(setProductPageSize(size));
    },
    [dispatch]
  );

  const handleSearch = useCallback(
    (query: string) => {
      dispatch(setProductSearchQuery(query));
    },
    [dispatch]
  );

  const handleSetCategoryFilter = useCallback(
    (categoryId: string | undefined) => {
      dispatch(setProductCategoryFilter(categoryId));
    },
    [dispatch]
  );

  const handleSetViewMode = useCallback(
    (mode: 'grid' | 'list') => {
      dispatch(setProductViewMode(mode));
    },
    [dispatch]
  );

  const handleOpenDialog = useCallback(
    (mode: 'create' | 'edit', productId?: string) => {
      dispatch(openProductDialog({ mode, productId }));
    },
    [dispatch]
  );

  const handleCloseDialog = useCallback(() => {
    dispatch(closeProductDialog());
  }, [dispatch]);

  const handleCreate = useCallback(
    async (data: CreateProductRequest) => {
      try {
        await createProduct(data).unwrap();
        handleCloseDialog();
        return { success: true };
      } catch (error) {
        return { success: false, error };
      }
    },
    [createProduct, handleCloseDialog]
  );

  const handleUpdate = useCallback(
    async (productId: string, data: UpdateProductRequest) => {
      try {
        await updateProduct({ productId, data }).unwrap();
        handleCloseDialog();
        return { success: true };
      } catch (error) {
        return { success: false, error };
      }
    },
    [updateProduct, handleCloseDialog]
  );

  const handleDelete = useCallback(
    async (productId: string) => {
      try {
        await deleteProduct(productId).unwrap();
        return { success: true };
      } catch (error) {
        return { success: false, error };
      }
    },
    [deleteProduct]
  );

  const handleResetFilters = useCallback(() => {
    dispatch(resetProductFilters());
  }, [dispatch]);

  return {
    // Data
    products: productsData?.items || [],
    totalItems: productsData?.totalItems || 0,
    totalPages: productsData?.totalPages || 0,
    selectedProduct,

    // Loading states
    isLoadingProducts,
    isLoadingProduct,
    isCreating,
    isUpdating,
    isDeleting,

    // UI state
    filters,
    dialogOpen,
    dialogMode,
    selectedProductId,
    viewMode,

    // Handlers
    setFilters: handleSetFilters,
    setPage: handleSetPage,
    setPageSize: handleSetPageSize,
    search: handleSearch,
    setCategoryFilter: handleSetCategoryFilter,
    setViewMode: handleSetViewMode,
    openDialog: handleOpenDialog,
    closeDialog: handleCloseDialog,
    create: handleCreate,
    update: handleUpdate,
    delete: handleDelete,
    resetFilters: handleResetFilters,
  };
};
