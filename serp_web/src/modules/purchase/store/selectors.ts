// Purchase Store Selectors (authors: QuanTuanHuy, Description: Part of Serp Project)

import { RootState } from '@/lib/store';

export const selectSelectedSupplier = (state: RootState) =>
  state.purchase.selectedSupplier;

export const selectSelectedProduct = (state: RootState) =>
  state.purchase.selectedProduct;

export const selectSelectedOrder = (state: RootState) =>
  state.purchase.selectedOrder;

export const selectSelectedCategory = (state: RootState) =>
  state.purchase.selectedCategory;

export const selectSelectedFacility = (state: RootState) =>
  state.purchase.selectedFacility;

export const selectSupplierFilters = (state: RootState) =>
  state.purchase.filters.suppliers;

export const selectProductFilters = (state: RootState) =>
  state.purchase.filters.products;

export const selectOrderFilters = (state: RootState) =>
  state.purchase.filters.orders;

export const selectCategoryFilters = (state: RootState) =>
  state.purchase.filters.categories;

export const selectFacilityFilters = (state: RootState) =>
  state.purchase.filters.facilities;
