# Purchase Module - Frontend Structure

## Overview

This document describes the frontend structure created for the Purchase Service in the Serp ERP system. The module follows Clean Architecture principles and uses RTK Query, Redux Toolkit, and TypeScript.

**Important:** This module uses a dedicated production API endpoint (`https://api.serp.texkis.com/purchase-service/api/v1`) instead of the default API Gateway base URL. This is configured in `services/purchaseApi.ts` and does not affect other modules.

## Created Files Structure

```
src/modules/purchase/
├── types/
│   ├── supplier.types.ts      # Supplier entity, DTOs, filters
│   ├── product.types.ts       # Product & Category types
│   ├── order.types.ts         # Order, OrderItem types
│   ├── facility.types.ts      # Facility types
│   ├── shipment.types.ts      # Shipment types
│   └── index.ts               # Barrel export
├── services/
│   ├── purchaseApi.ts         # RTK Query API endpoints
│   └── index.ts               # Barrel export
├── components/
│   ├── layout/
│   │   ├── PurchaseLayout.tsx # Main layout with sidebar
│   │   └── PurchaseHeader.tsx # Header with navigation
│   ├── products/
│   │   ├── ProductForm.tsx    # Product form component
│   │   └── ProductFormDialog.tsx # Product dialog wrapper
│   ├── PurchaseAuthGuard.tsx  # Auth guard for module
│   └── index.ts               # Barrel export
├── store/
│   ├── suppliers/
│   │   └── suppliersSlice.ts  # Suppliers UI state
│   ├── products/
│   │   └── productsSlice.ts   # Products UI state
│   ├── orders/
│   │   └── ordersSlice.ts     # Orders UI state
│   └── index.ts               # Combined reducers
├── hooks/
│   ├── useSuppliers.ts        # Suppliers business logic
│   ├── useProducts.ts         # Products business logic
│   ├── useOrders.ts           # Orders business logic
│   └── index.ts               # Barrel export
├── components/                # (To be created later)
└── index.ts                   # Module barrel export
```

## Features Implemented

### 1. Type Definitions (`types/`)

All TypeScript types for the purchase module including:

**Suppliers:**

- `Supplier` - Main supplier entity
- `SupplierDetail` - Supplier with address information
- `CreateSupplierRequest` / `UpdateSupplierRequest` - DTOs
- `SupplierFilters` - Search and filter params
- `Address` - Address entity

**Products:**

- `Product` - Main product entity
- `Category` - Product category
- `CreateProductRequest` / `UpdateProductRequest` - DTOs
- `ProductFilters` - Search and filter params

**Orders:**

- `Order` - Main order entity
- `OrderItem` - Order line items
- `OrderDetail` - Order with items
- `CreateOrderRequest` / `UpdateOrderRequest` - DTOs
- `AddOrderItemRequest` / `UpdateOrderItemRequest` - Item DTOs
- `CancelOrderRequest` - Cancellation DTO
- `OrderFilters` - Search and filter params
- `OrderStatus` - Status enum

**Facilities:**

- `Facility` - Warehouse/facility entity
- `FacilityDetail` - Facility with address
- `CreateFacilityRequest` / `UpdateFacilityRequest` - DTOs
- `FacilityFilters` - Search and filter params
- `FacilityStatus` - Status enum

**Shipments:**

- `Shipment` - Shipment entity
- `ShipmentItem` - Shipment line items
- `ShipmentDetail` - Shipment with items
- `CreateShipmentRequest` / `UpdateShipmentRequest` - DTOs
- `ShipmentFilters` - Search and filter params
- `ShipmentStatus` - Status enum

### 2. RTK Query API Service (`services/purchaseApi.ts`)

**API Configuration:**

```typescript
const PURCHASE_API_BASE_URL =
  'https://api.serp.texkis.com/purchase-service/api/v1';
```

This module uses a dedicated production API endpoint instead of the default API Gateway base URL configured in `apiSlice.ts`. All endpoints use `${PURCHASE_API_BASE_URL}/...` for full URL construction, ensuring requests go directly to the purchase service.

**Why separate base URL?**

- Direct access to purchase service in production environment
- Independent deployment and scaling
- Does not affect other modules (CRM, PTM, Admin) which continue using API Gateway

Complete API endpoints for all purchase entities:

**Supplier Endpoints:**

- `getSuppliers` - Paginated list with filters
- `getSupplierById` - Supplier detail with address
- `createSupplier` - Create new supplier
- `updateSupplier` - Update supplier
- `deleteSupplier` - Delete supplier

**Product Endpoints:**

- `getProducts` - Paginated list with filters
- `getProductById` - Product detail
- `createProduct` - Create new product
- `updateProduct` - Update product
- `deleteProduct` - Delete product

**Category Endpoints:**

- `getCategories` - List all categories
- `createCategory` - Create new category

**Order Endpoints:**

- `getOrders` - Paginated list with filters
- `getOrderById` - Order detail with items
- `createOrder` - Create new order
- `updateOrder` - Update order
- `approveOrder` - Approve order
- `cancelOrder` - Cancel order
- `addProductToOrder` - Add item to order
- `updateOrderItem` - Update order item
- `deleteOrderItem` - Remove item from order

**Facility Endpoints:**

- `getFacilities` - Paginated list with filters
- `getFacilityById` - Facility detail
- `createFacility` - Create new facility
- `updateFacility` - Update facility
- `deleteFacility` - Delete facility

**Shipment Endpoints:**

- `getShipments` - Paginated list with filters
- `getShipmentById` - Shipment detail with items
- `createShipment` - Create new shipment
- `updateShipment` - Update shipment
- `deleteShipment` - Delete shipment

### 3. Redux Slices (`store/`)

UI state management for each entity with filters, pagination, dialog states:

**Suppliers Slice:**

- Filters: query, statusId, pagination, sorting
- Dialog: open/close, create/edit/view modes
- Selected supplier ID
- View mode (list/grid)

**Products Slice:**

- Filters: query, categoryId, statusId, pagination, sorting
- Dialog: open/close, create/edit/view modes
- Selected product ID
- View mode (list/grid - default: grid)

**Orders Slice:**

- Filters: query, statusId, orderTypeId, supplierId, customerId, dateRange, pagination, sorting
- Dialog: open/close, create/edit/view modes
- Selected order ID
- View mode (list/grid)

### 4. Custom Hooks (`hooks/`)

Business logic hooks that combine RTK Query and Redux state:

**useSuppliers:**

- Data: suppliers list, selected supplier, pagination
- Loading states: isLoading, isFetching, isCreating, isUpdating, isDeleting
- Filter handlers: query, status, page, pageSize, sorting
- Dialog handlers: open create/edit, close
- CRUD handlers: create, update, delete
- Utilities: refetch, error handling

**useProducts:**

- Data: products list, selected product, categories, pagination
- Loading states: isLoading, isFetching, isCreating, isUpdating, isDeleting, isCategoriesLoading
- Filter handlers: query, category, status, page, pageSize, sorting
- Dialog handlers: open create/edit, close
- CRUD handlers: create, update, delete
- Utilities: refetch, error handling

**useOrders:**

- Data: orders list, selected order (with items), pagination
- Loading states: isLoading, isFetching, isCreating, isUpdating, isApproving, isCancelling, isAddingProduct, isUpdatingItem, isDeletingItem
- Filter handlers: query, status, orderType, supplier, customer, dateRange, page, pageSize, sorting
- Dialog handlers: open create/edit, close
- CRUD handlers: create, update, approve, cancel, add/update/delete items
- Utilities: refetch, error handling

### 5. Layout Components (`components/layout/`)

**PurchaseLayout:**

- Main layout wrapper for all Purchase pages
- Integrates `DynamicSidebar` with `PURCHASE` module code
- Responsive design with sidebar collapse functionality
- Uses `PurchaseAuthGuard` for role-based access control
- Wraps content with `RouteGuard` for permission checking
- Provides consistent header and content structure

**PurchaseHeader:**

- Fixed header with auto-hide on scroll down
- Breadcrumb navigation showing current location
- Global search bar for products, orders, suppliers
- Quick action buttons (New Product, New Order)
- Notifications indicator with count badge
- Theme toggle (light/dark mode)
- User menu with profile, settings, logout
- Responsive design for mobile and desktop

**PurchaseAuthGuard:**

- Role-based access control using `RoleGuard`
- Allowed roles: `PURCHASE_MANAGER`, `PURCHASE_OFFICER`, `SUPER_ADMIN`, `SYSTEM_MODERATOR`
- Custom access denied UI with helpful messaging
- Redirects unauthorized users
- Loading state during permission verification

### 6. Product Components (`components/products/`)

**ProductForm:**

- Comprehensive form with React Hook Form + Zod validation
- Fields: name, SKU, description, category, unit, price, quantity, supplier, status
- Real-time validation with error messages
- Category dropdown populated from API
- Status selection (ACTIVE/INACTIVE)
- Submit and cancel actions

**ProductFormDialog:**

- Dialog wrapper for ProductForm
- Modes: create/edit/view
- Handles form submission and API calls
- Loading states and error handling
- Responsive modal design

## Integration Points

### Updated Files

1. **`src/lib/store/api/apiSlice.ts`**
   - Added purchase tag types for cache invalidation:
     - `purchase/Supplier`
     - `purchase/Product`
     - `purchase/Category`
     - `purchase/Order`
     - `purchase/Facility`
     - `purchase/Shipment`

2. **`src/lib/store/store.ts`**
   - Imported `purchaseReducer`
   - Added to root reducer: `purchase: purchaseReducer`

## Backend API Mapping

The frontend is mapped to the following backend endpoints:

**Base URL:** `/api/v1/purchase-service`

**Suppliers:**

- `GET /supplier/search` - List suppliers
- `GET /supplier/search/{supplierId}` - Get supplier detail
- `POST /supplier/create` - Create supplier
- `PATCH /supplier/update/{supplierId}` - Update supplier
- `DELETE /supplier/delete/{supplierId}` - Delete supplier

**Products:**

- `GET /product/search` - List products
- `GET /product/search/{productId}` - Get product detail
- `POST /product/create` - Create product
- `PATCH /product/update/{productId}` - Update product
- `DELETE /product/delete/{productId}` - Delete product

**Orders:**

- `GET /order/search` - List orders
- `GET /order/search/{orderId}` - Get order detail
- `POST /order/create` - Create order
- `PATCH /order/update/{orderId}` - Update order
- `PATCH /order/manage/{orderId}/approve` - Approve order
- `PATCH /order/manage/{orderId}/cancel` - Cancel order
- `POST /order/create/{orderId}/add` - Add product to order
- `PATCH /order/update/{orderId}/update/{orderItemId}` - Update order item
- `PATCH /order/update/{orderId}/delete/{orderItemId}` - Delete order item

## Next Steps

### Components to Create

1. **Supplier Components:**
   - `SupplierForm.tsx` - Form with validation (name, email, phone, status, address)
   - `SupplierFormDialog.tsx` - Dialog wrapper
   - `SupplierCard.tsx` - Display card
   - `SupplierList.tsx` - List view with DataTable

2. **Product Components:**
   - `ProductForm.tsx` - Form with validation (name, prices, category, unit, dimensions)
   - `ProductFormDialog.tsx` - Dialog wrapper
   - `ProductCard.tsx` - Display card with image
   - `ProductGrid.tsx` - Grid view
   - `ProductList.tsx` - List view with DataTable

3. **Order Components:**
   - `OrderForm.tsx` - Form with order details and items
   - `OrderFormDialog.tsx` - Dialog wrapper
   - `OrderDetail.tsx` - Order detail view with items table
   - `OrderList.tsx` - List view with DataTable
   - `OrderItemForm.tsx` - Add/edit order items
   - `OrderStatusBadge.tsx` - Status indicator

4. **Facility Components:**
   - `FacilityForm.tsx` - Form with validation
   - `FacilityFormDialog.tsx` - Dialog wrapper
   - `FacilityCard.tsx` - Display card
   - `FacilityList.tsx` - List view

5. **Shipment Components:**
   - `ShipmentForm.tsx` - Form with shipment details
   - `ShipmentFormDialog.tsx` - Dialog wrapper
   - `ShipmentDetail.tsx` - Shipment detail view
   - `ShipmentList.tsx` - List view with DataTable
   - `ShipmentStatusBadge.tsx` - Status indicator

### Pages to Create

1. **`src/app/purchase/suppliers/page.tsx`**
   - Suppliers listing page with search, filters, create button
   - DataTable with columns: name, email, phone, status, actions

2. **`src/app/purchase/products/page.tsx`**
   - Products listing page with search, category filter, view toggle (grid/list)
   - Grid view with product cards
   - List view with DataTable

3. **`src/app/purchase/orders/page.tsx`**
   - Orders listing page with search, filters (status, type, supplier, customer, date range)
   - DataTable with columns: order ID, date, supplier/customer, total, status, actions

4. **`src/app/purchase/orders/[orderId]/page.tsx`**
   - Order detail page with items, actions (approve, cancel, add items)

5. **`src/app/purchase/facilities/page.tsx`**
   - Facilities listing page

6. **`src/app/purchase/shipments/page.tsx`**
   - Shipments listing page

7. **`src/app/purchase/layout.tsx`**
   - Purchase module layout with navigation sidebar

## Usage Examples

### Using in a Component

```typescript
'use client';

import { useSuppliers } from '@/modules/purchase';

export default function SuppliersPage() {
  const {
    suppliers,
    pagination,
    isLoading,
    filters,
    handleQueryChange,
    handleStatusChange,
    handlePageChange,
    handleOpenCreateDialog,
    handleOpenEditDialog,
    handleDeleteSupplier,
    dialogOpen,
    dialogMode,
    handleCloseDialog,
    handleCreateSupplier,
    handleUpdateSupplier,
    selectedSupplier,
  } = useSuppliers();

  // Use the data and handlers in your component
  return (
    <div>
      {/* Search and filters */}
      <input
        value={filters.query || ''}
        onChange={(e) => handleQueryChange(e.target.value)}
        placeholder="Search suppliers..."
      />

      {/* Suppliers list */}
      {isLoading ? (
        <div>Loading...</div>
      ) : (
        <ul>
          {suppliers.map((supplier) => (
            <li key={supplier.id}>
              {supplier.name}
              <button onClick={() => handleOpenEditDialog(supplier.id)}>
                Edit
              </button>
              <button onClick={() => handleDeleteSupplier(supplier.id)}>
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}

      {/* Pagination */}
      {pagination && (
        <div>
          Page {pagination.currentPage} of {pagination.totalPages}
          <button onClick={() => handlePageChange(pagination.currentPage - 1)}>
            Previous
          </button>
          <button onClick={() => handlePageChange(pagination.currentPage + 1)}>
            Next
          </button>
        </div>
      )}

      {/* Create/Edit Dialog */}
      {dialogOpen && (
        <SupplierFormDialog
          open={dialogOpen}
          onOpenChange={handleCloseDialog}
          supplier={dialogMode === 'edit' ? selectedSupplier : undefined}
          onSubmit={
            dialogMode === 'create'
              ? handleCreateSupplier
              : (data) =>
                  handleUpdateSupplier(selectedSupplier!.id, data)
          }
        />
      )}
    </div>
  );
}
```

## Notes

- All API calls include proper error handling with toast notifications
- Redux state persists filter selections across navigation
- RTK Query handles caching and automatic refetching
- All mutations invalidate relevant cache tags for automatic updates
- TypeScript ensures type safety across the entire data flow
- The module is fully isolated and follows the existing patterns in the codebase

## Environment Variables

Ensure `.env` file has:

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## Testing

After creating components and pages:

1. Start backend services: `docker-compose -f docker-compose.dev.yml up -d`
2. Start purchase service: `cd purchase_service && ./run-dev.sh`
3. Start frontend: `cd serp_web && npm run dev`
4. Navigate to `http://localhost:3000/purchase/*` to test features
