/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order list summary stats
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { CheckCircle2, Clock3, PackageCheck, TimerReset } from 'lucide-react';
import type {
  FirstMileOrderDetail,
  FirstMilePaginatedData,
} from '../../../../types';

interface OrderStatsCardsProps {
  canViewOrders: boolean;
  data?: FirstMilePaginatedData<FirstMileOrderDetail>;
  isFetching?: boolean;
}

const formatNumber = (value: number): string =>
  new Intl.NumberFormat('vi-VN').format(value);

export const OrderStatsCards: React.FC<OrderStatsCardsProps> = ({
  canViewOrders,
  data,
  isFetching = false,
}) => {
  const orders = data?.items ?? [];
  const confirmedOrders = orders.filter((order) => order.isConfirm).length;
  const pendingConfirmationOrders = orders.filter(
    (order) => !order.isConfirm
  ).length;
  const stats = [
    {
      label: 'Tổng kết quả',
      value: data?.totalItems ?? 0,
      description: 'Các đơn hàng khớp với bộ lọc hiện tại',
      icon: PackageCheck,
    },
    {
      label: 'Trang hiện tại',
      value: orders.length,
      description: 'Các đơn hàng đã tải trên trang này',
      icon: Clock3,
    },
    {
      label: 'Đã xác nhận',
      value: confirmedOrders,
      description: 'Các đơn hàng đã xác nhận trên trang này',
      icon: CheckCircle2,
    },
    {
      label: 'Chờ xác nhận',
      value: pendingConfirmationOrders,
      description: 'Các đơn hàng đang chờ xác nhận trên trang này',
      icon: TimerReset,
    },
  ];

  if (!canViewOrders) {
    return null;
  }

  return (
    <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-4'>
      {stats.map((stat) => {
        const Icon = stat.icon;

        return (
          <Card key={stat.label} className='gap-2 py-3'>
            <CardHeader className='flex flex-row items-center justify-between space-y-0 px-4 py-0'>
              <CardDescription>{stat.label}</CardDescription>
              <Icon className='h-4 w-4 text-muted-foreground' />
            </CardHeader>
            <CardContent className='px-4 py-0'>
              <CardTitle className='text-xl'>
                {formatNumber(stat.value)}
              </CardTitle>
              <p className='mt-1 text-xs text-muted-foreground'>
                {isFetching ? 'Đang tải lại...' : stat.description}
              </p>
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
};
