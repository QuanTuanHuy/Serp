/**
 * Product List Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Product catalog with filters
 */

'use client';

import React from 'react';
import { useGetProductsQuery } from '../../api/salesApi';
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

export const ProductListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const pagination = useAppSelector(selectProductPagination);
  const filters = useAppSelector(selectProductFilters);

  const { data, isLoading } = useGetProductsQuery({
    filters,
    pagination,
  });

  const [searchTerm, setSearchTerm] = React.useState(filters.query || '');

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
          <h1 className='text-3xl font-bold tracking-tight'>Products</h1>
          <p className='text-muted-foreground'>Manage your product catalog</p>
        </div>
        <Button>
          <Plus className='mr-2 h-4 w-4' />
          Add Product
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <CardTitle>Product Catalog</CardTitle>
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
            <div className='text-center py-8'>Loading...</div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Product</TableHead>
                    <TableHead>SKU</TableHead>
                    <TableHead>Price</TableHead>
                    <TableHead>Stock</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data?.data?.items?.map((product) => (
                    <TableRow key={product.id}>
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
                        ${product.retailPrice?.toLocaleString() || '0'}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            (product.quantityAvailable || 0) > 10
                              ? 'default'
                              : 'destructive'
                          }
                        >
                          {product.quantityAvailable || 0}
                        </Badge>
                      </TableCell>
                      <TableCell>
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
                      <TableCell className='text-right'>
                        <Button variant='ghost' size='sm'>
                          Edit
                        </Button>
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
    </div>
  );
};
