# Logistics Module

Quản lý logistics cho Serp ERP system với 9 entities chính.

## Structure

```
logistics/
├── types/              # TypeScript type definitions
│   ├── address.types.ts
│   ├── category.types.ts
│   ├── customer.types.ts
│   ├── facility.types.ts
│   ├── inventoryItem.types.ts
│   ├── order.types.ts
│   ├── product.types.ts
│   ├── shipment.types.ts
│   ├── supplier.types.ts
│   └── index.ts
├── services/           # RTK Query API endpoints
│   ├── api.ts
│   ├── addressApi.ts
│   ├── categoryApi.ts
│   ├── customerApi.ts
│   ├── productApi.ts
│   ├── supplierApi.ts
│   └── index.ts
│   # TODO: facilityApi, inventoryItemApi, orderApi, shipmentApi
├── store/              # Redux slices for UI state
│   ├── addressSlice.ts
│   ├── categorySlice.ts
│   ├── customerSlice.ts
│   ├── facilitySlice.ts
│   ├── inventoryItemSlice.ts
│   ├── orderSlice.ts
│   ├── productSlice.ts
│   ├── shipmentSlice.ts
│   ├── supplierSlice.ts
│   └── index.ts
├── hooks/              # Custom React hooks
│   ├── useAddresses.ts
│   ├── useCategories.ts
│   ├── useCustomers.ts
│   ├── useFacilities.ts
│   ├── useInventoryItems.ts
│   ├── useOrders.ts
│   ├── useProducts.ts
│   ├── useShipments.ts
│   ├── useSuppliers.ts
│   └── index.ts
├── components/         # React components (empty - to be implemented)
└── index.ts            # Barrel export
```

## Implementation Status

| Entity        | Types | API Service | Redux Slice | Custom Hook | Status  |
| ------------- | ----- | ----------- | ----------- | ----------- | ------- |
| Address       | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| Category      | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| Customer      | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| Facility      | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| InventoryItem | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| Order         | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| Product       | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| Shipment      | ✅    | ✅          | ✅          | ✅          | ✅ 100% |
| Supplier      | ✅    | ✅          | ✅          | ✅          | ✅ 100% |

### ✅ All Entities Completed

All 9 entities now have complete implementation across all 4 layers:

- **Types Layer**: All TypeScript interfaces, DTOs, request/response types, and filters
- **API Layer**: RTK Query endpoints with proper caching and invalidation
- **Store Layer**: Redux slices with UI state, actions, and selectors
- **Hooks Layer**: Custom hooks combining API + Redux + business logic

### 🎯 Ready for Use

All hooks can be imported and used immediately:

```typescript
import {
  useAddresses,
  useCategories,
  useCustomers,
  useFacilities,
  useInventoryItems,
  useOrders,
  useProducts,
  useShipments,
  useSuppliers,
} from '@/modules/logistics';
```

## Usage Examples

### Category Management

```typescript
import { useCategories } from '@/modules/logistics';

function CategoryList() {
  const {
    categories,
    isLoadingCategories,
    filters,
    setPage,
    search,
    openDialog,
    create,
  } = useCategories();

  return (
    <div>
      <input onChange={(e) => search(e.target.value)} />
      {categories.map((category) => (
        <div key={category.id}>{category.name}</div>
      ))}
    </div>
  );
}
```

### Product Management

```typescript
import { useProducts } from '@/modules/logistics';

function ProductGrid() {
  const {
    products,
    viewMode,
    setViewMode,
    setCategoryFilter,
    openDialog,
  } = useProducts();

  return (
    <div>
      <button onClick={() => setViewMode('grid')}>Grid</button>
      <button onClick={() => setViewMode('list')}>List</button>
      {/* ... */}
    </div>
  );
}
```

## API Endpoints

All endpoints proxy through API Gateway at `http://localhost:8080/logistics/api/v1/`:

- `POST /address/create`
- `PATCH /address/update/:addressId`
- `GET /address/search/by-entity/:entityId`
- `POST /category/create`
- `PATCH /category/update/:categoryId`
- `GET /category/search`
- `GET /category/search/:categoryId`
- `DELETE /category/delete/:categoryId`
- `GET /customer/search`
- `POST /product/create`
- `PATCH /product/update/:productId`
- `DELETE /product/delete/:productId`
- `GET /product/search`
- `GET /product/search/:productId`
- `GET /supplier/search`
- `GET /supplier/search/:supplierId`

## Integration

Module đã được integrate vào root store:

```typescript
// src/lib/store/store.ts
import { logisticsReducer } from '@/modules/logistics/store';

const rootReducer = combineReducers({
  // ...
  logistics: logisticsReducer,
});
```

## Next Steps

1. **Complete remaining APIs**: Facility, InventoryItem, Order, Shipment
2. **Complete Redux slices**: Add slices for remaining entities
3. **Complete custom hooks**: Implement useCustomers, useSuppliers, etc.
4. **Build UI components**: Tables, forms, dialogs for each entity
5. **Add pages**: Create Next.js pages in `src/app/logistics/`
6. **Write tests**: Unit tests for hooks, integration tests for APIs
7. **Add validation**: Form validation with Zod schemas
8. **Implement bulk operations**: Multi-select, bulk delete/update

## Notes

- Tất cả types đã được định nghĩa đầy đủ cho 9 entities
- API tags đã được thêm vào baseApi.ts cho cache invalidation
- Cấu trúc tuân thủ Clean Architecture và patterns của dự án
- Sử dụng RTK Query cho data fetching và caching
- Redux Toolkit cho UI state management
