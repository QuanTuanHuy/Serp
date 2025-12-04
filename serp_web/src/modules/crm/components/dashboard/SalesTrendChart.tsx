/**
 * SalesTrendChart Component - Sales trend visualization
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM Dashboard sales trend chart
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import { TrendingUp, TrendingDown, ArrowRight } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Skeleton,
} from '@/shared/components/ui';

export interface SalesDataPoint {
  label: string;
  value: number;
  previousValue?: number;
}

export interface SalesTrendChartProps {
  data: SalesDataPoint[];
  title?: string;
  subtitle?: string;
  isLoading?: boolean;
  className?: string;
  showComparison?: boolean;
  valuePrefix?: string;
}

const formatValue = (value: number, prefix = '$'): string => {
  if (value >= 1000000) {
    return `${prefix}${(value / 1000000).toFixed(1)}M`;
  } else if (value >= 1000) {
    return `${prefix}${(value / 1000).toFixed(0)}K`;
  }
  return `${prefix}${value.toLocaleString()}`;
};

export const SalesTrendChart: React.FC<SalesTrendChartProps> = ({
  data,
  title = 'Sales Trend',
  subtitle,
  isLoading = false,
  className,
  showComparison = true,
  valuePrefix = '$',
}) => {
  const maxValue = Math.max(...data.map((d) => d.value));
  const totalValue = data.reduce((sum, d) => sum + d.value, 0);
  const avgValue = data.length > 0 ? totalValue / data.length : 0;

  // Calculate overall trend
  const firstHalf = data.slice(0, Math.floor(data.length / 2));
  const secondHalf = data.slice(Math.floor(data.length / 2));
  const firstHalfAvg =
    firstHalf.length > 0
      ? firstHalf.reduce((sum, d) => sum + d.value, 0) / firstHalf.length
      : 0;
  const secondHalfAvg =
    secondHalf.length > 0
      ? secondHalf.reduce((sum, d) => sum + d.value, 0) / secondHalf.length
      : 0;
  const overallTrend =
    firstHalfAvg > 0
      ? ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100
      : 0;
  const isPositiveTrend = overallTrend >= 0;

  if (isLoading) {
    return (
      <Card className={cn('animate-pulse', className)}>
        <CardHeader>
          <Skeleton className='h-5 w-32 mb-2' />
          <Skeleton className='h-4 w-48' />
        </CardHeader>
        <CardContent>
          <div className='flex items-end gap-2 h-48'>
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <Skeleton
                key={i}
                className='flex-1 rounded-t'
                style={{ height: `${20 + Math.random() * 60}%` }}
              />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={className}>
      <CardHeader className='flex-row items-start justify-between space-y-0 pb-2'>
        <div>
          <CardTitle className='text-lg font-semibold'>{title}</CardTitle>
          {subtitle && (
            <p className='text-sm text-muted-foreground mt-1'>{subtitle}</p>
          )}
        </div>
        <div
          className={cn(
            'flex items-center gap-1 text-sm font-medium px-2.5 py-1 rounded-full',
            isPositiveTrend
              ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400'
              : 'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400'
          )}
        >
          {isPositiveTrend ? (
            <TrendingUp className='h-3.5 w-3.5' />
          ) : (
            <TrendingDown className='h-3.5 w-3.5' />
          )}
          {Math.abs(overallTrend).toFixed(1)}%
        </div>
      </CardHeader>

      <CardContent>
        {/* Chart */}
        <div className='relative'>
          {/* Y-axis labels */}
          <div className='absolute left-0 top-0 bottom-6 w-12 flex flex-col justify-between text-xs text-muted-foreground'>
            <span>{formatValue(maxValue, valuePrefix)}</span>
            <span>{formatValue(maxValue / 2, valuePrefix)}</span>
            <span>{formatValue(0, valuePrefix)}</span>
          </div>

          {/* Chart area */}
          <div className='ml-14'>
            {/* Grid lines */}
            <div className='absolute left-14 right-0 top-0 bottom-6 flex flex-col justify-between pointer-events-none'>
              {[0, 1, 2].map((i) => (
                <div
                  key={i}
                  className='border-t border-dashed border-border/50 w-full'
                />
              ))}
            </div>

            {/* Bars */}
            <div className='flex items-end gap-2 h-48'>
              {data.map((point, index) => {
                const heightPercentage =
                  maxValue > 0 ? (point.value / maxValue) * 100 : 0;
                const prevHeightPercentage =
                  point.previousValue && maxValue > 0
                    ? (point.previousValue / maxValue) * 100
                    : 0;
                const change = point.previousValue
                  ? ((point.value - point.previousValue) /
                      point.previousValue) *
                    100
                  : 0;
                const isUp = change >= 0;

                return (
                  <div
                    key={index}
                    className='flex-1 flex flex-col items-center gap-1 group'
                  >
                    {/* Tooltip */}
                    <div className='opacity-0 group-hover:opacity-100 transition-opacity absolute -top-2 left-1/2 -translate-x-1/2 bg-popover text-popover-foreground text-xs rounded-lg shadow-lg border px-3 py-2 pointer-events-none z-10 whitespace-nowrap'>
                      <div className='font-semibold'>
                        {formatValue(point.value, valuePrefix)}
                      </div>
                      {point.previousValue && (
                        <div
                          className={cn(
                            'text-xs',
                            isUp ? 'text-emerald-500' : 'text-rose-500'
                          )}
                        >
                          {isUp ? '+' : ''}
                          {change.toFixed(1)}% vs previous
                        </div>
                      )}
                    </div>

                    {/* Bar container */}
                    <div className='relative w-full flex justify-center items-end h-full'>
                      {/* Previous period bar (comparison) */}
                      {showComparison && point.previousValue && (
                        <div
                          className='absolute w-2/3 bg-muted/50 rounded-t transition-all duration-300'
                          style={{ height: `${prevHeightPercentage}%` }}
                        />
                      )}

                      {/* Current bar */}
                      <div
                        className={cn(
                          'relative w-full max-w-10 rounded-t transition-all duration-300 cursor-pointer',
                          'bg-gradient-to-t from-blue-600 to-blue-400',
                          'hover:from-blue-700 hover:to-blue-500',
                          'dark:from-blue-500 dark:to-blue-300',
                          'shadow-sm hover:shadow-md'
                        )}
                        style={{
                          height: `${Math.max(heightPercentage, 2)}%`,
                          minHeight: '4px',
                        }}
                      >
                        {/* Shine effect */}
                        <div className='absolute inset-0 bg-gradient-to-r from-white/20 to-transparent rounded-t' />
                      </div>
                    </div>

                    {/* X-axis label */}
                    <span className='text-xs text-muted-foreground mt-2 truncate max-w-full'>
                      {point.label}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Summary */}
        <div className='mt-6 pt-4 border-t grid grid-cols-3 gap-4'>
          <div>
            <p className='text-xs text-muted-foreground'>Total</p>
            <p className='text-lg font-semibold'>
              {formatValue(totalValue, valuePrefix)}
            </p>
          </div>
          <div>
            <p className='text-xs text-muted-foreground'>Average</p>
            <p className='text-lg font-semibold'>
              {formatValue(avgValue, valuePrefix)}
            </p>
          </div>
          <div>
            <p className='text-xs text-muted-foreground'>Best Period</p>
            <p className='text-lg font-semibold'>
              {data.length > 0
                ? data.reduce(
                    (max, d) => (d.value > max.value ? d : max),
                    data[0]
                  ).label
                : '-'}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

// Default sales data for demo
export const DEFAULT_SALES_DATA: SalesDataPoint[] = [
  { label: 'Jan', value: 85000, previousValue: 78000 },
  { label: 'Feb', value: 92000, previousValue: 85000 },
  { label: 'Mar', value: 78000, previousValue: 92000 },
  { label: 'Apr', value: 105000, previousValue: 78000 },
  { label: 'May', value: 125000, previousValue: 105000 },
  { label: 'Jun', value: 110000, previousValue: 125000 },
];
