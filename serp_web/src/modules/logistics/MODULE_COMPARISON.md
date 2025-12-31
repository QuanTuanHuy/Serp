/_
Author: QuanTuanHuy
Description: Part of Serp Project - Module Comparison Guide
_/

# Logistics vs Sales Module Comparison

## 📊 Architecture Alignment

Both modules follow **EXACTLY** the same architectural pattern, ensuring consistency across the codebase.

### File Structure

```
Module Structure (Identical Pattern)
├── api/
│   └── {module}Api.ts          ✅ RTK Query endpoints
├── components/                  ✅ React components
├── pages/                       ✅ Page-level components
├── store/
│   ├── {module}Slice.ts        ✅ Redux slice
│   ├── selectors.ts            ✅ Memoized selectors
│   └── index.ts                ✅ Barrel exports
├── types/
│   └── index.ts                ✅ TypeScript definitions
├── index.ts                    ✅ Module exports
└── README*.md                  ✅ Documentation
```

## 🔄 Entity Mapping

| Logistics Entity | Sales Entity  | Shared? | Notes                                |
| ---------------- | ------------- | ------- | ------------------------------------ |
| InventoryItem    | InventoryItem | ✅      | Same entity, different perspective   |
| Shipment         | -             | ❌      | Logistics-specific                   |
| Product          | Product       | ✅      | Same entity                          |
| Facility         | Facility      | ✅      | Same entity                          |
| Category         | Category      | ✅      | Same entity                          |
| Order            | Order         | ✅      | Same entity (PURCHASE vs SALES type) |
| Supplier         | -             | ❌      | Logistics reads from Purchase        |
| Customer         | Customer      | ✅      | Sales creates, Logistics reads       |
| Address          | Address       | ✅      | Same entity                          |

## 🎯 API Endpoints Comparison

### CRUD Operations

| Operation    | Sales Customer                                | Logistics Shipment                            |
| ------------ | --------------------------------------------- | --------------------------------------------- |
| **Create**   | `useCreateCustomerMutation()`                 | `useCreateShipmentMutation()`                 |
| **Read**     | `useGetCustomerQuery(id)`                     | `useGetShipmentQuery(id)`                     |
| **Read All** | `useGetCustomersQuery({filters, pagination})` | `useGetShipmentsQuery({filters, pagination})` |
| **Update**   | `useUpdateCustomerMutation()`                 | `useUpdateShipmentMutation()`                 |
| **Delete**   | `useDeleteCustomerMutation()`                 | `useDeleteShipmentMutation()`                 |

### Naming Convention

Both modules follow: `use{Action}{Entity}{Mutation|Query}()`

Examples:

- Sales: `useCreateCustomerMutation`, `useGetOrdersQuery`
- Logistics: `useCreateShipmentMutation`, `useGetInventoryItemsQuery`

## 🗂️ Store Structure Comparison

### State Shape (Identical Pattern)

```typescript
// Sales State
interface SalesState {
  ui: SalesUIState; // UI preferences
  customers: CustomerState; // Entity state
  products: ProductState;
  orders: OrderState;
  // ... more entities
}

// Logistics State
interface LogisticsState {
  ui: LogisticsUIState; // UI preferences
  inventoryItems: InventoryItemState; // Entity state
  shipments: ShipmentState;
  products: ProductState;
  // ... more entities
}
```

### Entity State Shape (Identical)

```typescript
// Pattern used in BOTH modules
interface EntityState {
  items: Entity[]; // Data array
  selectedItems: string[]; // Selection for bulk actions
  loading: boolean; // Loading state
  error: string | null; // Error message
  filters: EntityFilters; // Current filters
  pagination: PaginationParams; // Pagination config
  total: number; // Total count
}
```

### Actions Comparison

| Action Type          | Sales Example                 | Logistics Example                  |
| -------------------- | ----------------------------- | ---------------------------------- |
| **Set Filters**      | `setCustomerFilters(filters)` | `setInventoryItemFilters(filters)` |
| **Set Pagination**   | `setOrderPagination({page})`  | `setShipmentPagination({page})`    |
| **Toggle Selection** | `toggleCustomerSelection(id)` | `toggleShipmentSelection(id)`      |
| **Clear Selection**  | `clearCustomerSelection()`    | `clearInventoryItemSelection()`    |
| **Reset State**      | `resetCustomerState()`        | `resetInventoryItemState()`        |
| **UI Toggle**        | `toggleSidebar()`             | `toggleSidebar()`                  |
| **Set View**         | `setViewMode('grid')`         | `setViewMode('grid')`              |

## 🔍 Selector Patterns

### Base Selectors (Identical Pattern)

```typescript
// Sales
export const selectCustomerItems = createSelector(
  selectCustomers,
  (customers) => customers.items
);

// Logistics
export const selectInventoryItemItems = createSelector(
  selectInventoryItems,
  (inventoryItems) => inventoryItems.items
);
```

### Computed Selectors (Similar Logic)

```typescript
// Sales - Orders by Customer
export const selectOrdersByCustomer = createSelector(
  [selectOrderItems, (state, customerId) => customerId],
  (orders, customerId) =>
    orders.filter((order) => order.toCustomerId === customerId)
);

// Logistics - Shipments by Order
export const selectShipmentsByOrder = createSelector(
  [selectShipmentItems, (state, orderId) => orderId],
  (shipments, orderId) =>
    shipments.filter((shipment) => shipment.orderId === orderId)
);
```

### Business Logic Selectors

| Sales                    | Logistics                             |
| ------------------------ | ------------------------------------- |
| `selectTotalOrderAmount` | `selectTotalInventoryValue`           |
| `selectOrdersByStatus`   | `selectShipmentsByStatus`             |
| -                        | `selectLowStockProducts` ⭐           |
| -                        | `selectExpiredInventoryItems` ⭐      |
| -                        | `selectExpiringSoonInventoryItems` ⭐ |

_⭐ = Logistics-specific business logic_

## 📝 Type Definitions

### Common Types (Shared)

```typescript
// Both modules use identical base types
type ResponseStatus = 'SUCCESS' | 'FAILED';
type EntityType = 'PRODUCT' | 'SUPPLIER' | 'CUSTOMER' | 'FACILITY';
type AddressType = 'FACILIY' | 'SHIPPING' | 'BUSSINESS';

interface PaginationParams {
  page;
  size;
  sortBy;
  sortDirection;
}
interface PaginatedResponse<T> {
  items;
  totalItems;
  totalPages;
  currentPage;
}
interface APIResponse<T> {
  code;
  message;
  status;
  data;
}
```

### Module-Specific Types

**Sales Specific:**

```typescript
type SaleChannel = 'ONLINE' | 'PARTNER' | 'RETAIL';
// Used for: Order.saleChannelId
```

**Logistics Specific:**

```typescript
type ShipmentType = 'INBOUND' | 'OUTBOUND';
type ShipmentStatus = 'CREATED' | 'IMPORTED' | 'EXPORTED';
// Used for: Shipment entity
```

### Form Types (Same Pattern)

```typescript
// Sales
interface CustomerCreationForm { name, phone, email, ... }
interface CustomerUpdateForm { name?, phone?, email?, ... }

// Logistics
interface ShipmentCreationForm { orderId, shipmentName, ... }
interface ShipmentUpdateForm { shipmentName?, note?, ... }
```

## 🔌 Integration Patterns

### 1. RTK Query Tags (Namespace Pattern)

```typescript
// Sales uses simple tags
tagTypes: ['Customer', 'Order', 'Product'];
providesTags: [{ type: 'Customer', id }];

// Logistics uses namespaced tags (to avoid conflicts)
tagTypes: ['logistics/Customer', 'logistics/Shipment'];
providesTags: [{ type: 'logistics/Customer', id }];
```

### 2. Service Routing (extraOptions)

```typescript
// Sales
extraOptions: {
  service: 'sales';
}
// Routes to: /sales/api/v1/*

// Logistics
extraOptions: {
  service: 'logistics';
}
// Routes to: /logistics/api/v1/*
```

### 3. Store Registration

```typescript
// store.ts - Both registered identically
const rootReducer = combineReducers({
  // ...
  sales: salesReducer,
  logistics: logisticsReducer,
});
```

## 🎨 UI State Management

Both modules use **identical** UI state pattern:

```typescript
interface UIState {
  activeModule: string; // Current active view
  selectedItems: string[]; // Globally selected items
  bulkActionMode: boolean; // Bulk action mode
  viewMode: 'list' | 'grid' | 'kanban'; // Display mode
  sidebarCollapsed: boolean; // Sidebar state
  filterPanelOpen: boolean; // Filter panel state
}
```

## 🚀 Usage Patterns

### Component Integration (Identical Approach)

```typescript
// Sales Component
function CustomerList() {
  const { data } = useGetCustomersQuery({ filters, pagination });
  const customers = useAppSelector(selectCustomerItems);
  const dispatch = useAppDispatch();

  const handleFilter = (filters) => {
    dispatch(setCustomerFilters(filters));
  };
}

// Logistics Component
function ShipmentList() {
  const { data } = useGetShipmentsQuery({ filters, pagination });
  const shipments = useAppSelector(selectShipmentItems);
  const dispatch = useAppDispatch();

  const handleFilter = (filters) => {
    dispatch(setShipmentFilters(filters));
  };
}
```

### Mutation Handling (Same Pattern)

```typescript
// Sales
const [createCustomer, { isLoading }] = useCreateCustomerMutation();

try {
  const result = await createCustomer(formData).unwrap();
  toast.success('Customer created');
} catch (error) {
  toast.error('Failed to create customer');
}

// Logistics
const [createShipment, { isLoading }] = useCreateShipmentMutation();

try {
  const result = await createShipment(formData).unwrap();
  toast.success('Shipment created');
} catch (error) {
  toast.error('Failed to create shipment');
}
```

## 📊 Metrics Comparison

| Metric               | Sales          | Logistics      |
| -------------------- | -------------- | -------------- |
| **API Endpoints**    | 35+ hooks      | 40+ hooks      |
| **Store Actions**    | 55+ actions    | 60+ actions    |
| **Selectors**        | 70+ selectors  | 80+ selectors  |
| **TypeScript Types** | 25+ interfaces | 30+ interfaces |
| **Entities Managed** | 7 entities     | 8 entities     |
| **Lines of Code**    | ~1,800 LOC     | ~2,000 LOC     |

## ✅ Consistency Checklist

Both modules implement:

- ✅ RTK Query for API calls
- ✅ Redux Toolkit for state management
- ✅ Memoized selectors with Reselect
- ✅ TypeScript strict mode
- ✅ Pagination support
- ✅ Filter management
- ✅ Selection/bulk actions
- ✅ Error handling
- ✅ Loading states
- ✅ Cache invalidation
- ✅ Optimistic updates support
- ✅ Barrel exports
- ✅ Comprehensive documentation

## 🎯 Key Takeaways

1. **Architecture Consistency**: Both modules follow identical patterns, making it easy to switch between them.

2. **Code Reusability**: Shared types and utilities are used across both modules.

3. **Scalability**: The pattern is proven and can be replicated for new modules (e.g., HR, Finance).

4. **Developer Experience**: Same patterns = easier onboarding and maintenance.

5. **Type Safety**: Full TypeScript coverage in both modules.

## 🔮 Future Modules

When creating new modules (e.g., HR, Finance, Manufacturing), follow this proven pattern:

```typescript
// Template for any new module
/{module}/
  api/{module}Api.ts        // RTK Query endpoints
  store/{module}Slice.ts    // Redux slice
  store/selectors.ts        // Selectors
  types/index.ts            // Types
  index.ts                  // Module exports
```

**Result**: Consistent, maintainable, scalable architecture across the entire application! 🎉
