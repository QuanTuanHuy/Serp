/**
 * PTM v2 - Tasks Demo Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Showcase task components
 */

'use client';

import { useState } from 'react';
import { QuickAddTask, TaskList } from '@/modules/ptmv2';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';

export default function TasksPage() {
  return (
    <div className='container mx-auto p-6 space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold'>Tasks</h1>
          <p className='text-muted-foreground mt-1'>
            Manage your tasks efficiently
          </p>
        </div>
        <QuickAddTask />
      </div>

      {/* Task List */}
      <TaskList />
    </div>
  );
}
