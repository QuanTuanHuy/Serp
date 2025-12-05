/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Inventory Page
*/

'use client';

import { useState, useMemo } from 'react';
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
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components';
import { Search, RefreshCw, X, Package, Trash2 } from 'lucide-react';
import {
  useInventoryItems,
  useProducts,
  useFacilities,
} from '@/modules/logistics/hooks';
import { InventoryItemDetailDialog } from '@/modules/logistics/components';
import { formatDate } from '@/shared/utils';
import { toast } from 'sonner';
import type { InventoryItem } from '@/modules/logistics/types';

export default function InventoryPage() {
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState<InventoryItem | null>(null);

  const {
    inventoryItems,
    totalItems,
    totalPages,
    currentPage,
    isLoadingInventoryItems,
    filters,
    setFilters,
    setPage,
    setPageSize,
    search,
    setProductFilter,
    setFacilityFilter,
    resetFilters,
    delete: deleteItem,
    isDeleting,
  } = useInventoryItems();

  // Load products and facilities for name lookups
  const { products, isLoadingProducts } = useProducts();
  const { facilities, isLoadingFacilities } = useFacilities();

  // Create lookup maps for efficient name resolution
  const productNameMap = useMemo(() => {
    if (!products) return new Map<string, string>();
    return new Map(products.map((p) => [p.id, p.name]));
  }, [products]);

  const facilityNameMap = useMemo(() => {
    if (!facilities) return new Map<string, string>();
    return new Map(facilities.map((f) => [f.id, f.name]));
  }, [facilities]);

  const getProductName = (productId: string) => {
    return productNameMap.get(productId) || productId;
  };

  const getFacilityName = (facilityId: string) => {
    return facilityNameMap.get(facilityId) || facilityId;
  };

  const handleSearch = () => {
    if (filters.query) {
      search(filters.query);
    }
  };

  const handleResetFilters = () => {
    resetFilters();
  };

  const handleViewDetail = (itemId: string) => {
    setSelectedItemId(itemId);
    setDetailDialogOpen(true);
  };

  const handleDeleteClick = (item: InventoryItem) => {
    setItemToDelete(item);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!itemToDelete) return;

    try {
      const result = await deleteItem(itemToDelete.id);
      if (result.success) {
        toast.success('Xóa tồn kho thành công');
        setDeleteDialogOpen(false);
        setItemToDelete(null);
      } else {
        toast.error('Xóa tồn kho thất bại');
      }
    } catch (error) {
      toast.error('Xóa tồn kho thất bại');
    }
  };

  const getStatusBadge = (statusId: string) => {
    const statusMap: Record<string, { label: string; variant: any }> = {
      AVAILABLE: { label: 'Có sẵn', variant: 'success' },
      RESERVED: { label: 'Đã đặt', variant: 'warning' },
      IN_TRANSIT: { label: 'Đang vận chuyển', variant: 'default' },
      DAMAGED: { label: 'Hỏng', variant: 'destructive' },
      EXPIRED: { label: 'Hết hạn', variant: 'destructive' },
    };

    const status = statusMap[statusId] || {
      label: statusId,
      variant: 'secondary',
    };
    return <Badge variant={status.variant}>{status.label}</Badge>;
  };

  return (
    <div className='container mx-auto py-6 space-y-6'>
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Quản lý tồn kho</h1>
          <p className='text-muted-foreground'>
            Xem và theo dõi tình trạng tồn kho
          </p>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Tìm kiếm và lọc</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid gap-4 md:grid-cols-4'>
            <div className='md:col-span-2'>
              <div className='flex gap-2'>
                <div className='relative flex-1'>
                  <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                  <Input
                    placeholder='Tìm theo mã, sản phẩm, lô hàng...'
                    value={filters.query || ''}
                    onChange={(e) =>
                      setFilters({ ...filters, query: e.target.value })
                    }
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    className='pl-9'
                  />
                </div>
                <Button onClick={handleSearch}>
                  <Search className='h-4 w-4' />
                </Button>
              </div>
            </div>

            <Select
              value={filters.facilityId || 'all'}
              onValueChange={(value) =>
                setFacilityFilter(value === 'all' ? '' : value)
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Tất cả kho' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả kho</SelectItem>
                {facilities?.map((facility) => (
                  <SelectItem key={facility.id} value={facility.id}>
                    {facility.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select
              value={filters.statusId || 'all'}
              onValueChange={(value) =>
                setFilters({
                  ...filters,
                  statusId: value === 'all' ? undefined : value,
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Tất cả trạng thái' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Tất cả trạng thái</SelectItem>
                <SelectItem value='AVAILABLE'>Có sẵn</SelectItem>
                <SelectItem value='RESERVED'>Đã đặt</SelectItem>
                <SelectItem value='IN_TRANSIT'>Đang vận chuyển</SelectItem>
                <SelectItem value='DAMAGED'>Hỏng</SelectItem>
                <SelectItem value='EXPIRED'>Hết hạn</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {(filters.query || filters.facilityId || filters.statusId) && (
            <div className='mt-4 flex items-center gap-2'>
              <span className='text-sm text-muted-foreground'>
                Đang áp dụng bộ lọc
              </span>
              <Button
                variant='ghost'
                size='sm'
                onClick={handleResetFilters}
                className='h-8'
              >
                <X className='mr-2 h-4 w-4' />
                Xóa bộ lọc
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Data Table */}
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <div>
              <CardTitle>Danh sách tồn kho</CardTitle>
              <p className='text-sm text-muted-foreground mt-1'>
                Tổng số: {totalItems} mục
              </p>
            </div>
            <Button variant='outline' size='sm' onClick={() => {}}>
              <RefreshCw className='mr-2 h-4 w-4' />
              Làm mới
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoadingInventoryItems ||
          isLoadingProducts ||
          isLoadingFacilities ? (
            <div className='flex items-center justify-center py-12'>
              <div className='text-center space-y-3'>
                <Package className='h-12 w-12 mx-auto animate-pulse text-muted-foreground' />
                <p className='text-sm text-muted-foreground'>
                  Đang tải dữ liệu...
                </p>
              </div>
            </div>
          ) : inventoryItems.length === 0 ? (
            <div className='flex flex-col items-center justify-center py-12 text-center'>
              <Package className='h-12 w-12 text-muted-foreground mb-4' />
              <h3 className='text-lg font-semibold mb-2'>
                Không tìm thấy dữ liệu
              </h3>
              <p className='text-sm text-muted-foreground'>
                Không có mục tồn kho nào phù hợp với bộ lọc hiện tại
              </p>
            </div>
          ) : (
            <div className='space-y-4'>
              <div className='border rounded-lg overflow-hidden'>
                <table className='w-full'>
                  <thead className='bg-muted/50'>
                    <tr>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Mã tồn kho
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Sản phẩm
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Lô hàng
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Số lượng
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Kho
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Ngày nhận
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        NSX
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        HSD
                      </th>
                      <th className='px-4 py-3 text-left text-sm font-medium'>
                        Trạng thái
                      </th>
                      <th className='px-4 py-3 text-right text-sm font-medium'>
                        Thao tác
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {inventoryItems.map((item) => (
                      <tr key={item.id} className='border-t hover:bg-muted/30'>
                        <td
                          className='px-4 py-3 text-sm font-mono'
                          title={item.id}
                        >
                          {item.id.substring(0, 10)}
                        </td>
                        <td className='px-4 py-3 text-sm'>
                          <button
                            onClick={() => handleViewDetail(item.id)}
                            className='font-medium text-primary hover:underline cursor-pointer text-left'
                          >
                            {getProductName(item.productId)}
                          </button>
                        </td>
                        <td className='px-4 py-3 text-sm'>
                          {item.lotId || '-'}
                        </td>
                        <td className='px-4 py-3 text-sm font-semibold'>
                          {item.quantity}
                        </td>
                        <td className='px-4 py-3 text-sm'>
                          {getFacilityName(item.facilityId)}
                        </td>
                        <td className='px-4 py-3 text-sm'>
                          {item.receivedDate
                            ? formatDate(item.receivedDate)
                            : '-'}
                        </td>
                        <td className='px-4 py-3 text-sm'>
                          {item.manufacturingDate
                            ? formatDate(item.manufacturingDate)
                            : '-'}
                        </td>
                        <td className='px-4 py-3 text-sm'>
                          {item.expirationDate
                            ? formatDate(item.expirationDate)
                            : '-'}
                        </td>
                        <td className='px-4 py-3 text-sm'>
                          {getStatusBadge(item.statusId)}
                        </td>
                        <td className='px-4 py-3 text-sm text-right'>
                          <Button
                            variant='ghost'
                            size='sm'
                            onClick={() => handleDeleteClick(item)}
                          >
                            <Trash2 className='h-4 w-4 text-destructive' />
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {totalPages > 1 && (
                <div className='flex items-center justify-between'>
                  <p className='text-sm text-muted-foreground'>
                    Trang {currentPage} / {totalPages}
                  </p>
                  <div className='flex gap-2'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setPage(currentPage - 1)}
                      disabled={currentPage === 1}
                    >
                      Trước
                    </Button>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setPage(currentPage + 1)}
                      disabled={currentPage === totalPages}
                    >
                      Sau
                    </Button>
                  </div>
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Detail Dialog */}
      <InventoryItemDetailDialog
        open={detailDialogOpen}
        onOpenChange={setDetailDialogOpen}
        inventoryItemId={selectedItemId}
      />

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận xóa</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn xóa tồn kho này không? Hành động này không
              thể hoàn tác.
              {itemToDelete && (
                <div className='mt-3 space-y-1 text-sm text-foreground'>
                  <p>
                    <strong>Sản phẩm:</strong>{' '}
                    {getProductName(itemToDelete.productId)}
                  </p>
                  <p>
                    <strong>Kho:</strong>{' '}
                    {getFacilityName(itemToDelete.facilityId)}
                  </p>
                  <p>
                    <strong>Số lượng:</strong> {itemToDelete.quantity}
                  </p>
                </div>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteConfirm}
              disabled={isDeleting}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isDeleting ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
