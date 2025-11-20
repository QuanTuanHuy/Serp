/**
 * PTM v2 - Analytics Dashboard Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Productivity analytics and insights
 */

'use client';

import { useState } from 'react';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  TrendingUp,
  TrendingDown,
  Clock,
  CheckCircle2,
  Target,
  Zap,
  Calendar,
  Activity,
} from 'lucide-react';
import { cn } from '@/shared/utils';

interface AnalyticsDashboardProps {
  className?: string;
}

// Mock data for charts
const weeklyData = [
  { day: 'Mon', focusTime: 4.5, tasks: 8, completed: 6 },
  { day: 'Tue', focusTime: 3.0, tasks: 10, completed: 7 },
  { day: 'Wed', focusTime: 5.0, tasks: 12, completed: 10 },
  { day: 'Thu', focusTime: 4.0, tasks: 9, completed: 8 },
  { day: 'Fri', focusTime: 3.5, tasks: 11, completed: 9 },
  { day: 'Sat', focusTime: 2.0, tasks: 5, completed: 4 },
  { day: 'Sun', focusTime: 1.5, tasks: 3, completed: 3 },
];

const timeDistribution = [
  { name: 'Deep Work', value: 45, color: '#8b5cf6' },
  { name: 'Meetings', value: 20, color: '#3b82f6' },
  { name: 'Email', value: 15, color: '#10b981' },
  { name: 'Breaks', value: 10, color: '#f59e0b' },
  { name: 'Other', value: 10, color: '#6b7280' },
];

const completionTrend = [
  { week: 'Week 1', rate: 65 },
  { week: 'Week 2', rate: 72 },
  { week: 'Week 3', rate: 68 },
  { week: 'Week 4', rate: 85 },
];

const productivityHeatmap = [
  { hour: '6 AM', Mon: 1, Tue: 1, Wed: 2, Thu: 1, Fri: 1, Sat: 0, Sun: 0 },
  { hour: '9 AM', Mon: 5, Tue: 4, Wed: 5, Thu: 5, Fri: 4, Sat: 2, Sun: 1 },
  { hour: '12 PM', Mon: 3, Tue: 3, Wed: 4, Thu: 3, Fri: 3, Sat: 3, Sun: 2 },
  { hour: '3 PM', Mon: 4, Tue: 4, Wed: 4, Thu: 5, Fri: 4, Sat: 1, Sun: 1 },
  { hour: '6 PM', Mon: 2, Tue: 2, Wed: 3, Thu: 2, Fri: 2, Sat: 4, Sun: 3 },
  { hour: '9 PM', Mon: 1, Tue: 1, Wed: 1, Thu: 1, Fri: 2, Sat: 3, Sun: 2 },
];

const kpiData = [
  {
    label: 'Completion Rate',
    value: '85%',
    change: '+12%',
    trend: 'up' as const,
    icon: CheckCircle2,
    color: 'text-green-600',
    bgColor: 'bg-green-50 dark:bg-green-950/20',
  },
  {
    label: 'Focus Hours/Week',
    value: '23.5h',
    change: '+3.2h',
    trend: 'up' as const,
    icon: Clock,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50 dark:bg-purple-950/20',
  },
  {
    label: 'Active Projects',
    value: '12',
    change: '-2',
    trend: 'down' as const,
    icon: Target,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950/20',
  },
  {
    label: 'Productivity Score',
    value: '92',
    change: '+8',
    trend: 'up' as const,
    icon: Zap,
    color: 'text-amber-600',
    bgColor: 'bg-amber-50 dark:bg-amber-950/20',
  },
];

export function AnalyticsDashboard({ className }: AnalyticsDashboardProps) {
  const [timeRange, setTimeRange] = useState('7days');

  return (
    <div className={cn('space-y-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h2 className='text-2xl font-bold flex items-center gap-2'>
            <Activity className='h-6 w-6 text-blue-600' />
            Productivity Analytics
          </h2>
          <p className='text-muted-foreground mt-1'>
            Track your performance and identify patterns
          </p>
        </div>

        <Select value={timeRange} onValueChange={setTimeRange}>
          <SelectTrigger className='w-[150px]'>
            <Calendar className='h-4 w-4 mr-2' />
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='7days'>Last 7 Days</SelectItem>
            <SelectItem value='30days'>Last 30 Days</SelectItem>
            <SelectItem value='90days'>Last 90 Days</SelectItem>
            <SelectItem value='year'>This Year</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* KPI Cards */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
        {kpiData.map((kpi, index) => {
          const Icon = kpi.icon;
          return (
            <Card key={index}>
              <CardContent className='p-6'>
                <div className='flex items-center justify-between'>
                  <div className={cn('p-2 rounded-lg', kpi.bgColor)}>
                    <Icon className={cn('h-5 w-5', kpi.color)} />
                  </div>
                  <div
                    className={cn(
                      'flex items-center gap-1 text-sm font-medium',
                      kpi.trend === 'up' ? 'text-green-600' : 'text-red-600'
                    )}
                  >
                    {kpi.trend === 'up' ? (
                      <TrendingUp className='h-4 w-4' />
                    ) : (
                      <TrendingDown className='h-4 w-4' />
                    )}
                    {kpi.change}
                  </div>
                </div>
                <div className='mt-4'>
                  <p className='text-2xl font-bold'>{kpi.value}</p>
                  <p className='text-sm text-muted-foreground'>{kpi.label}</p>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Charts Row 1 */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        {/* Weekly Overview */}
        <Card>
          <CardHeader>
            <CardTitle>Weekly Overview</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width='100%' height={300}>
              <BarChart data={weeklyData}>
                <CartesianGrid strokeDasharray='3 3' stroke='#e5e7eb' />
                <XAxis dataKey='day' stroke='#6b7280' />
                <YAxis stroke='#6b7280' />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                  }}
                />
                <Legend />
                <Bar dataKey='focusTime' name='Focus Hours' fill='#8b5cf6' />
                <Bar
                  dataKey='completed'
                  name='Completed Tasks'
                  fill='#10b981'
                />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Time Distribution */}
        <Card>
          <CardHeader>
            <CardTitle>Time Distribution</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width='100%' height={300}>
              <PieChart>
                <Pie
                  data={timeDistribution}
                  cx='50%'
                  cy='50%'
                  labelLine={false}
                  label={({ name, percent }) =>
                    `${name} ${((percent || 0) * 100).toFixed(0)}%`
                  }
                  outerRadius={100}
                  fill='#8884d8'
                  dataKey='value'
                >
                  {timeDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Charts Row 2 */}
      <div className='grid grid-cols-1 gap-6'>
        {/* Completion Rate Trend */}
        <Card>
          <CardHeader>
            <CardTitle>Completion Rate Trend</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width='100%' height={250}>
              <LineChart data={completionTrend}>
                <CartesianGrid strokeDasharray='3 3' stroke='#e5e7eb' />
                <XAxis dataKey='week' stroke='#6b7280' />
                <YAxis stroke='#6b7280' />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                  }}
                />
                <Legend />
                <Line
                  type='monotone'
                  dataKey='rate'
                  name='Completion Rate (%)'
                  stroke='#8b5cf6'
                  strokeWidth={3}
                  dot={{ fill: '#8b5cf6', r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Productivity Heatmap */}
        <Card>
          <CardHeader>
            <CardTitle>Productivity Heatmap</CardTitle>
          </CardHeader>
          <CardContent>
            <div className='overflow-x-auto'>
              <table className='w-full border-collapse'>
                <thead>
                  <tr>
                    <th className='text-left p-2 text-sm font-medium text-muted-foreground'>
                      Time
                    </th>
                    {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(
                      (day) => (
                        <th
                          key={day}
                          className='text-center p-2 text-sm font-medium text-muted-foreground'
                        >
                          {day}
                        </th>
                      )
                    )}
                  </tr>
                </thead>
                <tbody>
                  {productivityHeatmap.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                      <td className='p-2 text-sm text-muted-foreground'>
                        {row.hour}
                      </td>
                      {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(
                        (day) => {
                          const value = row[day as keyof typeof row] as number;
                          const intensity =
                            value === 0
                              ? 'bg-gray-100 dark:bg-gray-800'
                              : value <= 2
                                ? 'bg-purple-200 dark:bg-purple-900/40'
                                : value <= 4
                                  ? 'bg-purple-400 dark:bg-purple-700/60'
                                  : 'bg-purple-600 dark:bg-purple-600';
                          return (
                            <td key={day} className='p-1'>
                              <div
                                className={cn(
                                  'w-full h-10 rounded cursor-pointer transition-all hover:scale-110',
                                  intensity
                                )}
                                title={`${day} ${row.hour}: ${value} tasks`}
                              />
                            </td>
                          );
                        }
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className='flex items-center justify-center gap-2 mt-4 text-xs text-muted-foreground'>
              <span>Less</span>
              <div className='flex gap-1'>
                {[0, 1, 2, 3, 4].map((level) => {
                  const color =
                    level === 0
                      ? 'bg-gray-100 dark:bg-gray-800'
                      : level <= 1
                        ? 'bg-purple-200 dark:bg-purple-900/40'
                        : level <= 3
                          ? 'bg-purple-400 dark:bg-purple-700/60'
                          : 'bg-purple-600 dark:bg-purple-600';
                  return (
                    <div key={level} className={cn('w-4 h-4 rounded', color)} />
                  );
                })}
              </div>
              <span>More</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Insights */}
      <Card className='bg-gradient-to-br from-blue-50 to-purple-50 dark:from-blue-950/20 dark:to-purple-950/20 border-blue-200 dark:border-blue-800'>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Zap className='h-5 w-5 text-amber-600' />
            Key Insights
          </CardTitle>
        </CardHeader>
        <CardContent className='space-y-3'>
          <div className='flex items-start gap-2'>
            <div className='w-2 h-2 rounded-full bg-green-600 mt-2' />
            <p className='text-sm'>
              Your completion rate increased by <strong>12%</strong> this week.
              Great progress!
            </p>
          </div>
          <div className='flex items-start gap-2'>
            <div className='w-2 h-2 rounded-full bg-purple-600 mt-2' />
            <p className='text-sm'>
              Peak productivity hours: <strong>9 AM - 12 PM</strong>. Schedule
              deep work during this time.
            </p>
          </div>
          <div className='flex items-start gap-2'>
            <div className='w-2 h-2 rounded-full bg-amber-600 mt-2' />
            <p className='text-sm'>
              Wednesday is your most productive day with an average of{' '}
              <strong>10 completed tasks</strong>.
            </p>
          </div>
          <div className='flex items-start gap-2'>
            <div className='w-2 h-2 rounded-full bg-blue-600 mt-2' />
            <p className='text-sm'>
              Consider reducing meeting time on Fridays to improve focus hours.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
