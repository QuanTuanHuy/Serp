/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM Calendar Chips
 */

'use client';

import React from 'react';
import { Phone, Mail, Video, CheckSquare } from 'lucide-react';
import { cn } from '@/shared/utils';
import type { Activity } from '../../types';

interface CRMCalendarChipProps {
  activity: Activity;
  onClick: () => void;
}

const iconMap: Record<string, React.ElementType> = {
  CALL: Phone,
  EMAIL: Mail,
  MEETING: Video,
  TASK: CheckSquare,
};

const colorClasses: Record<string, string> = {
  CALL: 'border-blue-500/20 bg-blue-500/10 text-blue-700 hover:bg-blue-500/15 dark:bg-blue-950/40 dark:text-blue-300 dark:border-blue-900',
  MEETING:
    'border-emerald-500/20 bg-emerald-500/10 text-emerald-700 hover:bg-emerald-500/15 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-900',
  EMAIL:
    'border-purple-500/20 bg-purple-500/10 text-purple-700 hover:bg-purple-500/15 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-900',
  TASK: 'border-amber-500/20 bg-amber-500/10 text-amber-700 hover:bg-amber-500/15 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-900',
};

export const CRMCalendarChip: React.FC<CRMCalendarChipProps> = React.memo(
  ({ activity, onClick }) => {
    const Icon = iconMap[activity.type] || CheckSquare;
    const colors = colorClasses[activity.type] || colorClasses.TASK;
    const startMoment = activity.scheduledDate
      ? new Date(activity.scheduledDate)
      : null;
    const timeLabel = startMoment
      ? startMoment.toLocaleTimeString('vi-VN', {
          hour: '2-digit',
          minute: '2-digit',
        })
      : '';

    return (
      <button
        type='button'
        onClick={(e) => {
          e.stopPropagation();
          onClick();
        }}
        className={cn(
          'group w-full rounded border px-2 py-1 text-left text-xs shadow-sm transition-colors flex flex-col gap-0.5 cursor-pointer',
          colors,
          activity.status === 'COMPLETED' ? 'font-medium' : 'border-dashed'
        )}
      >
        <div className='flex items-center gap-1 w-full min-w-0'>
          <Icon className='h-3.5 w-3.5 shrink-0' />
          <span className='truncate flex-1 font-semibold text-[11px]'>
            {activity.subject}
          </span>
        </div>
        <div className='flex items-center justify-between text-[9px] opacity-80 mt-0.5 w-full'>
          <span>{timeLabel}</span>
          <span className='truncate max-w-[60px]'>
            {activity.assignedToName || ''}
          </span>
        </div>
      </button>
    );
  }
);

CRMCalendarChip.displayName = 'CRMCalendarChip';
