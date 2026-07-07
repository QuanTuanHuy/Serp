import React from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';
import { ChartItemDto } from '../../types';

interface DashboardLineChartProps {
  data: ChartItemDto[];
  color?: string;
  title?: string;
}

export function DashboardLineChart({
  data,
  color = '#991B1B', // Crimson
  title,
}: DashboardLineChartProps) {
  const chartData = data || [];

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const dataPoint = payload[0].payload;
      return (
        <div className='rounded-lg border border-border bg-popover p-2.5 text-popover-foreground shadow-lg'>
          <p className='mb-1 text-xs font-semibold text-muted-foreground'>
            Ngày: {dataPoint.label || dataPoint.name}
          </p>
          <div className='flex items-center gap-2'>
            <span
              className='w-3 h-3 rounded-full'
              style={{ backgroundColor: color }}
            />
            <span className='text-sm font-semibold text-foreground'>
              Số chuyến:{' '}
              <span className='text-red-700 dark:text-red-400'>
                {dataPoint.count}
              </span>
            </span>
          </div>
        </div>
      );
    }
    return null;
  };

  const hasData = chartData.some((item) => item.count > 0);

  return (
    <div className='flex flex-col h-full'>
      {title && (
        <h3 className='mb-3 text-sm font-semibold text-foreground'>{title}</h3>
      )}
      <div className='flex-1 min-h-[220px] relative flex items-center justify-center'>
        {!hasData ? (
          <div className='text-sm italic text-muted-foreground'>
            Chưa có dữ liệu chuyến
          </div>
        ) : (
          <ResponsiveContainer width='100%' height='100%'>
            <AreaChart
              data={chartData as any}
              margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
            >
              <defs>
                <linearGradient id='colorTrips' x1='0' y1='0' x2='0' y2='1'>
                  <stop offset='5%' stopColor={color} stopOpacity={0.2} />
                  <stop offset='95%' stopColor={color} stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid
                strokeDasharray='3 3'
                vertical={false}
                stroke='var(--border)'
              />
              <XAxis
                dataKey='label'
                tick={{ fill: 'var(--muted-foreground)', fontSize: 10 }}
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                allowDecimals={false}
                tick={{ fill: 'var(--muted-foreground)', fontSize: 10 }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip content={<CustomTooltip />} />
              <Area
                type='monotone'
                dataKey='count'
                stroke={color}
                strokeWidth={2}
                fillOpacity={1}
                fill='url(#colorTrips)'
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
