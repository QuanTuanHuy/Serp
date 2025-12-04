'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity Timeline Chart Component
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '@/shared/components/ui/card';
import {
  Phone,
  Mail,
  Calendar,
  FileText,
  CheckCircle,
  Clock,
  Users,
  Video,
} from 'lucide-react';
import { cn } from '@/shared';

export interface ActivityData {
  date: string;
  calls: number;
  emails: number;
  meetings: number;
  tasks: number;
}

export interface ActivityTimelineProps {
  data: ActivityData[];
  title?: string;
  description?: string;
  className?: string;
}

const ACTIVITY_TYPES = [
  { key: 'calls', label: 'Cuộc gọi', color: 'bg-blue-500', icon: Phone },
  { key: 'emails', label: 'Email', color: 'bg-green-500', icon: Mail },
  { key: 'meetings', label: 'Họp', color: 'bg-purple-500', icon: Calendar },
  {
    key: 'tasks',
    label: 'Công việc',
    color: 'bg-orange-500',
    icon: CheckCircle,
  },
] as const;

export function ActivityTimeline({
  data,
  title = 'Hoạt động theo thời gian',
  description = '7 ngày gần đây',
  className,
}: ActivityTimelineProps) {
  const maxValue = Math.max(
    ...data.flatMap((d) => [d.calls, d.emails, d.meetings, d.tasks]),
    1
  );

  const totals = data.reduce(
    (acc, d) => ({
      calls: acc.calls + d.calls,
      emails: acc.emails + d.emails,
      meetings: acc.meetings + d.meetings,
      tasks: acc.tasks + d.tasks,
    }),
    { calls: 0, emails: 0, meetings: 0, tasks: 0 }
  );

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        {/* Legend */}
        <div className='flex flex-wrap gap-4 mb-4'>
          {ACTIVITY_TYPES.map((type) => {
            const Icon = type.icon;
            return (
              <div key={type.key} className='flex items-center gap-2 text-sm'>
                <div className={cn('w-3 h-3 rounded-full', type.color)} />
                <Icon className='h-3.5 w-3.5 text-muted-foreground' />
                <span className='text-muted-foreground'>{type.label}</span>
                <span className='font-medium text-foreground'>
                  {totals[type.key]}
                </span>
              </div>
            );
          })}
        </div>

        {/* Chart */}
        <div className='space-y-2'>
          {data.map((day, index) => {
            const dayDate = new Date(day.date);
            const dayName = dayDate.toLocaleDateString('vi-VN', {
              weekday: 'short',
            });
            const dayNum = dayDate.getDate();

            return (
              <div key={index} className='flex items-center gap-3'>
                <div className='w-12 text-right'>
                  <div className='text-xs font-medium text-foreground'>
                    {dayName}
                  </div>
                  <div className='text-xs text-muted-foreground'>{dayNum}</div>
                </div>
                <div className='flex-1 flex gap-1'>
                  {ACTIVITY_TYPES.map((type) => {
                    const value = day[type.key];
                    const width = Math.max((value / maxValue) * 100, 0);

                    return (
                      <div
                        key={type.key}
                        className='flex-1 h-6 bg-muted rounded overflow-hidden'
                        title={`${type.label}: ${value}`}
                      >
                        <div
                          className={cn(
                            'h-full rounded transition-all duration-300',
                            type.color
                          )}
                          style={{ width: `${width}%` }}
                        />
                      </div>
                    );
                  })}
                </div>
                <div className='w-8 text-right text-xs text-muted-foreground'>
                  {day.calls + day.emails + day.meetings + day.tasks}
                </div>
              </div>
            );
          })}
        </div>

        {/* Summary stats */}
        <div className='mt-6 pt-4 border-t border-border grid grid-cols-4 gap-4'>
          {ACTIVITY_TYPES.map((type) => {
            const Icon = type.icon;
            const avgPerDay = (totals[type.key] / data.length).toFixed(1);

            return (
              <div key={type.key} className='text-center'>
                <div
                  className={cn(
                    'inline-flex p-2 rounded-lg mb-2',
                    type.color,
                    'bg-opacity-10'
                  )}
                >
                  <Icon
                    className={cn(
                      'h-4 w-4',
                      type.color.replace('bg-', 'text-')
                    )}
                  />
                </div>
                <div className='text-lg font-bold text-foreground'>
                  {totals[type.key]}
                </div>
                <div className='text-xs text-muted-foreground'>
                  Ø {avgPerDay}/ngày
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
