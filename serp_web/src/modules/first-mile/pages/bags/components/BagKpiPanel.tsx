/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag KPI panel
 */

import { PackageCheck, Scale, Boxes, ListChecks } from 'lucide-react';

import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';

import type { SecondMileBaggingKpi } from '../../../types';
import { formatNumber, formatPercent } from '../bagPageModels';

interface BagKpiPanelProps {
  data?: SecondMileBaggingKpi;
  isFetching?: boolean;
}

export function BagKpiPanel({ data, isFetching }: BagKpiPanelProps) {
  const metrics = [
    {
      label: 'Sealed bags',
      value: formatNumber(data?.sealedBagCount, 0),
      icon: PackageCheck,
    },
    {
      label: 'Weight fill',
      value: formatPercent(data?.avgFillRateWeight),
      icon: Scale,
    },
    {
      label: 'Volume fill',
      value: formatPercent(data?.avgFillRateVolume),
      icon: Boxes,
    },
    {
      label: 'Orders per bag',
      value: formatNumber(data?.avgOrdersPerBag, 1),
      icon: ListChecks,
    },
  ];

  return (
    <div className='grid gap-3 md:grid-cols-4'>
      {metrics.map((metric) => {
        const Icon = metric.icon;
        return (
          <Card key={metric.label}>
            <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
              <CardTitle className='text-sm font-medium'>
                {metric.label}
              </CardTitle>
              <Icon className='h-4 w-4 text-muted-foreground' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-semibold'>
                {isFetching ? '-' : metric.value}
              </div>
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}
