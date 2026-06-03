/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile import history page
 */

'use client';

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { Loader2, RefreshCw } from 'lucide-react';
import {
  useGetImportHistoriesQuery,
  useGetOrderImportHistoriesQuery,
} from '../../api';
import type { ImportType } from '../../types';

const PAGE_SIZE = 20;

export const ImportHistoryListPage: React.FC = () => {
  const [page, setPage] = React.useState(0);
  const [type, setType] = React.useState<ImportType | undefined>(undefined);
  const isOrderHistory = type === 'ORDER';

  const firstMileHistory = useGetImportHistoriesQuery(
    {
      page,
      size: PAGE_SIZE,
      type,
    },
    { skip: isOrderHistory }
  );
  const orderHistory = useGetOrderImportHistoriesQuery(
    {
      page,
      size: PAGE_SIZE,
    },
    { skip: !isOrderHistory }
  );

  const data = isOrderHistory ? orderHistory.data : firstMileHistory.data;
  const isLoading = isOrderHistory
    ? orderHistory.isLoading
    : firstMileHistory.isLoading;
  const isFetching = isOrderHistory
    ? orderHistory.isFetching
    : firstMileHistory.isFetching;
  const refetch = isOrderHistory
    ? orderHistory.refetch
    : firstMileHistory.refetch;

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Import History</h1>
        <p className='text-muted-foreground'>
          Track asynchronous import jobs across TMS services.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Choose the history source or import type.</CardDescription>
        </CardHeader>
        <CardContent className='flex flex-wrap items-center gap-2'>
          <Button
            variant={type === undefined ? 'default' : 'outline'}
            onClick={() => {
              setPage(0);
              setType(undefined);
            }}
          >
            First-mile
          </Button>
          <Button
            variant={type === 'ORDER' ? 'default' : 'outline'}
            onClick={() => {
              setPage(0);
              setType('ORDER');
            }}
          >
            Order
          </Button>
          <Button
            variant={type === 'POST_OFFICE' ? 'default' : 'outline'}
            onClick={() => {
              setPage(0);
              setType('POST_OFFICE');
            }}
          >
            Post Office
          </Button>
          <Button
            variant={type === 'VEHICLE' ? 'default' : 'outline'}
            onClick={() => {
              setPage(0);
              setType('VEHICLE');
            }}
          >
            Vehicle
          </Button>
          <Button
            variant='outline'
            onClick={() => {
              void refetch();
            }}
            disabled={isFetching}
          >
            <RefreshCw className='h-4 w-4 mr-2' />
            Refresh
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className='flex items-center gap-2 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Loading import history...
            </div>
          ) : data && data.items.length > 0 ? (
            <div className='space-y-3'>
              {data.items.map((item) => (
                <div key={item.id} className='rounded-lg border p-3'>
                  <p className='font-medium'>{item.file_name}</p>
                  <p className='text-sm text-muted-foreground'>
                    Status: {item.status} | Type: {item.type}
                  </p>
                  <p className='text-sm text-muted-foreground'>
                    Success/Failed: {item.success_records}/{item.failed_records}
                  </p>
                  {item.error_message && (
                    <p className='text-xs text-destructive mt-1'>
                      {item.error_message}
                    </p>
                  )}
                </div>
              ))}

              <div className='flex items-center justify-between pt-2'>
                <Button
                  variant='outline'
                  onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                  disabled={!data.hasPrevious || isFetching}
                >
                  Previous
                </Button>
                <span className='text-sm text-muted-foreground'>
                  Page {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
                </span>
                <Button
                  variant='outline'
                  onClick={() => setPage((prev) => prev + 1)}
                  disabled={!data.hasNext || isFetching}
                >
                  Next
                </Button>
              </div>
            </div>
          ) : (
            <p className='text-muted-foreground'>No import history found.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
