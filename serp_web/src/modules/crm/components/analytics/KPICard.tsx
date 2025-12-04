'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM KPI Card Component
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { TrendingUp, TrendingDown, Minus, LucideIcon } from 'lucide-react';
import { cn } from '@/shared';

export interface KPICardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  trend?: {
    value: number;
    label?: string;
  };
  icon?: LucideIcon;
  iconColor?: string;
  className?: string;
}

export function KPICard({
  title,
  value,
  subtitle,
  trend,
  icon: Icon,
  iconColor = 'text-primary',
  className,
}: KPICardProps) {
  const getTrendIcon = () => {
    if (!trend) return null;
    if (trend.value > 0) return <TrendingUp className='h-3 w-3' />;
    if (trend.value < 0) return <TrendingDown className='h-3 w-3' />;
    return <Minus className='h-3 w-3' />;
  };

  const getTrendColor = () => {
    if (!trend) return '';
    if (trend.value > 0) return 'text-green-600 dark:text-green-400';
    if (trend.value < 0) return 'text-red-600 dark:text-red-400';
    return 'text-muted-foreground';
  };

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
        <CardTitle className='text-sm font-medium text-muted-foreground'>
          {title}
        </CardTitle>
        {Icon && (
          <div className={cn('rounded-md bg-muted p-2', iconColor)}>
            <Icon className='h-4 w-4' />
          </div>
        )}
      </CardHeader>
      <CardContent>
        <div className='text-2xl font-bold text-foreground'>{value}</div>
        <div className='flex items-center justify-between mt-1'>
          {subtitle && (
            <p className='text-xs text-muted-foreground'>{subtitle}</p>
          )}
          {trend && (
            <div
              className={cn('flex items-center gap-1 text-xs', getTrendColor())}
            >
              {getTrendIcon()}
              <span>{Math.abs(trend.value)}%</span>
              {trend.label && (
                <span className='text-muted-foreground ml-1'>
                  {trend.label}
                </span>
              )}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
