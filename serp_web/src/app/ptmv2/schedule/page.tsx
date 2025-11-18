/**
 * PTM v2 - Schedule Demo Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Showcase calendar components
 */

'use client';

import { CalendarView } from '@/modules/ptmv2';

export default function SchedulePage() {
  return (
    <div className='container mx-auto p-6 space-y-6'>
      {/* Header */}
      <div>
        <h1 className='text-3xl font-bold'>Schedule</h1>
        <p className='text-muted-foreground mt-1'>
          View and manage your schedule
        </p>
      </div>

      {/* Calendar */}
      <CalendarView />
    </div>
  );
}
