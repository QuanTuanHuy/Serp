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
}

export function DashboardBarChart({
  data,
  colorMap,
  defaultColor = '#991B1B', // Crimson
  title,
}: DashboardBarChartProps) {
  const chartData = data || [];

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const dataPoint = payload[0].payload;
      const color = colorMap ? (colorMap[dataPoint.name] || defaultColor) : defaultColor;
      return (
        <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-2.5 rounded-lg shadow-lg">
          <div className="flex items-center gap-2">
            <span
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: color }}
            />
            <span className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
              {dataPoint.label || dataPoint.name}
            </span>
          </div>
          <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-1 pl-5">
            Count: <span className="font-bold text-zinc-800 dark:text-zinc-200">{dataPoint.count}</span>
          </p>
        </div>
      );
    }
    return null;
  };

  const hasData = chartData.some((item) => item.count > 0);

  return (
    <div className="flex flex-col h-full">
      {title && (
        <h3 className="text-sm font-semibold text-zinc-700 dark:text-zinc-300 mb-3">
          {title}
        </h3>
      )}
      <div className="flex-1 min-h-[220px] relative flex items-center justify-center">
        {!hasData ? (
          <div className="text-sm text-zinc-400 dark:text-zinc-500 italic">
            No data available
          </div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={chartData as any}
              margin={{ top: 10, right: 10, left: -20, bottom: 0 }}

            >
              <CartesianGrid
                strokeDasharray="3 3"
                vertical={false}
                stroke="#E4E4E7"
                className="dark:stroke-zinc-800"
              />
              <XAxis
                dataKey="label"
                tick={{ fill: '#71717A', fontSize: 10 }}
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                allowDecimals={false}
                tick={{ fill: '#71717A', fontSize: 10 }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="count" radius={[4, 4, 0, 0]} maxBarSize={40}>
                {chartData.map((entry, index) => {
                  const color = colorMap ? (colorMap[entry.name] || defaultColor) : defaultColor;
                  return (
                    <Cell
                      key={`cell-${index}`}
                      fill={color}
                    />
                  );
                })}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
