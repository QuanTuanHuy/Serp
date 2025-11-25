/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase Products Page
*/

'use client';

import React, { useMemo, useState } from 'react';
import {
  useProducts,
  ProductFormDialog,
  type Product,
} from '@/modules/purchase';
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
  Pencil,
  Trash2,
  Grid3x3,
  List,
  RefreshCw,
} from 'lucide-react';

export default function ProductsPage() {
  const {
    products,
    selectedProduct,
    pagination,
    categories,
    isLoading,
    isFetching,
    isLoadingProduct,
    isCreating,
    isUpdating,
    isDeleting,
    filters,
    handleQueryChange,
    handleCategoryChange,
    handleStatusChange,
    handlePageChange,
    handlePageSizeChange,
    handleSortingChange,
    handleResetFilters,
    dialogOpen,
    dialogMode,
    handleOpenCreateDialog,
    handleOpenEditDialog,
    handleCloseDialog,
    handleCreateProduct,
    handleUpdateProduct,
    handleDeleteProduct,
    refetch,
  } = useProducts();

  const [searchInput, setSearchInput] = useState(filters.query || '');
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');

  // Define table columns
  const columns = useMemo(
    () => [
      {
        id: 'name',
        header: 'Tên sản phẩm',
        accessor: 'name',
        cell: ({ row }: any) => <div className='font-medium'>{row.name}</div>,
      },
      {
        id: 'unit',
        header: 'Đơn vị',
        accessor: 'unit',
        cell: ({ row }: any) => (
          <div className='text-sm text-muted-foreground'>{row.unit}</div>
        ),
      },
      {
        id: 'costPrice',
        header: 'Giá vốn',
        accessor: 'costPrice',
        cell: ({ row }: any) => (
          <div className='text-sm'>{row.costPrice.toLocaleString()}</div>
        ),
      },
      {
        id: 'wholeSalePrice',
        header: 'Giá bán sỉ',
        accessor: 'wholeSalePrice',
        cell: ({ row }: any) => (
          <div className='text-sm'>
            {(row.wholeSalePrice || 0).toLocaleString()}
          </div>
        ),
      },
      {
        id: 'categoryId',
        header: 'Danh mục',
        accessor: 'categoryId',
        cell: ({ row }: any) => {
          const category = categories.find((c) => c.id === row.categoryId);
          return (
            <div className='text-sm text-muted-foreground'>
              {category?.name || '-'}
            </div>
          );
        },
      },
      {
        id: 'statusId',
        header: 'Trạng thái',
        accessor: 'statusId',
        cell: ({ row }: any) => {
          const status = row.statusId;
          return (
            <Badge variant={status === 'ACTIVE' ? 'default' : 'secondary'}>
              {status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'}
            </Badge>
          );
        },
      },
      {
        id: 'actions',
        header: 'Hành động',
        accessor: () => '',
        cell: ({ row }: any) => (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='ghost' size='sm'>
                <MoreVertical className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem onClick={() => handleOpenEditDialog(row.id)}>
                <Pencil className='mr-2 h-4 w-4' />
                Chỉnh sửa
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => {
                  if (
                    window.confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')
                  ) {
                    handleDeleteProduct(row.id);
                  }
                }}
                className='text-red-600'
                disabled={isDeleting}
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Xóa
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ),
      },
    ],
    [categories, handleOpenEditDialog, handleDeleteProduct, isDeleting]
  );

  const handleSearchInputChange = (value: string) => {
    setSearchInput(value);
    handleQueryChange(value || undefined);
  };

  const handleFormSubmit = async (data: any) => {
    if (dialogMode === 'create') {
      await handleCreateProduct(data);
    } else if (selectedProduct) {
      await handleUpdateProduct(selectedProduct.id, data);
    }
  };

  return (
    <div className='flex flex-col gap-4 p-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Sản phẩm</h1>
          <p className='text-muted-foreground'>Quản lý danh mục sản phẩm</p>
        </div>
        <Button onClick={handleOpenCreateDialog}>
          <Plus className='mr-2 h-4 w-4' />
          Thêm sản phẩm
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className='text-lg'>Bộ lọc</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4'>
            {/* Left side - Search and Filters Group */}
            <div className='flex flex-col sm:flex-row gap-2 flex-1'>
              {/* Search */}
              <div className='flex-1 max-w-md'>
                <div className='relative'>
                  <Search className='absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                  <Input
                    placeholder='Tìm kiếm sản phẩm...'
                    value={searchInput}
                    onChange={(e) => handleSearchInputChange(e.target.value)}
                    className='pl-10'
                  />
                </div>
              </div>

              {/* Category Filter */}
              <div className='w-full sm:w-44'>
                <Select
                  value={filters.categoryId || 'ALL'}
                  onValueChange={(value) =>
                    handleCategoryChange(value === 'ALL' ? undefined : value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Tất cả danh mục' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='ALL'>Tất cả danh mục</SelectItem>
                    {categories.map((category) => (
                      <SelectItem key={category.id} value={category.id}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Status Filter */}
              <div className='w-full sm:w-44'>
                <Select
                  value={filters.statusId || 'ALL'}
                  onValueChange={(value) =>
                    handleStatusChange(value === 'ALL' ? undefined : value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Tất cả trạng thái' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='ALL'>Tất cả trạng thái</SelectItem>
                    <SelectItem value='ACTIVE'>Hoạt động</SelectItem>
                    <SelectItem value='INACTIVE'>Không hoạt động</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Reset Button */}
              <Button
                variant='outline'
                onClick={handleResetFilters}
                className='sm:w-auto'
              >
                Đặt lại
              </Button>
            </div>

            {/* Right side - Refetch */}
            <div className='flex justify-end'>
              <Button
                variant='outline'
                size='icon'
                onClick={() => refetch()}
                disabled={isFetching}
              >
                <RefreshCw
                  className={`h-4 w-4 ${isFetching ? 'animate-spin' : ''}`}
                />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Data Table */}
      <Card>
        <CardContent className='pt-6'>
          {isLoading ? (
            <div className='flex items-center justify-center h-64'>
              <RefreshCw className='h-8 w-8 animate-spin text-muted-foreground' />
            </div>
          ) : (
            <DataTable
              columns={columns}
              data={products}
              keyExtractor={(product: Product) => product.id}
              pagination={
                pagination
                  ? {
                      currentPage: pagination.currentPage,
                      totalPages: pagination.totalPages,
                      totalItems: pagination.totalItems,
                      onPageChange: handlePageChange,
                      isFetching,
                    }
                  : undefined
              }
              isLoading={isFetching}
            />
          )}
        </CardContent>
      </Card>

      {/* Product Form Dialog */}
      <ProductFormDialog
        open={dialogOpen}
        onOpenChange={handleCloseDialog}
        product={
          dialogMode === 'edit' && !isLoadingProduct
            ? selectedProduct
            : undefined
        }
        categories={categories}
        onSubmit={handleFormSubmit}
        isLoading={isCreating || isUpdating || isLoadingProduct}
      />
    </div>
  );
}
