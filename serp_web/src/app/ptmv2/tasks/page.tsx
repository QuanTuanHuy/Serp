/**
 * PTM v2 - Tasks Page (Modern Design)
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Modern task management interface
 */

'use client';

import { useMemo } from 'react';
import { QuickAddTask, TaskList } from '@/modules/ptmv2';
import { Card } from '@/shared/components/ui/card';
import { useGetTasksQuery } from '@/modules/ptmv2/services/taskApi';
import { CheckSquare, Circle, Clock } from 'lucide-react';

export default function TasksPage() {
  const { data: tasks = [], isLoading } = useGetTasksQuery({});

  // Calculate real-time stats
  const stats = useMemo(() => {
    const total = tasks.length;
    const completed = tasks.filter((t) => t.status === 'DONE').length;
    const inProgress = tasks.filter((t) => t.status === 'IN_PROGRESS').length;

    return { total, completed, inProgress };
  }, [tasks]);

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <div className='flex items-center gap-3 mb-2'>
            <div className='p-2 bg-primary/10 rounded-lg'>
              <CheckSquare className='h-6 w-6 text-primary' />
            </div>
            <h1 className='text-3xl font-bold tracking-tight'>Tasks</h1>
          </div>
          <p className='text-muted-foreground'>
            Stay organized and focused on what matters most
          </p>
        </div>
        <QuickAddTask />
      </div>

      {/* Stats Overview */}
      <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Total Tasks</p>
              <p className='text-2xl font-bold'>
                {isLoading ? '...' : stats.total}
              </p>
            </div>
            <div className='p-3 rounded-full bg-primary/10'>
              <CheckSquare className='h-5 w-5 text-primary' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>In Progress</p>
              <p className='text-2xl font-bold'>
                {isLoading ? '...' : stats.inProgress}
              </p>
            </div>
            <div className='p-3 rounded-full bg-amber-500/10'>
              <Clock className='h-5 w-5 text-amber-600 dark:text-amber-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Completed</p>
              <p className='text-2xl font-bold'>
                {isLoading ? '...' : stats.completed}
              </p>
            </div>
            <div className='p-3 rounded-full bg-green-500/10'>
              <Circle className='h-5 w-5 text-green-600 dark:text-green-400 fill-current' />
            </div>
          </div>
        </Card>
      </div>

      {/* Task List */}
      <TaskList />
    </div>
  );
}
