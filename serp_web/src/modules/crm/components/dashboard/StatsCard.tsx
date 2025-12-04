/**
 * StatsCard Component - Modern metrics card with glassmorphism effect
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM Dashboard metrics card
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import { Card, Skeleton } from '@/shared/components/ui';
import {
  TrendingUp,
  TrendingDown,
  Users,
  Target,
  DollarSign,
  BarChart3,
  type LucideIcon,
} from 'lucide-react';

export interface StatsCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: LucideIcon;
  trend?: {
    value: number;
    isPositive: boolean;
    label?: string;
  };
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger';
  isLoading?: boolean;
  className?: string;
}

const variantStyles = {
  default: {
    card: 'bg-card hover:bg-card/90 border-border',
    icon: 'bg-muted text-muted-foreground',
    iconRing: '',
  },
  primary: {
    card: 'bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/30 dark:to-blue-900/20 border-blue-200/50 dark:border-blue-800/30',
    icon: 'bg-blue-500 text-white shadow-blue-500/25',
    iconRing: 'ring-4 ring-blue-500/10',
  },
  success: {
    card: 'bg-gradient-to-br from-emerald-50 to-emerald-100/50 dark:from-emerald-950/30 dark:to-emerald-900/20 border-emerald-200/50 dark:border-emerald-800/30',
    icon: 'bg-emerald-500 text-white shadow-emerald-500/25',
    iconRing: 'ring-4 ring-emerald-500/10',
  },
  warning: {
    card: 'bg-gradient-to-br from-amber-50 to-amber-100/50 dark:from-amber-950/30 dark:to-amber-900/20 border-amber-200/50 dark:border-amber-800/30',
    icon: 'bg-amber-500 text-white shadow-amber-500/25',
    iconRing: 'ring-4 ring-amber-500/10',
  },
  danger: {
    card: 'bg-gradient-to-br from-rose-50 to-rose-100/50 dark:from-rose-950/30 dark:to-rose-900/20 border-rose-200/50 dark:border-rose-800/30',
    icon: 'bg-rose-500 text-white shadow-rose-500/25',
    iconRing: 'ring-4 ring-rose-500/10',
  },
};

const formatValue = (value: string | number): string => {
  if (typeof value === 'number') {
    if (value >= 1000000) {
      return `${(value / 1000000).toFixed(1)}M`;
    } else if (value >= 1000) {
      return `${(value / 1000).toFixed(1)}K`;
    }
    return value.toLocaleString();
  }
  return value;
};

export const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  subtitle,
  icon: Icon = BarChart3,
  trend,
  variant = 'default',
  isLoading = false,
  className,
}) => {
  const styles = variantStyles[variant];

  if (isLoading) {
    return (
      <Card
        className={cn(
          'relative overflow-hidden p-5 shadow-sm',
          styles.card,
          className
        )}
      >
        <div className='flex items-start justify-between gap-4'>
          <div className='space-y-3 flex-1'>
            <Skeleton className='h-4 w-24' />
            <Skeleton className='h-8 w-32' />
            <Skeleton className='h-3 w-20' />
          </div>
          <Skeleton className='h-12 w-12 rounded-xl' />
        </div>
      </Card>
    );
  }

  return (
    <Card
      className={cn(
        'group relative overflow-hidden p-5 shadow-sm transition-all duration-200 hover:shadow-md cursor-pointer',
        styles.card,
        className
      )}
    >
      {/* Background Pattern */}
      <div className='absolute inset-0 opacity-5 dark:opacity-10'>
        <svg
          className='absolute -right-8 -top-8 h-32 w-32 text-current'
          viewBox='0 0 100 100'
        >
          <circle
            cx='50'
            cy='50'
            r='40'
            fill='currentColor'
            fillOpacity='0.3'
          />
        </svg>
      </div>

      <div className='relative flex items-start justify-between gap-4'>
        {/* Content */}
        <div className='space-y-1 min-w-0 flex-1'>
          <p className='text-sm font-medium text-muted-foreground truncate'>
            {title}
          </p>
          <p className='text-2xl font-bold tracking-tight truncate'>
            {formatValue(value)}
          </p>

          {/* Trend Indicator */}
          {trend && (
            <div className='flex items-center gap-1.5 pt-1'>
              <span
                className={cn(
                  'inline-flex items-center gap-0.5 text-xs font-medium',
                  trend.isPositive
                    ? 'text-emerald-600 dark:text-emerald-400'
                    : 'text-rose-600 dark:text-rose-400'
                )}
              >
                {trend.isPositive ? (
                  <TrendingUp className='h-3 w-3' />
                ) : (
                  <TrendingDown className='h-3 w-3' />
                )}
                {Math.abs(trend.value)}%
              </span>
              {trend.label && (
                <span className='text-xs text-muted-foreground'>
                  {trend.label}
                </span>
              )}
            </div>
          )}

          {subtitle && !trend && (
            <p className='text-xs text-muted-foreground pt-1'>{subtitle}</p>
          )}
        </div>

        {/* Icon */}
        <div
          className={cn(
            'flex h-12 w-12 shrink-0 items-center justify-center rounded-xl shadow-lg transition-transform duration-200 group-hover:scale-105',
            styles.icon,
            styles.iconRing
          )}
        >
          <Icon className='h-6 w-6' />
        </div>
      </div>
    </Card>
  );
};

// Pre-configured stats cards for CRM
export const TotalCustomersStats: React.FC<{
  value: number;
  trend?: StatsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <StatsCard
    title='Total Customers'
    value={value}
    icon={Users}
    trend={trend}
    variant='primary'
    isLoading={isLoading}
  />
);

export const ActiveLeadsStats: React.FC<{
  value: number;
  trend?: StatsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <StatsCard
    title='Active Leads'
    value={value}
    icon={Target}
    trend={trend}
    variant='warning'
    isLoading={isLoading}
  />
);

export const RevenueStats: React.FC<{
  value: number;
  trend?: StatsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <StatsCard
    title='Revenue'
    value={`$${value.toLocaleString()}`}
    icon={DollarSign}
    trend={trend}
    variant='success'
    isLoading={isLoading}
  />
);

export const ConversionRateStats: React.FC<{
  value: number;
  trend?: StatsCardProps['trend'];
  isLoading?: boolean;
}> = ({ value, trend, isLoading }) => (
  <StatsCard
    title='Conversion Rate'
    value={`${value}%`}
    icon={BarChart3}
    trend={trend}
    variant='default'
    isLoading={isLoading}
  />
);
