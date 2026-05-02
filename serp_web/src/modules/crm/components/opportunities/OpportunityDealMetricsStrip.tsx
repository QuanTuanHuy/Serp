// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import { cn } from '@/shared/utils';

interface OpportunityDealMetricsStripProps {
  estimatedValue: number;
  weightedValue: number;
  probability: number;
  daysUntilClose: number;
  formatCurrency: (value?: number) => string;
}

export const OpportunityDealMetricsStrip: React.FC<
  OpportunityDealMetricsStripProps
> = ({
  estimatedValue,
  weightedValue,
  probability,
  daysUntilClose,
  formatCurrency,
}) => {
  const daysDisplay =
    daysUntilClose < 0
      ? `${Math.abs(daysUntilClose)} overdue`
      : String(daysUntilClose);

  return (
    <div className='grid grid-cols-2 gap-4 sm:grid-cols-4'>
      <div>
        <p className='text-xs text-muted-foreground'>Deal value</p>
        <p className='text-lg font-semibold tabular-nums tracking-tight'>
          {formatCurrency(estimatedValue)}
        </p>
      </div>
      <div>
        <p className='text-xs text-muted-foreground'>Weighted</p>
        <p className='text-lg font-semibold tabular-nums tracking-tight'>
          {formatCurrency(weightedValue)}
        </p>
      </div>
      <div>
        <p className='text-xs text-muted-foreground'>Probability</p>
        <p className='text-lg font-semibold tabular-nums tracking-tight'>
          {probability}%
        </p>
      </div>
      <div>
        <p className='text-xs text-muted-foreground'>Days to close</p>
        <p
          className={cn(
            'text-lg font-semibold tabular-nums tracking-tight',
            daysUntilClose < 0 && 'text-destructive'
          )}
        >
          {daysDisplay}
        </p>
      </div>
    </div>
  );
};
