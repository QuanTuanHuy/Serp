// SalesChart Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import { Card, CardContent, CardHeader } from '@/shared/components/ui';
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
      <div className='bg-white p-3 border border-gray-200 rounded-lg shadow-lg'>
        <p className='font-medium text-gray-900'>{label}</p>
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
  <div className='animate-pulse'>
    <div className='h-6 bg-gray-200 rounded w-48 mb-4'></div>
    <div className='h-64 bg-gray-200 rounded'></div>
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
          <h3 className='text-lg font-semibold'>{period} Sales Performance</h3>
        </CardHeader>
        <CardContent>
          <div className='flex items-center justify-center h-64 text-gray-500'>
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
            <h3 className='text-lg font-semibold'>
              {period} Sales Performance
            </h3>
            {showTrend && trend && (
              <p
                className={cn(
                  'text-sm font-medium',
                  trend.isPositive ? 'text-green-600' : 'text-red-600'
                )}
              >
                {trend.isPositive ? '↗' : '↘'} {trend.value.toFixed(1)}% vs
                last period
              </p>
            )}
          </div>
          <div className='text-right'>
            <p className='text-sm text-gray-500'>Total Revenue</p>
            <p className='text-xl font-bold text-green-600'>
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
          <div className='text-center p-3 bg-gray-50 rounded-lg'>
            <p className='text-sm text-gray-600'>Total Deals</p>
            <p className='text-xl font-bold text-blue-600'>
              {data.reduce((sum, item) => sum + item.deals, 0)}
            </p>
          </div>
          <div className='text-center p-3 bg-gray-50 rounded-lg'>
            <p className='text-sm text-gray-600'>Avg Deal Size</p>
            <p className='text-xl font-bold text-purple-600'>
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
          <div className='text-center p-3 bg-gray-50 rounded-lg'>
            <p className='text-sm text-gray-600'>Best Month</p>
            <p className='text-xl font-bold text-green-600'>
              {data.reduce(
                (max, item) => (item.revenue > max.revenue ? item : max),
                data[0]
              )?.period || 'N/A'}
            </p>
          </div>
          <div className='text-center p-3 bg-gray-50 rounded-lg'>
            <p className='text-sm text-gray-600'>Growth Rate</p>
            <p
              className={cn(
                'text-xl font-bold',
                trend?.isPositive ? 'text-green-600' : 'text-red-600'
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
