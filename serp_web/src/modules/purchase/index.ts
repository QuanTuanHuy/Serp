/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase module barrel export
*/

// ============= Types =============
export type * from './types';

// ============= Services (RTK Query hooks) =============
export {
  // Supplier hooks
  useGetSuppliersQuery,
  useGetSupplierByIdQuery,
  useCreateSupplierMutation,
  useUpdateSupplierMutation,
  useDeleteSupplierMutation,
  // Product hooks
  useGetProductsQuery,
  useGetProductByIdQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
  // Category hooks
  useGetCategoriesQuery,
  useCreateCategoryMutation,
  // Order hooks
  useGetOrdersQuery,
  useGetOrderByIdQuery,
  useCreateOrderMutation,
  useUpdateOrderMutation,
  useApproveOrderMutation,
  useCancelOrderMutation,
  useAddProductToOrderMutation,
  useUpdateOrderItemMutation,
  useDeleteOrderItemMutation,
  // Facility hooks
  useGetFacilitiesQuery,
  useGetFacilityByIdQuery,
  useCreateFacilityMutation,
  useUpdateFacilityMutation,
  useDeleteFacilityMutation,
  // Shipment hooks
  useGetShipmentsQuery,
  useGetShipmentByIdQuery,
  useCreateShipmentMutation,
  useUpdateShipmentMutation,
  useDeleteShipmentMutation,
} from './services';

// ============= Custom Hooks =============
export { useSuppliers, useProducts, useOrders } from './hooks';
export type {
  UseSuppliersReturn,
  UseProductsReturn,
  UseOrdersReturn,
} from './hooks';

// ============= Store (Actions & Selectors) =============
// Suppliers
export {
  setSuppliersQuery,
  setSuppliersStatusId,
  setSuppliersPage,
  setSuppliersPageSize,
  setSuppliersSorting,
  setSuppliersDialogOpen,
  openCreateSupplierDialog,
  openEditSupplierDialog,
  openViewSupplierDialog,
  setSelectedSupplier,
  setSuppliersViewMode,
  resetSuppliersFilters,
  selectSuppliersFilters,
  selectSuppliersDialogOpen,
  selectSuppliersDialogMode,
  selectSelectedSupplierId,
  selectSuppliersViewMode,
  selectSuppliersUiState,
} from './store';

// Products
export {
  setProductsQuery,
  setProductsCategoryId,
  setProductsStatusId,
  setProductsPage,
  setProductsPageSize,
  setProductsSorting,
  setProductsDialogOpen,
  openCreateProductDialog,
  openEditProductDialog,
  openViewProductDialog,
  setSelectedProduct,
  setProductsViewMode,
  resetProductsFilters,
  selectProductsFilters,
  selectProductsDialogOpen,
  selectProductsDialogMode,
  selectSelectedProductId,
  selectProductsViewMode,
  selectProductsUiState,
} from './store';

// Orders
export {
  setOrdersQuery,
  setOrdersStatusId,
  setOrdersOrderTypeId,
  setOrdersFromSupplierId,
  setOrdersToCustomerId,
  setOrdersDateRange,
  setOrdersPage,
  setOrdersPageSize,
  setOrdersSorting,
  setOrdersDialogOpen,
  openCreateOrderDialog,
  openEditOrderDialog,
  openViewOrderDialog,
  setSelectedOrder,
  setOrdersViewMode,
  resetOrdersFilters,
  selectOrdersFilters,
  selectOrdersDialogOpen,
  selectOrdersDialogMode,
  selectSelectedOrderId,
  selectOrdersViewMode,
  selectOrdersUiState,
} from './store';

// ============= Components =============
export {
  // Layout Components
  PurchaseLayout,
  PurchaseHeader,
  // Auth Guard
  PurchaseAuthGuard,
  // Product Components
  ProductForm,
  ProductFormDialog,
} from './components';
