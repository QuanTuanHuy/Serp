'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Pipeline Funnel Chart Component
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '@/shared/components/ui/card';
import { cn } from '@/shared';

export interface FunnelStage {
  name: string;
  count: number;
  value: number;
  color: string;
  conversionRate?: number;
}

export interface PipelineFunnelProps {
  stages: FunnelStage[];
  title?: string;
  description?: string;
  className?: string;
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(value);
}

export function PipelineFunnel({
  stages,
  title = 'Pipeline bán hàng',
  description = 'Tổng quan các giai đoạn trong pipeline',
  className,
}: PipelineFunnelProps) {
  const maxCount = Math.max(...stages.map((s) => s.count), 1);
  const totalValue = stages.reduce((sum, s) => sum + s.value, 0);

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className='space-y-3'>
          {stages.map((stage, index) => {
            const widthPercent = Math.max((stage.count / maxCount) * 100, 10);
            const valuePercent =
              totalValue > 0
                ? ((stage.value / totalValue) * 100).toFixed(1)
                : 0;

            return (
              <div key={stage.name} className='space-y-1'>
                <div className='flex items-center justify-between text-sm'>
                  <div className='flex items-center gap-2'>
                    <div
                      className='w-3 h-3 rounded-full'
                      style={{ backgroundColor: stage.color }}
                    />
                    <span className='font-medium text-foreground'>
                      {stage.name}
                    </span>
                    <span className='text-muted-foreground'>
                      ({stage.count})
                    </span>
                  </div>
                  <div className='flex items-center gap-3 text-right'>
                    <span className='text-foreground font-medium'>
                      {formatCurrency(stage.value)}
                    </span>
                    <span className='text-muted-foreground text-xs'>
                      {valuePercent}%
                    </span>
                  </div>
                </div>
                <div className='relative'>
                  <div className='h-8 w-full bg-muted rounded-md overflow-hidden'>
                    <div
                      className='h-full rounded-md transition-all duration-500 ease-out flex items-center justify-end pr-2'
                      style={{
                        width: `${widthPercent}%`,
                        backgroundColor: stage.color,
                      }}
                    >
                      {stage.conversionRate !== undefined &&
                        widthPercent > 20 && (
                          <span className='text-xs text-white font-medium'>
                            {stage.conversionRate}%
                          </span>
                        )}
                    </div>
                  </div>
                  {index < stages.length - 1 && (
                    <div className='absolute -bottom-2 left-1/2 transform -translate-x-1/2 text-muted-foreground'>
                      <svg
                        className='w-4 h-4'
                        viewBox='0 0 24 24'
                        fill='none'
                        stroke='currentColor'
                      >
                        <path
                          strokeLinecap='round'
                          strokeLinejoin='round'
                          strokeWidth={2}
                          d='M19 14l-7 7m0 0l-7-7m7 7V3'
                        />
                      </svg>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Summary */}
        <div className='mt-6 pt-4 border-t border-border'>
          <div className='flex items-center justify-between'>
            <span className='text-sm text-muted-foreground'>
              Tổng giá trị pipeline
            </span>
            <span className='text-lg font-bold text-foreground'>
              {formatCurrency(totalValue)}
            </span>
          </div>
          <div className='flex items-center justify-between mt-1'>
            <span className='text-sm text-muted-foreground'>Tổng cơ hội</span>
            <span className='text-sm font-medium text-foreground'>
              {stages.reduce((sum, s) => sum + s.count, 0)} cơ hội
            </span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
