/**
 * PTM Schedule Page - Modern Calendar with Drag & Drop
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Interactive calendar scheduling
 */

'use client';

import React, { useState } from 'react';
import { CalendarView } from '@/modules/ptmv2/components/schedule';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import {
  Calendar,
  Plus,
  Sparkles,
  Clock,
  Lock,
  List,
  Settings,
} from 'lucide-react';

const Schedule: React.FC = () => {
  const [showQuickAdd, setShowQuickAdd] = useState(false);

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div className='flex items-center gap-3'>
          <div className='p-2 rounded-lg bg-gradient-to-br from-purple-500 to-blue-500'>
            <Calendar className='h-6 w-6 text-white' />
          </div>
          <div>
            <h1 className='text-2xl font-bold'>Schedule</h1>
            <p className='text-sm text-muted-foreground'>
              Drag & drop to organize your time
            </p>
          </div>
        </div>

        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm'>
            <List className='h-4 w-4 mr-2' />
            Agenda
          </Button>
          <Button variant='outline' size='sm'>
            <Sparkles className='h-4 w-4 mr-2' />
            AI Optimize
          </Button>
          <Button onClick={() => setShowQuickAdd(true)} size='sm'>
            <Plus className='h-4 w-4 mr-2' />
            Add Event
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className='grid grid-cols-1 gap-4 md:grid-cols-3'>
        <Card className='p-4 bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-950/20 dark:to-purple-900/20 border-purple-200 dark:border-purple-800'>
          <div className='flex items-center gap-3'>
            <div className='p-2 rounded-lg bg-purple-500/10'>
              <Lock className='h-5 w-5 text-purple-600 dark:text-purple-400' />
            </div>
            <div>
              <p className='text-sm text-muted-foreground'>Focus Time Today</p>
              <p className='text-2xl font-bold text-purple-600 dark:text-purple-400'>
                4.5h
              </p>
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-950/20 dark:to-blue-900/20 border-blue-200 dark:border-blue-800'>
          <div className='flex items-center gap-3'>
            <div className='p-2 rounded-lg bg-blue-500/10'>
              <Clock className='h-5 w-5 text-blue-600 dark:text-blue-400' />
            </div>
            <div>
              <p className='text-sm text-muted-foreground'>Events Today</p>
              <p className='text-2xl font-bold text-blue-600 dark:text-blue-400'>
                7
              </p>
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-amber-50 to-amber-100 dark:from-amber-950/20 dark:to-amber-900/20 border-amber-200 dark:border-amber-800'>
          <div className='flex items-center gap-3'>
            <div className='p-2 rounded-lg bg-amber-500/10'>
              <Sparkles className='h-5 w-5 text-amber-600 dark:text-amber-400' />
            </div>
            <div>
              <p className='text-sm text-muted-foreground'>Utilization</p>
              <p className='text-2xl font-bold text-amber-600 dark:text-amber-400'>
                87%
              </p>
            </div>
          </div>
        </Card>
      </div>

      {/* Calendar */}
      <CalendarView />

      {/* Quick Tips */}
      <Card className='p-4 bg-gradient-to-r from-blue-50 to-purple-50 dark:from-blue-950/10 dark:to-purple-950/10 border-blue-200 dark:border-blue-800'>
        <div className='flex items-start gap-3'>
          <Sparkles className='h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0' />
          <div className='space-y-1'>
            <p className='font-semibold text-sm text-blue-900 dark:text-blue-100'>
              💡 Pro Tips
            </p>
            <ul className='text-sm text-blue-700 dark:text-blue-300 space-y-1'>
              <li>• Drag events to reschedule instantly</li>
              <li>• Resize events to adjust duration</li>
              <li>• Click "AI Optimize" to auto-schedule unassigned tasks</li>
              <li>• Purple blocks are protected focus time</li>
            </ul>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default Schedule;
