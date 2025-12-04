/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Orders Page (Read-only)
*/

'use client';

import { useState, useMemo } from 'react';
import { useOrders, useSuppliers, useCustomers } from '@/modules/logistics';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Button,
  Badge,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  DataTable,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components';
import { Search, RefreshCw, Eye, MoreHorizontal, X } from 'lucide-react';
import { OrderDetailDialog } from '@/modules/logistics/components/orders';

export default function LogisticsOrdersPage() {
  const {
    orders,
    totalItems,
    totalPages,
    currentPage,
    isLoadingOrders,
    filters,
    setFilters,
    setPage,
    setPageSize,
    search,
    setCustomerFilter,
    setSupplierFilter,
    resetFilters,
  } = useOrders();

  const { suppliers } = useSuppliers();
  const { customers } = useCustomers();

  // Detail dialog state
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);

  const [searchInput, setSearchInput] = useState(filters.query || '');

  // Find selected order for detail view
  const selectedOrder = useMemo(
    () => orders.find((order) => order.id === selectedOrderId),
    [orders, selectedOrderId]
  );

  // Status badge color mapping
  const getStatusBadgeVariant = (
    statusId?: string
  ): 'default' | 'destructive' | 'outline' | 'secondary' => {
    switch (statusId?.toUpperCase()) {
      case 'CREATED':
        return 'default';
      case 'APPROVED':
        return 'outline';
      case 'CANCELLED':
        return 'destructive';
      case 'COMPLETED':
        return 'secondary';
      default:
        return 'secondary';
    }
  };

  // Order type badge color mapping
  const getOrderTypeBadgeVariant = (orderTypeId?: string) => {
    switch (orderTypeId?.toUpperCase()) {
      case 'PURCHASE':
        return 'default';
      case 'SALE':
        return 'outline';
      default:
        return 'secondary';
    }
  };

  // Format date
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  // Format currency
  const formatCurrency = (amount?: number) => {
    if (amount === undefined || amount === null) return '-';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  // Create supplier and customer maps
  const supplierMap = useMemo(() => {
    const map = new Map<string, string>();
    suppliers?.forEach((supplier) => {
      map.set(supplier.id, supplier.name);
    });
    return map;
  }, [suppliers]);

  const customerMap = useMemo(() => {
    const map = new Map<string, string>();
    customers?.forEach((customer) => {
      map.set(customer.id, customer.name);
    });
    return map;
  }, [customers]);

  // Define table columns
  const columns = useMemo(
    () => [
      {
        id: 'id',
        header: 'Mã đơn hàng',
        accessor: 'id',
        cell: ({ row }: any) => (
          <div className='font-mono text-xs'>{row.id.substring(0, 8)}</div>
        ),
      },
      {
        id: 'orderName',
        header: 'Tên đơn hàng',
        accessor: 'orderName',
        cell: ({ row }: any) => (
          <button
            onClick={() => {
              setSelectedOrderId(row.id);
              setDetailDialogOpen(true);
            }}
            className='text-sm text-primary hover:underline cursor-pointer font-medium'
          >
            {row.orderName || '-'}
          </button>
        ),
      },
      {
        id: 'orderTypeId',
        header: 'Loại đơn',
        accessor: 'orderTypeId',
        cell: ({ row }: any) => (
          <Badge
            variant={getOrderTypeBadgeVariant(row.orderTypeId)}
            className='text-xs'
          >
            {row.orderTypeId || 'N/A'}
          </Badge>
        ),
      },
      {
        id: 'orderDate',
        header: 'Ngày đặt',
        accessor: 'orderDate',
        cell: ({ row }: any) => (
          <div className='text-sm'>{formatDate(row.orderDate)}</div>
        ),
      },
      {
        id: 'supplier',
        header: 'Nhà cung cấp',
        accessor: 'fromSupplierId',
        cell: ({ row }: any) => {
          if (!row.fromSupplierId) {
            return <span className='text-sm text-muted-foreground'>-</span>;
          }
          const supplierName = supplierMap.get(row.fromSupplierId);
          return (
            <div className='text-sm'>{supplierName || row.fromSupplierId}</div>
          );
        },
      },
      {
        id: 'customer',
        header: 'Khách hàng',
        accessor: 'toCustomerId',
        cell: ({ row }: any) => {
          if (!row.toCustomerId) {
            return <span className='text-sm text-muted-foreground'>-</span>;
          }
          const customerName = customerMap.get(row.toCustomerId);
          return (
            <div className='text-sm'>{customerName || row.toCustomerId}</div>
          );
        },
      },
      {
        id: 'totalAmount',
        header: 'Tổng tiền',
        accessor: 'totalAmount',
        cell: ({ row }: any) => (
          <div className='text-sm font-medium'>
            {formatCurrency(row.totalAmount)}
          </div>
        ),
      },
      {
        id: 'statusId',
        header: 'Trạng thái',
        accessor: 'statusId',
        cell: ({ row }: any) => (
          <Badge
            variant={getStatusBadgeVariant(row.statusId)}
            className='text-xs'
          >
            {row.statusId || 'N/A'}
          </Badge>
        ),
      },
      {
        id: 'actions',
        header: '',
        accessor: 'id',
        cell: ({ row }: any) => {
          return (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant='ghost' size='sm' className='h-8 w-8 p-0'>
                  <MoreHorizontal className='h-4 w-4' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end'>
                <DropdownMenuItem
                  onClick={() => {
                    setSelectedOrderId(row.id);
                    setDetailDialogOpen(true);
                  }}
                >
                  <Eye className='mr-2 h-4 w-4' />
                  Xem chi tiết
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          );
        },
      },
    ],
    [supplierMap, customerMap]
  );

  const handleSearchInputChange = (value: string) => {
    setSearchInput(value);
    search(value || '');
  };

  const hasActiveFilters =
    filters.query ||
    filters.statusId ||
    filters.orderTypeId ||
    filters.toCustomerId ||
    filters.fromSupplierId;

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Đơn hàng</h1>
          <p className='text-muted-foreground'>
            Xem và tra cứu thông tin đơn hàng
          </p>
        </div>
        <div className='flex gap-2'>
          <Button
            variant='outline'
            size='sm'
            onClick={() => window.location.reload()}
          >
            <RefreshCw className='mr-2 h-4 w-4' />
            Làm mới
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <CardTitle className='text-base'>Bộ lọc</CardTitle>
            {hasActiveFilters && (
              <Button
                variant='ghost'
                size='sm'
                onClick={() => {
                  resetFilters();
                  setSearchInput('');
                }}
              >
                <X className='mr-2 h-4 w-4' />
                Xóa bộ lọc
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4'>
            {/* Search */}
            <div className='lg:col-span-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                <Input
                  placeholder='Tìm kiếm đơn hàng...'
                  value={searchInput}
                  onChange={(e) => handleSearchInputChange(e.target.value)}
                  className='pl-9'
                />
              </div>
            </div>

            {/* Order Type Filter */}
            <Select
              value={filters.orderTypeId || 'all'}
              onValueChange={(value) =>
                setFilters({
                  ...filters,
                  orderTypeId: value === 'all' ? undefined : value,
                  page: 1,
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Loại đơn hàng' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả loại</SelectItem>
                <SelectItem value='PURCHASE'>Mua hàng</SelectItem>
                <SelectItem value='SALE'>Bán hàng</SelectItem>
              </SelectContent>
            </Select>

            {/* Status Filter */}
            <Select
              value={filters.statusId || 'all'}
              onValueChange={(value) =>
                setFilters({
                  ...filters,
                  statusId: value === 'all' ? undefined : value,
                  page: 1,
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Trạng thái' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả trạng thái</SelectItem>
                <SelectItem value='CREATED'>Đã tạo</SelectItem>
                <SelectItem value='APPROVED'>Đã duyệt</SelectItem>
                <SelectItem value='CANCELLED'>Đã hủy</SelectItem>
                <SelectItem value='COMPLETED'>Hoàn thành</SelectItem>
              </SelectContent>
            </Select>

            {/* Supplier Filter */}
            <Select
              value={filters.fromSupplierId || 'all'}
              onValueChange={(value) => {
                setSupplierFilter(value === 'all' ? undefined : value);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder='Nhà cung cấp' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả nhà cung cấp</SelectItem>
                {suppliers?.map((supplier) => (
                  <SelectItem key={supplier.id} value={supplier.id}>
                    {supplier.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Customer Filter - Full width */}
          <div className='mt-4'>
            <Select
              value={filters.toCustomerId || 'all'}
              onValueChange={(value) => {
                setCustomerFilter(value === 'all' ? undefined : value);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder='Khách hàng' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả khách hàng</SelectItem>
                {customers?.map((customer) => (
                  <SelectItem key={customer.id} value={customer.id}>
                    {customer.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Orders Table */}
      <Card>
        <DataTable
          columns={columns}
          data={orders}
          isLoading={isLoadingOrders}
          keyExtractor={(row) => row.id}
          pagination={{
            currentPage: currentPage,
            totalPages: totalPages,
            totalItems: totalItems,
            onPageChange: (page: number) => {
              setPage(page);
            },
          }}
        />
      </Card>

      {/* Order Detail Dialog */}
      {selectedOrder && (
        <OrderDetailDialog
          open={detailDialogOpen}
          onOpenChange={setDetailDialogOpen}
          order={selectedOrder}
        />
      )}
    </div>
  );
}
