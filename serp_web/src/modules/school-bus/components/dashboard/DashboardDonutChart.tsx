import React from 'react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
} from 'recharts';
import { ChartItemDto } from '../../types';

interface DashboardDonutChartProps {
  data: ChartItemDto[];
  colorMap: Record<string, string>;
  title?: string;
}

export function DashboardDonutChart({
  data,
  colorMap,
  title,
}: DashboardDonutChartProps) {
  // Filter out zero counts to keep the chart clean, but show in legend
  const chartData = data.filter((item) => item.count > 0);

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const dataPoint = payload[0].payload;
      const color = colorMap[dataPoint.name] || '#6B7280';
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
            Count:{' '}
            <span className='font-bold text-foreground'>{dataPoint.count}</span>
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className='flex flex-col h-full'>
      {title && (
        <h3 className='mb-3 text-sm font-semibold text-foreground'>{title}</h3>
      )}
      <div className='flex-1 min-h-[200px] relative flex items-center justify-center'>
        {chartData.length === 0 ? (
          <div className='text-sm italic text-muted-foreground'>
            No data available
          </div>
        ) : (
          <ResponsiveContainer width='100%' height='100%'>
            <PieChart>
              <Pie
                data={chartData as any}
                cx='50%'
                cy='50%'
                innerRadius={60}
                outerRadius={80}
                paddingAngle={4}
                dataKey='count'
              >
                {chartData.map((entry, index) => {
                  const color = colorMap[entry.name] || '#6B7280';
                  return (
                    <Cell
                      key={`cell-${index}`}
                      fill={color}
                      stroke='transparent'
                    />
                  );
                })}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
              <Legend
                verticalAlign='bottom'
                height={36}
                iconType='circle'
                iconSize={10}
                formatter={(value, entry: any) => {
                  const payload = entry.payload;
                  const item = data.find((d) => d.name === payload.name);
                  return (
                    <span className='ml-1 text-xs font-medium text-muted-foreground'>
                      {payload.label || payload.name} ({item?.count || 0})
                    </span>
                  );
                }}
              />
            </PieChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
