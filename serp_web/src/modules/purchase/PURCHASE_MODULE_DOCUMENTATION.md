# Purchase Module - Frontend Technical Documentation

**Version:** 1.0  
**Last Updated:** January 16, 2026  
**Author:** KienPT
**Project:** SERP - Smart ERP System

---

## Table of Contents

1. [Module Overview](#module-overview)
2. [Component Architecture](#component-architecture)
3. [State Management & Data Flow](#state-management--data-flow)
4. [User Interaction Flows](#user-interaction-flows)
5. [Form Validation & Business Rules](#form-validation--business-rules)
6. [API Integration](#api-integration)
7. [Shared Components Usage](#shared-components-usage)
8. [Type Definitions](#type-definitions)

---

## Module Overview

### Purpose

The Purchase module manages the complete procurement lifecycle within the SERP ERP system. It handles supplier relationships, product catalogs, purchase order management, warehouse facilities, and inbound shipment tracking.

### Key User Actions

| Action                    | Description                                                      | User Role              |
| ------------------------- | ---------------------------------------------------------------- | ---------------------- |
| **Create Purchase Order** | Create new purchase orders with multiple products from suppliers | Purchase Manager       |
| **Approve Order**         | Approve pending purchase orders to proceed with procurement      | Purchase Manager/Admin |
| **Cancel Order**          | Cancel orders with mandatory cancellation notes                  | Purchase Manager       |
| **Manage Suppliers**      | CRUD operations on supplier information and addresses            | Purchase Manager       |
| **Manage Products**       | Maintain product catalog with pricing, categories, and inventory | Inventory Manager      |
| **Track Shipments**       | Monitor inbound shipments linked to purchase orders              | Warehouse Manager      |
| **Manage Facilities**     | Oversee warehouse and storage facility information               | Facility Manager       |
| **Filter & Search**       | Advanced filtering by status, supplier, date ranges              | All Users              |
| **View Analytics**        | Dashboard with order statistics and total values                 | Manager/Admin          |

### Module Structure

```
purchase/
├── api/
│   └── purchaseApi.ts                 # RTK Query endpoints
├── components/
│   ├── forms/
│   │   └── OrderForm.tsx             # Order creation/edit form
│   ├── layout/
│   │   ├── PurchaseHeader.tsx        # Navigation header
│   │   └── PurchaseLayout.tsx        # Sidebar layout wrapper
│   └── PurchaseAuthGuard.tsx         # Route protection
├── pages/
│   └── purchase-orders/
│       ├── OrderListPage.tsx         # Order grid/list view
│       ├── OrderDetailPage.tsx       # Order details & item management
│       ├── CreateOrderPage.tsx       # Create order wrapper
│       └── EditOrderPage.tsx         # Edit order wrapper
├── store/
│   ├── purchaseSlice.ts              # Redux state management
│   └── selectors.ts                  # State selectors
├── types/
│   └── index.ts                      # TypeScript interfaces
└── index.ts                          # Module exports
```

---

## Component Architecture

### Visual Component Hierarchy

```
PurchaseLayout (Root Container)
│
├── PurchaseHeader (Navigation)
│   ├── Logo & Module Title
│   └── Navigation Links (Dashboard, Orders, Suppliers, Products, etc.)
│
└── Page Container
    │
    ├── OrderListPage (Main List View)
    │   ├── Page Header (Title + Create Button)
    │   ├── StatsCard × 4 (Quick Analytics)
    │   │   ├── Total Orders
    │   │   ├── Approved Orders
    │   │   ├── Delivered Orders
    │   │   └── Total Value
    │   ├── Search & Filter Bar
    │   │   ├── Search Input (with debounce)
    │   │   ├── Filter Toggle Button
    │   │   └── View Mode Toggle (Grid/List)
    │   ├── Expanded Filters (Collapsible)
    │   │   └── Status Dropdown
    │   ├── OrderCard × N (Grid/List Items)
    │   │   ├── Status Badge
    │   │   ├── Order Info (Date, Supplier, Priority)
    │   │   └── Total Amount
    │   └── Pagination Controls
    │
    ├── OrderDetailPage (Detail View)
    │   ├── Back Button + Header
    │   ├── Status Badge & Action Buttons
    │   │   ├── Approve Order (if CREATED)
    │   │   ├── Cancel Order (if not CANCELLED)
    │   │   ├── Edit Order
    │   │   └── Delete Order
    │   ├── Tabs Navigation
    │   │   ├── Overview Tab
    │   │   │   ├── Order Information Card
    │   │   │   │   ├── Order Details (Name, Priority, Channel)
    │   │   │   │   └── Dates (Order, Delivery)
    │   │   │   ├── Supplier Information Card
    │   │   │   │   ├── Supplier Details
    │   │   │   │   └── Contact Info (Email, Phone, Address)
    │   │   │   └── Creator/Approver Information Card
    │   │   ├── Items Tab
    │   │   │   ├── Add Product Button
    │   │   │   └── OrderItem × N
    │   │   │       ├── Product Info
    │   │   │       ├── Quantity/Tax/Discount
    │   │   │       └── Edit/Delete Actions
    │   │   └── Shipments Tab
    │   │       └── ShipmentCard × N
    │   │           ├── Shipment Details
    │   │           └── Inventory Items
    │   └── Sidebar Summary
    │       ├── Order Statistics
    │       └── Financial Summary
    │
    └── OrderForm (Create/Edit Form)
        ├── Form Header
        ├── Main Form Section
        │   ├── Basic Information Card
        │   │   ├── Supplier Selection (Searchable Dropdown)
        │   │   ├── Order Name Input
        │   │   ├── Sale Channel Dropdown
        │   │   ├── Priority Selector (1-10)
        │   │   ├── Delivery Date Pickers
        │   │   └── Notes Textarea
        │   └── Order Items Card (Create Mode Only)
        │       ├── Product Search (Autocomplete)
        │       └── OrderItemForm × N
        │           ├── Product Display
        │           ├── Quantity Input
        │           ├── Tax Input (%)
        │           ├── Discount Input (đ)
        │           └── Remove Button
        └── Sidebar Summary
            ├── Preview Information
            └── Submit/Cancel Buttons
```

### Critical Component Details

#### 1. **OrderListPage.tsx** (Main List View)

- **Responsibility:** Displays paginated list of purchase orders with filtering and search capabilities
- **Key Features:**
  - Real-time search with Enter key support
  - Status-based filtering (CREATED, APPROVED, CANCELLED, FULLY_DELIVERED)
  - Toggle between Grid and List view modes
  - Responsive stats cards showing aggregated metrics
  - Click-through navigation to order details
- **State Management:** Local state for UI controls (search, filters, pagination, view mode)
- **Data Fetching:** Triggers `useGetOrdersQuery` on mount and filter changes
- **Performance:** Uses `useMemo` for supplier mapping and stats calculation

#### 2. **OrderForm.tsx** (Create/Edit Form)

- **Responsibility:** Handles order creation and editing with dynamic product line items
- **Key Features:**
  - Pre-fills supplier if passed as prop (from supplier page)
  - Autocomplete search for suppliers and products
  - Dynamic item list with add/remove/update functionality
  - Real-time validation with error messages
  - Separate UI for create vs. edit mode (items not editable in edit mode)
  - Preview sidebar with order summary
- **State Management:**
  - Local form state with React `useState`
  - Validation errors stored separately
  - Submission loading state
- **Validation:** Client-side validation before submission (see [Validation Rules](#form-validation--business-rules))
- **Data Flow:** Calls `onSubmit` prop with `OrderCreationForm` or `OrderUpdateForm`

#### 3. **OrderDetailPage.tsx** (Detail View)

- **Responsibility:** Comprehensive order details with inline item management and actions
- **Key Features:**
  - Tabbed interface (Overview, Items, Shipments)
  - Inline product item editing (quantity, tax, discount)
  - Order status actions (Approve, Cancel, Delete)
  - Linked supplier and user profile information
  - Shipment tracking display
  - Cancellation dialog with mandatory note
- **State Management:** Local state for dialogs, selected items, and form inputs
- **Data Fetching:**
  - `useGetOrderQuery(orderId)` - Order data
  - `useGetSupplierQuery(supplierId)` - Supplier info
  - `useGetShipmentsByOrderQuery(orderId)` - Related shipments
  - `useGetUsersQuery` - User profiles (creator, approver)
- **Mutations:**
  - `useApproveOrderMutation` - Approve order
  - `useCancelOrderMutation` - Cancel order with note
  - `useAddProductToOrderMutation` - Add product to order
  - `useUpdateProductInOrderMutation` - Update order item
  - `useDeleteProductFromOrderMutation` - Remove order item
  - `useDeleteOrderMutation` - Delete entire order

#### 4. **StatsCard Component** (Analytics Widget)

- **Responsibility:** Displays key metrics with visual styling variants
- **Props:**
  - `title`: Metric label
  - `value`: Number or formatted string
  - `icon`: Lucide icon component
  - `variant`: 'default' | 'primary' | 'success' | 'warning' | 'danger'
- **Features:**
  - Gradient backgrounds based on variant
  - Icon with shadow and ring effects
  - Hover animations and transitions
  - SVG pattern background overlay

#### 5. **OrderCard Component** (List Item)

- **Responsibility:** Compact order display for grid/list views
- **Visual Elements:**
  - Gradient accent bar at top
  - Status badge with icon
  - Order info (date, supplier, priority, delivery date)
  - Total amount in prominent display
  - "View Details" button on hover
- **Interaction:** Click anywhere on card to navigate to detail page

---

## State Management & Data Flow

### Global State (Redux)

**Location:** `src/modules/purchase/store/purchaseSlice.ts`

**State Structure:**

```typescript
interface PurchaseState {
  // Selected entities (for detail views, not currently used in list views)
  selectedSupplier: Supplier | null;
  selectedProduct: Product | null;
  selectedOrder: Order | null;
  selectedCategory: Category | null;
  selectedFacility: Facility | null;

  // Persistent filters (preserved across navigation)
  filters: {
    suppliers: { query?: string; statusId?: string };
    products: { query?: string; categoryId?: string; statusId?: string };
    orders: {
      query?: string;
      statusId?: string;
      orderTypeId?: string;
      fromSupplierId?: string;
      fromDate?: string;
      toDate?: string;
    };
    categories: { query?: string };
    facilities: { query?: string; statusId?: string };
  };
}
```

**Actions:**

- `setSelectedSupplier/Product/Order/Category/Facility` - Store selected entity
- `setSupplier/Product/Order/Category/FacilityFilters` - Update filter state
- `clearFilters` - Reset all filters
- `resetPurchaseState` - Reset entire module state

**Usage Pattern:**

```typescript
// In components
import { useDispatch, useSelector } from 'react-redux';
import { setOrderFilters, selectOrderFilters } from '@/modules/purchase';

const filters = useSelector(selectOrderFilters);
const dispatch = useDispatch();

dispatch(setOrderFilters({ statusId: 'APPROVED' }));
```

### Local State (React State)

**Used for:**

- **UI Controls:** Search queries, dropdown visibility, dialog states, view modes
- **Form Inputs:** Order form fields, validation errors, submission states
- **Pagination:** Current page, page size
- **Temporary Data:** Search results, autocomplete suggestions

**Example (OrderListPage):**

```typescript
const [searchQuery, setSearchQuery] = useState('');
const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>('');
const [showFilters, setShowFilters] = useState(false);
const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
const [pagination, setPagination] = useState({ page: 0, size: 10 });
```

### Data Fetching (RTK Query)

**API Configuration:**

- **Base URL:** Configured in `@/lib/store/api` with `extraOptions: { service: 'purchase-service' }`
- **Routes to:** `/purchase-service/api/v1/*` via API Gateway
- **Authentication:** Automatic JWT injection from auth slice

**Query Pattern:**

```typescript
const { data, isLoading, error, refetch } = useGetOrdersQuery({
  filters: { query: searchQuery, statusId: statusFilter },
  pagination: { page: 0, size: 10 }
});

// Response shape
data = {
  code: 200,
  message: "Success",
  status: "SUCCESS",
  data: {
    items: Order[],
    totalItems: number,
    totalPages: number,
    currentPage: number
  }
}
```

**Cache Management:**

- **Provided Tags:** `providesTags` marks cached data with identifiers
  - List tags: `{ type: 'PurchaseOrder', id: 'LIST' }`
  - Individual tags: `{ type: 'PurchaseOrder', id: orderId }`
- **Invalidated Tags:** Mutations invalidate relevant cache entries
  - Creating order: Invalidates `'LIST'` tag → Refetches all order lists
  - Updating order: Invalidates specific `id` + `'LIST'` → Refetches that order + lists
  - Deleting order: Same as updating

**Example Cache Invalidation:**

```typescript
// After approving an order
useApproveOrderMutation({
  invalidatesTags: (result, error, orderId) => [
    { type: 'PurchaseOrder', id: orderId }, // Refetch this order
    { type: 'PurchaseOrder', id: 'LIST' }, // Refetch all order lists
  ],
});
```

### Data Flow Diagram

```
┌─────────────────┐
│   User Action   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│  Component Event        │
│  (onClick, onChange)    │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐      ┌──────────────────┐
│  Update Local State     │◄─────│  Form Validation │
│  (useState, useForm)    │      └──────────────────┘
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Dispatch Redux Action  │ (Optional, for persistent filters)
│  or Trigger Mutation    │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  RTK Query Hook         │
│  (useCreateOrderMutation)│
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  HTTP Request           │
│  POST /purchase-service/│
│      api/v1/order/create│
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Backend Response       │
│  { code, message, data }│
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  RTK Query Cache Update │
│  Invalidate Tags        │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Auto Refetch Queries   │
│  (Orders List, Detail)  │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Component Re-render    │
│  with New Data          │
└─────────────────────────┘
```

---

## User Interaction Flows

### Flow 1: Creating a New Purchase Order

**Starting Point:** User clicks "Tạo đơn hàng mới" button on Order List Page

1. **Navigation:** Router navigates to `/purchase/purchase-orders/new`
   - Renders `CreateOrderPage` component
   - Wraps `OrderForm` component

2. **Initial State:**
   - Form renders in "Create Mode" (`isEditMode = false`)
   - All fields empty except:
     - `saleChannel` defaults to `'ONLINE'`
     - `priority` defaults to `1`
     - `items` array is `[]`

3. **Supplier Selection:**
   - User types in "Nhà cung cấp" search field
   - **Trigger:** `onChange` event → Updates `supplierSearch` state
   - **API Call:** `useGetSuppliersQuery` with `{ query: supplierSearch }`
   - **Response:** Dropdown shows matching suppliers
   - User clicks a supplier → Sets `supplierId`, clears search, hides dropdown

4. **Form Filling:**
   - User fills optional fields (Order Name, Priority, Dates, Note)
   - Sale Channel dropdown selection
   - Delivery date validation happens on blur/submit

5. **Adding Products:**
   - User types in "Tìm kiếm và thêm sản phẩm" field
   - **API Call:** `useGetProductsQuery` with `{ query: productSearch }`
   - User clicks product from dropdown
   - **Action:** `addItem(productId)` function:
     ```typescript
     const maxSeqId = Math.max(0, ...items.map((i) => i.orderItemSeqId));
     setItems([
       ...items,
       {
         productId,
         orderItemSeqId: maxSeqId + 1,
         quantity: 1,
         tax: 0,
         discount: 0,
       },
     ]);
     ```
   - Product appears in "Danh sách sản phẩm" section
   - User adjusts quantity, tax, discount for each item

6. **Validation (Client-Side):**
   - User clicks "Tạo đơn hàng" button
   - **Validation Checks:**
     - `supplierId` must be selected → Error: "Nhà cung cấp là bắt buộc"
     - `saleChannel` must be selected → Error: "Kênh bán hàng là bắt buộc"
     - `items.length > 0` → Error: "Phải có ít nhất một sản phẩm"
     - If both dates exist: `deliveryBeforeDate >= deliveryAfterDate`
   - **If Errors:** Display error messages below fields, return early

7. **Submission:**
   - Set `isSubmitting = true` (disables button, shows spinner)
   - Construct `OrderCreationForm`:
     ```typescript
     {
       fromSupplierId: supplierId,
       orderName: orderName || undefined,
       saleChannelId: saleChannel,
       priority,
       deliveryBeforeDate: deliveryBeforeDate || undefined,
       deliveryAfterDate: deliveryAfterDate || undefined,
       note: note || undefined,
       items: items // Array of OrderItemForm
     }
     ```
   - **API Call:** `useCreateOrderMutation`
     - **Endpoint:** `POST /purchase-service/api/v1/order/create`
     - **Body:** OrderCreationForm
8. **Response Handling:**
   - **Success (200):**
     - Show success toast: "Đơn hàng đã được tạo thành công"
     - RTK Query invalidates `{ type: 'PurchaseOrder', id: 'LIST' }`
     - Navigate to order detail page: `/purchase/purchase-orders/${orderId}`
   - **Error (4xx/5xx):**
     - Show error toast: `error.data.message` or fallback
     - Set `isSubmitting = false`
     - User can retry

### Flow 2: Approving a Purchase Order

**Starting Point:** User is viewing an order in `CREATED` status on Order Detail Page

1. **UI Check:**
   - "Approve" button visible only if `order.statusId === 'CREATED'`
   - Button shows: "Phê duyệt đơn hàng" with `CheckCircle2` icon

2. **User Clicks "Phê duyệt đơn hàng":**
   - **API Call:** `useApproveOrderMutation`
     - **Endpoint:** `PATCH /purchase-service/api/v1/order/manage/${orderId}/approve`
     - **No Body Required**
3. **Loading State:**
   - Button shows loading spinner
   - Button disabled during request

4. **Response:**
   - **Success:**
     - Toast: "Đơn hàng đã được phê duyệt"
     - RTK Query cache updated:
       - Invalidates `{ type: 'PurchaseOrder', id: orderId }`
       - Invalidates `{ type: 'PurchaseOrder', id: 'LIST' }`
     - **Auto Refetch:** Order detail page re-fetches order data
     - Status badge updates to "Đã phê duyệt" (green with `CheckCircle2`)
     - "Approve" button disappears, "Cancel" button remains visible
   - **Error:**
     - Toast: Error message from backend
     - Order remains in `CREATED` status

### Flow 3: Adding a Product to an Existing Order

**Starting Point:** User viewing order Items tab on Order Detail Page

1. **User Clicks "Thêm sản phẩm" Button:**
   - Opens `AddProductDialog` modal
   - Modal contains:
     - Product search autocomplete
     - Quantity input (default: 1)
     - Tax input (default: 0)
     - Discount input (default: 0)

2. **Product Search:**
   - User types in search field
   - **API Call:** `useGetProductsQuery({ query: searchTerm })`
   - Dropdown shows matching products with SKU and price

3. **User Selects Product:**
   - Product ID stored in local state
   - Product details displayed in modal
   - User adjusts quantity/tax/discount

4. **User Clicks "Thêm":**
   - **Validation:** Product selected, quantity > 0
   - **API Call:** `useAddProductToOrderMutation`
     - **Endpoint:** `POST /purchase-service/api/v1/order/create/${orderId}/add`
     - **Body:**
       ```typescript
       {
         productId: string,
         orderItemSeqId: number, // Auto-incremented
         quantity: number,
         tax?: number,
         discount?: number
       }
       ```

5. **Response:**
   - **Success:**
     - Toast: "Sản phẩm đã được thêm vào đơn hàng"
     - RTK Query invalidates `{ type: 'PurchaseOrder', id: orderId }`
     - Order detail refetches → New product appears in items list
     - Modal closes automatically
   - **Error:**
     - Toast: Error message
     - Modal remains open for retry

### Flow 4: Canceling a Purchase Order

**Starting Point:** Order Detail Page with order not in `CANCELLED` status

1. **User Clicks "Hủy đơn hàng" Button:**
   - Opens cancellation confirmation dialog
   - Dialog contains:
     - Warning message
     - **Required** cancellation note textarea
     - "Xác nhận hủy" and "Đóng" buttons

2. **User Types Cancellation Reason:**
   - Local state: `cancellationNote` updated

3. **User Clicks "Xác nhận hủy":**
   - **Validation:** `cancellationNote.trim().length > 0`
   - If empty → Show error: "Vui lòng nhập lý do hủy"
4. **Submission:**
   - **API Call:** `useCancelOrderMutation`
     - **Endpoint:** `PATCH /purchase-service/api/v1/order/manage/${orderId}/cancel`
     - **Body:** `{ note: cancellationNote }`

5. **Response:**
   - **Success:**
     - Toast: "Đơn hàng đã được hủy"
     - Cache invalidation triggers refetch
     - Status badge updates to "Đã hủy" (red with `XCircle`)
     - "Cancel" button disappears
     - Cancellation note displayed in order details
   - **Error:**
     - Toast: Error message
     - Dialog remains open

### Flow 5: Filtering and Searching Orders

**Starting Point:** Order List Page

1. **Search by Query:**
   - User types in search input
   - **No Immediate API Call** (controlled input only)
   - User presses Enter or clicks "Tìm kiếm" button
   - **Trigger:** `handleSearch()` function
   - Sets `pagination.page = 0` (reset to first page)
   - **API Call:** `useGetOrdersQuery` with new `searchQuery`
   - List updates with matching orders

2. **Filter by Status:**
   - User clicks "Bộ lọc" button → `showFilters = true`
   - Expanded filter card appears
   - User selects status from dropdown (CREATED, APPROVED, etc.)
   - **onChange:** Updates `statusFilter` state
   - **Auto Trigger:** RTK Query re-fetches with new filters
   - Order list updates instantly

3. **Clear Filters:**
   - User clicks "Xóa tất cả bộ lọc" button
   - Calls `clearFilters()`:
     ```typescript
     setSearchQuery('');
     setStatusFilter('');
     setPagination({ ...pagination, page: 0 });
     ```
   - RTK Query refetches with empty filters

4. **Pagination:**
   - User clicks page number or Next/Previous buttons
   - Updates `pagination.page`
   - RTK Query refetches for new page
   - Scroll to top (browser behavior)

5. **View Mode Toggle:**
   - User clicks Grid or List icon
   - Updates `viewMode` state
   - Component re-renders with different CSS classes
   - **No API Call** (pure UI change)

---

## Form Validation & Business Rules

### OrderForm Validation Rules

#### 1. Supplier Selection (Create Mode Only)

- **Rule:** Supplier must be selected before creating an order
- **Validation:** `if (!supplierId)`
- **Error Message:** "Nhà cung cấp là bắt buộc"
- **UI Indicator:** Red border on input + error text below field
- **Business Logic:** Orders cannot exist without a supplier in purchase context

#### 2. Sale Channel

- **Rule:** Sale channel must be selected
- **Validation:** `if (!saleChannel)`
- **Error Message:** "Kênh bán hàng là bắt buộc"
- **Options:** ONLINE, PARTNER, RETAIL
- **Default Value:** 'ONLINE'
- **Business Logic:** Required for revenue tracking and sales analytics

#### 3. Order Items (Create Mode Only)

- **Rule:** At least one product must be added to the order
- **Validation:** `if (items.length === 0)`
- **Error Message:** "Phải có ít nhất một sản phẩm"
- **Business Logic:** Empty orders are not meaningful in procurement

#### 4. Delivery Date Logic

- **Rule:** Delivery Before Date must be later than or equal to Delivery After Date
- **Validation:**
  ```typescript
  if (deliveryBeforeDate && deliveryAfterDate) {
    const before = new Date(deliveryBeforeDate);
    const after = new Date(deliveryAfterDate);
    if (before < after) {
      /* Error */
    }
  }
  ```
- **Error Message:** "Ngày giao hàng trước phải sau ngày giao hàng sau"
- **UI Indicator:** Red border on "Delivery Before" date picker
- **Business Logic:** Defines acceptable delivery window (e.g., deliver after Jan 10, before Jan 15)

#### 5. Priority Range

- **Rule:** Priority must be between 1 and 10
- **Validation:** HTML `min='1' max='10'` attribute
- **Default Value:** 1
- **UI:** Number input with +/- controls
- **Business Logic:** 1 = Low priority, 10 = Urgent/High priority

#### 6. Product Quantity

- **Rule:** Quantity must be at least 1
- **Validation:** HTML `min='1'` + `parseInt(value) || 1`
- **Default Value:** 1 per item
- **UI:** Number input with validation
- **Business Logic:** Cannot order zero or negative quantities

#### 7. Tax Rate

- **Rule:** Tax percentage must be between 0% and 100%
- **Validation:** HTML `min='0' max='100' step='0.1'`
- **Default Value:** 0
- **Business Logic:** VAT or other tax rates applied to order items

#### 8. Discount

- **Rule:** Discount must be non-negative
- **Validation:** HTML `min='0'`
- **Default Value:** 0
- **Format:** Currency (đ)
- **Business Logic:** Discounts reduce item total, cannot be negative

#### 9. Order Name (Optional)

- **Rule:** No validation (optional field)
- **Use Case:** Descriptive name like "Đơn nhập hàng tháng 12"
- **Fallback Display:** If empty, shows "Đơn hàng #{orderId.slice(0,8)}..."

#### 10. Note (Optional)

- **Rule:** No validation (optional field)
- **UI:** Textarea with 4 rows
- **Use Case:** Additional instructions or comments

### Cancellation Form Validation

#### Required Cancellation Note

- **Rule:** User must provide a reason when canceling an order
- **Validation:** `cancellationNote.trim().length > 0`
- **Error Message:** "Vui lòng nhập lý do hủy"
- **Business Logic:** Audit trail requirement for order cancellations

### Frontend vs Backend Validation

| Validation                    | Frontend | Backend |
| ----------------------------- | -------- | ------- |
| Required fields               | ✅       | ✅      |
| Field formats                 | ✅       | ✅      |
| Business rules (dates)        | ✅       | ✅      |
| Data integrity (foreign keys) | ❌       | ✅      |
| Authorization                 | ❌       | ✅      |
| Database constraints          | ❌       | ✅      |

**Frontend Validation Purpose:**

- Immediate user feedback (no network delay)
- Prevents unnecessary API calls
- Enhances UX with inline error messages

**Backend Validation Purpose:**

- Security (cannot trust client-side validation)
- Data integrity enforcement
- Business rule consistency
- Handles edge cases (concurrent updates, etc.)

---

## API Integration

### API Endpoint Summary

All endpoints use base URL: `/purchase-service/api/v1/` (routed via API Gateway)

#### Supplier Endpoints

| Method | Endpoint                        | Purpose                     | Request Body                                      | Response                      |
| ------ | ------------------------------- | --------------------------- | ------------------------------------------------- | ----------------------------- |
| GET    | `/supplier/search`              | List suppliers with filters | Query params: `query`, `statusId`, `page`, `size` | `PaginatedResponse<Supplier>` |
| GET    | `/supplier/search/{supplierId}` | Get supplier details        | -                                                 | `Supplier`                    |
| POST   | `/supplier/create`              | Create supplier             | `SupplierCreationForm`                            | `Supplier`                    |
| PATCH  | `/supplier/update/{supplierId}` | Update supplier             | `SupplierUpdateForm`                              | `Supplier`                    |
| DELETE | `/supplier/delete/{supplierId}` | Delete supplier             | -                                                 | `{ deleted: boolean }`        |

#### Product Endpoints

| Method | Endpoint                      | Purpose                    | Request Body                                                    | Response                     |
| ------ | ----------------------------- | -------------------------- | --------------------------------------------------------------- | ---------------------------- |
| GET    | `/product/search`             | List products with filters | Query params: `query`, `categoryId`, `statusId`, `page`, `size` | `PaginatedResponse<Product>` |
| GET    | `/product/search/{productId}` | Get product details        | -                                                               | `Product`                    |
| POST   | `/product/create`             | Create product             | `ProductCreationForm`                                           | `Product`                    |
| PATCH  | `/product/update/{productId}` | Update product             | `ProductUpdateForm`                                             | `Product`                    |
| DELETE | `/product/delete/{productId}` | Delete product             | -                                                               | `{ deleted: boolean }`       |

#### Order Endpoints

| Method | Endpoint                                       | Purpose                  | Request Body                                                                                                              | Response                     |
| ------ | ---------------------------------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------- | ---------------------------- |
| GET    | `/order/search`                                | List orders with filters | Query params: `query`, `statusId`, `fromSupplierId`, `saleChannelId`, `orderDateAfter`, `orderDateBefore`, `page`, `size` | `PaginatedResponse<Order>`   |
| GET    | `/order/search/{orderId}`                      | Get order with items     | -                                                                                                                         | `Order` (includes `items[]`) |
| POST   | `/order/create`                                | Create order with items  | `OrderCreationForm`                                                                                                       | `Order`                      |
| PATCH  | `/order/update/{orderId}`                      | Update order metadata    | `OrderUpdateForm`                                                                                                         | `Order`                      |
| DELETE | `/order/delete/{orderId}`                      | Delete order             | -                                                                                                                         | `{ deleted: boolean }`       |
| PATCH  | `/order/manage/{orderId}/approve`              | Approve order            | -                                                                                                                         | `Order` (status → APPROVED)  |
| PATCH  | `/order/manage/{orderId}/cancel`               | Cancel order             | `{ note: string }`                                                                                                        | `Order` (status → CANCELLED) |
| POST   | `/order/create/{orderId}/add`                  | Add product to order     | `OrderItem`                                                                                                               | `Order` (updated items)      |
| PATCH  | `/order/update/{orderId}/update/{orderItemId}` | Update order item        | `OrderItemUpdateForm`                                                                                                     | `Order`                      |
| PATCH  | `/order/update/{orderId}/delete/{orderItemId}` | Remove order item        | -                                                                                                                         | `Order`                      |

#### Category Endpoints

| Method | Endpoint                        | Purpose         | Request Body                          | Response                      |
| ------ | ------------------------------- | --------------- | ------------------------------------- | ----------------------------- |
| GET    | `/category/search`              | List categories | Query params: `query`, `page`, `size` | `PaginatedResponse<Category>` |
| POST   | `/category/create`              | Create category | `{ name: string }`                    | `Category`                    |
| PATCH  | `/category/update/{categoryId}` | Update category | `{ name: string }`                    | `Category`                    |
| DELETE | `/category/delete/{categoryId}` | Delete category | -                                     | `{ deleted: boolean }`        |

#### Facility Endpoints

| Method | Endpoint                        | Purpose              | Request Body                                      | Response                      |
| ------ | ------------------------------- | -------------------- | ------------------------------------------------- | ----------------------------- |
| GET    | `/facility/search`              | List facilities      | Query params: `query`, `statusId`, `page`, `size` | `PaginatedResponse<Facility>` |
| GET    | `/facility/search/{facilityId}` | Get facility details | -                                                 | `Facility`                    |
| POST   | `/facility/create`              | Create facility      | `FacilityCreationForm`                            | `Facility`                    |
| PATCH  | `/facility/update/{facilityId}` | Update facility      | `FacilityUpdateForm`                              | `Facility`                    |
| DELETE | `/facility/delete/{facilityId}` | Delete facility      | -                                                 | `{ deleted: boolean }`        |

#### Address Endpoints

| Method | Endpoint                               | Purpose                   | Request Body          | Response    |
| ------ | -------------------------------------- | ------------------------- | --------------------- | ----------- |
| POST   | `/address/create`                      | Create address for entity | `AddressCreationForm` | `Address`   |
| PATCH  | `/address/update/{addressId}`          | Update address            | `AddressUpdateForm`   | `Address`   |
| GET    | `/address/search/by-entity/{entityId}` | Get addresses for entity  | -                     | `Address[]` |

#### Shipment Endpoints

| Method | Endpoint                              | Purpose                 | Request Body | Response     |
| ------ | ------------------------------------- | ----------------------- | ------------ | ------------ |
| GET    | `/shipment/search/{shipmentId}`       | Get shipment details    | -            | `Shipment`   |
| GET    | `/shipment/search/by-order/{orderId}` | Get shipments for order | -            | `Shipment[]` |

### RTK Query Hook Usage Examples

#### Querying Data (GET)

```typescript
// In a component
import { useGetOrdersQuery } from '@/modules/purchase/api/purchaseApi';

const OrdersList = () => {
  const { data, isLoading, error, refetch } = useGetOrdersQuery({
    filters: { statusId: 'APPROVED' },
    pagination: { page: 0, size: 20 }
  });

  if (isLoading) return <Spinner />;
  if (error) return <ErrorMessage error={error} />;

  const orders = data?.data?.items || [];
  return <OrderGrid orders={orders} />;
};
```

#### Mutations (POST/PATCH/DELETE)

```typescript
import { useCreateOrderMutation } from '@/modules/purchase/api/purchaseApi';

const CreateOrderForm = () => {
  const [createOrder, { isLoading, error }] = useCreateOrderMutation();

  const handleSubmit = async (formData: OrderCreationForm) => {
    try {
      const result = await createOrder(formData).unwrap();
      toast.success('Order created');
      router.push(`/purchase/purchase-orders/${result.data.id}`);
    } catch (err) {
      toast.error(err.data?.message || 'Failed to create order');
    }
  };

  return <form onSubmit={handleSubmit}>...</form>;
};
```

#### Conditional Fetching

```typescript
// Skip query until condition is met
const { data } = useGetSupplierQuery(supplierId, {
  skip: !supplierId, // Don't fetch if no ID
});
```

#### Manual Refetching

```typescript
const { data, refetch } = useGetOrdersQuery(filters);

// Manually trigger refetch
<Button onClick={() => refetch()}>Refresh</Button>
```

### Error Handling

**Response Structure:**

```typescript
// Success
{
  code: 200,
  message: "Success",
  status: "SUCCESS",
  data: { /* Entity or paginated response */ }
}

// Error
{
  code: 400,
  message: "Validation failed: Supplier not found",
  status: "FAILED"
}
```

**Frontend Error Handling:**

```typescript
try {
  await createOrder(data).unwrap();
  toast.success('Order created');
} catch (error: any) {
  // RTK Query wraps error in error.data
  const message = error?.data?.message || 'An error occurred';
  toast.error(message);
  console.error('API Error:', error);
}
```

---

## Shared Components Usage

The Purchase module leverages the shared component library from `@/shared/components/ui` (Shadcn UI) and follows consistent patterns.

### UI Components Used

#### 1. **Card** (Container)

```tsx
import { Card, CardHeader, CardContent } from '@/shared/components/ui';

<Card>
  <CardHeader>
    <h3>Order Information</h3>
  </CardHeader>
  <CardContent>
    <p>Order details...</p>
  </CardContent>
</Card>;
```

**Usage:** Primary container for sections (order details, stats, forms)

#### 2. **Button** (Actions)

```tsx
import { Button } from '@/shared/components/ui';

<Button onClick={handleSubmit} disabled={isLoading}>
  <Save className="h-4 w-4 mr-2" />
  Save Order
</Button>

<Button variant="outline" size="sm">Cancel</Button>
<Button variant="ghost" size="icon"><Edit /></Button>
```

**Variants:** `default`, `destructive`, `outline`, `secondary`, `ghost`, `link`
**Sizes:** `default`, `sm`, `lg`, `icon`

#### 3. **Input** (Forms)

```tsx
import { Input, Label } from '@/shared/components/ui';

<div>
  <Label htmlFor='orderName'>Order Name</Label>
  <Input
    id='orderName'
    value={orderName}
    onChange={(e) => setOrderName(e.target.value)}
    placeholder='Enter order name'
    className={cn(errors.orderName && 'border-destructive')}
  />
</div>;
```

**Usage:** Text inputs, search fields, number inputs

#### 4. **Badge** (Status Indicators)

```tsx
import { Badge } from '@/shared/components/ui';

<Badge variant='secondary' className={cn(status.bg, status.text)}>
  <CheckCircle2 className='h-3 w-3 mr-1' />
  Approved
</Badge>;
```

**Variants:** `default`, `secondary`, `destructive`, `outline`
**Custom Styling:** Background and text colors applied via `className`

#### 5. **Dialog** (Modals)

```tsx
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/components/ui';

<Dialog open={isOpen} onOpenChange={setIsOpen}>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Cancel Order</DialogTitle>
      <DialogDescription>
        Please provide a reason for cancellation
      </DialogDescription>
    </DialogHeader>
    <Textarea value={note} onChange={(e) => setNote(e.target.value)} />
    <DialogFooter>
      <Button onClick={handleCancel}>Confirm</Button>
    </DialogFooter>
  </DialogContent>
</Dialog>;
```

**Usage:** Confirmations, forms, detail overlays

#### 6. **Tabs** (Navigation)

```tsx
import {
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
} from '@/shared/components/ui';

<Tabs defaultValue='overview'>
  <TabsList>
    <TabsTrigger value='overview'>Overview</TabsTrigger>
    <TabsTrigger value='items'>Items</TabsTrigger>
    <TabsTrigger value='shipments'>Shipments</TabsTrigger>
  </TabsList>
  <TabsContent value='overview'>...</TabsContent>
  <TabsContent value='items'>...</TabsContent>
  <TabsContent value='shipments'>...</TabsContent>
</Tabs>;
```

**Usage:** Order detail page sections

#### 7. **DropdownMenu** (Action Menus)

```tsx
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
} from '@/shared/components/ui';

<DropdownMenu>
  <DropdownMenuTrigger asChild>
    <Button variant='ghost' size='icon'>
      <MoreHorizontal className='h-4 w-4' />
    </Button>
  </DropdownMenuTrigger>
  <DropdownMenuContent align='end'>
    <DropdownMenuItem onClick={handleEdit}>
      <Edit className='h-4 w-4 mr-2' />
      Edit
    </DropdownMenuItem>
    <DropdownMenuSeparator />
    <DropdownMenuItem onClick={handleDelete}>
      <Trash2 className='h-4 w-4 mr-2' />
      Delete
    </DropdownMenuItem>
  </DropdownMenuContent>
</DropdownMenu>;
```

**Usage:** Item actions, bulk actions

#### 8. **Textarea** (Multi-line Input)

```tsx
<Textarea
  value={note}
  onChange={(e) => setNote(e.target.value)}
  placeholder='Enter notes...'
  rows={4}
  className='resize-none'
/>
```

**Usage:** Notes, descriptions, cancellation reasons

### Icons (Lucide React)

Consistent icon usage throughout the module:

| Icon                           | Usage Context                      |
| ------------------------------ | ---------------------------------- |
| `ShoppingCart`                 | Order-related actions/displays     |
| `Package`                      | Products, items, shipments         |
| `Truck`                        | Delivery, shipments, logistics     |
| `User`                         | User profiles, creators, approvers |
| `Calendar`                     | Date fields and displays           |
| `Clock`                        | Timestamps, pending status         |
| `CheckCircle2`                 | Approved status, success states    |
| `XCircle`                      | Cancelled status, errors           |
| `AlertCircle`                  | Warnings, important notices        |
| `Edit`                         | Edit actions                       |
| `Trash2`                       | Delete actions                     |
| `Plus`                         | Add/create actions                 |
| `Search`                       | Search inputs                      |
| `SlidersHorizontal`            | Filter controls                    |
| `DollarSign`                   | Financial data                     |
| `TrendingUp`                   | Priority, analytics                |
| `Grid3X3` / `List`             | View mode toggles                  |
| `ArrowLeft`                    | Back navigation                    |
| `ChevronLeft` / `ChevronRight` | Pagination                         |

**Import:**

```typescript
import { ShoppingCart, Package, Calendar, CheckCircle2 } from 'lucide-react';
```

### Utility Functions

#### cn() - Class Name Utility

```typescript
import { cn } from '@/shared/utils';

// Conditional classes
<div className={cn(
  'base-class',
  isActive && 'active-class',
  variant === 'primary' && 'primary-class'
)} />
```

**Purpose:** Merges Tailwind classes with conditional logic (uses `clsx` + `tailwind-merge`)

### Toast Notifications (Sonner)

```typescript
import { toast } from 'sonner';

// Success
toast.success('Order created successfully');

// Error
toast.error('Failed to create order');

// Info
toast.info('Processing your request');

// Custom
toast('Order #12345', {
  description: 'Has been approved',
  action: {
    label: 'View',
    onClick: () => router.push(`/orders/${orderId}`),
  },
});
```

**Usage:** User feedback for all mutations

---

## Type Definitions

### Core Entity Types

#### Order

```typescript
interface Order {
  id: string;
  orderTypeId: OrderType; // 'PURCHASE' | 'SALES'
  fromSupplierId?: string; // For purchase orders
  toCustomerId: string;
  createdByUserId: number;
  createdStamp: string; // ISO 8601
  orderDate: string;
  statusId: OrderStatus; // 'CREATED' | 'APPROVED' | 'CANCELLED' | 'FULLY_DELIVERED'
  lastUpdatedStamp: string;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  note?: string;
  orderName?: string;
  priority?: number; // 1-10
  deliveryAddressId?: string;
  deliveryPhone?: string;
  saleChannelId: SaleChannel; // 'ONLINE' | 'PARTNER' | 'RETAIL'
  deliveryFullAddress?: string;
  totalQuantity?: number;
  totalAmount: number;
  costs?: string; // JSON string
  userApprovedId?: number;
  userCancelledId?: number;
  cancellationNote?: string;
  tenantId: number;
  items?: OrderItem[]; // Populated in detail view
  shipments?: Shipment[]; // Populated in shipments tab
}
```

#### OrderItem

```typescript
interface OrderItem {
  id: string;
  orderId: string;
  orderItemSeqId: number; // Sequence number for ordering
  productId: string;
  quantity: number;
  quantityRemaining: number; // For shipment tracking
  amount: number; // Calculated: quantity * price - discount + tax
  statusId: OrderItemStatus; // 'CREATED' | 'DELIVERED'
  createdStamp: string;
  lastUpdatedStamp: string;
  price: number;
  tax?: number; // Percentage
  discount?: number; // Currency
  unit: string;
  tenantId: number;
  product?: Product; // Populated when fetched
}
```

#### Product

```typescript
interface Product {
  id: string;
  name: string;
  weight?: number;
  height?: number;
  unit: string; // e.g., 'kg', 'piece', 'box'
  costPrice: number;
  wholeSalePrice: number;
  retailPrice: number;
  categoryId: string;
  statusId: ProductStatus; // 'ACTIVE' | 'INACTIVE'
  imageId?: string;
  extraProps?: string; // JSON for additional attributes
  createdStamp: string;
  lastUpdatedStamp: string;
  vatRate?: number;
  skuCode: string; // Stock Keeping Unit
  tenantId: number;
  quantityAvailable: number; // Current inventory
}
```

#### Supplier

```typescript
interface Supplier {
  id: string;
  name: string;
  currentAddressId?: string;
  email?: string;
  phone?: string;
  statusId: SupplierStatus; // 'ACTIVE' | 'INACTIVE'
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  address?: Address; // Populated when fetched with address
}
```

#### Shipment

```typescript
interface Shipment {
  id: string;
  shipmentTypeId: ShipmentType; // 'INBOUND' | 'OUTBOUND'
  fromSupplierId?: string;
  toCustomerId?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  orderId: string;
  createdByUserId: number;
  shipmentName: string;
  statusId: ShipmentStatus; // 'CREATED' | 'IMPORTED' | 'EXPORTED'
  handledByUserId?: number;
  note?: string;
  expectedDeliveryDate?: string;
  userCancelledId?: number;
  totalWeight?: number;
  totalQuantity?: number;
  tenantId: number;
  items?: InventoryItemDetail[];
}
```

### Form Types (Request DTOs)

#### OrderCreationForm

```typescript
interface OrderCreationForm {
  fromSupplierId?: string; // Required for purchase orders
  orderName?: string;
  note?: string;
  deliveryBeforeDate?: string; // ISO 8601 date
  deliveryAfterDate?: string;
  priority?: number;
  saleChannelId?: SaleChannel;
  items: OrderItemForm[]; // At least 1 item required
}

interface OrderItemForm {
  productId: string;
  orderItemSeqId: number;
  quantity: number;
  tax?: number;
  discount?: number;
}
```

#### OrderUpdateForm

```typescript
interface OrderUpdateForm {
  orderName?: string;
  note?: string;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  priority?: number;
  saleChannelId?: SaleChannel;
}
// Note: Items cannot be updated via this form, use separate item endpoints
```

#### SupplierCreationForm

```typescript
interface SupplierCreationForm {
  name: string;
  email?: string;
  phone?: string;
  statusId: SupplierStatus;
  // Address information
  addressType: AddressType; // 'FACILIY' | 'SHIPPING' | 'BUSSINESS'
  latitude?: number;
  longitude?: number;
  fullAddress: string;
}
```

### Enums and Constants

```typescript
// Order Status
type OrderStatus = 'CREATED' | 'APPROVED' | 'CANCELLED' | 'FULLY_DELIVERED';

// Order Type
type OrderType = 'PURCHASE' | 'SALES';

// Sale Channel
type SaleChannel = 'ONLINE' | 'PARTNER' | 'RETAIL';

// Supplier Status
type SupplierStatus = 'ACTIVE' | 'INACTIVE';

// Product Status
type ProductStatus = 'ACTIVE' | 'INACTIVE';

// Shipment Status
type ShipmentStatus = 'CREATED' | 'IMPORTED' | 'EXPORTED';

// Shipment Type
type ShipmentType = 'INBOUND' | 'OUTBOUND';
```

### Pagination and Response Types

```typescript
interface PaginationParams {
  page?: number; // 0-indexed
  size?: number; // Items per page
  sortBy?: string; // Field name
  sortDirection?: 'asc' | 'desc';
}

interface PaginatedResponse<T> {
  items: T[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

interface APIResponse<T> {
  code: number; // HTTP status code
  message: string; // Human-readable message
  status: ResponseStatus; // 'SUCCESS' | 'FAILED'
  data?: T; // Response payload
}
```

---

## Appendix: File Structure

### Complete Module File Tree

```
src/modules/purchase/
│
├── api/
│   └── purchaseApi.ts                    # 586 lines - All RTK Query endpoints
│
├── components/
│   ├── forms/
│   │   └── OrderForm.tsx                 # 663 lines - Order create/edit form
│   ├── layout/
│   │   ├── PurchaseHeader.tsx            # Navigation header
│   │   ├── PurchaseLayout.tsx            # Sidebar layout
│   │   └── index.ts                      # Barrel export
│   ├── PurchaseAuthGuard.tsx             # Route protection
│   └── index.ts
│
├── pages/
│   ├── purchase-orders/
│   │   ├── OrderListPage.tsx             # 676 lines - Order grid/list
│   │   ├── OrderDetailPage.tsx           # 1128 lines - Order detail
│   │   ├── CreateOrderPage.tsx           # Create wrapper
│   │   ├── EditOrderPage.tsx             # Edit wrapper
│   │   ├── shipments/                    # Shipment pages
│   │   └── index.ts
│   └── index.ts
│
├── store/
│   ├── purchaseSlice.ts                  # 150 lines - Redux state
│   ├── selectors.ts                      # State selectors
│   └── index.ts
│
├── types/
│   └── index.ts                          # 381 lines - TypeScript types
│
├── index.ts                              # Module barrel export
└── README.md                             # Module overview

src/app/purchase/                         # Next.js pages
├── layout.tsx                            # Purchase layout wrapper
├── page.tsx                              # Purchase dashboard
├── purchase-orders/
│   ├── page.tsx                          # Order list page
│   ├── new/
│   │   └── page.tsx                      # Create order page
│   └── [orderId]/
│       └── page.tsx                      # Order detail page
├── suppliers/
│   └── page.tsx
├── products/
│   └── page.tsx
├── facilities/
│   └── page.tsx
├── shipments/
│   └── page.tsx
└── dashboard/
    └── page.tsx
```

---

## Development Guidelines

### Adding New Features

1. **New Entity Type:**
   - Add type definitions in `types/index.ts`
   - Create API endpoints in `purchaseApi.ts`
   - Add Redux state in `purchaseSlice.ts` (if needed)
   - Create page components in `pages/`
   - Create form components in `components/forms/`
   - Add routes in `src/app/purchase/`

2. **New Validation Rule:**
   - Update form component validation logic
   - Add error state handling
   - Update documentation

3. **New API Endpoint:**
   - Add to `purchaseApi.ts` with proper cache tags
   - Export hook at bottom of file
   - Test cache invalidation

### Code Style Conventions

- **Component Naming:** PascalCase (e.g., `OrderListPage`)
- **File Naming:** Match component name (e.g., `OrderListPage.tsx`)
- **Props Interface:** `ComponentName + Props` (e.g., `OrderListPageProps`)
- **Function Names:** camelCase (e.g., `handleSubmit`)
- **Constants:** UPPER_SNAKE_CASE (e.g., `STATUS_CONFIG`)
- **Comments:** Use JSDoc for components, inline for complex logic

### Testing Checklist

- [ ] Create order with valid data
- [ ] Create order with missing required fields
- [ ] Edit order metadata
- [ ] Approve order (changes status)
- [ ] Cancel order with note
- [ ] Add product to order
- [ ] Update order item quantity
- [ ] Remove product from order
- [ ] Filter orders by status
- [ ] Search orders by query
- [ ] Pagination navigation
- [ ] View mode toggle (grid/list)
- [ ] Responsive design (mobile/tablet/desktop)
- [ ] Error handling (network errors, validation errors)
- [ ] Loading states
- [ ] Toast notifications

---

**End of Documentation**

For questions or updates, contact: QuanTuanHuy  
Last reviewed: January 16, 2026
