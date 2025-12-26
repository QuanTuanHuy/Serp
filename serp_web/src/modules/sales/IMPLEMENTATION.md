# Sales Module - Complete Frontend Implementation

Full-featured sales management module for the Serp ERP system, built with Next.js 15, TypeScript, Redux Toolkit, and RTK Query.

## 📁 Complete Module Structure

```
sales/
├── api/
│   └── salesApi.ts              # 38 RTK Query endpoints
├── components/
│   ├── layout/
│   │   ├── SalesLayout.tsx      # Main layout with sidebar
│   │   ├── SalesHeader.tsx      # Header component
│   │   └── index.ts
│   ├── SalesAuthGuard.tsx       # Route protection
│   └── index.ts
├── pages/
│   ├── dashboard/
│   │   └── SalesDashboardPage.tsx
│   ├── customers/
│   │   └── CustomerListPage.tsx
│   ├── products/
│   │   └── ProductListPage.tsx
│   ├── orders/
│   │   └── OrderListPage.tsx
│   └── index.ts
├── store/
│   ├── salesSlice.ts            # Redux state management
│   ├── selectors.ts             # Memoized selectors
│   └── index.ts
├── types/
│   └── index.ts                 # TypeScript definitions
├── index.ts                     # Module exports
└── README.md
```

## 🚀 Features Implemented

### ✅ API Layer (38 Endpoints)

- **Customers**: CRUD operations + search/filter
- **Products**: Full catalog management
- **Orders**: Complete order workflow (create, update, approve, cancel)
- **Categories**: Product categorization
- **Facilities**: Warehouse management
- **Inventory**: Stock tracking
- **Addresses**: Multi-entity address support

### ✅ State Management

- Redux Toolkit slices for all entities
- Memoized selectors for performance
- Pagination & filtering per entity
- Bulk selection support
- UI state management (sidebar, view mode, filters)

### ✅ UI Components

- Responsive layout with collapsible sidebar
- Search and quick actions header
- Dashboard with key metrics
- Data tables with pagination
- Form components (future enhancement)

### ✅ Pages

1. **Dashboard** - Sales overview with stats
2. **Customers** - Customer list with search/filter
3. **Products** - Product catalog with stock info
4. **Orders** - Order management with status tracking

### ✅ Routes

- `/sales` → redirects to dashboard
- `/sales/dashboard` → Sales overview
- `/sales/customers` → Customer management
- `/sales/products` → Product catalog
- `/sales/orders` → Order tracking

## 📖 Quick Start

### Using API Hooks

```typescript
import {
  useGetProductsQuery,
  useCreateOrderMutation
} from '@/modules/sales';

function ProductList() {
  // Query with pagination and filters
  const { data, isLoading } = useGetProductsQuery({
    filters: { categoryId: 'electronics', statusId: 'ACTIVE' },
    pagination: { page: 0, size: 20 }
  });

  // Mutation for creating orders
  const [createOrder, { isLoading: isCreating }] = useCreateOrderMutation();

  const handleCreateOrder = async () => {
    try {
      const result = await createOrder({
        orderTypeId: 'SALES_ORDER',
        toCustomerId: 'cust-123',
        items: [{ productId: 'prod-1', quantity: 5, unitPrice: 100 }]
      }).unwrap();
      console.log('Success:', result);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return <div>...</div>;
}
```

### Using Redux Store

```typescript
import { useAppDispatch, useAppSelector } from '@/lib/store/hooks';
import {
  selectProductItems,
  setProductFilters,
  selectLowStockItems
} from '@/modules/sales';

function StockAlert() {
  const dispatch = useAppDispatch();
  const lowStockItems = useAppSelector(selectLowStockItems);

  // Filter products
  dispatch(setProductFilters({
    statusId: 'ACTIVE',
    categoryId: 'electronics'
  }));

  return <div>Low stock: {lowStockItems.length}</div>;
}
```

## 🔌 API Endpoints Reference

### Customers (5 endpoints)

```typescript
useGetCustomersQuery({ filters, pagination });
useGetCustomerQuery(customerId);
useCreateCustomerMutation();
useUpdateCustomerMutation();
useDeleteCustomerMutation();
```

### Products (5 endpoints)

```typescript
useGetProductsQuery({ filters, pagination });
useGetProductQuery(productId);
useCreateProductMutation();
useUpdateProductMutation();
useDeleteProductMutation();
```

### Orders (9 endpoints)

```typescript
useGetOrdersQuery({ filters, pagination });
useGetOrderQuery(orderId);
useCreateOrderMutation();
useUpdateOrderMutation();
useDeleteOrderMutation();
useAddProductToOrderMutation();
useDeleteProductFromOrderMutation();
useApproveOrderMutation();
useCancelOrderMutation();
```

**Plus:** Categories (5), Facilities (5), Inventory (5), Addresses (4)

## 🎨 UI Components

All components built with:

- **Shadcn UI** - Modern component library
- **Tailwind CSS** - Utility-first styling
- **Lucide Icons** - Beautiful icons
- **Responsive Design** - Mobile-first approach

### Component Examples

```typescript
import { SalesLayout } from '@/modules/sales/components';
import { CustomerListPage } from '@/modules/sales/pages';

// Layout wraps all sales pages
<SalesLayout>
  <CustomerListPage />
</SalesLayout>

// Auth guard protects routes
<SalesAuthGuard>
  {children}
</SalesAuthGuard>
```

## 📊 Redux Store Structure

```typescript
{
  sales: {
    ui: {
      activeModule: 'dashboard' | 'customers' | 'products' | 'orders',
      viewMode: 'list' | 'grid' | 'kanban',
      sidebarCollapsed: boolean,
      filterPanelOpen: boolean,
      bulkActionMode: boolean,
      selectedItems: string[]
    },
    customers: {
      items: Customer[],
      filters: CustomerFilters,
      pagination: PaginationParams,
      selectedItems: string[],
      loading: boolean,
      error: string | null,
      total: number
    },
    products: { /* same structure */ },
    orders: { /* same structure */ },
    categories: { /* same structure */ },
    facilities: { /* same structure */ },
    inventoryItems: { /* same structure */ }
  }
}
```

## 🎯 Selectors

### UI Selectors

```typescript
(selectActiveModule, selectViewMode, selectIsSidebarCollapsed);
```

### Entity Selectors

```typescript
// Base selectors
(selectCustomerItems, selectCustomersLoading, selectCustomerFilters);

// By ID
selectCustomerById(state, id);
selectProductById(state, id);
selectOrderById(state, id);

// Filtered
selectProductsByCategory(state, categoryId);
selectOrdersByCustomer(state, customerId);
selectOrdersByStatus(state, statusId);

// Computed
selectTotalOrderAmount; // Sum of all order amounts
selectLowStockItems; // Products below reorder point
```

## 🔐 Authentication

Protected with `SalesAuthGuard`:

- Validates JWT token
- Redirects to login if unauthorized
- Shows loading state during verification

## 🔄 Cache Management

RTK Query handles caching automatically:

- **Tags**: `SalesCustomer`, `Product`, `Order`, `Category`, `Facility`, `InventoryItem`, `Address`
- **Auto-invalidation**: Mutations invalidate related queries
- **Refetch policies**: On focus, reconnect, or manual
- **Optimistic updates**: Immediate UI updates

## 🚧 Future Enhancements

- [ ] Customer detail pages
- [ ] Product detail with variants
- [ ] Order workflow visualization
- [ ] Advanced filtering panels
- [ ] Bulk operations UI
- [ ] Export to CSV/Excel
- [ ] Real-time updates (WebSocket)
- [ ] Analytics dashboard
- [ ] Invoice generation
- [ ] Shipping integration
- [ ] Mobile app support

## 📝 Type Safety

Fully typed with TypeScript:

- All API requests/responses
- Redux state and actions
- Component props
- Form data

Example:

```typescript
import type {
  Product,
  OrderCreationForm,
  APIResponse,
  PaginatedResponse
} from '@/modules/sales';

const response: APIResponse<PaginatedResponse<Product>> = ...
```

## 🔗 Integration Points

### API Gateway

Routes automatically prefixed: `/sales/api/v1/*`

### Backend Service

Port: 8088 (based on architecture guide)

### Database

Entities: Customer, Product, Order, Category, Facility, InventoryItem, Address

## 📚 References

- Backend API Docs: `sales/api-documents/`
- Architecture Guide: `.github/copilot-instructions.md`
- CRM Reference: `src/modules/crm/`

## 👥 Author

**QuanTuanHuy**  
Part of Serp Project

## 📄 License

Part of Serp ERP System
