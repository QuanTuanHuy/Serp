/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM dependency summary strip
 */

'use client';

import { AlertTriangle, GitBranch, Link2, ShieldAlert } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui';
import type { PMWorkItemDependencySummaryApi } from '../../../types/api';

interface PMProjectDependenciesSummaryProps {
  summary?: PMWorkItemDependencySummaryApi;
  isLoading?: boolean;
}

export function PMProjectDependenciesSummary({
  summary,
  isLoading,
}: PMProjectDependenciesSummaryProps) {
  const values = [
    {
      label: 'Dependencies',
      value: summary?.dependencyCount ?? 0,
      icon: GitBranch,
    },
    {
      label: 'Blockers',
      value: summary?.blockerCount ?? 0,
      icon: ShieldAlert,
    },
    {
      label: 'Blocked',
      value: summary?.blockedItemCount ?? 0,
      icon: AlertTriangle,
    },
    {
      label: 'Outside',
      value: summary?.outsideDependencyCount ?? 0,
      icon: Link2,
    },
    {
      label: 'Related',
      value: summary?.relatedLinkCount ?? 0,
      icon: Link2,
    },
    {
      label: 'Cycles',
      value: summary?.cycleCount ?? 0,
      icon: AlertTriangle,
    },
  ];

  return (
    <div className='grid gap-3 sm:grid-cols-2 lg:grid-cols-6'>
      {values.map((item) => {
        const Icon = item.icon;
        return (
          <Card key={item.label} className='shadow-sm'>
            <CardContent className='flex items-center justify-between gap-3 p-4'>
              <div className='min-w-0'>
                <p className='text-xs font-medium text-muted-foreground'>
                  {item.label}
                </p>
                <p className='mt-1 text-2xl font-semibold'>
                  {isLoading ? '-' : item.value}
                </p>
              </div>
              <Icon className='h-4 w-4 shrink-0 text-muted-foreground' />
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}
