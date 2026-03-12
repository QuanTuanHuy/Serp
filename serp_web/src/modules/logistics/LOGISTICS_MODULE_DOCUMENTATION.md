# Logistics Module Documentation

**Author:** KienPT
**Module:** Logistics  
**Last Updated:** January 16, 2026

---

## 1. Module Overview

### 1.1 Functional Purpose

The **Logistics Module** manages the complete supply chain workflow for the ERP system, including:

- **Purchase Orders Management**: Create, track, and approve purchase orders from suppliers
- **Inventory Management**: Monitor stock levels, track inventory items across facilities, and manage inventory status (valid, expired, damaged)
- **Shipment Management**: Handle inbound (receiving) and outbound (shipping) shipments with comprehensive tracking
- **Facility Management**: Manage warehouse and storage locations
- **Product & Supplier Data**: Access product catalogs and supplier information (read-only, managed by other modules)

This module serves as the central hub for warehouse operations, procurement tracking, and inventory visibility across the organization.

### 1.2 Key User Actions

**Purchase Orders:**

- View list of purchase orders with filtering and pagination
- View detailed order information including items, supplier, and delivery status
- Track order status: Created → Approved → Fully Delivered / Cancelled
- Monitor related shipments for each order

**Inventory:**

- Browse inventory items across all facilities
- Filter by product, facility, status (Valid/Expired/Damaged)
- View detailed inventory item information (lot numbers, expiry dates, quantities)
- Create new inventory entries manually
- Edit inventory item details (quantity, status, dates)
- Delete inventory items (with confirmation)

**Shipments:**

- View list of all shipments (inbound and outbound)
- Create new inbound shipments linked to purchase orders
- Edit shipment details and items
- Import shipments to transfer inventory into facilities
- Track shipment status: Created (Draft) → Imported/Exported
- Add, update, and remove items from shipments
- Delete shipments

**General Actions:**

- Search and filter across all entities
- Switch between grid and list view layouts
- Navigate via breadcrumb navigation
- Access statistics and KPI cards
- Export data (future enhancement)

---

## 2. Component Architecture

### 2.1 Component Hierarchy

```
LogisticsLayout (app/logistics/layout.tsx)
│
├─ LogisticsAuthGuard (module access control)
│  └─ SidebarProvider (sidebar state management)
│     └─ LogisticsLayoutContent
│        ├─ DynamicSidebar (moduleCode='LOGISTICS')
│        │  └─ Navigation items (Orders, Inventory, Shipments)
│        │
│        ├─ LogisticsHeader (scrollContainerRef)
│        │  └─ Module-specific header actions
│        │
│        └─ RouteGuard (moduleCode='LOGISTICS')
│           └─ Page Content (children)
│              │
│              ├─ Purchase Orders Flow
│              │  ├─ OrderListPage
│              │  │  ├─ StatsCard × 4 (Total, Approved, Pending, Delivered)
│              │  │  ├─ Search & Filter Controls
│              │  │  │  ├─ Input (Search by order ID, supplier)
│              │  │  │  ├─ Select (Supplier filter)
│              │  │  │  └─ Select (Status filter)
│              │  │  ├─ View Toggle (Grid/List)
│              │  │  ├─ OrderCard (Grid view) × N
│              │  │  │  ├─ Badge (Status)
│              │  │  │  ├─ Order details (ID, supplier, dates)
│              │  │  │  └─ Button (View Details)
│              │  │  ├─ OrderRow (List view) × N
│              │  │  └─ Pagination Controls
│              │  │
│              │  └─ OrderDetailPage ([orderId])
│              │     ├─ Header Section
│              │     │  ├─ Badge (Status)
│              │     │  ├─ Button (Back)
│              │     │  └─ Button (Edit - disabled)
│              │     ├─ InfoCard (Order Overview)
│              │     │  ├─ StatsRow (Total Amount, Items Count)
│              │     │  └─ Details (Dates, Creator, Supplier)
│              │     ├─ Tabs
│              │     │  ├─ "Chi tiết đơn hàng" Tab
│              │     │  │  ├─ SupplierInfoCard
│              │     │  │  │  └─ Supplier details with avatar
│              │     │  │  └─ OrderItemsTable
│              │     │  │     └─ OrderItemRow × N (Product, Quantity, Price)
│              │     │  │
│              │     │  └─ "Lịch sử giao hàng" Tab
│              │     │     └─ ShipmentCard × N (related shipments)
│              │     │
│              │     └─ UserProfileSection (Created by, Approved by)
│              │
│              ├─ Inventory Flow
│              │  ├─ InventoryListPage
│              │  │  ├─ StatsCard × 4 (Total Items, Valid, Expired, Damaged)
│              │  │  ├─ Search & Filter Controls
│              │  │  │  ├─ Input (Search by product)
│              │  │  │  ├─ Select (Facility filter)
│              │  │  │  ├─ Select (Product filter with search)
│              │  │  │  └─ Select (Status filter)
│              │  │  ├─ Button (Create New Item)
│              │  │  ├─ View Toggle (Grid/List)
│              │  │  ├─ InventoryCard (Grid view) × N
│              │  │  │  ├─ Badge (Status with icon)
│              │  │  │  ├─ Product info (name, SKU)
│              │  │  │  ├─ Facility & Location
│              │  │  │  ├─ Quantity metrics
│              │  │  │  └─ Button (View Details)
│              │  │  ├─ InventoryRow (List view) × N
│              │  │  └─ Pagination Controls
│              │  │
│              │  ├─ InventoryDetailPage ([itemId])
│              │  │  ├─ Header Section
│              │  │  │  ├─ Badge (Status)
│              │  │  │  ├─ Button (Back)
│              │  │  │  ├─ Button (Edit)
│              │  │  │  └─ Button (Delete with confirmation)
│              │  │  ├─ StatsRow (Available/Reserved/Total quantities)
│              │  │  ├─ Edit Mode Form
│              │  │  │  ├─ Input (Available Quantity)
│              │  │  │  ├─ Input (Reserved Quantity)
│              │  │  │  ├─ Input (Manufactured Date)
│              │  │  │  ├─ Input (Expiry Date)
│              │  │  │  ├─ Select (Status)
│              │  │  │  └─ Buttons (Save, Cancel)
│              │  │  └─ DetailSection (View Mode)
│              │  │     ├─ ProductInfoCard
│              │  │     ├─ FacilityInfoCard
│              │  │     └─ DatesInfoCard
│              │  │
│              │  └─ CreateInventoryItemPage
│              │     └─ InventoryCreationForm
│              │        ├─ Select (Facility)
│              │        ├─ Select (Product with search)
│              │        ├─ Input (Lot Number)
│              │        ├─ Input (Available Quantity)
│              │        ├─ Input (Reserved Quantity)
│              │        ├─ Input (Manufactured Date)
│              │        ├─ Input (Expiry Date)
│              │        ├─ Select (Status)
│              │        └─ Buttons (Submit, Cancel)
│              │
│              └─ Shipments Flow
│                 ├─ ShipmentListPage
│                 │  ├─ StatsCard × 4 (Total, Inbound, Outbound, Imported)
│                 │  ├─ Search & Filter Controls
│                 │  │  ├─ Input (Search by shipment name, ID)
│                 │  │  ├─ Select (Status filter)
│                 │  │  ├─ Select (Type filter: Inbound/Outbound)
│                 │  │  └─ Select (Supplier filter - for inbound)
│                 │  ├─ View Toggle (Grid/List)
│                 │  ├─ ShipmentCard (Grid view) × N
│                 │  │  ├─ Badge (Status + Type)
│                 │  │  ├─ Shipment details
│                 │  │  ├─ Order reference
│                 │  │  ├─ Supplier/Customer info
│                 │  │  └─ Button (View Details)
│                 │  ├─ ShipmentRow (List view) × N
│                 │  └─ Pagination Controls
│                 │
│                 ├─ ShipmentDetailPage ([shipmentId])
│                 │  ├─ Header Section
│                 │  │  ├─ Badge (Status + Type)
│                 │  │  ├─ Button (Back)
│                 │  │  ├─ DropdownMenu (Actions)
│                 │  │  │  ├─ MenuItem (Import Shipment)
│                 │  │  │  ├─ MenuItem (Edit)
│                 │  │  │  └─ MenuItem (Delete)
│                 │  │  └─ AlertDialog (Confirmations)
│                 │  ├─ Tabs
│                 │  │  ├─ "Chi tiết lô hàng" Tab
│                 │  │  │  ├─ OrderInfoCard
│                 │  │  │  ├─ ShipmentInfoCard
│                 │  │  │  └─ ShipmentItemsSection
│                 │  │  │     ├─ Button (Add Item)
│                 │  │  │     ├─ ItemCard × N
│                 │  │  │     │  ├─ Product details
│                 │  │  │     │  ├─ Facility selection
│                 │  │  │     │  ├─ Quantity/Weight inputs
│                 │  │  │     │  ├─ Inventory details (expiry, lot)
│                 │  │  │     │  └─ Buttons (Save, Delete)
│                 │  │  │     └─ Dialog (Add New Item)
│                 │  │  │        └─ ShipmentItemForm
│                 │  │  │
│                 │  │  └─ "Thông tin nhà cung cấp" Tab
│                 │  │     └─ SupplierInfoCard
│                 │  │
│                 │  └─ UserProfileSection (Created by)
│                 │
│                 ├─ CreateShipmentPage (new)
│                 │  └─ InboundShipmentForm
│                 │     ├─ OrderSearchSection
│                 │     │  ├─ Input (Search purchase order)
│                 │     │  └─ OrderCard (selected order display)
│                 │     ├─ BasicInfoSection
│                 │     │  ├─ Input (Shipment Name)
│                 │     │  ├─ Input (Expected Delivery Date)
│                 │     │  └─ Textarea (Note)
│                 │     ├─ ItemsSection
│                 │     │  ├─ Button (Add from Order Items)
│                 │     │  ├─ ShipmentItemFormRow × N
│                 │     │  │  ├─ Select (Facility)
│                 │     │  │  ├─ Input (Quantity)
│                 │     │  │  ├─ Input (Weight)
│                 │     │  │  ├─ Input (Manufactured Date)
│                 │     │  │  ├─ Input (Expiry Date)
│                 │     │  │  ├─ Input (Lot Number)
│                 │     │  │  └─ Button (Remove)
│                 │     │  └─ ItemSummarySection (Total items, weight)
│                 │     └─ ActionButtons (Submit, Cancel)
│                 │
│                 └─ EditShipmentPage ([shipmentId]/edit)
│                    └─ InboundShipmentForm (pre-filled with shipment data)
```

### 2.2 Component Breakdown

#### 2.2.1 Layout Components

**`LogisticsLayout`** ([components/layout/LogisticsLayout.tsx](components/layout/LogisticsLayout.tsx))

- **Role:** Root layout wrapper for all Logistics pages
- **Features:**
  - Wraps content with `LogisticsAuthGuard` for module access control
  - Provides `SidebarProvider` for collapsible sidebar state management
  - Integrates `DynamicSidebar` with `moduleCode='LOGISTICS'`
  - Includes `LogisticsHeader` with scroll-aware behavior
  - Applies responsive margin based on sidebar collapsed state
  - Contains `RouteGuard` for route-level permission checks
- **Styling:** Flex layout with sidebar + main content area, smooth transitions

**`LogisticsHeader`** ([components/layout/LogisticsHeader.tsx](components/layout/LogisticsHeader.tsx))

- **Role:** Module-specific header bar
- **Features:** Breadcrumb navigation, module title, action buttons, scroll-aware styling

**`LogisticsAuthGuard`** ([components/LogisticsAuthGuard.tsx](components/LogisticsAuthGuard.tsx))

- **Role:** Authentication wrapper for module access control
- **Features:** Verifies user has access to Logistics module, redirects unauthorized users

---

#### 2.2.2 Page Components

**Purchase Orders:**

**`OrderListPage`** ([pages/purchase-orders/OrderListPage.tsx](pages/purchase-orders/OrderListPage.tsx))

- **Role:** Displays paginated list of purchase orders with search and filters
- **Key Features:**
  - 4 KPI cards: Total Orders, Approved, Pending (Created), Delivered
  - Search by order ID or supplier name
  - Filter by supplier dropdown (fetches suppliers via API)
  - Filter by status (Created, Approved, Cancelled, Fully Delivered)
  - Toggle between grid and list views
  - Grid view displays `OrderCard` components
  - List view displays compact `OrderRow` components
  - Pagination with page size selector
- **State Management:** Redux slices for filters and pagination
- **UI Elements:** `StatsCard`, `Input`, `Select`, `Button`, `Badge`, `Card`

**`OrderDetailPage`** ([pages/purchase-orders/OrderDetailPage.tsx](pages/purchase-orders/OrderDetailPage.tsx))

- **Role:** Displays comprehensive purchase order details
- **Key Features:**
  - Order status badge and action buttons (Back, Edit - disabled for now)
  - Overview card with total amount and item count statistics
  - Tabbed interface:
    - **Order Details Tab:** Supplier info card, order items table with product images
    - **Delivery History Tab:** Related shipments displayed as `ShipmentCard` components
  - User profile sections showing creator and approver
- **Data Fetching:** Fetches order, supplier, customer, shipments, and user profiles
- **UI Elements:** `Tabs`, `Card`, `Badge`, `Button`, `UserProfile` (from Admin module)

---

**Inventory:**

**`InventoryListPage`** ([pages/inventory/InventoryListPage.tsx](pages/inventory/InventoryListPage.tsx))

- **Role:** Displays all inventory items across facilities with multi-filter capabilities
- **Key Features:**
  - 4 KPI cards: Total Items, Valid, Expired, Damaged (color-coded)
  - Search by product name or SKU
  - Filter by facility dropdown
  - Filter by product dropdown (with real-time search)
  - Filter by status (Valid, Expired, Damaged)
  - "Create New Item" button navigates to creation page
  - Toggle between grid and list views
  - Grid view displays `InventoryCard` with product image, status badge, and metrics
  - List view displays compact table-like rows
  - Pagination controls
- **Status Visualization:** Color-coded badges with icons (CheckCircle2, XCircle, AlertTriangle)
- **State Management:** Redux for filters and pagination
- **UI Elements:** `StatsCard`, `Input`, `Select`, `Button`, `Badge`, `Card`

**`InventoryDetailPage`** ([pages/inventory/InventoryDetailPage.tsx](pages/inventory/InventoryDetailPage.tsx))

- **Role:** View and edit individual inventory item details
- **Key Features:**
  - View mode: Displays item details in cards (Product, Facility, Dates)
  - Edit mode: Inline form for updating quantities, dates, and status
  - Statistics row: Available, Reserved, and Total quantities
  - Action buttons: Edit (toggles edit mode), Delete (with confirmation), Save, Cancel
  - Real-time validation for dates and quantities
- **CRUD Operations:** Update inventory item, Delete inventory item
- **Data Fetching:** Fetches inventory item, associated product, and facility
- **UI Elements:** `Card`, `Input`, `Select`, `Badge`, `Button`, `AlertDialog`

**`CreateInventoryItemPage`** ([pages/inventory/CreateInventoryItemPage.tsx](pages/inventory/CreateInventoryItemPage.tsx))

- **Role:** Form for manually creating new inventory items
- **Key Features:**
  - Facility selector (required)
  - Product search and selection (required)
  - Lot number input
  - Available and reserved quantity inputs
  - Manufactured and expiry date pickers
  - Status selector (Valid/Expired/Damaged)
  - Form validation before submission
  - Cancel button returns to list
- **UI Elements:** `Card`, `CardHeader`, `Input`, `Label`, `Select`, `Button`

---

**Shipments:**

**`ShipmentListPage`** ([pages/shipments/ShipmentListPage.tsx](pages/shipments/ShipmentListPage.tsx))

- **Role:** Displays all shipments (inbound and outbound) with comprehensive filtering
- **Key Features:**
  - 4 KPI cards: Total Shipments, Inbound, Outbound, Imported
  - Search by shipment name or ID
  - Filter by status (Created, Imported, Exported)
  - Filter by type (Inbound, Outbound)
  - Filter by supplier (for inbound shipments)
  - Toggle between grid and list views
  - Grid view uses `ShipmentCard` component
  - Pagination controls
- **Card Display:** Each `ShipmentCard` shows type, status, order reference, supplier/customer
- **State Management:** Redux for filters and pagination
- **UI Elements:** `StatsCard`, `Input`, `Select`, `Button`, `ShipmentCard`

**`ShipmentDetailPage`** ([pages/shipments/ShipmentDetailPage.tsx](pages/shipments/ShipmentDetailPage.tsx))

- **Role:** Comprehensive shipment detail view with item management
- **Key Features:**
  - Header with status/type badges and action dropdown menu
  - Actions: Import Shipment (changes status to IMPORTED), Edit, Delete
  - Tabbed interface:
    - **Shipment Details Tab:**
      - Order information card
      - Shipment basic info card
      - Items section with inline editing
      - Add new item dialog
      - Edit item quantities, weights, and inventory details
      - Delete items from shipment
    - **Supplier Info Tab:** Displays supplier details
  - User profile section (created by)
  - Real-time item management with optimistic updates
- **CRUD Operations:** Add item, Update item, Delete item, Import shipment, Delete shipment
- **Confirmation Dialogs:** AlertDialog for destructive actions
- **UI Elements:** `Tabs`, `Card`, `Badge`, `DropdownMenu`, `Dialog`, `AlertDialog`, `Input`, `Button`

**`CreateShipmentPage`** ([pages/shipments/CreateShipmentPage.tsx](pages/shipments/CreateShipmentPage.tsx))

- **Role:** Create new inbound shipments linked to purchase orders
- **Features:**
  - Wraps `InboundShipmentForm` component
  - Handles form submission and navigation after creation

**`EditShipmentPage`** ([pages/shipments/EditShipmentPage.tsx](pages/shipments/EditShipmentPage.tsx))

- **Role:** Edit existing shipment details
- **Features:**
  - Pre-fills `InboundShipmentForm` with existing shipment data
  - Handles update submission

---

#### 2.2.3 Form Components

**`InboundShipmentForm`** ([components/forms/InboundShipmentForm.tsx](components/forms/InboundShipmentForm.tsx))

- **Role:** Comprehensive form for creating/editing inbound shipments
- **Key Features:**
  - **Order Selection:** Search and select purchase order (required)
  - **Basic Information:**
    - Shipment name input (required)
    - Expected delivery date picker (required)
    - Note textarea (optional)
  - **Items Management:**
    - Add items from selected order
    - Each item row includes:
      - Product information (read-only from order)
      - Facility selector (required)
      - Quantity input (max: order item quantity, required)
      - Weight input (optional)
      - Manufactured date picker (optional)
      - Expiry date picker (optional)
      - Lot number input (optional)
    - Remove item button
  - **Summary Section:** Displays total items count and total weight
  - **Validation:** Real-time validation for required fields and quantity limits
- **State Management:** Local state for form data, items array, errors, and search
- **Submission:** Calls `onSubmit` prop with structured form data
- **UI Elements:** `Card`, `Input`, `Textarea`, `Label`, `Button`, `Badge`, `Select`

---

#### 2.2.4 Card Components

**`ShipmentCard`** ([components/cards/ShipmentCard.tsx](components/cards/ShipmentCard.tsx))

- **Role:** Display shipment summary in grid view
- **Props:** `shipment`, `order`, `customer`, `supplier`, `onClick`
- **Features:**
  - Status badge (Created/Imported/Exported) with icon
  - Type badge (Inbound/Outbound) with icon
  - Shipment name and ID
  - Order reference (ID)
  - Supplier name (for inbound) or Customer name (for outbound)
  - Expected delivery date
  - Creator name
  - "View Details" button
- **Styling:** Card with gradient hover effect, color-coded by status
- **UI Elements:** `Card`, `CardContent`, `Badge`, `Button`, Lucide icons

---

### 2.3 UI Elements from Shared Components

The Logistics module extensively uses shared ERP components from `@/shared/components/ui`:

**Layout & Structure:**

- `Card`, `CardHeader`, `CardTitle`, `CardContent` - Container components for sections
- `Tabs`, `TabsList`, `TabsTrigger`, `TabsContent` - Tabbed interface navigation

**Form Controls:**

- `Input` - Text, number, and date inputs
- `Textarea` - Multi-line text input (notes, descriptions)
- `Label` - Form field labels
- `Select`, `SelectTrigger`, `SelectContent`, `SelectItem`, `SelectValue` - Dropdown selectors
- `Button` - Primary actions, secondary actions, icon buttons

**Feedback & Indicators:**

- `Badge` - Status indicators (color-coded: blue, green, amber, rose, purple)
- `AlertDialog` - Confirmation dialogs for destructive actions
- `Dialog` - Modal windows for forms (add item, etc.)
- `DropdownMenu` - Action menus (three-dot menus)
- `toast` (from `sonner`) - Success/error notifications

**Data Display:**

- `StatsCard` - Custom component for KPI metrics (built on `Card`)
- `Avatar` - User profile images (from Admin module's `UserProfile`)
- Lucide Icons - Extensive use of icons for visual hierarchy

**Navigation:**

- `Button` (variants: ghost, outline, default) - Navigation and actions
- Breadcrumbs (integrated in `LogisticsHeader`)

**Utilities:**

- `cn()` utility (from `@/shared/utils`) - Conditional class name merging
- Color schemes follow Tailwind conventions with dark mode support

---

## 3. State Management & Data Integration

### 3.1 State Architecture

The Logistics module uses a **hybrid state management approach** combining Redux Toolkit (global state) and React local state (component-specific state).

#### 3.1.1 Global State (Redux Store)

**Store Structure** ([store/logisticsSlice.ts](store/logisticsSlice.ts)):

```typescript
interface LogisticsState {
  ui: LogisticsUIState; // UI preferences
  categories: CategoryState; // Category data
  customers: CustomerState; // Customer data (read-only)
  facilities: FacilityState; // Facility/warehouse data
  inventoryItems: InventoryItemState; // Inventory data
  orders: OrderState; // Order data (read-only)
  products: ProductState; // Product data
  shipments: ShipmentState; // Shipment data
  suppliers: SupplierState; // Supplier data (read-only)
}
```

Each entity state contains:

- `items: T[]` - Cached list items
- `selectedItems: string[]` - Selected item IDs (for bulk operations)
- `loading: boolean` - Loading state
- `error: string | null` - Error messages
- `filters: TFilters` - Active filter values
- `pagination: PaginationParams` - Current page, size, sort settings
- `total: number` - Total items count

**UI State:**

```typescript
interface LogisticsUIState {
  activeModule:
    | 'dashboard'
    | 'inventory'
    | 'shipments'
    | 'orders'
    | 'products'
    | 'facilities'
    | 'suppliers';
  selectedItems: string[];
  bulkActionMode: boolean;
  viewMode: 'list' | 'grid' | 'kanban';
  sidebarCollapsed: boolean;
  filterPanelOpen: boolean;
}
```

**Redux Actions** (via `logisticsSlice.ts`):

- **Filters:** `setOrderFilters`, `setInventoryItemFilters`, `setShipmentFilters`, `setCategoryFilters`, etc.
- **Pagination:** `setOrderPagination`, `setInventoryItemPagination`, `setShipmentPagination`, etc.
- **UI:** `setActiveModule`, `setViewMode`, `toggleSidebar`, `toggleFilterPanel`, `toggleBulkActionMode`
- **Selection:** `setGlobalSelectedItems`, `clearGlobalSelectedItems`, `toggleItemSelection`

**Selectors** ([store/selectors.ts](store/selectors.ts)):

- Base selectors: `selectOrders`, `selectInventoryItems`, `selectShipments`, etc.
- Derived selectors: `selectOrderFilters`, `selectInventoryItemPagination`, `selectShipmentItems`, etc.
- Memoized selectors using `createSelector` for performance

**Usage Pattern:**

```typescript
// In components
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { setOrderFilters, selectOrderFilters } from '@/modules/logistics';

const filters = useAppSelector(selectOrderFilters);
const dispatch = useAppDispatch();
dispatch(setOrderFilters({ statusId: 'APPROVED' }));
```

#### 3.1.2 Local State (React useState/useReducer)

**Component-Level State:**

- **Form Data:** Input values, validation errors, submission status
- **UI Toggles:** Dropdown visibility, modal open/closed, edit mode
- **Temporary Search:** Search queries before applying filters
- **Optimistic Updates:** Immediate UI feedback before API confirmation

**Examples:**

- `InboundShipmentForm`: Form data, items array, errors, search queries
- `InventoryDetailPage`: Edit mode toggle, edit data, delete confirmation dialog
- `OrderListPage`: Search query (local), view mode override (if not using Redux)

#### 3.1.3 Server State (RTK Query)

**Automatic Caching & Synchronization:**

- RTK Query manages API data fetching, caching, and invalidation
- Hooks like `useGetOrdersQuery` automatically:
  - Fetch data on mount
  - Cache results with unique keys (filters + pagination)
  - Refetch when dependencies change
  - Provide `isLoading`, `isFetching`, `error` states

**Tag-Based Invalidation:**

- Tags: `logistics/Order`, `logistics/InventoryItem`, `logistics/Shipment`, etc.
- Mutations automatically invalidate related tags to trigger refetches
- Example: Creating a shipment invalidates `logistics/Shipment:LIST` → shipment list refetches

---

### 3.2 API Endpoints Mapping

All API calls are routed through the API Gateway with `extraOptions: { service: 'logistics' }`, which prefixes URLs with `/logistics/api/v1`.

**Base Configuration:**

- Service: `logistics` (backend service on port 8089)
- Base Path: `/logistics/api/v1`
- Authentication: JWT via API Gateway
- Error Handling: Global error interceptor in RTK Query base config

#### 3.2.1 Address Endpoints

| Method | Hook                           | Endpoint                               | Purpose                    |
| ------ | ------------------------------ | -------------------------------------- | -------------------------- |
| POST   | `useCreateAddressMutation`     | `/address/create`                      | Create address for entity  |
| GET    | `useGetAddressesByEntityQuery` | `/address/search/by-entity/{entityId}` | Get addresses by entity ID |
| PATCH  | `useUpdateAddressMutation`     | `/address/update/{addressId}`          | Update address             |

#### 3.2.2 Category Endpoints

| Method | Hook                        | Endpoint                        | Purpose                     |
| ------ | --------------------------- | ------------------------------- | --------------------------- |
| GET    | `useGetCategoriesQuery`     | `/category/search`              | List categories (paginated) |
| GET    | `useGetCategoryQuery`       | `/category/search/{categoryId}` | Get category by ID          |
| POST   | `useCreateCategoryMutation` | `/category/create`              | Create category             |
| PATCH  | `useUpdateCategoryMutation` | `/category/update/{categoryId}` | Update category             |
| DELETE | `useDeleteCategoryMutation` | `/category/delete/{categoryId}` | Delete category             |

#### 3.2.3 Customer Endpoints (Read-Only)

| Method | Hook                   | Endpoint                        | Purpose                    |
| ------ | ---------------------- | ------------------------------- | -------------------------- |
| GET    | `useGetCustomersQuery` | `/customer/search`              | List customers (paginated) |
| GET    | `useGetCustomerQuery`  | `/customer/search/{customerId}` | Get customer by ID         |

#### 3.2.4 Facility Endpoints

| Method | Hook                        | Endpoint                        | Purpose                     |
| ------ | --------------------------- | ------------------------------- | --------------------------- |
| GET    | `useGetFacilitiesQuery`     | `/facility/search`              | List facilities (paginated) |
| GET    | `useGetFacilityQuery`       | `/facility/search/{facilityId}` | Get facility by ID          |
| POST   | `useCreateFacilityMutation` | `/facility/create`              | Create facility             |
| PATCH  | `useUpdateFacilityMutation` | `/facility/update/{facilityId}` | Update facility             |
| DELETE | `useDeleteFacilityMutation` | `/facility/delete/{facilityId}` | Delete facility             |

#### 3.2.5 Inventory Item Endpoints

| Method | Hook                             | Endpoint                          | Purpose                                    |
| ------ | -------------------------------- | --------------------------------- | ------------------------------------------ |
| GET    | `useGetInventoryItemsQuery`      | `/inventory-item/search`          | List inventory items (paginated, filtered) |
| GET    | `useGetInventoryItemQuery`       | `/inventory-item/search/{itemId}` | Get inventory item by ID                   |
| POST   | `useCreateInventoryItemMutation` | `/inventory-item/create`          | Create inventory item manually             |
| PATCH  | `useUpdateInventoryItemMutation` | `/inventory-item/update/{itemId}` | Update inventory item                      |
| DELETE | `useDeleteInventoryItemMutation` | `/inventory-item/delete/{itemId}` | Delete inventory item                      |

#### 3.2.6 Order Endpoints (Read-Only)

| Method | Hook                | Endpoint                  | Purpose                           |
| ------ | ------------------- | ------------------------- | --------------------------------- |
| GET    | `useGetOrdersQuery` | `/order/search`           | List orders (paginated, filtered) |
| GET    | `useGetOrderQuery`  | `/order/search/{orderId}` | Get order by ID with items        |

#### 3.2.7 Product Endpoints

| Method | Hook                       | Endpoint                      | Purpose                             |
| ------ | -------------------------- | ----------------------------- | ----------------------------------- |
| GET    | `useGetProductsQuery`      | `/product/search`             | List products (paginated, filtered) |
| GET    | `useGetProductQuery`       | `/product/search/{productId}` | Get product by ID                   |
| POST   | `useCreateProductMutation` | `/product/create`             | Create product                      |
| PATCH  | `useUpdateProductMutation` | `/product/update/{productId}` | Update product                      |
| DELETE | `useDeleteProductMutation` | `/product/delete/{productId}` | Delete product                      |

#### 3.2.8 Shipment Endpoints

| Method | Hook                                | Endpoint                                        | Purpose                              |
| ------ | ----------------------------------- | ----------------------------------------------- | ------------------------------------ |
| GET    | `useGetShipmentsQuery`              | `/shipment/search`                              | List shipments (paginated, filtered) |
| GET    | `useGetShipmentQuery`               | `/shipment/search/{shipmentId}`                 | Get shipment by ID with items        |
| POST   | `useCreateShipmentMutation`         | `/shipment/create`                              | Create shipment with items           |
| PATCH  | `useUpdateShipmentMutation`         | `/shipment/update/{shipmentId}`                 | Update shipment basic info           |
| DELETE | `useDeleteShipmentMutation`         | `/shipment/delete/{shipmentId}`                 | Delete shipment                      |
| POST   | `useAddItemToShipmentMutation`      | `/shipment/create/{shipmentId}/add`             | Add item to shipment                 |
| PATCH  | `useUpdateItemInShipmentMutation`   | `/shipment/update/{shipmentId}/update/{itemId}` | Update item in shipment              |
| PATCH  | `useDeleteItemFromShipmentMutation` | `/shipment/update/{shipmentId}/delete/{itemId}` | Remove item from shipment            |
| PATCH  | `useImportShipmentMutation`         | `/shipment/manage/{shipmentId}/import`          | Import shipment (creates inventory)  |

#### 3.2.9 Supplier Endpoints (Read-Only)

| Method | Hook                   | Endpoint                        | Purpose                    |
| ------ | ---------------------- | ------------------------------- | -------------------------- |
| GET    | `useGetSuppliersQuery` | `/supplier/search`              | List suppliers (paginated) |
| GET    | `useGetSupplierQuery`  | `/supplier/search/{supplierId}` | Get supplier by ID         |

**Note:** "Read-only" entities (Customer, Order, Supplier) are managed by other modules (CRM, Purchase) but exposed to Logistics for reference.

---

## 4. User Interaction Flows

### 4.1 Purchase Order Detail View Flow

**User Action:** User navigates to `/logistics/purchase-orders/{orderId}`

**Flow:**

1. **Route Initialization:**
   - Next.js renders `OrderDetailPage` component with `orderId` param
   - Component checks `LogisticsAuthGuard` → verifies user has Logistics module access

2. **Data Fetching (Parallel):**

   ```typescript
   const { data: orderResponse } = useGetOrderQuery(orderId);
   const order = orderResponse?.data;

   // Conditional fetches based on order data
   const { data: supplierResponse } = useGetSupplierQuery(order?.supplierId);
   const { data: shipmentsResponse } = useGetShipmentsQuery({
     filters: { orderId },
   });
   const { data: usersResponse } = useGetUsersQuery({
     userIds: [order?.createdBy, order?.approvedBy],
   });
   ```

3. **Render States:**
   - `isLoading` → Show skeleton loaders
   - `error || !order` → Show "Order not found" error card with back button
   - Success → Render order details with tabs

4. **Tab Navigation:**
   - **Order Details Tab:**
     - Displays supplier info card (avatar, contact, address)
     - Shows order items table (product image, name, SKU, quantity, price)
     - Calculates total: `items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)`
   - **Delivery History Tab:**
     - Renders `ShipmentCard` for each related shipment
     - Click on card → Navigate to `/logistics/shipments/{shipmentId}`

5. **User Profile Display:**
   - Fetches user data via `useGetUsersQuery` from Admin module
   - Displays `UserProfile` component for creator and approver (if exists)

**Key Logic:**

- Orders are read-only in Logistics module (created in Purchase module)
- Conditional data fetching prevents unnecessary API calls
- Automatic refetch when `orderId` changes (RTK Query dependency)

---

### 4.2 Inventory Item Creation Flow

**User Action:** User clicks "Create New Item" button in Inventory List → navigates to `/logistics/inventory/new`

**Flow:**

1. **Page Load:**
   - `CreateInventoryItemPage` initializes with empty form state
   - Fetches initial product and facility lists (first 20 items)

2. **Product Selection:**

   ```typescript
   // User types in search box
   setProductSearch(e.target.value) → triggers useGetProductsQuery refetch

   // User clicks product from dropdown
   setFormData({ ...formData, productId: product.id })
   setShowProductDropdown(false)

   // Display selected product card
   selectedProduct = products.find(p => p.id === formData.productId)
   ```

3. **Facility Selection:**
   - Similar flow to product selection
   - Searchable dropdown with real-time filtering

4. **Form Validation (on Submit):**

   ```typescript
   validateForm():
   - Product required: if (!formData.productId) → error
   - Facility required: if (!formData.facilityId) → error
   - Lot ID required: if (!formData.lotId.trim()) → error
   - Quantity validation: if (formData.quantity <= 0) → error "Số lượng phải lớn hơn 0"
   ```

5. **Submission:**

   ```typescript
   handleSubmit():
   1. Call validateForm() → if fails, show toast error and return
   2. Call createInventoryItem mutation:
      await createItem(formData).unwrap()
   3. On success:
      - Show toast: "Nhập kho thành công"
      - Navigate to: router.push('/logistics/inventory')
      - RTK Query invalidates 'logistics/InventoryItem:LIST' → list refetches
   4. On error:
      - Show toast: "Không thể nhập kho. Vui lòng thử lại."
      - Log error to console
   ```

6. **Cache Invalidation:**
   - Mutation invalidates `[{ type: 'logistics/InventoryItem', id: 'LIST' }]`
   - Inventory list page automatically refetches if mounted
   - New item appears in list without manual refresh

**Key Features:**

- Debounced search (implicit via RTK Query caching)
- Dropdown closes on selection
- Error messages display inline below each field
- Submit button disabled during submission (`isCreating` state)

---

### 4.3 Shipment Creation & Import Flow

**User Action:** User creates inbound shipment from approved purchase order

**Flow:**

1. **Navigation:**
   - From Order Detail → Click "Create Shipment" (future feature)
   - OR from Shipments List → Click "New Shipment" → redirects to `/logistics/shipments/new`

2. **Shipment Creation Page:**
   - Renders `InboundShipmentForm` component
   - Requires `orderId` query parameter (validates order exists and is APPROVED)

3. **Order Validation:**

   ```typescript
   const { data: orderResponse } = useGetOrderQuery(orderId);

   // Guard conditions:
   if (!orderId) → Show "Missing order information" error
   if (orderError || !order) → Show "Order not found" error
   if (order.statusId !== 'APPROVED') → Show "Order not approved yet" error
   ```

4. **Form Filling:**
   - **Shipment Name:** User enters name (required, e.g., "Shipment PO-001-01")
   - **Expected Delivery Date:** User selects date (required)
   - **Note:** Optional text area

5. **Adding Items from Order:**

   ```typescript
   // User searches for order item by product name
   const availableOrderItems = order.items?.filter(item => item.quantityRemaining > 0)

   // User clicks "Add" on an order item
   addItem(orderItemId):
   1. Find order item: orderItem = order.items.find(oi => oi.id === orderItemId)
   2. Check availability: if (orderItem.quantityRemaining <= 0) → toast warning
   3. Add to items array with initial values:
      {
        orderItemId: orderItem.id,
        quantity: 1,
        lotId: '',          // User must fill
        facilityId: '',     // User must select
        note: '',
        expirationDate: '',
        manufacturingDate: ''
      }
   ```

6. **Item Configuration:**
   - **Facility:** User selects warehouse (required)
   - **Quantity:** User enters quantity (required, max: `orderItem.quantityRemaining`)
   - **Weight:** Optional
   - **Lot Number:** User enters (required)
   - **Dates:** Manufacturing and expiry dates (optional)

7. **Validation (on Submit):**

   ```typescript
   validateForm():
   - Shipment name required: if (!formData.shipmentName.trim()) → error
   - Expected delivery date required: if (!formData.expectedDeliveryDate) → error
   - At least one item: if (items.length === 0) → error

   For each item:
   - Order item selected: if (!item.orderItemId) → error
   - Lot number: if (!item.lotId.trim()) → error
   - Facility: if (!item.facilityId) → error
   - Quantity > 0: if (item.quantity <= 0) → error
   - Quantity within limit: if (item.quantity > orderItem.quantityRemaining) → error
   ```

8. **Submission:**

   ```typescript
   handleSubmit():
   1. Validate form → if fails, stop
   2. Construct payload:
      {
        orderId: orderId,
        shipmentName: formData.shipmentName,
        note: formData.note,
        expectedDeliveryDate: formData.expectedDeliveryDate,
        items: items  // Array of ShipmentItemForm
      }
   3. Call API: await createShipment(payload).unwrap()
   4. On success:
      - Toast: "Tạo phiếu nhập thành công"
      - Navigate to shipment detail page
   ```

9. **Shipment Import (from Detail Page):**
   ```typescript
   // User clicks "Import Shipment" action
   handleImport():
   1. Show confirmation dialog: "Import this shipment to inventory?"
   2. User confirms
   3. Call API: await importShipment({ shipmentId }).unwrap()
   4. Backend logic:
      - Changes shipment status: CREATED → IMPORTED
      - Creates inventory items for each shipment item
      - Updates order delivery progress
   5. On success:
      - Toast: "Nhập kho thành công"
      - Refetch shipment data
      - Invalidates inventory items list
   6. Shipment status badge updates to "Đã nhập kho"
   ```

**Critical Business Rules:**

- Only APPROVED orders can have shipments
- Cannot import shipment twice (status check)
- Quantity in shipment cannot exceed remaining quantity in order
- Import operation is transactional (all-or-nothing)

---

### 4.4 Inventory Item Edit & Delete Flow

**User Action:** User views inventory item detail and clicks "Edit"

**Flow:**

1. **Edit Mode Activation:**

   ```typescript
   // Click "Edit" button
   setIsEditing(true);
   setEditData({
     quantity: item.quantityOnHand,
     statusId: item.statusId,
     manufacturingDate: formatDateForInput(item.manufacturingDate),
     expirationDate: formatDateForInput(item.expirationDate),
   });
   ```

2. **Inline Editing:**
   - Form fields become editable inputs (previously read-only cards)
   - User can change:
     - Available quantity (number input)
     - Status (dropdown: Valid/Expired/Damaged)
     - Manufacturing date (date picker)
     - Expiry date (date picker)

3. **Save Changes:**

   ```typescript
   handleUpdate():
   1. Call API: await updateInventoryItem({
        inventoryItemId: itemId,
        data: editData
      }).unwrap()
   2. On success:
      - Toast: "Cập nhật tồn kho thành công"
      - Exit edit mode: setIsEditing(false)
      - RTK Query refetches item data → UI updates automatically
   3. On error:
      - Toast: "Không thể cập nhật tồn kho"
      - Keep edit mode open
   ```

4. **Cancel Edit:**

   ```typescript
   handleCancelEdit():
   - Revert editData to original item values
   - Exit edit mode: setIsEditing(false)
   ```

5. **Delete Flow:**

   ```typescript
   // Click "Delete" in dropdown menu
   setShowDeleteDialog(true)  // Show confirmation AlertDialog

   // User confirms deletion
   handleDelete():
   1. Call API: await deleteInventoryItem(itemId).unwrap()
   2. On success:
      - Toast: "Xóa tồn kho thành công"
      - Navigate back: router.push('/logistics/inventory')
      - Item removed from list (cache invalidation)
   3. On error:
      - Toast: "Không thể xóa tồn kho"
      - Dialog closes, item remains
   ```

**Key Features:**

- Optimistic UI: Edit mode provides instant feedback
- Confirmation dialog prevents accidental deletion
- No manual refetch needed (RTK Query handles it)
- Form validation on save (quantity must be >= 0)

---

### 4.5 Filtering & Pagination Flow

**User Action:** User applies filters or changes page in any list view

**Flow:**

1. **Filter Application:**

   ```typescript
   // User selects filter (e.g., status in OrderListPage)
   const handleStatusChange = (statusId: string) => {
     dispatch(
       setOrderFilters({
         ...filters,
         statusId: statusId || undefined,
       })
     );
   };

   // Redux action updates store:
   state.logistics.orders.filters = { ...oldFilters, statusId: 'APPROVED' };
   ```

2. **Automatic Refetch:**

   ```typescript
   // Component reads filters from Redux
   const filters = useAppSelector(selectOrderFilters);
   const pagination = useAppSelector(selectOrderPagination);

   // RTK Query hook watches filters
   const { data } = useGetOrdersQuery({ filters, pagination });

   // When filters change → RTK Query detects new cache key → refetches automatically
   ```

3. **Loading State:**
   - `isFetching` becomes `true` during refetch
   - List shows loading overlay or skeleton
   - Previous data remains visible (stale-while-revalidate pattern)

4. **Pagination:**

   ```typescript
   // User clicks "Next Page"
   dispatch(
     setOrderPagination({
       ...pagination,
       page: pagination.page + 1,
     })
   );

   // Triggers refetch with new page parameter
   // URL updates to reflect current page (future enhancement)
   ```

5. **Search vs Filter:**
   - **Local Search (not Redux):** Temporary state, debounced, triggers filter update on Enter or blur
   - **Redux Filter:** Persisted in store, used in API query immediately

**Performance Optimization:**

- RTK Query caches results per unique `{ filters, pagination }` combination
- Switching between filters reuses cached data if available
- Prevents duplicate requests within cache lifetime (default 60s)

---

## 5. Validation & Business Rules

### 5.1 Frontend Validation Rules

#### 5.1.1 Inventory Item Creation/Update

**Required Fields:**

- ✅ Product: Must select from dropdown (not empty string)
- ✅ Facility: Must select from dropdown
- ✅ Lot Number: Non-empty string after trimming
- ✅ Quantity: Must be > 0

**Validation Messages:**

```typescript
productId: 'Vui lòng chọn sản phẩm';
facilityId: 'Vui lòng chọn kho';
lotId: 'Vui lòng nhập mã lô hàng';
quantity: 'Số lượng phải lớn hơn 0';
```

**Optional Fields:**

- Manufacturing Date (string, ISO format)
- Expiry Date (string, ISO format)
- Status (defaults to 'VALID')

**Date Validation:**

- Dates must be valid ISO strings (YYYY-MM-DD)
- No frontend validation for manufacturing < expiry (backend handles)

---

#### 5.1.2 Shipment Creation

**Required Fields:**

- ✅ Order ID: Must exist and have status 'APPROVED'
- ✅ Shipment Name: Non-empty string after trimming
- ✅ Expected Delivery Date: Valid date string
- ✅ Items: Array must have at least 1 item

**Validation Messages:**

```typescript
shipmentName: 'Vui lòng nhập tên phiếu nhập';
expectedDeliveryDate: 'Vui lòng chọn ngày dự kiến giao hàng';
items: 'Vui lòng thêm ít nhất một sản phẩm';
```

**Per-Item Validation:**

```typescript
orderItemId: 'Chưa chọn sản phẩm';
lotId: 'Vui lòng nhập lô hàng';
facilityId: 'Vui lòng chọn kho';
quantity: 'Số lượng phải > 0';
quantity_max: 'Số lượng vượt quá số lượng còn lại trong đơn hàng';
```

**Quantity Rules:**

- Must be > 0
- Must be ≤ `orderItem.quantityRemaining`
- Example: Order has 100 units, 60 already shipped → max 40 for new shipment

**Guard Conditions:**

- Order must exist: `if (!order) → block form`
- Order must be approved: `if (order.statusId !== 'APPROVED') → block form`
- Order items must have remaining quantity: `quantityRemaining > 0`

---

#### 5.1.3 Shipment Item Update (in Detail Page)

**Validation:**

- Quantity: `1 ≤ quantity ≤ orderItem.quantityRemaining`
- Facility: Required
- Lot Number: Required, non-empty
- Weight: Optional, must be ≥ 0 if provided
- Dates: Optional, valid ISO format

**Real-time Validation:**

- Quantity input shows error if exceeds limit
- Facility dropdown shows error if not selected
- Save button disabled if validation fails

---

### 5.2 Business Rules

#### 5.2.1 Order Status Workflow

**Read-Only in Logistics:**

- Orders are created and managed in Purchase Service
- Logistics can only VIEW orders (no create/update/delete)

**Status Flow:**

1. **CREATED** → Initial state (not visible in Logistics)
2. **APPROVED** → Ready for shipments (visible in Logistics)
3. **FULLY_DELIVERED** → All items received (automatic status)
4. **CANCELLED** → Order cancelled (no shipments allowed)

**Delivery Progress:**

- Backend calculates: `totalQuantityDelivered / totalQuantityOrdered`
- Status changes to FULLY_DELIVERED when all items are 100% delivered
- Shown in Order Detail page (future enhancement: progress bar)

---

#### 5.2.2 Inventory Status Rules

**Status Types:**

- **VALID**: Normal inventory, available for allocation
- **EXPIRED**: Past expiry date, cannot be used
- **DAMAGED**: Physically damaged, cannot be used

**Frontend Indicators:**

- Color-coded badges: Green (Valid), Red (Expired), Amber (Damaged)
- Icons: CheckCircle2 (Valid), XCircle (Expired), AlertTriangle (Damaged)
- Warning badge: "Sắp hết hạn" if expiry within 30 days

**Status Change:**

- Users can manually change status in Edit mode
- Backend may auto-update status based on expiry date (future feature)

---

#### 5.2.3 Shipment Import Rules

**Pre-conditions:**

- Shipment must have status 'CREATED' (draft)
- Cannot import already-imported shipments (status = 'IMPORTED')
- All items must have:
  - Valid facility ID
  - Positive quantity
  - Lot number

**Import Operation (Backend):**

1. Validate shipment is in CREATED status
2. For each shipment item:
   - Create inventory item with:
     - `productId` from order item
     - `facilityId` from shipment item
     - `quantityOnHand` = shipment item quantity
     - `lotId`, `expirationDate`, `manufacturingDate` from shipment item
     - `statusId` = 'VALID' (default)
3. Update order item:
   - `quantityDelivered` += shipment item quantity
   - `quantityRemaining` -= shipment item quantity
4. Check if order is fully delivered:
   - If all items have `quantityRemaining = 0` → set order status to 'FULLY_DELIVERED'
5. Update shipment status to 'IMPORTED'

**Transaction Safety:**

- Entire operation is atomic (all-or-nothing)
- If any step fails, rollback all changes
- Frontend cannot trigger duplicate imports (button disabled after first import)

---

#### 5.2.4 Filter & Search Behavior

**Search:**

- **Product Search:** Searches by product name and SKU (case-insensitive)
- **Supplier Search:** Searches by supplier name (case-insensitive)
- **Facility Search:** Searches by facility name (case-insensitive)

**Filters:**

- **Status Filters:** Exact match (e.g., `statusId=APPROVED`)
- **Entity Filters:** Foreign key match (e.g., `supplierId=123`)
- **Date Filters:** Range queries (e.g., `createdStampFrom`, `createdStampTo`)

**Pagination:**

- Default page size: 10 items
- Page index: 0-based (page 0 = first page)
- Sort: Default `createdStamp DESC` (newest first)
- User can change page size: 10, 20, 50, 100

**Filter Persistence:**

- Filters persist in Redux store during session
- Cleared on page refresh (unless saved to localStorage - future feature)
- Each entity has independent filters (order filters ≠ inventory filters)

---

#### 5.2.5 Permission & Access Control

**Module-Level:**

- `LogisticsAuthGuard` checks user has access to Logistics module
- Redirects to 403 or home if unauthorized

**Route-Level:**

- `RouteGuard` with `moduleCode='LOGISTICS'` on every page
- Checks specific route permissions (future enhancement)

**Action-Level (Backend):**

- Create/Update/Delete require specific permissions
- Managed by Keycloak roles (e.g., `LOGISTICS_ADMIN`, `LOGISTICS_USER`)
- Frontend shows/hides buttons based on user role (future feature)

---

### 5.3 Error Handling Patterns

**API Errors:**

```typescript
try {
  await mutation(data).unwrap();
  toast.success('Success message');
} catch (error: any) {
  toast.error(error?.data?.message || 'Generic error message');
  console.error('Error details:', error);
}
```

**Validation Errors:**

- Displayed inline below form fields
- Red border on invalid inputs
- Submit button remains enabled (validation on submit)

**Network Errors:**

- RTK Query shows `error` state
- Component renders error card with retry button
- Example: "Không thể tải dữ liệu. Vui lòng thử lại."

**Not Found Errors:**

- 404 responses render "Not found" card
- Back button to return to list
- Example: "Không tìm thấy đơn hàng"
