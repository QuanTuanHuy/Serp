/**
 * PTM v2 - Tasks Page (Modern Design)
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Modern task management interface
 */

'use client';

import { useState } from 'react';
import { QuickAddTask, TaskList } from '@/modules/ptmv2';
import { Card } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import {
  CheckSquare,
  Circle,
  Clock,
  TrendingUp,
  Filter,
  LayoutGrid,
  List,
  Calendar,
  Sparkles,
} from 'lucide-react';

export default function TasksPage() {
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');

  return (
    <div className='space-y-6'>
      {/* Header with Gradient Background */}
      <div className='relative overflow-hidden rounded-lg bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 p-8'>
        <div className='absolute inset-0 bg-grid-white/10' />
        <div className='relative z-10 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
          <div className='text-white'>
            <div className='flex items-center gap-2 mb-2'>
              <CheckSquare className='h-8 w-8' />
              <h1 className='text-3xl font-bold'>Tasks</h1>
            </div>
            <p className='text-white/90 text-sm'>
              Stay organized and focused on what matters most
            </p>
          </div>
          <QuickAddTask />
        </div>
      </div>

      {/* Stats Overview */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <Card className='p-4 bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-950/20 dark:to-blue-900/20 border-blue-200 dark:border-blue-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Total Tasks</p>
              <p className='text-2xl font-bold text-blue-600 dark:text-blue-400'>
                24
              </p>
            </div>
            <div className='p-3 rounded-full bg-blue-500/10'>
              <CheckSquare className='h-5 w-5 text-blue-600 dark:text-blue-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-green-50 to-green-100 dark:from-green-950/20 dark:to-green-900/20 border-green-200 dark:border-green-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Completed</p>
              <p className='text-2xl font-bold text-green-600 dark:text-green-400'>
                18
              </p>
            </div>
            <div className='p-3 rounded-full bg-green-500/10'>
              <Circle className='h-5 w-5 text-green-600 dark:text-green-400 fill-current' />
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-amber-50 to-amber-100 dark:from-amber-950/20 dark:to-amber-900/20 border-amber-200 dark:border-amber-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>In Progress</p>
              <p className='text-2xl font-bold text-amber-600 dark:text-amber-400'>
                6
              </p>
            </div>
            <div className='p-3 rounded-full bg-amber-500/10'>
              <Clock className='h-5 w-5 text-amber-600 dark:text-amber-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-950/20 dark:to-purple-900/20 border-purple-200 dark:border-purple-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Completion</p>
              <p className='text-2xl font-bold text-purple-600 dark:text-purple-400'>
                75%
              </p>
            </div>
            <div className='p-3 rounded-full bg-purple-500/10'>
              <TrendingUp className='h-5 w-5 text-purple-600 dark:text-purple-400' />
            </div>
          </div>
        </Card>
      </div>

      {/* Quick Filters */}
      <div className='flex flex-wrap items-center gap-2'>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          All Tasks
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          My Tasks
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          🔴 High Priority
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          ⏰ Due Today
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          🟣 Deep Work
        </Badge>
      </div>

      {/* Toolbar */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm'>
            <Filter className='h-4 w-4 mr-2' />
            Filter
          </Button>
          <Button variant='outline' size='sm'>
            <Calendar className='h-4 w-4 mr-2' />
            Group by Project
          </Button>
          <Button variant='outline' size='sm'>
            <Sparkles className='h-4 w-4 mr-2' />
            AI Sort
          </Button>
        </div>

        <div className='flex items-center gap-1 bg-muted p-1 rounded-lg'>
          <Button
            variant={viewMode === 'list' ? 'default' : 'ghost'}
            size='sm'
            onClick={() => setViewMode('list')}
            className='h-8'
          >
            <List className='h-4 w-4' />
          </Button>
          <Button
            variant={viewMode === 'grid' ? 'default' : 'ghost'}
            size='sm'
            onClick={() => setViewMode('grid')}
            className='h-8'
          >
            <LayoutGrid className='h-4 w-4' />
          </Button>
        </div>
      </div>

      {/* Task List */}
      <TaskList />

      {/* AI Insights */}
      <Card className='p-4 bg-gradient-to-r from-purple-50 to-blue-50 dark:from-purple-950/10 dark:to-blue-950/10 border-purple-200 dark:border-purple-800'>
        <div className='flex items-start gap-3'>
          <Sparkles className='h-5 w-5 text-purple-600 dark:text-purple-400 mt-0.5 flex-shrink-0' />
          <div className='space-y-1'>
            <p className='font-semibold text-sm text-purple-900 dark:text-purple-100'>
              💡 AI Suggestion
            </p>
            <p className='text-sm text-purple-700 dark:text-purple-300'>
              You have 3 high-priority tasks due today. Consider scheduling a
              2-hour focus block this afternoon to complete them.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}
