/**
 * PTM Schedule Page - Optimize Your Tasks
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - task optimization
 */

'use client';

import React from 'react';
import { Button, Card } from '@/shared/components';
import { Brain, Zap, Target, Clock, TrendingUp, RefreshCw } from 'lucide-react';

const Schedule: React.FC = () => {
  const optimizedTasks = [
    {
      id: 1,
      title: 'Code review for authentication module',
      priority: 'High',
      estimatedTime: '2 hours',
      suggestedTime: '9:00 AM - 11:00 AM',
      reasoning: 'Best focus time, matches your peak productivity hours',
    },
    {
      id: 2,
      title: 'Team standup meeting',
      priority: 'Medium',
      estimatedTime: '30 minutes',
      suggestedTime: '11:30 AM - 12:00 PM',
      reasoning: 'Good transition time before lunch break',
    },
    {
      id: 3,
      title: 'Database optimization research',
      priority: 'Medium',
      estimatedTime: '1.5 hours',
      suggestedTime: '2:00 PM - 3:30 PM',
      reasoning: 'Post-lunch energy suitable for research tasks',
    },
  ];

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Brain className='h-6 w-6' />
          <h1 className='text-2xl font-bold'>Optimize Your Tasks</h1>
        </div>
        <Button>
          <RefreshCw className='mr-2 h-4 w-4' />
          Re-optimize
        </Button>
      </div>

      {/* AI Insights */}
      <div className='grid grid-cols-1 gap-4 md:grid-cols-3'>
        <Card className='p-6'>
          <div className='flex items-center gap-2 mb-2'>
            <Zap className='h-5 w-5 text-yellow-500' />
            <h3 className='font-semibold'>Energy Level</h3>
          </div>
          <p className='text-2xl font-bold'>High</p>
          <p className='text-sm text-muted-foreground'>
            Peak productivity window: 9 AM - 11 AM
          </p>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center gap-2 mb-2'>
            <Target className='h-5 w-5 text-green-500' />
            <h3 className='font-semibold'>Focus Score</h3>
          </div>
          <p className='text-2xl font-bold'>8.5/10</p>
          <p className='text-sm text-muted-foreground'>
            Excellent focus conditions today
          </p>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center gap-2 mb-2'>
            <TrendingUp className='h-5 w-5 text-blue-500' />
            <h3 className='font-semibold'>Efficiency</h3>
          </div>
          <p className='text-2xl font-bold'>92%</p>
          <p className='text-sm text-muted-foreground'>+15% from last week</p>
        </Card>
      </div>

      {/* Optimized Schedule */}
      <Card className='p-6'>
        <h3 className='mb-4 text-lg font-semibold'>AI-Optimized Schedule</h3>
        <div className='space-y-4'>
          {optimizedTasks.map((task) => (
            <div key={task.id} className='rounded-lg border p-4'>
              <div className='flex items-start justify-between'>
                <div className='flex-1'>
                  <h4 className='font-medium'>{task.title}</h4>
                  <div className='mt-2 flex items-center gap-4 text-sm text-muted-foreground'>
                    <div className='flex items-center gap-1'>
                      <Target className='h-4 w-4' />
                      <span>{task.priority}</span>
                    </div>
                    <div className='flex items-center gap-1'>
                      <Clock className='h-4 w-4' />
                      <span>{task.estimatedTime}</span>
                    </div>
                  </div>
                  <div className='mt-2'>
                    <p className='text-sm font-medium text-primary'>
                      Suggested: {task.suggestedTime}
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      {task.reasoning}
                    </p>
                  </div>
                </div>
                <div className='flex gap-2'>
                  <Button size='sm' variant='outline'>
                    Adjust
                  </Button>
                  <Button size='sm'>Accept</Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Productivity Tips */}
      <Card className='p-6'>
        <h3 className='mb-4 text-lg font-semibold'>
          Today's Productivity Tips
        </h3>
        <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
          <div className='rounded-lg bg-muted p-4'>
            <h4 className='font-medium'>🎯 Focus Block</h4>
            <p className='text-sm text-muted-foreground'>
              Schedule 2-hour focus blocks for deep work. Turn off notifications
              during these periods.
            </p>
          </div>
          <div className='rounded-lg bg-muted p-4'>
            <h4 className='font-medium'>⏰ Time Boxing</h4>
            <p className='text-sm text-muted-foreground'>
              Set strict time limits for each task to maintain momentum and
              avoid perfectionism.
            </p>
          </div>
          <div className='rounded-lg bg-muted p-4'>
            <h4 className='font-medium'>🔄 Pomodoro Technique</h4>
            <p className='text-sm text-muted-foreground'>
              Work in 25-minute focused sprints followed by 5-minute breaks.
            </p>
          </div>
          <div className='rounded-lg bg-muted p-4'>
            <h4 className='font-medium'>📊 Track Progress</h4>
            <p className='text-sm text-muted-foreground'>
              Log your completed tasks to build momentum and track productivity
              patterns.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default Schedule;
