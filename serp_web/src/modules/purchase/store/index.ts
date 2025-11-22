/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase store barrel export
*/

import { combineReducers } from '@reduxjs/toolkit';
import suppliersReducer from './suppliers/suppliersSlice';
import productsReducer from './products/productsSlice';
import ordersReducer from './orders/ordersSlice';

// Combine purchase reducers
export const purchaseReducer = combineReducers({
  suppliers: suppliersReducer,
  products: productsReducer,
  orders: ordersReducer,
});

// Re-export actions and selectors from suppliers
export {
  setQuery as setSuppliersQuery,
  setStatusId as setSuppliersStatusId,
  setPage as setSuppliersPage,
  setPageSize as setSuppliersPageSize,
  setSorting as setSuppliersSorting,
  setDialogOpen as setSuppliersDialogOpen,
  openCreateDialog as openCreateSupplierDialog,
  openEditDialog as openEditSupplierDialog,
  openViewDialog as openViewSupplierDialog,
  setSelectedSupplier,
  setViewMode as setSuppliersViewMode,
  resetFilters as resetSuppliersFilters,
  selectSuppliersFilters,
  selectSuppliersDialogOpen,
  selectSuppliersDialogMode,
  selectSelectedSupplierId,
  selectSuppliersViewMode,
  selectSuppliersUiState,
} from './suppliers/suppliersSlice';

// Re-export actions and selectors from products
export {
  setQuery as setProductsQuery,
  setCategoryId as setProductsCategoryId,
  setStatusId as setProductsStatusId,
  setPage as setProductsPage,
  setPageSize as setProductsPageSize,
  setSorting as setProductsSorting,
  setDialogOpen as setProductsDialogOpen,
  openCreateDialog as openCreateProductDialog,
  openEditDialog as openEditProductDialog,
  openViewDialog as openViewProductDialog,
  setSelectedProduct,
  setViewMode as setProductsViewMode,
  resetFilters as resetProductsFilters,
  selectProductsFilters,
  selectProductsDialogOpen,
  selectProductsDialogMode,
  selectSelectedProductId,
  selectProductsViewMode,
  selectProductsUiState,
} from './products/productsSlice';

// Re-export actions and selectors from orders
export {
  setQuery as setOrdersQuery,
  setStatusId as setOrdersStatusId,
  setOrderTypeId as setOrdersOrderTypeId,
  setFromSupplierId as setOrdersFromSupplierId,
  setToCustomerId as setOrdersToCustomerId,
  setDateRange as setOrdersDateRange,
  setPage as setOrdersPage,
  setPageSize as setOrdersPageSize,
  setSorting as setOrdersSorting,
  setDialogOpen as setOrdersDialogOpen,
  openCreateDialog as openCreateOrderDialog,
  openEditDialog as openEditOrderDialog,
  openViewDialog as openViewOrderDialog,
  setSelectedOrder,
  setViewMode as setOrdersViewMode,
  resetFilters as resetOrdersFilters,
  selectOrdersFilters,
  selectOrdersDialogOpen,
  selectOrdersDialogMode,
  selectSelectedOrderId,
  selectOrdersViewMode,
  selectOrdersUiState,
} from './orders/ordersSlice';
