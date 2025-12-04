'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Lead Source Distribution Chart
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

export interface SourceData {
  source: string;
  count: number;
  value: number;
  conversionRate: number;
  color: string;
}

export interface LeadSourceChartProps {
  data: SourceData[];
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

export function LeadSourceChart({
  data,
  title = 'Phân bố nguồn Lead',
  description = 'Phân tích nguồn gốc và hiệu quả',
  className,
}: LeadSourceChartProps) {
  const totalCount = data.reduce((sum, d) => sum + d.count, 0);
  const totalValue = data.reduce((sum, d) => sum + d.value, 0);
  const sortedData = [...data].sort((a, b) => b.count - a.count);

  // Calculate donut segments
  let cumulativeAngle = 0;
  const donutSegments = sortedData.map((item) => {
    const angle = (item.count / totalCount) * 360;
    const startAngle = cumulativeAngle;
    cumulativeAngle += angle;
    return { ...item, startAngle, angle };
  });

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className='flex items-start gap-6'>
          {/* Donut Chart */}
          <div className='relative w-40 h-40 flex-shrink-0'>
            <svg viewBox='0 0 100 100' className='w-full h-full -rotate-90'>
              {donutSegments.map((segment, index) => {
                const radius = 40;
                const circumference = 2 * Math.PI * radius;
                const strokeDasharray = (segment.angle / 360) * circumference;
                const strokeDashoffset =
                  -(segment.startAngle / 360) * circumference;

                return (
                  <circle
                    key={index}
                    cx='50'
                    cy='50'
                    r={radius}
                    fill='none'
                    stroke={segment.color}
                    strokeWidth='12'
                    strokeDasharray={`${strokeDasharray} ${circumference}`}
                    strokeDashoffset={strokeDashoffset}
                    className='transition-all duration-500'
                  />
                );
              })}
            </svg>
            <div className='absolute inset-0 flex flex-col items-center justify-center'>
              <span className='text-2xl font-bold text-foreground'>
                {totalCount}
              </span>
              <span className='text-xs text-muted-foreground'>Leads</span>
            </div>
          </div>

          {/* Legend */}
          <div className='flex-1 space-y-2'>
            {sortedData.map((item, index) => {
              const percentage = ((item.count / totalCount) * 100).toFixed(1);

              return (
                <div key={index} className='flex items-center gap-3'>
                  <div
                    className='w-3 h-3 rounded-full flex-shrink-0'
                    style={{ backgroundColor: item.color }}
                  />
                  <div className='flex-1 min-w-0'>
                    <div className='flex items-center justify-between'>
                      <span className='text-sm font-medium text-foreground truncate'>
                        {item.source}
                      </span>
                      <span className='text-sm text-muted-foreground ml-2'>
                        {item.count}
                      </span>
                    </div>
                    <div className='flex items-center justify-between text-xs text-muted-foreground'>
                      <span>{percentage}%</span>
                      <span className='text-green-600 dark:text-green-400'>
                        CV: {item.conversionRate}%
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Summary */}
        <div className='mt-6 pt-4 border-t border-border grid grid-cols-3 gap-4 text-center'>
          <div>
            <div className='text-lg font-bold text-foreground'>
              {totalCount}
            </div>
            <div className='text-xs text-muted-foreground'>Tổng Lead</div>
          </div>
          <div>
            <div className='text-lg font-bold text-foreground'>
              {formatCurrency(totalValue)}
            </div>
            <div className='text-xs text-muted-foreground'>Tổng giá trị</div>
          </div>
          <div>
            <div className='text-lg font-bold text-foreground'>
              {(
                data.reduce((sum, d) => sum + d.conversionRate, 0) / data.length
              ).toFixed(1)}
              %
            </div>
            <div className='text-xs text-muted-foreground'>TB chuyển đổi</div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
