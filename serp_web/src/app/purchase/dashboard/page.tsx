'use client';

import React, { useMemo } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';
import { Badge } from '@/shared/components/ui';
import {
  Package,
  ShoppingCart,
  Truck,
  DollarSign,
  Clock,
  CheckCircle2,
  XCircle,
} from 'lucide-react';
import {
  useGetSuppliersQuery,
  useGetOrdersQuery,
} from '@/modules/purchase/api/purchaseApi';

const statusStyles = {
  CREATED: {
    label: 'Chờ phê duyệt',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
    icon: Clock,
  },
  APPROVED: {
    label: 'Đã duyệt',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
  },
  CANCELLED: {
    label: 'Đã hủy',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
  },
  FULLY_DELIVERED: {
    label: 'Đã giao hàng',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: Package,
  },
};

export default function PurchaseDashboardPage() {
  // Date range: start of current month -> start of next month
  const now = new Date();
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  const startOfNextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);

  const orderDateAfter = startOfMonth.toISOString().split('T')[0];
  const orderDateBefore = startOfNextMonth.toISOString().split('T')[0];

  // previous month range
  const prevStart = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  const prevEnd = startOfMonth;
  const prevAfter = prevStart.toISOString().split('T')[0];
  const prevBefore = prevEnd.toISOString().split('T')[0];

  // fetch suppliers (get a large page and filter client-side by createdStamp)
  const { data: suppliersAll } = useGetSuppliersQuery({
    filters: {},
    pagination: { page: 0, size: 1000 },
  });

  const suppliersItems = suppliersAll?.data?.items || [];

  const suppliersThisMonth = suppliersItems.filter((s) => {
    const d = s.createdStamp ? new Date(s.createdStamp) : null;
    return d && d >= startOfMonth && d < startOfNextMonth;
  }).length;

  const suppliersPrevMonth = suppliersItems.filter((s) => {
    const d = s.createdStamp ? new Date(s.createdStamp) : null;
    return d && d >= prevStart && d < prevEnd;
  }).length;

  const suppliersPercent =
    suppliersPrevMonth === 0
      ? 100
      : Math.round(
          ((suppliersThisMonth - suppliersPrevMonth) / suppliersPrevMonth) * 100
        );

  const activeSuppliers = suppliersItems.filter(
    (s) => s.statusId === 'ACTIVE'
  ).length;

  // fetch orders for this and previous month
  const { data: ordersThis } = useGetOrdersQuery({
    filters: { orderDateAfter, orderDateBefore },
    pagination: { page: 0, size: 1000 },
  });

  const { data: ordersPrev } = useGetOrdersQuery({
    filters: { orderDateAfter: prevAfter, orderDateBefore: prevBefore },
    pagination: { page: 0, size: 1000 },
  });

  const ordersThisItems = ordersThis?.data?.items || [];
  const ordersPrevItems = ordersPrev?.data?.items || [];

  const approvedStatuses = ['APPROVED', 'FULLY_DELIVERED'];

  const approvedThisCount = ordersThisItems.filter((o) =>
    approvedStatuses.includes(o.statusId)
  ).length;
  const approvedPrevCount = ordersPrevItems.filter((o) =>
    approvedStatuses.includes(o.statusId)
  ).length;

  const ordersPercent =
    approvedPrevCount === 0
      ? 100
      : Math.round(
          ((approvedThisCount - approvedPrevCount) / approvedPrevCount) * 100
        );

  const totalSpent = useMemo(() => {
    return ordersThisItems
      .filter((o) => approvedStatuses.includes(o.statusId))
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);
  }, [ordersThisItems]);

  const recentOrders = ordersThisItems
    .filter((o) => approvedStatuses.includes(o.statusId))
    .sort((a, b) => +new Date(b.orderDate) - +new Date(a.orderDate))
    .slice(0, 5);

  const pendingOrders = ordersThisItems
    .filter((o) => o.statusId === 'CREATED')
    .sort((a, b) => +new Date(b.orderDate) - +new Date(a.orderDate));

  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <h1 className='text-3xl font-bold'>Dashboard mua hàng</h1>
      </div>

      {/* Quick Stats */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>Nhà cung cấp</CardTitle>
            <Package className='h-4 w-4 text-muted-foreground' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>{activeSuppliers}</div>
            <p className='text-xs'>
              <span
                className={
                  suppliersPercent >= 0 ? 'text-emerald-600' : 'text-rose-600'
                }
              >
                {suppliersPercent >= 0 ? '+' : ''}
                {suppliersPercent}%
              </span>{' '}
              <span className='text-muted-foreground'>so với tháng trước</span>
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>Đơn đặt hàng</CardTitle>
            <ShoppingCart className='h-4 w-4 text-muted-foreground' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>{approvedThisCount}</div>
            <p className='text-xs'>
              <span
                className={
                  ordersPercent >= 0 ? 'text-emerald-600' : 'text-rose-600'
                }
              >
                {ordersPercent >= 0 ? '+' : ''}
                {ordersPercent}%
              </span>{' '}
              <span className='text-muted-foreground'>so với tháng trước</span>
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>
              Số lô hàng được nhập
            </CardTitle>
            <Truck className='h-4 w-4 text-muted-foreground' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>
              {ordersThisItems.reduce(
                (acc: number, o: any) => acc + (o.shipments?.length || 0),
                0
              ) || 0}
            </div>
            <p className='text-xs text-muted-foreground'>Trong tháng này</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>Tổng chi tiêu</CardTitle>
            <DollarSign className='h-4 w-4 text-muted-foreground' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>
              {new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND',
              }).format(totalSpent)}
            </div>
            <p className='text-xs text-muted-foreground'>Trong tháng này</p>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity */}
      <div className='grid gap-4 md:grid-cols-2'>
        <Card>
          <CardHeader>
            <CardTitle>Đơn đặt hàng gần đây</CardTitle>
          </CardHeader>
          <CardContent>
            {recentOrders.length === 0 ? (
              <p className='text-sm text-muted-foreground'>
                Không có đơn đặt hàng trong tháng này
              </p>
            ) : (
              <ul className='space-y-2'>
                {recentOrders.map((o) => {
                  const s =
                    statusStyles[o.statusId as keyof typeof statusStyles] ||
                    statusStyles.APPROVED;
                  return (
                    <li key={o.id} className='text-sm'>
                      <div className='flex items-center justify-between'>
                        <div>
                          <div className='font-medium'>
                            {o.orderName || o.id}
                          </div>
                          <div className='text-xs text-muted-foreground'>
                            {new Date(o.orderDate).toLocaleDateString('vi-VN')}{' '}
                            —{' '}
                            {new Intl.NumberFormat('vi-VN', {
                              style: 'currency',
                              currency: 'VND',
                            }).format(o.totalAmount || 0)}
                          </div>
                        </div>
                        <div className='flex items-center gap-2'>
                          <Badge
                            variant='secondary'
                            className={`${s.bg} ${s.text} gap-2`}
                          >
                            <span
                              className={`h-2.5 w-2.5 rounded-full ${s.dot}`}
                            />
                            {s.label}
                          </Badge>
                        </div>
                      </div>
                    </li>
                  );
                })}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Đơn đang chờ phê duyệt</CardTitle>
          </CardHeader>
          <CardContent>
            {pendingOrders.length === 0 ? (
              <p className='text-sm text-muted-foreground'>
                Không có đơn đang chờ phê duyệt
              </p>
            ) : (
              <ul className='space-y-2'>
                {pendingOrders.map((o) => {
                  const s =
                    statusStyles[o.statusId as keyof typeof statusStyles] ||
                    statusStyles.CREATED;
                  return (
                    <li key={o.id} className='text-sm'>
                      <div className='flex items-center justify-between'>
                        <div>
                          <div className='font-medium'>
                            {o.orderName || o.id}
                          </div>
                          <div className='text-xs text-muted-foreground'>
                            {new Date(o.orderDate).toLocaleDateString('vi-VN')}{' '}
                            —{' '}
                            {new Intl.NumberFormat('vi-VN', {
                              style: 'currency',
                              currency: 'VND',
                            }).format(o.totalAmount || 0)}
                          </div>
                        </div>
                        <div className='flex items-center gap-2'>
                          <Badge
                            variant='secondary'
                            className={`${s.bg} ${s.text} gap-2`}
                          >
                            <span
                              className={`h-2.5 w-2.5 rounded-full ${s.dot}`}
                            />
                            {s.label}
                          </Badge>
                        </div>
                      </div>
                    </li>
                  );
                })}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
