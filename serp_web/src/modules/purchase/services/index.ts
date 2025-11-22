/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase services barrel export
*/

export { purchaseApi } from './purchaseApi';

// Export all hooks
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
} from './purchaseApi';
