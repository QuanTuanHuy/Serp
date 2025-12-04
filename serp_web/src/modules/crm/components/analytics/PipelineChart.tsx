// PipelineChart Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Skeleton,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import type { PipelineMetrics } from '../../types';

interface PipelineChartProps {
  data: PipelineMetrics[];
  isLoading?: boolean;
  className?: string;
  chartType?: 'bar' | 'pie';
  showValue?: boolean;
}

// Color palette for different stages
const STAGE_COLORS = {
  PROSPECTING: '#3B82F6',
  QUALIFICATION: '#8B5CF6',
  PROPOSAL: '#F59E0B',
  NEGOTIATION: '#F97316',
  CLOSED_WON: '#10B981',
  CLOSED_LOST: '#EF4444',
};

// Get color for stage
const getStageColor = (stage: string): string => {
  return STAGE_COLORS[stage as keyof typeof STAGE_COLORS] || '#6B7280';
};

// Custom tooltip for charts
const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className='bg-popover text-popover-foreground p-3 border border-border rounded-lg shadow-lg'>
        <p className='font-medium'>{label}</p>
        <p className='text-sm text-muted-foreground'>
          Count:{' '}
          <span className='font-medium text-primary'>{payload[0].value}</span>
        </p>
        {payload[0].payload.totalValue && (
          <p className='text-sm text-muted-foreground'>
            Value:{' '}
            <span className='font-medium text-emerald-600 dark:text-emerald-400'>
              ${payload[0].payload.totalValue.toLocaleString()}
            </span>
          </p>
        )}
      </div>
    );
  }
  return null;
};

// Loading skeleton
const LoadingSkeleton = () => (
  <div className='space-y-4'>
    <Skeleton className='h-6 w-48' />
    <div className='space-y-3'>
      <Skeleton className='h-4 w-full' />
      <Skeleton className='h-4 w-5/6' />
      <Skeleton className='h-4 w-4/6' />
      <Skeleton className='h-4 w-3/6' />
    </div>
  </div>
);

export const PipelineChart: React.FC<PipelineChartProps> = ({
  data,
  isLoading = false,
  className,
  chartType = 'bar',
  showValue = true,
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
          <CardTitle>Pipeline Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='flex items-center justify-center h-64 text-muted-foreground'>
            <div className='text-center'>
              <p>No pipeline data available</p>
              <p className='text-sm mt-1'>
                Data will appear here once opportunities are created
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Prepare data for charts
  const chartData = data.map((item) => ({
    stage: item.stage
      .replace('_', ' ')
      .toLowerCase()
      .replace(/\b\w/g, (l) => l.toUpperCase()),
    count: item.count,
    totalValue: item.value,
    fill: getStageColor(item.stage),
  }));

  return (
    <Card className={cn('', className)}>
      <CardHeader>
        <div className='flex items-center justify-between'>
          <CardTitle>Pipeline Overview</CardTitle>
          <span className='text-sm text-muted-foreground'>
            Total: {data.reduce((sum, item) => sum + item.count, 0)}{' '}
            opportunities
          </span>
        </div>
      </CardHeader>
      <CardContent>
        {chartType === 'bar' ? (
          <div className='h-80'>
            <ResponsiveContainer width='100%' height='100%'>
              <BarChart
                data={chartData}
                margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray='3 3' />
                <XAxis
                  dataKey='stage'
                  tick={{ fontSize: 12 }}
                  angle={-45}
                  textAnchor='end'
                  height={80}
                />
                <YAxis />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey='count' fill='#3B82F6' radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className='h-80'>
            <ResponsiveContainer width='100%' height='100%'>
              <PieChart>
                <Pie
                  data={chartData}
                  cx='50%'
                  cy='50%'
                  labelLine={false}
                  label={({ stage, count, percent }: any) =>
                    `${stage}: ${count} (${(percent * 100).toFixed(0)}%)`
                  }
                  outerRadius={80}
                  fill='#8884d8'
                  dataKey='count'
                >
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Pipeline Summary */}
        {showValue && (
          <div className='mt-6 grid grid-cols-2 md:grid-cols-3 gap-4'>
            <div className='text-center p-3 bg-muted/50 rounded-lg'>
              <p className='text-sm text-muted-foreground'>
                Total Opportunities
              </p>
              <p className='text-xl font-bold text-foreground'>
                {data.reduce((sum, item) => sum + item.count, 0)}
              </p>
            </div>
            <div className='text-center p-3 bg-muted/50 rounded-lg'>
              <p className='text-sm text-muted-foreground'>Total Value</p>
              <p className='text-xl font-bold text-emerald-600 dark:text-emerald-400'>
                $
                {data
                  .reduce((sum, item) => sum + item.value, 0)
                  .toLocaleString()}
              </p>
            </div>
            <div className='text-center p-3 bg-muted/50 rounded-lg col-span-2 md:col-span-1'>
              <p className='text-sm text-muted-foreground'>Avg Deal Size</p>
              <p className='text-xl font-bold text-primary'>
                $
                {(
                  data.reduce((sum, item) => sum + item.value, 0) /
                  Math.max(
                    data.reduce((sum, item) => sum + item.count, 0),
                    1
                  )
                ).toLocaleString()}
              </p>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default PipelineChart;
