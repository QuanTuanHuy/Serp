/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS entity orders tab
 */

import {
  ChevronLeft,
  ChevronRight,
  Loader2,
  PackageSearch,
} from 'lucide-react';

import {
  Badge,
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';

import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  FirstMilePaginatedData,
} from '../types';

interface TmsEntityOrdersTabProps {
  data?: FirstMilePaginatedData<FirstMileOrderDetail>;
  isFetching?: boolean;
  page: number;
  emptyText: string;
  onPreviousPage: () => void;
  onNextPage: () => void;
}

const formatOrderStatusLabel = (status?: FirstMileOrderStatus) => {
  if (!status) {
    return '--';
  }

  return status
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(' ');
};

const getOrderStatusBadgeVariant = (
  status?: FirstMileOrderStatus
): 'default' | 'secondary' | 'outline' | 'destructive' => {
  if (!status) {
    return 'outline';
  }
  if (status === 'CANCELLED' || status === 'LOST_OR_DAMAGED') {
    return 'destructive';
  }
  if (
    status === 'AT_ORIGIN_POST_OFFICE' ||
    status === 'INBOUND_AT_ORIGIN_HUB'
  ) {
    return 'default';
  }
  if (
    status === 'BAGGING_IN_PROGRESS' ||
    status === 'BAGGED' ||
    status === 'BAG_SEALED'
  ) {
    return 'secondary';
  }
  return 'outline';
};

const formatRoute = (order: FirstMileOrderDetail) => {
  const origin = order.originPostOfficeCode || '--';
  const destination = order.destinationPostOfficeCode || '--';
  return `${origin} -> ${destination}`;
};

const formatParty = (name?: string, phone?: string) => {
  if (name && phone) {
    return `${name} (${phone})`;
  }
  return name || phone || '--';
};

const formatDateTime = (value?: string) => {
  if (!value) {
    return '--';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date);
};

export function TmsEntityOrdersTab({
  data,
  isFetching,
  page,
  emptyText,
  onPreviousPage,
  onNextPage,
}: TmsEntityOrdersTabProps) {
  const orders = data?.items ?? [];
  const hasOrders = orders.length > 0;

  if (isFetching && !hasOrders) {
    return (
      <div className='flex items-center gap-2 rounded-md border p-4 text-sm text-muted-foreground'>
        <Loader2 className='h-4 w-4 animate-spin' />
        Loading orders...
      </div>
    );
  }

  if (!hasOrders) {
    return (
      <div className='flex items-center gap-2 rounded-md border p-4 text-sm text-muted-foreground'>
        <PackageSearch className='h-4 w-4' />
        {emptyText}
      </div>
    );
  }

  return (
    <div className='space-y-3'>
      <div className='flex items-center justify-between gap-3 text-sm'>
        <p className='font-medium'>Orders ({data?.totalItems ?? 0})</p>
        {isFetching && (
          <span className='flex items-center gap-1 text-muted-foreground'>
            <Loader2 className='h-3.5 w-3.5 animate-spin' />
            Refreshing
          </span>
        )}
      </div>

      <div className='overflow-x-auto rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Order</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Route</TableHead>
              <TableHead>Receiver</TableHead>
              <TableHead>Updated</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.map((order) => (
              <TableRow key={order.id || order.orderCode}>
                <TableCell className='min-w-40'>
                  <p className='font-medium'>{order.orderCode}</p>
                  {order.customerOrderCode && (
                    <p className='text-xs text-muted-foreground'>
                      {order.customerOrderCode}
                    </p>
                  )}
                </TableCell>
                <TableCell className='min-w-44'>
                  <Badge variant={getOrderStatusBadgeVariant(order.status)}>
                    {formatOrderStatusLabel(order.status)}
                  </Badge>
                </TableCell>
                <TableCell className='min-w-44 text-xs text-muted-foreground'>
                  {formatRoute(order)}
                </TableCell>
                <TableCell className='min-w-48 text-xs text-muted-foreground'>
                  {formatParty(order.receiverName, order.receiverPhone)}
                </TableCell>
                <TableCell className='min-w-36 text-xs text-muted-foreground'>
                  {formatDateTime(order.updatedAt || order.createdAt)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <div className='flex items-center justify-between gap-3 text-sm text-muted-foreground'>
        <span>
          Page {page + 1}
          {data?.totalPages ? ` of ${data.totalPages}` : ''}
        </span>
        <div className='flex items-center gap-2'>
          <Button
            type='button'
            variant='outline'
            size='sm'
            disabled={!data?.hasPrevious || isFetching}
            onClick={onPreviousPage}
          >
            <ChevronLeft className='h-4 w-4' />
            Previous
          </Button>
          <Button
            type='button'
            variant='outline'
            size='sm'
            disabled={!data?.hasNext || isFetching}
            onClick={onNextPage}
          >
            Next
            <ChevronRight className='h-4 w-4' />
          </Button>
        </div>
      </div>
    </div>
  );
}
