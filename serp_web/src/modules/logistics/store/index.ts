/**
 * Logistics Module - Store Configuration
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Combine all logistics reducers
 */

import { combineReducers } from '@reduxjs/toolkit';
import categoryReducer from './categorySlice';
import productReducer from './productSlice';
import customerReducer from './customerSlice';
import supplierReducer from './supplierSlice';
import facilityReducer from './facilitySlice';
import inventoryItemReducer from './inventoryItemSlice';
import orderReducer from './orderSlice';
import shipmentReducer from './shipmentSlice';
import addressReducer from './addressSlice';

export const logisticsReducer = combineReducers({
  category: categoryReducer,
  product: productReducer,
  customer: customerReducer,
  supplier: supplierReducer,
  facility: facilityReducer,
  inventoryItem: inventoryItemReducer,
  order: orderReducer,
  shipment: shipmentReducer,
  address: addressReducer,
});

// Re-export actions and selectors for convenience
export {
  setFilters as setCategoryFilters,
  setPage as setCategoryPage,
  setPageSize as setCategoryPageSize,
  setSearchQuery as setCategorySearchQuery,
  openDialog as openCategoryDialog,
  closeDialog as closeCategoryDialog,
  resetFilters as resetCategoryFilters,
  selectCategoryFilters,
  selectCategoryDialogOpen,
  selectCategoryDialogMode,
  selectSelectedCategoryId,
} from './categorySlice';

export {
  setFilters as setProductFilters,
  setPage as setProductPage,
  setPageSize as setProductPageSize,
  setSearchQuery as setProductSearchQuery,
  setCategoryFilter as setProductCategoryFilter,
  setViewMode as setProductViewMode,
  openDialog as openProductDialog,
  closeDialog as closeProductDialog,
  resetFilters as resetProductFilters,
  selectProductFilters,
  selectProductDialogOpen,
  selectProductDialogMode,
  selectSelectedProductId,
  selectProductViewMode,
} from './productSlice';

// Customer
export {
  selectCustomer,
  selectSelectedCustomerId,
} from './customerSlice';

// Supplier
export {
  selectSupplier,
  selectSelectedSupplierId,
} from './supplierSlice';

// Facility
export {
  setFilters as setFacilityFilters,
  setPage as setFacilityPage,
  setPageSize as setFacilityPageSize,
  setSearchQuery as setFacilitySearchQuery,
  openDialog as openFacilityDialog,
  closeDialog as closeFacilityDialog,
  resetFilters as resetFacilityFilters,
  selectFacilityFilters,
  selectFacilityDialogOpen,
  selectFacilityDialogMode,
  selectSelectedFacilityId,
} from './facilitySlice';

// Inventory Item
export {
  setFilters as setInventoryItemFilters,
  setPage as setInventoryItemPage,
  setPageSize as setInventoryItemPageSize,
  setSearchQuery as setInventoryItemSearchQuery,
  setProductFilter as setInventoryItemProductFilter,
  setFacilityFilter as setInventoryItemFacilityFilter,
  openDialog as openInventoryItemDialog,
  closeDialog as closeInventoryItemDialog,
  resetFilters as resetInventoryItemFilters,
  selectInventoryItemFilters,
  selectInventoryItemDialogOpen,
  selectInventoryItemDialogMode,
  selectSelectedInventoryItemId,
} from './inventoryItemSlice';

// Order
export {
  setFilters as setOrderFilters,
  setPage as setOrderPage,
  setPageSize as setOrderPageSize,
  setSearchQuery as setOrderSearchQuery,
  setCustomerFilter as setOrderCustomerFilter,
  setSupplierFilter as setOrderSupplierFilter,
  selectOrder,
  resetFilters as resetOrderFilters,
  selectOrderFilters,
  selectSelectedOrderId,
} from './orderSlice';

// Shipment
export {
  setFilters as setShipmentFilters,
  setPage as setShipmentPage,
  setPageSize as setShipmentPageSize,
  setSearchQuery as setShipmentSearchQuery,
  setCustomerFilter as setShipmentCustomerFilter,
  setSupplierFilter as setShipmentSupplierFilter,
  setOrderFilter as setShipmentOrderFilter,
  openDialog as openShipmentDialog,
  closeDialog as closeShipmentDialog,
  openItemDialog as openShipmentItemDialog,
  closeItemDialog as closeShipmentItemDialog,
  resetFilters as resetShipmentFilters,
  selectShipmentFilters,
  selectShipmentDialogOpen,
  selectShipmentDialogMode,
  selectSelectedShipmentId,
  selectShipmentItemDialogOpen,
  selectSelectedItemId,
} from './shipmentSlice';

// Address
export {
  openDialog as openAddressDialog,
  closeDialog as closeAddressDialog,
  selectAddressDialogOpen,
  selectAddressDialogMode,
  selectSelectedAddressId,
  selectAddressEntityId,
  selectAddressEntityTypeId,
} from './addressSlice';
