/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile import history page
 */

'use client';

import React from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { Loader2, RefreshCw } from 'lucide-react';
import {
  useGetImportHistoriesQuery,
  useGetOrderImportHistoriesQuery,
} from '../../api';
import type { ImportHistoryStatus, ImportType } from '../../types';

const PAGE_SIZE = 20;

type ImportHistorySource = 'FIRST_MILE' | 'ORDER';

const SOURCE_OPTIONS: Array<{
  value: ImportHistorySource;
  label: string;
}> = [
  { value: 'FIRST_MILE', label: 'First-mile' },
  { value: 'ORDER', label: 'Orders' },
];

const FIRST_MILE_TYPE_OPTIONS: Array<{
  value: ImportType | undefined;
  label: string;
}> = [
  { value: undefined, label: 'All first-mile' },
  { value: 'POST_OFFICE', label: 'Post offices' },
  { value: 'VEHICLE', label: 'First-mile vehicles' },
];

const STATUS_VARIANTS: Record<
  ImportHistoryStatus,
  'default' | 'secondary' | 'destructive' | 'outline'
> = {
  PENDING: 'outline',
  PROCESSING: 'secondary',
  COMPLETED: 'default',
  PARTIAL_SUCCESS: 'secondary',
  FAILED: 'destructive',
};

function formatImportType(type?: ImportType): string {
  if (type === 'POST_OFFICE') return 'Post office';
  if (type === 'VEHICLE') return 'Vehicle';
  if (type === 'ORDER') return 'Order';
  if (type === 'HUB') return 'Hub';
  return '-';
}

function formatDateTime(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('en-US');
}

export const ImportHistoryListPage: React.FC = () => {
  const [page, setPage] = React.useState(0);
  const [source, setSource] = React.useState<ImportHistorySource>('FIRST_MILE');
  const [firstMileType, setFirstMileType] = React.useState<
    ImportType | undefined
  >(undefined);
  const isOrderHistory = source === 'ORDER';

  const firstMileHistory = useGetImportHistoriesQuery(
    {
      page,
      size: PAGE_SIZE,
      type: firstMileType,
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
          Track asynchronous import jobs in one place.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Choose a source and import type.</CardDescription>
        </CardHeader>
        <CardContent className='space-y-3'>
          <div className='flex flex-wrap items-center gap-2'>
            {SOURCE_OPTIONS.map((option) => (
              <Button
                key={option.value}
                variant={source === option.value ? 'default' : 'outline'}
                onClick={() => {
                  setPage(0);
                  setSource(option.value);
                }}
              >
                {option.label}
              </Button>
            ))}
          </div>
          {!isOrderHistory && (
            <div className='flex flex-wrap items-center gap-2'>
              {FIRST_MILE_TYPE_OPTIONS.map((option) => (
                <Button
                  key={option.value ?? 'ALL'}
                  variant={
                    firstMileType === option.value ? 'default' : 'outline'
                  }
                  onClick={() => {
                    setPage(0);
                    setFirstMileType(option.value);
                  }}
                >
                  {option.label}
                </Button>
              ))}
            </div>
          )}
          <Button
            variant='outline'
            onClick={() => {
              void refetch();
            }}
            disabled={isFetching}
          >
            <RefreshCw className='mr-2 h-4 w-4' />
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
            <div className='space-y-4'>
              <div className='overflow-hidden rounded-md border'>
                <div className='overflow-x-auto'>
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>File</TableHead>
                        <TableHead>Source</TableHead>
                        <TableHead>Type</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Records</TableHead>
                        <TableHead>Started</TableHead>
                        <TableHead>Finished</TableHead>
                        <TableHead>Error</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {data.items.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className='font-medium'>
                            {item.file_name || `Import #${item.id}`}
                          </TableCell>
                          <TableCell>
                            {isOrderHistory ? 'Orders' : 'First-mile'}
                          </TableCell>
                          <TableCell>{formatImportType(item.type)}</TableCell>
                          <TableCell>
                            <Badge
                              variant={
                                STATUS_VARIANTS[item.status] ?? 'outline'
                              }
                            >
                              {item.status}
                            </Badge>
                          </TableCell>
                          <TableCell className='text-sm text-muted-foreground'>
                            {item.success_records}/{item.failed_records}/
                            {item.total_records}
                          </TableCell>
                          <TableCell className='text-sm text-muted-foreground'>
                            {formatDateTime(item.started_at)}
                          </TableCell>
                          <TableCell className='text-sm text-muted-foreground'>
                            {formatDateTime(item.finished_at)}
                          </TableCell>
                          <TableCell className='max-w-80 truncate text-sm text-destructive'>
                            {item.error_message || '-'}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>

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
