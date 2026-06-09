/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard charts
 */

import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

import type {
  TmsDashboardBreakdownItem,
  TmsDashboardTrendPoint,
} from '@/modules/first-mile/types';
import {
  CHART_COLORS,
  formatNumber,
  formatPercent,
  toChartData,
} from '../dashboardPageModels';

interface BreakdownChartProps {
  title: string;
  items: TmsDashboardBreakdownItem[];
  emptyText: string;
}

interface TrendChartProps {
  title: string;
  points: TmsDashboardTrendPoint[];
  emptyText: string;
}

export function BreakdownChart({
  title,
  items,
  emptyText,
}: BreakdownChartProps) {
  const data = toChartData(items).filter((item) => item.value > 0);

  return (
    <Card className='rounded-lg'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-base'>{title}</CardTitle>
      </CardHeader>
      <CardContent>
        {data.length === 0 ? (
          <ChartEmptyState text={emptyText} />
        ) : (
          <div className='grid min-h-64 gap-4 md:grid-cols-[0.9fr_1.1fr]'>
            <div className='h-64'>
              <ResponsiveContainer width='100%' height='100%'>
                <PieChart>
                  <Pie
                    data={data}
                    dataKey='value'
                    nameKey='name'
                    innerRadius={54}
                    outerRadius={82}
                    paddingAngle={2}
                  >
                    {data.map((entry, index) => (
                      <Cell
                        key={entry.name}
                        fill={CHART_COLORS[index % CHART_COLORS.length]}
                      />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(value: number) => formatNumber(value)}
                    labelFormatter={(label) => String(label)}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className='space-y-2 self-center'>
              {data.map((item, index) => (
                <div
                  key={item.name}
                  className='flex items-center justify-between gap-3 text-sm'
                >
                  <div className='flex min-w-0 items-center gap-2'>
                    <span
                      className='h-2.5 w-2.5 rounded-full'
                      style={{
                        backgroundColor:
                          CHART_COLORS[index % CHART_COLORS.length],
                      }}
                    />
                    <span className='truncate'>{item.name}</span>
                  </div>
                  <span className='shrink-0 text-muted-foreground'>
                    {formatNumber(item.value)} ·{' '}
                    {formatPercent(item.percentage)}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export function TrendChart({ title, points, emptyText }: TrendChartProps) {
  const hasData = points.some((point) => point.value > 0);

  return (
    <Card className='rounded-lg'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-base'>{title}</CardTitle>
      </CardHeader>
      <CardContent>
        {!hasData ? (
          <ChartEmptyState text={emptyText} />
        ) : (
          <div className='h-64'>
            <ResponsiveContainer width='100%' height='100%'>
              <BarChart data={points} margin={{ left: -18, right: 8 }}>
                <CartesianGrid strokeDasharray='3 3' vertical={false} />
                <XAxis
                  dataKey='label'
                  tickLine={false}
                  axisLine={false}
                  tickMargin={8}
                />
                <YAxis
                  allowDecimals={false}
                  tickLine={false}
                  axisLine={false}
                  tickMargin={8}
                />
                <Tooltip formatter={(value: number) => formatNumber(value)} />
                <Bar
                  dataKey='value'
                  fill='var(--chart-2)'
                  radius={[4, 4, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function ChartEmptyState({ text }: { text: string }) {
  return (
    <div className='flex h-64 items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground'>
      {text}
    </div>
  );
}
