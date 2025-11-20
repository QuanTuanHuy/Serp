/**
 * PTM v2 - Schedule Header Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Intelligent header with AI stats and quick actions
 */

'use client';

import { useState } from 'react';
import {
  Calendar,
  TrendingUp,
  Target,
  Zap,
  Flame,
  Sparkles,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { cn } from '@/shared/utils';

interface ScheduleHeaderProps {
  dateRange: { start: Date; end: Date };
  stats: {
    optimizedPercentage: number;
    tasksScheduled: number;
    totalTasks: number;
    plannedHours: number;
    focusBlocks: number;
  };
  onOptimize?: () => void;
  className?: string;
}

export function ScheduleHeader({
  dateRange,
  stats,
  onOptimize,
  className,
}: ScheduleHeaderProps) {
  const formatDateRange = () => {
    const start = dateRange.start;
    const end = dateRange.end;

    const monthStart = start.toLocaleDateString('en-US', { month: 'short' });
    const monthEnd = end.toLocaleDateString('en-US', { month: 'short' });
    const dayStart = start.getDate();
    const dayEnd = end.getDate();
    const year = start.getFullYear();

    if (monthStart === monthEnd) {
      return `${monthStart} ${dayStart}-${dayEnd}, ${year}`;
    }
    return `${monthStart} ${dayStart} - ${monthEnd} ${dayEnd}, ${year}`;
  };

  const getOptimizationColor = (percentage: number) => {
    if (percentage >= 80) return 'text-green-600 dark:text-green-400';
    if (percentage >= 60) return 'text-amber-600 dark:text-amber-400';
    return 'text-red-600 dark:text-red-400';
  };

  return (
    <div className={cn('space-y-4', className)}>
      {/* Title & Date Range */}
      <div className='flex items-center justify-between'>
        <div>
          <div className='flex items-center gap-3'>
            <Calendar className='h-8 w-8 text-primary' />
            <div>
              <h1 className='text-3xl font-bold'>Schedule</h1>
              <p className='text-muted-foreground mt-0.5'>
                Week of {formatDateRange()}
              </p>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className='flex items-center gap-2'>
          <Button onClick={onOptimize} className='flex items-center gap-2'>
            <Sparkles className='h-4 w-4 mr-2' />
            Re-optimize
            <kbd className='ml-3 hidden sm:inline-flex h-5 select-none items-center gap-1 rounded border bg-white/20 px-1.5 font-mono text-[10px] font-medium opacity-100'>
              ⌘O
            </kbd>
          </Button>
        </div>
      </div>

      {/* AI Stats Cards */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        {/* Optimization Score */}
        <Card className='border-2'>
          <CardHeader className='pb-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <div className='p-2 rounded-lg bg-purple-500/10'>
                  <TrendingUp className='h-4 w-4 text-purple-600 dark:text-purple-400' />
                </div>
                <span className='text-sm font-medium text-muted-foreground'>
                  AI Optimized
                </span>
              </div>
              <Badge variant='secondary' className='text-xs'>
                Live
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <div className='space-y-2'>
              <div
                className={cn(
                  'text-3xl font-bold',
                  getOptimizationColor(stats.optimizedPercentage)
                )}
              >
                {stats.optimizedPercentage}%
              </div>
              <div className='w-full h-2 bg-muted rounded-full overflow-hidden'>
                <div
                  className='h-full bg-gradient-to-r from-purple-600 to-blue-600 transition-all duration-500'
                  style={{ width: `${stats.optimizedPercentage}%` }}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Tasks Scheduled */}
        <Card className='border-2'>
          <CardHeader className='pb-3'>
            <div className='flex items-center gap-2'>
              <div className='p-2 rounded-lg bg-blue-500/10'>
                <Target className='h-4 w-4 text-blue-600 dark:text-blue-400' />
              </div>
              <span className='text-sm font-medium text-muted-foreground'>
                Tasks Scheduled
              </span>
            </div>
          </CardHeader>
          <CardContent>
            <div className='space-y-1'>
              <div className='text-3xl font-bold'>
                {stats.tasksScheduled}
                <span className='text-lg text-muted-foreground font-normal'>
                  /{stats.totalTasks}
                </span>
              </div>
              <p className='text-xs text-muted-foreground'>
                {stats.totalTasks - stats.tasksScheduled} unscheduled
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Planned Hours */}
        <Card className='border-2'>
          <CardHeader className='pb-3'>
            <div className='flex items-center gap-2'>
              <div className='p-2 rounded-lg bg-amber-500/10'>
                <Zap className='h-4 w-4 text-amber-600 dark:text-amber-400' />
              </div>
              <span className='text-sm font-medium text-muted-foreground'>
                Planned Hours
              </span>
            </div>
          </CardHeader>
          <CardContent>
            <div className='space-y-1'>
              <div className='text-3xl font-bold'>
                {stats.plannedHours}
                <span className='text-lg text-muted-foreground font-normal'>
                  h
                </span>
              </div>
              <p className='text-xs text-muted-foreground'>
                ~{Math.round(stats.plannedHours / 5)}h per day
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Focus Blocks */}
        <Card className='border-2'>
          <CardHeader className='pb-3'>
            <div className='flex items-center gap-2'>
              <div className='p-2 rounded-lg bg-red-500/10'>
                <Flame className='h-4 w-4 text-red-600 dark:text-red-400' />
              </div>
              <span className='text-sm font-medium text-muted-foreground'>
                Focus Blocks
              </span>
            </div>
          </CardHeader>
          <CardContent>
            <div className='space-y-1'>
              <div className='text-3xl font-bold'>{stats.focusBlocks}</div>
              <p className='text-xs text-muted-foreground'>
                Deep work sessions
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
