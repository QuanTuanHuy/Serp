# Logistics Module API

## Tổng quan

Module Logistics cung cấp các API endpoints để quản lý:

- **Address**: Địa chỉ (cơ sở, giao hàng, kinh doanh)
- **Category**: Danh mục sản phẩm
- **Customer**: Khách hàng (read-only)
- **Facility**: Cơ sở/kho
- **Inventory Item**: Tồn kho
- **Order**: Đơn hàng (read-only)
- **Product**: Sản phẩm
- **Shipment**: Lô hàng nhập/xuất
- **Supplier**: Nhà cung cấp (read-only)

## Cấu trúc

```
logistics/
├── api/
│   └── logisticsApi.ts       # RTK Query endpoints
├── types/
│   └── index.ts              # TypeScript types
└── index.ts                  # Module exports
```

## Sử dụng

### Import

```typescript
import {
  useGetProductsQuery,
  useCreateProductMutation,
  useGetInventoryItemsQuery,
  useGetFacilitiesQuery,
  useGetShipmentsQuery,
  // ... other hooks
} from '@/modules/logistics';
```

### Ví dụ: Lấy danh sách sản phẩm

```typescript
const ProductList = () => {
  const { data, isLoading, error } = useGetProductsQuery({
    filters: {
      query: 'laptop',
      categoryId: 'cat-123',
      statusId: 'ACTIVE'
    },
    pagination: { page: 0, size: 20 }
  });

  const products = data?.data?.items || [];

  return (
    <div>
      {products.map(product => (
        <div key={product.id}>{product.name}</div>
      ))}
    </div>
  );
};
```

### Ví dụ: Tạo sản phẩm mới

```typescript
const CreateProduct = () => {
  const [createProduct, { isLoading }] = useCreateProductMutation();

  const handleSubmit = async (formData) => {
    try {
      await createProduct({
        name: 'Laptop Dell XPS 15',
        skuCode: 'DELL-XPS-15',
        categoryId: 'cat-123',
        retailPrice: 25000000,
        statusId: 'ACTIVE'
      }).unwrap();

      toast.success('Tạo sản phẩm thành công');
    } catch (error) {
      toast.error('Có lỗi xảy ra');
    }
  };

  return <form onSubmit={handleSubmit}>...</form>;
};
```

### Ví dụ: Quản lý tồn kho

```typescript
const InventoryManagement = () => {
  // Lấy danh sách tồn kho
  const { data: inventoryData } = useGetInventoryItemsQuery({
    filters: {
      productId: 'prod-123',
      facilityId: 'fac-456',
      statusId: 'VALID'
    },
    pagination: { page: 0, size: 50 }
  });

  // Tạo inventory item mới
  const [createInventoryItem] = useCreateInventoryItemMutation();

  // Cập nhật inventory item
  const [updateInventoryItem] = useUpdateInventoryItemMutation();

  const handleCreateInventory = async () => {
    await createInventoryItem({
      productId: 'prod-123',
      facilityId: 'fac-456',
      statusId: 'VALID',
      quantityOnHand: 100,
      lotId: 'LOT-2024-001',
      expireDate: '2025-12-31'
    });
  };

  return <div>...</div>;
};
```

### Ví dụ: Quản lý shipment (lô hàng)

```typescript
const ShipmentManagement = () => {
  const [createShipment] = useCreateShipmentMutation();
  const [addItemToShipment] = useAddItemToShipmentMutation();
  const [importShipment] = useImportShipmentMutation();

  // Tạo shipment mới
  const handleCreateShipment = async () => {
    const result = await createShipment({
      shipmentTypeId: 'INCOMING',
      facilityId: 'fac-456',
      supplierId: 'sup-789',
      estimatedArrivalDate: '2024-01-15',
      items: [
        {
          productId: 'prod-123',
          quantity: 100,
          unitPrice: 200000
        }
      ]
    }).unwrap();

    const shipmentId = result.data.id;

    // Thêm sản phẩm vào shipment
    await addItemToShipment({
      shipmentId,
      data: {
        productId: 'prod-456',
        quantity: 50,
        unitPrice: 150000
      }
    });

    // Nhập kho (import shipment)
    await importShipment({ shipmentId });
  };

  return <button onClick={handleCreateShipment}>Tạo shipment</button>;
};
```

## API Endpoints

### Products

- `getProducts` - Lấy danh sách sản phẩm
- `getProduct` - Lấy chi tiết sản phẩm
- `createProduct` - Tạo sản phẩm mới
- `updateProduct` - Cập nhật sản phẩm
- `deleteProduct` - Xóa sản phẩm

### Inventory Items

- `getInventoryItems` - Lấy danh sách tồn kho
- `getInventoryItem` - Lấy chi tiết tồn kho
- `createInventoryItem` - Tạo inventory item
- `updateInventoryItem` - Cập nhật inventory item
- `deleteInventoryItem` - Xóa inventory item

### Facilities

- `getFacilities` - Lấy danh sách cơ sở/kho
- `getFacility` - Lấy chi tiết cơ sở
- `createFacility` - Tạo cơ sở mới
- `updateFacility` - Cập nhật cơ sở
- `deleteFacility` - Xóa cơ sở

### Categories

- `getCategories` - Lấy danh sách danh mục
- `getCategory` - Lấy chi tiết danh mục
- `createCategory` - Tạo danh mục mới
- `updateCategory` - Cập nhật danh mục
- `deleteCategory` - Xóa danh mục

### Shipments

- `getShipments` - Lấy danh sách lô hàng
- `getShipment` - Lấy chi tiết lô hàng
- `createShipment` - Tạo lô hàng mới
- `updateShipment` - Cập nhật lô hàng
- `deleteShipment` - Xóa lô hàng
- `addItemToShipment` - Thêm sản phẩm vào lô hàng
- `updateItemInShipment` - Cập nhật sản phẩm trong lô hàng
- `deleteItemFromShipment` - Xóa sản phẩm khỏi lô hàng
- `importShipment` - Nhập kho (import shipment vào inventory)

### Addresses

- `createAddress` - Tạo địa chỉ mới
- `getAddressesByEntity` - Lấy địa chỉ theo entity
- `updateAddress` - Cập nhật địa chỉ

### Read-only Endpoints

- `getLogisticsCustomers` / `getLogisticsCustomer` - Xem khách hàng
- `getLogisticsOrders` / `getLogisticsOrder` - Xem đơn hàng
- `getSuppliers` / `getSupplier` - Xem nhà cung cấp

## Types

### Product

```typescript
interface Product {
  id: string;
  name: string;
  description?: string;
  skuCode?: string;
  barcode?: string;
  categoryId?: string;
  unitOfMeasure?: string;
  weight?: number;
  retailPrice?: number;
  wholesalePrice?: number;
  costPrice?: number;
  quantityAvailable?: number;
  reorderLevel?: number;
  statusId: 'ACTIVE' | 'INACTIVE';
}
```

### Inventory Item

```typescript
interface InventoryItem {
  id: string;
  productId: string;
  facilityId: string;
  statusId: 'VALID' | 'EXPIRED' | 'DAMAGED';
  quantityOnHand: number;
  quantityCommitted?: number;
  quantityReserved?: number;
  lotId?: string;
  manufactureDate?: string;
  expireDate?: string;
}
```

### Shipment

```typescript
interface Shipment {
  id: string;
  shipmentName?: string;
  shipmentTypeId: 'INCOMING' | 'OUTGOING';
  statusId: 'CREATED' | 'APPROVED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  facilityId: string;
  supplierId?: string;
  estimatedArrivalDate?: string;
  actualArrivalDate?: string;
  totalAmount?: number;
  items?: ShipmentItem[];
}
```

## Cache Invalidation

RTK Query tự động quản lý cache với các tag types:

- `logistics/Product`
- `logistics/InventoryItem`
- `logistics/Facility`
- `logistics/Category`
- `logistics/Shipment`
- `logistics/Address`
- `logistics/Customer` (read-only)
- `logistics/Order` (read-only)
- `logistics/Supplier` (read-only)

Khi mutation thành công, cache tương ứng sẽ tự động invalidate và refetch.

## API Gateway

Tất cả requests được route qua API Gateway:

- Base URL: `/logistics/api/v1`
- Authentication: JWT token trong header `Authorization: Bearer <token>`
- Service: `logistics` (port 8089)

## Lưu ý

1. **Read-only entities**: Customer, Order, Supplier chỉ có quyền đọc từ logistics module
2. **Import Shipment**: Khi import shipment, inventory items sẽ tự động được tạo/cập nhật
3. **Tag prefix**: Tất cả tags đều có prefix `logistics/` để tránh conflict với các module khác
4. **Error handling**: Sử dụng try-catch và toast để xử lý lỗi
5. **Pagination**: Mặc định page bắt đầu từ 0, size thường là 20-50 items

## Tích hợp với modules khác

- **Sales module**: Chia sẻ Customer, Product, Order types
- **Purchase module**: Chia sẻ Supplier, Product, Shipment concepts
- **Account module**: JWT authentication và user management
