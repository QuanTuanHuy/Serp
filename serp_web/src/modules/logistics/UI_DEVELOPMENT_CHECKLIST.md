/_
Author: QuanTuanHuy
Description: Part of Serp Project - UI Development Checklist
_/

# Logistics Module - UI Development Checklist

## ✅ Prerequisites (All Complete)

- ✅ API endpoints configured and tested
- ✅ Redux store integrated
- ✅ TypeScript types defined
- ✅ Selectors created
- ✅ Actions ready
- ✅ Documentation complete

## 📋 UI Development Tasks

### Phase 1: Core Pages (Priority: High)

#### 1.1 Dashboard Page

**Location**: `src/app/logistics/page.tsx`

**Features to Implement**:

- [ ] Overview cards (total inventory, low stock, expiring items)
- [ ] Recent shipments table
- [ ] Low stock alerts
- [ ] Expiration warnings
- [ ] Quick actions (create shipment, add product)

**Hooks to Use**:

- `useGetInventoryItemsQuery`
- `selectLowStockProducts`
- `selectExpiredInventoryItems`
- `selectExpiringSoonInventoryItems`
- `useGetShipmentsQuery`

**Example Component**:

```typescript
function LogisticsDashboard() {
  const lowStock = useAppSelector(selectLowStockProducts);
  const expiring = useAppSelector(selectExpiringSoonInventoryItems);
  const { data: recentShipments } = useGetShipmentsQuery({
    filters: {},
    pagination: { page: 0, size: 5, sortBy: 'createdStamp', sortDirection: 'desc' }
  });

  return (
    <div className="grid grid-cols-4 gap-4">
      <StatCard title="Low Stock Items" value={lowStock.length} />
      <StatCard title="Expiring Soon" value={expiring.length} />
      {/* More cards */}
    </div>
  );
}
```

#### 1.2 Inventory Management Page

**Location**: `src/app/logistics/inventory/page.tsx`

**Features to Implement**:

- [ ] Inventory list/grid view
- [ ] Advanced filters (facility, product, status, expiration date)
- [ ] Search functionality
- [ ] Pagination controls
- [ ] Sort options
- [ ] Bulk actions (update status, delete)
- [ ] Export to CSV

**Components Needed**:

- `InventoryList` - Main list component
- `InventoryCard` - Individual item card
- `InventoryFilters` - Filter panel
- `InventoryActions` - Bulk action bar
- `InventoryStats` - Statistics widgets

**Hooks to Use**:

- `useGetInventoryItemsQuery`
- `useUpdateInventoryItemMutation`
- `useDeleteInventoryItemMutation`
- `selectInventoryItemItems`
- `selectInventoryItemFilters`
- `setInventoryItemFilters`
- `setInventoryItemPagination`

#### 1.3 Inventory Item Detail Page

**Location**: `src/app/logistics/inventory/[id]/page.tsx`

**Features to Implement**:

- [ ] Item details display
- [ ] Edit mode
- [ ] History/activity log
- [ ] Related shipments
- [ ] Facility information
- [ ] Product information
- [ ] Expiration warnings

**Hooks to Use**:

- `useGetInventoryItemQuery`
- `useUpdateInventoryItemMutation`

### Phase 2: Shipment Management (Priority: High)

#### 2.1 Shipments List Page

**Location**: `src/app/logistics/shipments/page.tsx`

**Features to Implement**:

- [ ] Shipment list with status badges
- [ ] Filter by type (INBOUND/OUTBOUND), status, date
- [ ] Search by shipment name, order ID
- [ ] Create shipment button
- [ ] Status timeline/progress indicator
- [ ] Import action for INBOUND shipments

**Components Needed**:

- `ShipmentList` - Main list
- `ShipmentCard` - Individual shipment card
- `ShipmentFilters` - Filter panel
- `ShipmentStatusBadge` - Status indicator
- `ShipmentTypeIcon` - Type icon

**Hooks to Use**:

- `useGetShipmentsQuery`
- `selectShipmentItems`
- `selectShipmentsByStatus`
- `setShipmentFilters`

#### 2.2 Create Shipment Page

**Location**: `src/app/logistics/shipments/create/page.tsx`

**Features to Implement**:

- [ ] Multi-step form (order selection → items → details)
- [ ] Order selection dropdown
- [ ] Shipment item manager
- [ ] Lot ID input
- [ ] Expiration/Manufacturing date pickers
- [ ] Facility selector
- [ ] Form validation
- [ ] Save draft functionality

**Components Needed**:

- `ShipmentForm` - Main form
- `OrderSelector` - Search and select order
- `ShipmentItemsTable` - Manage items
- `AddItemModal` - Add item dialog
- `FacilitySelector` - Choose facility

**Hooks to Use**:

- `useCreateShipmentMutation`
- `useGetOrdersQuery`
- `useGetProductsQuery`
- `useGetFacilitiesQuery`

#### 2.3 Shipment Detail Page

**Location**: `src/app/logistics/shipments/[id]/page.tsx`

**Features to Implement**:

- [ ] Shipment header (status, type, dates)
- [ ] Shipment items table with edit/delete
- [ ] Add item button
- [ ] Import button (for INBOUND)
- [ ] Export button (for OUTBOUND)
- [ ] Order information
- [ ] Supplier/Customer information
- [ ] Activity timeline

**Components Needed**:

- `ShipmentHeader` - Header info
- `ShipmentItemsTable` - Editable items table
- `ShipmentTimeline` - Activity timeline
- `ImportShipmentButton` - Import action

**Hooks to Use**:

- `useGetShipmentQuery`
- `useUpdateShipmentMutation`
- `useAddItemToShipmentMutation`
- `useUpdateItemInShipmentMutation`
- `useDeleteItemFromShipmentMutation`
- `useImportShipmentMutation`

### Phase 3: Product & Facility Management (Priority: Medium)

#### 3.1 Products Page

**Location**: `src/app/logistics/products/page.tsx`

**Features to Implement**:

- [ ] Product catalog view
- [ ] Category filter
- [ ] Status filter (ACTIVE/INACTIVE)
- [ ] Inventory level indicator
- [ ] Quick view modal
- [ ] Create/Edit product

**Hooks to Use**:

- `useGetProductsQuery`
- `useCreateProductMutation`
- `useUpdateProductMutation`
- `selectProductsByCategory`

#### 3.2 Facilities Page

**Location**: `src/app/logistics/facilities/page.tsx`

**Features to Implement**:

- [ ] Facility list with capacity info
- [ ] Map view of facilities
- [ ] Inventory count per facility
- [ ] Set default facility
- [ ] Create/Edit facility

**Hooks to Use**:

- `useGetFacilitiesQuery`
- `useCreateFacilityMutation`
- `useUpdateFacilityMutation`
- `selectDefaultFacility`

### Phase 4: Order Tracking (Priority: Low)

#### 4.1 Orders Page

**Location**: `src/app/logistics/orders/page.tsx`

**Features to Implement**:

- [ ] Order list (read-only)
- [ ] Filter by status, customer, supplier
- [ ] View order details
- [ ] Track shipments for order
- [ ] Link to create shipment

**Hooks to Use**:

- `useGetOrdersQuery`
- `useGetOrderQuery`
- `selectOrdersByStatus`

### Phase 5: Shared Components (Priority: Medium)

#### 5.1 Layout & Navigation

- [ ] `LogisticsLayout` - Main layout with sidebar
- [ ] `LogisticsSidebar` - Navigation menu
- [ ] `LogisticsHeader` - Page header with breadcrumbs

#### 5.2 Common Components

- [ ] `DataTable` - Reusable table with sorting/filtering
- [ ] `FilterPanel` - Collapsible filter panel
- [ ] `StatusBadge` - Color-coded status badges
- [ ] `SearchInput` - Debounced search input
- [ ] `PaginationControls` - Pagination component
- [ ] `EmptyState` - Empty data placeholder
- [ ] `LoadingSpinner` - Loading indicator
- [ ] `ErrorAlert` - Error display

#### 5.3 Form Components

- [ ] `DateRangePicker` - Date range selection
- [ ] `FacilitySelector` - Facility dropdown
- [ ] `ProductAutocomplete` - Product search & select
- [ ] `CustomerAutocomplete` - Customer search & select
- [ ] `SupplierAutocomplete` - Supplier search & select
- [ ] `StatusSelect` - Status dropdown
- [ ] `CategorySelect` - Category dropdown

#### 5.4 Widget Components

- [ ] `LowStockWidget` - Low stock alert widget
- [ ] `ExpirationWidget` - Expiration tracking widget
- [ ] `InventoryValueCard` - Total value display
- [ ] `ShipmentStatsCard` - Shipment statistics
- [ ] `RecentActivityFeed` - Activity timeline

### Phase 6: Advanced Features (Priority: Low)

#### 6.1 Analytics & Reports

- [ ] Inventory valuation report
- [ ] Stock movement report
- [ ] Expiration forecast
- [ ] Shipment performance metrics
- [ ] Facility utilization

#### 6.2 Bulk Operations

- [ ] Bulk status update
- [ ] Bulk delete with confirmation
- [ ] Bulk export to CSV/Excel
- [ ] Bulk print labels

#### 6.3 Notifications

- [ ] Low stock notifications
- [ ] Expiration alerts
- [ ] Shipment status updates
- [ ] Real-time inventory changes

## 🎨 Design Guidelines

### Color Scheme

```typescript
const STATUS_COLORS = {
  // Inventory
  VALID: 'green',
  EXPIRED: 'red',
  DAMAGED: 'orange',

  // Shipment
  CREATED: 'blue',
  IMPORTED: 'green',
  EXPORTED: 'purple',

  // Product
  ACTIVE: 'green',
  INACTIVE: 'gray',
};
```

### Layout Structure

```
┌─────────────────────────────────────┐
│  Header (Breadcrumbs, Actions)     │
├─────────┬───────────────────────────┤
│         │  Main Content Area        │
│ Sidebar │                           │
│ (Nav)   │  - Filters Panel          │
│         │  - Data Table/Grid        │
│         │  - Pagination             │
│         │                           │
└─────────┴───────────────────────────┘
```

### Responsive Breakpoints

- Mobile: < 640px
- Tablet: 640px - 1024px
- Desktop: > 1024px

## 🧪 Testing Checklist

For each page/component:

- [ ] Unit tests for business logic
- [ ] Integration tests with Redux
- [ ] API mock tests
- [ ] Loading state handling
- [ ] Error state handling
- [ ] Empty state handling
- [ ] Form validation
- [ ] Accessibility (a11y) compliance

## 📱 Mobile Considerations

- [ ] Responsive design for all pages
- [ ] Touch-friendly interactions
- [ ] Simplified filters on mobile
- [ ] Swipe gestures for actions
- [ ] Mobile-optimized tables (card view)

## ⚡ Performance Optimization

- [ ] Lazy load heavy components
- [ ] Virtual scrolling for large lists
- [ ] Debounce search inputs
- [ ] Optimize images
- [ ] Code splitting by route
- [ ] Use React.memo for expensive renders
- [ ] Implement skeleton loaders

## 🔒 Security Considerations

- [ ] Role-based access control (RBAC)
- [ ] Validate all user inputs
- [ ] Sanitize data before display
- [ ] Secure file uploads
- [ ] XSS prevention
- [ ] CSRF protection (handled by API)

## 📦 Third-Party Libraries Recommended

```json
{
  "shadcn/ui": "latest", // UI components
  "react-hook-form": "^7.x", // Form management
  "zod": "^3.x", // Schema validation
  "date-fns": "^2.x", // Date utilities
  "recharts": "^2.x", // Charts
  "react-table": "^8.x", // Advanced tables
  "react-hot-toast": "^2.x" // Notifications
}
```

## 🚀 Development Workflow

1. **Start with Dashboard** - Get overview working first
2. **Build Core CRUD** - Inventory & Shipments
3. **Add Filters** - Make data searchable
4. **Polish UX** - Loading states, error handling
5. **Add Advanced Features** - Bulk actions, analytics
6. **Test Thoroughly** - All user flows
7. **Optimize** - Performance tuning

## ✅ Definition of Done

A feature is "done" when:

- [ ] Code is written and follows style guide
- [ ] Types are properly defined
- [ ] Tests are written and passing
- [ ] Error handling is implemented
- [ ] Loading states are handled
- [ ] Responsive design works
- [ ] Accessibility is verified
- [ ] Code review is complete
- [ ] Documentation is updated
- [ ] Feature is deployed to staging

## 📚 Resources

- **API Documentation**: `README_API.md`
- **Quick Reference**: `QUICK_REFERENCE.md`
- **Module Comparison**: `MODULE_COMPARISON.md`
- **Shadcn UI**: https://ui.shadcn.com
- **Redux Toolkit**: https://redux-toolkit.js.org
- **RTK Query**: https://redux-toolkit.js.org/rtk-query/overview

---

**Ready to build amazing logistics UI! 🚀**

Start with the dashboard, then move to inventory and shipments. The data layer is solid and ready to support all your UI needs.
