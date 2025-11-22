/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase Orders Page
*/

'use client';

import React, { useMemo, useState } from 'react';
import {
  useOrders,
  OrderFormDialog,
  type OrderDetail,
  useGetSuppliersQuery,
} from '@/modules/purchase';
import { useProducts } from '@/modules/purchase';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
  Badge,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { DataTable } from '@/shared/components';
import {
  Plus,
  Search,
  Filter,
  MoreVertical,
  Eye,
  CheckCircle,
  XCircle,
  RefreshCw,
} from 'lucide-react';

export default function OrdersPage() {
  const {
    orders,
    selectedOrder,
    pagination,
    isLoading,
    isFetching,
    isCreating,
    isUpdating,
    isApproving,
    isCancelling,
    filters,
    handleQueryChange,
    handleStatusChange,
    handleOrderTypeChange,
    handlePageChange,
    handlePageSizeChange,
    handleResetFilters,
    dialogOpen,
    dialogMode,
    handleOpenCreateDialog,
    handleOpenEditDialog,
    handleCloseDialog,
    handleCreateOrder,
    handleUpdateOrder,
    handleApproveOrder,
    handleCancelOrder,
    refetch,
  } = useOrders();

  const { products } = useProducts();

  // Fetch all suppliers for mapping
  const { data: suppliersData } = useGetSuppliersQuery({
    page: 1,
    size: 1000, // Fetch enough suppliers for lookup
  });

  // Create supplier ID to name map
  const supplierMap = useMemo(() => {
    const map = new Map<string, string>();
    suppliersData?.data?.items?.forEach((supplier) => {
      map.set(supplier.id, supplier.name);
    });
    return map;
  }, [suppliersData]);

  const [searchInput, setSearchInput] = useState(filters.query || '');

  // Status badge color mapping
  const getStatusBadgeVariant = (statusId: string) => {
    switch (statusId?.toUpperCase()) {
      case 'CREATED':
        return 'default';
      case 'APPROVED':
        return 'outline';
      case 'CANCELLED':
        return 'destructive';
      default:
        return 'secondary';
    }
  };

  // Format date
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  // Define table columns
  const columns = useMemo(
    () => [
      {
        id: 'id',
        header: 'Order ID',
        accessor: 'id',
        cell: ({ row }: any) => (
          <div className='font-mono text-sm'>{row.id.substring(0, 8)}</div>
        ),
      },
      {
        id: 'orderTypeId',
        header: 'Type',
        accessor: 'orderTypeId',
        cell: ({ row }: any) => {
          const type = row.orderTypeId;
          return (
            <Badge variant='outline'>
              {type === 'PURCHASE'
                ? 'Purchase'
                : type === 'SALES'
                  ? 'Sales'
                  : 'Transfer'}
            </Badge>
          );
        },
      },
      {
        id: 'orderName',
        header: 'Order Name',
        accessor: 'orderName',
        cell: ({ row }: any) => <div className='text-sm'>{row.orderName}</div>,
      },
      {
        id: 'orderDate',
        header: 'Order Date',
        accessor: 'orderDate',
        cell: ({ row }: any) => (
          <div className='text-sm'>{formatDate(row.orderDate)}</div>
        ),
      },
      {
        id: 'supplier',
        header: 'Supplier',
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
        id: 'statusId',
        header: 'Status',
        accessor: 'statusId',
        cell: ({ row }: any) => (
          <Badge variant={getStatusBadgeVariant(row.statusId)}>
            {row.statusId || 'Unknown'}
          </Badge>
        ),
      },
      {
        id: 'actions',
        header: '',
        accessor: 'id',
        cell: ({ row }: any) => (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='ghost' size='sm'>
                <MoreVertical className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem onClick={() => handleOpenEditDialog(row.id)}>
                <Eye className='mr-2 h-4 w-4' />
                View Details
              </DropdownMenuItem>
              {row.statusId?.toUpperCase() === 'CREATED' && (
                <DropdownMenuItem
                  onClick={() => handleApproveOrder(row.id)}
                  disabled={isApproving}
                >
                  <CheckCircle className='mr-2 h-4 w-4' />
                  Approve Order
                </DropdownMenuItem>
              )}
              {['CREATED', 'PENDING', 'APPROVED'].includes(
                row.statusId?.toUpperCase()
              ) && (
                <DropdownMenuItem
                  onClick={() => handleCancelOrder(row.id)}
                  disabled={isCancelling}
                  className='text-destructive'
                >
                  <XCircle className='mr-2 h-4 w-4' />
                  Cancel Order
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        ),
      },
    ],
    [
      handleOpenEditDialog,
      handleApproveOrder,
      handleCancelOrder,
      isApproving,
      isCancelling,
      supplierMap,
    ]
  );

  const handleSearchInputChange = (value: string) => {
    setSearchInput(value);
    handleQueryChange(value || undefined);
  };

  const handleFormSubmit = async (data: any) => {
    if (dialogMode === 'edit' && selectedOrder) {
      await handleUpdateOrder(selectedOrder.id, data);
    } else {
      await handleCreateOrder(data);
    }
  };

  return (
    <div className='space-y-6 p-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Orders</h1>
          <p className='text-muted-foreground'>
            Manage purchase orders, sales orders, and transfers
          </p>
        </div>
        <div className='flex gap-2'>
          <Button variant='outline' size='icon' onClick={() => refetch()}>
            <RefreshCw className='h-4 w-4' />
          </Button>
          <Button onClick={handleOpenCreateDialog}>
            <Plus className='mr-2 h-4 w-4' />
            Create Order
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Filter className='h-5 w-5' />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-1 md:grid-cols-4 gap-2'>
            {/* Search */}
            <div className='md:col-span-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                <Input
                  placeholder='Search orders...'
                  value={searchInput}
                  onChange={(e) => handleSearchInputChange(e.target.value)}
                  className='pl-10'
                />
              </div>
            </div>

            {/* Status Filter */}
            <Select
              value={filters.statusId || 'all'}
              onValueChange={(value) =>
                handleStatusChange(value === 'all' ? undefined : value)
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Status' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>All Statuses</SelectItem>
                <SelectItem value='CREATED'>Created</SelectItem>
                <SelectItem value='PENDING'>Pending</SelectItem>
                <SelectItem value='APPROVED'>Approved</SelectItem>
                <SelectItem value='CONFIRMED'>Confirmed</SelectItem>
                <SelectItem value='COMPLETED'>Completed</SelectItem>
                <SelectItem value='CANCELLED'>Cancelled</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Reset Filters */}
          {(filters.query || filters.statusId) && (
            <div className='mt-4'>
              <Button variant='outline' size='sm' onClick={handleResetFilters}>
                Reset Filters
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Orders Table */}
      <Card>
        <CardContent className='p-0'>
          <DataTable
            columns={columns}
            data={orders}
            keyExtractor={(row: any) => row.id}
            pagination={
              pagination
                ? {
                    currentPage: pagination.currentPage,
                    totalPages: pagination.totalPages,
                    totalItems: pagination.totalItems,
                    onPageChange: handlePageChange,
                  }
                : undefined
            }
            isLoading={isLoading || isFetching}
          />
        </CardContent>
      </Card>

      {/* Order Form Dialog */}
      <OrderFormDialog
        open={dialogOpen}
        onOpenChange={handleCloseDialog}
        order={dialogMode === 'edit' ? selectedOrder : undefined}
        products={products}
        onSubmit={handleFormSubmit}
        isLoading={isCreating || isUpdating}
      />
    </div>
  );
}
