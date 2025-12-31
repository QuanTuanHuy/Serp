/_
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Module API Documentation
_/

# Logistics Module API Endpoints

This document provides an overview of all available API endpoints in the Logistics module.

## Available Hooks

### Address Management

- `useCreateAddressMutation` - Create new address
- `useGetAddressesByEntityQuery` - Get addresses by entity ID
- `useUpdateAddressMutation` - Update address

### Category Management

- `useGetCategoriesQuery` - Get categories with filters and pagination
- `useGetCategoryQuery` - Get single category by ID
- `useCreateCategoryMutation` - Create new category
- `useUpdateCategoryMutation` - Update category
- `useDeleteCategoryMutation` - Delete category

### Customer Management (Read-only)

- `useGetCustomersQuery` - Get customers with filters and pagination
- `useGetCustomerQuery` - Get single customer by ID

### Facility Management

- `useGetFacilitiesQuery` - Get facilities with filters and pagination
- `useGetFacilityQuery` - Get single facility by ID
- `useCreateFacilityMutation` - Create new facility
- `useUpdateFacilityMutation` - Update facility
- `useDeleteFacilityMutation` - Delete facility

### Inventory Item Management

- `useGetInventoryItemsQuery` - Get inventory items with filters and pagination
- `useGetInventoryItemQuery` - Get single inventory item with details by ID
- `useCreateInventoryItemMutation` - Create new inventory item
- `useUpdateInventoryItemMutation` - Update inventory item
- `useDeleteInventoryItemMutation` - Delete inventory item

### Order Management (Read-only)

- `useGetOrdersQuery` - Get orders with filters and pagination
- `useGetOrderQuery` - Get single order by ID

### Product Management

- `useGetProductsQuery` - Get products with filters and pagination
- `useGetProductQuery` - Get single product by ID
- `useCreateProductMutation` - Create new product
- `useUpdateProductMutation` - Update product
- `useDeleteProductMutation` - Delete product

### Shipment Management

- `useGetShipmentsQuery` - Get shipments with filters and pagination
- `useGetShipmentQuery` - Get single shipment by ID
- `useCreateShipmentMutation` - Create new shipment
- `useUpdateShipmentMutation` - Update shipment
- `useDeleteShipmentMutation` - Delete shipment
- `useAddItemToShipmentMutation` - Add item to shipment
- `useUpdateItemInShipmentMutation` - Update shipment item
- `useDeleteItemFromShipmentMutation` - Delete item from shipment
- `useImportShipmentMutation` - Import shipment (process inbound shipment)

### Supplier Management (Read-only)

- `useGetSuppliersQuery` - Get suppliers with filters and pagination
- `useGetSupplierQuery` - Get single supplier by ID

## Usage Example

```typescript
import {
  useGetInventoryItemsQuery,
  useCreateShipmentMutation,
  useImportShipmentMutation,
} from '@/modules/logistics';

function InventoryManagement() {
  // Query data
  const { data, isLoading, error } = useGetInventoryItemsQuery({
    filters: { facilityId: '123', statusId: 'VALID' },
    pagination: { page: 0, size: 10 },
  });

  // Mutations
  const [createShipment] = useCreateShipmentMutation();
  const [importShipment] = useImportShipmentMutation();

  const handleCreateShipment = async (formData) => {
    try {
      const result = await createShipment(formData).unwrap();
      console.log('Shipment created:', result);
    } catch (error) {
      console.error('Failed to create shipment:', error);
    }
  };

  // ... component logic
}
```

## Store Selectors

All selectors are available from `@/modules/logistics/store`:

```typescript
import {
  selectInventoryItemItems,
  selectShipmentsByStatus,
  selectLowStockProducts,
  selectExpiringSoonInventoryItems,
} from '@/modules/logistics/store';
import { useAppSelector } from '@/lib/store';

function MyComponent() {
  const inventoryItems = useAppSelector(selectInventoryItemItems);
  const lowStock = useAppSelector(selectLowStockProducts);
  const expiringSoon = useAppSelector(selectExpiringSoonInventoryItems);

  // ... component logic
}
```

## Store Actions

All actions are available from `@/modules/logistics/store`:

```typescript
import {
  setInventoryItemFilters,
  setShipmentPagination,
  toggleShipmentSelection,
} from '@/modules/logistics/store';
import { useAppDispatch } from '@/lib/store';

function MyComponent() {
  const dispatch = useAppDispatch();

  const handleFilterChange = (filters) => {
    dispatch(setInventoryItemFilters(filters));
  };

  const handlePageChange = (page) => {
    dispatch(setShipmentPagination({ page }));
  };

  // ... component logic
}
```

## Filter Types

### InventoryItemFilters

```typescript
{
  query?: string;
  productId?: string;
  facilityId?: string;
  expirationDateFrom?: string;
  expirationDateTo?: string;
  manufacturingDateFrom?: string;
  manufacturingDateTo?: string;
  statusId?: InventoryItemStatus;
}
```

### ShipmentFilters

```typescript
{
  query?: string;
  statusId?: ShipmentStatus;
  shipmentTypeId?: ShipmentType;
  toCustomerId?: string;
  fromSupplierId?: string;
  orderId?: string;
}
```

### ProductFilters

```typescript
{
  query?: string;
  categoryId?: string;
  statusId?: ProductStatus;
}
```

### OrderFilters

```typescript
{
  query?: string;
  statusId?: string;
  toCustomerId?: string;
  fromSupplierId?: string;
  saleChannelId?: SaleChannel;
  orderDateAfter?: string;
  orderDateBefore?: string;
  deliveryAfter?: string;
  deliveryBefore?: string;
}
```

## Type Constants

```typescript
// Entity Types
type EntityType = 'PRODUCT' | 'SUPPLIER' | 'CUSTOMER' | 'FACILITY';

// Address Types
type AddressType = 'FACILIY' | 'SHIPPING' | 'BUSSINESS';

// Status Types
type FacilityStatus = 'ACTIVE' | 'INACTIVE';
type InventoryItemStatus = 'VALID' | 'EXPIRED' | 'DAMAGED';
type ProductStatus = 'ACTIVE' | 'INACTIVE';
type ShipmentStatus = 'CREATED' | 'IMPORTED' | 'EXPORTED';
type ShipmentType = 'INBOUND' | 'OUTBOUND';
type SupplierStatus = 'ACTIVE' | 'INACTIVE';
type OrderStatus = 'CREATED' | 'APPROVED' | 'CANCELLED' | 'FULLY_DELIVERED';
type OrderType = 'PURCHASE' | 'SALES';
```

## Notes

1. **Read-only resources**: Customer and Supplier endpoints are read-only from the logistics module perspective.
2. **Cache invalidation**: Shipment import automatically invalidates inventory item cache.
3. **Pagination**: All list endpoints support pagination with default `page: 0, size: 10`.
4. **Authentication**: All endpoints require valid JWT token from API Gateway.
5. **Service routing**: Requests automatically route to `localhost:8089/logistics/api/v1/*`.
