/_
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Module Quick Reference
_/

# Logistics Module - Quick Reference

## 🚀 Quick Start

```typescript
// Import everything you need
import {
  // API Hooks
  useGetInventoryItemsQuery,
  useCreateShipmentMutation,

  // Selectors
  selectInventoryItemItems,
  selectLowStockProducts,

  // Actions
  setInventoryItemFilters,

  // Types
  InventoryItem,
  Shipment,
  ShipmentCreationForm,
} from '@/modules/logistics';

import { useAppSelector, useAppDispatch } from '@/lib/store';
```

## 📦 Main Entities

| Entity            | Description                | CRUD Operations        |
| ----------------- | -------------------------- | ---------------------- |
| **InventoryItem** | Stock items in facilities  | ✅ Full CRUD           |
| **Shipment**      | Inbound/Outbound shipments | ✅ Full CRUD + Import  |
| **Product**       | Product catalog            | ✅ Full CRUD           |
| **Facility**      | Warehouses/Locations       | ✅ Full CRUD           |
| **Category**      | Product categories         | ✅ Full CRUD           |
| **Order**         | Purchase/Sales orders      | 📖 Read-only           |
| **Customer**      | Customer data              | 📖 Read-only           |
| **Supplier**      | Supplier data              | 📖 Read-only           |
| **Address**       | Entity addresses           | ✅ Create, Update, Get |

## 🎯 Common Use Cases

### 1. Display Inventory List

```typescript
function InventoryList() {
  const { data, isLoading, error } = useGetInventoryItemsQuery({
    filters: {
      facilityId: '123',
      statusId: 'VALID'
    },
    pagination: {
      page: 0,
      size: 20
    }
  });

  if (isLoading) return <Loading />;
  if (error) return <Error message={error} />;

  return (
    <div>
      {data?.data?.items.map(item => (
        <InventoryCard key={item.id} item={item} />
      ))}
    </div>
  );
}
```

### 2. Create Shipment

```typescript
function CreateShipmentForm() {
  const [createShipment, { isLoading }] = useCreateShipmentMutation();

  const handleSubmit = async (formData: ShipmentCreationForm) => {
    try {
      const result = await createShipment(formData).unwrap();
      toast.success('Shipment created successfully');
      router.push(`/logistics/shipments/${result.data.id}`);
    } catch (error) {
      toast.error('Failed to create shipment');
    }
  };

  return <ShipmentForm onSubmit={handleSubmit} loading={isLoading} />;
}
```

### 3. Import Shipment (Process Inbound)

```typescript
function ShipmentDetail({ shipmentId }: { shipmentId: string }) {
  const [importShipment, { isLoading }] = useImportShipmentMutation();

  const handleImport = async () => {
    try {
      await importShipment({ shipmentId }).unwrap();
      toast.success('Shipment imported successfully');
      // This will also invalidate inventory cache
    } catch (error) {
      toast.error('Failed to import shipment');
    }
  };

  return (
    <Button onClick={handleImport} loading={isLoading}>
      Import Shipment
    </Button>
  );
}
```

### 4. Show Low Stock Alert

```typescript
function LowStockAlert() {
  const lowStockProducts = useAppSelector(selectLowStockProducts);

  if (lowStockProducts.length === 0) return null;

  return (
    <Alert variant="warning">
      {lowStockProducts.length} products are running low on stock!
      <ul>
        {lowStockProducts.slice(0, 5).map(product => (
          <li key={product.id}>{product.name}</li>
        ))}
      </ul>
    </Alert>
  );
}
```

### 5. Track Expiring Items

```typescript
function ExpirationDashboard() {
  const expiringSoon = useAppSelector(selectExpiringSoonInventoryItems);
  const expired = useAppSelector(selectExpiredInventoryItems);

  return (
    <div className="grid grid-cols-2 gap-4">
      <Card>
        <CardHeader>Expiring Soon (30 days)</CardHeader>
        <CardContent>{expiringSoon.length} items</CardContent>
      </Card>
      <Card>
        <CardHeader>Expired</CardHeader>
        <CardContent className="text-red-600">
          {expired.length} items
        </CardContent>
      </Card>
    </div>
  );
}
```

### 6. Update Filters

```typescript
function InventoryFilters() {
  const dispatch = useAppDispatch();
  const filters = useAppSelector(selectInventoryItemFilters);

  const handleFilterChange = (newFilters: Partial<InventoryItemFilters>) => {
    dispatch(setInventoryItemFilters({
      ...filters,
      ...newFilters
    }));
  };

  return (
    <form>
      <Select
        value={filters.facilityId}
        onChange={(e) => handleFilterChange({ facilityId: e.target.value })}
      >
        {/* Facility options */}
      </Select>

      <Select
        value={filters.statusId}
        onChange={(e) => handleFilterChange({ statusId: e.target.value })}
      >
        <option value="VALID">Valid</option>
        <option value="EXPIRED">Expired</option>
        <option value="DAMAGED">Damaged</option>
      </Select>
    </form>
  );
}
```

### 7. Manage Shipment Items

```typescript
function ShipmentItemManager({ shipmentId }: { shipmentId: string }) {
  const [addItem] = useAddItemToShipmentMutation();
  const [updateItem] = useUpdateItemInShipmentMutation();
  const [deleteItem] = useDeleteItemFromShipmentMutation();

  const handleAddItem = async (item: ShipmentItemForm) => {
    try {
      await addItem({ shipmentId, data: item }).unwrap();
      toast.success('Item added');
    } catch (error) {
      toast.error('Failed to add item');
    }
  };

  const handleUpdateItem = async (itemId: string, data: ShipmentItemForm) => {
    try {
      await updateItem({ shipmentId, itemId, data }).unwrap();
      toast.success('Item updated');
    } catch (error) {
      toast.error('Failed to update item');
    }
  };

  const handleDeleteItem = async (itemId: string) => {
    try {
      await deleteItem({ shipmentId, itemId }).unwrap();
      toast.success('Item removed');
    } catch (error) {
      toast.error('Failed to remove item');
    }
  };

  return <ShipmentItemList /* ... */ />;
}
```

### 8. Product Selection with Search

```typescript
function ProductSelector({ onSelect }: { onSelect: (product: Product) => void }) {
  const [search, setSearch] = useState('');

  const { data } = useGetProductsQuery({
    filters: { query: search, statusId: 'ACTIVE' },
    pagination: { page: 0, size: 10 }
  });

  return (
    <Combobox value={selected} onChange={onSelect}>
      <ComboboxInput
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search products..."
      />
      <ComboboxOptions>
        {data?.data?.items.map((product) => (
          <ComboboxOption key={product.id} value={product}>
            {product.name} - {product.skuCode}
          </ComboboxOption>
        ))}
      </ComboboxOptions>
    </Combobox>
  );
}
```

## 🎨 Status Constants

```typescript
// Use these for status filtering and display
export const INVENTORY_STATUS = {
  VALID: 'VALID',
  EXPIRED: 'EXPIRED',
  DAMAGED: 'DAMAGED',
} as const;

export const SHIPMENT_STATUS = {
  CREATED: 'CREATED',
  IMPORTED: 'IMPORTED',
  EXPORTED: 'EXPORTED',
} as const;

export const SHIPMENT_TYPE = {
  INBOUND: 'INBOUND',
  OUTBOUND: 'OUTBOUND',
} as const;
```

## 🔍 Useful Selectors

```typescript
// Inventory
selectInventoryItemItems; // All items
selectInventoryItemsByProduct; // Filter by product
selectInventoryItemsByFacility; // Filter by facility
selectInventoryItemsByStatus; // Filter by status
selectLowStockProducts; // Products with low stock
selectExpiredInventoryItems; // Expired items
selectExpiringSoonInventoryItems; // Expiring within 30 days
selectTotalInventoryValue; // Total $ value
selectTotalInventoryQuantity; // Total units

// Shipments
selectShipmentItems; // All shipments
selectShipmentsByStatus; // Filter by status
selectShipmentsByType; // Inbound/Outbound
selectShipmentsByOrder; // For specific order
selectTotalShipmentWeight; // Total weight
selectTotalShipmentQuantity; // Total quantity

// Products
selectProductItems; // All products
selectProductsByCategory; // Filter by category
selectProductsByStatus; // Active/Inactive

// Facilities
selectFacilityItems; // All facilities
selectDefaultFacility; // Get default facility
```

## 🔧 Common Actions

```typescript
// Filters
setInventoryItemFilters(filters)
setShipmentFilters(filters)
setProductFilters(filters)

// Pagination
setInventoryItemPagination({ page, size })
setShipmentPagination({ page, size })

// Selection (for bulk actions)
toggleInventoryItemSelection(id)
clearInventoryItemSelection()
setInventoryItemSelectedItems([...ids])

// UI State
setActiveModule('inventory' | 'shipments' | ...)
setViewMode('list' | 'grid' | 'kanban')
toggleFilterPanel()
```

## 💡 Pro Tips

1. **Cache Management**: RTK Query automatically caches. Use `refetch()` to refresh data.

2. **Optimistic Updates**: For better UX, use optimistic updates:

   ```typescript
   const [update] = useUpdateInventoryItemMutation();
   // RTK Query handles optimistic updates automatically
   ```

3. **Error Handling**: Always handle errors in mutations:

   ```typescript
   try {
     await mutation(data).unwrap();
   } catch (error) {
     // Handle error
   }
   ```

4. **Loading States**: Use `isLoading`, `isFetching` from queries:

   ```typescript
   const { data, isLoading, isFetching } = useGetInventoryItemsQuery(...);
   // isLoading: initial load
   // isFetching: any fetch including refetch
   ```

5. **Lazy Queries**: Use `useLazyQuery` for on-demand fetching:
   ```typescript
   const [trigger, result] = useLazyGetInventoryItemQuery();
   // Call trigger(params) when needed
   ```

## 📚 Further Reading

- Full API Documentation: `README_API.md`
- Implementation Details: `IMPLEMENTATION_SUMMARY.md`
- Architecture Guide: `/docs/MODULAR_ARCHITECTURE.md`
