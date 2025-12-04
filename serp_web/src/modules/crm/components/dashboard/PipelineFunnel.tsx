/**
 * PipelineFunnel Component - Visual sales pipeline funnel
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM Dashboard pipeline visualization
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  Skeleton,
} from '@/shared/components/ui';
import { ArrowRight, TrendingUp } from 'lucide-react';

export interface PipelineStage {
  name: string;
  count: number;
  value: number;
  conversionRate?: number;
  color: string;
}

export interface PipelineFunnelProps {
  stages: PipelineStage[];
  title?: string;
  subtitle?: string;
  isLoading?: boolean;
  className?: string;
  onStageClick?: (stage: PipelineStage) => void;
}

const stageColors: Record<
  string,
  { bg: string; border: string; text: string }
> = {
  blue: {
    bg: 'bg-blue-500',
    border: 'border-blue-500/30',
    text: 'text-blue-600 dark:text-blue-400',
  },
  indigo: {
    bg: 'bg-indigo-500',
    border: 'border-indigo-500/30',
    text: 'text-indigo-600 dark:text-indigo-400',
  },
  yellow: {
    bg: 'bg-amber-500',
    border: 'border-amber-500/30',
    text: 'text-amber-600 dark:text-amber-400',
  },
  orange: {
    bg: 'bg-orange-500',
    border: 'border-orange-500/30',
    text: 'text-orange-600 dark:text-orange-400',
  },
  purple: {
    bg: 'bg-purple-500',
    border: 'border-purple-500/30',
    text: 'text-purple-600 dark:text-purple-400',
  },
  green: {
    bg: 'bg-emerald-500',
    border: 'border-emerald-500/30',
    text: 'text-emerald-600 dark:text-emerald-400',
  },
  red: {
    bg: 'bg-rose-500',
    border: 'border-rose-500/30',
    text: 'text-rose-600 dark:text-rose-400',
  },
};

const formatCurrency = (value: number): string => {
  if (value >= 1000000) {
    return `$${(value / 1000000).toFixed(1)}M`;
  } else if (value >= 1000) {
    return `$${(value / 1000).toFixed(0)}K`;
  }
  return `$${value.toLocaleString()}`;
};

export const PipelineFunnel: React.FC<PipelineFunnelProps> = ({
  stages,
  title = 'Sales Pipeline',
  subtitle,
  isLoading = false,
  className,
  onStageClick,
}) => {
  const maxCount = Math.max(...stages.map((s) => s.count));
  const totalValue = stages.reduce((sum, s) => sum + s.value, 0);

  if (isLoading) {
    return (
      <Card className={className}>
        <CardHeader>
          <Skeleton className='h-5 w-32 mb-2' />
          <Skeleton className='h-4 w-48' />
        </CardHeader>
        <CardContent className='space-y-3'>
          {[1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className='h-14 rounded-lg' />
          ))}
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className='text-lg font-semibold'>{title}</CardTitle>
        {subtitle && <CardDescription>{subtitle}</CardDescription>}
      </CardHeader>

      <CardContent className='space-y-3'>
        {stages.map((stage, index) => {
          const colors = stageColors[stage.color] || stageColors.blue;
          const widthPercentage =
            maxCount > 0 ? (stage.count / maxCount) * 100 : 0;
          const isLast = index === stages.length - 1;

          return (
            <div key={stage.name}>
              {/* Stage Bar */}
              <div
                className={cn(
                  'group relative rounded-lg border p-4 transition-all duration-200',
                  'hover:shadow-md cursor-pointer',
                  colors.border,
                  'bg-gradient-to-r from-background to-muted/30'
                )}
                onClick={() => onStageClick?.(stage)}
              >
                {/* Progress Indicator */}
                <div
                  className={cn(
                    'absolute left-0 top-0 bottom-0 rounded-l-lg opacity-10 transition-all',
                    colors.bg
                  )}
                  style={{ width: `${Math.max(widthPercentage, 5)}%` }}
                />

                <div className='relative flex items-center justify-between'>
                  {/* Stage Info */}
                  <div className='flex items-center gap-3'>
                    <div
                      className={cn(
                        'h-2.5 w-2.5 rounded-full shadow-sm',
                        colors.bg
                      )}
                    />
                    <div>
                      <p className='font-medium text-foreground'>
                        {stage.name}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        {stage.count} deal{stage.count !== 1 ? 's' : ''}
                      </p>
                    </div>
                  </div>

                  {/* Value & Conversion */}
                  <div className='flex items-center gap-4'>
                    {stage.conversionRate !== undefined && !isLast && (
                      <div className='hidden sm:flex items-center gap-1 text-xs text-muted-foreground'>
                        <TrendingUp className='h-3 w-3' />
                        <span>{stage.conversionRate}%</span>
                      </div>
                    )}
                    <div className='text-right'>
                      <p className={cn('font-semibold', colors.text)}>
                        {formatCurrency(stage.value)}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        {totalValue > 0
                          ? `${((stage.value / totalValue) * 100).toFixed(0)}%`
                          : '0%'}{' '}
                        of total
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Conversion Arrow */}
              {!isLast && (
                <div className='flex justify-center py-1'>
                  <ArrowRight className='h-4 w-4 text-muted-foreground/50 rotate-90' />
                </div>
              )}
            </div>
          );
        })}

        {/* Total Summary */}
        <div className='mt-6 pt-4 border-t flex items-center justify-between'>
          <span className='text-sm text-muted-foreground'>
            Total Pipeline Value
          </span>
          <span className='text-xl font-bold text-foreground'>
            {formatCurrency(totalValue)}
          </span>
        </div>
      </CardContent>
    </Card>
  );
};

// Default pipeline stages for CRM
export const DEFAULT_PIPELINE_STAGES: PipelineStage[] = [
  {
    name: 'Prospecting',
    count: 12,
    value: 45000,
    conversionRate: 75,
    color: 'blue',
  },
  {
    name: 'Qualification',
    count: 8,
    value: 32000,
    conversionRate: 62,
    color: 'indigo',
  },
  {
    name: 'Proposal',
    count: 6,
    value: 28000,
    conversionRate: 50,
    color: 'yellow',
  },
  {
    name: 'Negotiation',
    count: 4,
    value: 18000,
    conversionRate: 80,
    color: 'orange',
  },
  { name: 'Closed Won', count: 4, value: 22000, color: 'green' },
];
