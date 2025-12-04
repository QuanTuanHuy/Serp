'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Revenue Trend Chart Component
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '@/shared/components/ui/card';
import { TrendingUp, TrendingDown } from 'lucide-react';
import { cn } from '@/shared';

export interface RevenueData {
  month: string;
  revenue: number;
  target?: number;
  deals: number;
}

export interface RevenueTrendChartProps {
  data: RevenueData[];
  title?: string;
  description?: string;
  className?: string;
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(value);
}

function formatCurrencyFull(value: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value);
}

export function RevenueTrendChart({
  data,
  title = 'Xu hướng doanh thu',
  description = 'Doanh thu theo tháng',
  className,
}: RevenueTrendChartProps) {
  const maxValue = Math.max(
    ...data.map((d) => Math.max(d.revenue, d.target || 0)),
    1
  );
  const totalRevenue = data.reduce((sum, d) => sum + d.revenue, 0);
  const totalTarget = data.reduce((sum, d) => sum + (d.target || 0), 0);
  const totalDeals = data.reduce((sum, d) => sum + d.deals, 0);
  const achievementRate =
    totalTarget > 0 ? ((totalRevenue / totalTarget) * 100).toFixed(1) : 0;

  // Calculate trend
  const lastMonth = data[data.length - 1]?.revenue || 0;
  const prevMonth = data[data.length - 2]?.revenue || 0;
  const trend = prevMonth > 0 ? ((lastMonth - prevMonth) / prevMonth) * 100 : 0;

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader className='flex flex-row items-start justify-between'>
        <div>
          <CardTitle>{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </div>
        <div className='text-right'>
          <div className='text-2xl font-bold text-foreground'>
            {formatCurrency(totalRevenue)}
          </div>
          <div
            className={cn(
              'flex items-center gap-1 text-sm justify-end',
              trend > 0
                ? 'text-green-600 dark:text-green-400'
                : 'text-red-600 dark:text-red-400'
            )}
          >
            {trend > 0 ? (
              <TrendingUp className='h-4 w-4' />
            ) : (
              <TrendingDown className='h-4 w-4' />
            )}
            <span>{Math.abs(trend).toFixed(1)}%</span>
            <span className='text-muted-foreground'>vs tháng trước</span>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {/* Chart */}
        <div className='relative h-48 flex items-end gap-2 pb-6'>
          {data.map((item, index) => {
            const revenueHeight = (item.revenue / maxValue) * 100;
            const targetHeight = item.target
              ? (item.target / maxValue) * 100
              : 0;
            const isAboveTarget = item.target
              ? item.revenue >= item.target
              : true;

            return (
              <div
                key={index}
                className='flex-1 flex flex-col items-center gap-1 group'
              >
                {/* Tooltip on hover */}
                <div className='absolute bottom-full mb-2 left-1/2 -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity z-10 pointer-events-none'>
                  <div className='bg-popover text-popover-foreground text-xs rounded-lg px-3 py-2 shadow-lg border border-border whitespace-nowrap'>
                    <div className='font-medium'>{item.month}</div>
                    <div className='text-green-600 dark:text-green-400'>
                      DT: {formatCurrencyFull(item.revenue)}
                    </div>
                    {item.target && (
                      <div className='text-muted-foreground'>
                        MT: {formatCurrencyFull(item.target)}
                      </div>
                    )}
                    <div className='text-muted-foreground'>
                      {item.deals} deals
                    </div>
                  </div>
                </div>

                {/* Bars container */}
                <div className='relative w-full h-full flex items-end justify-center gap-0.5'>
                  {/* Target bar (background) */}
                  {item.target && (
                    <div
                      className='absolute bottom-0 left-1/2 -translate-x-1/2 w-3/4 bg-muted/50 rounded-t transition-all duration-300 border border-dashed border-muted-foreground/30'
                      style={{ height: `${targetHeight}%` }}
                    />
                  )}
                  {/* Revenue bar */}
                  <div
                    className={cn(
                      'relative w-full rounded-t transition-all duration-300 cursor-pointer',
                      isAboveTarget
                        ? 'bg-gradient-to-t from-green-600 to-green-400'
                        : 'bg-gradient-to-t from-orange-600 to-orange-400'
                    )}
                    style={{
                      height: `${revenueHeight}%`,
                      minHeight: item.revenue > 0 ? '4px' : 0,
                    }}
                  />
                </div>

                {/* Month label */}
                <div className='absolute -bottom-1 left-0 w-full'>
                  <span className='text-xs text-muted-foreground whitespace-nowrap'>
                    {item.month.slice(0, 3)}
                  </span>
                </div>
              </div>
            );
          })}
        </div>

        {/* Legend */}
        <div className='flex items-center justify-center gap-6 mt-4 pt-4 border-t border-border'>
          <div className='flex items-center gap-2'>
            <div className='w-3 h-3 rounded-sm bg-gradient-to-t from-green-600 to-green-400' />
            <span className='text-xs text-muted-foreground'>Doanh thu</span>
          </div>
          <div className='flex items-center gap-2'>
            <div className='w-3 h-3 rounded-sm border border-dashed border-muted-foreground/50 bg-muted/50' />
            <span className='text-xs text-muted-foreground'>Mục tiêu</span>
          </div>
        </div>

        {/* Summary stats */}
        <div className='grid grid-cols-3 gap-4 mt-4 pt-4 border-t border-border text-center'>
          <div>
            <div className='text-lg font-bold text-foreground'>
              {totalDeals}
            </div>
            <div className='text-xs text-muted-foreground'>Deals đóng</div>
          </div>
          <div>
            <div className='text-lg font-bold text-foreground'>
              {formatCurrency(totalRevenue / totalDeals || 0)}
            </div>
            <div className='text-xs text-muted-foreground'>TB/Deal</div>
          </div>
          <div>
            <div
              className={cn(
                'text-lg font-bold',
                Number(achievementRate) >= 100
                  ? 'text-green-600 dark:text-green-400'
                  : 'text-orange-600 dark:text-orange-400'
              )}
            >
              {achievementRate}%
            </div>
            <div className='text-xs text-muted-foreground'>Đạt mục tiêu</div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
