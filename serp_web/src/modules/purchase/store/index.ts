/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase store barrel export
*/

import { combineReducers } from '@reduxjs/toolkit';
import suppliersReducer from './suppliers/suppliersSlice';
import productsReducer from './products/productsSlice';
import ordersReducer from './orders/ordersSlice';
import addressesReducer from './addresses/addressesSlice';

// Combine purchase reducers
export const purchaseReducer = combineReducers({
  suppliers: suppliersReducer,
  products: productsReducer,
  orders: ordersReducer,
  addresses: addressesReducer,
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
  setFromSupplierId as setOrdersFromSupplierId,
  setSalesChannelId as setOrdersSalesChannelId,
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

// Re-export actions and selectors from addresses
export {
  setEntityId as setAddressesEntityId,
  setEntityType as setAddressesEntityType,
  setAddressType as setAddressesAddressType,
  setPage as setAddressesPage,
  setPageSize as setAddressesPageSize,
  setSorting as setAddressesSorting,
  setDialogOpen as setAddressesDialogOpen,
  openCreateDialog as openCreateAddressDialog,
  openEditDialog as openEditAddressDialog,
  openViewDialog as openViewAddressDialog,
  setSelectedAddress,
  resetFilters as resetAddressesFilters,
  selectAddressesFilters,
  selectAddressesDialogOpen,
  selectAddressesDialogMode,
  selectSelectedAddressId,
  selectAddressesUiState,
} from './addresses/addressesSlice';

