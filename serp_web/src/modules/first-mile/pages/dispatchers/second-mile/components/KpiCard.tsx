/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution KPI card
 */

import type { ComponentType } from 'react';

import { Card, CardContent } from '@/shared/components/ui/card';

interface KpiCardProps {
  icon: ComponentType<{ className?: string }>;
  label: string;
  value: string;
  hint: string;
}

export function KpiCard({ icon: Icon, label, value, hint }: KpiCardProps) {
  return (
    <Card>
      <CardContent className='flex items-center gap-3 p-4'>
        <div className='flex h-9 w-9 items-center justify-center rounded-md bg-primary/10 text-primary'>
          <Icon className='h-4 w-4' />
        </div>
        <div>
          <p className='text-xs text-muted-foreground'>{label}</p>
          <p className='text-xl font-semibold'>{value}</p>
          <p className='text-xs text-muted-foreground'>{hint}</p>
        </div>
      </CardContent>
    </Card>
  );
}
