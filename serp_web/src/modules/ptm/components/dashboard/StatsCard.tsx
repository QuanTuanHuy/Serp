/**
 * PTM v2 - Stats Card Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Dashboard stats card
 */

'use client';

import { LucideIcon } from 'lucide-react';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { cn } from '@/shared/utils';

interface StatsCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  trend?: {
    value: number;
    label: string;
    isPositive: boolean;
  };
  color?: 'blue' | 'green' | 'purple' | 'amber' | 'red';
  isLoading?: boolean;
  className?: string;
}

const colorConfig = {
  blue: {
    bg: 'bg-blue-50 dark:bg-blue-950/30',
    icon: 'text-blue-600 dark:text-blue-400',
    iconBg: 'bg-blue-100 dark:bg-blue-900/50',
    trend: 'text-blue-600 dark:text-blue-400',
  },
  green: {
    bg: 'bg-green-50 dark:bg-green-950/30',
    icon: 'text-green-600 dark:text-green-400',
    iconBg: 'bg-green-100 dark:bg-green-900/50',
    trend: 'text-green-600 dark:text-green-400',
  },
  purple: {
    bg: 'bg-purple-50 dark:bg-purple-950/30',
    icon: 'text-purple-600 dark:text-purple-400',
    iconBg: 'bg-purple-100 dark:bg-purple-900/50',
    trend: 'text-purple-600 dark:text-purple-400',
  },
  amber: {
    bg: 'bg-amber-50 dark:bg-amber-950/30',
    icon: 'text-amber-600 dark:text-amber-400',
    iconBg: 'bg-amber-100 dark:bg-amber-900/50',
    trend: 'text-amber-600 dark:text-amber-400',
  },
  red: {
    bg: 'bg-red-50 dark:bg-red-950/30',
    icon: 'text-red-600 dark:text-red-400',
    iconBg: 'bg-red-100 dark:bg-red-900/50',
    trend: 'text-red-600 dark:text-red-400',
  },
};

export function StatsCard({
  title,
  value,
  subtitle,
  icon: Icon,
  trend,
  color = 'blue',
  isLoading = false,
  className,
}: StatsCardProps) {
  const config = colorConfig[color];

  if (isLoading) {
    return (
      <Card className={cn('', className)}>
        <CardHeader className='flex flex-row items-center justify-between pb-2'>
          <Skeleton className='h-4 w-24' />
          <Skeleton className='h-10 w-10 rounded-lg' />
        </CardHeader>
        <CardContent>
          <Skeleton className='h-8 w-16 mb-2' />
          <Skeleton className='h-3 w-32' />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={cn('hover:shadow-md transition-shadow', className)}>
      <CardHeader className='flex flex-row items-center justify-between pb-2'>
        <h3 className='text-sm font-medium text-muted-foreground'>{title}</h3>
        <div className={cn('p-2.5 rounded-lg', config.iconBg)}>
          <Icon className={cn('h-5 w-5', config.icon)} />
        </div>
      </CardHeader>

      <CardContent>
        <div className='space-y-1'>
          <div className='text-3xl font-bold tracking-tight'>{value}</div>

          {subtitle && (
            <p className='text-xs text-muted-foreground'>{subtitle}</p>
          )}

          {trend && (
            <div className='flex items-center gap-1 text-xs mt-2'>
              <span
                className={cn(
                  'font-medium',
                  trend.isPositive
                    ? 'text-green-600 dark:text-green-400'
                    : 'text-red-600 dark:text-red-400'
                )}
              >
                {trend.isPositive ? '+' : ''}
                {trend.value}%
              </span>
              <span className='text-muted-foreground'>{trend.label}</span>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

// Skeleton for loading state
export function StatsCardSkeleton({ className }: { className?: string }) {
  return (
    <Card className={cn('', className)}>
      <CardHeader className='flex flex-row items-center justify-between pb-2'>
        <Skeleton className='h-4 w-24' />
        <Skeleton className='h-10 w-10 rounded-lg' />
      </CardHeader>
      <CardContent>
        <Skeleton className='h-8 w-16 mb-2' />
        <Skeleton className='h-3 w-32' />
      </CardContent>
    </Card>
  );
}
