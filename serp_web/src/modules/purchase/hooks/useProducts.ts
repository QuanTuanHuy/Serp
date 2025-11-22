/*
Author: QuanTuanHuy
Description: Part of Serp Project - useProducts custom hook
*/

import { useCallback, useMemo } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks/redux';
import { useNotification } from '@/shared/hooks/use-notification';
import {
  useGetProductsQuery,
  useGetProductByIdQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
  useGetCategoriesQuery,
} from '../services';
import {
  selectProductsFilters,
  selectProductsDialogOpen,
  selectProductsDialogMode,
  selectSelectedProductId,
  setProductsQuery,
  setProductsCategoryId,
  setProductsStatusId,
  setProductsPage,
  setProductsPageSize,
  setProductsSorting,
  openCreateProductDialog,
  openEditProductDialog,
  setProductsDialogOpen,
  resetProductsFilters,
} from '../store';
import type { CreateProductRequest, UpdateProductRequest } from '../types';

export function useProducts() {
  const dispatch = useAppDispatch();
  const { success, error: showError } = useNotification();

  // Redux state
  const filters = useAppSelector(selectProductsFilters);
  const dialogOpen = useAppSelector(selectProductsDialogOpen);
  const dialogMode = useAppSelector(selectProductsDialogMode);
  const selectedProductId = useAppSelector(selectSelectedProductId);

  // RTK Query hooks
  const {
    data: response,
    isLoading,
    isFetching,
    error: queryError,
    refetch,
  } = useGetProductsQuery(filters);

  const { data: selectedProductData, isLoading: isLoadingProduct } =
    useGetProductByIdQuery(selectedProductId!, {
      skip: !selectedProductId,
    });

  const { data: categoriesData, isLoading: isCategoriesLoading } =
    useGetCategoriesQuery();

  const [createProduct, { isLoading: isCreating }] =
    useCreateProductMutation();
  const [updateProduct, { isLoading: isUpdating }] =
    useUpdateProductMutation();
  const [deleteProduct, { isLoading: isDeleting }] =
    useDeleteProductMutation();

  // Extract data from response
  const products = useMemo(() => {
    return response?.data?.items || [];
  }, [response]);

  const pagination = useMemo(() => {
    if (!response?.data) return null;
    return {
      currentPage: response.data.currentPage - 1,
      totalPages: response.data.totalPages,
      totalItems: response.data.totalItems,
      pageSize: filters.size || 10,
    };
  }, [response, filters.size]);

  const selectedProduct = useMemo(() => {
    return selectedProductData?.data;
  }, [selectedProductData]);

  const categories = useMemo(() => {
    return categoriesData?.data?.items || [];
  }, [categoriesData]);

  // Filter handlers
  const handleQueryChange = useCallback(
    (query: string | undefined) => {
      dispatch(setProductsQuery(query));
    },
    [dispatch]
  );

  const handleCategoryChange = useCallback(
    (categoryId: string | undefined) => {
      dispatch(setProductsCategoryId(categoryId));
    },
    [dispatch]
  );

  const handleStatusChange = useCallback(
    (statusId: string | undefined) => {
      dispatch(setProductsStatusId(statusId));
    },
    [dispatch]
  );

  const handlePageChange = useCallback(
    (page: number) => {
      dispatch(setProductsPage(page));
    },
    [dispatch]
  );

  const handlePageSizeChange = useCallback(
    (size: number) => {
      dispatch(setProductsPageSize(size));
    },
    [dispatch]
  );

  const handleSortingChange = useCallback(
    (sortBy: string, sortDirection: 'asc' | 'desc') => {
      dispatch(setProductsSorting({ sortBy, sortDirection }));
    },
    [dispatch]
  );

  const handleResetFilters = useCallback(() => {
    dispatch(resetProductsFilters());
  }, [dispatch]);

  // Dialog handlers
  const handleOpenCreateDialog = useCallback(() => {
    dispatch(openCreateProductDialog());
  }, [dispatch]);

  const handleOpenEditDialog = useCallback(
    (productId: string) => {
      dispatch(openEditProductDialog(productId));
    },
    [dispatch]
  );

  const handleCloseDialog = useCallback(() => {
    dispatch(setProductsDialogOpen(false));
  }, [dispatch]);

  // CRUD handlers
  const handleCreateProduct = useCallback(
    async (data: CreateProductRequest) => {
      try {
        const result = await createProduct(data).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Product created successfully', {
            description: result.message,
          });
          handleCloseDialog();
          refetch();
          return true;
        } else {
          showError('Failed to create product', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error creating product', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [createProduct, success, showError, handleCloseDialog, refetch]
  );

  const handleUpdateProduct = useCallback(
    async (productId: string, data: UpdateProductRequest) => {
      try {
        const result = await updateProduct({ productId, data }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Product updated successfully', {
            description: result.message,
          });
          handleCloseDialog();
          refetch();
          return true;
        } else {
          showError('Failed to update product', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error updating product', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [updateProduct, success, showError, handleCloseDialog, refetch]
  );

  const handleDeleteProduct = useCallback(
    async (productId: string) => {
      try {
        await deleteProduct(productId).unwrap();
        success('Product deleted successfully');
        refetch();
        return true;
      } catch (err: any) {
        showError('Error deleting product', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [deleteProduct, success, showError, refetch]
  );

  return {
    // Data
    products,
    selectedProduct,
    pagination,
    categories,
    // Loading states
    isLoading,
    isFetching,
    isLoadingProduct,
    isCreating,
    isUpdating,
    isDeleting,
    isCategoriesLoading,
    // Error
    error: queryError,
    // Filters
    filters,
    handleQueryChange,
    handleCategoryChange,
    handleStatusChange,
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
    handleCreateProduct,
    handleUpdateProduct,
    handleDeleteProduct,
    // Utilities
    refetch,
  };
}

export type UseProductsReturn = ReturnType<typeof useProducts>;
