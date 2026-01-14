/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Selectors
*/

import { createSelector } from '@reduxjs/toolkit';
import type { RootState } from '../../../lib/store';
import type {
  Category,
  Customer,
  Facility,
  InventoryItem,
  Order,
  Product,
  Shipment,
  Supplier,
} from '../types';

// Base selectors
export const selectLogistics = (state: RootState) => state.logistics;
export const selectLogisticsUI = (state: RootState) => state.logistics.ui;
export const selectCategories = (state: RootState) =>
  state.logistics.categories;
export const selectCustomers = (state: RootState) => state.logistics.customers;
export const selectFacilities = (state: RootState) =>
  state.logistics.facilities;
export const selectInventoryItems = (state: RootState) =>
  state.logistics.inventoryItems;
export const selectOrders = (state: RootState) => state.logistics.orders;
export const selectProducts = (state: RootState) => state.logistics.products;
export const selectShipments = (state: RootState) => state.logistics.shipments;
export const selectSuppliers = (state: RootState) => state.logistics.suppliers;

// UI selectors
export const selectActiveModule = createSelector(
  selectLogisticsUI,
  (ui) => ui.activeModule
);

export const selectViewMode = createSelector(
  selectLogisticsUI,
  (ui) => ui.viewMode
);

export const selectIsSidebarCollapsed = createSelector(
  selectLogisticsUI,
  (ui) => ui.sidebarCollapsed
);

export const selectIsFilterPanelOpen = createSelector(
  selectLogisticsUI,
  (ui) => ui.filterPanelOpen
);

export const selectIsBulkActionMode = createSelector(
  selectLogisticsUI,
  (ui) => ui.bulkActionMode
);

export const selectGlobalSelectedItems = createSelector(
  selectLogisticsUI,
  (ui) => ui.selectedItems
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

export const selectCategorySelectedItems = createSelector(
  selectCategories,
  (categories) => categories.selectedItems
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

export const selectFacilitySelectedItems = createSelector(
  selectFacilities,
  (facilities) => facilities.selectedItems
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

export const selectDefaultFacility = createSelector(
  selectFacilityItems,
  (facilities: Facility[]) =>
    facilities.find((facility: Facility) => facility.default === true)
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

export const selectInventoryItemSelectedItems = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.selectedItems
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
  [
    selectInventoryItemItems,
    (state: RootState, productId: string) => productId,
  ],
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

export const selectInventoryItemsByStatus = createSelector(
  [selectInventoryItemItems, (state: RootState, statusId: string) => statusId],
  (inventoryItems: InventoryItem[], statusId: string) =>
    inventoryItems.filter((item: InventoryItem) => item.statusId === statusId)
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
  (orders: Order[], id: string) =>
    orders.find((order: Order) => order.id === id)
);

export const selectOrdersByStatus = createSelector(
  [selectOrderItems, (state: RootState, statusId: string) => statusId],
  (orders: Order[], statusId: string) =>
    orders.filter((order: Order) => order.statusId === statusId)
);

export const selectOrdersByCustomer = createSelector(
  [selectOrderItems, (state: RootState, customerId: string) => customerId],
  (orders: Order[], customerId: string) =>
    orders.filter((order: Order) => order.toCustomerId === customerId)
);

export const selectOrdersBySupplier = createSelector(
  [selectOrderItems, (state: RootState, supplierId: string) => supplierId],
  (orders: Order[], supplierId: string) =>
    orders.filter((order: Order) => order.fromSupplierId === supplierId)
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

export const selectProductsByStatus = createSelector(
  [selectProductItems, (state: RootState, statusId: string) => statusId],
  (products: Product[], statusId: string) =>
    products.filter((product: Product) => product.statusId === statusId)
);

// Shipment selectors
export const selectShipmentItems = createSelector(
  selectShipments,
  (shipments) => shipments.items
);

export const selectShipmentsLoading = createSelector(
  selectShipments,
  (shipments) => shipments.loading
);

export const selectShipmentsError = createSelector(
  selectShipments,
  (shipments) => shipments.error
);

export const selectShipmentFilters = createSelector(
  selectShipments,
  (shipments) => shipments.filters
);

export const selectShipmentPagination = createSelector(
  selectShipments,
  (shipments) => shipments.pagination
);

export const selectShipmentSelectedItems = createSelector(
  selectShipments,
  (shipments) => shipments.selectedItems
);

export const selectShipmentTotal = createSelector(
  selectShipments,
  (shipments) => shipments.total
);

export const selectShipmentById = createSelector(
  [selectShipmentItems, (state: RootState, id: string) => id],
  (shipments: Shipment[], id: string) =>
    shipments.find((shipment: Shipment) => shipment.id === id)
);

export const selectShipmentsByStatus = createSelector(
  [selectShipmentItems, (state: RootState, statusId: string) => statusId],
  (shipments: Shipment[], statusId: string) =>
    shipments.filter((shipment: Shipment) => shipment.statusId === statusId)
);

export const selectShipmentsByType = createSelector(
  [
    selectShipmentItems,
    (state: RootState, shipmentTypeId: string) => shipmentTypeId,
  ],
  (shipments: Shipment[], shipmentTypeId: string) =>
    shipments.filter(
      (shipment: Shipment) => shipment.shipmentTypeId === shipmentTypeId
    )
);

export const selectShipmentsByOrder = createSelector(
  [selectShipmentItems, (state: RootState, orderId: string) => orderId],
  (shipments: Shipment[], orderId: string) =>
    shipments.filter((shipment: Shipment) => shipment.orderId === orderId)
);

// Supplier selectors
export const selectSupplierItems = createSelector(
  selectSuppliers,
  (suppliers) => suppliers.items
);

export const selectSuppliersLoading = createSelector(
  selectSuppliers,
  (suppliers) => suppliers.loading
);

export const selectSuppliersError = createSelector(
  selectSuppliers,
  (suppliers) => suppliers.error
);

export const selectSupplierFilters = createSelector(
  selectSuppliers,
  (suppliers) => suppliers.filters
);

export const selectSupplierPagination = createSelector(
  selectSuppliers,
  (suppliers) => suppliers.pagination
);

export const selectSupplierSelectedItems = createSelector(
  selectSuppliers,
  (suppliers) => suppliers.selectedItems
);

export const selectSupplierTotal = createSelector(
  selectSuppliers,
  (suppliers) => suppliers.total
);

export const selectSupplierById = createSelector(
  [selectSupplierItems, (state: RootState, id: string) => id],
  (suppliers: Supplier[], id: string) =>
    suppliers.find((supplier: Supplier) => supplier.id === id)
);

export const selectSuppliersByStatus = createSelector(
  [selectSupplierItems, (state: RootState, statusId: string) => statusId],
  (suppliers: Supplier[], statusId: string) =>
    suppliers.filter((supplier: Supplier) => supplier.statusId === statusId)
);

// Computed selectors
export const selectTotalInventoryValue = createSelector(
  selectInventoryItemItems,
  (inventoryItems: InventoryItem[]) =>
    inventoryItems.reduce(
      (total, item) => total + item.quantityOnHand * (item as any).price || 0,
      0
    )
);

export const selectTotalInventoryQuantity = createSelector(
  selectInventoryItemItems,
  (inventoryItems: InventoryItem[]) =>
    inventoryItems.reduce((total, item) => total + item.quantityOnHand, 0)
);

export const selectTotalShipmentWeight = createSelector(
  selectShipmentItems,
  (shipments: Shipment[]) =>
    shipments.reduce(
      (total, shipment) => total + (shipment.totalWeight || 0),
      0
    )
);

export const selectTotalShipmentQuantity = createSelector(
  selectShipmentItems,
  (shipments: Shipment[]) =>
    shipments.reduce(
      (total, shipment) => total + (shipment.totalQuantity || 0),
      0
    )
);

export const selectLowStockProducts = createSelector(
  [selectProductItems, selectInventoryItemItems],
  (products: Product[], inventoryItems: InventoryItem[]) => {
    return products.filter((product) => {
      const totalStock = inventoryItems
        .filter((item) => item.productId === product.id)
        .reduce((sum, item) => sum + item.quantityOnHand, 0);
      return totalStock < 10; // Threshold for low stock
    });
  }
);

export const selectExpiredInventoryItems = createSelector(
  selectInventoryItemItems,
  (inventoryItems: InventoryItem[]) => {
    const now = new Date();
    return inventoryItems.filter((item) => {
      if (!item.expirationDate) return false;
      return new Date(item.expirationDate) < now;
    });
  }
);

export const selectExpiringSoonInventoryItems = createSelector(
  selectInventoryItemItems,
  (inventoryItems: InventoryItem[]) => {
    const now = new Date();
    const thirtyDaysFromNow = new Date(
      now.getTime() + 30 * 24 * 60 * 60 * 1000
    );
    return inventoryItems.filter((item) => {
      if (!item.expirationDate) return false;
      const expirationDate = new Date(item.expirationDate);
      return expirationDate > now && expirationDate <= thirtyDaysFromNow;
    });
  }
);
