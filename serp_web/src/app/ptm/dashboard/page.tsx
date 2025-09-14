/**
 * PTM Dashboard Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Personal Task Management Dashboard
 */

'use client';

import React from 'react';
import { Card } from '@/shared/components';
import {
  LayoutDashboard,
  CheckCircle,
  Clock,
  Calendar,
  TrendingUp,
} from 'lucide-react';

const Dashboard: React.FC = () => {
  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center gap-2'>
        <LayoutDashboard className='h-6 w-6' />
        <h1 className='text-2xl font-bold'>Dashboard</h1>
      </div>

      {/* Stats Cards */}
      <div className='grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <Card className='p-6'>
          <div className='flex items-center gap-2'>
            <CheckCircle className='h-5 w-5 text-green-500' />
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Completed Tasks
              </p>
              <p className='text-2xl font-bold'>24</p>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center gap-2'>
            <Clock className='h-5 w-5 text-yellow-500' />
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Pending Tasks
              </p>
              <p className='text-2xl font-bold'>12</p>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center gap-2'>
            <Calendar className='h-5 w-5 text-blue-500' />
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Today's Events
              </p>
              <p className='text-2xl font-bold'>5</p>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center gap-2'>
            <TrendingUp className='h-5 w-5 text-purple-500' />
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Productivity
              </p>
              <p className='text-2xl font-bold'>87%</p>
            </div>
          </div>
        </Card>
      </div>

      {/* Recent Activity */}
      <div className='grid grid-cols-1 gap-4 lg:grid-cols-2'>
        <Card className='p-6'>
          <h3 className='mb-4 text-lg font-semibold'>Recent Tasks</h3>
          <div className='space-y-2'>
            <div className='flex items-center gap-2 rounded-lg border p-3'>
              <CheckCircle className='h-4 w-4 text-green-500' />
              <span className='text-sm'>Complete project documentation</span>
            </div>
            <div className='flex items-center gap-2 rounded-lg border p-3'>
              <Clock className='h-4 w-4 text-yellow-500' />
              <span className='text-sm'>Review team progress</span>
            </div>
            <div className='flex items-center gap-2 rounded-lg border p-3'>
              <Clock className='h-4 w-4 text-yellow-500' />
              <span className='text-sm'>Prepare presentation slides</span>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <h3 className='mb-4 text-lg font-semibold'>Upcoming Events</h3>
          <div className='space-y-2'>
            <div className='flex items-center gap-2 rounded-lg border p-3'>
              <Calendar className='h-4 w-4 text-blue-500' />
              <span className='text-sm'>Team meeting at 2:00 PM</span>
            </div>
            <div className='flex items-center gap-2 rounded-lg border p-3'>
              <Calendar className='h-4 w-4 text-blue-500' />
              <span className='text-sm'>Client call at 4:00 PM</span>
            </div>
            <div className='flex items-center gap-2 rounded-lg border p-3'>
              <Calendar className='h-4 w-4 text-blue-500' />
              <span className='text-sm'>Project deadline tomorrow</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
