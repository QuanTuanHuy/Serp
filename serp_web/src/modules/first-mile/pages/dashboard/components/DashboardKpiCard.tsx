/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard KPI card
 */

import Link from 'next/link';
import { ArrowDownRight, ArrowUpRight, Minus } from 'lucide-react';

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';

import type { DashboardKpiConfig } from '../dashboardPageModels';
import { formatPercent } from '../dashboardPageModels';

const toneClasses: Record<DashboardKpiConfig['tone'], string> = {
  blue: 'bg-sky-500/10 text-sky-700 dark:text-sky-300',
  green: 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
  amber: 'bg-amber-500/10 text-amber-700 dark:text-amber-300',
  rose: 'bg-rose-500/10 text-rose-700 dark:text-rose-300',
};

export function DashboardKpiCard({ item }: { item: DashboardKpiConfig }) {
  const Icon = item.icon;
  const TrendIcon =
    item.trend === undefined
      ? Minus
      : item.trend > 0
        ? ArrowUpRight
        : item.trend < 0
          ? ArrowDownRight
          : Minus;

  const content = (
    <Card className='h-full rounded-lg'>
      <CardHeader className='flex flex-row items-start justify-between space-y-0 pb-2'>
        <CardTitle className='text-sm font-medium text-muted-foreground'>
          {item.title}
        </CardTitle>
        <div
          className={cn(
            'flex h-9 w-9 items-center justify-center rounded-md',
            toneClasses[item.tone]
          )}
        >
          <Icon className='h-4 w-4' />
        </div>
      </CardHeader>
      <CardContent className='space-y-2'>
        <div className='text-2xl font-semibold tracking-normal'>
          {item.value}
        </div>
        <div className='flex min-h-5 items-center gap-2 text-xs text-muted-foreground'>
          {item.trend !== undefined && (
            <span
              className={cn(
                'inline-flex items-center gap-1 font-medium',
                item.trend > 0 && 'text-emerald-600',
                item.trend < 0 && 'text-rose-600'
              )}
            >
              <TrendIcon className='h-3.5 w-3.5' />
              {formatPercent(Math.abs(item.trend))}
            </span>
          )}
          <span>{item.detail}</span>
        </div>
      </CardContent>
    </Card>
  );

  if (!item.href) {
    return content;
  }

  return (
    <Link href={item.href} className='block h-full'>
      {content}
    </Link>
  );
}
