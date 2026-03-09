# Sales Module Documentation

**Author:** KienPT  
**Generated:** January 16, 2026

---

## 1. Module Overview

### 1.1 Functional Purpose

The **Sales** module is a comprehensive sales management system within the SERP ERP application. It manages the complete sales lifecycle including:

- **Sales Orders** - Create, track, approve, cancel, and deliver sales orders
- **Product Catalog** - Manage products with categories, pricing, and descriptions
- **Customer Management** - Maintain customer records, contacts, and addresses
- **Inventory Tracking** - Monitor stock levels across multiple facilities with status tracking (valid, expired, damaged)
- **Facilities** - Manage warehouses and storage locations
- **Dashboard Analytics** - Overview of sales performance, order metrics, and key statistics

### 1.2 Key User Actions

**Sales Orders:**

- Create new orders with customer selection and product line items
- View order list with advanced filtering (status, customer, date range, priority)
- View detailed order information with product items, pricing, and history
- Edit existing orders (items, quantities, prices)
- Approve orders (change status from CREATED → APPROVED)
- Cancel orders with cancellation notes
- Add/remove products to/from orders
- Track order delivery status

**Products:**

- Browse product catalog with search and filters
- View product details (name, description, price, category, status)
- Create new products
- Update product information
- Delete products

**Customers:**

- List customers with pagination and filters (status, query)
- View customer details with full profile and addresses
- Create new customer records
- Edit customer information
- Delete customers
- Manage customer addresses (shipping, business)

**Inventory:**

- View inventory items across all facilities
- Filter by status (VALID, EXPIRED, DAMAGED)
- Track product quantities at specific facilities
- View facility locations
- Monitor stock expiration dates
- Create and edit inventory items

**Dashboard:**

- View sales overview statistics
- Monitor total revenue trends
- Track active orders count
- Review product and customer metrics
- Access quick links to recent orders

---

## 2. Component Architecture

### 2.1 Component Hierarchy

```
SalesLayout (Root Layout)
├── SalesAuthGuard (Authentication wrapper)
├── SidebarProvider (Sidebar state management)
├── DynamicSidebar (moduleCode='SALES')
│   └── Navigation items (Dashboard, Orders, Products, Customers, Inventory)
└── SalesLayoutContent
    ├── SalesHeader (Top header with breadcrumbs)
    └── RouteGuard (moduleCode='SALES')
        └── Page Components (children)

Page Components Hierarchy:

SalesDashboardPage
├── StatsCard (x4 - Revenue, Orders, Products, Customers)
├── Card (Recent Orders)
└── Card (Top Products)

OrderListPage
├── StatsCard (x6 - Total, Created, Approved, Delivered, Cancelled, Revenue)
├── FilterSection
│   ├── Input (Search)
│   ├── Select (Status filter)
│   ├── Select (Customer filter)
│   ├── DatePicker (From date)
│   ├── DatePicker (To date)
│   └── Select (Priority filter)
├── ViewToggle (Grid/List)
├── OrderCard[] (Grid view)
│   └── For each order:
│       ├── Badge (Status)
│       ├── Customer info display
│       ├── Amount display
│       └── Metadata (date, priority)
└── Pagination controls

OrderDetailPage
├── Header section
│   ├── Back button
│   ├── Order title & ID
│   ├── Status badge
│   └── Action menu (Edit, Approve, Cancel, Delete)
├── Tabs
│   ├── Tab: Overview
│   │   ├── Card (Order Information)
│   │   │   ├── OrderName, OrderDate, Priority
│   │   │   ├── Delivery dates
│   │   │   ├── Sale Channel
│   │   │   └── Note
│   │   ├── Card (Customer Information)
│   │   │   ├── Customer name
│   │   │   ├── Contact info (phone, email)
│   │   │   └── Address
│   │   ├── Card (Order Items) [Editable]
│   │   │   ├── Table (Product, Quantity, Price, Tax, Discount, Total)
│   │   │   ├── Add product button + search
│   │   │   └── Delete item buttons
│   │   └── Card (Financial Summary)
│   │       ├── Subtotal
│   │       ├── Total Tax
│   │       ├── Total Discount
│   │       └── Grand Total
│   ├── Tab: History
│   │   └── Timeline of order events (created, approved, cancelled, delivered)
│   └── Tab: Documents
│       └── Related documents/attachments
└── Cancel Dialog (when cancelling)
    ├── Textarea (Cancellation note)
    └── Confirm/Cancel buttons

CreateOrderPage / EditOrderPage
└── OrderForm
    ├── Card (Order Information)
    │   ├── Input (Order name)
    │   ├── Select (Sale channel: ONLINE, PARTNER, RETAIL)
    │   ├── Select (Priority: 1-5)
    │   ├── DatePicker (Delivery before date)
    │   ├── DatePicker (Delivery after date)
    │   └── Textarea (Note)
    ├── Card (Customer Selection)
    │   └── SearchableSelect (Customer dropdown with search)
    ├── Card (Order Items)
    │   ├── Table (Product, Quantity, Unit Price, Tax %, Discount %, Total)
    │   ├── Add item button
    │   ├── Product search dropdown
    │   └── Remove item buttons
    ├── Card (Summary)
    │   ├── Subtotal calculation
    │   ├── Total tax
    │   ├── Total discount
    │   └── Grand total (bold)
    └── Action buttons (Save, Cancel)

ProductListPage
├── Header (Title + Add Product button)
├── Card (Product Catalog)
│   ├── Search bar
│   ├── Filter button
│   ├── Table
│   │   └── Rows (Product name, Category, Price, Status, Actions)
│   └── Pagination controls
└── Filters panel (collapsible)

CustomerListPage
├── StatsCard (x3 - Total, Active, Inactive)
├── FilterSection
│   ├── Input (Search)
│   └── Select (Status filter)
├── ViewToggle (Grid/List)
├── CustomerCard[] (Grid view)
│   └── For each customer:
│       ├── Customer name
│       ├── Contact info
│       ├── Status badge
│       └── Action buttons (View, Edit, Delete)
└── Pagination controls

CustomerDetailPage / CreateCustomerPage / EditCustomerPage
└── CustomerForm
    ├── Card (Basic Information)
    │   ├── Input (First name, Last name)
    │   ├── Input (Email, Phone)
    │   ├── Select (Status)
    │   └── Textarea (Note)
    └── Card (Addresses)
        └── AddressForm[] (multiple addresses)
            ├── Select (Address type)
            ├── Input (Full address)
            ├── Input (Latitude, Longitude)
            └── Checkbox (Default address)

InventoryListPage
├── StatsCard (x4 - Total Items, Valid, Expired, Damaged)
├── FilterSection
│   ├── Input (Search)
│   ├── Select (Status filter)
│   ├── Select (Product filter)
│   └── Select (Facility filter)
├── ViewToggle (Grid/List)
├── InventoryItemCard[] (Grid view)
│   └── For each item:
│       ├── Product name
│       ├── Facility location
│       ├── Quantity
│       ├── Status badge
│       ├── Expiry date
│       └── Action buttons
└── Pagination controls

InventoryDetailPage / CreateInventoryItemPage
└── InventoryItemForm
    ├── Card (Item Information)
    │   ├── Select (Product)
    │   ├── Select (Facility)
    │   ├── Input (Quantity)
    │   ├── Select (Status: VALID, EXPIRED, DAMAGED)
    │   ├── DatePicker (Manufactured date)
    │   └── DatePicker (Expiry date)
    └── Action buttons (Save, Cancel)
```

### 2.2 Component Breakdown

#### **Page-Level Components** (`src/modules/sales/pages/`)

**1. Dashboard**

- **SalesDashboardPage**: Main entry point displaying key sales metrics and summaries. Shows 4 stat cards (revenue, orders, products, customers) with trend indicators, recent orders list, and top products display.

**2. Orders**

- **OrderListPage**: Primary order management interface with advanced filtering (status, customer, date range, priority), grid/list view toggle, and pagination. Displays orders as cards with status indicators, customer info, and quick actions.
- **OrderDetailPage**: Comprehensive single-order view with tabbed interface (Overview, History, Documents). Allows inline editing of order items, approval/cancellation workflows, and displays detailed financial breakdown.
- **CreateOrderPage**: New order creation wrapper that initializes the OrderForm and handles submission to the API.
- **EditOrderPage**: Order editing wrapper that loads existing order data into OrderForm for modification.

**3. Products**

- **ProductListPage**: Product catalog browser with search functionality, status filters, and tabular display. Shows product details (name, category, price, status) with action buttons for CRUD operations.

**4. Customers**

- **CustomerListPage**: Customer directory with search, status filtering, and grid/list views. Displays customer cards with contact information, status badges, and quick action buttons.
- **CustomerDetailPage**: Single customer profile view showing full details, contact information, and associated addresses.
- **CreateCustomerPage**: Wrapper for customer creation using CustomerForm.
- **EditCustomerPage**: Wrapper for customer editing using CustomerForm with pre-populated data.

**5. Inventory**

- **InventoryListPage**: Stock management interface tracking inventory across multiple facilities. Provides filtering by status (VALID, EXPIRED, DAMAGED), product, and facility. Displays quantity, expiry dates, and facility locations.
- **InventoryDetailPage**: Single inventory item view with full details.
- **CreateInventoryItemPage**: Form for adding new inventory items with product, facility, quantity, and expiry date selection.

#### **Form Components** (`src/modules/sales/components/forms/`)

**OrderForm**

- Complex form handling order creation/editing with dynamic product line items
- Customer selection with searchable dropdown
- Sale channel and priority configuration
- Delivery date management (before/after dates)
- Dynamic item addition/removal with real-time total calculations
- Automatic price calculations including tax and discounts
- **Props:** `order` (for edit mode), `customerId` (pre-selected customer), `onSubmit`, `onCancel`

**CustomerForm**

- Customer profile management (name, email, phone, status)
- Multiple address management with type selection
- Default address designation
- Address geolocation support (latitude/longitude)
- **Props:** `customer` (for edit mode), `onSubmit`, `onCancel`

#### **Card Components** (`src/modules/sales/components/cards/`)

**OrderCard**

- Compact order display for list views
- Visual status indicator with color-coded badges
- Customer information preview
- Total amount display with currency formatting
- Order metadata (date, ID, priority)
- Gradient accent header for visual appeal
- Hover effects for interactivity
- **Props:** `order`, `customer` (optional), `onClick`

**StatsCard** (Inline component used in multiple pages)

- KPI display with icon and value
- Variant-based styling (primary, success, warning, danger, completed)
- Background gradient effects
- Animated hover states
- Shadow and ring decorations
- **Props:** `title`, `value`, `icon`, `variant`

#### **Layout Components** (`src/modules/sales/components/layout/`)

**SalesLayout**

- Root layout wrapper for all Sales module pages
- Integrates SalesAuthGuard for permission checking
- Manages sidebar state with SidebarProvider
- Responsive layout with dynamic sidebar (collapsed/expanded states)
- Smooth transitions on sidebar toggle
- **Children:** Wraps all page content

**SalesHeader**

- Top navigation bar with breadcrumb trail
- Module title display
- Scroll-aware styling (shadows on scroll)
- **Props:** `scrollContainerRef` (for scroll detection)

**SalesAuthGuard**

- Authentication and authorization guard
- Checks user permissions for SALES module access
- Redirects unauthorized users
- Displays loading state during auth checks

### 2.3 Shared UI Elements

The Sales module leverages the following shared ERP components from `@/shared/components/ui`:

**Data Display:**

- **Card / CardContent / CardHeader / CardTitle**: Primary container components for content sections
- **Badge**: Status indicators with variant support (order status, customer status, product status)
- **Table / TableBody / TableCell / TableHead / TableHeader / TableRow**: Structured data display for products, inventory, and order items

**Form Controls:**

- **Input**: Text fields for names, emails, searches, quantities
- **Label**: Form field labels
- **Button**: Action triggers with icon support (Primary actions, secondary actions, danger actions)
- **Textarea**: Multi-line inputs for notes and descriptions
- **Select / SelectContent / SelectItem / SelectTrigger**: Dropdown selections for status, priority, filters

**Navigation:**

- **Tabs / TabsContent / TabsList / TabsTrigger**: Content organization in detail pages (OrderDetailPage uses 3 tabs)
- **DropdownMenu / DropdownMenuContent / DropdownMenuItem / DropdownMenuSeparator / DropdownMenuTrigger**: Action menus (Edit, Delete, Approve, Cancel)

**Layout:**

- **DynamicSidebar**: Module navigation with collapsible state (moduleCode='SALES')
- **RouteGuard**: Permission-based route protection (moduleCode='SALES')
- **SidebarProvider**: Sidebar state management context

**Utilities:**

- **Icons from Lucide React**: 30+ icons used throughout (Search, Plus, Edit, Trash2, Package, ShoppingCart, Calendar, User, etc.)
- **cn() utility**: Conditional className merging for dynamic styling
- **Toast notifications (Sonner)**: Success/error feedback on actions

**Smart Features:**

- Searchable dropdowns with real-time filtering (customer selection, product selection)
- Pagination controls with page indicators
- View toggles (Grid/List) for flexible data display
- Date pickers for delivery dates and filters
- Real-time calculations in forms (totals, tax, discounts)
- Responsive tables with horizontal scroll on mobile
- Loading states during data fetching
- Error boundaries for graceful failure handling

---

## 3. State Management & Data Integration

### 3.1 State Architecture Overview

The Sales module uses a **hybrid state management approach** combining:

- **RTK Query** for server state (API data caching, automatic refetching)
- **Redux Toolkit** for client state (filters, pagination, UI preferences)
- **Local Component State** (React useState) for ephemeral UI state (form inputs, dialogs, search)

### 3.2 Global State (Redux Store)

**Store Location:** `src/modules/sales/store/salesSlice.ts`

The `salesSlice` manages six entity collections, each with consistent structure:

```typescript
SalesState {
  ui: {
    activeModule: 'dashboard' | 'customers' | 'products' | 'orders' | 'categories' | 'facilities' | 'inventory'
    viewMode: 'list' | 'grid' | 'kanban'
    sidebarCollapsed: boolean
    filterPanelOpen: boolean
    bulkActionMode: boolean
    selectedItems: string[]  // Global selection across modules
  }

  customers: {
    items: Customer[]        // Cache (not used in RTK Query pattern)
    selectedItems: string[]  // Selected customer IDs
    filters: CustomerFilters // { query?, statusId? }
    pagination: { page, size, sortBy, sortDirection }
    loading: boolean
    error: string | null
    total: number
  }

  products: { ... }    // Same structure as customers
  orders: { ... }      // Same structure as customers
  categories: { ... }  // Same structure as customers
  facilities: { ... }  // Same structure as customers
  inventoryItems: { ... } // Same structure as customers
}
```

**Key Redux Actions:**

- **Filters:** `setOrderFilters`, `setCustomerFilters`, `setProductFilters`, etc.
- **Pagination:** `setOrderPagination`, `setCustomerPagination`, etc.
- **Selection:** `toggleOrderSelection`, `clearOrderSelection`, etc.
- **UI:** `setViewMode`, `toggleSidebar`, `toggleFilterPanel`, `setActiveModule`
- **Reset:** `resetOrderState`, `resetAllState`

**Usage Pattern:**

```typescript
// In OrderListPage component
const dispatch = useAppDispatch();
const filters = useAppSelector(selectOrderFilters);
const pagination = useAppSelector(selectOrderPagination);

// Update filters (resets page to 0 automatically)
dispatch(setOrderFilters({ statusId: 'APPROVED' }));

// Update pagination
dispatch(setOrderPagination({ page: 2, size: 20 }));
```

**Selectors:** `src/modules/sales/store/selectors.ts`

- Base: `selectSales`, `selectOrders`, `selectCustomers`, `selectProducts`
- Pagination: `selectOrderPagination`, `selectCustomerPagination`
- Filters: `selectOrderFilters`, `selectCustomerFilters`
- UI: `selectViewMode`, `selectActiveModule`, `selectIsSidebarCollapsed`

### 3.3 Server State (RTK Query)

**API Configuration:** `src/modules/sales/api/salesApi.ts`

All endpoints use `api.injectEndpoints()` with `extraOptions: { service: 'sales' }` for automatic routing through API Gateway to the Sales service.

**Cache Tags for Automatic Invalidation:**

- `SalesCustomer`: Individual customer or list
- `Product`: Individual product or list
- `Order`: Individual order or list
- `Category`, `Facility`, `InventoryItem`, `Address`: Same pattern

**Example Invalidation Flow:**

```typescript
// When creating a new order:
createOrder: builder.mutation({
  // ...
  invalidatesTags: [{ type: 'Order', id: 'LIST' }], // Refetch order lists
});

// When updating an order:
updateOrder: builder.mutation({
  // ...
  invalidatesTags: (result, error, { orderId }) => [
    { type: 'Order', id: orderId }, // Refetch this specific order
    { type: 'Order', id: 'LIST' }, // Refetch order lists
  ],
});
```

### 3.4 Local Component State

**OrderForm Component:**

```typescript
// Form field state (controlled inputs)
const [customerId, setCustomerId] = useState('');
const [orderName, setOrderName] = useState('');
const [items, setItems] = useState<OrderItem[]>([]);

// UI interaction state
const [showCustomerDropdown, setShowCustomerDropdown] = useState(false);
const [customerSearch, setCustomerSearch] = useState('');
const [isSubmitting, setIsSubmitting] = useState(false);
const [errors, setErrors] = useState<Record<string, string>>({});
```

**OrderDetailPage Component:**

```typescript
const [activeTab, setActiveTab] = useState('overview'); // Tab navigation
const [showCancelDialog, setShowCancelDialog] = useState(false);
const [showAddItemDialog, setShowAddItemDialog] = useState(false);
const [editingItemId, setEditingItemId] = useState<string | null>(null);
```

**Rule:** Use local state for:

- Form inputs (before submission)
- Dialog/modal visibility
- Dropdown open/close state
- Active tab selection
- Loading/submitting flags

---

## 4. API Mapping & Endpoints

All requests route through **API Gateway** (`/sales/api/v1/*`) with JWT authentication.

### 4.1 Customer Endpoints

| Hook                        | Method | Path                    | Purpose                                  |
| --------------------------- | ------ | ----------------------- | ---------------------------------------- |
| `useGetCustomersQuery`      | GET    | `/customer/search`      | List customers with filters & pagination |
| `useGetCustomerQuery`       | GET    | `/customer/search/{id}` | Get single customer detail               |
| `useCreateCustomerMutation` | POST   | `/customer/create`      | Create new customer                      |
| `useUpdateCustomerMutation` | PATCH  | `/customer/update/{id}` | Update customer info                     |
| `useDeleteCustomerMutation` | DELETE | `/customer/delete/{id}` | Delete customer                          |

**Query Params (GET /customer/search):**

- `query`: Search term (name, email, phone)
- `statusId`: ACTIVE | INACTIVE
- `page`, `size`, `sortBy`, `sortDirection`: Pagination

### 4.2 Product Endpoints

| Hook                       | Method | Path                   | Purpose                    |
| -------------------------- | ------ | ---------------------- | -------------------------- |
| `useGetProductsQuery`      | GET    | `/product/search`      | List products with filters |
| `useGetProductQuery`       | GET    | `/product/search/{id}` | Get product detail         |
| `useCreateProductMutation` | POST   | `/product/create`      | Create new product         |
| `useUpdateProductMutation` | PATCH  | `/product/update/{id}` | Update product             |
| `useDeleteProductMutation` | DELETE | `/product/delete/{id}` | Delete product             |

**Query Params (GET /product/search):**

- `query`: Search term
- `categoryId`: Filter by category
- `statusId`: ACTIVE | INACTIVE
- `page`, `size`, `sortBy`, `sortDirection`

### 4.3 Order Endpoints

| Hook                                | Method | Path                                      | Purpose                            |
| ----------------------------------- | ------ | ----------------------------------------- | ---------------------------------- |
| `useGetOrdersQuery`                 | GET    | `/order/search`                           | List orders with filters           |
| `useGetOrderQuery`                  | GET    | `/order/search/{id}`                      | Get order detail with items        |
| `useCreateOrderMutation`            | POST   | `/order/create`                           | Create order with items            |
| `useUpdateOrderMutation`            | PATCH  | `/order/update/{id}`                      | Update order info (not items)      |
| `useDeleteOrderMutation`            | DELETE | `/order/delete/{id}`                      | Delete order                       |
| `useAddProductToOrderMutation`      | POST   | `/order/create/{id}/add`                  | Add product to existing order      |
| `useDeleteProductFromOrderMutation` | PATCH  | `/order/update/{orderId}/delete/{itemId}` | Remove item from order             |
| `useApproveOrderMutation`           | PATCH  | `/order/manage/{id}/approve`              | Approve order (CREATED → APPROVED) |
| `useCancelOrderMutation`            | PATCH  | `/order/manage/{id}/cancel`               | Cancel order with note             |

**Query Params (GET /order/search):**

- `query`: Search order name/ID
- `statusId`: CREATED | APPROVED | CANCELLED | FULLY_DELIVERED
- `toCustomerId`: Filter by customer
- `fromDate`, `toDate`: Date range filter
- `priority`: 1-5
- `page`, `size`, `sortBy`, `sortDirection`

**Order Creation Body:**

```json
{
  "toCustomerId": "uuid",
  "orderName": "string (optional)",
  "saleChannelId": "ONLINE | PARTNER | RETAIL",
  "priority": 1-5,
  "deliveryBeforeDate": "ISO date (optional)",
  "deliveryAfterDate": "ISO date (optional)",
  "note": "string (optional)",
  "items": [
    {
      "productId": "uuid",
      "orderItemSeqId": 1,
      "quantity": 10,
      "tax": 5.5,
      "discount": 2.0
    }
  ]
}
```

### 4.4 Inventory Endpoints

| Hook                             | Method | Path                          | Purpose                     |
| -------------------------------- | ------ | ----------------------------- | --------------------------- |
| `useGetInventoryItemsQuery`      | GET    | `/inventory-item/search`      | List inventory with filters |
| `useGetInventoryItemQuery`       | GET    | `/inventory-item/search/{id}` | Get inventory item          |
| `useCreateInventoryItemMutation` | POST   | `/inventory-item/create`      | Add inventory item          |
| `useUpdateInventoryItemMutation` | PATCH  | `/inventory-item/update/{id}` | Update item                 |
| `useDeleteInventoryItemMutation` | DELETE | `/inventory-item/delete/{id}` | Delete item                 |

**Query Params (GET /inventory-item/search):**

- `productId`: Filter by product
- `facilityId`: Filter by facility
- `statusId`: VALID | EXPIRED | DAMAGED
- `page`, `size`, `sortBy`, `sortDirection`

### 4.5 Facility Endpoints

| Hook                        | Method | Path                    | Purpose                   |
| --------------------------- | ------ | ----------------------- | ------------------------- |
| `useGetFacilitiesQuery`     | GET    | `/facility/search`      | List facilities           |
| `useGetFacilityQuery`       | GET    | `/facility/search/{id}` | Get facility detail       |
| `useCreateFacilityMutation` | POST   | `/facility/create`      | Create warehouse/facility |
| `useUpdateFacilityMutation` | PATCH  | `/facility/update/{id}` | Update facility           |
| `useDeleteFacilityMutation` | DELETE | `/facility/delete/{id}` | Delete facility           |

### 4.6 Category Endpoints

| Hook                        | Method | Path                    | Purpose                 |
| --------------------------- | ------ | ----------------------- | ----------------------- |
| `useGetCategoriesQuery`     | GET    | `/category/search`      | List product categories |
| `useGetCategoryQuery`       | GET    | `/category/search/{id}` | Get category            |
| `useCreateCategoryMutation` | POST   | `/category/create`      | Create category         |
| `useUpdateCategoryMutation` | PATCH  | `/category/update/{id}` | Update category         |
| `useDeleteCategoryMutation` | DELETE | `/category/delete/{id}` | Delete category         |

### 4.7 Address Endpoints

| Hook                       | Method | Path                   | Purpose                  |
| -------------------------- | ------ | ---------------------- | ------------------------ |
| `useGetAddressesQuery`     | GET    | `/address/search`      | Get addresses for entity |
| `useCreateAddressMutation` | POST   | `/address/create`      | Create address           |
| `useUpdateAddressMutation` | PATCH  | `/address/update/{id}` | Update address           |
| `useDeleteAddressMutation` | DELETE | `/address/delete/{id}` | Delete address           |

**Query Params (GET /address/search):**

- `entityId`: Customer ID, Facility ID, etc.
- `entityType`: CUSTOMER | FACILITY | SUPPLIER | PRODUCT

---

## 5. User Interaction Flow (Step-by-Step)

### 5.1 Order Creation Flow

**Scenario:** User creates a new sales order with 2 products.

1. **Navigation:** User clicks "Create Order" button on OrderListPage
   - Router: `router.push('/sales/orders/new')`
   - Next.js renders: `app/sales/orders/new/page.tsx` → `CreateOrderPage`

2. **Component Mount:** `CreateOrderPage` renders `OrderForm`
   - Local state initialized: empty customerId, items[], no errors
   - If `customerId` prop passed: Pre-loads customer via `useGetCustomerQuery(customerId)`

3. **Customer Selection:**
   - User types in customer search field
   - `setCustomerSearch('ABC Corp')` triggers re-render
   - `useGetCustomersQuery({ filters: { query: 'ABC Corp' } })` fetches matching customers
   - Dropdown shows results, user clicks "ABC Corp"
   - `setCustomerId(customer.id)` updates local state
   - `setShowCustomerDropdown(false)` hides dropdown

4. **Form Fill:**
   - User enters:
     - Order name: "Q1 Office Supplies"
     - Sale channel: ONLINE (dropdown)
     - Priority: 3 (1-5 scale)
     - Delivery dates: Optional
     - Note: "Urgent delivery requested"

5. **Add Product Items:**
   - User clicks "Add Product" button
   - Product search opens: `setShowProductDropdown(true)`
   - User types "Laptop"
   - `useGetProductsQuery({ filters: { query: 'Laptop' } })` fetches products
   - User selects "Dell Laptop XPS 15"
   - `addItem(productId)` creates new OrderItem:
     ```typescript
     {
       productId: "uuid-123",
       orderItemSeqId: 1,  // Auto-incremented
       quantity: 1,
       tax: 0,
       discount: 0
     }
     ```
   - User updates quantity to 5, tax to 10%, discount to 5%
   - `updateItem(0, 'quantity', 5)` modifies items array
   - User adds second product (same process, seqId = 2)

6. **Real-Time Calculations:**
   - Form calculates totals on every change:

     ```typescript
     // OrderForm logic
     const subtotal = items.reduce((sum, item) => {
       const product = getProductById(item.productId);
       const price = product?.unitPrice || 0;
       return sum + price * item.quantity;
     }, 0);

     const totalTax = items.reduce((sum, item) => {
       const price = getProductById(item.productId)?.unitPrice || 0;
       return sum + (price * item.quantity * item.tax) / 100;
     }, 0);

     const totalDiscount = items.reduce((sum, item) => {
       const price = getProductById(item.productId)?.unitPrice || 0;
       return sum + (price * item.quantity * item.discount) / 100;
     }, 0);

     const grandTotal = subtotal + totalTax - totalDiscount;
     ```

   - Summary card updates live

7. **Form Submission:**
   - User clicks "Create Order" button
   - `handleSubmit(e)` called:

     ```typescript
     e.preventDefault();
     if (!validateForm()) return; // Validation step (see 5.6)
     setIsSubmitting(true);

     const createData: OrderCreationForm = {
       toCustomerId: customerId,
       orderName: orderName || undefined,
       saleChannelId: saleChannel,
       priority,
       deliveryBeforeDate: deliveryBeforeDate || undefined,
       deliveryAfterDate: deliveryAfterDate || undefined,
       note: note || undefined,
       items: items, // Array of OrderItem
     };

     await createOrder(createData).unwrap();
     ```

8. **API Request:**
   - RTK Query sends: `POST /sales/api/v1/order/create`
   - API Gateway validates JWT, extracts userID & tenantID
   - Forwards to Sales service (port 8088)
   - Backend creates order + items in transaction, publishes Kafka event

9. **Success Handling:**
   - `createOrder` resolves with new order ID
   - Cache invalidated: `{ type: 'Order', id: 'LIST' }`
   - Toast notification: "Đơn hàng đã được tạo thành công"
   - Navigation: `router.push('/sales/orders/{newOrderId}')`
   - OrderDetailPage loads showing new order

10. **Error Handling:**
    - If API fails (e.g., product out of stock):
    - `catch (error)` block executes
    - Toast error: `error?.data?.message || 'Có lỗi xảy ra'`
    - Form remains open for corrections
    - `setIsSubmitting(false)` re-enables submit button

### 5.2 Order Approval Flow

**Scenario:** Manager approves a CREATED order.

1. **View Order Detail:** User navigates to `/sales/orders/{orderId}`
   - `useGetOrderQuery(orderId)` fetches order data
   - Status badge shows "Đã tạo" (CREATED) with blue styling

2. **Action Menu:** User clicks "⋮" (MoreHorizontal) button
   - Dropdown shows: Edit, Approve, Cancel, Delete
   - Only CREATED orders show "Approve" option (conditional rendering)

3. **Approve Click:**
   - User clicks "Approve"
   - `handleApprove()` called:

     ```typescript
     if (!confirm('Bạn có chắc chắn muốn phê duyệt đơn hàng này không?'))
       return;

     try {
       await approveOrder(orderId).unwrap();
       toast.success('Phê duyệt đơn hàng thành công');
     } catch (error) {
       toast.error(error?.data?.message || 'Không thể phê duyệt đơn hàng');
     }
     ```

4. **API Request:**
   - `PATCH /sales/api/v1/order/manage/{orderId}/approve`
   - Backend checks user permissions (requires APPROVE_ORDER permission)
   - Updates order status: CREATED → APPROVED
   - Records `userApprovedId`, `approvedDate`
   - Publishes Kafka event: ORDER_APPROVED

5. **Cache Update:**
   - RTK Query invalidates: `{ type: 'Order', id: orderId }` + `{ type: 'Order', id: 'LIST' }`
   - `useGetOrderQuery(orderId)` automatically refetches
   - UI updates: Status badge changes to "Đã phê duyệt" (green)
   - Approve button disappears from dropdown (conditional logic)

### 5.3 Order Cancellation Flow (with Note)

1. **Cancel Trigger:** User clicks "Cancel" in action menu
   - `setShowCancelDialog(true)` opens modal

2. **Dialog Display:**
   - Modal shows:
     - Title: "Cancel Order"
     - Textarea: "Cancellation reason (required)"
     - Buttons: Confirm, Cancel
   - User enters note: "Customer requested cancellation"

3. **Validation on Submit:**
   - User clicks "Confirm"
   - `handleCancel()` validates:
     ```typescript
     if (!cancellationNote.trim()) {
       toast.error('Vui lòng cung cấp lý do hủy đơn hàng');
       return;
     }
     ```

4. **API Call:**
   - `PATCH /sales/api/v1/order/manage/{orderId}/cancel`
   - Body: `{ "cancellationNote": "Customer requested cancellation" }`
   - Backend updates: statusId → CANCELLED, records `userCancelledId`, `cancelledDate`, `cancellationNote`

5. **UI Update:**
   - Dialog closes: `setShowCancelDialog(false)`
   - Cache refetch shows status as "Đã hủy" (red badge)
   - Order items become read-only (no more add/delete actions)

### 5.4 Adding Product to Existing Order

**Scenario:** User adds a product to an APPROVED order.

1. **Order Detail View:** On OrderDetailPage, "Overview" tab active
   - Order Items card displays existing items in table

2. **Add Item Button:** User clicks "Add Product" button
   - `setShowAddItemDialog(true)` opens inline form

3. **Product Search:**
   - User types "Mouse" in product search
   - `setProductSearch('Mouse')` triggers re-render
   - `useGetProductsQuery({ filters: { query: 'Mouse' } })` fetches products
   - Dropdown shows results with stock quantities

4. **Product Selection & Quantity:**
   - User selects "Logitech MX Master 3"
   - `setSelectedProductId(product.id)` stores selection
   - User enters quantity: 10
   - User sets tax: 8%, discount: 0%

5. **Stock Validation:**
   - Before submission, `handleAddItem()` checks:

     ```typescript
     const selectedProduct = products.find((p) => p.id === selectedProductId);
     const maxQuantity = selectedProduct?.quantityAvailable || 0;

     if (itemQuantity > maxQuantity) {
       toast.error('Số lượng đặt hàng không được vượt quá số lượng còn tồn');
       return;
     }
     ```

6. **API Call:**
   - `POST /sales/api/v1/order/create/{orderId}/add`
   - Body:
     ```json
     {
       "productId": "uuid-789",
       "orderItemSeqId": 3, // Auto-incremented from max existing
       "quantity": 10,
       "tax": 8,
       "discount": 0
     }
     ```

7. **Success & Cleanup:**
   - Backend adds item, recalculates order totals
   - RTK Query refetches order: `invalidatesTags: [{ type: 'Order', id: orderId }]`
   - Table updates showing new item
   - Form resets: `setShowAddItemDialog(false)`, `setSelectedProductId('')`, `setItemQuantity(1)`
   - Toast: "Thêm sản phẩm thành công"

### 5.5 Filtering & Pagination Flow

**Scenario:** User filters orders by status and customer.

1. **Initial Load:** OrderListPage mounts
   - Redux state: `filters = {}`, `pagination = { page: 0, size: 10 }`
   - `useGetOrdersQuery({ filters, pagination })` fetches first page
   - Displays 10 orders with stats cards

2. **Status Filter:**
   - User opens status dropdown, selects "APPROVED"
   - `dispatch(setOrderFilters({ statusId: 'APPROVED' }))`
   - Redux reducer:
     - Updates `state.orders.filters = { statusId: 'APPROVED' }`
     - Resets `state.orders.pagination.page = 0` (back to first page)
   - `useGetOrdersQuery` detects filter change, refetches with new params
   - API: `GET /order/search?statusId=APPROVED&page=0&size=10`

3. **Customer Filter:**
   - User selects customer from dropdown: "ABC Corp"
   - `dispatch(setOrderFilters({ ...filters, toCustomerId: 'uuid-abc' }))`
   - Combined filters now: `{ statusId: 'APPROVED', toCustomerId: 'uuid-abc' }`
   - API refetches with both filters

4. **Pagination:**
   - User clicks "Next Page" button
   - `dispatch(setOrderPagination({ page: 1 }))`
   - `useGetOrdersQuery` refetches with `page=1`
   - Pagination controls update: "Page 2 of 5"

5. **Clear Filters:**
   - User clicks "Clear" button with X icon
   - `dispatch(setOrderFilters({}))`
   - Resets to initial state, fetches all orders

### 5.6 Validation Flow (Order Form)

**Client-Side Validation in `validateForm()`:**

```typescript
const validateForm = (): boolean => {
  const newErrors: Record<string, string> = {};

  // 1. Required field checks
  if (!customerId) {
    newErrors.customerId = 'Customer is required';
  }

  if (!saleChannel) {
    newErrors.saleChannel = 'Sale channel is required';
  }

  // 2. Business rule: Must have at least one item (create mode only)
  if (!isEditMode && items.length === 0) {
    newErrors.items = 'At least one item is required';
  }

  // 3. Date logic validation
  if (deliveryBeforeDate && deliveryAfterDate) {
    const before = new Date(deliveryBeforeDate);
    const after = new Date(deliveryAfterDate);
    if (before < after) {
      newErrors.deliveryBeforeDate =
        'Delivery before date must be after delivery after date';
    }
  }

  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};
```

**Validation Triggers:**

- On form submission (before API call)
- Errors displayed as red text under inputs
- Submit button re-enabled only after errors cleared

---

## 6. Validation & Business Rules

### 6.1 Frontend Validation Rules

#### **Order Creation/Editing:**

| Field          | Rule                                      | Error Message                                            |
| -------------- | ----------------------------------------- | -------------------------------------------------------- |
| Customer       | Required                                  | "Customer is required"                                   |
| Sale Channel   | Required (ONLINE/PARTNER/RETAIL)          | "Sale channel is required"                               |
| Items          | At least 1 item required (create mode)    | "At least one item is required"                          |
| Delivery Dates | If both set: beforeDate > afterDate       | "Delivery before date must be after delivery after date" |
| Item Quantity  | Must be > 0                               | "Số lượng phải lớn hơn 0"                                |
| Item Quantity  | Cannot exceed `product.quantityAvailable` | "Số lượng đặt hàng không được vượt quá số lượng còn tồn" |
| Tax/Discount   | Must be ≥ 0 (percentage)                  | Implicit validation (number input)                       |

#### **Customer Creation:**

| Field      | Rule                       | Error Message             |
| ---------- | -------------------------- | ------------------------- |
| First Name | Required (if enforced)     | Implementation-dependent  |
| Last Name  | Required (if enforced)     | Implementation-dependent  |
| Email      | Must be valid email format | Browser native validation |
| Phone      | Must be valid phone format | Browser native validation |
| Status     | Required (ACTIVE/INACTIVE) | Defaults to ACTIVE        |

#### **Inventory Item Creation:**

| Field       | Rule                             | Error Message                     |
| ----------- | -------------------------------- | --------------------------------- |
| Product     | Required                         | "Product is required"             |
| Facility    | Required                         | "Facility is required"            |
| Quantity    | Must be > 0                      | "Quantity must be greater than 0" |
| Status      | Required (VALID/EXPIRED/DAMAGED) | "Status is required"              |
| Expiry Date | Should be > manufactured date    | Logical validation                |

#### **Order Cancellation:**

| Field             | Rule                       | Error Message                          |
| ----------------- | -------------------------- | -------------------------------------- |
| Cancellation Note | Required, non-empty string | "Vui lòng cung cấp lý do hủy đơn hàng" |

### 6.2 Business Logic Rules

#### **Order Status Transitions:**

```
CREATED → APPROVED    (via Approve button, requires permission)
CREATED → CANCELLED   (via Cancel action with note)
APPROVED → CANCELLED  (via Cancel action with note)
APPROVED → FULLY_DELIVERED  (backend logic, triggered by logistics)
```

**UI Enforcement:**

- Approve button only visible for CREATED orders
- Cancel button visible for CREATED/APPROVED orders
- CANCELLED/FULLY_DELIVERED orders are read-only (no edit/add items)

#### **Order Item Management:**

- **Add Item:** Only allowed if order status is CREATED or APPROVED
- **Delete Item:** Only allowed if order status is CREATED or APPROVED
- **Edit Item:** Not directly supported (must delete and re-add)
- **Sequential IDs:** `orderItemSeqId` auto-incremented based on `Math.max(...existingSeqIds) + 1`

#### **Financial Calculations:**

```typescript
// Per-item calculations
itemSubtotal = unitPrice × quantity
itemTax = itemSubtotal × (tax / 100)
itemDiscount = itemSubtotal × (discount / 100)
itemTotal = itemSubtotal + itemTax - itemDiscount

// Order totals
orderSubtotal = Σ(all itemSubtotals)
orderTotalTax = Σ(all itemTax)
orderTotalDiscount = Σ(all itemDiscount)
orderGrandTotal = orderSubtotal + orderTotalTax - orderTotalDiscount
```

#### **Inventory Stock Validation:**

- When adding product to order: `requestedQuantity ≤ product.quantityAvailable`
- Backend performs additional validation on order approval
- UI shows available stock in product search dropdown

#### **Permission-Based Actions:**

- **Create Order:** Requires SALES module access
- **Approve Order:** Requires APPROVE_ORDER permission (backend check)
- **Cancel Order:** Requires CANCEL_ORDER permission (backend check)
- **Delete Order:** Requires DELETE_ORDER permission (backend check)

### 6.3 Error Handling Patterns

**Network Errors:**

```typescript
try {
  await createOrder(data).unwrap();
  toast.success('Success message');
} catch (error: any) {
  console.error('Operation failed:', error);
  toast.error(error?.data?.message || 'Generic error message');
}
```

**Validation Errors:**

- Frontend: Display inline errors under form fields
- Backend: API returns 400 with validation errors in response body
- UI shows toast with backend error message

**Authorization Errors:**

- Backend returns 403 Forbidden
- UI shows toast: "You don't have permission to perform this action"
- RouteGuard redirects to login if JWT invalid (401)

**Confirmation Dialogs:**

- Destructive actions (Delete, Cancel) require `confirm()` dialog
- User can abort operation before API call

### 6.4 Data Consistency Rules

**RTK Query Cache Invalidation Strategy:**

| Action               | Invalidates                | Effect                                   |
| -------------------- | -------------------------- | ---------------------------------------- |
| Create Order         | `Order:LIST`               | Refetch order lists                      |
| Update Order         | `Order:{id}`, `Order:LIST` | Refetch detail + lists                   |
| Delete Order         | `Order:{id}`, `Order:LIST` | Refetch lists, remove from cache         |
| Add Item to Order    | `Order:{id}`, `Order:LIST` | Refetch order detail (includes new item) |
| Approve/Cancel Order | `Order:{id}`, `Order:LIST` | Refetch detail (status changed)          |

**Optimistic Updates:** Not implemented (relies on automatic refetching)

**Real-Time Updates:** Not implemented (future enhancement: WebSocket/SSE for order status changes)

---

## 7. Key Technical Patterns

### 7.1 Component Communication

**Parent → Child (Props):**

```typescript
<OrderForm
  order={order}           // Pre-populate for edit mode
  customerId={customerId} // Pre-select customer
  onSubmit={handleSubmit} // Callback for form submission
  onCancel={handleCancel} // Callback for cancellation
/>
```

**Child → Parent (Callbacks):**

```typescript
// In OrderForm
await onSubmit(orderData); // Sends data back to parent
onCancel(); // Notifies parent to close form
```

**Sibling Communication (via Redux):**

```typescript
// Component A sets filter
dispatch(setOrderFilters({ statusId: 'APPROVED' }));

// Component B reads filter (automatic re-render)
const filters = useAppSelector(selectOrderFilters);
```

### 7.2 Conditional Rendering Patterns

**Status-Based Actions:**

```typescript
{order?.statusId === 'CREATED' && (
  <DropdownMenuItem onClick={handleApprove}>
    <CheckCircle2 className="mr-2 h-4 w-4" />
    Approve
  </DropdownMenuItem>
)}

{['CREATED', 'APPROVED'].includes(order?.statusId || '') && (
  <DropdownMenuItem onClick={() => setShowCancelDialog(true)}>
    <XCircle className="mr-2 h-4 w-4" />
    Cancel
  </DropdownMenuItem>
)}
```

**Loading States:**

```typescript
{isLoading && <Skeleton className="h-20" />}
{isError && <ErrorDisplay message="Failed to load data" />}
{data && <OrderCard order={data} />}
```

**Empty States:**

```typescript
{orders.length === 0 && (
  <EmptyState
    icon={ShoppingCart}
    title="No orders found"
    description="Create your first order to get started"
  />
)}
```

### 7.3 Search & Debouncing

**Product/Customer Search (in forms):**

```typescript
// User types in search input
<Input
  value={productSearch}
  onChange={(e) => setProductSearch(e.target.value)}
/>

// RTK Query auto-fetches with search term (no manual debounce)
const { data } = useGetProductsQuery({
  filters: { query: productSearch },
  pagination: { page: 0, size: 20 }
});

// Dropdown shows results
{showProductDropdown && products.map(product => (
  <DropdownItem onClick={() => selectProduct(product.id)}>
    {product.name} - Stock: {product.quantityAvailable}
  </DropdownItem>
))}
```

**Note:** RTK Query includes built-in request deduplication (doesn't debounce, but prevents duplicate simultaneous requests).
