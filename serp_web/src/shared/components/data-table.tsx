/*
Author: QuanTuanHuy
Description: Part of Serp Project - Reusable DataTable component with column visibility
*/

'use client';

import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { DataTableProps } from '../types/datatable.types';
import { useColumnVisibility } from '../hooks/use-column-visibility';
import { ColumnVisibilityMenu } from './column-visibility-menu';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import { cn } from '../utils';

export function DataTable<TData = any>({
  columns,
  data,
  keyExtractor,
  isLoading,
  error,
  onRowClick,
  storageKey,
  className,
  emptyState,
  loadingState,
  errorState,
  pagination,
}: DataTableProps<TData> & {
  emptyState?: React.ReactNode;
  loadingState?: React.ReactNode;
  errorState?: React.ReactNode;
}) {
  const {
    visibility,
    setColumnVisibility,
    resetVisibility,
    getVisibleColumns,
  } = useColumnVisibility(columns, storageKey);

  const visibleColumns = getVisibleColumns();

  // Get cell value from accessor
  const getCellValue = (row: TData, accessor: any) => {
    if (typeof accessor === 'function') {
      return accessor(row);
    }
    // Support nested properties like "user.email"
    return accessor
      .split('.')
      .reduce((obj: any, key: string) => obj?.[key], row);
  };

  // Render cell content
  const renderCell = (column: any, row: TData) => {
    const value = getCellValue(row, column.accessor);
    if (column.cell) {
      return column.cell({ row, value });
    }
    return value ?? '-';
  };

  return (
    <Card className={className}>
      <CardContent className='p-0'>
        {/* Column Visibility Menu - Positioned at top right of table */}
        <div className='flex justify-end p-4 border-b'>
          <ColumnVisibilityMenu
            columns={columns}
            visibility={visibility}
            onVisibilityChange={setColumnVisibility}
            onReset={resetVisibility}
          />
        </div>

        {/* Loading State */}
        {isLoading &&
          (loadingState || (
            <div className='flex items-center justify-center h-64'>
              <div className='text-muted-foreground'>Loading...</div>
            </div>
          ))}

        {/* Error State */}
        {error &&
          !isLoading &&
          (errorState || (
            <div className='flex items-center justify-center h-64'>
              <div className='text-destructive'>Failed to load data</div>
            </div>
          ))}

        {/* Table */}
        {!isLoading && !error && data && (
          <>
            <div className='overflow-x-auto'>
              <table className='w-full'>
                <thead className='border-b bg-muted/50'>
                  <tr>
                    {visibleColumns.map((column) => (
                      <th
                        key={column.id}
                        className={cn(
                          'px-4 py-3 text-sm font-medium',
                          column.align === 'right' && 'text-right',
                          column.align === 'center' && 'text-center',
                          !column.align && 'text-left'
                        )}
                        style={{ width: column.width }}
                      >
                        {column.header}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className='divide-y'>
                  {data.length === 0 ? (
                    <tr>
                      <td
                        colSpan={visibleColumns.length}
                        className='px-4 py-12'
                      >
                        {emptyState || (
                          <div className='text-center text-muted-foreground'>
                            No data available
                          </div>
                        )}
                      </td>
                    </tr>
                  ) : (
                    data.map((row) => (
                      <tr
                        key={keyExtractor(row)}
                        className={cn(
                          'hover:bg-muted/50 transition-colors',
                          onRowClick && 'cursor-pointer'
                        )}
                        onClick={() => onRowClick?.(row)}
                      >
                        {visibleColumns.map((column) => (
                          <td
                            key={column.id}
                            className={cn(
                              'px-4 py-3',
                              column.align === 'right' && 'text-right',
                              column.align === 'center' && 'text-center',
                              !column.align && 'text-left'
                            )}
                          >
                            {renderCell(column, row)}
                          </td>
                        ))}
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {pagination && (
              <div className='flex items-center justify-between border-t px-4 py-4'>
                <div className='text-sm text-muted-foreground'>
                  Showing {data.length} of {pagination.totalItems} items
                </div>

                <div className='flex items-center gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() =>
                      pagination.onPageChange(pagination.currentPage - 1)
                    }
                    disabled={
                      pagination.currentPage === 0 || pagination.isFetching
                    }
                  >
                    <ChevronLeft className='h-4 w-4' />
                    Previous
                  </Button>

                  <div className='text-sm'>
                    Page {pagination.currentPage + 1} of {pagination.totalPages}
                  </div>

                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() =>
                      pagination.onPageChange(pagination.currentPage + 1)
                    }
                    disabled={
                      pagination.currentPage >= pagination.totalPages - 1 ||
                      pagination.isFetching
                    }
                  >
                    Next
                    <ChevronRight className='h-4 w-4' />
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}
