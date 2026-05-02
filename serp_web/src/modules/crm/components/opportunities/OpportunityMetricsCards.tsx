// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import { Card, CardContent } from '@/shared/components/ui';
import { DollarSign, Target, Percent, Clock } from 'lucide-react';
import { cn } from '@/shared/utils';

interface OpportunityMetricsCardsProps {
  estimatedValue: number;
  weightedValue: number;
  probability: number;
  daysUntilClose: number;
  formatCurrency: (value?: number) => string;
}

export const OpportunityMetricsCards: React.FC<
  OpportunityMetricsCardsProps
> = ({
  estimatedValue,
  weightedValue,
  probability,
  daysUntilClose,
  formatCurrency,
}) => {
  return (
    <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
      <Card className='border-green-200 bg-gradient-to-br from-green-50 to-green-100/50 dark:border-green-800/50 dark:from-green-950 dark:to-green-900/50'>
        <CardContent className='py-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Deal Value</p>
              <p className='text-2xl font-bold text-green-700 dark:text-green-300'>
                {formatCurrency(estimatedValue)}
              </p>
            </div>
            <div className='rounded-full bg-green-500/20 p-3'>
              <DollarSign className='h-6 w-6 text-green-600 dark:text-green-400' />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className='border-blue-200 bg-gradient-to-br from-blue-50 to-blue-100/50 dark:border-blue-800/50 dark:from-blue-950 dark:to-blue-900/50'>
        <CardContent className='py-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Weighted Value</p>
              <p className='text-2xl font-bold text-blue-700 dark:text-blue-300'>
                {formatCurrency(weightedValue)}
              </p>
            </div>
            <div className='rounded-full bg-blue-500/20 p-3'>
              <Target className='h-6 w-6 text-blue-600 dark:text-blue-400' />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className='border-purple-200 bg-gradient-to-br from-purple-50 to-purple-100/50 dark:border-purple-800/50 dark:from-purple-950 dark:to-purple-900/50'>
        <CardContent className='py-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Probability</p>
              <p className='text-2xl font-bold text-purple-700 dark:text-purple-300'>
                {probability}%
              </p>
            </div>
            <div className='rounded-full bg-purple-500/20 p-3'>
              <Percent className='h-6 w-6 text-purple-600 dark:text-purple-400' />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className='border-orange-200 bg-gradient-to-br from-orange-50 to-orange-100/50 dark:border-orange-800/50 dark:from-orange-950 dark:to-orange-900/50'>
        <CardContent className='py-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Days to Close</p>
              <p
                className={cn(
                  'text-2xl font-bold',
                  daysUntilClose < 0
                    ? 'text-red-700 dark:text-red-300'
                    : 'text-orange-700 dark:text-orange-300'
                )}
              >
                {daysUntilClose < 0
                  ? `${Math.abs(daysUntilClose)} overdue`
                  : daysUntilClose}
              </p>
            </div>
            <div className='rounded-full bg-orange-500/20 p-3'>
              <Clock className='h-6 w-6 text-orange-600 dark:text-orange-400' />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
