'use client';

import { CalendarCheck, Truck } from 'lucide-react';
import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { TransportPlanListItem, TransportPlanStatus } from '../types';

interface TodayRoutesCardProps {
  plans: TransportPlanListItem[];
}

const STATUS_BADGE: Record<TransportPlanStatus, string> = {
  CREATED: 'bg-blue-100 text-blue-700 border-blue-200',
  EXECUTING: 'bg-orange-100 text-orange-700 border-orange-200',
  COMPLETED: 'bg-green-100 text-green-700 border-green-200',
  CANCELLED: 'bg-red-100 text-red-700 border-red-200',
};

function isToday(dt: string | null): boolean {
  if (!dt) return false;
  const today = new Date();
  const d = new Date(dt.replace(' ', 'T'));
  return (
    d.getFullYear() === today.getFullYear() &&
    d.getMonth() === today.getMonth() &&
    d.getDate() === today.getDate()
  );
}

function formatTime(dt: string | null): string {
  if (!dt) return '—';
  return dt.replace('T', ' ').slice(0, 16);
}

export function TodayRoutesCard({ plans }: TodayRoutesCardProps) {
  const todayPlans = plans.filter((p) => isToday(p.startTime));
  const hasExecuting = todayPlans.some((p) => p.status === 'EXECUTING');

  return (
    <Card
      className={cn(
        'border-2 transition-colors',
        hasExecuting
          ? 'border-orange-400 bg-orange-50 dark:bg-orange-950/30'
          : 'border-blue-300 bg-blue-50 dark:bg-blue-950/30'
      )}
    >
      <CardHeader className='pb-3'>
        <CardTitle className='flex items-center gap-2 text-base font-semibold'>
          <CalendarCheck
            className={cn(
              'h-5 w-5',
              hasExecuting ? 'text-orange-600' : 'text-blue-600'
            )}
          />
          Today&apos;s Routes
          <span className='ml-1 rounded-full bg-white px-2 py-0.5 text-xs font-bold text-gray-700 shadow-sm dark:bg-gray-800 dark:text-gray-200'>
            {todayPlans.length}
          </span>
        </CardTitle>
      </CardHeader>

      <CardContent>
        {todayPlans.length === 0 ? (
          <p className='text-sm text-muted-foreground'>
            No routes scheduled for today.
          </p>
        ) : (
          <div className='flex flex-wrap gap-3'>
            {todayPlans.map((plan) => (
              <div
                key={plan.id}
                className='flex items-center gap-3 rounded-lg border bg-white px-4 py-2.5 shadow-sm dark:bg-gray-900'
              >
                <Truck className='h-4 w-4 flex-shrink-0 text-muted-foreground' />
                <span className='font-mono text-sm font-semibold'>
                  {plan.truckCode}
                </span>
                <Badge
                  className={cn('border text-xs', STATUS_BADGE[plan.status])}
                >
                  {plan.status}
                </Badge>
                <span className='text-xs text-muted-foreground tabular-nums'>
                  {formatTime(plan.startTime)}
                  {plan.endTime ? ` → ${formatTime(plan.endTime)}` : ''}
                </span>
                <span className='text-xs text-muted-foreground'>
                  {plan.stopCount} stops
                </span>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
