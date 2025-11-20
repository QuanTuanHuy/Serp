/**
 * PTM v2 - Activity Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Activity tracking and productivity insights
 */

'use client';

import { useMemo } from 'react';
import { Activity, Clock, TrendingUp, Zap } from 'lucide-react';
import { ActivityFeedEnhanced } from '@/modules/ptm/components/activity';
import { Card } from '@/shared/components/ui/card';

export default function ActivityPage() {
  // Mock stats - replace with real data from API
  const stats = useMemo(
    () => ({
      todayActivities: 12,
      weekActivities: 47,
      averagePerDay: 6.7,
      mostActiveHour: '10 AM',
    }),
    []
  );

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <div className='flex items-center gap-3 mb-2'>
            <div className='p-2 bg-blue-500/10 rounded-lg'>
              <Activity className='h-6 w-6 text-blue-600' />
            </div>
            <h1 className='text-3xl font-bold tracking-tight'>Activity</h1>
          </div>
          <p className='text-muted-foreground'>
            Track your productivity and see what's been happening
          </p>
        </div>
      </div>

      {/* Stats Overview */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Today</p>
              <p className='text-2xl font-bold mt-1'>{stats.todayActivities}</p>
            </div>
            <div className='p-2 bg-blue-100 dark:bg-blue-950/20 rounded-lg'>
              <Activity className='h-5 w-5 text-blue-600' />
            </div>
          </div>
          <p className='text-xs text-muted-foreground mt-2'>Activities today</p>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>This Week</p>
              <p className='text-2xl font-bold mt-1'>{stats.weekActivities}</p>
            </div>
            <div className='p-2 bg-green-100 dark:bg-green-950/20 rounded-lg'>
              <TrendingUp className='h-5 w-5 text-green-600' />
            </div>
          </div>
          <p className='text-xs text-green-600 mt-2 flex items-center gap-1'>
            <span>↑ 12%</span>
            <span className='text-muted-foreground'>from last week</span>
          </p>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Avg/Day</p>
              <p className='text-2xl font-bold mt-1'>
                {stats.averagePerDay.toFixed(1)}
              </p>
            </div>
            <div className='p-2 bg-purple-100 dark:bg-purple-950/20 rounded-lg'>
              <Zap className='h-5 w-5 text-purple-600' />
            </div>
          </div>
          <p className='text-xs text-muted-foreground mt-2'>
            Activities per day
          </p>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Most Active</p>
              <p className='text-2xl font-bold mt-1'>{stats.mostActiveHour}</p>
            </div>
            <div className='p-2 bg-amber-100 dark:bg-amber-950/20 rounded-lg'>
              <Clock className='h-5 w-5 text-amber-600' />
            </div>
          </div>
          <p className='text-xs text-muted-foreground mt-2'>Peak hour today</p>
        </Card>
      </div>

      {/* Activity Feed */}
      <ActivityFeedEnhanced maxItems={50} />
    </div>
  );
}
