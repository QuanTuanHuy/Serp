/**
 * Logistics Module - Services Barrel Export
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Export all API hooks
 */

// Address API
export {
  useCreateAddressMutation,
  useUpdateAddressMutation,
  useGetAddressesByEntityQuery,
} from './addressApi';

// Category API
export {
  useGetCategoriesQuery,
  useGetCategoryQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeleteCategoryMutation,
} from './categoryApi';

// Customer API
export { useGetCustomersQuery } from './customerApi';

// Product API
export {
  useGetProductsQuery,
  useGetProductQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
} from './productApi';

// Supplier API
export { useGetSuppliersQuery, useGetSupplierQuery } from './supplierApi';

// Facility API
export {
  useGetFacilitiesQuery,
  useGetFacilityQuery,
  useCreateFacilityMutation,
  useUpdateFacilityMutation,
  useDeleteFacilityMutation,
} from './facilityApi';

// Inventory Item API
export {
  useGetInventoryItemsQuery,
  useGetInventoryItemQuery,
  useCreateInventoryItemMutation,
  useUpdateInventoryItemMutation,
  useDeleteInventoryItemMutation,
} from './inventoryItemApi';

// Order API
export { useGetOrdersQuery, useGetOrderQuery } from './orderApi';

// Shipment API
export {
  useGetShipmentsQuery,
  useGetShipmentQuery,
  useCreateShipmentMutation,
  useUpdateShipmentMutation,
  useImportShipmentMutation,
  useDeleteShipmentMutation,
  useAddItemToShipmentMutation,
  useUpdateItemInShipmentMutation,
  useDeleteItemFromShipmentMutation,
} from './shipmentApi';
