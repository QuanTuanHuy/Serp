/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings stats card component
 */

'use client';

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { ArrowUp, ArrowDown, Minus } from 'lucide-react';
import { cn } from '@/shared/utils';

export interface SettingsStatsCardProps {
  title: string;
  value: string | number;
  description?: string;
  icon?: React.ReactNode;
  trend?: {
    value: number;
    label?: string;
  };
  className?: string;
}

export const SettingsStatsCard: React.FC<SettingsStatsCardProps> = ({
  title,
  value,
  description,
  icon,
  trend,
  className,
}) => {
  const getTrendColor = () => {
    if (!trend) return '';
    if (trend.value > 0) return 'text-green-600 dark:text-green-500';
    if (trend.value < 0) return 'text-red-600 dark:text-red-500';
    return 'text-muted-foreground';
  };

  const getTrendIcon = () => {
    if (!trend) return null;
    if (trend.value > 0) return <ArrowUp className='h-3 w-3' />;
    if (trend.value < 0) return <ArrowDown className='h-3 w-3' />;
    return <Minus className='h-3 w-3' />;
  };

  return (
    <Card className={className}>
      <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
        <CardTitle className='text-xs sm:text-sm font-medium truncate pr-2'>
          {title}
        </CardTitle>
        {icon && (
          <div className='text-muted-foreground flex-shrink-0'>{icon}</div>
        )}
      </CardHeader>
      <CardContent className='pt-0'>
        <div className='text-xl sm:text-2xl font-bold'>{value}</div>

        {(description || trend) && (
          <div className='flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-2 mt-1'>
            {trend && (
              <div
                className={cn(
                  'flex items-center gap-1 text-xs font-medium flex-shrink-0',
                  getTrendColor()
                )}
              >
                {getTrendIcon()}
                <span>{Math.abs(trend.value)}%</span>
              </div>
            )}
            {description && (
              <p className='text-xs text-muted-foreground truncate'>
                {trend && trend.label ? trend.label : description}
              </p>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
};
