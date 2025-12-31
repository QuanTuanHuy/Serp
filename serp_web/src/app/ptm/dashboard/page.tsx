/**
 * PTM v2 - Dashboard Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Main dashboard view
 */

'use client';

import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { CheckSquare, Target, Clock, AlertCircle, Plus } from 'lucide-react';
import { setActiveView } from '@/modules/ptm/store/uiSlice';
import {
  StatsCard,
  TodaySchedule,
  RecentTasks,
  WeeklyOverview,
} from '@/modules/ptm/components/dashboard';

import { useDashboardStats, useTaskDialogs } from '@/modules/ptm/hooks';
import { Button } from '@/shared';
import { CreateTaskDialog } from '@/modules/ptm/components/tasks/dialogs';

export default function PTMDashboardPage() {
  const dispatch = useDispatch();
  const { stats, isLoading } = useDashboardStats();
  const { createDialog } = useTaskDialogs();

  useEffect(() => {
    dispatch(setActiveView('dashboard'));
  }, [dispatch]);

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Cmd/Ctrl + R to refresh dashboard
      if ((e.metaKey || e.ctrlKey) && e.key === 'r') {
        e.preventDefault();
        window.location.reload();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-2xl font-bold'>Dashboard</h1>
          <p className='text-muted-foreground'>
            Welcome back! Here's your productivity overview.
          </p>
        </div>
        <Button onClick={createDialog.openCreate}>
          <Plus className='mr-2 h-4 w-4' />
          Add Task
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
        <StatsCard
          title='Total Tasks'
          value={stats.totalTasks}
          subtitle={`${stats.completedTasks} completed`}
          icon={CheckSquare}
          color='blue'
          isLoading={isLoading}
        />

        <StatsCard
          title='Completion Rate'
          value={`${stats.completionRate.toFixed(0)}%`}
          subtitle='of all tasks'
          icon={Target}
          color='green'
          trend={{
            value: stats.trends.completionRateTrend,
            label: 'from last week',
            isPositive: stats.trends.completionRateTrend > 0,
          }}
          isLoading={isLoading}
        />

        <StatsCard
          title='Focus Time Today'
          value={`${stats.focusTimeToday.toFixed(1)}h`}
          subtitle={`${stats.tasksToday} tasks scheduled`}
          icon={Clock}
          color='purple'
          trend={{
            value: stats.trends.focusTimeTrend,
            label: 'from last week',
            isPositive: stats.trends.focusTimeTrend > 0,
          }}
          isLoading={isLoading}
        />

        <StatsCard
          title='Overdue Tasks'
          value={stats.overdueTasks}
          subtitle='need attention'
          icon={AlertCircle}
          color={stats.overdueTasks > 0 ? 'red' : 'amber'}
          isLoading={isLoading}
        />
      </div>

      {/* Main Content Grid */}
      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
        {/* Left Column - 2/3 width */}
        <div className='lg:col-span-2 space-y-6'>
          <TodaySchedule />
          <WeeklyOverview />
        </div>

        {/* Right Column - 1/3 width */}
        <div className='lg:col-span-1'>
          <RecentTasks />
        </div>
      </div>

      {/* Dialogs */}
      <CreateTaskDialog {...createDialog} />
    </div>
  );
}
