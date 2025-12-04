// SalesChart Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Skeleton,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
} from 'recharts';
import type { SalesMetrics } from '../../types';

interface SalesChartProps {
  data: SalesMetrics[];
  isLoading?: boolean;
  className?: string;
  chartType?: 'line' | 'area';
  showTrend?: boolean;
  period?: string;
}

// Custom tooltip for sales chart
const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className='bg-popover text-popover-foreground p-3 border border-border rounded-lg shadow-lg'>
        <p className='font-medium'>{label}</p>
        <div className='space-y-1'>
          {payload.map((entry: any, index: number) => (
            <p key={index} className='text-sm' style={{ color: entry.color }}>
              {entry.name}: ${entry.value?.toLocaleString()}
            </p>
          ))}
        </div>
      </div>
    );
  }
  return null;
};

// Loading skeleton
const LoadingSkeleton = () => (
  <div className='space-y-4'>
    <Skeleton className='h-6 w-48' />
    <Skeleton className='h-64 w-full' />
  </div>
);

export const SalesChart: React.FC<SalesChartProps> = ({
  data,
  isLoading = false,
  className,
  chartType = 'area',
  showTrend = true,
  period = 'Monthly',
}) => {
  if (isLoading) {
    return (
      <Card className={cn('p-6', className)}>
        <LoadingSkeleton />
      </Card>
    );
  }

  if (!data || data.length === 0) {
    return (
      <Card className={cn('p-6', className)}>
        <CardHeader>
          <CardTitle>{period} Sales Performance</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='flex items-center justify-center h-64 text-muted-foreground'>
            <div className='text-center'>
              <p>No sales data available</p>
              <p className='text-sm mt-1'>
                Data will appear here once sales are recorded
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Prepare data for chart
  const chartData = data.map((item) => ({
    period: item.period,
    revenue: item.revenue,
    deals: item.deals,
    target: 0, // Not included in type, using default
  }));

  // Calculate trend
  const calculateTrend = () => {
    if (data.length < 2) return null;
    const current = data[data.length - 1].revenue;
    const previous = data[data.length - 2].revenue;
    const change = ((current - previous) / previous) * 100;
    return {
      value: Math.abs(change),
      isPositive: change >= 0,
    };
  };

  const trend = calculateTrend();

  return (
    <Card className={cn('', className)}>
      <CardHeader>
        <div className='flex items-center justify-between'>
          <div>
            <CardTitle>{period} Sales Performance</CardTitle>
            {showTrend && trend && (
              <p
                className={cn(
                  'text-sm font-medium',
                  trend.isPositive
                    ? 'text-emerald-600 dark:text-emerald-400'
                    : 'text-rose-600 dark:text-rose-400'
                )}
              >
                {trend.isPositive ? '↗' : '↘'} {trend.value.toFixed(1)}% vs
                last period
              </p>
            )}
          </div>
          <div className='text-right'>
            <p className='text-sm text-muted-foreground'>Total Revenue</p>
            <p className='text-xl font-bold text-emerald-600 dark:text-emerald-400'>
              $
              {data
                .reduce((sum, item) => sum + item.revenue, 0)
                .toLocaleString()}
            </p>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className='h-80'>
          <ResponsiveContainer width='100%' height='100%'>
            {chartType === 'line' ? (
              <LineChart
                data={chartData}
                margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray='3 3' />
                <XAxis dataKey='period' tick={{ fontSize: 12 }} />
                <YAxis
                  tickFormatter={(value) => `$${value / 1000}K`}
                  tick={{ fontSize: 12 }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Line
                  type='monotone'
                  dataKey='revenue'
                  stroke='#10B981'
                  strokeWidth={3}
                  dot={{ fill: '#10B981', strokeWidth: 2, r: 4 }}
                  name='Revenue'
                />
                {chartData.some((item) => item.target > 0) && (
                  <Line
                    type='monotone'
                    dataKey='target'
                    stroke='#EF4444'
                    strokeWidth={2}
                    strokeDasharray='5 5'
                    dot={{ fill: '#EF4444', strokeWidth: 2, r: 3 }}
                    name='Target'
                  />
                )}
              </LineChart>
            ) : (
              <AreaChart
                data={chartData}
                margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray='3 3' />
                <XAxis dataKey='period' tick={{ fontSize: 12 }} />
                <YAxis
                  tickFormatter={(value) => `$${value / 1000}K`}
                  tick={{ fontSize: 12 }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Area
                  type='monotone'
                  dataKey='revenue'
                  stroke='#10B981'
                  fill='#10B981'
                  fillOpacity={0.6}
                  name='Revenue'
                />
                {chartData.some((item) => item.target > 0) && (
                  <Area
                    type='monotone'
                    dataKey='target'
                    stroke='#EF4444'
                    fill='#EF4444'
                    fillOpacity={0.3}
                    name='Target'
                  />
                )}
              </AreaChart>
            )}
          </ResponsiveContainer>
        </div>

        {/* Sales Summary */}
        <div className='mt-6 grid grid-cols-2 md:grid-cols-4 gap-4'>
          <div className='text-center p-3 bg-muted/50 rounded-lg'>
            <p className='text-sm text-muted-foreground'>Total Deals</p>
            <p className='text-xl font-bold text-primary'>
              {data.reduce((sum, item) => sum + item.deals, 0)}
            </p>
          </div>
          <div className='text-center p-3 bg-muted/50 rounded-lg'>
            <p className='text-sm text-muted-foreground'>Avg Deal Size</p>
            <p className='text-xl font-bold text-violet-600 dark:text-violet-400'>
              $
              {(
                data.reduce((sum, item) => sum + item.revenue, 0) /
                Math.max(
                  data.reduce((sum, item) => sum + item.deals, 0),
                  1
                )
              ).toLocaleString()}
            </p>
          </div>
          <div className='text-center p-3 bg-muted/50 rounded-lg'>
            <p className='text-sm text-muted-foreground'>Best Month</p>
            <p className='text-xl font-bold text-emerald-600 dark:text-emerald-400'>
              {data.reduce(
                (max, item) => (item.revenue > max.revenue ? item : max),
                data[0]
              )?.period || 'N/A'}
            </p>
          </div>
          <div className='text-center p-3 bg-muted/50 rounded-lg'>
            <p className='text-sm text-muted-foreground'>Growth Rate</p>
            <p
              className={cn(
                'text-xl font-bold',
                trend?.isPositive
                  ? 'text-emerald-600 dark:text-emerald-400'
                  : 'text-rose-600 dark:text-rose-400'
              )}
            >
              {trend
                ? `${trend.isPositive ? '+' : '-'}${trend.value.toFixed(1)}%`
                : 'N/A'}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default SalesChart;
