# Sales Module API

This module provides API integration for the Sales service, following the same pattern as the CRM module.

## Structure

```
sales/
├── api/
│   └── salesApi.ts      # RTK Query API endpoints
├── types/
│   └── index.ts         # TypeScript types and interfaces
└── index.ts             # Barrel exports
```

## Features

### API Endpoints

The module includes complete CRUD operations for:

1. **Customers** - Customer management for sales
   - `useGetCustomersQuery` - Get paginated customers with filters
   - `useGetCustomerQuery` - Get single customer detail
   - `useCreateCustomerMutation` - Create new customer
   - `useUpdateCustomerMutation` - Update customer
   - `useDeleteCustomerMutation` - Delete customer

2. **Addresses** - Address management for entities
   - `useGetAddressesQuery` - Get addresses for entity
   - `useCreateAddressMutation` - Create new address
   - `useUpdateAddressMutation` - Update address
   - `useDeleteAddressMutation` - Delete address

3. **Categories** - Product categories
   - `useGetCategoriesQuery` - Get paginated categories
   - `useGetCategoryQuery` - Get category detail
   - `useCreateCategoryMutation` - Create category
   - `useUpdateCategoryMutation` - Update category
   - `useDeleteCategoryMutation` - Delete category

4. **Facilities** - Warehouse/storage facilities
   - `useGetFacilitiesQuery` - Get paginated facilities
   - `useGetFacilityQuery` - Get facility detail
   - `useCreateFacilityMutation` - Create facility
   - `useUpdateFacilityMutation` - Update facility
   - `useDeleteFacilityMutation` - Delete facility

5. **Inventory Items** - Stock inventory management
   - `useGetInventoryItemsQuery` - Get paginated inventory items
   - `useGetInventoryItemQuery` - Get inventory item detail
   - `useCreateInventoryItemMutation` - Create inventory item
   - `useUpdateInventoryItemMutation` - Update inventory item
   - `useDeleteInventoryItemMutation` - Delete inventory item

6. **Products** - Product catalog
   - `useGetProductsQuery` - Get paginated products
   - `useGetProductQuery` - Get product detail
   - `useCreateProductMutation` - Create product
   - `useUpdateProductMutation` - Update product
   - `useDeleteProductMutation` - Delete product

7. **Orders** - Sales order management
   - `useGetOrdersQuery` - Get paginated orders
   - `useGetOrderQuery` - Get order detail
   - `useCreateOrderMutation` - Create order
   - `useUpdateOrderMutation` - Update order
   - `useDeleteOrderMutation` - Delete order
   - `useAddProductToOrderMutation` - Add product to order
   - `useDeleteProductFromOrderMutation` - Remove product from order
   - `useApproveOrderMutation` - Approve order
   - `useCancelOrderMutation` - Cancel order

## Usage Example

```typescript
import {
  useGetProductsQuery,
  useCreateOrderMutation,
  Product,
  OrderCreationForm,
} from '@/modules/sales';

function ProductList() {
  const { data, isLoading } = useGetProductsQuery({
    filters: { statusId: 'ACTIVE' },
    pagination: { page: 0, size: 10 },
  });

  const [createOrder] = useCreateOrderMutation();

  const handleCreateOrder = async (orderData: OrderCreationForm) => {
    try {
      const result = await createOrder(orderData).unwrap();
      console.log('Order created:', result);
    } catch (error) {
      console.error('Failed to create order:', error);
    }
  };

  // ... component logic
}
```

## API Routes

All endpoints are automatically prefixed with `/sales/api/v1` by the base API configuration.

For example:

- `useGetProductsQuery` → `GET /sales/api/v1/product/search`
- `useCreateOrderMutation` → `POST /sales/api/v1/order/create`
- `useGetCustomerQuery(id)` → `GET /sales/api/v1/customer/search/{id}`

## Cache Tags

The module uses RTK Query's cache invalidation with the following tags:

- `SalesCustomer` - Customer data
- `Address` - Address data
- `Category` - Category data
- `Facility` - Facility data
- `InventoryItem` - Inventory item data
- `Product` - Product data
- `Order` - Order data

These tags ensure automatic cache invalidation when data is created, updated, or deleted.

## Type Safety

All API endpoints are fully typed with TypeScript, providing:

- Type-safe request/response handling
- Autocomplete support in IDE
- Compile-time error checking
- Consistent data structures

## Authors

QuanTuanHuy - Part of Serp Project
