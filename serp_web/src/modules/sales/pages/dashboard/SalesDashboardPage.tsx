/**
 * Sales Dashboard Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Sales overview dashboard
 */

'use client';

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  DollarSign,
  Package,
  ShoppingCart,
  TrendingUp,
  Users,
} from 'lucide-react';
import {
  useGetOrdersQuery,
  useGetProductsQuery,
  useGetCustomersQuery,
} from '../../api/salesApi';

export const SalesDashboardPage: React.FC = () => {
  const { data: ordersData } = useGetOrdersQuery({
    filters: {},
    pagination: { page: 0, size: 10 },
  });

  const { data: productsData } = useGetProductsQuery({
    filters: {},
    pagination: { page: 0, size: 10 },
  });

  const { data: customersData } = useGetCustomersQuery({
    filters: {},
    pagination: { page: 0, size: 10 },
  });

  const stats = [
    {
      title: 'Total Revenue',
      value: '$45,231.89',
      change: '+20.1%',
      icon: DollarSign,
      trend: 'up',
    },
    {
      title: 'Orders',
      value: ordersData?.data?.totalItems || 0,
      change: '+12.5%',
      icon: ShoppingCart,
      trend: 'up',
    },
    {
      title: 'Products',
      value: productsData?.data?.totalItems || 0,
      change: '+5.2%',
      icon: Package,
      trend: 'up',
    },
    {
      title: 'Customers',
      value: customersData?.data?.totalItems || 0,
      change: '+8.3%',
      icon: Users,
      trend: 'up',
    },
  ];

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-3xl font-bold tracking-tight'>Sales Dashboard</h1>
        <p className='text-muted-foreground'>
          Overview of your sales performance and key metrics
        </p>
      </div>

      {/* Stats Grid */}
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
              <p className='text-xs text-muted-foreground'>
                <span
                  className={
                    stat.trend === 'up' ? 'text-green-600' : 'text-red-600'
                  }
                >
                  {stat.change}
                </span>{' '}
                from last month
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Recent Activity */}
      <div className='grid gap-4 md:grid-cols-2'>
        <Card>
          <CardHeader>
            <CardTitle>Recent Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <div className='space-y-3'>
              {ordersData?.data?.items?.slice(0, 5).map((order) => (
                <div
                  key={order.id}
                  className='flex items-center justify-between'
                >
                  <div>
                    <p className='font-medium'>{order.orderName || 'Order'}</p>
                    <p className='text-sm text-muted-foreground'>
                      {order.orderDate
                        ? new Date(order.orderDate).toLocaleDateString()
                        : '-'}
                    </p>
                  </div>
                  <div className='text-right'>
                    <p className='font-medium'>
                      ${order.totalAmount?.toLocaleString() || '0'}
                    </p>
                    <p className='text-sm text-muted-foreground'>
                      {order.statusId}
                    </p>
                  </div>
                </div>
              ))}
              {(!ordersData?.data?.items ||
                ordersData.data.items.length === 0) && (
                <p className='text-center text-sm text-muted-foreground py-4'>
                  No recent orders
                </p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Top Products</CardTitle>
          </CardHeader>
          <CardContent>
            <div className='space-y-3'>
              {productsData?.data?.items?.slice(0, 5).map((product) => (
                <div
                  key={product.id}
                  className='flex items-center justify-between'
                >
                  <div>
                    <p className='font-medium'>{product.name}</p>
                    <p className='text-sm text-muted-foreground'>
                      {product.skuCode}
                    </p>
                  </div>
                  <div className='text-right'>
                    <p className='font-medium'>
                      ${product.retailPrice?.toLocaleString() || '0'}
                    </p>
                    <p className='text-sm text-muted-foreground'>
                      Stock: {product.quantityAvailable || 0}
                    </p>
                  </div>
                </div>
              ))}
              {(!productsData?.data?.items ||
                productsData.data.items.length === 0) && (
                <p className='text-center text-sm text-muted-foreground py-4'>
                  No products available
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
