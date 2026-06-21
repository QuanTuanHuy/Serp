// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import { TrendingUp, DollarSign, Trophy, Target } from 'lucide-react';
import { StatsCard } from '../dashboard';
import { formatCurrency } from '../../utils';

interface OpportunityStatsProps {
  totalValue: number;
  weightedValue: number;
  wonCount: number;
  avgDealSize: number;
}

export const OpportunityStats: React.FC<OpportunityStatsProps> = ({
  totalValue,
  weightedValue,
  wonCount,
  avgDealSize,
}) => {
  return (
    <div className='grid grid-cols-2 gap-4 sm:grid-cols-4'>
      <StatsCard
        title='Total Pipeline'
        value={formatCurrency(totalValue)}
        icon={TrendingUp}
        variant='primary'
      />
      <StatsCard
        title='Weighted Value'
        value={formatCurrency(weightedValue)}
        icon={DollarSign}
        variant='warning'
      />
      <StatsCard
        title='Won Deals'
        value={wonCount}
        icon={Trophy}
        variant='success'
      />
      <StatsCard
        title='Avg. Deal Size'
        value={formatCurrency(avgDealSize)}
        icon={Target}
        variant='default'
      />
    </div>
  );
};
