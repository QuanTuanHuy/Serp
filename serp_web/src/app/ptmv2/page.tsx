/**
 * PTM v2 - Dashboard Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Main dashboard view
 */

'use client';

import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { setActiveView } from '@/modules/ptmv2/store/uiSlice';
import { DashboardSkeleton } from '@/modules/ptmv2/components/shared';

export default function PTMDashboardPage() {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(setActiveView('dashboard'));
  }, [dispatch]);

  return (
    <div className='p-6 space-y-6'>
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-2xl font-bold'>Dashboard</h1>
          <p className='text-muted-foreground'>
            Welcome back! Here's your productivity overview.
          </p>
        </div>
      </div>

      {/* Placeholder - Will be implemented in Phase 2 */}
      <DashboardSkeleton />

      <div className='mt-8 p-8 border-2 border-dashed rounded-lg text-center'>
        <h3 className='text-lg font-semibold mb-2'>Dashboard Coming Soon</h3>
        <p className='text-sm text-muted-foreground'>
          Phase 2 will implement stats cards, today's schedule, and task
          overview.
        </p>
      </div>
    </div>
  );
}
