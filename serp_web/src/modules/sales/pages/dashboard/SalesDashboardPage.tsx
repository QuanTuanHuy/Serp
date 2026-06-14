'use client';

import React, { useMemo } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui';
import {
  CheckCircle2,
  Clock,
  DollarSign,
  Package,
  ShoppingCart,
  Users,
  XCircle,
} from 'lucide-react';
import {
  useGetCustomersQuery,
  useGetOrdersQuery,
  useGetProductsQuery,
} from '../../api/salesApi';

const STATUS_CONFIG = {
  CREATED: {
    label: 'Chờ duyệt',
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

const APPROVED_STATUSES = ['APPROVED', 'FULLY_DELIVERED'];

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
});

export const SalesDashboardPage: React.FC = () => {
  const now = new Date();
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  const startOfNextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);

  const orderDateAfter = startOfMonth.toISOString().split('T')[0];
  const orderDateBefore = startOfNextMonth.toISOString().split('T')[0];

  const prevStart = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  const prevEnd = startOfMonth;
  const prevAfter = prevStart.toISOString().split('T')[0];
  const prevBefore = prevEnd.toISOString().split('T')[0];

  const { data: customersData } = useGetCustomersQuery({
    filters: {},
    pagination: { page: 0, size: 1000 },
  });

  const { data: productsData } = useGetProductsQuery({
    filters: {},
    pagination: { page: 0, size: 1000 },
  });

  const { data: ordersData } = useGetOrdersQuery({
    filters: { orderDateAfter, orderDateBefore },
    pagination: { page: 0, size: 1000 },
  });

  const { data: previousOrdersData } = useGetOrdersQuery({
    filters: { orderDateAfter: prevAfter, orderDateBefore: prevBefore },
    pagination: { page: 0, size: 1000 },
  });

  const customers = customersData?.data?.items || [];
  const products = productsData?.data?.items || [];
  const orders = ordersData?.data?.items || [];
  const previousOrders = previousOrdersData?.data?.items || [];

  const activeCustomers = customers.filter(
    (customer) => customer.statusId === 'ACTIVE'
  ).length;

  const activeCustomersPrevMonth = customers.filter((customer) => {
    const createdAt = new Date(customer.createdStamp);
    return (
      customer.statusId === 'ACTIVE' &&
      createdAt >= prevStart &&
      createdAt < prevEnd
    );
  }).length;

  const activeProducts = products.filter(
    (product) => product.statusId === 'ACTIVE'
  ).length;

  const activeProductsPrevMonth = products.filter((product) => {
    const createdAt = new Date(product.createdStamp);
    return (
      product.statusId === 'ACTIVE' &&
      createdAt >= prevStart &&
      createdAt < prevEnd
    );
  }).length;

  const approvedOrdersThisMonth = orders.filter((order) =>
    APPROVED_STATUSES.includes(order.statusId)
  ).length;

  const approvedOrdersPrevMonth = previousOrders.filter((order) =>
    APPROVED_STATUSES.includes(order.statusId)
  ).length;

  const totalRevenue = useMemo(() => {
    return orders
      .filter((order) => APPROVED_STATUSES.includes(order.statusId))
      .reduce((sum, order) => sum + (order.totalAmount || 0), 0);
  }, [orders]);

  const previousRevenue = useMemo(() => {
    return previousOrders
      .filter((order) => APPROVED_STATUSES.includes(order.statusId))
      .reduce((sum, order) => sum + (order.totalAmount || 0), 0);
  }, [previousOrders]);

  const customersPercent =
    activeCustomersPrevMonth === 0
      ? 100
      : Math.round(
          ((activeCustomers - activeCustomersPrevMonth) /
            activeCustomersPrevMonth) *
            100
        );

  const productsPercent =
    activeProductsPrevMonth === 0
      ? 100
      : Math.round(
          ((activeProducts - activeProductsPrevMonth) /
            activeProductsPrevMonth) *
            100
        );

  const ordersPercent =
    approvedOrdersPrevMonth === 0
      ? 100
      : Math.round(
          ((approvedOrdersThisMonth - approvedOrdersPrevMonth) /
            approvedOrdersPrevMonth) *
            100
        );

  const revenuePercent =
    previousRevenue === 0
      ? 100
      : Math.round(((totalRevenue - previousRevenue) / previousRevenue) * 100);

  const recentOrders = orders
    .filter((order) => APPROVED_STATUSES.includes(order.statusId))
    .sort((a, b) => +new Date(b.orderDate) - +new Date(a.orderDate))
    .slice(0, 5);

  const pendingOrders = orders
    .filter((order) => order.statusId === 'CREATED')
    .sort((a, b) => +new Date(b.orderDate) - +new Date(a.orderDate))
    .slice(0, 5);

  const stats = [
    {
      title: 'Khách hàng hoạt động',
      value: activeCustomers,
      change: customersPercent,
      icon: Users,
    },
    {
      title: 'Đơn bán hàng',
      value: approvedOrdersThisMonth,
      change: ordersPercent,
      icon: ShoppingCart,
    },
    {
      title: 'Sản phẩm kinh doanh',
      value: activeProducts,
      change: productsPercent,
      icon: Package,
    },
    {
      title: 'Tổng doanh thu',
      value: currencyFormatter.format(totalRevenue),
      change: revenuePercent,
      icon: DollarSign,
    },
  ];

  const formatChange = (value: number) => `${value >= 0 ? '+' : ''}${value}%`;

  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <h1 className='text-3xl font-bold'>Dashboard bán hàng</h1>
      </div>

      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        {stats.map((stat) => (
          <Card key={stat.title}>
            <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
              <CardTitle className='text-sm font-medium'>
                {stat.title}
              </CardTitle>
              <stat.icon className='h-4 w-4 text-muted-foreground' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold'>{stat.value}</div>
              <p className='text-xs'>
                <span className='text-emerald-600'>
                  {formatChange(stat.change)}
                </span>{' '}
                <span className='text-muted-foreground'>
                  so với tháng trước
                </span>
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className='grid gap-4 md:grid-cols-2'>
        <Card>
          <CardHeader>
            <CardTitle>Đơn hàng gần đây</CardTitle>
          </CardHeader>
          <CardContent>
            {recentOrders.length === 0 ? (
              <p className='py-4 text-center text-sm text-muted-foreground'>
                Không có đơn hàng gần đây
              </p>
            ) : (
              <ul className='space-y-2'>
                {recentOrders.map((order) => {
                  const status =
                    STATUS_CONFIG[
                      order.statusId as keyof typeof STATUS_CONFIG
                    ] || STATUS_CONFIG.APPROVED;

                  return (
                    <li key={order.id} className='text-sm'>
                      <div className='flex items-center justify-between gap-4'>
                        <div>
                          <div className='font-medium'>
                            {order.orderName || order.id}
                          </div>
                          <div className='text-xs text-muted-foreground'>
                            {new Date(order.orderDate).toLocaleDateString(
                              'vi-VN'
                            )}{' '}
                            — {currencyFormatter.format(order.totalAmount || 0)}
                          </div>
                        </div>
                        <Badge
                          variant='secondary'
                          className={`${status.bg} ${status.text} gap-2`}
                        >
                          <span
                            className={`h-2.5 w-2.5 rounded-full ${status.dot}`}
                          />
                          {status.label}
                        </Badge>
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
            <CardTitle>Đơn đang chờ duyệt</CardTitle>
          </CardHeader>
          <CardContent>
            {pendingOrders.length === 0 ? (
              <p className='py-4 text-center text-sm text-muted-foreground'>
                Không có đơn đang chờ duyệt
              </p>
            ) : (
              <ul className='space-y-2'>
                {pendingOrders.map((order) => {
                  const status =
                    STATUS_CONFIG[
                      order.statusId as keyof typeof STATUS_CONFIG
                    ] || STATUS_CONFIG.CREATED;

                  return (
                    <li key={order.id} className='text-sm'>
                      <div className='flex items-center justify-between gap-4'>
                        <div>
                          <div className='font-medium'>
                            {order.orderName || order.id}
                          </div>
                          <div className='text-xs text-muted-foreground'>
                            {new Date(order.orderDate).toLocaleDateString(
                              'vi-VN'
                            )}{' '}
                            — {currencyFormatter.format(order.totalAmount || 0)}
                          </div>
                        </div>
                        <Badge
                          variant='secondary'
                          className={`${status.bg} ${status.text} gap-2`}
                        >
                          <span
                            className={`h-2.5 w-2.5 rounded-full ${status.dot}`}
                          />
                          {status.label}
                        </Badge>
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
};
