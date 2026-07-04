'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useGetProductsQuery } from '../../api/salesApi';
import type { Product } from '../../types';
import { EditProductDialog } from './EditProductDialog';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { setProductPagination, setProductFilters } from '../../store';
import {
  selectProductPagination,
  selectProductFilters,
} from '../../store/selectors';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Plus, Search, Filter, Package } from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Badge } from '@/shared/components/ui/badge';
import { formatStringCurrencyVN } from '@/shared/utils/format';
import { useUser } from '@/modules/account';

export const ProductListPage: React.FC = () => {
  const router = useRouter();
  const dispatch = useAppDispatch();

  const { user } = useUser();

  const pagination = useAppSelector(selectProductPagination);
  const filters = useAppSelector(selectProductFilters);

  const { data, isLoading } = useGetProductsQuery({
    filters,
    pagination,
  });

  const [searchTerm, setSearchTerm] = React.useState(filters.query || '');
  const [selectedProduct, setSelectedProduct] = React.useState<Product | null>(
    null
  );
  const [editDialogOpen, setEditDialogOpen] = React.useState(false);

  const isSalesAdmin = user?.roles?.includes('SALES_ADMIN');

  const handleSearch = () => {
    dispatch(setProductFilters({ ...filters, query: searchTerm }));
  };

  const handlePageChange = (newPage: number) => {
    dispatch(setProductPagination({ ...pagination, page: newPage }));
  };

  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Sản phẩm</h1>
          <p className='text-muted-foreground'>Quản lý danh mục sản phẩm</p>
        </div>
        {isSalesAdmin && (
          <Button onClick={() => router.push('/sales/products/new')}>
            <Plus className='mr-2 h-4 w-4' />
            Thêm Sản Phẩm
          </Button>
        )}
      </div>

      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <CardTitle>Danh mục sản phẩm</CardTitle>
            <div className='flex items-center gap-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search products...'
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  className='pl-10 w-64'
                />
              </div>
              <Button variant='outline' size='icon' onClick={handleSearch}>
                <Search className='h-4 w-4' />
              </Button>
              <Button variant='outline' size='icon'>
                <Filter className='h-4 w-4' />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className='text-center py-8'>Đang tải...</div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Sản phẩm</TableHead>
                    <TableHead>SKU</TableHead>
                    <TableHead>Giá</TableHead>
                    <TableHead>Khả dụng</TableHead>
                    <TableHead className='text-right'>Trạng thái</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data?.data?.items?.map((product) => (
                    <TableRow
                      key={product.id}
                      onClick={() => {
                        setSelectedProduct(product);
                        if (isSalesAdmin) {
                          setEditDialogOpen(true);
                        }
                      }}
                      className='cursor-pointer hover:bg-muted/50'
                    >
                      <TableCell>
                        <div className='flex items-center gap-3'>
                          <div className='flex h-10 w-10 items-center justify-center rounded bg-muted'>
                            <Package className='h-5 w-5 text-muted-foreground' />
                          </div>
                          <div>
                            <p className='font-medium'>{product.name}</p>
                            <p className='text-sm text-muted-foreground'>
                              {product.unit}
                            </p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>{product.skuCode || '-'}</TableCell>
                      <TableCell>
                        {formatStringCurrencyVN(
                          product.retailPrice?.toString() || '0'
                        )}
                      </TableCell>
                      <TableCell>
                        <p className='font-medium'>
                          {product.quantityAvailable || 0}
                        </p>
                      </TableCell>
                      <TableCell className='text-right'>
                        <Badge
                          variant={
                            product.statusId === 'ACTIVE'
                              ? 'default'
                              : 'secondary'
                          }
                        >
                          {product.statusId || 'Unknown'}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                  {(!data?.data?.items || data.data.items.length === 0) && (
                    <TableRow>
                      <TableCell
                        colSpan={6}
                        className='text-center py-8 text-muted-foreground'
                      >
                        No products found
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>

              {/* Pagination */}
              {data?.data && data.data.totalItems > 0 && (
                <div className='flex items-center justify-between mt-4'>
                  <p className='text-sm text-muted-foreground'>
                    Showing{' '}
                    {data.data.currentPage * (pagination.size || 10) + 1} to{' '}
                    {Math.min(
                      (data.data.currentPage + 1) * (pagination.size || 10),
                      data.data.totalItems
                    )}{' '}
                    of {data.data.totalItems} results
                  </p>
                  <div className='flex gap-2'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => handlePageChange(pagination.page! - 1)}
                      disabled={pagination.page === 0}
                    >
                      Previous
                    </Button>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => handlePageChange(pagination.page! + 1)}
                      disabled={
                        data.data.currentPage >= data.data.totalPages - 1
                      }
                    >
                      Next
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      <EditProductDialog
        product={selectedProduct}
        open={editDialogOpen}
        onOpenChange={setEditDialogOpen}
      />
    </div>
  );
};
