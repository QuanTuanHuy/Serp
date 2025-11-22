/*
Author: QuanTuanHuy
Description: Part of Serp Project - Addresses management page
*/

'use client';

import { useMemo } from 'react';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Badge } from '@/shared/components/ui/badge';
import { Plus, MapPin } from 'lucide-react';
import { DataTable } from '@/shared/components/data-table';
import {
  useAddresses,
  AddressFormDialog,
  type Address,
} from '@/modules/purchase';

export default function AddressesPage() {
  const {
    addresses,
    isLoading,
    pagination,
    filters,
    dialogOpen,
    dialogMode,
    selectedAddress,
    handleEntityIdChange,
    handleEntityTypeChange,
    handleAddressTypeChange,
    handlePageChange,
    handlePageSizeChange,
    handleOpenCreateDialog,
    handleOpenEditDialog,
    handleCloseDialog,
    handleCreateAddress,
    handleUpdateAddress,
    isCreating,
    isUpdating,
  } = useAddresses();

  // Define table columns
  const columns = useMemo(
    () => [
      {
        id: 'id',
        header: 'ID',
        accessor: 'id',
        cell: ({ row }: { row: Address }) => (
          <span className='font-medium text-sm'>#{row.id.slice(0, 8)}</span>
        ),
      },
      {
        id: 'entity',
        header: 'Thực thể',
        accessor: (row: Address) => `${row.entityType}-${row.entityId}`,
        cell: ({ row }: { row: Address }) => (
          <div>
            <Badge variant='outline' className='text-xs'>
              {row.entityType}
            </Badge>
            <p className='text-sm mt-1'>{row.entityId}</p>
          </div>
        ),
      },
      {
        id: 'addressType',
        header: 'Loại địa chỉ',
        accessor: 'addressType',
        cell: ({ row }: { row: Address }) => {
          const typeMap: Record<string, string> = {
            shipping: 'Giao hàng',
            billing: 'Thanh toán',
            office: 'Văn phòng',
            warehouse: 'Kho',
          };
          return <span>{typeMap[row.addressType] || row.addressType}</span>;
        },
      },
      {
        id: 'fullAddress',
        header: 'Địa chỉ',
        accessor: 'fullAddress',
        cell: ({ row }: { row: Address }) => (
          <div className='max-w-md'>
            <div className='flex items-start gap-1'>
              <MapPin className='h-4 w-4 mt-0.5 flex-shrink-0' />
              <span className='text-sm'>{row.fullAddress}</span>
            </div>
            <p className='text-xs text-muted-foreground mt-1'>
              {row.latitude.toFixed(4)}, {row.longitude.toFixed(4)}
            </p>
          </div>
        ),
      },
      {
        id: 'isDefault',
        header: 'Mặc định',
        accessor: 'isDefault',
        cell: ({ row }: { row: Address }) =>
          row.isDefault ? (
            <Badge variant='default'>Mặc định</Badge>
          ) : (
            <Badge variant='outline'>Phụ</Badge>
          ),
      },
      {
        id: 'actions',
        header: 'Thao tác',
        accessor: 'id',
        cell: ({ row }: { row: Address }) => (
          <div className='flex gap-2'>
            <Button
              variant='outline'
              size='sm'
              onClick={() => handleOpenEditDialog(row.id)}
            >
              Sửa
            </Button>
          </div>
        ),
      },
    ],
    [handleOpenEditDialog]
  );

  return (
    <div className='container mx-auto py-6 space-y-6'>
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <div>
              <h2 className='text-2xl font-bold'>Quản lý địa chỉ</h2>
              <p className='text-muted-foreground'>
                Quản lý địa chỉ của nhà cung cấp, khách hàng và cơ sở
              </p>
            </div>
            <Button onClick={handleOpenCreateDialog}>
              <Plus className='mr-2 h-4 w-4' />
              Tạo địa chỉ
            </Button>
          </div>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Filters */}
          <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
            <Input
              placeholder='Tìm kiếm theo mã thực thể...'
              value={filters.entityId || ''}
              onChange={(e) => handleEntityIdChange(e.target.value)}
            />
            <Select
              value={filters.entityType || 'all'}
              onValueChange={(value) =>
                handleEntityTypeChange(value === 'all' ? undefined : value)
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Loại thực thể' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả</SelectItem>
                <SelectItem value='supplier'>Nhà cung cấp</SelectItem>
                <SelectItem value='customer'>Khách hàng</SelectItem>
                <SelectItem value='facility'>Cơ sở</SelectItem>
              </SelectContent>
            </Select>
            <Select
              value={filters.addressType || 'all'}
              onValueChange={(value) =>
                handleAddressTypeChange(value === 'all' ? undefined : value)
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Loại địa chỉ' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả</SelectItem>
                <SelectItem value='shipping'>Giao hàng</SelectItem>
                <SelectItem value='billing'>Thanh toán</SelectItem>
                <SelectItem value='office'>Văn phòng</SelectItem>
                <SelectItem value='warehouse'>Kho</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Data Table */}
          <DataTable
            columns={columns}
            data={addresses}
            isLoading={isLoading}
            pagination={{
              currentPage: pagination?.currentPage || 1,
              totalPages: pagination?.totalPages || 1,
              totalItems: pagination?.totalItems || 0,
              onPageChange: handlePageChange,
            }}
            keyExtractor={(row: Address) => row.id}
          />
        </CardContent>
      </Card>

      {/* Address Form Dialog */}
      <AddressFormDialog
        open={dialogOpen}
        onOpenChange={handleCloseDialog}
        address={selectedAddress}
        onSubmit={(data: any) => {
          if (dialogMode === 'create') {
            handleCreateAddress(data);
          } else if (selectedAddress) {
            handleUpdateAddress(selectedAddress.id, data);
          }
        }}
        isSubmitting={isCreating || isUpdating}
        mode={dialogMode}
      />
    </div>
  );
}
