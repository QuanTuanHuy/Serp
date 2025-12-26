// Sales Selectors (authors: QuanTuanHuy, Description: Part of Serp Project)

import { createSelector } from '@reduxjs/toolkit';
import type { RootState } from '../../../lib/store';
import type {
  Customer,
  Product,
  Order,
  Category,
  Facility,
  InventoryItem,
} from '../types';

// Base selectors
export const selectSales = (state: RootState) => state.sales;
export const selectSalesUI = (state: RootState) => state.sales.ui;
export const selectCustomers = (state: RootState) => state.sales.customers;
export const selectProducts = (state: RootState) => state.sales.products;
export const selectOrders = (state: RootState) => state.sales.orders;
export const selectCategories = (state: RootState) => state.sales.categories;
export const selectFacilities = (state: RootState) => state.sales.facilities;
export const selectInventoryItems = (state: RootState) =>
  state.sales.inventoryItems;

// UI selectors
export const selectActiveModule = createSelector(
  selectSalesUI,
  (ui) => ui.activeModule
);

export const selectViewMode = createSelector(
  selectSalesUI,
  (ui) => ui.viewMode
);

export const selectIsSidebarCollapsed = createSelector(
  selectSalesUI,
  (ui) => ui.sidebarCollapsed
);

export const selectIsFilterPanelOpen = createSelector(
  selectSalesUI,
  (ui) => ui.filterPanelOpen
);

export const selectIsBulkActionMode = createSelector(
  selectSalesUI,
  (ui) => ui.bulkActionMode
);

export const selectGlobalSelectedItems = createSelector(
  selectSalesUI,
  (ui) => ui.selectedItems
);

// Customer selectors
export const selectCustomerItems = createSelector(
  selectCustomers,
  (customers) => customers.items
);

export const selectCustomersLoading = createSelector(
  selectCustomers,
  (customers) => customers.loading
);

export const selectCustomersError = createSelector(
  selectCustomers,
  (customers) => customers.error
);

export const selectCustomerFilters = createSelector(
  selectCustomers,
  (customers) => customers.filters
);

export const selectCustomerPagination = createSelector(
  selectCustomers,
  (customers) => customers.pagination
);

export const selectCustomerSelectedItems = createSelector(
  selectCustomers,
  (customers) => customers.selectedItems
);

export const selectCustomerTotal = createSelector(
  selectCustomers,
  (customers) => customers.total
);

export const selectCustomerById = createSelector(
  [selectCustomerItems, (state: RootState, id: string) => id],
  (customers: Customer[], id: string) =>
    customers.find((customer: Customer) => customer.id === id)
);

// Product selectors
export const selectProductItems = createSelector(
  selectProducts,
  (products) => products.items
);

export const selectProductsLoading = createSelector(
  selectProducts,
  (products) => products.loading
);

export const selectProductsError = createSelector(
  selectProducts,
  (products) => products.error
);

export const selectProductFilters = createSelector(
  selectProducts,
  (products) => products.filters
);

export const selectProductPagination = createSelector(
  selectProducts,
  (products) => products.pagination
);

export const selectProductSelectedItems = createSelector(
  selectProducts,
  (products) => products.selectedItems
);

export const selectProductTotal = createSelector(
  selectProducts,
  (products) => products.total
);

export const selectProductById = createSelector(
  [selectProductItems, (state: RootState, id: string) => id],
  (products: Product[], id: string) =>
    products.find((product: Product) => product.id === id)
);

export const selectProductsByCategory = createSelector(
  [selectProductItems, (state: RootState, categoryId: string) => categoryId],
  (products: Product[], categoryId: string) =>
    products.filter((product: Product) => product.categoryId === categoryId)
);

// Order selectors
export const selectOrderItems = createSelector(
  selectOrders,
  (orders) => orders.items
);

export const selectOrdersLoading = createSelector(
  selectOrders,
  (orders) => orders.loading
);

export const selectOrdersError = createSelector(
  selectOrders,
  (orders) => orders.error
);

export const selectOrderFilters = createSelector(
  selectOrders,
  (orders) => orders.filters
);

export const selectOrderPagination = createSelector(
  selectOrders,
  (orders) => orders.pagination
);

export const selectOrderSelectedItems = createSelector(
  selectOrders,
  (orders) => orders.selectedItems
);

export const selectOrderTotal = createSelector(
  selectOrders,
  (orders) => orders.total
);

export const selectOrderById = createSelector(
  [selectOrderItems, (state: RootState, id: string) => id],
  (orders: Order[], id: string) => orders.find((order: Order) => order.id === id)
);

export const selectOrdersByCustomer = createSelector(
  [selectOrderItems, (state: RootState, customerId: string) => customerId],
  (orders: Order[], customerId: string) =>
    orders.filter((order: Order) => order.toCustomerId === customerId)
);

export const selectOrdersByStatus = createSelector(
  [selectOrderItems, (state: RootState, statusId: string) => statusId],
  (orders: Order[], statusId: string) =>
    orders.filter((order: Order) => order.statusId === statusId)
);

// Category selectors
export const selectCategoryItems = createSelector(
  selectCategories,
  (categories) => categories.items
);

export const selectCategoriesLoading = createSelector(
  selectCategories,
  (categories) => categories.loading
);

export const selectCategoriesError = createSelector(
  selectCategories,
  (categories) => categories.error
);

export const selectCategoryFilters = createSelector(
  selectCategories,
  (categories) => categories.filters
);

export const selectCategoryPagination = createSelector(
  selectCategories,
  (categories) => categories.pagination
);

export const selectCategoryTotal = createSelector(
  selectCategories,
  (categories) => categories.total
);

export const selectCategoryById = createSelector(
  [selectCategoryItems, (state: RootState, id: string) => id],
  (categories: Category[], id: string) =>
    categories.find((category: Category) => category.id === id)
);

// Facility selectors
export const selectFacilityItems = createSelector(
  selectFacilities,
  (facilities) => facilities.items
);

export const selectFacilitiesLoading = createSelector(
  selectFacilities,
  (facilities) => facilities.loading
);

export const selectFacilitiesError = createSelector(
  selectFacilities,
  (facilities) => facilities.error
);

export const selectFacilityFilters = createSelector(
  selectFacilities,
  (facilities) => facilities.filters
);

export const selectFacilityPagination = createSelector(
  selectFacilities,
  (facilities) => facilities.pagination
);

export const selectFacilityTotal = createSelector(
  selectFacilities,
  (facilities) => facilities.total
);

export const selectFacilityById = createSelector(
  [selectFacilityItems, (state: RootState, id: string) => id],
  (facilities: Facility[], id: string) =>
    facilities.find((facility: Facility) => facility.id === id)
);

// Inventory Item selectors
export const selectInventoryItemItems = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.items
);

export const selectInventoryItemsLoading = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.loading
);

export const selectInventoryItemsError = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.error
);

export const selectInventoryItemFilters = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.filters
);

export const selectInventoryItemPagination = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.pagination
);

export const selectInventoryItemTotal = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.total
);

export const selectInventoryItemById = createSelector(
  [selectInventoryItemItems, (state: RootState, id: string) => id],
  (inventoryItems: InventoryItem[], id: string) =>
    inventoryItems.find((item: InventoryItem) => item.id === id)
);

export const selectInventoryItemsByProduct = createSelector(
  [selectInventoryItemItems, (state: RootState, productId: string) => productId],
  (inventoryItems: InventoryItem[], productId: string) =>
    inventoryItems.filter((item: InventoryItem) => item.productId === productId)
);

export const selectInventoryItemsByFacility = createSelector(
  [
    selectInventoryItemItems,
    (state: RootState, facilityId: string) => facilityId,
  ],
  (inventoryItems: InventoryItem[], facilityId: string) =>
    inventoryItems.filter(
      (item: InventoryItem) => item.facilityId === facilityId
    )
);

// Computed selectors
export const selectTotalOrderAmount = createSelector(
  selectOrderItems,
  (orders: Order[]) =>
    orders.reduce((total, order) => total + (order.totalAmount || 0), 0)
);
