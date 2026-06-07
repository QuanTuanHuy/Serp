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

  return (
    <div className="flex flex-col h-full">
      {title && (
        <h3 className="text-sm font-semibold text-zinc-700 dark:text-zinc-300 mb-3">
          {title}
        </h3>
      )}
      <div className="flex-1 min-h-[200px] relative flex items-center justify-center">
        {chartData.length === 0 ? (
          <div className="text-sm text-zinc-400 dark:text-zinc-500 italic">
            No data available
          </div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData as any}
                cx="50%"

                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={4}
                dataKey="count"
              >
                {chartData.map((entry, index) => {
                  const color = colorMap[entry.name] || '#6B7280';
                  return (
                    <Cell
                      key={`cell-${index}`}
                      fill={color}
                      stroke="transparent"
                    />
                  );
                })}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
              <Legend
                verticalAlign="bottom"
                height={36}
                iconType="circle"
                iconSize={10}
                formatter={(value, entry: any) => {
                  const payload = entry.payload;
                  const item = data.find((d) => d.name === payload.name);
                  return (
                    <span className="text-xs text-zinc-600 dark:text-zinc-400 font-medium ml-1">
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
