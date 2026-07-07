import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Cell,
} from 'recharts';
import { ChartItemDto } from '../../types';

interface DashboardBarChartProps {
  data: ChartItemDto[];
  colorMap?: Record<string, string>;
  defaultColor?: string;
  title?: string;
  minHeightClassName?: string;
}

export function DashboardBarChart({
  data,
  colorMap,
  defaultColor = '#991B1B', // Crimson
  title,
  minHeightClassName = 'min-h-[240px]',
}: DashboardBarChartProps) {
  const chartData = data || [];

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const dataPoint = payload[0].payload;
      const color = colorMap
        ? colorMap[dataPoint.name] || defaultColor
        : defaultColor;
      return (
        <div className='rounded-lg border border-border bg-popover p-2.5 text-popover-foreground shadow-lg'>
          <div className='flex items-center gap-2'>
            <span
              className='w-3 h-3 rounded-full'
              style={{ backgroundColor: color }}
            />
            <span className='text-sm font-semibold text-foreground'>
              {dataPoint.label || dataPoint.name}
            </span>
          </div>
          <p className='mt-1 pl-5 text-xs text-muted-foreground'>
            Số lượng:{' '}
            <span className='font-bold text-foreground'>{dataPoint.count}</span>
          </p>
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
      <div
        className={`flex-1 ${minHeightClassName} relative flex items-center justify-center`}
      >
        {!hasData ? (
          <div className='text-sm italic text-muted-foreground'>
            Chưa có dữ liệu
          </div>
        ) : (
          <ResponsiveContainer width='100%' height='100%'>
            <BarChart
              data={chartData as any}
              margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
            >
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
              <Bar dataKey='count' radius={[4, 4, 0, 0]} maxBarSize={40}>
                {chartData.map((entry, index) => {
                  const color = colorMap
                    ? colorMap[entry.name] || defaultColor
                    : defaultColor;
                  return <Cell key={`cell-${index}`} fill={color} />;
                })}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
