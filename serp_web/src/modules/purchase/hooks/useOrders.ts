/*
Author: QuanTuanHuy
Description: Part of Serp Project - useOrders custom hook
*/

import { useCallback, useMemo } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks/redux';
import { useNotification } from '@/shared/hooks/use-notification';
import {
  useGetOrdersQuery,
  useGetOrderByIdQuery,
  useCreateOrderMutation,
  useUpdateOrderMutation,
  useApproveOrderMutation,
  useCancelOrderMutation,
  useAddProductToOrderMutation,
  useUpdateOrderItemMutation,
  useDeleteOrderItemMutation,
} from '../services';
import {
  selectOrdersFilters,
  selectOrdersDialogOpen,
  selectOrdersDialogMode,
  selectSelectedOrderId,
  setOrdersQuery,
  setOrdersStatusId,
  setOrdersOrderTypeId,
  setOrdersFromSupplierId,
  setOrdersToCustomerId,
  setOrdersDateRange,
  setOrdersPage,
  setOrdersPageSize,
  setOrdersSorting,
  openCreateOrderDialog,
  openEditOrderDialog,
  setOrdersDialogOpen,
  resetOrdersFilters,
} from '../store';
import type {
  CreateOrderRequest,
  UpdateOrderRequest,
  AddOrderItemRequest,
  UpdateOrderItemRequest,
  CancelOrderRequest,
} from '../types';

export function useOrders() {
  const dispatch = useAppDispatch();
  const { success, error: showError } = useNotification();

  // Redux state
  const filters = useAppSelector(selectOrdersFilters);
  const dialogOpen = useAppSelector(selectOrdersDialogOpen);
  const dialogMode = useAppSelector(selectOrdersDialogMode);
  const selectedOrderId = useAppSelector(selectSelectedOrderId);

  // RTK Query hooks
  const {
    data: response,
    isLoading,
    isFetching,
    error: queryError,
    refetch,
  } = useGetOrdersQuery(filters);

  const { data: selectedOrderData } = useGetOrderByIdQuery(selectedOrderId!, {
    skip: !selectedOrderId,
  });

  const [createOrder, { isLoading: isCreating }] = useCreateOrderMutation();
  const [updateOrder, { isLoading: isUpdating }] = useUpdateOrderMutation();
  const [approveOrder, { isLoading: isApproving }] = useApproveOrderMutation();
  const [cancelOrder, { isLoading: isCancelling }] = useCancelOrderMutation();
  const [addProductToOrder, { isLoading: isAddingProduct }] =
    useAddProductToOrderMutation();
  const [updateOrderItem, { isLoading: isUpdatingItem }] =
    useUpdateOrderItemMutation();
  const [deleteOrderItem, { isLoading: isDeletingItem }] =
    useDeleteOrderItemMutation();

  // Extract data from response
  const orders = useMemo(() => {
    return response?.data?.items || [];
  }, [response]);

  const pagination = useMemo(() => {
    if (!response?.data) return null;
    return {
      currentPage: response.data.currentPage,
      totalPages: response.data.totalPages,
      totalItems: response.data.totalItems,
      pageSize: filters.size || 10,
    };
  }, [response, filters.size]);

  const selectedOrder = useMemo(() => {
    return selectedOrderData?.data;
  }, [selectedOrderData]);

  // Filter handlers
  const handleQueryChange = useCallback(
    (query: string | undefined) => {
      dispatch(setOrdersQuery(query));
    },
    [dispatch]
  );

  const handleStatusChange = useCallback(
    (statusId: string | undefined) => {
      dispatch(setOrdersStatusId(statusId));
    },
    [dispatch]
  );

  const handleOrderTypeChange = useCallback(
    (orderTypeId: string | undefined) => {
      dispatch(setOrdersOrderTypeId(orderTypeId));
    },
    [dispatch]
  );

  const handleSupplierChange = useCallback(
    (fromSupplierId: string | undefined) => {
      dispatch(setOrdersFromSupplierId(fromSupplierId));
    },
    [dispatch]
  );

  const handleCustomerChange = useCallback(
    (toCustomerId: string | undefined) => {
      dispatch(setOrdersToCustomerId(toCustomerId));
    },
    [dispatch]
  );

  const handleDateRangeChange = useCallback(
    (fromDate?: string, toDate?: string) => {
      dispatch(setOrdersDateRange({ fromDate, toDate }));
    },
    [dispatch]
  );

  const handlePageChange = useCallback(
    (page: number) => {
      dispatch(setOrdersPage(page));
    },
    [dispatch]
  );

  const handlePageSizeChange = useCallback(
    (size: number) => {
      dispatch(setOrdersPageSize(size));
    },
    [dispatch]
  );

  const handleSortingChange = useCallback(
    (sortBy: string, sortDirection: 'asc' | 'desc') => {
      dispatch(setOrdersSorting({ sortBy, sortDirection }));
    },
    [dispatch]
  );

  const handleResetFilters = useCallback(() => {
    dispatch(resetOrdersFilters());
  }, [dispatch]);

  // Dialog handlers
  const handleOpenCreateDialog = useCallback(() => {
    dispatch(openCreateOrderDialog());
  }, [dispatch]);

  const handleOpenEditDialog = useCallback(
    (orderId: string) => {
      dispatch(openEditOrderDialog(orderId));
    },
    [dispatch]
  );

  const handleCloseDialog = useCallback(() => {
    dispatch(setOrdersDialogOpen(false));
  }, [dispatch]);

  // CRUD handlers
  const handleCreateOrder = useCallback(
    async (data: CreateOrderRequest) => {
      try {
        const result = await createOrder(data).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Order created successfully', {
            description: result.message,
          });
          handleCloseDialog();
          refetch();
          return true;
        } else {
          showError('Failed to create order', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error creating order', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [createOrder, success, showError, handleCloseDialog, refetch]
  );

  const handleUpdateOrder = useCallback(
    async (orderId: string, data: UpdateOrderRequest) => {
      try {
        const result = await updateOrder({ orderId, data }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Order updated successfully', {
            description: result.message,
          });
          refetch();
          return true;
        } else {
          showError('Failed to update order', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error updating order', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [updateOrder, success, showError, refetch]
  );

  const handleApproveOrder = useCallback(
    async (orderId: string) => {
      try {
        const result = await approveOrder(orderId).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Order approved successfully', {
            description: result.message,
          });
          refetch();
          return true;
        } else {
          showError('Failed to approve order', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error approving order', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [approveOrder, success, showError, refetch]
  );

  const handleCancelOrder = useCallback(
    async (orderId: string, data?: CancelOrderRequest) => {
      try {
        const result = await cancelOrder({ orderId, data }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Order cancelled successfully', {
            description: result.message,
          });
          refetch();
          return true;
        } else {
          showError('Failed to cancel order', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error cancelling order', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [cancelOrder, success, showError, refetch]
  );

  const handleAddProductToOrder = useCallback(
    async (orderId: string, data: AddOrderItemRequest) => {
      try {
        const result = await addProductToOrder({ orderId, data }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Product added to order successfully', {
            description: result.message,
          });
          return true;
        } else {
          showError('Failed to add product to order', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error adding product to order', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [addProductToOrder, success, showError]
  );

  const handleUpdateOrderItem = useCallback(
    async (
      orderId: string,
      orderItemId: string,
      data: UpdateOrderItemRequest
    ) => {
      try {
        const result = await updateOrderItem({
          orderId,
          orderItemId,
          data,
        }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Order item updated successfully', {
            description: result.message,
          });
          return true;
        } else {
          showError('Failed to update order item', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error updating order item', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [updateOrderItem, success, showError]
  );

  const handleDeleteOrderItem = useCallback(
    async (orderId: string, orderItemId: string) => {
      try {
        const result = await deleteOrderItem({
          orderId,
          orderItemId,
        }).unwrap();
        if (result.code === 200 && result.status.toLowerCase() === 'success') {
          success('Order item removed successfully', {
            description: result.message,
          });
          return true;
        } else {
          showError('Failed to remove order item', {
            description: result.message,
          });
          return false;
        }
      } catch (err: any) {
        showError('Error removing order item', {
          description: err?.data?.message || 'An unexpected error occurred',
        });
        return false;
      }
    },
    [deleteOrderItem, success, showError]
  );

  return {
    // Data
    orders,
    selectedOrder,
    pagination,
    // Loading states
    isLoading,
    isFetching,
    isCreating,
    isUpdating,
    isApproving,
    isCancelling,
    isAddingProduct,
    isUpdatingItem,
    isDeletingItem,
    // Error
    error: queryError,
    // Filters
    filters,
    handleQueryChange,
    handleStatusChange,
    handleOrderTypeChange,
    handleSupplierChange,
    handleCustomerChange,
    handleDateRangeChange,
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
    handleCreateOrder,
    handleUpdateOrder,
    handleApproveOrder,
    handleCancelOrder,
    handleAddProductToOrder,
    handleUpdateOrderItem,
    handleDeleteOrderItem,
    // Utilities
    refetch,
  };
}

export type UseOrdersReturn = ReturnType<typeof useOrders>;
