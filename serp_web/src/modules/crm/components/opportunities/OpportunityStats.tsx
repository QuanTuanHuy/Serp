// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import { TrendingUp, DollarSign, Trophy, Target } from 'lucide-react';
import { StatsCard } from '../dashboard';

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
        value={`$${Math.round(totalValue).toLocaleString()}`}
        icon={TrendingUp}
        variant='primary'
      />
      <StatsCard
        title='Weighted Value'
        value={`$${Math.round(weightedValue).toLocaleString()}`}
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
        value={`$${Math.round(avgDealSize).toLocaleString()}`}
        icon={Target}
        variant='default'
      />
    </div>
  );
};
