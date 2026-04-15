/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile order list page
 */

'use client';

import React from 'react';
import { useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2, RefreshCw, Search, Shield } from 'lucide-react';
import { useGetOrdersQuery } from '../../api';
import type { FirstMileOrderDetail, FirstMileOrderStatus } from '../../types';

const PAGE_SIZE = 20;

type OrderAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_POST_OFFICE'
  | 'COURIER_ASSIGNED'
  | 'CUSTOMER_CREATED'
  | 'NO_ACCESS';

type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline';
type OrderStatusFilter = 'ALL' | FirstMileOrderStatus;

const ORDER_STATUS_OPTIONS: FirstMileOrderStatus[] = [
  'CREATED',
  'ASSIGNED_TO_PICKUP',
  'PICKING_UP',
  'PICKUP_FAILED',
  'PICKED_UP',
  'AT_ORIGIN_POST_OFFICE',
  'CANCELLED',
  'LOST_OR_DAMAGED',
];

const resolveOrderAccessScope = (roles: string[]): OrderAccessScope => {
  if (roles.includes('TMS_ADMIN')) {
    return 'ADMIN_ALL';
  }

  if (roles.includes('TMS_POSTOFFICER_MANAGER')) {
    return 'MANAGER_POST_OFFICE';
  }

  if (roles.includes('TMS_POSTOFFICER')) {
    return 'COURIER_ASSIGNED';
  }

  if (roles.includes('TMS_CUSTOMER')) {
    return 'CUSTOMER_CREATED';
  }

  return 'NO_ACCESS';
};

const getScopeBadgeLabel = (scope: OrderAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'TMS admin';
    case 'MANAGER_POST_OFFICE':
      return 'Post office manager';
    case 'COURIER_ASSIGNED':
      return 'Courier';
    case 'CUSTOMER_CREATED':
      return 'Customer';
    default:
      return 'No access';
  }
};

const getScopeDescription = (scope: OrderAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'You can view all orders in the system.';
    case 'MANAGER_POST_OFFICE':
      return 'You can view orders assigned to the post office you manage.';
    case 'COURIER_ASSIGNED':
      return 'You can view orders assigned to you.';
    case 'CUSTOMER_CREATED':
      return 'You can only view orders created by you.';
    default:
      return 'Your current account does not have permission to access the order list.';
  }
};

const formatStatusLabel = (status: FirstMileOrderStatus): string =>
  status.replaceAll('_', ' ');

const getStatusBadgeVariant = (status: FirstMileOrderStatus): BadgeVariant => {
  switch (status) {
    case 'CREATED':
    case 'ASSIGNED_TO_PICKUP':
      return 'secondary';
    case 'PICKING_UP':
    case 'PICKED_UP':
    case 'AT_ORIGIN_POST_OFFICE':
      return 'default';
    case 'PICKUP_FAILED':
      return 'outline';
    case 'CANCELLED':
    case 'LOST_OR_DAMAGED':
      return 'destructive';
    default:
      return 'outline';
  }
};

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('en-US');
};

const buildOrderAddressLabel = (
  name?: string,
  phone?: string,
  addressDetail?: string
): string => {
  const mainLabel = [name, phone].filter(Boolean).join(' - ');
  if (!mainLabel && !addressDetail) {
    return '--';
  }

  if (!addressDetail) {
    return mainLabel;
  }

  return `${mainLabel || '--'} | ${addressDetail}`;
};

const buildPostOfficeAssignmentLabel = (
  order: FirstMileOrderDetail
): string => {
  const origin = order.originPostOfficeCode || '--';
  const destination = order.destinationPostOfficeCode || '--';
  return `${origin} -> ${destination}`;
};

export const OrderListPage: React.FC = () => {
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];

  const [page, setPage] = React.useState(0);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [statusInput, setStatusInput] =
    React.useState<OrderStatusFilter>('ALL');
  const [keyword, setKeyword] = React.useState<string | undefined>(undefined);
  const [status, setStatus] = React.useState<FirstMileOrderStatus | undefined>(
    undefined
  );

  const accessScope = React.useMemo(
    () => resolveOrderAccessScope(roles),
    [roles]
  );
  const canViewOrders = accessScope !== 'NO_ACCESS';

  const { data, isLoading, isFetching, refetch } = useGetOrdersQuery(
    {
      page,
      size: PAGE_SIZE,
      keyword,
      status,
    },
    {
      skip: !canViewOrders,
    }
  );

  const handleApplyFilters = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
    setStatus(statusInput === 'ALL' ? undefined : statusInput);
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Orders</h1>
        <p className='text-muted-foreground'>
          Track first-mile orders based on your role and access scope.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Shield className='h-5 w-5' />
            Access Scope
          </CardTitle>
          <CardDescription>
            The system automatically limits visible orders by your current role.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-2'>
          <Badge
            variant={canViewOrders ? 'secondary' : 'destructive'}
            className='w-fit'
          >
            {getScopeBadgeLabel(accessScope)}
          </Badge>
          <p className='text-sm text-muted-foreground'>
            {getScopeDescription(accessScope)}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Search & Filters</CardTitle>
          <CardDescription>
            Search by order code, customer order code, sender, or receiver
            name/phone.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleApplyFilters}
            className='flex flex-col gap-3 md:flex-row md:items-center'
          >
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                className='pl-10'
                value={keywordInput}
                onChange={(event) => setKeywordInput(event.target.value)}
                placeholder='Search orders...'
                disabled={!canViewOrders}
              />
            </div>

            <Select
              value={statusInput}
              onValueChange={(value) =>
                setStatusInput(value as OrderStatusFilter)
              }
              disabled={!canViewOrders}
            >
              <SelectTrigger className='w-full md:w-[220px]'>
                <SelectValue placeholder='Filter by status' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All statuses</SelectItem>
                {ORDER_STATUS_OPTIONS.map((statusOption) => (
                  <SelectItem key={statusOption} value={statusOption}>
                    {formatStatusLabel(statusOption)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Button type='submit' disabled={!canViewOrders}>
              Apply
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={() => refetch()}
              disabled={!canViewOrders || isFetching}
            >
              <RefreshCw className='mr-2 h-4 w-4' />
              Refresh
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
        </CardHeader>
        <CardContent>
          {!canViewOrders ? (
            <p className='text-muted-foreground'>
              You do not have permission to access order data.
            </p>
          ) : isLoading ? (
            <div className='flex items-center gap-2 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Loading orders...
            </div>
          ) : data && data.items.length > 0 ? (
            <div className='space-y-3'>
              {data.items.map((order) => (
                <div
                  key={order.id}
                  className='rounded-lg border p-3 flex flex-col gap-2'
                >
                  <div className='flex flex-wrap items-center justify-between gap-2'>
                    <div>
                      <p className='font-medium'>{order.orderCode}</p>
                      <p className='text-xs text-muted-foreground'>
                        Customer order: {order.customerOrderCode || '--'}
                      </p>
                    </div>
                    <div className='flex items-center gap-2'>
                      <Badge variant={getStatusBadgeVariant(order.status)}>
                        {formatStatusLabel(order.status)}
                      </Badge>
                      <Badge variant='outline'>
                        {order.isConfirm ? 'Confirmed' : 'Pending confirm'}
                      </Badge>
                    </div>
                  </div>

                  <p className='text-xs text-muted-foreground'>
                    Sender:{' '}
                    {buildOrderAddressLabel(
                      order.senderName,
                      order.senderPhone,
                      order.senderAddressDetail
                    )}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Receiver:{' '}
                    {buildOrderAddressLabel(
                      order.receiverName,
                      order.receiverPhone,
                      order.receiverAddressDetail
                    )}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Assigned post office:{' '}
                    {buildPostOfficeAssignmentLabel(order)}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Pickup window: {formatDateTime(order.pickupTimeStart)} -{' '}
                    {formatDateTime(order.pickupTimeEnd)}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Created by: {order.createdBy || '--'} | Updated:{' '}
                    {formatDateTime(order.updatedAt)}
                  </p>
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
            <p className='text-muted-foreground'>No orders found.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
